package gov.nasa.jpl.mbee.ems.sync.status;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;

/**
 * Created by igomes on 8/16/16.
 */
public class SyncStatusProjectEventListenerAdapter extends ProjectEventListenerAdapter {

    @Override
    public void projectOpened(Project project) {
        SyncStatusConfigurator.getSyncStatusAction().update();
    }

    @Override
    public void projectClosed(Project project) {
        projectOpened(project);
    }

    @Override
    public void projectReplaced(Project oldProject, Project newProject) {
        projectOpened(newProject);
    }

    @Override
    public void projectSaved(Project project, boolean savedInServer) {
        projectOpened(project);
    }
}
