package gov.nasa.jpl.mbee.mdk.mms.sync.delta;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.teamwork2.locks.ILockProjectService;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.mms.actions.MMSLoginAction;
import gov.nasa.jpl.mbee.mdk.mms.actions.UpdateClientElementAction;
import gov.nasa.jpl.mbee.mdk.mms.sync.jms.JMSMessageListener;
import gov.nasa.jpl.mbee.mdk.mms.sync.jms.JMSSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.mdk.mms.sync.local.LocalSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.mdk.mms.sync.local.LocalSyncTransactionCommitListener;
import gov.nasa.jpl.mbee.mdk.mms.validation.BranchValidator;
import gov.nasa.jpl.mbee.mdk.mms.validation.ElementValidator;
import gov.nasa.jpl.mbee.mdk.mms.validation.ProjectValidator;
import gov.nasa.jpl.mbee.mdk.options.MDKOptionsGroup;
import gov.nasa.jpl.mbee.mdk.util.*;
import gov.nasa.jpl.mbee.mdk.validation.ValidationSuite;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class DeltaSyncRunner implements RunnableWithProgress {
    private final boolean shouldCommitDeletes, shouldCommit, shouldUpdate;

    private final Project project = Application.getInstance().getProject();

    private boolean failure = true;

    private Changelog<String, Element> failedLocalChangelog = new Changelog<>();
    private Changelog<String, Void> failedJmsChangelog = new Changelog<>(), successfulJmsChangelog = new Changelog<>();

    private List<ValidationSuite> vss = new ArrayList<>();

    public DeltaSyncRunner(boolean shouldCommit, boolean shouldCommitDeletes, boolean shouldUpdate) {
        this.shouldCommit = shouldCommit;
        this.shouldCommitDeletes = shouldCommitDeletes;
        this.shouldUpdate = shouldUpdate;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(ProgressStatus progressStatus) {
        progressStatus.setDescription("Initializing");
        if (ProjectUtilities.isFromEsiServer(project.getPrimaryProject()) && EsiUtils.getLoggedUserName() == null) {
            Utils.guilog("[WARNING] You need to be logged in to Teamwork Cloud first. Skipping sync. All changes will be persisted in the model and re-attempted in the next sync.");
            return;
        }
        try {
            if (!TicketUtils.isTicketValid(project, progressStatus)) {
                Utils.guilog("[WARNING] You are not logged in to MMS. Skipping sync. All changes will be persisted in the model and re-attempted in the next sync.");
                new Thread(() -> MMSLoginAction.loginAction(project)).start();
                return;
            }
        } catch (IOException | URISyntaxException | ServerException e) {
            Utils.guilog("[ERROR] An error occurred while validating credentials. Credentials will be cleared. Skipping sync. All changes will be persisted in the model and re-attempted in the next sync. Reason: " + e.getMessage());
            new Thread(() -> MMSLoginAction.loginAction(project)).start();
            return;
        }

        ProjectValidator pv = new ProjectValidator(project);
        pv.validate();
        if (pv.hasErrors()) {
            Application.getInstance().getGUILog().log("[WARNING] Coordinated Sync can not complete and will be skipped.");
            return;
        }
        if (pv.getValidationSuite().hasErrors()) {
            Application.getInstance().getGUILog().log("[WARNING] Project has not been committed to MMS. Skipping sync. You must commit the project and model to MMS before Coordinated Sync can complete.");
            Utils.displayValidationWindow(project, pv.getValidationSuite(), "Coordinated Sync Pre-Condition Validation");
            return;
        }

        BranchValidator bv = new BranchValidator(project);
        bv.validate(null, false);
        if (bv.hasErrors()) {
            Application.getInstance().getGUILog().log("[WARNING] Coordinated sync can not complete and will be skipped.");
            return;
        }
        if (bv.getValidationSuite().hasErrors()) {
            Application.getInstance().getGUILog().log("[WARNING] Branch has not been committed to MMS. Skipping sync. You must commit the branch to MMS and sync the model before Coordinated Sync can complete.");
            Utils.displayValidationWindow(project, bv.getValidationSuite(), "Coordinated Sync Pre-Condition Validation");
            return;
        }

        LocalSyncTransactionCommitListener listener = LocalSyncProjectEventListenerAdapter.getProjectMapping(project).getLocalSyncTransactionCommitListener();

        // UPDATE LOCKS

        ILockProjectService lockService = EsiUtils.getLockService(project);
        if (lockService == null) {
            Application.getInstance().getGUILog().log("[ERROR] Teamwork Cloud lock service unavailable. Skipping sync. All changes will be re-attempted in the next sync.");
            return;
        }

        listener.setDisabled(true);
        try {
            lockService.updateLocks(progressStatus);
        } catch (RuntimeException e) {
            Application.getInstance().getGUILog().log("[ERROR] Failed to update locks from Teamwork Cloud. Skipping sync. All changes will be persisted in the model and re-attempted in the next sync. Reason: " + e.getMessage());
            e.printStackTrace();
            return;
        } finally {
            listener.setDisabled(false);
        }

        // LOCK SYNC FOLDER

        //listener.setDisabled(true);
        //SyncElements.lockSyncFolder(project);
        //listener.setDisabled(false);

        // DOWNLOAD MMS MESSAGES IF ASYNC CONSUMER IS DISABLED

        JMSSyncProjectEventListenerAdapter.JMSSyncProjectMapping jmsSyncProjectMapping = JMSSyncProjectEventListenerAdapter.getProjectMapping(Application.getInstance().getProject());
        JMSMessageListener jmsMessageListener = jmsSyncProjectMapping.getJmsMessageListener();
        if (jmsMessageListener == null) {
            if (MDKOptionsGroup.getMDKOptions().isChangeListenerEnabled()) {
                Application.getInstance().getGUILog().log("[WARNING] Not connected to MMS queue. Skipping sync. All changes will be re-attempted in the next sync.");
            }
            return;
        }

        /*if (jmsSyncProjectMapping.isDisabled()) {
            jmsSyncProjectMapping.setDisabled(!JMSSyncProjectEventListenerAdapter.initDurable(project, jmsSyncProjectMapping));
            List<TextMessage> textMessages = jmsSyncProjectMapping.getAllTextMessages(true);
            if (textMessages == null) {
                Utils.guilog("[ERROR] Could not get changes from MMS. Please check your network connection and try again.");
                failure = true;
                return;
            }
            for (TextMessage textMessage : textMessages) {
                jmsMessageListener.onMessage(textMessage);
            }
        }*/

        // BUILD COMPLETE LOCAL CHANGELOG

        Changelog<String, Element> persistedLocalChangelog = new Changelog<>();
        //JSONObject persistedLocalChanges = DeltaSyncProjectEventListenerAdapter.getUpdatesOrFailed(Application.getInstance().getProject(), "update");
        Collection<SyncElement> persistedLocalSyncElements = SyncElements.getAllByType(project, SyncElement.Type.LOCAL);
        for (SyncElement syncElement : persistedLocalSyncElements) {
            persistedLocalChangelog = persistedLocalChangelog.and(SyncElements.buildChangelog(syncElement), (key, value) -> Converters.getIdToElementConverter().apply(key, project));
        }
        Changelog<String, Element> localChangelog = persistedLocalChangelog.and(listener.getInMemoryLocalChangelog());


        Map<String, Element> localCreated = localChangelog.get(Changelog.ChangeType.CREATED),
                localUpdated = localChangelog.get(Changelog.ChangeType.UPDATED),
                localDeleted = localChangelog.get(Changelog.ChangeType.DELETED);

        // BUILD COMPLETE MMS CHANGELOG

        Changelog<String, Void> persistedJmsChangelog = new Changelog<>();
        Collection<SyncElement> persistedJmsSyncElements = SyncElements.getAllByType(project, SyncElement.Type.MMS);
        //JSONObject persistedJmsChanges = DeltaSyncProjectEventListenerAdapter.getUpdatesOrFailed(Application.getInstance().getProject(), "jms");
        for (SyncElement syncElement : persistedJmsSyncElements) {
            persistedJmsChangelog = persistedJmsChangelog.and(SyncElements.buildChangelog(syncElement));
        }
        Changelog<String, Void> jmsChangelog = persistedJmsChangelog.and(jmsMessageListener.getInMemoryJMSChangelog(), (key, objectNode) -> null);

        Map<String, Void> jmsCreated = jmsChangelog.get(Changelog.ChangeType.CREATED),
                jmsUpdated = jmsChangelog.get(Changelog.ChangeType.UPDATED),
                jmsDeleted = jmsChangelog.get(Changelog.ChangeType.DELETED);

        Set<String> elementIdsToGet = new HashSet<>(jmsUpdated.keySet());
        elementIdsToGet.addAll(jmsCreated.keySet());
        if (shouldUpdate && !jmsChangelog.isEmpty()) {
            int size = jmsChangelog.flattenedSize();
            Application.getInstance().getGUILog().log("[INFO] Getting " + size + " changed element" + (size != 1 ? "s" : "") + " from the MMS.");
        }

        Map<String, ObjectNode> jmsJsons = new HashMap<>(elementIdsToGet.size());

        // Get latest json for element added/changed from MMS

        if (!elementIdsToGet.isEmpty()) {
            progressStatus.setDescription("Getting " + elementIdsToGet.size() + " added/changed element" + (elementIdsToGet.size() != 1 ? "s" : "") + " from MMS");
            File responseFile;
            ObjectNode response;
            try {
                responseFile = MMSUtils.getElements(project, elementIdsToGet, progressStatus);
                try (JsonParser jsonParser = JacksonUtils.getJsonFactory().createParser(responseFile)) {
                    response = JacksonUtils.parseJsonObject(jsonParser);
                }
            } catch (IOException | URISyntaxException | ServerException e) {
                if (progressStatus.isCancel()) {
                    Application.getInstance().getGUILog().log("[INFO] Sync manually cancelled. All changes will be re-attempted in the next sync.");
                    return;
                }
                Application.getInstance().getGUILog().log("[ERROR] Cannot get elements from MMS. Skipping sync. All changes will be re-attempted in the next sync. Reason: " + e.getMessage());
                e.printStackTrace();
                return;
            }

            if (progressStatus.isCancel()) {
                Application.getInstance().getGUILog().log("[INFO] Sync manually cancelled. All changes will be attempted at next update.");
                return;
            }

            if (response == null) {
                Application.getInstance().getGUILog().log("[ERROR] Cannot get elements from MMS server. Skipping sync. All changes will be re-attempted in the next sync.");
                return;
            }
            JsonNode elementsArrayNode = response.get("elements");
            if (elementsArrayNode == null || !elementsArrayNode.isArray()) {
                elementsArrayNode = JacksonUtils.getObjectMapper().createArrayNode();
            }
            for (JsonNode jsonNode : elementsArrayNode) {
                if (!jsonNode.isObject()) {
                    continue;
                }
                String webId = jsonNode.get(MDKConstants.ID_KEY).asText();
                jmsJsons.put(webId, (ObjectNode) jsonNode);
            }
        }

        // NEW CONFLICT DETECTION

        progressStatus.setDescription("Detecting conflicts");
        Map<String, Pair<Changelog.Change<Element>, Changelog.Change<Void>>> conflictedChanges = new LinkedHashMap<>(),
                unconflictedChanges = new LinkedHashMap<>();
        localChangelog.findConflicts(jmsChangelog, (change, change2) -> change != null && change2 != null, conflictedChanges, unconflictedChanges);

        // MAP CHANGES TO ACTIONABLE GROUPS

        Map<String, Element> localElementsToPost = new LinkedHashMap<>(localCreated.size() + localUpdated.size());
        Set<String> deleteElements = new HashSet<>(localDeleted.size());

        Map<String, ObjectNode> jmsElementsToCreateLocally = new LinkedHashMap<>(jmsCreated.size());
        Map<String, Pair<ObjectNode, Element>> jmsElementsToUpdateLocally = new LinkedHashMap<>(jmsUpdated.size());
        Map<String, Element> jmsElementsToDeleteLocally = new LinkedHashMap<>(jmsDeleted.size());

        // only one side of the pair will have a value when unconflicted
        for (Map.Entry<String, Pair<Changelog.Change<Element>, Changelog.Change<Void>>> unconflictedEntry : unconflictedChanges.entrySet()) {
            String id = unconflictedEntry.getKey();
            Changelog.Change<Element> localChange = unconflictedEntry.getValue().getKey();
            Changelog.Change<ObjectNode> jmsChange = unconflictedEntry.getValue().getValue() != null ?
                    new Changelog.Change<>(jmsJsons.get(id), unconflictedEntry.getValue().getValue().getType()) : null;

            if (shouldCommit && localChange != null) {
                Element element = localChange.getChanged();
                switch (localChange.getType()) {
                    case CREATED:
                    case UPDATED:
                        if (element == null) {
                            Application.getInstance().getGUILog().log("[INFO] Attempted to create/update element " + id + " on the MMS, but it no longer exists locally. Skipping.");
                            continue;
                        }
                        localElementsToPost.put(id, element);
                        break;
                    case DELETED:
                        if (element != null && !project.isDisposed(element)) {
                            Application.getInstance().getGUILog().log("[INFO] Attempted to delete element " + id + " from the MMS, but it still exists locally. Skipping.");
                            continue;
                        }
                        deleteElements.add(id);
                        break;
                }
            }
            else if (shouldUpdate && jmsChange != null) {
                ObjectNode objectNode = jmsChange.getChanged();
                Element element = Converters.getIdToElementConverter().apply(id, project);
                switch (jmsChange.getType()) {
                    case CREATED:
                        if (objectNode == null) {
                            Application.getInstance().getGUILog().log("[INFO] Attempted to create element " + id + " locally, but it no longer exists on the MMS. Skipping.");
                            continue;
                        }
                        if (element != null) {
                            Application.getInstance().getGUILog().log("[INFO] Attempted to create element " + id + " locally, but it already exists. Skipping.");
                            continue;
                        }
                        jmsElementsToCreateLocally.put(id, objectNode);
                        break;
                    case UPDATED:
                        if (objectNode == null) {
                            Application.getInstance().getGUILog().log("[INFO] Attempted to update element " + id + " locally, but it no longer exists on the MMS. Skipping.");
                            continue;
                        }
                        if (element == null) {
                            Application.getInstance().getGUILog().log("[INFO] Attempted to update element " + id + " locally, but it does not exist. Skipping.");
                            continue;
                        }
                        if (!element.isEditable() && lockService.isLocked(element) && !lockService.isLockedByMe(element)) {
                            if (MDUtils.isDeveloperMode()) {
                                Application.getInstance().getGUILog().log("[INFO] Attempted to update element " + id + " locally, but it is locked by someone else. Skipping.");
                            }
                            failedJmsChangelog.addChange(id, null, Changelog.ChangeType.UPDATED);
                            continue;
                        }
                        jmsElementsToUpdateLocally.put(id, new Pair<>(objectNode, element));
                        break;
                    case DELETED:
                        if (element == null) {
                            Application.getInstance().getGUILog().log("[INFO] Attempted to delete element " + id + " locally, but it doesn't exist. Skipping.");
                            continue;
                        }
                        if (!element.isEditable() && lockService.isLocked(element) && !lockService.isLockedByMe(element)) {
                            if (MDUtils.isDeveloperMode()) {
                                Application.getInstance().getGUILog().log("[INFO] Attempted to delete element " + id + " locally, but it is locked by someone else. Skipping.");
                            }
                            failedJmsChangelog.addChange(id, null, Changelog.ChangeType.DELETED);
                            continue;
                        }
                        jmsElementsToDeleteLocally.put(id, element);
                        break;
                }
            }
        }

        if (progressStatus.isCancel()) {
            Application.getInstance().getGUILog().log("[INFO] Sync manually cancelled. All changes will be re-attempted in the next sync.");
            return;
        }

        // POINT OF NO RETURN

        // COMMIT UNCONFLICTED CREATIONS AND UPDATES TO MMS

        boolean shouldLogNoLocalChanges = shouldCommit;
        if (shouldCommit && !localElementsToPost.isEmpty()) {
            progressStatus.setDescription("Committing creations and updates to MMS");
            LinkedList<ObjectNode> postElements = new LinkedList<>();
            for (Element element : localElementsToPost.values()) {
                ObjectNode elementObjectNode = Converters.getElementToJsonConverter().apply(element, project);
                if (elementObjectNode != null) {
                    postElements.add(elementObjectNode);
                }
            }
            if (!postElements.isEmpty()) {
                URIBuilder requestUri = MMSUtils.getServiceProjectsRefsElementsUri(project);
                try {
                    File file = MMSUtils.createEntityFile(this.getClass(), ContentType.APPLICATION_JSON, postElements, MMSUtils.JsonBlobType.ELEMENT_JSON);

                    HttpRequestBase request = MMSUtils.buildRequest(MMSUtils.HttpRequestType.POST, requestUri, file, ContentType.APPLICATION_JSON);
                    TaskRunner.runWithProgressStatus(progressStatus1 -> {
                        try {
                            MMSUtils.sendMMSRequest(project, request, progressStatus1);
                        } catch (IOException | ServerException | URISyntaxException e) {
                            // TODO Implement error handling that was previously not possible due to OutputQueue implementation
                            e.printStackTrace();
                        }
                    }, "Sync Create/Update x" + NumberFormat.getInstance().format(postElements.size()), true, TaskRunner.ThreadExecutionStrategy.SINGLE);
                } catch (IOException | URISyntaxException e) {
                    Application.getInstance().getGUILog().log("[ERROR] An error occurred. Skipping sync. All changes will be re-attempted in the next sync. Reason: " + e.getMessage());
                    e.printStackTrace();
                    return;
                }
                shouldLogNoLocalChanges = false;
            }
        }

        // COMMIT UNCONFLICTED DELETIONS TO MMS
        // NEEDS TO BE AFTER LOCAL; EX: MOVE ELEMENT OUT ON MMS, DELETE OWNER LOCALLY, WHAT HAPPENS?

        if (shouldCommit && shouldCommitDeletes && !deleteElements.isEmpty()) {
            progressStatus.setDescription("Committing deletions to MMS");
            URIBuilder requestUri = MMSUtils.getServiceProjectsRefsElementsUri(project);
            try {
                File file = MMSUtils.createEntityFile(this.getClass(), ContentType.APPLICATION_JSON, deleteElements, MMSUtils.JsonBlobType.ELEMENT_ID);
                HttpRequestBase request = MMSUtils.buildRequest(MMSUtils.HttpRequestType.DELETE, requestUri, file, ContentType.APPLICATION_JSON);
                TaskRunner.runWithProgressStatus(progressStatus1 -> {
                    try {
                        MMSUtils.sendMMSRequest(project, request, progressStatus1);
                    } catch (IOException | ServerException | URISyntaxException e) {
                        // TODO Implement error handling that was previously not possible due to OutputQueue implementation
                        e.printStackTrace();
                    }
                }, "Sync Delete x" + NumberFormat.getInstance().format(deleteElements.size()), true, TaskRunner.ThreadExecutionStrategy.SINGLE);
            } catch (IOException | URISyntaxException e) {
                Application.getInstance().getGUILog().log("[ERROR] An error occurred. Skipping sync. All changes will be re-attempted in the next sync. Reason: " + e.getMessage());
                e.printStackTrace();
                return;
            }
            shouldLogNoLocalChanges = false;
        }

        // OUTPUT RESULT OF LOCAL CHANGES

        if (shouldLogNoLocalChanges) {
            Application.getInstance().getGUILog().log("[INFO] No local changes to commit to MMS.");
        }

        // ADD CREATED ELEMENTS LOCALLY FROM MMS
        // CHANGE UPDATED ELEMENTS LOCALLY FROM MMS
        // REMOVE DELETED ELEMENTS LOCALLY FROM MMS

        if (shouldUpdate) {
            listener.setDisabled(true);

            // Create and update maps are mutually exclusive at this point, so this is safe. If they weren't then the ordering may be messed up.
            List<ObjectNode> jmsElementsToCreateOrUpdateLocally = new ArrayList<>(jmsElementsToCreateLocally.size() + jmsElementsToUpdateLocally.size());
            jmsElementsToCreateOrUpdateLocally.addAll(jmsElementsToCreateLocally.values());
            jmsElementsToUpdateLocally.values().forEach(pair -> jmsElementsToCreateOrUpdateLocally.add(pair.getKey()));

            UpdateClientElementAction updateClientElementAction = new UpdateClientElementAction(project);
            updateClientElementAction.setElementsToUpdate(jmsElementsToCreateOrUpdateLocally);
            updateClientElementAction.setElementsToDelete(jmsElementsToDeleteLocally.values().stream().map(Converters.getElementToIdConverter()).filter(id -> id != null).filter(id -> !id.isEmpty()).collect(Collectors.toList()));
            updateClientElementAction.run(progressStatus);

            failedJmsChangelog = failedJmsChangelog.and(updateClientElementAction.getFailedChangelog(), (id, objectNode) -> null);
            successfulJmsChangelog = updateClientElementAction.getSuccessfulChangelog();
            listener.setDisabled(false);
        }

        // HANDLE CONFLICTS

        progressStatus.setDescription("Finishing up");

        Set<Element> localConflictedElements = new HashSet<>();
        Set<ObjectNode> jmsConflictedElements = new HashSet<>();

        for (Map.Entry<String, Pair<Changelog.Change<Element>, Changelog.Change<Void>>> conflictedEntry : conflictedChanges.entrySet()) {
            String id = conflictedEntry.getKey();
            Changelog.Change<Element> localChange = conflictedEntry.getValue().getKey();
            Changelog.Change<ObjectNode> jmsChange = conflictedEntry.getValue().getValue() != null ?
                    new Changelog.Change<>(jmsJsons.get(id), conflictedEntry.getValue().getValue().getType()) : null;

            if (localChange != null && localChange.getChanged() != null && !project.isDisposed(localChange.getChanged())) {
                localConflictedElements.add(localChange.getChanged());
            }
            if (jmsChange != null && jmsChange.getChanged() != null) {
                jmsConflictedElements.add(jmsChange.getChanged());
            }
        }

        ElementValidator elementValidator = new ElementValidator("CSync Conflict Validation", ElementValidator.buildElementPairs(localConflictedElements, project), jmsConflictedElements, project);
        elementValidator.run(progressStatus);
        if (!elementValidator.getInvalidElements().isEmpty()) {
            Application.getInstance().getGUILog().log("[INFO] There are potential conflicts in " + elementValidator.getInvalidElements().size() + " element" + (elementValidator.getInvalidElements().size() != 1 ? "s" : "") + " between MMS and local changes. Please resolve them and re-sync.");
            vss.add(elementValidator.getValidationSuite());
            Utils.displayValidationWindow(project, elementValidator.getValidationSuite(), "Delta Sync Conflict Validation");

            for (Map.Entry<String, Pair<Changelog.Change<Element>, Changelog.Change<Void>>> conflictedEntry : conflictedChanges.entrySet()) {
                String id = conflictedEntry.getKey();
                if (!elementValidator.getInvalidElements().containsKey(id)) {
                    continue;
                }
                Changelog.Change<Element> localChange = conflictedEntry.getValue().getKey();
                Changelog.Change<ObjectNode> jmsChange = conflictedEntry.getValue().getValue() != null ?
                        new Changelog.Change<>(jmsJsons.get(id), conflictedEntry.getValue().getValue().getType()) : null;

                if (localChange != null && (localChange.getChanged() != null || Changelog.ChangeType.DELETED.equals(localChange.getType()))) {
                    failedLocalChangelog.addChange(conflictedEntry.getKey(), localChange.getChanged(), localChange.getType());
                }
                if (jmsChange != null && (jmsChange.getChanged() != null || Changelog.ChangeType.DELETED.equals(jmsChange.getType()))) {
                    failedJmsChangelog.addChange(conflictedEntry.getKey(), null, jmsChange.getType());
                }
            }
        }

        // CLEAR IN-MEMORY AND PERSIST UNPROCESSED & FAILURES

        listener.getInMemoryLocalChangelog().clear();
        jmsMessageListener.getInMemoryJMSChangelog().clear();

        listener.setDisabled(true);
        if (!SessionManager.getInstance().isSessionCreated()) {
            SessionManager.getInstance().createSession("Delta Sync Changelog Persistence");
        }

        Changelog<String, Void> unprocessedLocalChangelog = new Changelog<>();
        if (!shouldCommit) {
            unprocessedLocalChangelog = unprocessedLocalChangelog.and(localChangelog, (s, element) -> null);
        }
        if (shouldCommit && !shouldCommitDeletes) {
            Map<String, Void> unprocessedLocalDeletedChanges = unprocessedLocalChangelog.get(Changelog.ChangeType.DELETED);
            for (String key : localChangelog.get(Changelog.ChangeType.DELETED).keySet()) {
                unprocessedLocalDeletedChanges.put(key, null);
            }
        }
        unprocessedLocalChangelog = unprocessedLocalChangelog.and(failedLocalChangelog, (s, element) -> null);
        try {
            SyncElements.setByType(project, SyncElement.Type.LOCAL, JacksonUtils.getObjectMapper().writeValueAsString(SyncElements.buildJson(unprocessedLocalChangelog)));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Changelog<String, Void> unprocessedJmsChangelog = new Changelog<>();
        if (!shouldUpdate) {
            unprocessedJmsChangelog = unprocessedJmsChangelog.and(jmsChangelog);
        }
        unprocessedJmsChangelog = unprocessedJmsChangelog.and(failedJmsChangelog);
        try {
            SyncElements.setByType(project, SyncElement.Type.MMS, JacksonUtils.getObjectMapper().writeValueAsString(SyncElements.buildJson(unprocessedJmsChangelog)));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        SessionManager.getInstance().closeSession();
        listener.setDisabled(false);

        // SUCCESS
        failure = false;
    }

    public Changelog<String, Void> getSuccessfulJmsChangelog() {
        return successfulJmsChangelog;
    }

    public boolean isFailure() {
        return failure;
    }

    public List<ValidationSuite> getValidations() {
        return vss;
    }

}
