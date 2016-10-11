package gov.nasa.jpl.mbee.mdk.ems.sync.delta;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.MDKPlugin;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRule;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ViolationSeverity;
import gov.nasa.jpl.mbee.mdk.ems.ExportUtility;
import gov.nasa.jpl.mbee.mdk.ems.MMSUtils;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
import gov.nasa.jpl.mbee.mdk.ems.actions.UpdateClientElementAction;
import gov.nasa.jpl.mbee.mdk.ems.sync.jms.JMSMessageListener;
import gov.nasa.jpl.mbee.mdk.ems.sync.jms.JMSSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.mdk.ems.sync.local.LocalSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.mdk.ems.sync.local.LocalSyncTransactionCommitListener;
import gov.nasa.jpl.mbee.mdk.ems.sync.queue.OutputQueue;
import gov.nasa.jpl.mbee.mdk.ems.sync.queue.Request;
import gov.nasa.jpl.mbee.mdk.ems.validation.ElementValidator;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.lib.Changelog;
import gov.nasa.jpl.mbee.mdk.lib.MDUtils;
import gov.nasa.jpl.mbee.mdk.lib.Pair;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import gov.nasa.jpl.mbee.mdk.options.MDKOptionsGroup;

import java.io.IOException;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class DeltaSyncRunner implements RunnableWithProgress {
    private final boolean shouldCommitDeletes, shouldCommit, shouldUpdate;

    private final Project project = Application.getInstance().getProject();

    private boolean failure = true;

    private ValidationSuite changelogSuite = new ValidationSuite("Updated Elements/Failed Updates");
    private ValidationRule locallyChangedValidationRule = new ValidationRule("updated", "updated", ViolationSeverity.INFO);
    private ValidationRule cannotUpdate = new ValidationRule("cannotUpdate", "cannotUpdate", ViolationSeverity.ERROR);
    private ValidationRule cannotRemove = new ValidationRule("cannotDelete", "cannotDelete", ViolationSeverity.WARNING);
    private ValidationRule cannotCreate = new ValidationRule("cannotCreate", "cannotCreate", ViolationSeverity.ERROR);

    private Changelog<String, Element> failedLocalChangelog = new Changelog<>();
    private Changelog<String, Void> failedJmsChangelog = new Changelog<>(), successfulJmsChangelog = new Changelog<>();

    private List<ValidationSuite> vss = new ArrayList<>();

    {
        changelogSuite.addValidationRule(locallyChangedValidationRule);
        changelogSuite.addValidationRule(cannotUpdate);
        changelogSuite.addValidationRule(cannotRemove);
        changelogSuite.addValidationRule(cannotCreate);
    }

    /*public DeltaSyncRunner(boolean shouldCommit, boolean skipUpdate, boolean shouldDelete) {
        this.shouldCommit = shouldCommit;
        this.skipUpdate = skipUpdate;
        this.shouldDelete = shouldDelete;
    }*/

    @Deprecated
    public DeltaSyncRunner(boolean shouldCommit, boolean shouldCommitDeletes) {
        this(shouldCommit, shouldCommitDeletes, true);
    }

    public DeltaSyncRunner(boolean shouldCommmit, boolean shouldCommitDeletes, boolean shouldUpdate) {
        this.shouldCommit = shouldCommmit;
        this.shouldCommitDeletes = shouldCommitDeletes;
        this.shouldUpdate = shouldUpdate;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(ProgressStatus ps) {
        ps.setDescription("Initializing");
        // TODO Abstract to common sync checks @donbot
        if (ProjectUtilities.isFromTeamworkServer(project.getPrimaryProject()) && TeamworkUtils.getLoggedUserName() == null) {
            Utils.guilog("[ERROR] You need to be logged in to Teamwork first.");
            return;
        }

        LocalSyncTransactionCommitListener listener = LocalSyncProjectEventListenerAdapter.getProjectMapping(project).getLocalSyncTransactionCommitListener();
        if (listener == null) {
            Utils.guilog("[ERROR] Unexpected error occurred. Cannot get commit listener.");
            return;
        }

        String url = ExportUtility.getUrl(project);
        if (url == null || url.isEmpty()) {
            Application.getInstance().getGUILog().log("[ERROR] Url not specified. Skipping sync. All changes will be re-attempted in the next sync.");
            return;
        }
        String site = ExportUtility.getSite();
        if (site == null || site.isEmpty()) {
            Application.getInstance().getGUILog().log("[ERROR] Site not specified. Skipping sync. All changes will be re-attempted in the next sync.");
            return;
        }
        try {
            if (!ExportUtility.hasSiteEditPermission(url, site)) {
                Application.getInstance().getGUILog().log("[ERROR] User does not have sufficient permissions on MMS or the site/url is misconfigured. Skipping sync. All changes will be re-attempted in the next sync.");
                return;
            }
        } catch (ServerException e) {
            e.printStackTrace();
            Application.getInstance().getGUILog().log("[ERROR] An error occurred while verifying site permissions. Skipping sync. All changes will be re-attempted in the next sync. Error: " + e.getMessage());
            return;
        }

        // LOCK SYNC FOLDER

        listener.setDisabled(true);
        SyncElements.lockSyncFolder(project);
        listener.setDisabled(false);

        // DOWNLOAD MMS MESSAGES IF ASYNC CONSUMER IS DISABLED

        JMSSyncProjectEventListenerAdapter.JMSSyncProjectMapping jmsSyncProjectMapping = JMSSyncProjectEventListenerAdapter.getProjectMapping(Application.getInstance().getProject());
        JMSMessageListener jmsMessageListener = jmsSyncProjectMapping.getJmsMessageListener();
        if (jmsMessageListener == null) {
            if (MDKOptionsGroup.getMDKOptions().isChangeListenerEnabled()) {
                Application.getInstance().getGUILog().log("[ERROR] Not connected to MMS queue. Skipping sync. All changes will be re-attempted in the next sync.");
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
        Collection<SyncElement> persistedLocalSyncElements = SyncElements.getAllOfType(project, SyncElement.Type.LOCAL);
        for (SyncElement syncElement : persistedLocalSyncElements) {
            persistedLocalChangelog = persistedLocalChangelog.and(SyncElements.buildChangelog(syncElement), (key, value) -> Converters.getIdToElementConverter().apply(key, project));
        }
        Changelog<String, Element> localChangelog = persistedLocalChangelog.and(listener.getInMemoryLocalChangelog());


        Map<String, Element> localCreated = localChangelog.get(Changelog.ChangeType.CREATED),
                localUpdated = localChangelog.get(Changelog.ChangeType.UPDATED),
                localDeleted = localChangelog.get(Changelog.ChangeType.DELETED);

        // BUILD COMPLETE MMS CHANGELOG

        Changelog<String, Void> persistedJmsChangelog = new Changelog<>();
        Collection<SyncElement> persistedJmsSyncElements = SyncElements.getAllOfType(project, SyncElement.Type.MMS);
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
            ps.setDescription("Getting " + elementIdsToGet.size() + " added/changed element" + (elementIdsToGet.size() != 1 ? "s" : "") + " from MMS");
            ObjectNode response = null;
            try {
                response = MMSUtils.getElementsById(elementIdsToGet, ps);
            } catch (ServerException | IOException e) {
                if (!ps.isCancel()) {
                    Application.getInstance().getGUILog().log("[ERROR] Cannot get elements from MMS. Sync aborted. All changes will be attempted at next update.");
                }
            }
            if (ps.isCancel()) {
                Application.getInstance().getGUILog().log("Sync manually aborted. All changes will be attempted at next update.");
                return;
            }
            if (response == null) {
                Utils.guilog("[ERROR] Cannot get elements from MMS server. Sync aborted. All changes will be attempted at next update.");
                return;
            }
            ArrayNode elementsArrayNode = (ArrayNode) response.get("elements");
            for (JsonNode jsonNode : elementsArrayNode) {
                if (!jsonNode.isObject()) {
                    continue;
                }
                String webId = jsonNode.get(MDKConstants.SYSML_ID_KEY).asText();
                jmsJsons.put(webId, (ObjectNode) jsonNode);
            }
        }

        // NEW CONFLICT DETECTION

        ps.setDescription("Detecting conflicts");
        Map<String, Pair<Changelog.Change<Element>, Changelog.Change<Void>>> conflictedChanges = new LinkedHashMap<>(),
                unconflictedChanges = new LinkedHashMap<>();
        localChangelog.findConflicts(jmsChangelog, new BiPredicate<Changelog.Change<Element>, Changelog.Change<Void>>() {
            @Override
            public boolean test(Changelog.Change<Element> change, Changelog.Change<Void> change2) {
                return change != null && change2 != null;
            }
        }, conflictedChanges, unconflictedChanges);

        // MAP CHANGES TO ACTIONABLE GROUPS

        Map<String, Element> localElementsToPost = new LinkedHashMap<>(localCreated.size() + localUpdated.size());
        Set<String> localElementsToDelete = new HashSet<>(localDeleted.size());

        Map<String, ObjectNode> jmsElementsToCreateLocally = new LinkedHashMap<>(jmsCreated.size());
        Map<String, Pair<ObjectNode, Element>> jmsElementsToUpdateLocally = new LinkedHashMap<>(jmsUpdated.size());
        Map<String, Element> jmsElementsToDeleteLocally = new LinkedHashMap<>(jmsDeleted.size());

        // only one side of the pair will have a value when unconflicted
        for (Map.Entry<String, Pair<Changelog.Change<Element>, Changelog.Change<Void>>> unconflictedEntry : unconflictedChanges.entrySet()) {
            String id = unconflictedEntry.getKey();
            Changelog.Change<Element> localChange = unconflictedEntry.getValue().getFirst();
            Changelog.Change<ObjectNode> jmsChange = unconflictedEntry.getValue().getSecond() != null ?
                    new Changelog.Change<>(jmsJsons.get(id), unconflictedEntry.getValue().getSecond().getType()) : null;

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
                        localElementsToDelete.add(id);
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
                        if (!element.isEditable()) {
                            if (MDUtils.isDeveloperMode()) {
                                Application.getInstance().getGUILog().log("[INFO] Attempted to update element " + id + " locally, but it is not editable. Skipping.");
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
                        if (!element.isEditable()) {
                            if (MDUtils.isDeveloperMode()) {
                                Application.getInstance().getGUILog().log("[INFO] Attempted to delete element " + id + " locally, but it is not editable. Skipping.");
                            }
                            failedJmsChangelog.addChange(id, null, Changelog.ChangeType.DELETED);
                            continue;
                        }
                        jmsElementsToDeleteLocally.put(id, element);
                        break;
                }
            }
        }

        if (ps.isCancel()) {
            Application.getInstance().getGUILog().log("[INFO] Sync manually aborted. All changes will be attempted at next update.");
            return;
        }

        // COMMIT UNCONFLICTED CREATIONS AND UPDATES TO MMS

        boolean shouldLogNoLocalChanges = shouldCommit;
        if (shouldCommit && !localElementsToPost.isEmpty()) {
            ps.setDescription("Committing creations and updates to MMS");

            ArrayNode elementsArrayNode = JacksonUtils.getObjectMapper().createArrayNode();
            for (Element element : localElementsToPost.values()) {
                ObjectNode elementObjectNode = Converters.getElementToJsonConverter().apply(element, project);
                if (elementObjectNode != null) {
                    elementsArrayNode.add(elementObjectNode);
                }
            }
            if (elementsArrayNode.size() > 0) {
                ObjectNode body = JacksonUtils.getObjectMapper().createObjectNode();
                body.set("elements", elementsArrayNode);
                body.put("source", "magicdraw");
                body.put("mmsVersion", MDKPlugin.VERSION);
                Application.getInstance().getGUILog().log("[INFO] Queueing request to create/update " + elementsArrayNode.size() + " local element" + (elementsArrayNode.size() != 1 ? "s" : "") + " on the MMS.");
                try {
                    OutputQueue.getInstance().offer(new Request(ExportUtility.getPostElementsUrl(), JacksonUtils.getObjectMapper().writeValueAsString(body), "POST", true, elementsArrayNode.size(), "Sync Changes"));
                } catch (JsonProcessingException e) {
                    Application.getInstance().getGUILog().log("[ERROR] Unexpected JSON processing exception. See logs for more information.");
                    e.printStackTrace();
                }
                shouldLogNoLocalChanges = false;
            }
        }

        // COMMIT UNCONFLICTED DELETIONS TO MMS
        // NEEDS TO BE AFTER LOCAL; EX: MOVE ELEMENT OUT ON MMS, DELETE OWNER LOCALLY, WHAT HAPPENS?

        if (shouldCommit && shouldCommitDeletes && !localElementsToDelete.isEmpty()) {
            ps.setDescription("Committing deletions to MMS");

            ArrayNode elementsArrayNode = JacksonUtils.getObjectMapper().createArrayNode();
            for (String id : localElementsToDelete) {
                ObjectNode elementObjectNode = JacksonUtils.getObjectMapper().createObjectNode();
                elementObjectNode.put(MDKConstants.SYSML_ID_KEY, id);
                elementsArrayNode.add(elementObjectNode);
            }
            ObjectNode body = JacksonUtils.getObjectMapper().createObjectNode();
            body.set("elements", elementsArrayNode);
            body.put("source", "magicdraw");
            body.put("mmsVersion", MDKPlugin.VERSION);
            Application.getInstance().getGUILog().log("[INFO] Queuing request to delete " + elementsArrayNode.size() + " local element" + (elementsArrayNode.size() != 1 ? "s" : "") + " on the MMS.");
            try {
                OutputQueue.getInstance().offer(new Request(ExportUtility.getUrlWithWorkspace() + "/elements", JacksonUtils.getObjectMapper().writeValueAsString(body), "DELETEALL", true, elementsArrayNode.size(), "Sync Deletes"));
            } catch (JsonProcessingException e) {
                Application.getInstance().getGUILog().log("[ERROR] Unexpected JSON processing exception. See logs for more information.");
                e.printStackTrace();
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
            jmsElementsToUpdateLocally.values().forEach(pair -> jmsElementsToCreateOrUpdateLocally.add(pair.getFirst()));

            UpdateClientElementAction updateClientElementAction = new UpdateClientElementAction(project);
            updateClientElementAction.process(jmsElementsToCreateOrUpdateLocally, jmsElementsToDeleteLocally.values().stream().map(Converters.getElementToIdConverter()).filter(id -> id != null).collect(Collectors.toList()));
            failedJmsChangelog = failedJmsChangelog.and(updateClientElementAction.getFailedChangelog(), (id, objectNode) -> null);

            listener.setDisabled(false);
        }

        // HANDLE CONFLICTS

        ps.setDescription("Finishing up");

        Set<Element> localConflictedElements = new HashSet<>();
        Set<ObjectNode> jmsConflictedElements = new HashSet<>();

        for (Map.Entry<String, Pair<Changelog.Change<Element>, Changelog.Change<Void>>> conflictedEntry : conflictedChanges.entrySet()) {
            String id = conflictedEntry.getKey();
            Changelog.Change<Element> localChange = conflictedEntry.getValue().getFirst();
            Changelog.Change<ObjectNode> jmsChange = conflictedEntry.getValue().getSecond() != null ?
                    new Changelog.Change<>(jmsJsons.get(id), conflictedEntry.getValue().getSecond().getType()) : null;

            if (localChange != null && localChange.getChanged() != null && !project.isDisposed(localChange.getChanged())) {
                localConflictedElements.add(localChange.getChanged());
            }
            if (jmsChange != null && jmsChange.getChanged() != null) {
                jmsConflictedElements.add(jmsChange.getChanged());
            }
        }

        ElementValidator elementValidator = new ElementValidator(ElementValidator.buildElementPairs(localConflictedElements, project), jmsConflictedElements, project);
        elementValidator.run(ps);
        if (!elementValidator.getInvalidElements().isEmpty()) {
            Application.getInstance().getGUILog().log("[INFO] There are potential conflicts in " + elementValidator.getInvalidElements().size() + " element" + (elementValidator.getInvalidElements().size() != 1 ? "s" : "") + " between MMS and local changes. Please resolve them and re-sync.");
            vss.add(elementValidator.getValidationSuite());
            Utils.displayValidationWindow(elementValidator.getValidationSuite(), "Delta Sync Conflict Validation");

            for (Map.Entry<String, Pair<Changelog.Change<Element>, Changelog.Change<Void>>> conflictedEntry : conflictedChanges.entrySet()) {
                String id = conflictedEntry.getKey();
                if (!elementValidator.getInvalidElements().containsKey(id)) {
                    continue;
                }
                Changelog.Change<Element> localChange = conflictedEntry.getValue().getFirst();
                Changelog.Change<ObjectNode> jmsChange = conflictedEntry.getValue().getSecond() != null ?
                        new Changelog.Change<>(jmsJsons.get(id), conflictedEntry.getValue().getSecond().getType()) : null;

                if (localChange != null && localChange.getChanged() != null || Changelog.ChangeType.DELETED.equals(localChange.getType())) {
                    failedLocalChangelog.addChange(conflictedEntry.getKey(), localChange.getChanged(), localChange.getType());
                }
                if (jmsChange != null && jmsChange.getChanged() != null || Changelog.ChangeType.DELETED.equals(jmsChange.getType())) {
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
