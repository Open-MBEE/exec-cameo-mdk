package gov.nasa.jpl.mbee.mdk.mms.sync.coordinated;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.ci.persistence.IProject;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.core.SaveParticipant;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.mms.actions.MMSLoginAction;
import gov.nasa.jpl.mbee.mdk.mms.jms.JMSUtils;
import gov.nasa.jpl.mbee.mdk.mms.sync.delta.DeltaSyncRunner;
import gov.nasa.jpl.mbee.mdk.mms.sync.delta.SyncElements;
import gov.nasa.jpl.mbee.mdk.mms.sync.jms.JMSMessageListener;
import gov.nasa.jpl.mbee.mdk.mms.sync.jms.JMSSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.mdk.options.MDKOptionsGroup;
import gov.nasa.jpl.mbee.mdk.util.MDUtils;
import gov.nasa.jpl.mbee.mdk.util.TicketUtils;

import javax.annotation.CheckForNull;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by igomes on 6/22/16.
 */
public class CoordinatedSyncProjectEventListenerAdapter extends ProjectEventListenerAdapter implements SaveParticipant {
    private static final Map<Project, CoordinatedSyncProjectMapping> projectMappings = new ConcurrentHashMap<>();
    private DeltaSyncRunner deltaSyncRunner;

    @Override
    public void projectClosed(Project project) {
        CoordinatedSyncProjectMapping coordinatedSyncProjectMapping = getProjectMapping(project);
        if (coordinatedSyncProjectMapping.isDisabled()) {
            return;
        }
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
                || !StereotypesHelper.hasStereotype(project.getPrimaryModel(), "ModelManagementSystem")
                || CoordinatedSyncProjectEventListenerAdapter.getProjectMapping(project).isDisabled()
                || JMSSyncProjectEventListenerAdapter.getProjectMapping(project).getJmsMessageListener().isDisabled()) {
            // skip csync
            return;
        }
        deltaSyncRunner = new DeltaSyncRunner(true, true, true);
        ProgressStatusRunner.runWithProgressStatus(deltaSyncRunner, "Coordinated Sync", true, 0);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void projectSaved(Project project, boolean savedInServer) {
        CoordinatedSyncProjectMapping coordinatedSyncProjectMapping = getProjectMapping(project);
        if (coordinatedSyncProjectMapping.isDisabled() || deltaSyncRunner == null || deltaSyncRunner.isFailure()) {
            // CSync isn't running, so return
            return;
        }
        JMSSyncProjectEventListenerAdapter.JMSSyncProjectMapping jmsSyncProjectMapping = JMSSyncProjectEventListenerAdapter.getProjectMapping(project);
        JMSMessageListener jmsMessageListener = jmsSyncProjectMapping.getJmsMessageListener();

        // ACKNOWLEDGE LAST MMS MESSAGE TO CLEAR OWN QUEUE

        Message lastMessage;
        if (jmsMessageListener != null && (lastMessage = jmsMessageListener.getLastMessage()) != null) {
            try {
                lastMessage.acknowledge();
            } catch (JMSException | IllegalStateException e) {
                e.printStackTrace();
            }
        }

        // NOTIFY OTHER USERS OF PROCESSED ELEMENTS

        if (!deltaSyncRunner.getSuccessfulJmsChangelog().isEmpty()) {
            ObjectNode teamworkCommittedMessage = JacksonUtils.getObjectMapper().createObjectNode();
            teamworkCommittedMessage.put("source", "magicdraw");
            teamworkCommittedMessage.put("sender", TicketUtils.getUsername(project));
            teamworkCommittedMessage.set("synced", SyncElements.buildJson(deltaSyncRunner.getSuccessfulJmsChangelog()));
            try {
                TextMessage successfulTextMessage = jmsSyncProjectMapping.getSession().createTextMessage(JacksonUtils.getObjectMapper().writeValueAsString(teamworkCommittedMessage));
                successfulTextMessage.setStringProperty(JMSUtils.MSG_SELECTOR_PROJECT_ID, Converters.getIProjectToIdConverter().apply(project.getPrimaryProject()));
                successfulTextMessage.setStringProperty(JMSUtils.MSG_SELECTOR_REF_ID, MDUtils.getBranchId(project) + "_mdk");
                jmsSyncProjectMapping.getMessageProducer().send(successfulTextMessage);
                int syncCount = deltaSyncRunner.getSuccessfulJmsChangelog().flattenedSize();
                Application.getInstance().getGUILog().log("[INFO] Notified other clients of " + syncCount + " locally updated element" + (syncCount != 1 ? "s" : "") + ".");
            } catch (JMSException | JsonProcessingException e) {
                e.printStackTrace();
                Application.getInstance().getGUILog().log("[ERROR] Failed to notify other clients of synced elements. This could result in redundant local updates.");
            }
        }
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
        if (!StereotypesHelper.hasStereotype(project.getPrimaryModel(), "ModelManagementSystem")) {
            return true;
        }
        if (CoordinatedSyncProjectEventListenerAdapter.getProjectMapping(project).isDisabled()) {
            return true;
        }
        if (JMSSyncProjectEventListenerAdapter.getProjectMapping(project).getJmsMessageListener().isDisabled()) {
            new Thread(() -> MMSLoginAction.loginAction(project)).start();
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
            return (disabled || !MDKOptionsGroup.getMDKOptions().isCoordinatedSyncEnabled());
        }

        public synchronized void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }
    }
}
