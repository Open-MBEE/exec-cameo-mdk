package gov.nasa.jpl.mbee.ems.sync.delta;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.DocGenPlugin;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.ImportException;
import gov.nasa.jpl.mbee.ems.ImportUtility;
import gov.nasa.jpl.mbee.ems.ServerException;
import gov.nasa.jpl.mbee.ems.sync.Request;
import gov.nasa.jpl.mbee.ems.sync.common.CommonSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.ems.sync.common.CommonSyncTransactionCommitListener;
import gov.nasa.jpl.mbee.ems.sync.jms.JMSSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.ems.validation.ModelValidator;
import gov.nasa.jpl.mbee.ems.validation.actions.DetailDiff;
import gov.nasa.jpl.mbee.lib.Changelog;
import gov.nasa.jpl.mbee.lib.Pair;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.function.BiPredicate;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.jms.TextMessage;
import java.util.*;

public class DeltaSyncRunner implements RunnableWithProgress {
    private static Map<String, Changelog.ChangeType> PERSISTED_KEY_CHANGE_MAPPING = new LinkedHashMap<>(3);

    static {
        PERSISTED_KEY_CHANGE_MAPPING.put("added", Changelog.ChangeType.CREATED);
        PERSISTED_KEY_CHANGE_MAPPING.put("deleted", Changelog.ChangeType.DELETED);
        PERSISTED_KEY_CHANGE_MAPPING.put("changed", Changelog.ChangeType.UPDATED);
    }

    private boolean shouldDelete, shouldCommit, shouldUpdate;

    private Logger log = Logger.getLogger(getClass());
    private Project project = Application.getInstance().getProject();

    private boolean failure = false;
    private boolean skipUpdate = false;

    private ValidationSuite changelogSuite = new ValidationSuite("Updated Elements/Failed Updates");
    private ValidationRule locallyChangedValidationRule = new ValidationRule("updated", "updated", ViolationSeverity.INFO);
    private ValidationRule cannotUpdate = new ValidationRule("cannotUpdate", "cannotUpdate", ViolationSeverity.ERROR);
    private ValidationRule cannotRemove = new ValidationRule("cannotDelete", "cannotDelete", ViolationSeverity.WARNING);
    private ValidationRule cannotCreate = new ValidationRule("cannotCreate", "cannotCreate", ViolationSeverity.ERROR);
    private Changelog<String, Void> unmodifiableChangelog = new Changelog<>();

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

    public DeltaSyncRunner(boolean shouldCommit, boolean shouldDelete) {
        this(shouldCommit, shouldDelete, true);
    }

    public DeltaSyncRunner(boolean shouldCommmit, boolean shouldDelete, boolean shouldUpdate) {
        this.shouldCommit = shouldCommmit;
        this.shouldDelete = shouldDelete;
        this.shouldUpdate = shouldUpdate;
    }

