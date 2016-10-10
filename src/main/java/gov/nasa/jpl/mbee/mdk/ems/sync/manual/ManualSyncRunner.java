package gov.nasa.jpl.mbee.mdk.ems.sync.manual;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRule;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ViolationSeverity;
import gov.nasa.jpl.mbee.mdk.ems.ExportUtility;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
import gov.nasa.jpl.mbee.mdk.ems.validation.ElementValidator;
import gov.nasa.jpl.mbee.mdk.ems.validation.actions.InitializeProjectModel;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.lib.Pair;
import gov.nasa.jpl.mbee.mdk.lib.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by igomes on 9/26/16.
 */
public class ManualSyncRunner implements RunnableWithProgress {
    private final Collection<Element> rootElements;
    private final Project project;
    private final boolean recurse;
    private int depth;

    // TODO Move me to common sync pre-conditions @donbot
    private ValidationSuite validationSuite = new ValidationSuite("Sync Pre-Condition Validation");
    private ValidationRule projectExistenceValidationRule = new ValidationRule("Project Existence", "The project shall exist in the specified site.", ViolationSeverity.ERROR);

    {
        validationSuite.addValidationRule(projectExistenceValidationRule);
    }

    private ElementValidator elementValidator;

    public ManualSyncRunner(Collection<Element> rootElements, Project project, boolean recurse, int depth) {
        this.rootElements = rootElements;
        this.project = project;
        this.recurse = recurse;
        this.depth = depth;
    }

    @Override
    public void run(ProgressStatus progressStatus) {
        progressStatus.setDescription("Validating sync pre-conditions");
        progressStatus.setIndeterminate(true);
        if (!checkProject()) {
            if (validationSuite.hasErrors()) {
                Utils.displayValidationWindow(validationSuite, validationSuite.getName());
            }
            return;
        }

        progressStatus.setDescription("Processing and querying for " + rootElements.size() + " " + (recurse || depth > 0 ? "root " : "") + "element" + (rootElements.size() != 1 ? "s" : ""));
        progressStatus.setIndeterminate(false);
        progressStatus.setMax(rootElements.size());
        progressStatus.setCurrent(0);

        List<Pair<Element, ObjectNode>> clientElements = new ArrayList<>(rootElements.size());
        List<ObjectNode> serverElements = new ArrayList<>(rootElements.size());
        for (Element element : rootElements) {
            collectClientElementsRecursively(element, project, recurse, depth, clientElements);
            Collection<ObjectNode> jsonObjects = getServerElementsRecursively(element, recurse, depth, progressStatus);
            if (jsonObjects == null) {
                if (!progressStatus.isCancel()) {
                    Application.getInstance().getGUILog().log("[ERROR] Failed to get elements from the server. Aborting manual sync.");
                }
                return;
            }
            serverElements.addAll(jsonObjects);

            progressStatus.increase();
        }
        elementValidator = new ElementValidator(clientElements, serverElements, project);
        elementValidator.run(progressStatus);
        Utils.displayValidationWindow(elementValidator.getValidationSuite(), "Manual Sync Validation");
    }

    public void collectClientElementsRecursively(Element element, Project project, boolean recurse, int depth, List<Pair<Element, ObjectNode>> elements) {
        ObjectNode jsonObject = Converters.getElementToJsonConverter().apply(element, project);
        if (jsonObject == null) {
            return;
        }
        elements.add(new Pair<>(element, jsonObject));
        if (recurse || depth > 0) {
            for (Element e : element.getOwnedElement()) {
                collectClientElementsRecursively(e, project, recurse, --depth, elements);
            }
        }
    }

    // TODO Make common across all sync types @donbot
    public boolean checkProject() {
        String projectUrl = ExportUtility.getUrlForProject();
        if (projectUrl == null) {
            return false;
        }
        String globalUrl = ExportUtility.getUrl(Application.getInstance().getProject());
        globalUrl += "/workspaces/master/elements/" + Application.getInstance().getProject().getPrimaryProject().getProjectID();
        String globalResponse = null;
        try {
            globalResponse = ExportUtility.get(globalUrl, false);
        } catch (ServerException ex) {

        }
        String url = ExportUtility.getUrlWithWorkspace();
        if (url == null) {
            return false;
        }
        if (globalResponse == null) {
            ValidationRuleViolation v;
            if (url.contains("master")) {
                v = new ValidationRuleViolation(Application.getInstance().getProject().getModel(), "The project doesn't exist on the web.");
                // TODO Change me back to false @donbot
                v.addAction(new InitializeProjectModel(true));
            }
            else {
                v = new ValidationRuleViolation(Application.getInstance().getProject().getModel(), "The trunk project doesn't exist on the web. Export the trunk first.");
            }
            projectExistenceValidationRule.addViolation(v);
            return false;
        }
        String response = null;
        try {
            response = ExportUtility.get(projectUrl, false);
        } catch (ServerException ex) {

        }
        if (response == null || response.contains("Site node is null") || response.contains("Could not find project")) {//tears

            ValidationRuleViolation v = new ValidationRuleViolation(Application.getInstance().getProject().getModel(), "The project exists on the server already under a different site.");
            //v.addAction(new InitializeProjectModel(false));
            projectExistenceValidationRule.addViolation(v);
            return false;
        }
        for (Element start : rootElements) {
            if (ProjectUtilities.isElementInAttachedProject(start)) {
                Utils.showPopupMessage("You should not validate or export elements not from this project! Open the right project and do it from there");
                return false;
            }
        }
        return true;
    }

    // TODO Fix me and move me to MMSUtils @donbot
    public Collection<ObjectNode> getServerElementsRecursively(Element element, boolean recurse, int depth, ProgressStatus progressStatus) {
        String url = ExportUtility.getUrlWithWorkspace();
        String id = Converters.getElementToIdConverter().apply(element);
        final String url2;
        if (depth > 0) {
            url2 = url + "/elements/" + id + "?depth=" + java.lang.Integer.toString(depth) + "&qualified=false";
        }
        else {
            url2 = url + "/elements/" + id + "?recurse=" + java.lang.Boolean.toString(recurse) + "&qualified=false";
        }

        final AtomicReference<String> res = new AtomicReference<>();
        Thread t = new Thread(() -> {
            String tres = null;
            try {
                tres = ExportUtility.get(url2, false);
            } catch (ServerException ex) {
            }
            res.set(tres);
        });
        t.start();
        try {
            t.join(10000);
            while (t.isAlive()) {
                if (progressStatus.isCancel()) {
                    Application.getInstance().getGUILog().log("[INFO] Request to server for elements cancelled.");
                    //clean up thread?
                    return null;
                }
                t.join(10000);
            }
        } catch (Exception e) {

        }
        //response = ExportUtility.get(url2, false);

        String response = res.get();
        Application.getInstance().getGUILog().log("[INFO] Finished getting elements");
        if (response == null) {
            response = "{\"elements\": []}";
        }
        JsonNode responseJsonNode;
        try {
            responseJsonNode = JacksonUtils.getObjectMapper().readTree(response);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        if (responseJsonNode != null && responseJsonNode.has("elements")) {
            JsonNode elementsJsonNode = responseJsonNode.get("elements");
            if (elementsJsonNode.isArray()) {
                return StreamSupport.stream(elementsJsonNode.spliterator(), false).filter(JsonNode::isObject).map(jsonNode -> (ObjectNode) jsonNode).collect(Collectors.toList());
            }
        }
        return null;
    }

    public ValidationSuite getValidationSuite() {
        return validationSuite.hasErrors() ? validationSuite : (elementValidator != null ? elementValidator.getValidationSuite() : null);
    }
}
