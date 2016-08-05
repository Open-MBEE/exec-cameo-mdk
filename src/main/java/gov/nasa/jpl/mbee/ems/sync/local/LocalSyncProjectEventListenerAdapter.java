package gov.nasa.jpl.mbee.ems.sync.local;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;
import com.nomagic.magicdraw.uml.transaction.MDTransactionManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by igomes on 6/28/16.
 */
public class LocalSyncProjectEventListenerAdapter extends ProjectEventListenerAdapter {
    private static final Map<String, CommonSyncProjectMapping> projectMappings = new ConcurrentHashMap<>();

    @Override
    public void projectOpened(Project project) {
        projectClosed(project);
        LocalSyncTransactionCommitListener listener = new LocalSyncTransactionCommitListener();
        ((MDTransactionManager) project.getRepository().getTransactionManager()).addTransactionCommitListenerIncludingUndoAndRedo(listener);
        getProjectMapping(project).setLocalSyncTransactionCommitListener(listener);
    }

    @Override
    public void projectClosed(Project project) {
        CommonSyncProjectMapping commonSyncProjectMapping = getProjectMapping(project);
        if (commonSyncProjectMapping.getLocalSyncTransactionCommitListener() != null) {
            project.getRepository().getTransactionManager().removeTransactionCommitListener(commonSyncProjectMapping.getLocalSyncTransactionCommitListener());
        }
        projectMappings.remove(project.getID());
    }

    @Override
    public void projectSaved(Project project, boolean savedInServer) {
        CommonSyncProjectMapping commonSyncProjectMapping = LocalSyncProjectEventListenerAdapter.getProjectMapping(project);

        LocalSyncTransactionCommitListener localSyncTransactionCommitListener = commonSyncProjectMapping.getLocalSyncTransactionCommitListener();
        if (localSyncTransactionCommitListener != null) {
            localSyncTransactionCommitListener.getInMemoryLocalChangelog().clear();
        }
    }

    public static CommonSyncProjectMapping getProjectMapping(Project project) {
        CommonSyncProjectMapping commonSyncProjectMapping = projectMappings.get(project.getID());
        if (commonSyncProjectMapping == null) {
            projectMappings.put(project.getID(), commonSyncProjectMapping = new CommonSyncProjectMapping());
        }
        return commonSyncProjectMapping;
    }

    public static class CommonSyncProjectMapping {
        private LocalSyncTransactionCommitListener localSyncTransactionCommitListener;

        public LocalSyncTransactionCommitListener getLocalSyncTransactionCommitListener() {
            return localSyncTransactionCommitListener;
        }

        public void setLocalSyncTransactionCommitListener(LocalSyncTransactionCommitListener localSyncTransactionCommitListener) {
            this.localSyncTransactionCommitListener = localSyncTransactionCommitListener;
        }
    }
}
