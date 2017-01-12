package gov.nasa.jpl.mbee.mdk.ems.sync.local;

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
    public void projectCreated(Project project) {
        projectOpened(project);
    }

    @Override
    public void projectOpened(Project project) {
        projectClosed(project);
        LocalSyncProjectMapping localSyncProjectMapping = getProjectMapping(project);
        gov.nasa.jpl.mbee.mdk.ems.sync.local.LocalSyncTransactionCommitListener listener = localSyncProjectMapping.getLocalSyncTransactionCommitListener() != null ? localSyncProjectMapping.getLocalSyncTransactionCommitListener() : new gov.nasa.jpl.mbee.mdk.ems.sync.local.LocalSyncTransactionCommitListener(project);
        if (project.isRemote()) {
            ((MDTransactionManager) project.getRepository().getTransactionManager()).addTransactionCommitListenerIncludingUndoAndRedo(listener);
        }
        localSyncProjectMapping.setLocalSyncTransactionCommitListener(listener);
    }

    @Override
    public void projectClosed(Project project) {
        LocalSyncProjectMapping localSyncProjectMapping = getProjectMapping(project);
        if (localSyncProjectMapping.getLocalSyncTransactionCommitListener() != null) {
            project.getRepository().getTransactionManager().removeTransactionCommitListener(localSyncProjectMapping.getLocalSyncTransactionCommitListener());
        }
        //projectMappings.remove(project.getPrimaryProject().getProjectID());
    }

    @Override
    public void projectReplaced(Project oldProject, Project newProject) {
        projectClosed(oldProject);
        projectOpened(newProject);
    }

    @Override
    public void projectSaved(Project project, boolean savedInServer) {
        LocalSyncProjectMapping localSyncProjectMapping = LocalSyncProjectEventListenerAdapter.getProjectMapping(project);

        gov.nasa.jpl.mbee.mdk.ems.sync.local.LocalSyncTransactionCommitListener localSyncTransactionCommitListener = localSyncProjectMapping.getLocalSyncTransactionCommitListener();
        if (localSyncTransactionCommitListener == null) {
            projectOpened(project);
            localSyncTransactionCommitListener = LocalSyncProjectEventListenerAdapter.getProjectMapping(project).getLocalSyncTransactionCommitListener();
        }
        localSyncTransactionCommitListener.getInMemoryLocalChangelog().clear();
    }

    public static LocalSyncProjectMapping getProjectMapping(Project project) {
        LocalSyncProjectMapping localSyncProjectMapping = projectMappings.get(project.getPrimaryProject().getProjectID());
        if (localSyncProjectMapping == null) {
            projectMappings.put(project.getPrimaryProject().getProjectID(), localSyncProjectMapping = new LocalSyncProjectMapping());
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
