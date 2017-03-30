package gov.nasa.jpl.mbee.mdk.ems.sync.manual;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRule;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mbee.mdk.ems.MMSUtils;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
import gov.nasa.jpl.mbee.mdk.ems.validation.BranchValidator;
import gov.nasa.jpl.mbee.mdk.ems.validation.ElementValidator;
import gov.nasa.jpl.mbee.mdk.ems.validation.ProjectValidator;
import gov.nasa.jpl.mbee.mdk.lib.Pair;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
            attachedModels.forEach(attachedModel -> collectClientElementsRecursively(project, attachedModel, 0, elements));
        }
    }

    private static JsonParser collectServerElementsRecursively(Project project, Element element, int depth, ProgressStatus progressStatus)
            throws ServerException, IOException, URISyntaxException {
        String id = Converters.getElementToIdConverter().apply(element);
        Collection<String> elementIds = new ArrayList<>(1);
        elementIds.add(id);
        return MMSUtils.getElementsRecursively(project, elementIds, depth, progressStatus);
    }

    private static JsonParser collectServerModuleElementsRecursively(Project project, int depth, ProgressStatus progressStatus)
            throws ServerException, IOException, URISyntaxException {
        Collection<Element> attachedModels = new ArrayList<>(project.getModels());
        attachedModels.remove(project.getPrimaryModel());
        Collection<String> attachedModelIds = attachedModels.stream().map(Converters.getElementToIdConverter()).filter(amId -> amId != null).collect(Collectors.toList());
        return MMSUtils.getElements(project, attachedModelIds, null);
    }

    private static JsonParser collectServerHoldingBinElementsRecursively(Project project, int depth, ProgressStatus progressStatus)
            throws ServerException, IOException, URISyntaxException {
        String holdingBinId = "holding_bin_" + Converters.getIProjectToIdConverter().apply(project.getPrimaryProject());
        return MMSUtils.getElementRecursively(project, holdingBinId, depth, progressStatus);
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

