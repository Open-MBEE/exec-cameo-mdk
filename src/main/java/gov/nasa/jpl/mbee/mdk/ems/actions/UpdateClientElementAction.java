package gov.nasa.jpl.mbee.mdk.ems.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.docgen.validation.*;
import gov.nasa.jpl.mbee.mdk.emf.EMFBulkImporter;
import gov.nasa.jpl.mbee.mdk.ems.sync.local.LocalSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.mdk.ems.sync.local.LocalSyncTransactionCommitListener;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.json.JsonPatchUtils;
import gov.nasa.jpl.mbee.mdk.lib.Changelog;
import gov.nasa.jpl.mbee.mdk.lib.Pair;
import gov.nasa.jpl.mbee.mdk.lib.Utils;

import javax.annotation.CheckForNull;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Created by igomes on 9/27/16.
 */
public class UpdateClientElementAction extends RuleViolationAction implements AnnotationAction, IRuleViolationAction, RunnableWithProgress {
    private static final String NAME = "Update Element from MMS";

    private final String sysmlId;
    private final Element element;
    private final ObjectNode elementObjectNode;
    private final Project project;

    private final Changelog<String, ObjectNode> failedChangelog = new Changelog<>();

    private ValidationSuite validationSuite = new ValidationSuite("Update Changelog");
    private ValidationRule editableValidationRule = new ValidationRule("Element Editability", "The element to be updated shall be editable.", ViolationSeverity.WARNING),
            failedChangeValidationRule = new ValidationRule("Failed Change", "The element shall not fail to change.", ViolationSeverity.ERROR),
            equivalentElementValidationRule = new ValidationRule("Element Equivalency", "The changed element shall be equivalent to the source element.", ViolationSeverity.WARNING),
            successfulChangeValidationRule = new ValidationRule("Successful Change", "The element shall successfully change.", ViolationSeverity.INFO);

    {
        validationSuite.addValidationRule(editableValidationRule);
        validationSuite.addValidationRule(failedChangeValidationRule);
        validationSuite.addValidationRule(equivalentElementValidationRule);
        validationSuite.addValidationRule(successfulChangeValidationRule);
    }

    private Collection<ObjectNode> elementsToUpdate;
    private Collection<String> elementsToDelete;

    public UpdateClientElementAction(Project project) {
        this(null, null, null, project);
    }

    public UpdateClientElementAction(String sysmlId, Element element, ObjectNode elementObjectNode, Project project) {
        super(UpdateClientElementAction.class.getSimpleName(), NAME, null, null);
        this.sysmlId = sysmlId;
        this.element = element;
        this.elementObjectNode = elementObjectNode;
        this.project = project;
    }

