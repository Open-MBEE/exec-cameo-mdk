package gov.nasa.jpl.mbee.ems.sync.coordinated;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;

import gov.nasa.jpl.mbee.actions.ems.EMSLoginAction;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.jms.JMSUtils;
import gov.nasa.jpl.mbee.ems.sync.delta.DeltaSyncRunner;
import gov.nasa.jpl.mbee.ems.sync.delta.SyncElements;
import gov.nasa.jpl.mbee.ems.sync.jms.JMSMessageListener;
import gov.nasa.jpl.mbee.ems.sync.jms.JMSSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.options.MDKOptionsGroup;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;
import org.json.simple.JSONObject;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by igomes on 6/22/16.
 */
public class CoordinatedSyncProjectEventListenerAdapter extends ProjectEventListenerAdapter {
    private static final Map<String, CoordinatedSyncProjectMapping> projectMappings = new ConcurrentHashMap<>();
    private DeltaSyncRunner deltaSyncRunner;

    @Override
    public void projectClosed(Project project) {
        CoordinatedSyncProjectMapping coordinatedSyncProjectMapping = getProjectMapping(project);
        if (coordinatedSyncProjectMapping.isDisabled()) {
            return;
        }
        projectMappings.remove(project.getID());
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
        if ( !StereotypesHelper.hasStereotype(project.getModel(), "ModelManagementSystem")
                || !MDKOptionsGroup.getMDKOptions().isCoordinatedSyncEnabled()
                || CoordinatedSyncProjectEventListenerAdapter.getProjectMapping(project).isDisabled()
                || JMSSyncProjectEventListenerAdapter.getProjectMapping(project).isDisabled() ) {
            // skip csync
            return;
        }
        if (ViewEditUtils.getTicket() == null || ViewEditUtils.getTicket().isEmpty()) {
            Application.getInstance().getGUILog().log("[INFO] User is not logged in to MMS. Coordinated sync will be skipped for this commit. Attempting to reconnect to MMS for next commit.");
            EMSLoginAction.loginAction("", "", true);
            return;
        }
        if (project.isTeamworkServerProject() && !savedInServer) {
            Application.getInstance().getGUILog().log("[INFO] Teamwork project is being saved locally. Coordinated sync skipped.");
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
        JMSSyncProjectEventListenerAdapter.JMSSyncProjectMapping jmsSyncProjectMapping = JMSSyncProjectEventListenerAdapter.getProjectMapping(Application.getInstance().getProject());
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
            JSONObject teamworkCommittedMessage = new JSONObject();
            teamworkCommittedMessage.put("source", "magicdraw");
            teamworkCommittedMessage.put("sender", ViewEditUtils.getUsername());
            teamworkCommittedMessage.put("synced", SyncElements.buildJson(deltaSyncRunner.getSuccessfulJmsChangelog()));
            try {
                TextMessage successfulTextMessage = jmsSyncProjectMapping.getSession().createTextMessage(teamworkCommittedMessage.toJSONString());
                successfulTextMessage.setStringProperty(JMSUtils.MSG_SELECTOR_PROJECT_ID, ExportUtility.getProjectId(project));
                successfulTextMessage.setStringProperty(JMSUtils.MSG_SELECTOR_WORKSPACE_ID, ExportUtility.getWorkspace() + "_mdk");
                jmsSyncProjectMapping.getMessageProducer().send(successfulTextMessage);
                int syncCount = deltaSyncRunner.getSuccessfulJmsChangelog().flattenedSize();
                Application.getInstance().getGUILog().log("[INFO] Notified other clients of " + syncCount + " locally updated element" + (syncCount != 1 ? "s" : "") + ".");
            } catch (JMSException e) {
                e.printStackTrace();
                Application.getInstance().getGUILog().log("[ERROR] Failed to notify other clients of synced elements. This could result in redundant local updates.");
            }
        }
    }

    public static CoordinatedSyncProjectMapping getProjectMapping(Project project) {
        CoordinatedSyncProjectMapping coordinatedSyncProjectMapping = projectMappings.get(project.getID());
        if (coordinatedSyncProjectMapping == null) {
            projectMappings.put(project.getID(), coordinatedSyncProjectMapping = new CoordinatedSyncProjectMapping());
            projectMappings.get(project.getID()).setDisabled(!project.isRemote());
        }
        return coordinatedSyncProjectMapping;
    }

    public DeltaSyncRunner getDeltaSyncRunner() {
        return deltaSyncRunner;
    }

    public static class CoordinatedSyncProjectMapping {
        private boolean disabled;

        public synchronized boolean isDisabled() {
            return disabled;
        }

        public synchronized void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }
    }
}
