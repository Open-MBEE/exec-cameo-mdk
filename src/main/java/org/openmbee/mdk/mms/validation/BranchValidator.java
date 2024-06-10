package org.openmbee.mdk.mms.validation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.actions.ActionsCategory;
import com.nomagic.ci.persistence.IPrimaryProject;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.core.project.ProjectDescriptorsFactory;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;

import org.openmbee.mdk.actions.ClipboardAction;
import org.openmbee.mdk.api.incubating.MDKConstants;
import org.openmbee.mdk.api.incubating.convert.Converters;
import org.openmbee.mdk.http.ServerException;
import org.openmbee.mdk.json.JacksonUtils;
import org.openmbee.mdk.mms.MMSUtils;
import org.openmbee.mdk.mms.actions.CommitBranchAction;
import org.openmbee.mdk.mms.endpoints.MMSEndpointBuilderConstants;
import org.openmbee.mdk.mms.endpoints.MMSRefsEndpoint;
import org.openmbee.mdk.mms.json.JsonPatchFunction;
import org.openmbee.mdk.util.MDUtils;
import org.openmbee.mdk.util.Pair;
import org.openmbee.mdk.util.Utils;
import org.openmbee.mdk.validation.ValidationRule;
import org.openmbee.mdk.validation.ValidationRuleViolation;
import org.openmbee.mdk.validation.ValidationSuite;
import org.openmbee.mdk.validation.ViolationSeverity;
import org.apache.http.client.methods.HttpRequestBase;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.text.NumberFormat;
import java.util.*;

public class BranchValidator implements RunnableWithProgress {

    private final Project project;
    private boolean allBranches;
    private boolean errors;
    private ValidationSuite validationSuite = new ValidationSuite("structure");
    //    private ValidationRule twcMissingBranchValidationRule = new ValidationRule("Missing in Client", "Branch shall exist in TWC if it exists in MMS.", ViolationSeverity.WARNING);
    private ValidationRule mmsMissingBranchValidationRule = new ValidationRule("Missing on Server", "Branch shall exist in MMS if it exists in Teamwork Cloud.", ViolationSeverity.ERROR);
    private ValidationRule branchEquivalenceValidationRule = new ValidationRule("Branch Equivalence", "Branch shall be represented in MagicDraw and MMS equivalently.", ViolationSeverity.ERROR);
    private ValidationRule mmsBuildingBranchValidationRule = new ValidationRule("Building on Server", "Branch shall be completely built on MMS before it is used.", ViolationSeverity.WARNING);

    public BranchValidator(Project project, boolean allBranches) {
        this.project = project;
        this.allBranches = allBranches;
//        validationSuite.addValidationRule(twcMissingBranchValidationRule);
        validationSuite.addValidationRule(mmsMissingBranchValidationRule);
        validationSuite.addValidationRule(branchEquivalenceValidationRule);
        validationSuite.addValidationRule(mmsBuildingBranchValidationRule);
    }

