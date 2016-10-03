package gov.nasa.jpl.mbee.mdk.ems.actions;

import com.fasterxml.jackson.databind.node.ObjectNode;
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
    private final ObjectNode elementObjectNode;
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

    public UpdateClientElementAction(String id, Element element, ObjectNode elementObjectNode, Project project) {
        super(NAME, NAME, null, null);
        this.id = id;
        this.element = element;
        this.elementObjectNode = elementObjectNode;
        this.project = project;
    }

    @Override
    public void execute(Collection<Annotation> annotations) {
        List<ObjectNode> elementsToUpdate = new ArrayList<>(annotations.size());
        List<String> elementsToDelete = new ArrayList<>(annotations.size());

        for (Annotation annotation : annotations) {
            for (NMAction action : annotation.getActions()) {
                if (action instanceof UpdateClientElementAction) {
                    ObjectNode objectNode = ((UpdateClientElementAction) action).getElementObjectNode();
                    if (objectNode != null) {
                        elementsToUpdate.add(objectNode);
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

    public ObjectNode getElementObjectNode() {
        return elementObjectNode;
    }

    @Override
    public void actionPerformed(@CheckForNull ActionEvent actionEvent) {
        List<ObjectNode> elementsToUpdate = new ArrayList<>(1);
        List<String> elementsToDelete = new ArrayList<>(1);
        if (elementObjectNode != null) {
            elementsToUpdate.add(elementObjectNode);
        }
        else {
            elementsToDelete.add(id);
        }
        process(elementsToUpdate, elementsToDelete, project);
    }

    private void process(List<ObjectNode> elementsToUpdate, List<String> elementsToDelete, Project project) {
        if (elementsToUpdate != null && !elementsToUpdate.isEmpty()) {
            Application.getInstance().getGUILog().log("[INFO] Attempting to create/update " + elementsToUpdate.size() + " element" + (elementsToUpdate.size() != 1 ? "s" : "") + " locally.");
            EMFBulkImporter emfBulkImporter = new EMFBulkImporter(UpdateClientElementAction.class.getName() + " Creations/Updates");
            Changelog<String, Pair<Element, ObjectNode>> changelog = emfBulkImporter.apply(elementsToUpdate, project);
            for (Map.Entry<Pair<Element, ObjectNode>, ImportException> entry : emfBulkImporter.getFailedJsonObjects().entrySet()) {
                Element element = entry.getKey().getFirst();
                ObjectNode objectNode = entry.getKey().getSecond();
                ImportException importException = entry.getValue();
                // TODO Abstract this stuff to a converter @donbot
                String name = null;
                if (element == null) {
                    name = objectNode.get(MDKConstants.NAME_KEY).asText("<>");
                    if (name == null || name.isEmpty()) {
                        name = "<>";
                    }
                }
                failedChangeValidationRule.addViolation(new ValidationRuleViolation(element != null ? element : project.getPrimaryModel(), "["
                        + (element != null ? "UPDATE" : "CREATE") + " FAILED]" + (element == null ? " " + objectNode.get(MDKConstants.TYPE_KEY).asText("Element") + " " + name + " : " + objectNode.get(MDKConstants.SYSML_ID_KEY).asText("<>") : "")
                        + (element == null && importException != null ? " -" : "") + (importException != null ? " " + importException.getMessage() : "")));
            }
            for (Map.Entry<Element, ObjectNode> entry : emfBulkImporter.getNonEquivalentElements().entrySet()) {
                equivalentElementValidationRule.addViolation(new ValidationRuleViolation(entry.getKey(), "[NOT EQUIVALENT]"));
                // TODO Add detail diff here @donbot
            }
            for (Changelog.ChangeType changeType : Changelog.ChangeType.values()) {
                for (Map.Entry<String, Pair<Element, ObjectNode>> entry : changelog.get(changeType).entrySet()) {
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