    @Override
    public void execute(Collection<Annotation> annotations) {
        elementsToUpdate = new ArrayList<>(annotations.size());
        elementsToDelete = new ArrayList<>(annotations.size());

        for (Annotation annotation : annotations) {
            for (NMAction action : annotation.getActions()) {
                if (action instanceof UpdateClientElementAction) {
                    ObjectNode objectNode = ((UpdateClientElementAction) action).getElementObjectNode();
                    if (objectNode != null) {
                        elementsToUpdate.add(objectNode);
                    }
                    else {
                        elementsToDelete.add(sysmlId);
                    }
                    break;
                }
            }
        }
        ProgressStatusRunner.runWithProgressStatus(this, NAME, true, 0);
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

    public Changelog<String, ObjectNode> getFailedChangelog() {
        return failedChangelog;
    }

    @Override
    public void actionPerformed(@CheckForNull ActionEvent actionEvent) {
        elementsToUpdate = new ArrayList<>(1);
        elementsToDelete = new ArrayList<>(1);
        if (elementObjectNode != null) {
            elementsToUpdate.add(elementObjectNode);
        }
        else {
            elementsToDelete.add(sysmlId);
        }
        ProgressStatusRunner.runWithProgressStatus(this, NAME, true, 0);
    }

    @Override
    public void run(ProgressStatus progressStatus) {
        validationSuite.getValidationRules().forEach(validationRule -> validationRule.getViolations().clear());
        LocalSyncTransactionCommitListener localSyncTransactionCommitListener = LocalSyncProjectEventListenerAdapter.getProjectMapping(project).getLocalSyncTransactionCommitListener();

        if ((elementsToUpdate == null || elementsToUpdate.isEmpty()) && (elementsToDelete == null || elementsToDelete.isEmpty())) {
            Application.getInstance().getGUILog().log("[INFO] No MMS changes to update locally.");
            return;
        }
        if (elementsToUpdate != null && !elementsToUpdate.isEmpty()) {
            Application.getInstance().getGUILog().log("[INFO] Attempting to create/update " + NumberFormat.getInstance().format(elementsToUpdate.size()) + " element" + (elementsToUpdate.size() != 1 ? "s" : "") + " locally.");

            if (localSyncTransactionCommitListener != null) {
                localSyncTransactionCommitListener.setDisabled(true);
            }

            EMFBulkImporter emfBulkImporter = new EMFBulkImporter(NAME);
            Changelog<String, Pair<Element, ObjectNode>> changelog = emfBulkImporter.apply(elementsToUpdate, project, progressStatus);
            for (Map.Entry<Pair<Element, ObjectNode>, Exception> entry : emfBulkImporter.getFailedElementMap().entrySet()) {
                Element element = entry.getKey().getFirst();
                ObjectNode objectNode = entry.getKey().getSecond();
                Exception exception = entry.getValue();
                JsonNode sysmlIdJsonNode = objectNode.get(MDKConstants.ID_KEY);
                if (sysmlIdJsonNode == null || !sysmlIdJsonNode.isTextual()) {
                    continue;
                }
                String sysmlId = sysmlIdJsonNode.asText();
                // TODO Abstract this stuff to a converter @donbot
                String name = null;
                if (element == null) {
                    JsonNode nameJsonNode = objectNode.get(MDKConstants.NAME_KEY);
                    if (nameJsonNode != null && nameJsonNode.isTextual()) {
                        name = nameJsonNode.asText("<>");
                    }
                    if (name == null || name.isEmpty()) {
                        name = "<>";
                    }
                }
                (exception instanceof ReadOnlyElementException ? editableValidationRule : failedChangeValidationRule).addViolation(new ValidationRuleViolation(element != null ? element : project.getPrimaryModel(), "["
                        + (element != null ? "UPDATE" : "CREATE") + " FAILED]" + (element == null ? " " + objectNode.get(MDKConstants.TYPE_KEY).asText("Element") + " " + name + " : " + sysmlId : "")
                        + (element == null && exception != null ? " -" : "") + (exception != null ? " " + (exception instanceof ReadOnlyElementException ? "Element is not editable." : exception.getMessage()) : "")));
                failedChangelog.addChange(sysmlId, objectNode, element != null ? Changelog.ChangeType.UPDATED : Changelog.ChangeType.CREATED);
            }
            for (Map.Entry<Element, ObjectNode> entry : emfBulkImporter.getNonEquivalentElements().entrySet()) {
                try {
                    // TODO Change me to clipboard stuff @donbot
                    ObjectNode clientElementObjectNode = Converters.getElementToJsonConverter().apply(entry.getKey(), project);
                    ObjectNode serverElementObjectNode = entry.getValue();
                    System.err.println("[NOT EQUIVALENT] " + Converters.getElementToIdConverter().apply(entry.getKey()));
                    System.err.println(clientElementObjectNode);
                    System.err.println(serverElementObjectNode);
                    equivalentElementValidationRule.addViolation(new ValidationRuleViolation(entry.getKey(), "[NOT EQUIVALENT] " + JacksonUtils.getObjectMapper().writeValueAsString(JsonPatchUtils.getDiffAsJson(clientElementObjectNode, serverElementObjectNode))));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                // TODO Add detail diff here @donbot
            }
            for (Changelog.ChangeType changeType : Changelog.ChangeType.values()) {
                for (Map.Entry<String, Pair<Element, ObjectNode>> entry : changelog.get(changeType).entrySet()) {
                    successfulChangeValidationRule.addViolation(new ValidationRuleViolation(entry.getValue().getFirst(), "[" + changeType.name() + "]"));
                }
            }

            if (localSyncTransactionCommitListener != null) {
                localSyncTransactionCommitListener.setDisabled(false);
            }
        }
        if (elementsToDelete != null && !elementsToDelete.isEmpty()) {
            Application.getInstance().getGUILog().log("[INFO] Attempting to delete " + NumberFormat.getInstance().format(elementsToDelete.size()) + " element" + (elementsToDelete.size() != 1 ? "s" : "") + " locally.");

            if (localSyncTransactionCommitListener != null) {
                localSyncTransactionCommitListener.setDisabled(true);
            }
            if (!SessionManager.getInstance().isSessionCreated()) {
                SessionManager.getInstance().createSession(UpdateClientElementAction.class.getName() + " Deletes");
            }

            for (String id : elementsToDelete) {
                Exception exception = null;
                Element element = Converters.getIdToElementConverter().apply(id, project);
                if (element == null) {
                    continue;
                }
                try {
                    ModelElementsManager.getInstance().removeElement(element);
                } catch (ReadOnlyElementException | RuntimeException e) {
                    exception = e;
                }
                if (exception == null) {
                    successfulChangeValidationRule.addViolation(project.getPrimaryModel(), "[" + Changelog.ChangeType.DELETED.name() + "] " + element.getHumanName());
                }
                else {
                    (exception instanceof ReadOnlyElementException ? editableValidationRule : failedChangeValidationRule).addViolation(element, "[DELETE FAILED] " + exception.getMessage());
                    failedChangelog.addChange(id, null, Changelog.ChangeType.DELETED);
                }
            }

            if (localSyncTransactionCommitListener != null) {
                localSyncTransactionCommitListener.setDisabled(false);
            }
            if (SessionManager.getInstance().isSessionCreated()) {
                SessionManager.getInstance().closeSession();
            }
        }
        if (validationSuite.hasErrors()) {
            Utils.displayValidationWindow(validationSuite, validationSuite.getName());
        }
    }

    public Collection<ObjectNode> getElementsToUpdate() {
        return elementsToUpdate;
    }

    public void setElementsToUpdate(Collection<ObjectNode> elementsToUpdate) {
        this.elementsToUpdate = elementsToUpdate;
    }

    public Collection<String> getElementsToDelete() {
        return elementsToDelete;
    }

    public void setElementsToDelete(Collection<String> elementsToDelete) {
        this.elementsToDelete = elementsToDelete;
    }
}
