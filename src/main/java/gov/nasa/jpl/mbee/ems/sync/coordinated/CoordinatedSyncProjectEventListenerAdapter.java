package gov.nasa.jpl.mbee.ems.sync.coordinated;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import gov.nasa.jpl.mbee.ems.sync.delta.DeltaSyncRunner;
import gov.nasa.jpl.mbee.options.MDKOptionsGroup;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by igomes on 6/22/16.
 */
public class CoordinatedSyncProjectEventListenerAdapter extends ProjectEventListenerAdapter {
    private static final Map<String, RealTimeSyncProjectMapping> projectMappings = new ConcurrentHashMap<>();

    @Override
    public void projectOpened(Project project) {
        /*RealTimeSyncProjectMapping realTimeSyncProjectMapping = getProjectMapping(project);
        if (realTimeSyncProjectMapping.isDisabled()) {
            return;
        }*/
        // ...
    }

    /*@Override
    public void projectActivated(Project project) {
        projectOpened(project);
    }*/

    @Override
    public void projectClosed(Project project) {
        RealTimeSyncProjectMapping realTimeSyncProjectMapping = getProjectMapping(project);
        if (realTimeSyncProjectMapping.isDisabled()) {
            return;
        }
        projectMappings.remove(project.getID());
    }

    /*@Override
    public void projectDeActivated(Project project) {
        projectClosed(project);
    }*/

    @Override
    public void projectPreSaved(Project project, boolean savedInServer) {
        /*boolean tempDisabled = true;
        if (tempDisabled) {
            return;
        }*/
        RealTimeSyncProjectMapping realTimeSyncProjectMapping = getProjectMapping(project);
        if (realTimeSyncProjectMapping.isDisabled()) {
            return;
        }
        boolean save = MDKOptionsGroup.getMDKOptions().isSaveChanges();
        if (!save) {
            return;
        }
        if (!StereotypesHelper.hasStereotype(project.getModel(), "ModelManagementSystem")) {
            return;
        }
        if (project.isTeamworkServerProject() && !savedInServer) {
            Application.getInstance().getGUILog().log("Teamwork server project is being saved locally. Real time sync skipped.");
            return;
        }
        DeltaSyncRunner deltaSyncRunner = new DeltaSyncRunner(true, true, true);
        ProgressStatusRunner.runWithProgressStatus(deltaSyncRunner, "Delta Sync", true, 0);
    }

    public static RealTimeSyncProjectMapping getProjectMapping(Project project) {
        RealTimeSyncProjectMapping realTimeSyncProjectMapping = projectMappings.get(project.getID());
        if (realTimeSyncProjectMapping == null) {
            projectMappings.put(project.getID(), realTimeSyncProjectMapping = new RealTimeSyncProjectMapping());
        }
        return realTimeSyncProjectMapping;
    }

    public static class RealTimeSyncProjectMapping {
        private volatile boolean disabled;

        public boolean isDisabled() {
            return disabled;
        }

        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }
    }
}
