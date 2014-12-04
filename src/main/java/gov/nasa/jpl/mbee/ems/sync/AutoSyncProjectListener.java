package gov.nasa.jpl.mbee.ems.sync;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import gov.nasa.jpl.mbee.ems.ExportUtility;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.magicdraw.uml.transaction.MDTransactionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.transaction.TransactionManager;

/*
 * This class is responsible for taking action when a project is opened.
 * This class does the following when instantiated:
 *   1. Create a transaction manager
 *   2. Create a TransactionCommitListener object
 *   3. Add the listener to the transaction manager object 
 *   4. Create a JMS topic and connection to that topic
 *   5. Store that connection so we keep track of the connections to JMS.
 *   
 */
public class AutoSyncProjectListener extends ProjectEventListenerAdapter {

    private static final String CONNECTION = "Connection";
    public static final String LISTENER = "AutoSyncCommitListener";
    private static final String SESSION = "Session";
    private static final String CONSUMER = "MessageConsumer";

    private static final String MSG_SELECTOR_PROJECT_ID = "projectId";
    private static final String MSG_SELECTOR_WS_ID = "workspace";

    public static String getJMSUrl() {
        String url = ExportUtility.getUrl();
        if (url != null) {
            if (url.startsWith("https://"))
                url = url.substring(8);
            else if (url.startsWith("http://"))
                url = url.substring(7);
            int index = url.indexOf(":");
            if (index != -1)
                url = url.substring(0, index);
            if (url.endsWith("/alfresco/service"))
                url = url.substring(0, url.length() - 17);
            url = "tcp://" + url + ":61616";
        } 
        return url;
    }

    public static void initDurable(Project project) {
        Map<String, Object> projectInstances = ProjectListenerMapping.getInstance().get(project);
        String projectID = ExportUtility.getProjectId(project);
        String wsID = ExportUtility.getWorkspace();
        String subscriberId = getSubscriberId(project);
        // Check if the keywords are found in the current project. If so, it
        // indicates that this JMS subscriber has already been init'ed.
        //
        if (projectInstances.containsKey(CONNECTION) || projectInstances.containsKey(SESSION)
                || projectInstances.containsKey(CONSUMER) || projectInstances.containsKey(LISTENER)) {
            return;
        }
        String url = getJMSUrl();
        if (url == null) {
            Application.getInstance().getGUILog().log("[ERROR] sync initialization failed - cannot get server url");
            return;
        }
        if (wsID == null) {
            Application.getInstance().getGUILog().log("[ERROR] sync initialization failed - cannot get server workspace that corresponds to this project branch");
            return;
        }
        Integer webVersion = ExportUtility.getAlfrescoProjectVersion(ExportUtility.getProjectId(project));
        Integer localVersion = ExportUtility.getProjectVersion(project);
        if (localVersion != null && !localVersion.equals(webVersion)) {
            Application.getInstance().getGUILog().log("[ERROR] autosync not allowed - project versions currently don't match - project may be out of date");
            return;
        }
        if (ProjectUtilities.isFromTeamworkServer(project.getPrimaryProject())) {
            String user = TeamworkUtils.getLoggedUserName();
            if (user == null) {
                Application.getInstance().getGUILog().log("[ERROR] You must be logged into teamwork - autosync will not start");
                return;
            }
            Collection<Element> lockedByUser = TeamworkUtils.getLockedElement(project, user);
            Collection<Element> lockedByAll = TeamworkUtils.getLockedElement(project, null);
            if (!lockedByUser.equals(lockedByAll)) {
                Application.getInstance().getGUILog().log("[ERROR] Another user has locked part of the project - autosync will not start");
                return;
            }
            if (!TeamworkUtils.lockElement(project, project.getModel(), true)) {
                Application.getInstance().getGUILog().log("[ERROR] cannot lock project recursively - autosync will not start");
                return;
            }
        }
        try {
            AutoSyncCommitListener listener = new AutoSyncCommitListener();
            MDTransactionManager transactionManager = (MDTransactionManager) project.getRepository()
                    .getTransactionManager();
            listener.setTm(transactionManager);

            transactionManager.addTransactionCommitListenerIncludingUndoAndRedo(listener);
            projectInstances.put(LISTENER, listener);

            ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);

            Connection connection = connectionFactory.createConnection();
            connection.setClientID(subscriberId);// + (new Date()).toString());
            // connection.setExceptionListener(this);
            Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

            Topic topic = session.createTopic("master");

            String messageSelector = constructSelectorString(projectID, wsID);
            
            MessageConsumer consumer = session.createDurableSubscriber(topic, subscriberId, messageSelector, true);
            consumer.setMessageListener(new JMSMessageListener(project));
            connection.start();
            projectInstances.put(CONNECTION, connection);
            projectInstances.put(SESSION, session);
            projectInstances.put(CONSUMER, consumer);

            Application.getInstance().getGUILog().log("[INFO] sync initiated");
        }
        catch (Exception e) {
            Application.getInstance().getGUILog().log("[ERROR] sync initialization failed: " + e.getMessage());
        }
    }

    public static String getSubscriberId(Project proj) {
        String projId = ExportUtility.getProjectId(proj);
        String ws = ExportUtility.getTeamworkBranch(proj);
        if (ws == null)
            ws = "master";
        return projId + "/" + ws;
    }

    public static String constructSelectorString(String projectID, String workspaceID) {
        StringBuilder selectorBuilder = new StringBuilder();

        //selectorBuilder.append("(").append(MSG_SELECTOR_WS_ID).append("='").append(workspaceID).append("')");

         selectorBuilder.append("(").append(MSG_SELECTOR_PROJECT_ID).append(" = '").append(projectID).append("')")
         .append(" AND ").append("(").append(MSG_SELECTOR_WS_ID).append(" = '").append(workspaceID).append("')");

        String outputMsgSelector = selectorBuilder.toString();
        selectorBuilder.delete(0, selectorBuilder.length());

        return outputMsgSelector;
    }

    public static void close(Project project) {
        Map<String, Object> projectInstances = ProjectListenerMapping.getInstance().get(project);
        AutoSyncCommitListener listener = (AutoSyncCommitListener) projectInstances.remove(LISTENER);
        if (listener != null)
            project.getRepository().getTransactionManager().removeTransactionCommitListener(listener);
        Connection connection = (Connection) projectInstances.remove(CONNECTION);
        Session session = (Session) projectInstances.remove(SESSION);
        MessageConsumer consumer = (MessageConsumer) projectInstances.remove(CONSUMER);
        try {
            if (consumer != null)
                consumer.close();
            if (session != null)
                session.close();
            if (connection != null)
                connection.close();
        }
        catch (Exception e) {

        }
        Application.getInstance().getGUILog().log("[INFO] sync ended");
    }

    @Override
    public void projectOpened(Project project) {
        Map<String, Object> projectInstances = new HashMap<String, Object>();
        ProjectListenerMapping.getInstance().put(project, projectInstances);
        //ExportUtility.updateWorkspaceIdMapping();
        // init(project);
    }

    @Override
    public void projectClosed(Project project) {
        close(project);
        ProjectListenerMapping.getInstance().remove(project);
    }
    
    @Override
    public void projectSaved(Project project, boolean savedInServer) {
        Map<String, Object> projectInstances = ProjectListenerMapping.getInstance().get(project);
        if (projectInstances.containsKey(CONNECTION) || projectInstances.containsKey(SESSION)
                || projectInstances.containsKey(CONSUMER) || projectInstances.containsKey(LISTENER)) {
            //autosync is on
            ExportUtility.sendProjectVersion();
        }
    }
}
