package gov.nasa.jpl.mbee.mdk.ems.sync.manual;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRule;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ViolationSeverity;
import gov.nasa.jpl.mbee.mdk.ems.MMSUtils;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
import gov.nasa.jpl.mbee.mdk.ems.actions.CommitProjectAction;
import gov.nasa.jpl.mbee.mdk.ems.validation.ElementValidator;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.lib.MDUtils;
import gov.nasa.jpl.mbee.mdk.lib.Pair;
import io.vertx.core.json.Json;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by igomes on 9/26/16.
 */
public class ManualSyncRunner implements RunnableWithProgress {

    public static final String INITIALIZE_PROJECT_COMMENT = "The project doesn't exist on the web.";
    private final Collection<Element> rootElements;
    private final Project project;
    private int depth;

    // TODO Move me to common sync pre-conditions @donbot
    private ValidationSuite validationSuite = new ValidationSuite("Manual Sync Validation");
    private ValidationRule projectExistenceValidationRule = new ValidationRule("Project Existence", "The project shall exist in the specified site.", ViolationSeverity.ERROR);

    {
        validationSuite.addValidationRule(projectExistenceValidationRule);
    }

    private ElementValidator elementValidator;

    public ManualSyncRunner(Collection<Element> rootElements, Project project, int depth) {
        this.rootElements = rootElements;
        this.project = project;
        this.depth = depth;
    }

    @Override
    public void run(ProgressStatus progressStatus) {
        progressStatus.setDescription("Validating sync pre-conditions");
        progressStatus.setIndeterminate(true);
        try {
            if (!checkProject()) {
                if (validationSuite.hasErrors()) {
                    validationSuite.setName("Sync Pre-Condition Validation");
                }
                else {
                    Application.getInstance().getGUILog().log("[ERROR] Project does not exist but no validation errors generated.");
                }
                return;
            }
        }  catch (IOException | URISyntaxException | ServerException e) {
            Application.getInstance().getGUILog().log("[ERROR] Exception occurred while locating project on MMS. Reason: " + e.getMessage());
            e.printStackTrace();
            validationSuite = null;
            return;
        }

        progressStatus.setDescription("Processing and querying for " + rootElements.size() + " " + ((depth != 0) ? "root " : "") + "element" + (rootElements.size() != 1 ? "s" : ""));
        progressStatus.setIndeterminate(false);
        progressStatus.setMax(rootElements.size());
        progressStatus.setCurrent(0);

        List<Pair<Element, ObjectNode>> clientElements = new ArrayList<>(rootElements.size());
        Collection<JsonParser> jsonParsers = new ArrayList<>(3);
        for (Element element : rootElements) {
            collectClientElementsRecursively(project, element, depth, clientElements);
            try {
                jsonParsers.add(collectServerElementsRecursively(project, element, depth, progressStatus));
                if (element == project.getPrimaryModel() && depth != 0) {
                    // scan of initial return for holding bin is expensive. assume it's not there and request anyway
                    if (progressStatus.isCancel()) {
                        Application.getInstance().getGUILog().log("[INFO] Manual sync cancelled by user. Aborting.");
                        return;
                    }
                    jsonParsers.add(collectServerHoldingBinElementsRecursively(project, depth - 1, progressStatus));

                    if (progressStatus.isCancel()) {
                        Application.getInstance().getGUILog().log("[INFO] Manual sync cancelled by user. Aborting.");
                        return;
                    }
                    //TODO confirm depth 0 for modules
                    jsonParsers.add(collectServerModuleElementsRecursively(project, 0, progressStatus));
                }
            } catch (ServerException | URISyntaxException | IOException e) {
                Application.getInstance().getGUILog().log("[ERROR] Exception occurred while getting elements from the server. Aborting manual sync.");
                e.printStackTrace();
                validationSuite = null;
                return;
            }
            if (jsonParsers.isEmpty()) {
                if (!progressStatus.isCancel()) {
                    Application.getInstance().getGUILog().log("[ERROR] Failed to get elements from the server. Aborting manual sync.");
                }
                validationSuite = null;
                return;
            }
            progressStatus.increase();
        }
        if (progressStatus.isCancel()) {
            Application.getInstance().getGUILog().log("[INFO] Manual sync cancelled by user. Aborting.");
            return;
        }
        elementValidator = new ElementValidator(clientElements, null, jsonParsers, project);
        elementValidator.run(progressStatus);
    }

    private static void collectClientElementsRecursively(Project project, Element element, int depth, List<Pair<Element, ObjectNode>> elements) {
        ObjectNode jsonObject = Converters.getElementToJsonConverter().apply(element, project);
        if (jsonObject == null) {
            return;
        }
        elements.add(new Pair<>(element, jsonObject));
        if (depth != 0) {
            for (Element elementChild : element.getOwnedElement()) {
                collectClientElementsRecursively(project, elementChild, --depth, elements);
            }
        }
        if (element.equals(project.getPrimaryModel())) {
            List<Package> attachedModels = project.getModels();
            attachedModels.remove(project.getPrimaryModel());
//            final int moduleDepth = depth;
            // TODO why depth 0?!?!?!
            attachedModels.forEach(attachedModel -> collectClientElementsRecursively(project, attachedModel, 0, elements));
        }
    }