    public Changelog<String, Void> getUnmodifiableChangelog() {
        return unmodifiableChangelog;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(ProgressStatus ps) {
        if (ProjectUtilities.isFromTeamworkServer(project.getPrimaryProject())) {
            if (TeamworkUtils.getLoggedUserName() == null) {
                failure = true;
                Utils.guilog("[ERROR] You need to be logged in to Teamwork first.");
                return;
            }
        }

        CommonSyncTransactionCommitListener listener = CommonSyncProjectEventListenerAdapter.getProjectMapping(project).getCommonSyncTransactionCommitListener();
        if (listener == null) {
            Utils.guilog("[ERROR] Unexpected error occurred. Cannot get commit listener.");
            failure = true;
            return;
        }

        // LOCK SYNC FOLDER

        listener.setDisabled(true);
        DeltaSyncProjectEventListenerAdapter.lockSyncFolder(project);
        listener.setDisabled(false);

        // DOWNLOAD JMS MESSAGES IF ASYNC CONSUMER NOT ENABLED

        JMSSyncProjectEventListenerAdapter.JMSSyncProjectMapping jmsSyncProjectMapping = JMSSyncProjectEventListenerAdapter.getProjectMapping(Application.getInstance().getProject());
        if (jmsSyncProjectMapping.isDisabled()) {
            List<TextMessage> textMessages = jmsSyncProjectMapping.getAllTextMessages(true);
            if (textMessages == null) {
                Utils.guilog("[ERROR] Could not get changes from MMS. Please check your network connection and try again.");
                failure = true;
                return;
            }
            for (TextMessage textMessage : textMessages) {
                jmsSyncProjectMapping.getJmsMessageListener().onMessage(textMessage);
            }
        }

        /*Changelog<String, Void> jmsChangelog = new Changelog<>();
        Map<String, Void> jmsAdded = jmsChangelog.get(Changelog.ChangeType.CREATED),
                jmsUpdated = jmsChangelog.get(Changelog.ChangeType.UPDATED),
                jmsDeleted = jmsChangelog.get(Changelog.ChangeType.DELETED);

        for (TextMessage textMessage : jmsTextMessages) {
            String text;
            try {
                text = textMessage.getText();
            } catch (JMSException e) {
                e.printStackTrace();
                continue;
            }
            Application.getInstance().getGUILog().log(text);
            JSONObject ob = (JSONObject) JSONValue.parse(text);
            boolean fromMagicDraw = ob.get("source") != null && ob.get("source").equals("magicdraw");
            JSONObject workspace2 = (JSONObject) ob.get("workspace2");
            if (workspace2 == null) {
                continue;
            }

            JSONArray updated = (JSONArray) workspace2.get("updatedElements");
            JSONArray added = (JSONArray) workspace2.get("addedElements");
            JSONArray deleted = (JSONArray) workspace2.get("deletedElements");
            JSONArray moved = (JSONArray) workspace2.get("movedElements");

            for (Object e : updated) {
                String id = (String) ((JSONObject) e).get("sysmlid");
                if (!fromMagicDraw) {
                    jmsUpdated.put(id, null);
                }
                jmsDeleted.remove(id);
            }
            for (Object e : added) {
                String id = (String) ((JSONObject) e).get("sysmlid");
                if (!fromMagicDraw) {
                    jmsAdded.put(id, null);
                }
                jmsDeleted.remove(id);
            }
            for (Object e : moved) {
                String id = (String) ((JSONObject) e).get("sysmlid");
                if (!fromMagicDraw) {
                    jmsUpdated.put(id, null);
                }
                jmsDeleted.remove(id);
            }
            for (Object e : deleted) {
                String id = (String) ((JSONObject) e).get("sysmlid");
                if (!fromMagicDraw) {
                    jmsUpdated.put(id, null);
                }
                jmsAdded.remove(id);
                jmsUpdated.remove(id);
            }
        }*/


        /*JSONObject previousUpdates = DeltaSyncProjectEventListenerAdapter.getUpdatesOrFailed(Application.getInstance().getProject(), "update");
        if (previousUpdates != null) {
            for (String added : (List<String>) previousUpdates.get("added")) {
                if (localCreated.containsKey(added) || localUpdated.containsKey(added)) {
                    continue;
                }
                Element e = ExportUtility.getElementFromID(added);
                if (e != null) {
                    localCreated.put(added, e);
                }
            }
            for (String updated : (List<String>) previousUpdates.get("changed")) {
                if (!localUpdated.containsKey(updated)) {
                    Element e = ExportUtility.getElementFromID(updated);
                    if (e != null) {
                        localUpdated.put(updated, e);
                    }
                }
                localCreated.remove(updated);
            }
            for (String deleted : (List<String>) previousUpdates.get("deleted")) {
                if (ExportUtility.getElementFromID(deleted) != null) {
                    localDeleted.remove(deleted);
                    continue; //not deleted?
                }
                localDeleted.put(deleted, null);
                localCreated.remove(deleted);
                localUpdated.remove(deleted);
            }
        }*/

        // GET CONFLICTS

        // JSONObject conflictedChanges = DeltaSyncProjectEventListenerAdapter.getConflicts(Application.getInstance().getProject());

        // BUILD COMPLETE LOCAL CHANGELOG

        Changelog<String, Element> persistedLocalChangelog = new Changelog<>();
        JSONObject persistedLocalChanges = DeltaSyncProjectEventListenerAdapter.getUpdatesOrFailed(Application.getInstance().getProject(), "update");
        for (Map.Entry<String, Changelog.ChangeType> entry : PERSISTED_KEY_CHANGE_MAPPING.entrySet()) {
            if (persistedLocalChanges != null) {
                Object o = persistedLocalChanges.get(entry.getKey());
                if (Collection.class.isAssignableFrom(o.getClass())) {
                    for (Object collectionObject : (Collection) o) {
                        if (collectionObject instanceof String) {
                            Element element = ExportUtility.getElementFromID((String) collectionObject);
                            persistedLocalChangelog.addChange((String) collectionObject, element, entry.getValue());
                        }
                    }
                }
            }
            /*if (conflictedChanges != null) {
                Object o = conflictedChanges.get(entry.getKey());
                if (Collection.class.isAssignableFrom(o.getClass())) {
                    for (Object collectionObject : (Collection) o) {
                        if (collectionObject instanceof String) {
                            Element element = ExportUtility.getElementFromID((String) collectionObject);
                            persistedLocalChangelog.addChange((String) collectionObject, element, entry.getValue());
                        }
                    }
                }
            }*/
        }
        Changelog<String, Element> localChangelog = persistedLocalChangelog.and(listener.getInMemoryLocalChangelog());


        Map<String, Element> localCreated = localChangelog.get(Changelog.ChangeType.CREATED),
                localUpdated = localChangelog.get(Changelog.ChangeType.UPDATED),
                localDeleted = localChangelog.get(Changelog.ChangeType.DELETED);

        // BUILD COMPLETE JMS CHANGELOG

        Changelog<String, Void> persistedJmsChangelog = new Changelog<>();
        JSONObject persistedJmsChanges = DeltaSyncProjectEventListenerAdapter.getUpdatesOrFailed(Application.getInstance().getProject(), "jms");
        for (Map.Entry<String, Changelog.ChangeType> entry : PERSISTED_KEY_CHANGE_MAPPING.entrySet()) {
            if (persistedJmsChanges != null) {
                Object o = persistedJmsChanges.get(entry.getKey());
                if (Collection.class.isAssignableFrom(o.getClass())) {
                    for (Object collectionObject : (Collection) o) {
                        if (collectionObject instanceof String) {
                            persistedJmsChangelog.addChange((String) collectionObject, null, entry.getValue());
                        }
                    }
                }
            }
            /*if (conflictedChanges != null) {
                Object o = conflictedChanges.get(entry.getKey());
                if (Collection.class.isAssignableFrom(o.getClass())) {
                    for (Object collectionObject : (Collection) o) {
                        if (collectionObject instanceof String) {
                            persistedLocalChangelog.addChange((String) collectionObject, null, entry.getValue());
                        }
                    }
                }
            }*/
        }
        Changelog<String, Void> jmsChangelog = persistedJmsChangelog.clone();
        for (Changelog.ChangeType changeType : Changelog.ChangeType.values()) {
            for (Map.Entry<String, JSONObject> entry : jmsSyncProjectMapping.getJmsMessageListener().getInMemoryJMSChangelog().get(changeType).entrySet()) {
                jmsChangelog.addChange(entry.getKey(), null, changeType);
            }
        }

        Map<String, Void> jmsCreated = jmsChangelog.get(Changelog.ChangeType.CREATED),
                jmsUpdated = jmsChangelog.get(Changelog.ChangeType.UPDATED),
                jmsDeleted = jmsChangelog.get(Changelog.ChangeType.DELETED);


        Set<String> elementIdsToGet = new HashSet<>(jmsUpdated.keySet());
        elementIdsToGet.addAll(jmsCreated.keySet());
        if (shouldUpdate) {
            int size = jmsChangelog.size();
            Application.getInstance().getGUILog().log("[INFO] Getting " + size + " changed elements" + (size != 1 ? "s" : "") + " from the MMS.");
        }

        Map<String, JSONObject> jmsJsons = new HashMap<>(elementIdsToGet.size());

        // Get latest json for element added/changed from MMS

        if (!elementIdsToGet.isEmpty()) {
            JSONObject getJson = new JSONObject();
            JSONArray getElements = new JSONArray();
            getJson.put("elements", getElements);
            for (String e : elementIdsToGet) {
                JSONObject el = new JSONObject();
                el.put("sysmlid", e);
                getElements.add(el);
            }
            String url = ExportUtility.getUrlWithWorkspace();
            url += "/elements";
            String response = null;
            try {
                response = ExportUtility.getWithBody(url, getJson.toJSONString());
            } catch (ServerException ex) {
                Utils.guilog("[ERROR] Get elements failed.");
            }
            // TODO Reconsider @Ivan
            if (response == null) {
                JSONObject abort = new JSONObject();
                JSONArray abortChanged = new JSONArray();
                JSONArray abortDeleted = new JSONArray();
                JSONArray abortAdded = new JSONArray();
                abortChanged.addAll(jmsUpdated.keySet());
                abortDeleted.addAll(jmsDeleted.keySet());
                abortAdded.addAll(jmsCreated.keySet());
                abort.put("added", abortAdded);
                abort.put("deleted", abortDeleted);
                abort.put("changed", abortChanged);

                listener.setDisabled(true);
                DeltaSyncProjectEventListenerAdapter.lockSyncFolder(project);
                SessionManager.getInstance().createSession("failed changes");
                try {
                    DeltaSyncProjectEventListenerAdapter.setUpdatesOrFailed(project, abort, "error", true);
                    SessionManager.getInstance().closeSession();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SessionManager.getInstance().cancelSession();
                }
                listener.setDisabled(false);
                failure = true;
                Utils.guilog("[ERROR] Cannot get elements from MMS server. Update aborted. All changes will be attempted at next update.");
                return;
            }
            JSONObject webObject = (JSONObject) JSONValue.parse(response);
            JSONArray webArray = (JSONArray) webObject.get("elements");
            for (Object o : webArray) {
                String webId = (String) ((JSONObject) o).get("sysmlid");
                jmsJsons.put(webId, (JSONObject) o);
            }
        }

        // NEW CONFLICT DETECTION

        Map<String, Pair<Changelog.Change<Element>, Changelog.Change<Void>>> conflictedChanges = new LinkedHashMap<>(),
                unconflictedChanges = new LinkedHashMap<>();
        localChangelog.findConflicts(jmsChangelog, new BiPredicate<Changelog.Change<Element>, Changelog.Change<Void>>() {
            @Override
            public boolean test(Changelog.Change<Element> changes, Changelog.Change<Void> changes2) {
                return changes != null && changes2 != null;
            }
        }, conflictedChanges, unconflictedChanges);

        // MAP CHANGES TO ACTIONABLE GROUPS

        Map<String, Element> localElementsToPost = new LinkedHashMap<>(localCreated.size() + localUpdated.size());
        Set<String> localElementsToDelete = new HashSet<>(localDeleted.size());

        Map<String, JSONObject> jmsElementsToCreateLocally = new LinkedHashMap<>(jmsCreated.size());
        Map<String, Pair<JSONObject, Element>> jmsElementsToUpdateLocally = new LinkedHashMap<>(jmsUpdated.size());
        Map<String, Element> jmsElementsToDeleteLocally = new LinkedHashMap<>(jmsDeleted.size());

        // only one side of the pair will have a value
        for (Map.Entry<String, Pair<Changelog.Change<Element>, Changelog.Change<Void>>> unconflictedEntry : unconflictedChanges.entrySet()) {
            String id = unconflictedEntry.getKey();
            Changelog.Change<Element> localChange = unconflictedEntry.getValue().getFirst();
            Changelog.Change<JSONObject> jmsChange = unconflictedEntry.getValue().getSecond() != null ?
                    new Changelog.Change<>(jmsJsons.get(id), unconflictedEntry.getValue().getSecond().getSecond()) : null;

            if (shouldCommit && localChange != null) {
                Element element = localChange.getFirst();
                switch (localChange.getSecond()) {
                    case CREATED:
                    case UPDATED:
                        if (element == null) {
                            Application.getInstance().getGUILog().log("[INFO] Attempted to create/update element " + id + " on the MMS, but it no longer exists locally. Skipping.");
                            continue;
                        }
                        localElementsToPost.put(id, element);
                        break;
                    case DELETED:
                        if (element != null && !element.isInvalid()) {
                            Application.getInstance().getGUILog().log("[INFO] Attempted to delete element " + id + " from the MMS, but it still exists locally. Skipping.");
                            continue;
                        }
                        localElementsToDelete.add(id);
                        break;
                }
            }
            else if (shouldUpdate && jmsChange != null) {
                JSONObject json = jmsChange.getFirst();
                Element element = ExportUtility.getElementFromID(id);
                switch (jmsChange.getSecond()) {
                    case CREATED:
                        if (json == null) {
                            Application.getInstance().getGUILog().log("[INFO] Attempted to create element " + id + " locally, but it no longer exists on the MMS. Skipping.");
                            continue;
                        }
                        if (element != null) {
                            Application.getInstance().getGUILog().log("[INFO] Attempted to create element " + id + " locally, but it already exists. Skipping.");
                            continue;
                        }
                        jmsElementsToCreateLocally.put(id, json);
                        break;
                    case UPDATED:
                        if (json == null) {
                            Application.getInstance().getGUILog().log("[INFO] Attempted to update element " + id + " locally, but it no longer exists on the MMS. Skipping.");
                            continue;
                        }
                        if (element == null) {
                            Application.getInstance().getGUILog().log("[INFO] Attempted to update element " + id + " locally, but it does not exist. Skipping.");
                            continue;
                        }
                        if (!element.isEditable()) {
                            // TODO Remove me to stop being noisy @Ivan
                            Application.getInstance().getGUILog().log("[INFO] Attempted to update element " + id + " locally, but it is not editable. Skipping.");
                            continue;
                        }
                        jmsElementsToUpdateLocally.put(id, new Pair<>(json, element));
                        break;
                    case DELETED:
                        if (element == null) {
                            Application.getInstance().getGUILog().log("[INFO] Attempted to delete element " + id + " locally, but it doesn't exist. Skipping.");
                            continue;
                        }
                        if (!element.isEditable()) {
                            // TODO Remove me to stop being noisy @Ivan
                            Application.getInstance().getGUILog().log("[INFO] Attempted to delete element " + id + " locally, but it is not editable. Skipping.");
                            continue;
                        }
                        jmsElementsToDeleteLocally.put(id, element);
                        break;
                }
            }
        }

        // COMMIT UNCONFLICTED CREATIONS AND UPDATES TO MMS

        boolean shouldLogNoLocalChanges = !shouldCommit;
        if (shouldCommit && !localElementsToPost.isEmpty()) {
            JSONArray elementsJsonArray = new JSONArray();
            for (Element element : localElementsToPost.values()) {
                JSONObject elementJsonObject = ExportUtility.fillElement(element, null);
                if (elementJsonObject != null) {
                    elementsJsonArray.add(elementJsonObject);
                }
            }
            if (!elementsJsonArray.isEmpty()) {
                JSONObject body = new JSONObject();
                body.put("elements", elementsJsonArray);
                body.put("source", "magicdraw");
                body.put("mmsVersion", DocGenPlugin.VERSION);
                Application.getInstance().getGUILog().log("[INFO] Queueing request to create/update " + elementsJsonArray.size() + " local element" + (elementsJsonArray.size() != 1 ? "s" : "") + " on the MMS.");
                gov.nasa.jpl.mbee.ems.sync.queue.OutputQueue.getInstance().offer(new Request(ExportUtility.getPostElementsUrl(), body.toJSONString(), "POST", true, elementsJsonArray.size(), "Sync Changes"));
                shouldLogNoLocalChanges = true;
            }
        }

        // COMMIT UNCONFLICTED DELETIONS TO MMS

        if (shouldCommit && shouldDelete && !localElementsToDelete.isEmpty()) {
            Application.getInstance().getGUILog().log("[INFO] Adding local deletions to MMS request queue.");
            JSONArray elementsJsonArray = new JSONArray();
            for (String id : localElementsToDelete) {
                JSONObject elementJsonObject = new JSONObject();
                elementJsonObject.put("sysmlid", id);
                elementsJsonArray.add(elementJsonObject);
            }
            JSONObject body = new JSONObject();
            body.put("elements", elementsJsonArray);
            body.put("source", "magicdraw");
            body.put("mmsVersion", DocGenPlugin.VERSION);
            Application.getInstance().getGUILog().log("[INFO] Queuing request to delete " + elementsJsonArray.size() + " local element" + (elementsJsonArray.size() != 1 ? "s" : "") + " on the MMS.");
            gov.nasa.jpl.mbee.ems.sync.queue.OutputQueue.getInstance().offer(new Request(ExportUtility.getUrlWithWorkspace() + "/elements", body.toJSONString(), "DELETEALL", true, elementsJsonArray.size(), "Sync Deletes"));
            shouldLogNoLocalChanges = true;
        }

        // OUTPUT RESULT OF LOCAL CHANGES

        if (!shouldLogNoLocalChanges) {
            Application.getInstance().getGUILog().log("[INFO] No local changes to commit to MMS.");
        }

        // ADD CREATED ELEMENTS FROM MMS

        boolean shouldLogNoJmsChanges = !shouldUpdate;
        if (shouldUpdate && !jmsElementsToCreateLocally.isEmpty()) {
            ImportUtility.CreationOrder creationOrder = ImportUtility.getCreationOrder(jmsElementsToCreateLocally.values());
            List<JSONObject> sortedJmsElementsToCreateLocally = creationOrder.getOrder();
            if (!sortedJmsElementsToCreateLocally.isEmpty()) {
                listener.setDisabled(true);
                if (!SessionManager.getInstance().isSessionCreated()) {
                    SessionManager.getInstance().createSession("DeltaSyncRunner execution");
                }
                ImportUtility.setShouldOutputError(false);
                List<JSONObject> createdElementJsons = new ArrayList<>(sortedJmsElementsToCreateLocally.size());
                for (JSONObject elementJson : sortedJmsElementsToCreateLocally) {
                    try {
                        Element element = ImportUtility.createElement(elementJson, false);
                        if (element != null) {
                            createdElementJsons.add(elementJson);
                        }
                    } catch (ImportException ie) {
                        ie.printStackTrace();
                        unmodifiableChangelog.addChange((String) elementJson.get("sysmlid"), null, Changelog.ChangeType.CREATED);
                        ValidationRuleViolation vrv = new ValidationRuleViolation(null, "[CREATE FAILED] " + ie.getMessage());
                        vrv.addAction(new DetailDiff(new JSONObject(), elementJson));
                        cannotCreate.addViolation(vrv);
                    }
                }
                List<Element> createdElements = new ArrayList<>(createdElementJsons.size());
                for (JSONObject elementJson : createdElementJsons) {
                    try {
                        Element element = ImportUtility.createElement(elementJson, true);
                        createdElements.add(element);
                        locallyChangedValidationRule.addViolation(new ValidationRuleViolation(element, "[CREATED]"));
                    } catch (ImportException ie) {
                        ie.printStackTrace();
                        unmodifiableChangelog.addChange((String) elementJson.get("sysmlid"), null, Changelog.ChangeType.CREATED);
                        ValidationRuleViolation vrv = new ValidationRuleViolation(null, "[CREATE FAILED] " + ie.getMessage());
                        vrv.addAction(new DetailDiff(new JSONObject(), elementJson));
                        cannotCreate.addViolation(vrv);
                    }
                }
                listener.setDisabled(false);
                ImportUtility.setShouldOutputError(true);
                if (!createdElements.isEmpty()) {
                    shouldLogNoJmsChanges = false;
                    Application.getInstance().getGUILog().log("[INFO] Added " + createdElements.size() + " element" + (createdElements.size() != 1 ? "s" : "") + " locally from the MMS.");
                }
                for (JSONObject element : creationOrder.getFailed()) {
                    unmodifiableChangelog.addChange((String) element.get("sysmlid"), null, Changelog.ChangeType.CREATED);
                    ValidationRuleViolation vrv = new ValidationRuleViolation(null, "[CREATE FAILED] Owner or chain of owners not found");
                    vrv.addAction(new DetailDiff(new JSONObject(), element));
                    cannotCreate.addViolation(vrv);
                }
            }
        }

        // CHANGE UPDATED ELEMENTS FROM MMS

        if (shouldUpdate && !jmsElementsToUpdateLocally.isEmpty()) {
            listener.setDisabled(true);
            if (!SessionManager.getInstance().isSessionCreated()) {
                SessionManager.getInstance().createSession("DeltaSyncRunner execution");
            }
            List<Element> updatedElements = new ArrayList<>(jmsElementsToUpdateLocally.size());
            for (Map.Entry<String, Pair<JSONObject, Element>> elementEntry : jmsElementsToUpdateLocally.entrySet()) {
                Element element = elementEntry.getValue().getSecond();
                JSONObject elementJson = elementEntry.getValue().getFirst();
                // Element both exists and is editable here
                try {
                    ImportUtility.updateElement(element, elementJson);
                    if (!(element.getOwner() != null && elementJson.get("qualifiedId") instanceof String && ((String) elementJson.get("qualifiedId")).contains("/holding_bin/"))) {
                        ImportUtility.setOwner(element, elementJson);
                    }
                    updatedElements.add(element);
                    locallyChangedValidationRule.addViolation(new ValidationRuleViolation(element, "[UPDATED]"));
                } catch (ImportException ie) {
                    ie.printStackTrace();
                    ValidationRuleViolation vrv = new ValidationRuleViolation(element, "[UPDATE FAILED] " + ie.getMessage());
                    cannotUpdate.addViolation(vrv);
                    unmodifiableChangelog.addChange(elementEntry.getKey(), null, Changelog.ChangeType.UPDATED);
                }
            }
            listener.setDisabled(false);
            if (!updatedElements.isEmpty()) {
                shouldLogNoJmsChanges = false;
                Application.getInstance().getGUILog().log("[INFO] Updated " + updatedElements.size() + " element" + (updatedElements.size() != 1 ? "s" : "") + " locally from the MMS.");
            }
        }

        // REMOVE DELETED ELEMENTS FROM MMS

        if (shouldUpdate && !jmsElementsToDeleteLocally.isEmpty()) {
            listener.setDisabled(true);
            if (!SessionManager.getInstance().isSessionCreated()) {
                SessionManager.getInstance().createSession("DeltaSyncRunner execution");
            }
            List<String> deletedElementIDs = new ArrayList<>(jmsElementsToDeleteLocally.size());
            for (Map.Entry<String, Element> elementEntry : jmsElementsToDeleteLocally.entrySet()) {
                Element element = elementEntry.getValue();
                try {
                    ModelElementsManager.getInstance().removeElement(element);
                    deletedElementIDs.add(elementEntry.getKey());
                    locallyChangedValidationRule.addViolation(new ValidationRuleViolation(element, "[DELETED]"));
                } catch (ReadOnlyElementException roee) {
                    roee.printStackTrace();
                    ValidationRuleViolation vrv = new ValidationRuleViolation(element, "[DELETE FAILED] " + roee.getMessage());
                    cannotUpdate.addViolation(vrv);
                    unmodifiableChangelog.addChange(elementEntry.getKey(), null, Changelog.ChangeType.DELETED);
                }
            }
            listener.setDisabled(false);
            if (!deletedElementIDs.isEmpty()) {
                shouldLogNoJmsChanges = false;
                Application.getInstance().getGUILog().log("[INFO] Deleted " + deletedElementIDs.size() + " element" + (deletedElementIDs.size() != 1 ? "s" : "") + " locally from the MMS.");
            }
        }

        // OUTPUT RESULT OF JMS CHANGES

        if (!shouldLogNoJmsChanges) {
            Application.getInstance().getGUILog().log("[INFO] No MMS changes to update locally.");
        }

        // SHOW VALIDATION WINDOW OF UNCONFLICTED CHANGES

        vss.add(changelogSuite);
        if (changelogSuite.hasErrors()) {
            Utils.displayValidationWindow(vss, "Delta Sync Changelog");
        }

        // CLOSE SESSION IF OPENED

        if (SessionManager.getInstance().isSessionCreated()) {
            SessionManager.getInstance().closeSession();
        }

        // HANDLE CONFLICTS

        Set<Element> localConflictedElements = new HashSet<>();
        JSONObject body = new JSONObject();
        JSONArray elementsJsonArray = new JSONArray();
        body.put("elements", elementsJsonArray);

        for (Map.Entry<String, Pair<Changelog.Change<Element>, Changelog.Change<Void>>> conflictedEntry : conflictedChanges.entrySet()) {
            String id = conflictedEntry.getKey();
            Changelog.Change<Element> localChange = conflictedEntry.getValue().getFirst();
            Changelog.Change<JSONObject> jmsChange = conflictedEntry.getValue().getSecond() != null ?
                    new Changelog.Change<>(jmsJsons.get(id), conflictedEntry.getValue().getSecond().getSecond()) : null;

            if (localChange != null) {
                if (localChange.getFirst() != null) {
                    localConflictedElements.add(localChange.getFirst());
                }
            }
            else if (jmsChange != null) {
                if (jmsChange.getFirst() != null) {
                    elementsJsonArray.add(jmsChange.getFirst());
                }
            }
        }

        ModelValidator modelValidator = new ModelValidator(null, body, false, localConflictedElements, false);
        try {
            modelValidator.validate(false, null);
        } catch (ServerException ignored) {

        }
        if (!modelValidator.getDifferentElements().isEmpty()) {
            Application.getInstance().getGUILog().log("[INFO] There are potential conflicts in " + modelValidator.getDifferentElements().size() + " element" + (modelValidator.getDifferentElements().size() != 1 ? "s" : "") + " between MMS and local changes. Please resolve them and re-sync.");
            vss.add(modelValidator.getSuite());
            modelValidator.showWindow();
        }
    }

    public boolean isFailure() {
        return failure;
    }

    public List<ValidationSuite> getValidations() {
        return vss;
    }

}
