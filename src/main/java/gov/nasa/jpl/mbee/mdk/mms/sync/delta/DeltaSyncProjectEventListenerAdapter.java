package gov.nasa.jpl.mbee.mdk.mms.sync.delta;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.mms.sync.local.LocalDeltaProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.mdk.mms.sync.local.LocalDeltaTransactionCommitListener;
import gov.nasa.jpl.mbee.mdk.options.MDKEnvironmentOptionsGroup;
import gov.nasa.jpl.mbee.mdk.options.MDKProjectOptions;
import gov.nasa.jpl.mbee.mdk.util.Changelog;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class DeltaSyncProjectEventListenerAdapter extends ProjectEventListenerAdapter {
    private static final Map<SyncElement.Type, Function<Project, Changelog<String, ?>>> CHANGELOG_FUNCTIONS = new HashMap<>(2);

    static {
        CHANGELOG_FUNCTIONS.put(SyncElement.Type.LOCAL, project -> {
            Changelog<String, Void> combinedPersistedChangelog = new Changelog<>();
            for (SyncElement syncElement : SyncElements.getAllByType(project, SyncElement.Type.LOCAL)) {
                combinedPersistedChangelog = combinedPersistedChangelog.and(SyncElements.buildChangelog(syncElement));
            }
            LocalDeltaTransactionCommitListener localDeltaTransactionCommitListener = LocalDeltaProjectEventListenerAdapter.getProjectMapping(project).getLocalDeltaTransactionCommitListener();
            if (localDeltaTransactionCommitListener == null) {
                return combinedPersistedChangelog;
            }
            return combinedPersistedChangelog.and(localDeltaTransactionCommitListener.getInMemoryLocalChangelog(), (key, element) -> null);
        });
        /*
        CHANGELOG_FUNCTIONS.put(SyncElement.Type.MMS, project -> {
            Changelog<String, Void> combinedPersistedChangelog = new Changelog<>();
            for (SyncElement syncElement : SyncElements.getAllByType(project, SyncElement.Type.MMS)) {
                combinedPersistedChangelog = combinedPersistedChangelog.and(SyncElements.buildChangelog(syncElement));
            }
            return combinedPersistedChangelog.and(MMSDeltaProjectEventListenerAdapter.getProjectMapping(project).getInMemoryChangelog());
        });
        */
    }

    @Override
    public void projectPreSaved(Project project, boolean savedInServer) {
        boolean save = MDKEnvironmentOptionsGroup.getInstance().isPersistChangelog();
        if (!save) {
            return;
        }
        if (!MDKProjectOptions.getMbeeEnabled(project)) {
            return;
        }
        persistChanges(project);
    }

    private static void persistChanges(Project project) {

        LocalDeltaTransactionCommitListener listener = LocalDeltaProjectEventListenerAdapter.getProjectMapping(project).getLocalDeltaTransactionCommitListener();
        if (listener != null) {
            listener.setDisabled(true);
        }

        if (!SessionManager.getInstance().isSessionCreated(project)) {
            SessionManager.getInstance().createSession(project, "Delta Sync Changelog Persistence #2");
        }
        for (Map.Entry<SyncElement.Type, Function<Project, Changelog<String, ?>>> entry : CHANGELOG_FUNCTIONS.entrySet()) {
            Changelog<String, ?> changelog = entry.getValue().apply(project);
            try {
                SyncElements.setByType(project, entry.getKey(), JacksonUtils.getObjectMapper().writeValueAsString(SyncElements.buildJson(changelog)));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        if (SessionManager.getInstance().isSessionCreated(project)) {
            SessionManager.getInstance().closeSession(project);
        }

        if (listener != null) {
            listener.setDisabled(false);
        }
    }
}