    private static /*Collection<ObjectNode>*/ JsonParser collectServerElementsRecursively(Project project, Element element, int depth, ProgressStatus progressStatus)
            throws ServerException, IOException, URISyntaxException {
        String id = Converters.getElementToIdConverter().apply(element);
        Collection<String> elementIds = new ArrayList<>(1);
        elementIds.add(id);
        return MMSUtils.getElementsRecursively(project, elementIds, depth, progressStatus);
//        // TODO @donbot stream this better
//        ObjectNode response = JacksonUtils.parseJsonObject(MMSUtils.getElementsRecursively(project, elementIds, depth, progressStatus));
//        // process response
//        JsonNode value;
//        if (response != null && (value = response.get("elements")) != null && value.isArray()) {
//            Collection<ObjectNode> serverElements = StreamSupport.stream(value.spliterator(), false)
//                    .filter(JsonNode::isObject).map(jsonNode -> (ObjectNode) jsonNode).collect(Collectors.toList());
//
//            // check if we're validating the model root
//            if (id.equals(Converters.getElementToIdConverter().apply(project.getPrimaryModel()))) {
//                if (depth != 0) {
//                    Collection<Element> attachedModels = new ArrayList<>(project.getModels());
//                    attachedModels.remove(project.getPrimaryModel());
//                    Collection<String> attachedModelIds = attachedModels.stream().map(Converters.getElementToIdConverter())
//                            .filter(amId -> amId != null).collect(Collectors.toList());
//                    // TODO @donbot stream this better
//                    // TODO why depth 0?!?!?!
//                    response = JacksonUtils.parseJsonObject(MMSUtils.getElements(project, attachedModelIds, null));
//                    if (response != null && (value = response.get("elements")) != null && value.isArray()) {
//                        serverElements.addAll(StreamSupport.stream(value.spliterator(), false)
//                                .filter(JsonNode::isObject).map(jsonNode -> (ObjectNode) jsonNode).collect(Collectors.toList()));
//                    }
//
//                    String holdingBinId = "holding_bin_" + Converters.getIProjectToIdConverter().apply(project.getPrimaryProject());
//                    boolean found = false;
//                    // check to see if the holding bin was returned
//                    for (ObjectNode elem : serverElements) {
//                        if ((value = elem.get(MDKConstants.ID_KEY)) != null && value.isTextual()
//                                && value.asText().equals(holdingBinId)) {
//                            found = true;
//                            break;
//                        }
//                    }
//                    // if no holding bin in server collection && model was element && (depth > 0 || depth == -1)
//                    if (!found) {
//                        // TODO @donbot stream this better
//                        response = JacksonUtils.parseJsonObject(MMSUtils.getElementRecursively(project, holdingBinId, depth, progressStatus));
//                        if (response != null && (value = response.get("elements")) != null && value.isArray()) {
//                            serverElements.addAll(StreamSupport.stream(value.spliterator(), false)
//                                    .filter(JsonNode::isObject).map(jsonNode -> (ObjectNode) jsonNode).collect(Collectors.toList()));
//                        }
//                    }
//                }
//            }
//            return serverElements;
//        }
//        return new ArrayList<>();
    }

    private static JsonParser collectServerModuleElementsRecursively(Project project, int depth, ProgressStatus progressStatus)
            throws ServerException, IOException, URISyntaxException {
        Collection<Element> attachedModels = new ArrayList<>(project.getModels());
        attachedModels.remove(project.getPrimaryModel());
        Collection<String> attachedModelIds = attachedModels.stream().map(Converters.getElementToIdConverter()).filter(amId -> amId != null).collect(Collectors.toList());
        // TODO why depth 0?!?!?!
        return MMSUtils.getElements(project, attachedModelIds, null);
    }

    private static JsonParser collectServerHoldingBinElementsRecursively(Project project, int depth, ProgressStatus progressStatus)
            throws ServerException, IOException, URISyntaxException {
        String holdingBinId = "holding_bin_" + Converters.getIProjectToIdConverter().apply(project.getPrimaryProject());
        return MMSUtils.getElementRecursively(project, holdingBinId, depth, progressStatus);
    }

    // TODO Make common across all sync types @donbot
    private boolean checkProject() throws ServerException, IOException, URISyntaxException {
        String branch = MDUtils.getWorkspace(project);

        // process response for project element, missing projects will return {}
        if (!MMSUtils.isProjectOnMms(project)) {
            ValidationRuleViolation v;

            if (branch.equals("master")) {
                v = new ValidationRuleViolation(project.getPrimaryModel(), INITIALIZE_PROJECT_COMMENT);
                v.addAction(new CommitProjectAction(project, true));
            } else {
                v = new ValidationRuleViolation(project.getPrimaryModel(), "The trunk project doesn't exist on the web. Export the trunk first.");
            }
            projectExistenceValidationRule.addViolation(v);
            return false;
        }

        if (!branch.equals("master") && !MMSUtils.isBranchOnMms(project, branch)) {
            ValidationRuleViolation v = new ValidationRuleViolation(project.getPrimaryModel(), "The branch doesn't exist on the web. Export the trunk first.");
            // TODO @donbot re-add branch creation
//            v.addAction(new CommitBranchAction(project, true));
            projectExistenceValidationRule.addViolation(v);
            return false;
        }

        return true;
    }

    public ValidationSuite getValidationSuite() {
        if (validationSuite == null) {
            return null;
        }
        return validationSuite.hasErrors() ? validationSuite : (elementValidator != null ? elementValidator.getValidationSuite() : null);
    }
}

