package gov.nasa.jpl.mbee.mdk.mms.validation;

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
import gov.nasa.jpl.mbee.mdk.actions.ClipboardAction;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.mms.actions.CommitBranchAction;
import gov.nasa.jpl.mbee.mdk.mms.json.JsonPatchFunction;
import gov.nasa.jpl.mbee.mdk.util.Pair;
import gov.nasa.jpl.mbee.mdk.util.Utils;
import gov.nasa.jpl.mbee.mdk.validation.ValidationRule;
import gov.nasa.jpl.mbee.mdk.validation.ValidationRuleViolation;
import gov.nasa.jpl.mbee.mdk.validation.ValidationSuite;
import gov.nasa.jpl.mbee.mdk.validation.ViolationSeverity;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.*;

public class BranchValidator {

    private final Project project;
    private boolean errors;
    private ValidationSuite validationSuite = new ValidationSuite("structure");
    //    private ValidationRule twcMissingBranchValidationRule = new ValidationRule("Missing in Client", "Branch shall exist in TWC if it exists in MMS.", ViolationSeverity.WARNING);
    private ValidationRule mmsMissingBranchValidationRule = new ValidationRule("Missing on Server", "Branch shall exist in MMS if it exists in Teamwork Cloud.", ViolationSeverity.ERROR);
    private ValidationRule branchEquivalenceValidationRule = new ValidationRule("Branch Equivalence", "Branch shall be represented in MagicDraw and MMS equivalently.", ViolationSeverity.ERROR);
    private ValidationRule mmsBuildingBranchValidationRule = new ValidationRule("Building on Server", "Branch shall be completely built on MMS before it is used.", ViolationSeverity.WARNING);

    public BranchValidator(Project project) {
        this.project = project;
//        validationSuite.addValidationRule(twcMissingBranchValidationRule);
        validationSuite.addValidationRule(mmsMissingBranchValidationRule);
        validationSuite.addValidationRule(branchEquivalenceValidationRule);
        validationSuite.addValidationRule(mmsBuildingBranchValidationRule);
    }

    public void validate(ProgressStatus progressStatus, boolean allBranches) {
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

        String currentBranch = EsiUtils.getCurrentBranch(primaryProject).getName();
        if (currentBranch.equals("trunk")) {
            currentBranch = "master";
        }
        Map<String, Pair<EsiUtils.EsiBranchInfo, ObjectNode>> clientBranches = new HashMap<>();
        Map<String, ObjectNode> serverBranches = new HashMap<>();

        if (progressStatus != null) {
            progressStatus.setDescription("Mapping Teamwork Cloud branches");
            progressStatus.setIndeterminate(true);
        }

        Collection<EsiUtils.EsiBranchInfo> targetBranches = null;
        if (allBranches) {
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
            if ((value = branchJson.get(MDKConstants.NAME_KEY)) != null && value.isTextual()) {
                entryKey = value.asText();
                if (allBranches || entryKey.equals(currentBranch)) {
                    clientBranches.put(entryKey, new Pair<>(branch, branchJson));
                }
            }
        }

        if (progressStatus != null) {
            progressStatus.setDescription("Mapping MMS branches");
        }

        URIBuilder requestUri = MMSUtils.getServiceProjectsRefsUri(project);
        if (requestUri == null) {
            errors = true;
            Application.getInstance().getGUILog().log("[ERROR] Unable to get MMS URL. Branch validation aborted.");
            return;
        }
        try {
            HttpRequestBase request = MMSUtils.buildRequest(MMSUtils.HttpRequestType.GET, requestUri);
            File responseFile = MMSUtils.sendMMSRequest(project, request);
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
                        if ((value = refObjectNode.get(MDKConstants.NAME_KEY)) != null && value.isTextual()) {
                            entryKey = value.asText();
                            if (allBranches || entryKey.equals(currentBranch)) {
                                serverBranches.put(entryKey, refObjectNode);
                            }
                        }
                    }
                }
            }
        } catch (IOException | URISyntaxException | ServerException e) {
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
            // TODO @DONBOT 3.0.1 remove this check/skip for master branch after master is updatable
            if (key.equals("master")) {
                continue;
            }

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
    }

    public ValidationSuite getValidationSuite() {
        return validationSuite;
    }
}