    public void run(ProgressStatus progressStatus) {
        if (project == null) {
            return;
        }
        IPrimaryProject primaryProject = project.getPrimaryProject();

        if (!ProjectUtilities.isRemote(primaryProject)) {
            return;
        }
        if (EsiUtils.getLoggedUserName() == null) {
            errors = true;
            Utils.guilog("[INFO] You need to be logged in to Teamwork Cloud first to do branch validation. Aborting.");
            return;
        }

        String currentBranch = MDUtils.getBranchId(project);
        Map<String, Pair<EsiUtils.EsiBranchInfo, ObjectNode>> clientBranches = new HashMap<>();
        Map<String, ObjectNode> serverBranches = new HashMap<>();

        if (progressStatus != null) {
            progressStatus.setDescription("Mapping Teamwork Cloud branches");
            progressStatus.setIndeterminate(true);
        }

        Collection<EsiUtils.EsiBranchInfo> targetBranches = null;
        if (this.allBranches) {
            try {
                ProjectDescriptor projectDescriptor = ProjectDescriptorsFactory.createAnyRemoteProjectDescriptor(project);
                targetBranches = EsiUtils.getBranches(projectDescriptor);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (targetBranches == null || targetBranches.isEmpty()) {
                return;
            }
        }
        else {
            targetBranches = new ArrayList<>(1);
            targetBranches.add(EsiUtils.getCurrentBranch(primaryProject));
        }
        for (EsiUtils.EsiBranchInfo branch : targetBranches) {
            ObjectNode branchJson = generateRefObjectNode(project, branch, null);
            if (branchJson == null) {
                continue;
            }
            branchJson.remove(MDKConstants.PARENT_REF_ID_KEY);
            JsonNode value;
            String entryKey;
            if ((value = branchJson.get(MDKConstants.ID_KEY)) != null && value.isTextual()) {
                entryKey = value.asText();
                if (this.allBranches || entryKey.equals(currentBranch)) {
                    clientBranches.put(entryKey, new Pair<>(branch, branchJson));
                }
            }
        }

        if (progressStatus != null) {
            progressStatus.setDescription("Mapping MMS branches");
        }

        try {
            String projectId = Converters.getIProjectToIdConverter().apply(project.getPrimaryProject());
            HttpRequestBase refsRequest = MMSUtils.prepareEndpointBuilderBasicGet(MMSRefsEndpoint.builder(), project)
                    .addParam(MMSEndpointBuilderConstants.URI_PROJECT_SUFFIX, projectId).build();
            File responseFile = MMSUtils.sendMMSRequest(project, refsRequest);
            ObjectNode response;
            try (JsonParser jsonParser = JacksonUtils.getJsonFactory().createParser(responseFile)) {
                response = JacksonUtils.parseJsonObject(jsonParser);
            }
            JsonNode refsArray, value;
            if ((refsArray = response.get("refs")) != null && refsArray.isArray()) {
                for (JsonNode refJson : refsArray) {
                    if (refJson.isObject()) {
                        ObjectNode refObjectNode = (ObjectNode) refJson;
                        refObjectNode.remove(MDKConstants.PARENT_REF_ID_KEY);
                        String entryKey;
                        if ((value = refObjectNode.get(MDKConstants.ID_KEY)) != null && value.isTextual()) {
                            entryKey = value.asText();
                            if (this.allBranches || entryKey.equals(currentBranch)) {
                                serverBranches.put(entryKey, refObjectNode);
                            }
                        }
                    }
                }
            }
        } catch (IOException | URISyntaxException | ServerException | GeneralSecurityException e) {
            errors = true;
            e.printStackTrace();
            Application.getInstance().getGUILog().log("[ERROR] An error occurred while getting MMS branches. Branch validation aborted. Reason: " + e.getMessage());
            return;
        }

        Set<String> keySet = new HashSet<>();
        keySet.addAll(clientBranches.keySet());
        keySet.addAll(serverBranches.keySet());

        if (progressStatus != null) {
            progressStatus.setDescription("Generating validation results for " + NumberFormat.getInstance().format(keySet.size()) + " branch" + (keySet.size() != 1 ? "es" : ""));
            progressStatus.setIndeterminate(false);
            progressStatus.setMax(keySet.size());
            progressStatus.setCurrent(0);
        }

        for (String key : keySet) {
            Pair<EsiUtils.EsiBranchInfo, ObjectNode> clientBranch = clientBranches.get(key);
            ObjectNode serverBranch = serverBranches.get(key);

            if (clientBranch == null) {
                //TODO @donbot 3.0.1 add support for importing MMS branch into TWC
//                ValidationRuleViolation v = new ValidationRuleViolation(project.getPrimaryModel(), "[BRANCH MISSING ON MMS] The MMS branch \"" + key + "\" does not have a corresponding Teamwork Cloud branch.");
//                // add actions here
//                twcMissingBranchValidationRule.addViolation(v);
            }
            else if (serverBranch == null) {
                ValidationRuleViolation v = new ValidationRuleViolation(project.getPrimaryModel(), "[BRANCH MISSING ON MMS] The Teamwork Cloud branch \"" + clientBranch.getKey().getName() + "\" does not have a corresponding MMS branch.");
                v.addAction(new CommitBranchAction(key, project, clientBranch.getKey(), false));
                v.addAction(new CommitBranchAction(key, project, clientBranch.getKey(), true));
                mmsMissingBranchValidationRule.addViolation(v);
            }
            else {
                JsonNode statusNode;
                if ((statusNode = serverBranch.get(MDKConstants.STATUS_KEY)) != null && statusNode.isTextual() && !statusNode.asText().equals(MDKConstants.REF_CREATED_STATUS)) {
                    ValidationRuleViolation v = new ValidationRuleViolation(project.getPrimaryModel(), "[BRANCH BUILDING ON MMS] The Teamwork Cloud branch \"" + clientBranch.getKey().getName() + "\" is still being built on MMS and is not ready for use.");
                    mmsBuildingBranchValidationRule.addViolation(v);
                    continue;
                }
                serverBranch.remove(MDKConstants.STATUS_KEY);
                serverBranch.remove(MDKConstants.PARENT_COMMIT_ID);

                clientBranch.getValue().put(MDKConstants.ARCHIVED_FIELD, false);
                if (serverBranch.get(MDKConstants.DELETED_FIELD) != null) {
                    serverBranch.set(MDKConstants.ARCHIVED_FIELD, serverBranch.get(MDKConstants.DELETED_FIELD));
                    serverBranch.remove(MDKConstants.DELETED_FIELD);
                }
                JsonNode diff = JsonPatchFunction.getInstance().apply(clientBranch.getValue(), serverBranch);
                if (diff != null && diff.isArray() && diff.size() > 0) {
                    ValidationRuleViolation v = new ValidationRuleViolation(project.getPrimaryModel(), "[BRANCH NOT EQUIVALENT] The Teamwork Cloud branch \"" + clientBranch.getKey().getName() + "\" is not equivalent to the corresponding MMS branch.");
                    v.addAction(new CommitBranchAction(key, project, clientBranch.getKey(), false));
                    v.addAction(new CommitBranchAction(key, project, clientBranch.getKey(), true));
                    ActionsCategory copyActionsCategory = new ActionsCategory("COPY", "Copy...");
                    copyActionsCategory.setNested(true);
                    v.addAction(copyActionsCategory);
                    try {
                        copyActionsCategory.addAction(new ClipboardAction("Diff", JacksonUtils.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(diff)));
                    } catch (JsonProcessingException ignored) {
                    }
                    branchEquivalenceValidationRule.addViolation(v);
                    continue;
                }
            }
            if (progressStatus != null) {
                progressStatus.increase();
            }
        }
    }

    public static ObjectNode generateRefObjectNode(Project project, EsiUtils.EsiBranchInfo branchInfo, String parentRefId) {
        ObjectNode refObjectNode = JacksonUtils.getObjectMapper().createObjectNode();
        String name = branchInfo.getName();
        if (name.equals("master")) {
            return null;
        }
        if (name.equals("trunk")) {
            name = "master";
        }
        String twcId = !name.equals("master") ? branchInfo.getID().toString() : "master";
        refObjectNode.put(MDKConstants.ID_KEY, twcId);
        refObjectNode.put(MDKConstants.TWC_ID_KEY, twcId);
        refObjectNode.put(MDKConstants.NAME_KEY, name);
        refObjectNode.put(MDKConstants.TYPE_KEY, "Branch");
        refObjectNode.put(MDKConstants.PARENT_REF_ID_KEY, parentRefId);
        return refObjectNode;
    }

    public boolean hasErrors() {
        return this.errors;
    }

    public void showWindow() {
        List<ValidationSuite> vss = new ArrayList<>();
        vss.add(validationSuite);
        if (validationSuite.hasErrors()) {
            Utils.displayValidationWindow(project, vss, "Branch Differences");
        }
        else {
            Application.getInstance().getGUILog().log("[INFO] All branches are in sync between TWC and MMS.");
        }
    }

    public ValidationSuite getValidationSuite() {
        return validationSuite;
    }
}
