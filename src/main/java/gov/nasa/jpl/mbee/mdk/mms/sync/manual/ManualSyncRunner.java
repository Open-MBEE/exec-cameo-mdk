package gov.nasa.jpl.mbee.mdk.mms.sync.manual;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.mms.endpoints.MMSEndpoint;
import gov.nasa.jpl.mbee.mdk.mms.endpoints.MMSEndpointType;
import gov.nasa.jpl.mbee.mdk.mms.endpoints.MMSEndpointFactory;
import gov.nasa.jpl.mbee.mdk.mms.endpoints.MMSSearchEndpoint;
import gov.nasa.jpl.mbee.mdk.mms.validation.BranchValidator;
import gov.nasa.jpl.mbee.mdk.mms.validation.ElementValidator;
import gov.nasa.jpl.mbee.mdk.mms.validation.ProjectValidator;
import gov.nasa.jpl.mbee.mdk.util.MDUtils;
import gov.nasa.jpl.mbee.mdk.util.Pair;
import gov.nasa.jpl.mbee.mdk.util.Utils;
import gov.nasa.jpl.mbee.mdk.validation.ValidationSuite;
import org.apache.http.entity.ContentType;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Created by igomes on 9/26/16.
 */
public class ManualSyncRunner implements RunnableWithProgress {

    private final Collection<Element> rootElements;
    private final Project project;
    private final int depth;

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

        ProjectValidator pv = new ProjectValidator(project);
        pv.validate();
        if (pv.hasErrors()) {
            Application.getInstance().getGUILog().log("[ERROR] Project validation could not be completed. Manual validation aborted.");
            return;
        }
        if (pv.getValidationSuite().hasErrors()) {
            Utils.displayValidationWindow(project, pv.getValidationSuite(), "Sync Pre-Condition Validation");
            return;
        }

        if (project.isRemote()) {
            BranchValidator bv = new BranchValidator(project);
            bv.validate(null, false);
            if (bv.hasErrors()) {
                Application.getInstance().getGUILog().log("[ERROR] Branch validation could not be completed. Manual validation aborted.");
                return;
            }
            if (bv.getValidationSuite().hasErrors()) {
                Utils.displayValidationWindow(project, bv.getValidationSuite(), "Sync Pre-Condition Validation");
                return;
            }
        }

        progressStatus.setDescription("Processing and querying for " + rootElements.size() + " " + ((depth != 0) ? "root " : "") + "element" + (rootElements.size() != 1 ? "s" : ""));
        progressStatus.setIndeterminate(false);
        progressStatus.setMax(rootElements.size());
        progressStatus.setCurrent(0);

        List<Pair<Element, ObjectNode>> clientElements = new ArrayList<>(rootElements.size());
        Collection<File> responseFiles = new ArrayList<>(3);
        for (Element element : rootElements) {
            collectClientElementsRecursively(project, element, depth, clientElements);
            try {
                File searchFile = searchForServerElements(project, progressStatus);
                if(searchFile != null) {
                    responseFiles.add(searchFile);
                }
            } catch (ServerException | URISyntaxException | IOException e) {
                Application.getInstance().getGUILog().log("[ERROR] An error occurred while getting elements from the server. Manual sync aborted. Reason: " + e.getMessage());
                e.printStackTrace();
                validationSuite = null;
                return;
            }
            if (responseFiles.isEmpty()) {
                if (!progressStatus.isCancel()) {
                    Application.getInstance().getGUILog().log("[ERROR] Failed to get elements from the server. Manual sync aborted.");
                }
                validationSuite = null;
                return;
            }
            progressStatus.increase();
        }
        if (progressStatus.isCancel()) {
            return;
        }
        elementValidator = new ElementValidator("Element Validation", clientElements, null, project, responseFiles);
        elementValidator.run(progressStatus);
    }

    private File searchForServerElements(Project project, ProgressStatus progressStatus) throws ServerException, IOException, URISyntaxException {
        // prepare endpoint
        MMSEndpoint mmsEndpoint = MMSEndpointFactory.getMMSEndpoint(MMSUtils.getServerUrl(project), MMSEndpointType.SEARCH);
        mmsEndpoint.prepareUriPath();
        ((MMSSearchEndpoint) mmsEndpoint).setProjectId(Converters.getIProjectToIdConverter().apply(project.getPrimaryProject()));
        ((MMSSearchEndpoint) mmsEndpoint).setRefId(MDUtils.getBranchId(project));
        Collection<String> nodeIds = new HashSet<>();
        nodeIds.add(Converters.getIProjectToIdConverter().apply(project.getPrimaryProject())); // adding the root node because we'll recurse to get the entire tree from it
        File sendData = MMSUtils.createEntityFile(this.getClass(), ContentType.APPLICATION_JSON, nodeIds, MMSUtils.JsonBlobType.SEARCH);
        // use endpoint to make request
        return MMSUtils.sendMMSRequest(project, mmsEndpoint.buildRequest(MMSUtils.HttpRequestType.POST, sendData, project), progressStatus);
    }

    private static void collectClientElementsRecursively(Project project, Element element, int depth, List<Pair<Element, ObjectNode>> elements) {
        ObjectNode jsonObject = Converters.getElementToJsonConverter().apply(element, project);
        if (jsonObject == null) {
            return;
        }
        elements.add(new Pair<>(element, jsonObject));
        if (depth-- != 0) {
            for (Element elementChild : element.getOwnedElement()) {
                collectClientElementsRecursively(project, elementChild, depth, elements);
            }
        }
        if (element.equals(project.getPrimaryModel())) {
            List<Package> attachedModels = project.getModels();
            attachedModels.remove(project.getPrimaryModel());
            attachedModels.forEach(attachedModel -> collectClientElementsRecursively(project, attachedModel, 0, elements));
        }
    }

    public ValidationSuite getValidationSuite() {
        if (validationSuite == null) {
            return null;
        }
        return validationSuite.hasErrors() ? validationSuite : (elementValidator != null ? elementValidator.getValidationSuite() : null);
    }
}

