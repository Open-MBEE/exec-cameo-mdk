package org.openmbee.mdk.mms.sync.mms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.magicdraw.teamwork2.locks.ILockProjectService;
import org.openmbee.mdk.api.incubating.MDKConstants;
import org.openmbee.mdk.api.incubating.convert.Converters;
import org.openmbee.mdk.emf.EMFImporter;
import org.openmbee.mdk.http.ServerException;
import org.openmbee.mdk.json.ImportException;
import org.openmbee.mdk.json.JacksonUtils;
import org.openmbee.mdk.mms.MMSUtils;
import org.openmbee.mdk.mms.actions.MMSLoginAction;
import org.openmbee.mdk.mms.endpoints.MMSCommitEndpoint;
import org.openmbee.mdk.mms.endpoints.MMSCommitsEndpoint;
import org.openmbee.mdk.mms.endpoints.MMSEndpointBuilderConstants;
import org.openmbee.mdk.mms.sync.delta.SyncElement;
import org.openmbee.mdk.mms.sync.delta.SyncElements;
import org.openmbee.mdk.mms.sync.status.SyncStatusConfigurator;
import org.openmbee.mdk.options.MDKProjectOptions;
import org.apache.http.client.methods.HttpRequestBase;
import org.openmbee.mdk.util.Changelog;
import org.openmbee.mdk.util.MDUtils;
import org.openmbee.mdk.util.TicketUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

public class MMSDeltaProjectEventListenerAdapter extends ProjectEventListenerAdapter {
    private static final Map<Project, MMSDeltaProjectMapping> projectMappings = Collections.synchronizedMap(new WeakHashMap<>());

    @Override
    public void projectOpened(Project project) {
        if (!project.isRemote()) {
            return;
        }
        projectClosed(project);
        // no need to poll mms when it'll just do the whole thing at commit
        /*
        getProjectMapping(project).setScheduledFuture(TaskRunner.scheduleWithProgressStatus(progressStatus -> {
            try {
                getProjectMapping(project).update();
            } catch (URISyntaxException | IOException | ServerException | GeneralSecurityException e) {
                e.printStackTrace();
            }
        }, "MMS Fetch", false, TaskRunner.ThreadExecutionStrategy.POOLED, false, (r, ses) -> ses.scheduleAtFixedRate(r, 0, 60, TimeUnit.MINUTES)));
        */
        if (MDKProjectOptions.getMbeeEnabled(project)) {
            MMSLoginAction.loginAction(project);
        }
    }

    @Override
    public void projectClosed(Project project) {
        getProjectMapping(project).getInMemoryCommits().clear();
        getProjectMapping(project).getInMemoryChangelog().clear();
        SyncStatusConfigurator.getSyncStatusAction().update();

        ScheduledFuture<?> scheduledFuture = getProjectMapping(project).getScheduledFuture();
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
        projectMappings.remove(project);
    }

    @Override
    public void projectReplaced(Project project, Project project1) {
        projectClosed(project);
        projectOpened(project1);
    }

    @Override
    public void projectCreated(Project project) {
        projectOpened(project);
    }

    public static MMSDeltaProjectMapping getProjectMapping(Project project) {
        MMSDeltaProjectMapping projectMapping = projectMappings.get(project);
        if (projectMapping == null) {
            projectMappings.put(project, projectMapping = new MMSDeltaProjectMapping(project));
        }
        return projectMapping;
    }

    @Override
    public void projectSaved(Project project, boolean b) {
        // Need to clear out changes after TWC update so we don't repeat.
        // Example: MD1 and MD2 have a project open. MMS has unsynced changes. MD1 syncs and commits. MD2 still has these changes in memory and re-syncs the elements.
        // Unfortunately there is no projectUpdated hook, but projects are saved
        // MDUMLCS-26866 Add projectUpdated event to ProjectEventListener
        projectClosed(project);
    }

    public static class MMSDeltaProjectMapping {
        private static final Map<String, Changelog.ChangeType> CHANGE_MAPPING = new LinkedHashMap<>(3);

        static {
            CHANGE_MAPPING.put("added", Changelog.ChangeType.CREATED);
            CHANGE_MAPPING.put("deleted", Changelog.ChangeType.DELETED);
            CHANGE_MAPPING.put("updated", Changelog.ChangeType.UPDATED);
        }

        private final Project project;
        private ScheduledFuture<?> scheduledFuture;

        private Set<String> inMemoryCommits = new LinkedHashSet<>();
        private Changelog<String, Void> inMemoryChangelog = new Changelog<>();
        private String lastSyncedCommitId;

        public MMSDeltaProjectMapping(Project project) {
            this.project = project;
        }

        public ScheduledFuture<?> getScheduledFuture() {
            return scheduledFuture;
        }

