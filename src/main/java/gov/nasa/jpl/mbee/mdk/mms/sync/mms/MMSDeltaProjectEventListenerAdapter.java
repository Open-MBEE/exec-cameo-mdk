package gov.nasa.jpl.mbee.mdk.mms.sync.mms;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.magicdraw.teamwork2.locks.ILockProjectService;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.emf.EMFImporter;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.json.ImportException;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.mms.actions.MMSLoginAction;
import gov.nasa.jpl.mbee.mdk.mms.sync.delta.SyncElement;
import gov.nasa.jpl.mbee.mdk.mms.sync.delta.SyncElements;
import gov.nasa.jpl.mbee.mdk.mms.sync.status.SyncStatusConfigurator;
import gov.nasa.jpl.mbee.mdk.util.Changelog;
import gov.nasa.jpl.mbee.mdk.util.MDUtils;
import gov.nasa.jpl.mbee.mdk.util.TaskRunner;
import gov.nasa.jpl.mbee.mdk.util.TicketUtils;
import org.apache.http.client.utils.URIBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class MMSDeltaProjectEventListenerAdapter extends ProjectEventListenerAdapter {
    private static final Map<Project, MMSDeltaProjectMapping> projectMappings = Collections.synchronizedMap(new WeakHashMap<>());

    @Override
    public void projectOpened(Project project) {
        if (!project.isRemote()) {
            return;
        }
        projectClosed(project);
        getProjectMapping(project).setScheduledFuture(TaskRunner.scheduleWithProgressStatus(progressStatus -> {
            try {
                getProjectMapping(project).update();
            } catch (URISyntaxException | IOException | ServerException e) {
                e.printStackTrace();
            }
        }, "MMS Fetch", false, TaskRunner.ThreadExecutionStrategy.POOLED, false, (r, ses) -> ses.scheduleAtFixedRate(r, 0, 1, TimeUnit.MINUTES)));
        if (StereotypesHelper.hasStereotype(project.getPrimaryModel(), "ModelManagementSystem")) {
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

        public synchronized boolean update() throws URISyntaxException, IOException, ServerException, IllegalStateException {
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
            int exponent = 0;

            doWhile:
            do {
                commitIdDeque.clear();
                int limit = (int) Math.pow(10, exponent++);

                URIBuilder commitsUriBuilder = MMSUtils.getServiceProjectsRefsUri(project);
                commitsUriBuilder.setPath(commitsUriBuilder.getPath() + "/" + MDUtils.getBranchId(project) + "/commits");
                commitsUriBuilder.setParameter("limit", Integer.toString(limit));
                File responseFile = MMSUtils.sendMMSRequest(project, MMSUtils.buildRequest(MMSUtils.HttpRequestType.GET, commitsUriBuilder));

                JsonToken current;
                try (JsonParser jsonParser = JacksonUtils.getJsonFactory().createParser(responseFile)) {
                    current = jsonParser.nextToken();
                    if (current != JsonToken.START_OBJECT) {
                        throw new IllegalStateException();
                    }
                    current = jsonParser.nextToken();
                    if (current != JsonToken.FIELD_NAME || !jsonParser.getCurrentName().equals("commits")) {
                        throw new IllegalStateException();
                    }
                    current = jsonParser.nextToken();
                    if (current != JsonToken.START_ARRAY) {
                        throw new IllegalStateException();
                    }
                    JsonNode value;
                    int size = 0;
                    while (jsonParser.nextToken() == JsonToken.START_OBJECT) {
                        String id;
                        ObjectNode objectNode = JacksonUtils.parseJsonObject(jsonParser);
                        if ((value = objectNode.get(MDKConstants.ID_KEY)) != null && value.isTextual()) {
                            id = value.asText();
                            if (lastSyncedCommitId == null) {
                                lastSyncedCommitId = id;
                            }
                            if (lastSyncedCommitId.equals(id)) {
                                break doWhile;
                            }
                            if (inMemoryCommits.contains(id)) {
                                break doWhile;
                            }
                            commitIdDeque.addFirst(id);
                        }
                        size++;
                    }
                    if (size < limit) {
                        break;
                    }
                }
            } while (true);

            if (commitIdDeque.isEmpty()) {
                return true;
            }

            ILockProjectService lockService;
            Set<String> lockedElementIds = project.isRemote() && (lockService = EsiUtils.getLockService(project)) != null ? lockService.getLockedByMe().stream().map(Converters.getElementToIdConverter()).collect(Collectors.toSet()) : Collections.emptySet();

            while (!commitIdDeque.isEmpty()) {
                String commitId = commitIdDeque.removeFirst();
                URIBuilder uriBuilder = MMSUtils.getServiceProjectsUri(project);
                uriBuilder.setPath(uriBuilder.getPath() + "/" + Converters.getIProjectToIdConverter().apply(project.getPrimaryProject()) + "/commits/" + commitId);
                File responseFile = MMSUtils.sendMMSRequest(project, MMSUtils.buildRequest(MMSUtils.HttpRequestType.GET, uriBuilder));

                try (JsonParser jsonParser = JacksonUtils.getJsonFactory().createParser(responseFile)) {
                    ObjectNode objectNode = JacksonUtils.parseJsonObject(jsonParser);
                    if (objectNode == null) {
                        throw new IllegalStateException();
                    }
                    JsonNode jsonNode;
                    if ((jsonNode = objectNode.get("commits")) == null || !jsonNode.isArray() || jsonNode.size() == 0) {
                        throw new IllegalStateException();
                    }
                    jsonNode = jsonNode.get(0);
                    JsonNode sourceJsonNode = jsonNode.get("source");
                    boolean isSyncingCommit = sourceJsonNode != null && sourceJsonNode.isTextual() && "magicdraw".equalsIgnoreCase(sourceJsonNode.asText());
                    int size = 0;
                    for (Map.Entry<String, Changelog.ChangeType> entry : CHANGE_MAPPING.entrySet()) {
                        JsonNode changesJsonArray = jsonNode.get(entry.getKey());
                        if (changesJsonArray == null || !changesJsonArray.isArray()) {
                            throw new IllegalStateException();
                        }
                        for (JsonNode changeJsonObject : changesJsonArray) {
                            if (!changeJsonObject.isObject()) {
                                throw new IllegalStateException();
                            }
                            JsonNode typeJsonNode = changeJsonObject.get(MDKConstants.TYPE_KEY);
                            if (typeJsonNode != null && typeJsonNode.isTextual() && !"element".equalsIgnoreCase(typeJsonNode.asText())) {
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
                    if (MDUtils.isDeveloperMode()) {
                        Application.getInstance().getGUILog().log("[INFO] " + project.getName() + " - " + (isSyncingCommit ? "Removed" : "Added") + " " + NumberFormat.getInstance().format(size) + " MMS element change" + (size != 1 ? "s" : "") + " for commit " + commitId + ".");
                    }
                }
                inMemoryCommits.add(commitId);
            }
            SyncStatusConfigurator.getSyncStatusAction().update();
            return true;
        }
    }
}
