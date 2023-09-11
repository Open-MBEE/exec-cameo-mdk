package org.openmbee.mdk.mms.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.commands.Command;
import com.nomagic.magicdraw.commands.CommandHistory;
import com.nomagic.magicdraw.commands.MacroCommand;
import com.nomagic.magicdraw.commands.RemoveCommandCreator;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import org.openmbee.mdk.actions.ClipboardAction;
import org.openmbee.mdk.actions.LockAction;
import org.openmbee.mdk.api.incubating.MDKConstants;
import org.openmbee.mdk.api.incubating.convert.Converters;
import org.openmbee.mdk.emf.EMFBulkImporter;
import org.openmbee.mdk.json.JacksonUtils;
import org.openmbee.mdk.mms.json.JsonPatchFunction;
import org.openmbee.mdk.mms.sync.delta.SyncElement;
import org.openmbee.mdk.mms.sync.local.LocalDeltaProjectEventListenerAdapter;
import org.openmbee.mdk.mms.sync.local.LocalDeltaTransactionCommitListener;
import org.openmbee.mdk.util.Changelog;
import org.openmbee.mdk.util.Pair;
import org.openmbee.mdk.util.Utils;
import org.openmbee.mdk.validation.*;

import javax.annotation.CheckForNull;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
    private final Changelog<String, Void> successfulChangelog = new Changelog<>();

    private ValidationSuite validationSuite = new ValidationSuite("Update Changelog");
    private ValidationRule editableValidationRule = new ValidationRule("Element Editability", "The element to be updated shall be editable.", ViolationSeverity.WARNING),
            failedChangeValidationRule = new ValidationRule("Failed Change", "The element shall not fail to change.", ViolationSeverity.ERROR),
            equivalentElementValidationRule = new ValidationRule("Element Equivalency", "The changed element shall be equivalent to the source element.", ViolationSeverity.WARNING),
            successfulChangeValidationRule = new ValidationRule("Successful Change", "The element shall successfully change.", ViolationSeverity.INFO);
            //deletionOnSuccessValidationRule = new ValidationRule("Deletion on Success", "The element to be deleted shall only be deleted if all elements to be created/updated are successfully imported.", ViolationSeverity.WARNING);

    {
        validationSuite.addValidationRule(editableValidationRule);
        validationSuite.addValidationRule(failedChangeValidationRule);
        validationSuite.addValidationRule(equivalentElementValidationRule);
        validationSuite.addValidationRule(successfulChangeValidationRule);
        //validationSuite.addValidationRule(deletionOnSuccessValidationRule);
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
                    UpdateClientElementAction updateClientElementAction = (UpdateClientElementAction) action;
                    if (updateClientElementAction.getElementObjectNode() != null) {
                        elementsToUpdate.add(updateClientElementAction.getElementObjectNode());
                    }
                    else {
                        elementsToDelete.add(updateClientElementAction.getElementId());
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

    public String getElementId() {
        return this.sysmlId;
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

    public Changelog<String, Void> getSuccessfulChangelog() {
        return successfulChangelog;
    }

    @Override
    public void actionPerformed(@CheckForNull ActionEvent actionEvent) {
        elementsToUpdate = new ArrayList<>(1);
        elementsToDelete = new ArrayList<>(1);
        if (getElementObjectNode() != null) {
            elementsToUpdate.add(getElementObjectNode());
        }
        else {
            elementsToDelete.add(getElementId());
        }
        ProgressStatusRunner.runWithProgressStatus(this, NAME, true, 0);
    }

    @Override
    public void run(ProgressStatus progressStatus) {
        validationSuite.getValidationRules().forEach(validationRule -> validationRule.getViolations().clear());
        LocalDeltaTransactionCommitListener localDeltaTransactionCommitListener = LocalDeltaProjectEventListenerAdapter.getProjectMapping(project).getLocalDeltaTransactionCommitListener();
        elementsToUpdate = (elementsToUpdate == null ? Collections.emptyList() : elementsToUpdate);
        elementsToDelete = (elementsToDelete == null ? Collections.emptyList() : elementsToDelete);
        if (elementsToUpdate.isEmpty() && elementsToDelete.isEmpty()) {
            Application.getInstance().getGUILog().log("[INFO] No MMS changes to update locally.");
            return;
        }
        // check elementsToUpdate against getEditableValidationRuleViolation function, and remove if appropriate (allows overriding of the function to remove elements under different conditions)
        Collection<ObjectNode> elementsToNotUpdate = new ArrayList<>();
        for (ObjectNode currentObjectNode : elementsToUpdate) {
            Element currentElement = null;
            JsonNode idValue;
            String currentId = null;
            if ((idValue = currentObjectNode.get(MDKConstants.ID_KEY)) != null && idValue.isTextual() && !(currentId = idValue.asText()).isEmpty()) {
                currentElement = Converters.getIdToElementConverter().apply(currentId, project);
            }
            if (currentElement == null && currentId == null) {
                elementsToNotUpdate.add(currentObjectNode);
                continue;
            }
            ValidationRuleViolation validationRuleViolation = getEditableValidationRuleViolation(currentElement, currentObjectNode, currentId);
            if (validationRuleViolation != null) {
                if (element != null && !element.isEditable()) {
                    validationRuleViolation.addAction(new LockAction(element, false));
                    validationRuleViolation.addAction(new LockAction(element, true));
                }
                addUpdateElementActions(validationRuleViolation, currentElement, currentId, currentObjectNode);
                editableValidationRule.addViolation(validationRuleViolation);
                failedChangelog.addChange(currentId, currentObjectNode, (currentElement != null && !project.isDisposed(currentElement) ? Changelog.ChangeType.UPDATED : Changelog.ChangeType.CREATED));
                elementsToNotUpdate.add(currentObjectNode);
            }
        }
        elementsToUpdate.removeAll(elementsToNotUpdate);
        boolean initialAutoNumbering = Application.getInstance().getProject().getOptions().isAutoNumbering();
        Application.getInstance().getProject().getOptions().setAutoNumbering(false);
        if (localDeltaTransactionCommitListener != null) {
            localDeltaTransactionCommitListener.setDisabled(true);
        }
        try {
            if (!elementsToUpdate.isEmpty()) {
                Application.getInstance().getGUILog().log("[INFO] Attempting to create/update " + NumberFormat.getInstance().format(elementsToUpdate.size()) + " element" + (elementsToUpdate.size() != 1 ? "s" : "") + " locally.");
            }
            EMFBulkImporter emfBulkImporter = new EMFBulkImporter(NAME) {
                @Override
                public void onSuccess() {
                    if (!elementsToDelete.isEmpty()) {
                        Application.getInstance().getGUILog().log("[INFO] Attempting to delete " + NumberFormat.getInstance().format(elementsToDelete.size()) + " element" + (elementsToDelete.size() != 1 ? "s" : "") + " locally.");
                    }
                    if (!SessionManager.getInstance().isSessionCreated(project)) {
                        SessionManager.getInstance().createSession(project, UpdateClientElementAction.class.getName() + " Deletes");
                    }
                    for (String id : elementsToDelete) {
                        Exception exception = null;
                        Element element = Converters.getIdToElementConverter().apply(id, project);
                        if (element == null) {
                            continue;
                        }

                        // check elements against getEditableValidationRuleViolation function, and continue if they should not be deleted (allows overriding of the function to remove elements under different conditions)
                        ValidationRuleViolation validationRuleViolation = getEditableValidationRuleViolation(element, null, id);
                        if (validationRuleViolation != null) {
                            addUpdateElementActions(validationRuleViolation, element, id, null);
                            editableValidationRule.addViolation(validationRuleViolation);
                            failedChangelog.addChange(id, null, Changelog.ChangeType.DELETED);
                            continue;
                        }

                        try {
                            Command command = RemoveCommandCreator.getCommand(element);
                            command.execute();
                            MacroCommand macroCommand = CommandHistory.getCommandForAppend(element);
                            macroCommand.add(command);
                        } catch (RuntimeException e) {
                            exception = e;
                        }
                        if (exception == null) {
                            successfulChangeValidationRule.addViolation(project.getPrimaryModel(), "[" + Changelog.ChangeType.DELETED.name() + "] " + element.getHumanName());
                        }
                        else {
                            validationRuleViolation = new ValidationRuleViolation(element, "[DELETE FAILED] " + exception.getMessage());
                            addUpdateElementActions(validationRuleViolation, element, id, null);
                            failedChangeValidationRule.addViolation(validationRuleViolation);
                            failedChangelog.addChange(id, null, Changelog.ChangeType.DELETED);
                        }
                    }
                    if (SessionManager.getInstance().isSessionCreated(project)) {
                        SessionManager.getInstance().closeSession(project);
                    }
                }

                @Override
                public void onFailure() {
                    onSuccess();
                    // The original intent was to skip all deletions on the existence of any failure during import to avoid the edge case of move out owned elements and then delete owning element.
                    // However, with MagicDraw locks the likelihood of failure is too high and not deleting results in corrupted elements.
                    // Will investigate the potential of ignoring MagicDraw locks or restricting one CSync/commit at a time to mitigate, but for now reverting to deleting always.
                    /*if (elementsToDelete != null) {
                        for (String id : elementsToDelete) {
                            Element element = Converters.getIdToElementConverter().apply(id, project);
                            if (element == null) {
                                continue;
                            }
                            deletionOnSuccessValidationRule.addViolation(element, "[DELETE SKIPPED] " + deletionOnSuccessValidationRule.getDescription());
                            failedChangelog.addChange(id, null, Changelog.ChangeType.DELETED);
                        }
                    }*/
                }
            };
            Changelog<String, Pair<Element, ObjectNode>> changelog = emfBulkImporter.apply(elementsToUpdate, project, progressStatus);
            for (Map.Entry<Pair<Element, ObjectNode>, Exception> entry : emfBulkImporter.getFailedElementMap().entrySet()) {
                Element entryElement = entry.getKey().getKey();
                ObjectNode entryObjectNode = entry.getKey().getValue();
                Exception entryException = entry.getValue();
                JsonNode sysmlIdJsonNode = entryObjectNode.get(MDKConstants.ID_KEY);
                if (sysmlIdJsonNode == null || !sysmlIdJsonNode.isTextual()) {
                    continue;
                }
                JsonNode typeNode = entryObjectNode.get(MDKConstants.TYPE_KEY);
                if (typeNode == null) { // this may happen with ve added artifacts in a beta version that's not attached to a model element
                    continue;
                }
                String entryId = sysmlIdJsonNode.asText();
                // TODO Abstract this stuff to a converter @donbot
                String name = null;
                if (entryElement == null || Project.isElementDisposed(entryElement)) {
                    JsonNode nameJsonNode = entryObjectNode.get(MDKConstants.NAME_KEY);
                    if (nameJsonNode != null && nameJsonNode.isTextual()) {
                        name = nameJsonNode.asText("<>");
                    }
                    if (name == null || name.isEmpty()) {
                        name = "<>";
                    }
                }
                String type = typeNode != null ? typeNode.asText("Element") : "Element";
                ValidationRuleViolation validationRuleViolation = new ValidationRuleViolation(entryElement != null && !Project.isElementDisposed(entryElement) ? entryElement : project.getPrimaryModel(), "["
                        + (entryElement != null && !Project.isElementDisposed(entryElement) ? "UPDATE" : "CREATE") + " FAILED]" + (entryElement == null || Project.isElementDisposed(entryElement) ? " " + type + " " + name + " : " + entryId : "")
                        + ((entryElement == null || Project.isElementDisposed(entryElement)) && entryException != null ? " -" : "") + (entryException != null ? " " + (entryException instanceof ReadOnlyElementException ? "Element is not editable." : entryException.getMessage()) : ""));
                addUpdateElementActions(validationRuleViolation, entryElement, entryId, entryObjectNode);
                (entryException instanceof ReadOnlyElementException ? editableValidationRule : failedChangeValidationRule).addViolation(validationRuleViolation);
                if (entryException != null && !(entryException instanceof ReadOnlyElementException)) {
                    entryException.printStackTrace();
                }
                failedChangelog.addChange(entryId, entryObjectNode, entryElement != null && !Project.isElementDisposed(entryElement) ? Changelog.ChangeType.UPDATED : Changelog.ChangeType.CREATED);
            }
            for (Map.Entry<Element, ObjectNode> entry : emfBulkImporter.getNonEquivalentElements().entrySet()) {
                Element entryElement = entry.getKey();
                String entryId = entryElement.getLocalID();
                ObjectNode entryClientElementObjectNode = Converters.getElementToJsonConverter().apply(entryElement, project);
                ObjectNode entryServerElementObjectNode = entry.getValue();
                JsonNode diff = JsonPatchFunction.getInstance().apply(entryClientElementObjectNode, entryServerElementObjectNode);

                ValidationRuleViolation validationRuleViolation = new ValidationRuleViolation(entry.getKey(), "[NOT EQUIVALENT]");
                ActionsCategory copyActionsCategory = new ActionsCategory("COPY", "Copy...");
                copyActionsCategory.setNested(true);
                validationRuleViolation.addAction(copyActionsCategory);
                copyActionsCategory.addAction(new ClipboardAction("ID", entryId));
                copyActionsCategory.addAction(new ClipboardAction("Element Hyperlink", "mdel://" + entryElement.getLocalID()));
                try {
                    copyActionsCategory.addAction(new ClipboardAction("Local JSON", JacksonUtils.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(entryClientElementObjectNode)));
                } catch (JsonProcessingException ignored) {
                }
                try {
                    copyActionsCategory.addAction(new ClipboardAction("MMS JSON", JacksonUtils.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(entryServerElementObjectNode)));
                } catch (JsonProcessingException ignored) {
                }
                try {
                    copyActionsCategory.addAction(new ClipboardAction("Diff", JacksonUtils.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(diff)));
                } catch (JsonProcessingException ignored) {
                }
                equivalentElementValidationRule.addViolation(validationRuleViolation);
            }
            for (Changelog.ChangeType changeType : Changelog.ChangeType.values()) {
                for (Map.Entry<String, Pair<Element, ObjectNode>> entry : changelog.get(changeType).entrySet()) {
                    Element successElement = entry.getValue().getKey();
                    successfulChangeValidationRule.addViolation(new ValidationRuleViolation(successElement, "Source: [" + SyncElement.Type.MMS.name() + "] | Type: [" + changeType.name() + "] | Target: [" + SyncElement.Type.LOCAL.name() + "]"));
                    successfulChangelog.addChange(successElement.getLocalID(), null, changeType);
                }
            }
        } finally {
            Application.getInstance().getProject().getOptions().setAutoNumbering(initialAutoNumbering);
            if (localDeltaTransactionCommitListener != null) {
                localDeltaTransactionCommitListener.setDisabled(false);
            }
        }
        if (validationSuite.hasErrors()) {
            Utils.displayValidationWindow(project, validationSuite, validationSuite.getName());
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

    protected ValidationRuleViolation getEditableValidationRuleViolation(Element element, ObjectNode objectNode, String sysmlId) {
        return null;
    }

    protected void addUpdateElementActions(ValidationRuleViolation validationRuleViolation, Element element, String sysmlId, ObjectNode objectNode) {
        ActionsCategory copyActionsCategory = new ActionsCategory("COPY", "Copy...");
        copyActionsCategory.setNested(true);
        validationRuleViolation.addAction(copyActionsCategory);
        copyActionsCategory.addAction(new ClipboardAction("ID", sysmlId));
        JsonNode diff = null;
        if (element != null) {
            copyActionsCategory.addAction(new ClipboardAction("Element Hyperlink", "mdel://" + element.getLocalID()));
            ObjectNode elementObjectNode = Converters.getElementToJsonConverter().apply(element, project);
            if (elementObjectNode != null) {
                try {
                    copyActionsCategory.addAction(new ClipboardAction("Local JSON", JacksonUtils.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(elementObjectNode)));
                } catch (JsonProcessingException ignored) {
                }
                diff = JsonPatchFunction.getInstance().apply(elementObjectNode, objectNode);
            }
        }
        try {
            copyActionsCategory.addAction(new ClipboardAction("MMS JSON", JacksonUtils.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(objectNode)));
        } catch (JsonProcessingException ignored) {
        }
        if (diff != null) {
            try {
                copyActionsCategory.addAction(new ClipboardAction("Diff", JacksonUtils.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(diff)));
            } catch (JsonProcessingException ignored) {
            }
        }

    }
}