        public void setScheduledFuture(ScheduledFuture<?> scheduledFuture) {
            this.scheduledFuture = scheduledFuture;
        }

        public Set<String> getInMemoryCommits() {
            return inMemoryCommits;
        }

        public Changelog<String, Void> getInMemoryChangelog() {
            return inMemoryChangelog;
        }

        public synchronized String getLastSyncedCommitId() {
            return lastSyncedCommitId;
        }

        public String getLastSyncedMmsCommit() throws RuntimeException {
            String branchId = MDUtils.getBranchId(project);
            return SyncElements.getAllByType(project, SyncElement.Type.MMS_COMMIT).stream().map(SyncElements::getValue).map(s -> {
                try {
                    return JacksonUtils.getObjectMapper().readTree(s);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }).filter(json -> {
                if (json == null || !json.isObject()) {
                    return false;
                }
                JsonNode branchIdNode = json.get("branchId");
                if (branchIdNode == null || !branchIdNode.isTextual()) {
                    return false;
                }
                return branchId.equals(branchIdNode.asText());
            }).map(json -> json.get("commitId")).filter(commitIdNode -> commitIdNode != null && commitIdNode.isTextual()).map(JsonNode::asText).findAny().orElse(null);
        }

        public synchronized boolean update() throws URISyntaxException, IOException, ServerException, IllegalStateException, GeneralSecurityException {
            if (!project.isRemote()) {
                return false;
            }
            // TODO test if branch exists to avoid 404 on commits GET
            if (!TicketUtils.isTicketSet(project)) {
                inMemoryCommits.clear();
                inMemoryChangelog.clear();
                SyncStatusConfigurator.getSyncStatusAction().update();
                return false;
            }

            // https://support.nomagic.com/browse/MDUMLCS-28121
            try {
                lastSyncedCommitId = getLastSyncedMmsCommit();
            } catch (NullPointerException e) {
                e.printStackTrace();
                return false;
            }
            Deque<String> commitIdDeque = new ArrayDeque<>();

            obtainAndParseCommits(commitIdDeque, project);

            if (commitIdDeque.isEmpty()) {
                return true;
            }

            ILockProjectService lockService;
            Set<String> lockedElementIds = project.isRemote() && (lockService = EsiUtils.getLockService(project)) != null ? lockService.getLockedByMe().stream().map(Converters.getElementToIdConverter()).collect(Collectors.toSet()) : Collections.emptySet();

            while (!commitIdDeque.isEmpty()) {
                String commitId = commitIdDeque.removeFirst();
                HttpRequestBase commitRequest = MMSUtils.prepareEndpointBuilderBasicGet(MMSCommitEndpoint.builder(), project)
                        .addParam(MMSEndpointBuilderConstants.URI_PROJECT_SUFFIX, Converters.getIProjectToIdConverter().apply(project.getPrimaryProject()))
                        .addParam(MMSEndpointBuilderConstants.URI_COMMIT_SUFFIX, commitId).build();
                File responseFile = MMSUtils.sendMMSRequest(project, commitRequest);

                determineChangesUsingCommitResponse(responseFile, lockedElementIds, commitId);
                inMemoryCommits.add(commitId);
            }
            SyncStatusConfigurator.getSyncStatusAction().update();
            return true;
        }

        private void obtainAndParseCommits(Deque<String> commitIdDeque, Project project)
                throws URISyntaxException, IOException, ServerException, GeneralSecurityException {
            // look at commits until it gets to lastSyncedCommitId
            commitIdDeque.clear();
                HashMap<String, String> uriBuilderParams = new HashMap<>();
                //uriBuilderParams.put("limit", Integer.toString(limit));
                HttpRequestBase commitsRequest = MMSUtils.prepareEndpointBuilderBasicGet(MMSCommitsEndpoint.builder(), project)
                        .addParam(MMSEndpointBuilderConstants.URI_PROJECT_SUFFIX, Converters.getIProjectToIdConverter().apply(project.getPrimaryProject()))
                        .addParam(MMSEndpointBuilderConstants.URI_REF_SUFFIX, MDUtils.getBranchId(project))
                        .addParam(MMSEndpointBuilderConstants.URI_BUILDER_PARAMETERS, uriBuilderParams).build();
                File responseFile = MMSUtils.sendMMSRequest(project, commitsRequest);

                Map<String, List<ObjectNode>> parsedResponseObjects = JacksonUtils.parseResponseIntoObjects(responseFile, MDKConstants.COMMITS_NODE);
                List<ObjectNode> elementObjects = parsedResponseObjects.get(MDKConstants.COMMITS_NODE);
                if(elementObjects != null && !elementObjects.isEmpty()) {
                    for(ObjectNode jsonObject : elementObjects) {
                        JsonNode idValue = jsonObject.get(MDKConstants.ID_KEY);
                        if(idValue != null && idValue.isTextual()) {
                            String id = idValue.asText();
                            if (lastSyncedCommitId == null) {
                                lastSyncedCommitId = id;
                            }
                            if (lastSyncedCommitId.equals(id) || inMemoryCommits.contains(id)) {
                                return;
                            }
                            commitIdDeque.addFirst(id);
                        }
                    }
                }
        }

        private void determineChangesUsingCommitResponse(File responseFile, Set<String> lockedElementIds, String commitId) throws IOException {
            // turns out the response still uses commits as the field of interest in terms of parsing
            Map<String, List<ObjectNode>> parsedResponseObjects = JacksonUtils.parseResponseIntoObjects(responseFile, MDKConstants.COMMITS_NODE);
            List<ObjectNode> commitObjects = parsedResponseObjects.get(MDKConstants.COMMITS_NODE);

            if(commitObjects != null && !commitObjects.isEmpty()) {
                Map<String, Integer> commitSizes = new HashMap<>();
                for(ObjectNode jsonObject : commitObjects) {
                    if(jsonObject.isObject() && jsonObject.size() > 0) {
                        validateJsonElementArray(jsonObject, lockedElementIds, commitSizes);
                    }
                    if (MDUtils.isDeveloperMode()) {
                        if (commitSizes.isEmpty()) {
                            Application.getInstance().getGUILog().log("[INFO] No objects found in commit.");
                            return;
                        }else {
                            for (Map.Entry<String, Integer> size : commitSizes.entrySet()) {
                                Application.getInstance().getGUILog().log("[INFO] " + project.getName() + " - " + size.getKey() + " " + NumberFormat.getInstance().format(size.getValue()) + " MMS element change" + (size.getValue() != 1 ? "s" : "") + " for commit " + commitId + ".");
                            }
                        }
                    }
                }
            }
        }

        private void validateJsonElementArray(JsonNode objectNode, Set<String> lockedElementIds, Map<String, Integer> sizes) {
            JsonNode sourceField = objectNode.get(MDKConstants.SOURCE_FIELD);
            String commitSyncDirection = "";
            int size = 0;
            boolean isSyncingCommit = sourceField != null && sourceField.isTextual() && MDKConstants.MAGICDRAW_SOURCE_VALUE.equalsIgnoreCase(sourceField.asText());
            if(isSyncingCommit) {
                commitSyncDirection = "Removed";
            } else {
                commitSyncDirection = "Added";
            }
            if (sizes.containsKey(commitSyncDirection)) {
                size = sizes.get(commitSyncDirection);
            }
            for (Map.Entry<String, Changelog.ChangeType> entry : CHANGE_MAPPING.entrySet()) {
                JsonNode changesJsonArray = objectNode.get(entry.getKey());
                if (changesJsonArray == null || !changesJsonArray.isArray()) {
                    throw new IllegalStateException();
                }

                for (JsonNode changeJsonObject : changesJsonArray) {
                    if (!changeJsonObject.isObject()) {
                        throw new IllegalStateException();
                    }
                    JsonNode typeJsonNode = changeJsonObject.get(MDKConstants.TYPE_KEY);
                    if (typeJsonNode != null && typeJsonNode.isTextual() && !MDKConstants.ELEMENT_TYPE_VALUE.equalsIgnoreCase(typeJsonNode.asText())) {
                        continue;
                    }
                    JsonNode idJsonNode = changeJsonObject.get(MDKConstants.ID_KEY);
                    if (!idJsonNode.isTextual() || idJsonNode.asText().isEmpty()) {
                        continue;
                    }
                    String elementId = idJsonNode.asText();
                    try {
                        ObjectNode elementJsonNode = JacksonUtils.getObjectMapper().createObjectNode();
                        elementJsonNode.put(MDKConstants.ID_KEY, elementId);
                        if (EMFImporter.PreProcessor.SYSML_ID_VALIDATION.getFunction().apply(elementJsonNode, project, false, project.getPrimaryModel()) == null) {
                            continue;
                        }
                    } catch (ImportException ignored) {
                        continue;
                    }
                    if (isSyncingCommit) {
                        if (lockedElementIds.contains(elementId)) {
                            continue;
                        }
                        for (Changelog.ChangeType changeType : Changelog.ChangeType.values()) {
                            inMemoryChangelog.get(changeType).remove(elementId);
                        }
                    }
                    else {
                        inMemoryChangelog.addChange(elementId, null, entry.getValue());
                    }
                    size++;
                }
            }
            sizes.put(commitSyncDirection, size);
        }
    }
}
