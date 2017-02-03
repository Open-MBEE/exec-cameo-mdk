package gov.nasa.jpl.mbee.mdk.ems.sync.status;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;

/**
 * Created by igomes on 8/16/16.
 */
public class SyncStatusProjectEventListenerAdapter extends ProjectEventListenerAdapter {

    @Override
    public void projectOpened(Project project) {
        updateSyncStatus();
    }

    @Override
    public void projectClosed(Project project) {
        updateSyncStatus();
    }

    @Override
    public void projectReplaced(Project oldProject, Project newProject) {
        projectOpened(newProject);
    }

    @Override
    public void projectSaved(Project project, boolean savedInServer) {
        updateSyncStatus();
    }

    private void updateSyncStatus() {
        SyncStatusConfigurator.getSyncStatusAction().update();
    }
}
