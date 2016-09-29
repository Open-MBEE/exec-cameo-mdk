package gov.nasa.jpl.mbee.mdk.ems.actions;

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.docgen.validation.*;
import gov.nasa.jpl.mbee.mdk.emf.EMFBulkImporter;
import gov.nasa.jpl.mbee.mdk.ems.ImportException;
import gov.nasa.jpl.mbee.mdk.lib.Changelog;
import gov.nasa.jpl.mbee.mdk.lib.Pair;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import org.json.simple.JSONObject;

import javax.annotation.CheckForNull;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by igomes on 9/27/16.
 */
public class UpdateClientElementAction extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {
    private static final String NAME = "Update Element from MMS";

    private final String id;
    private final Element element;
    private final JSONObject elementJson;
    private final Project project;

    private ValidationSuite validationSuite = new ValidationSuite("Update Changelog");
    private ValidationRule failedChangeValidationRule = new ValidationRule("Failed Change", "The element shall not fail to change.", ViolationSeverity.ERROR),
            equivalentElementValidationRule = new ValidationRule("Element Equivalency", "The changed element shall be equivalent to the source element.", ViolationSeverity.WARNING),
            successfulChangeValidationRule = new ValidationRule("Successful Change", "The element shall successfully change.", ViolationSeverity.INFO);

    {
        validationSuite.addValidationRule(failedChangeValidationRule);
        validationSuite.addValidationRule(equivalentElementValidationRule);
        validationSuite.addValidationRule(successfulChangeValidationRule);
    }

    public UpdateClientElementAction(String id, Element element, JSONObject elementJson, Project project) {
        super(NAME, NAME, null, null);
        this.id = id;
        this.element = element;
        this.elementJson = elementJson;
        this.project = project;
    }

    @Override
    public void execute(Collection<Annotation> annotations) {
        List<JSONObject> elementsToUpdate = new ArrayList<>(annotations.size());
        List<String> elementsToDelete = new ArrayList<>(annotations.size());

        for (Annotation annotation : annotations) {
            for (NMAction action : annotation.getActions()) {
                if (action instanceof UpdateClientElementAction) {
                    JSONObject jsonObject = ((UpdateClientElementAction) action).getElementJson();
                    if (jsonObject != null) {
                        elementsToUpdate.add(jsonObject);
                    }
                    else {
                        elementsToDelete.add(id);
                    }
                    break;
                }
            }
        }
        process(elementsToUpdate, elementsToDelete, project);
    }

    @Override
    public boolean canExecute(Collection<Annotation> annotations) {
        return true;
    }

    public Element getElement() {
        return element;
    }

    public JSONObject getElementJson() {
        return elementJson;
    }

    @Override
    public void actionPerformed(@CheckForNull ActionEvent actionEvent) {
        List<JSONObject> elementsToUpdate = new ArrayList<>(1);
        List<String> elementsToDelete = new ArrayList<>(1);
        if (elementJson != null) {
            elementsToUpdate.add(elementJson);
        }
        else {
            elementsToDelete.add(id);
        }
        process(elementsToUpdate, elementsToDelete, project);
    }

    private void process(List<JSONObject> elementsToUpdate, List<String> elementsToDelete, Project project) {
        if (elementsToUpdate != null && !elementsToUpdate.isEmpty()) {
            Application.getInstance().getGUILog().log("[INFO] Attempting to create/update " + elementsToUpdate.size() + " element" + (elementsToUpdate.size() != 1 ? "s" : "") + " locally.");
            EMFBulkImporter emfBulkImporter = new EMFBulkImporter(UpdateClientElementAction.class.getName() + " Creations/Updates");
            Changelog<String, Pair<Element, JSONObject>> changelog = emfBulkImporter.apply(elementsToUpdate, project);
            for (Map.Entry<Pair<Element, JSONObject>, ImportException> entry : emfBulkImporter.getFailedJsonObjects().entrySet()) {
                Element element = entry.getKey().getFirst();
                JSONObject jsonObject = entry.getKey().getSecond();
                ImportException importException = entry.getValue();
                // TODO Abstract this stuff to a converter @donbot
                String name = null;
                if (element == null) {
                    name = (String) jsonObject.getOrDefault(MDKConstants.NAME_KEY, "<>");
                    if (name == null || name.isEmpty()) {
                        name = "<>";
                    }
                }
                failedChangeValidationRule.addViolation(new ValidationRuleViolation(element != null ? element : project.getPrimaryModel(), "["
                        + (element != null ? "UPDATE" : "CREATE") + " FAILED]" + (element == null ? " " + jsonObject.getOrDefault(MDKConstants.TYPE_KEY, "Element") + " " + name + " : " + jsonObject.getOrDefault(MDKConstants.SYSML_ID_KEY, "<>") : "")
                        + (element == null && importException != null ? " -" : "") + (importException != null ? " " + importException.getMessage() : "")));
            }
            for (Map.Entry<Element, JSONObject> entry : emfBulkImporter.getNonEquivalentElements().entrySet()) {
                equivalentElementValidationRule.addViolation(new ValidationRuleViolation(entry.getKey(), "[NOT EQUIVALENT]"));
                // TODO Add detail diff here @donbot
            }
            for (Changelog.ChangeType changeType : Changelog.ChangeType.values()) {
                for (Map.Entry<String, Pair<Element, JSONObject>> entry : changelog.get(changeType).entrySet()) {
                    successfulChangeValidationRule.addViolation(new ValidationRuleViolation(entry.getValue().getFirst(), "[" + changeType.name() + "]"));
                }
            }
        }
        if (elementsToDelete != null && !elementsToDelete.isEmpty()) {
            Application.getInstance().getGUILog().log("[INFO] Attempting to delete " + elementsToDelete.size() + " element" + (elementsToDelete.size() != 1 ? "s" : "") + " locally.");
            if (!SessionManager.getInstance().isSessionCreated()) {
                SessionManager.getInstance().createSession(UpdateClientElementAction.class.getName() + " Deletes");
            }
            for (String id : elementsToDelete) {
                boolean success = false;
                Element element = Converters.getIdToElementConverter().apply(id, project);
                if (element == null) {
                    continue;
                }
                try {
                    ModelElementsManager.getInstance().removeElement(element);
                    success = true;
                } catch (ReadOnlyElementException ignored) {
                }
                if (success) {
                    successfulChangeValidationRule.addViolation(project.getPrimaryModel(), "[" + Changelog.ChangeType.DELETED.name() + "] " + element.getHumanName());
                }
                else {
                    failedChangeValidationRule.addViolation(element, "[DELETE FAILED]");
                }
            }
            if (SessionManager.getInstance().isSessionCreated()) {
                SessionManager.getInstance().closeSession();;
            }
        }
        if (validationSuite.hasErrors()) {
            Utils.displayValidationWindow(validationSuite, validationSuite.getName());
        }
    }
}
