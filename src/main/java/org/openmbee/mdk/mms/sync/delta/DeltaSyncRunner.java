package org.openmbee.mdk.mms.sync.delta;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterables;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import org.openmbee.mdk.api.incubating.MDKConstants;
import org.openmbee.mdk.api.incubating.convert.Converters;
import org.openmbee.mdk.http.ServerException;
import org.openmbee.mdk.json.JacksonUtils;
import org.openmbee.mdk.mms.MMSUtils;
import org.openmbee.mdk.mms.actions.MMSLoginAction;
import org.openmbee.mdk.mms.actions.UpdateClientElementAction;
import org.openmbee.mdk.mms.endpoints.MMSElementsEndpoint;
import org.openmbee.mdk.mms.endpoints.MMSEndpointBuilderConstants;
import org.openmbee.mdk.mms.sync.local.LocalDeltaProjectEventListenerAdapter;
import org.openmbee.mdk.mms.sync.local.LocalDeltaTransactionCommitListener;
import org.openmbee.mdk.mms.sync.mms.MMSDeltaProjectEventListenerAdapter;
import org.openmbee.mdk.mms.validation.BranchValidator;
import org.openmbee.mdk.mms.validation.ElementValidator;
import org.openmbee.mdk.mms.validation.ProjectValidator;
import org.openmbee.mdk.util.*;
import org.openmbee.mdk.validation.ValidationSuite;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeltaSyncRunner implements RunnableWithProgress {
    private final boolean shouldCommitDeletes, shouldCommit, shouldUpdate;

    private final Project project = Application.getInstance().getProject();

    private boolean failure = true;

    private Changelog<String, Element> failedLocalChangelog = new Changelog<>();
    private Changelog<String, Void> failedMmsChangelog = new Changelog<>(), successfulMmsChangelog = new Changelog<>();

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
            if (!TicketUtils.isTicketValid(project, progressStatus) && !MMSLoginAction.loginAction(project)) {
                Utils.guilog("[WARNING] You are not logged in to MMS. Skipping sync. All changes will be persisted in the model and re-attempted in the next sync.");
                return;
            }
        } catch (IOException | URISyntaxException | ServerException | GeneralSecurityException e) {
            Utils.guilog("[ERROR] An error occurred while validating credentials. Credentials will be cleared. Skipping sync. All changes will be persisted in the model and re-attempted in the next sync. Reason: " + e.getMessage());
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

        LocalDeltaTransactionCommitListener listener = LocalDeltaProjectEventListenerAdapter.getProjectMapping(project).getLocalDeltaTransactionCommitListener();

        // UPDATE LOCKS

        listener.setDisabled(true);

        // UPDATE MMS CHANGELOG

        try {
            if (!MMSDeltaProjectEventListenerAdapter.getProjectMapping(project).update()) {
                Application.getInstance().getGUILog().log("[WARNING] MMS history is unavailable. Skipping sync. All changes will be re-attempted in the next sync.");
                return;
            }
        } catch (URISyntaxException | IOException | IllegalStateException | GeneralSecurityException | ServerException e) {
            Application.getInstance().getGUILog().log("[ERROR] An error occurred while updating MMS history. Credentials will be cleared. Skipping sync. All changes will be persisted in the model and re-attempted in the next sync. Reason: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // BUILD COMPLETE LOCAL CHANGELOG

        Changelog<String, Element> persistedLocalChangelog = new Changelog<>();
        Collection<SyncElement> persistedLocalSyncElements = SyncElements.getAllByType(project, SyncElement.Type.LOCAL);
        for (SyncElement syncElement : persistedLocalSyncElements) {
            persistedLocalChangelog = persistedLocalChangelog.and(SyncElements.buildChangelog(syncElement), (key, value) -> Converters.getIdToElementConverter().apply(key, project));
        }
        Changelog<String, Element> localChangelog = persistedLocalChangelog.and(listener.getInMemoryLocalChangelog());

        // HANDLE CASE WHERE VALUE SPECIFICATION IS DIRTIED EXTERNALLY
        // Workaround: Dirty all referencing elements as ValueSpecifications aren't given their own identity

        localChangelog.values().stream().flatMap(map -> map.values().stream()).filter(element -> element instanceof ValueSpecification).flatMap(element -> element.eClass().getEAllReferences().stream().flatMap(reference -> {
            List<Element> elements = new ArrayList<>();
            Object value = element.eGet(reference);
            if (value == null) {
                return Stream.empty();
            }
            if (reference.isMany() && value instanceof Collection) {
                ((Collection<Object>) value).stream().filter(o -> o instanceof Element).map(o -> (Element) o).forEach(elements::add);
            }
            else if (value instanceof Element) {
                elements.add((Element) value);
            }
            return elements.stream();
        })).collect(Collectors.toSet()).forEach(element -> localChangelog.addChange(Converters.getElementToIdConverter().apply(element), element, Changelog.ChangeType.UPDATED));


        Map<String, Element> localCreated = localChangelog.get(Changelog.ChangeType.CREATED),
                localUpdated = localChangelog.get(Changelog.ChangeType.UPDATED),
                localDeleted = localChangelog.get(Changelog.ChangeType.DELETED);

        // BUILD COMPLETE MMS CHANGELOG

        Changelog<String, Void> persistedMmsChangelog = new Changelog<>();
        Collection<SyncElement> persistedMmsSyncElements = SyncElements.getAllByType(project, SyncElement.Type.MMS);
        for (SyncElement syncElement : persistedMmsSyncElements) {
            persistedMmsChangelog = persistedMmsChangelog.and(SyncElements.buildChangelog(syncElement));
        }
        Changelog<String, Void> mmsChangelog = persistedMmsChangelog.and(MMSDeltaProjectEventListenerAdapter.getProjectMapping(project).getInMemoryChangelog(), (key, objectNode) -> null);

        Map<String, Void> mmsCreated = mmsChangelog.get(Changelog.ChangeType.CREATED),
                mmsUpdated = mmsChangelog.get(Changelog.ChangeType.UPDATED),
                mmsDeleted = mmsChangelog.get(Changelog.ChangeType.DELETED);

        Set<String> elementIdsToGet = new HashSet<>(mmsUpdated.keySet());
        elementIdsToGet.addAll(mmsCreated.keySet());
        if (shouldUpdate && !mmsChangelog.isEmpty()) {
            int size = mmsChangelog.flattenedSize();
            Application.getInstance().getGUILog().log("[INFO] Getting " + size + " changed element" + (size != 1 ? "s" : "") + " from the MMS.");
        }

        Map<String, ObjectNode> mmsJsons = new HashMap<>(elementIdsToGet.size());

        // Get latest json for element added/changed from MMS

        if (!elementIdsToGet.isEmpty()) {
            progressStatus.setDescription("Getting " + elementIdsToGet.size() + " added/changed element" + (elementIdsToGet.size() != 1 ? "s" : "") + " from MMS");
            File responseFile;
            ObjectNode response;
            try {
                responseFile = MMSUtils.getElementsRecursively(project, elementIdsToGet, progressStatus);
                try (JsonParser jsonParser = JacksonUtils.getJsonFactory().createParser(responseFile)) {
                    response = JacksonUtils.parseJsonObject(jsonParser);
                }
            } catch (IOException | URISyntaxException | ServerException | GeneralSecurityException e) {
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
                mmsJsons.put(webId, (ObjectNode) jsonNode);
            }
        }

        // NEW CONFLICT DETECTION

        progressStatus.setDescription("Detecting conflicts");
        Map<String, Pair<Changelog.Change<Element>, Changelog.Change<Void>>> conflictedChanges = new LinkedHashMap<>(),
                unconflictedChanges = new LinkedHashMap<>();
        localChangelog.findConflicts(mmsChangelog, (change, change2) -> change != null && change2 != null, conflictedChanges, unconflictedChanges);

        // MAP CHANGES TO ACTIONABLE GROUPS

        Map<String, Element> localElementsToPost = new LinkedHashMap<>(localCreated.size() + localUpdated.size());
        Set<String> deleteElements = new HashSet<>(localDeleted.size());

        Map<String, ObjectNode> mmsElementsToCreateLocally = new LinkedHashMap<>(mmsCreated.size());
        Map<String, Pair<ObjectNode, Element>> mmsElementsToUpdateLocally = new LinkedHashMap<>(mmsUpdated.size());
        Map<String, Element> mmsElementsToDeleteLocally = new LinkedHashMap<>(mmsDeleted.size());

        // only one side of the pair will have a value when unconflicted
        for (Map.Entry<String, Pair<Changelog.Change<Element>, Changelog.Change<Void>>> unconflictedEntry : unconflictedChanges.entrySet()) {
            String id = unconflictedEntry.getKey();
            Changelog.Change<Element> localChange = unconflictedEntry.getValue().getKey();
            Changelog.Change<ObjectNode> mmsChange = unconflictedEntry.getValue().getValue() != null ?
                    new Changelog.Change<>(mmsJsons.get(id), unconflictedEntry.getValue().getValue().getType()) : null;

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
            else if (shouldUpdate && mmsChange != null) {
                ObjectNode objectNode = mmsChange.getChanged();
                Element element = Converters.getIdToElementConverter().apply(id, project);
                switch (mmsChange.getType()) {
                    case CREATED:
                        if (objectNode == null) {
                            Application.getInstance().getGUILog().log("[INFO] Attempted to create element " + id + " locally, but it no longer exists on the MMS. Skipping.");
                            continue;
                        }
                        if (element != null) {
                            Application.getInstance().getGUILog().log("[INFO] Attempted to create element " + id + " locally, but it already exists. Skipping.");
                            continue;
                        }
                        mmsElementsToCreateLocally.put(id, objectNode);
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
                        mmsElementsToUpdateLocally.put(id, new Pair<>(objectNode, element));
                        break;
                    case DELETED:
                        if (element == null) {
                            Application.getInstance().getGUILog().log("[INFO] Attempted to delete element " + id + " locally, but it doesn't exist. Skipping.");
                            continue;
                        }
                        mmsElementsToDeleteLocally.put(id, element);
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
        String projectId = Converters.getIProjectToIdConverter().apply(project.getPrimaryProject());
        String refId = MDUtils.getBranchId(project);
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
                try {
                    File file = MMSUtils.createEntityFile(this.getClass(), ContentType.APPLICATION_JSON, postElements, MMSUtils.JsonBlobType.ELEMENT_JSON);
                    // HashMap<String, String> uriBuilderParams = new HashMap<>();
                    // uriBuilderParams.put("overwrite", "true"); // Removing so Overwrite doesnt happen on DeltaSync
                    HttpRequestBase elementsUpdateCreateRequest = MMSUtils.prepareEndpointBuilderBasicJsonPostRequest(MMSElementsEndpoint.builder(), project, file)
                            .addParam(MMSEndpointBuilderConstants.URI_PROJECT_SUFFIX, projectId)
                            .addParam(MMSEndpointBuilderConstants.URI_REF_SUFFIX, refId)
                            // .addParam(MMSEndpointBuilderConstants.URI_BUILDER_PARAMETERS, uriBuilderParams) // Removing so Overwrite doesnt happen on DeltaSync
                            .build();
                    TaskRunner.runWithProgressStatus(progressStatus1 -> {
                        try {
                            MMSUtils.sendMMSRequest(project, elementsUpdateCreateRequest, progressStatus1);
                        } catch (IOException | ServerException | URISyntaxException | GeneralSecurityException e) {
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
            try {
                File file = MMSUtils.createEntityFile(this.getClass(), ContentType.APPLICATION_JSON, deleteElements, MMSUtils.JsonBlobType.ELEMENT_ID);
                HttpRequestBase elementsDeleteRequest = MMSUtils.prepareEndpointBuilderBasicJsonDeleteRequest(MMSElementsEndpoint.builder(), project, file)
                        .addParam(MMSEndpointBuilderConstants.URI_PROJECT_SUFFIX, projectId)
                        .addParam(MMSEndpointBuilderConstants.URI_REF_SUFFIX, refId).build();
                TaskRunner.runWithProgressStatus(progressStatus1 -> {
                    try {
                        MMSUtils.sendMMSRequest(project, elementsDeleteRequest, progressStatus1);
                    } catch (IOException | ServerException | URISyntaxException | GeneralSecurityException e) {
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
            List<ObjectNode> mmsElementsToCreateOrUpdateLocally = new ArrayList<>(mmsElementsToCreateLocally.size() + mmsElementsToUpdateLocally.size());
            mmsElementsToCreateOrUpdateLocally.addAll(mmsElementsToCreateLocally.values());
            mmsElementsToUpdateLocally.values().forEach(pair -> mmsElementsToCreateOrUpdateLocally.add(pair.getKey()));

            UpdateClientElementAction updateClientElementAction = new UpdateClientElementAction(project);
            updateClientElementAction.setElementsToUpdate(mmsElementsToCreateOrUpdateLocally);
            updateClientElementAction.setElementsToDelete(mmsElementsToDeleteLocally.values().stream().map(Converters.getElementToIdConverter()).filter(Objects::nonNull).filter(id -> !id.isEmpty()).collect(Collectors.toList()));
            updateClientElementAction.run(progressStatus);

            failedMmsChangelog = failedMmsChangelog.and(updateClientElementAction.getFailedChangelog(), (id, objectNode) -> null);
            successfulMmsChangelog = updateClientElementAction.getSuccessfulChangelog();
            listener.setDisabled(false);
        }

        // HANDLE CONFLICTS

        progressStatus.setDescription("Finishing up");

        Set<Element> localConflictedElements = new HashSet<>();
        Set<ObjectNode> mmsConflictedElements = new HashSet<>();

        for (Map.Entry<String, Pair<Changelog.Change<Element>, Changelog.Change<Void>>> conflictedEntry : conflictedChanges.entrySet()) {
            String id = conflictedEntry.getKey();
            Changelog.Change<Element> localChange = conflictedEntry.getValue().getKey();
            Changelog.Change<ObjectNode> mmsChange = conflictedEntry.getValue().getValue() != null ?
                    new Changelog.Change<>(mmsJsons.get(id), conflictedEntry.getValue().getValue().getType()) : null;

            if (localChange != null && localChange.getChanged() != null && !project.isDisposed(localChange.getChanged())) {
                localConflictedElements.add(localChange.getChanged());
            }
            if (mmsChange != null && mmsChange.getChanged() != null) {
                mmsConflictedElements.add(mmsChange.getChanged());
            }
        }

        ElementValidator elementValidator = new ElementValidator("CSync Conflict Validation", ElementValidator.buildElementPairs(localConflictedElements, project), mmsConflictedElements, project);
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
                Changelog.Change<ObjectNode> mmsChange = conflictedEntry.getValue().getValue() != null ?
                        new Changelog.Change<>(mmsJsons.get(id), conflictedEntry.getValue().getValue().getType()) : null;

                if (localChange != null && (localChange.getChanged() != null || Changelog.ChangeType.DELETED.equals(localChange.getType()))) {
                    failedLocalChangelog.addChange(conflictedEntry.getKey(), localChange.getChanged(), localChange.getType());
                }
                if (mmsChange != null && (mmsChange.getChanged() != null || Changelog.ChangeType.DELETED.equals(mmsChange.getType()))) {
                    failedMmsChangelog.addChange(conflictedEntry.getKey(), null, mmsChange.getType());
                }
            }
        }

        // CLEAR IN-MEMORY AND PERSIST UNPROCESSED & FAILURES

        listener.setDisabled(true);
        Project project = Application.getInstance().getProject();
        if (!SessionManager.getInstance().isSessionCreated(project)) {
            SessionManager.getInstance().createSession(project, "Delta Sync Changelog Persistence");
        }

        MMSDeltaProjectEventListenerAdapter.MMSDeltaProjectMapping mmsDeltaProjectMapping = MMSDeltaProjectEventListenerAdapter.getProjectMapping(project);
        String commitId = !mmsDeltaProjectMapping.getInMemoryCommits().isEmpty() ? Iterables.getLast(mmsDeltaProjectMapping.getInMemoryCommits()) : mmsDeltaProjectMapping.getLastSyncedCommitId();
        if (commitId != null) {
            ObjectNode mmsCommitObjectNode = JacksonUtils.getObjectMapper().createObjectNode();
            mmsCommitObjectNode.put("branchId", MDUtils.getBranchId(project));
            mmsCommitObjectNode.put("commitId", commitId);

            try {
                SyncElements.setByType(project, SyncElement.Type.MMS_COMMIT, JacksonUtils.getObjectMapper().writeValueAsString(mmsCommitObjectNode));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        listener.getInMemoryLocalChangelog().clear();
        MMSDeltaProjectEventListenerAdapter.getProjectMapping(project).getInMemoryChangelog().clear();
        MMSDeltaProjectEventListenerAdapter.getProjectMapping(project).getInMemoryCommits().clear();

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

        Changelog<String, Void> unprocessedMmsChangelog = new Changelog<>();
        if (!shouldUpdate) {
            unprocessedMmsChangelog = unprocessedMmsChangelog.and(mmsChangelog);
        }
        unprocessedMmsChangelog = unprocessedMmsChangelog.and(failedMmsChangelog);
        try {
            SyncElements.setByType(project, SyncElement.Type.MMS, JacksonUtils.getObjectMapper().writeValueAsString(SyncElements.buildJson(unprocessedMmsChangelog)));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        SessionManager.getInstance().closeSession(project);
        listener.setDisabled(false);

        // SUCCESS
        failure = false;
    }

    public Changelog<String, Void> getSuccessfulMmsChangelog() {
        return successfulMmsChangelog;
    }

    public boolean isFailure() {
        return failure;
    }

    public List<ValidationSuite> getValidations() {
        return vss;
    }

}
