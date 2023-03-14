package org.openmbee.mdk.mms.sync.local;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by igomes on 6/28/16.
 */
public class LocalDeltaProjectEventListenerAdapter extends ProjectEventListenerAdapter {
    private static final Map<Project, LocalSyncProjectMapping> projectMappings = Collections.synchronizedMap(new WeakHashMap<>());

    @Override
    public void projectCreated(Project project) {
        projectOpened(project);
    }

    // Cannot rely on this being called when MagicDraw programmatically reloads a project, which makes the old Project stale.
    // Mitigating by duplicating logic in mapping constructor as an attempt to have better coverage.
    @Override
    public void projectOpened(Project project) {
        closeLocalCommitListener(project);
        if (project.isRemote()) {
            project.getRepository().getTransactionManager().addTransactionCommitListenerIncludingUndoAndRedo(getProjectMapping(project).getLocalDeltaTransactionCommitListener());
        }
    }

    @Override
    public void projectClosed(Project project) {
        closeLocalCommitListener(project);
        projectMappings.remove(project);
    }

    @Override
    public void projectReplaced(Project oldProject, Project newProject) {
        projectClosed(oldProject);
        projectOpened(newProject);
    }

    @Override
    public void projectSaved(Project project, boolean savedInServer) {
        LocalSyncProjectMapping localSyncProjectMapping = getProjectMapping(project);
        LocalDeltaTransactionCommitListener listener = localSyncProjectMapping.getLocalDeltaTransactionCommitListener();
        if (listener == null) {
            projectOpened(project);
            listener = LocalDeltaProjectEventListenerAdapter.getProjectMapping(project).getLocalDeltaTransactionCommitListener();
        }
        listener.getInMemoryLocalChangelog().clear();
    }

    private static void closeLocalCommitListener(Project project) {
        LocalSyncProjectMapping localSyncProjectMapping = projectMappings.get(project);
        if (localSyncProjectMapping != null && localSyncProjectMapping.getLocalDeltaTransactionCommitListener() != null) {
            project.getRepository().getTransactionManager().removeTransactionCommitListener(localSyncProjectMapping.getLocalDeltaTransactionCommitListener());
        }
    }

    public static LocalSyncProjectMapping getProjectMapping(Project project) {
        LocalSyncProjectMapping localSyncProjectMapping = projectMappings.get(project);
        if (localSyncProjectMapping == null) {
            projectMappings.put(project, localSyncProjectMapping = new LocalSyncProjectMapping(project));
            if (project.isRemote()) {
                project.getRepository().getTransactionManager().addTransactionCommitListenerIncludingUndoAndRedo(localSyncProjectMapping.getLocalDeltaTransactionCommitListener());
            }
        }
        return localSyncProjectMapping;
    }

    public static class LocalSyncProjectMapping {
        private LocalDeltaTransactionCommitListener localDeltaTransactionCommitListener;

        public LocalSyncProjectMapping(Project project) {
            localDeltaTransactionCommitListener = new LocalDeltaTransactionCommitListener(project);
        }

        public LocalDeltaTransactionCommitListener getLocalDeltaTransactionCommitListener() {
            return localDeltaTransactionCommitListener;
        }

    }
}
