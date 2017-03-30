package gov.nasa.jpl.mbee.mdk.ems.sync.manual;

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
import gov.nasa.jpl.mbee.mdk.ems.validation.BranchValidator;
import gov.nasa.jpl.mbee.mdk.ems.validation.ElementValidator;
import gov.nasa.jpl.mbee.mdk.ems.validation.ProjectValidator;
import gov.nasa.jpl.mbee.mdk.lib.MDUtils;
import gov.nasa.jpl.mbee.mdk.lib.Pair;

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

    private final Collection<Element> rootElements;
    private final Project project;
    private final int depth;

    // TODO Move me to common sync pre-conditions @donbot
    private ValidationSuite validationSuite = new ValidationSuite("Manual Sync Validation");
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
        List<ObjectNode> serverElements = new ArrayList<>(rootElements.size());
        for (Element element : rootElements) {
            collectClientElementsRecursively(project, element, depth, clientElements);
            Collection<ObjectNode> jsonObjects = null;
            try {
                jsonObjects = collectServerElementsRecursively(project, element, depth, progressStatus);
            } catch (ServerException | URISyntaxException | IOException e) {
                Application.getInstance().getGUILog().log("[ERROR] Exception occurred while getting elements from the server. Aborting manual sync.");
                e.printStackTrace();
                validationSuite = null;
                return;
            }
            if (jsonObjects == null) {
                if (!progressStatus.isCancel()) {
                    Application.getInstance().getGUILog().log("[ERROR] Failed to get elements from the server. Aborting manual sync.");
                }
                validationSuite = null;
                return;
            }
            serverElements.addAll(jsonObjects);
            progressStatus.increase();
        }
        if (progressStatus.isCancel()) {
            Application.getInstance().getGUILog().log("[INFO] Manual sync cancelled by user. Aborting.");
            return;
        }
        elementValidator = new ElementValidator(clientElements, serverElements, project);
        elementValidator.run(progressStatus);
    }

    private static void collectClientElementsRecursively(Project project, Element element, int depth, List<Pair<Element, ObjectNode>> elements) {
        ObjectNode jsonObject = Converters.getElementToJsonConverter().apply(element, project);
        if (jsonObject == null) {
            return;
        }
        elements.add(new Pair<>(element, jsonObject));
        if (element.equals(project.getPrimaryModel())) {
            List<Package> attachedModels = project.getModels();
            attachedModels.remove(project.getPrimaryModel());
            attachedModels.forEach(attachedModel -> collectClientElementsRecursively(project, attachedModel, 0, elements));
        }
        if (depth != 0) {
            for (Element e : element.getOwnedElement()) {
                collectClientElementsRecursively(project, e, --depth, elements);
            }
        }
    }

    private static Collection<ObjectNode> collectServerElementsRecursively(Project project, Element element, int depth, ProgressStatus progressStatus)
            throws ServerException, IOException, URISyntaxException {
        String id = Converters.getElementToIdConverter().apply(element);
        Collection<String> elementIds = new ArrayList<>(1);
        elementIds.add(id);
        ObjectNode response = MMSUtils.getElementsRecursively(project, elementIds, depth, progressStatus);
        // process response
        JsonNode value;
        if (response != null && (value = response.get("elements")) != null && value.isArray()) {
            Collection<ObjectNode> serverElements = StreamSupport.stream(value.spliterator(), false)
                    .filter(JsonNode::isObject).map(jsonNode -> (ObjectNode) jsonNode).collect(Collectors.toList());

            // check if we're validating the model root
            if (id.equals(Converters.getElementToIdConverter().apply(project.getPrimaryModel()))) {
                if (depth != 0) {
                    Collection<Element> attachedModels = new ArrayList<>(project.getModels());
                    attachedModels.remove(project.getPrimaryModel());
                    Collection<String> attachedModelIds = attachedModels.stream().map(Converters.getElementToIdConverter())
                            .filter(amId -> amId != null).collect(Collectors.toList());
                    response = MMSUtils.getElements(project, attachedModelIds, null);
                    if (response != null && (value = response.get("elements")) != null && value.isArray()) {
                        serverElements.addAll(StreamSupport.stream(value.spliterator(), false)
                                .filter(JsonNode::isObject).map(jsonNode -> (ObjectNode) jsonNode).collect(Collectors.toList()));
                    }

                    String holdingBinId = MDKConstants.HOLDING_BIN_PREFIX + Converters.getIProjectToIdConverter().apply(project.getPrimaryProject());
                    boolean found = false;
                    // check to see if the holding bin was returned
                    for (ObjectNode elem : serverElements) {
                        if ((value = elem.get(MDKConstants.ID_KEY)) != null && value.isTextual()
                                && value.asText().equals(holdingBinId)) {
                            found = true;
                            break;
                        }
                    }
                    // if no holding bin in server collection && model was element && (depth > 0 || depth == -1)
                    if (!found) {
                        response = MMSUtils.getElementRecursively(project, holdingBinId, depth, progressStatus);
                        if (response != null && (value = response.get("elements")) != null && value.isArray()) {
                            serverElements.addAll(StreamSupport.stream(value.spliterator(), false)
                                    .filter(JsonNode::isObject).map(jsonNode -> (ObjectNode) jsonNode).collect(Collectors.toList()));
                        }
                    }
                }
            }
            return serverElements;
        }
        return new ArrayList<>();
    }

    // TODO Make common across all sync types @donbot
    private boolean checkProject() throws ServerException, IOException, URISyntaxException {
        ProjectValidator pv = new ProjectValidator(project);
        pv.validate();
        if (pv.getValidationSuite().hasErrors()) {
            for (ValidationRule vr : pv.getValidationSuite().getValidationRules()) {
                validationSuite.addValidationRule(vr);
            }
            return false;
        }

        if (project.isRemote()) {
            BranchValidator bv = new BranchValidator(project);
            bv.validate(null, false);
            if (bv.getValidationSuite().hasErrors()) {
                for (ValidationRule vr : bv.getValidationSuite().getValidationRules()) {
                    validationSuite.addValidationRule(vr);
                }
                return false;
            }
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

