package gov.nasa.jpl.mbee.mdk.mms.sync.coordinated;

import com.nomagic.ci.persistence.IProject;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.core.SaveParticipant;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import gov.nasa.jpl.mbee.mdk.mms.actions.MMSLoginAction;
import gov.nasa.jpl.mbee.mdk.mms.sync.delta.DeltaSyncRunner;
import gov.nasa.jpl.mbee.mdk.options.MDKEnvironmentOptionsGroup;
import gov.nasa.jpl.mbee.mdk.options.MDKProjectOptions;
import gov.nasa.jpl.mbee.mdk.util.TicketUtils;

import javax.annotation.CheckForNull;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by igomes on 6/22/16.
 */
public class CoordinatedSyncProjectEventListenerAdapter extends ProjectEventListenerAdapter implements SaveParticipant {
    private static final Map<Project, CoordinatedSyncProjectMapping> projectMappings = Collections.synchronizedMap(new WeakHashMap<>());
    private DeltaSyncRunner deltaSyncRunner;

    @Override
    public void projectClosed(Project project) {
        projectMappings.remove(project);
    }

    @Override
    public void projectReplaced(Project oldProject, Project newProject) {
        projectClosed(oldProject);
        projectOpened(newProject);
    }

    @Override
    public void projectPreSaved(Project project, boolean savedInServer) {
        deltaSyncRunner = null;
        /*boolean tempDisabled = true;
        if (tempDisabled) {
            return;
        }*/
        if ((project.isRemote() && !savedInServer)
                || !MDKProjectOptions.getMbeeEnabled(project)
                || CoordinatedSyncProjectEventListenerAdapter.getProjectMapping(project).isDisabled()
                || !TicketUtils.isTicketSet(project)) {
            // skip csync
            return;
        }
        deltaSyncRunner = new DeltaSyncRunner(true, true, true);
        ProgressStatusRunner.runWithProgressStatus(deltaSyncRunner, "Coordinated Sync", true, 0);
    }

    @Override
    public void projectSaved(Project project, boolean savedInServer) {

    }

    public static CoordinatedSyncProjectMapping getProjectMapping(Project project) {
        CoordinatedSyncProjectMapping coordinatedSyncProjectMapping = projectMappings.get(project);
        if (coordinatedSyncProjectMapping == null) {
            projectMappings.put(project, coordinatedSyncProjectMapping = new CoordinatedSyncProjectMapping());
            projectMappings.get(project).setDisabled(!project.isRemote());
        }
        return coordinatedSyncProjectMapping;
    }

    public DeltaSyncRunner getDeltaSyncRunner() {
        return deltaSyncRunner;
    }

    @Override
    public boolean isReadyForSave(Project project, ProjectDescriptor projectDescriptor) {
        return true;
    }

    @Override
    public void doBeforeSave(Project project, ProjectDescriptor projectDescriptor) {

    }

    @Override
    public void doAfterSave(Project project, ProjectDescriptor projectDescriptor) {

    }

    @Override
    public boolean isReadyForCommit(IProject iProject, @CheckForNull String s) {
        Project project = ProjectUtilities.getProject(iProject);
        if (project == null) {
            return true;
        }
        if (!MDKProjectOptions.getMbeeEnabled(project)) {
            return true;
        }
        if (CoordinatedSyncProjectEventListenerAdapter.getProjectMapping(project).isDisabled()) {
            return true;
        }
        if (!TicketUtils.isTicketSet(project) && !MMSLoginAction.loginAction(project)) {
            Application.getInstance().getGUILog().log("<span style=\"color:#FF0000; font-weight:bold\">[WARNING] You must be logged in to MMS to synchronize the commit. Skipping commit. Please login to MMS and then commit again.</span>");
            if (s != null && !s.isEmpty()) {
                Application.getInstance().getGUILog().log("[INFO] Recovered commit message: <pre>" + s + "</pre>");
            }
            return false;
        }
        return true;
    }


    public static class CoordinatedSyncProjectMapping {
        private boolean disabled;

        public synchronized boolean isDisabled() {
            return (disabled || !MDKEnvironmentOptionsGroup.getInstance().isCoordinatedSyncEnabled());
        }

        public synchronized void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }
    }
}
