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
    private static final Map<String, LocalSyncProjectMapping> projectMappings = new ConcurrentHashMap<>();

    @Override
    public void projectOpened(Project project) {
        projectClosed(project);
        LocalSyncTransactionCommitListener listener = new LocalSyncTransactionCommitListener(project);
        ((MDTransactionManager) project.getRepository().getTransactionManager()).addTransactionCommitListenerIncludingUndoAndRedo(listener);
        getProjectMapping(project).setLocalSyncTransactionCommitListener(listener);
    }

    @Override
    public void projectClosed(Project project) {
        LocalSyncProjectMapping localSyncProjectMapping = getProjectMapping(project);
        if (localSyncProjectMapping.getLocalSyncTransactionCommitListener() != null) {
            project.getRepository().getTransactionManager().removeTransactionCommitListener(localSyncProjectMapping.getLocalSyncTransactionCommitListener());
        }
        projectMappings.remove(project.getID());
    }

    @Override
    public void projectReplaced(Project oldProject, Project newProject) {
        projectClosed(oldProject);
        projectOpened(newProject);
    }

    @Override
    public void projectSaved(Project project, boolean savedInServer) {
        LocalSyncProjectMapping localSyncProjectMapping = LocalSyncProjectEventListenerAdapter.getProjectMapping(project);

        LocalSyncTransactionCommitListener localSyncTransactionCommitListener = localSyncProjectMapping.getLocalSyncTransactionCommitListener();
        if (localSyncTransactionCommitListener != null) {
            localSyncTransactionCommitListener.getInMemoryLocalChangelog().clear();
        }
    }

    public static LocalSyncProjectMapping getProjectMapping(Project project) {
        LocalSyncProjectMapping localSyncProjectMapping = projectMappings.get(project.getID());
        if (localSyncProjectMapping == null) {
            projectMappings.put(project.getID(), localSyncProjectMapping = new LocalSyncProjectMapping());
        }
        return localSyncProjectMapping;
    }

    public static class LocalSyncProjectMapping {
        private LocalSyncTransactionCommitListener localSyncTransactionCommitListener;

        public LocalSyncTransactionCommitListener getLocalSyncTransactionCommitListener() {
            return localSyncTransactionCommitListener;
        }

        public void setLocalSyncTransactionCommitListener(LocalSyncTransactionCommitListener localSyncTransactionCommitListener) {
            this.localSyncTransactionCommitListener = localSyncTransactionCommitListener;
        }
    }
}
