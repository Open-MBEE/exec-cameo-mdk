package gov.nasa.jpl.mbee.ems.sync.jms;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.ServerException;
import gov.nasa.jpl.mbee.ems.jms.JMSUtils;
import gov.nasa.jpl.mbee.options.MDKOptionsGroup;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;

import javax.jms.*;
import javax.naming.NameNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by igomes on 6/28/16.
 */
public class JMSSyncProjectEventListenerAdapter extends ProjectEventListenerAdapter {
    private static final Map<String, JMSSyncProjectMapping> projectMappings = new ConcurrentHashMap<>();

    @Override
    public void projectOpened(Project project) {
        projectClosed(project);
        JMSSyncProjectMapping jmsSyncProjectMapping = getProjectMapping(project);
        jmsSyncProjectMapping.setDisabled(!MDKOptionsGroup.getMDKOptions().isChangeListenerEnabled() || !StereotypesHelper.hasStereotype(project.getModel(), "ModelManagementSystem") || !initDurable(project, jmsSyncProjectMapping));
    }

    @Override
    public void projectClosed(Project project) {
        JMSSyncProjectMapping jmsSyncProjectMapping = getProjectMapping(project);
        try {
            if (jmsSyncProjectMapping.getMessageConsumer() != null) {
                jmsSyncProjectMapping.getMessageConsumer().close();
            }
            if (jmsSyncProjectMapping.getSession() != null) {
                jmsSyncProjectMapping.getSession().close();
            }
            if (jmsSyncProjectMapping.getConnection() != null) {
                jmsSyncProjectMapping.getConnection().close();
            }
        } catch (JMSException e) {
            e.printStackTrace();
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
        JMSSyncProjectMapping jmsSyncProjectMapping = JMSSyncProjectEventListenerAdapter.getProjectMapping(project);

        JMSMessageListener JMSMessageListener = jmsSyncProjectMapping.getJmsMessageListener();
        if (JMSMessageListener != null) {
            JMSMessageListener.getInMemoryJMSChangelog().clear();
        }
        if (jmsSyncProjectMapping.isDisabled() && MDKOptionsGroup.getMDKOptions().isChangeListenerEnabled() && StereotypesHelper.hasStereotype(project.getModel(), "ModelManagementSystem")) {
            Application.getInstance().getGUILog().log("[INFO] Attempting to re-initialize MMS sync.");
            projectOpened(project);
        }
    }

    public static boolean initDurable(Project project, final JMSSyncProjectMapping jmsSyncProjectMapping) {
        String projectID = ExportUtility.getProjectId(project);
        String workspaceID = ExportUtility.getWorkspace();


        JMSUtils.JMSInfo jmsInfo;
        try {
            jmsInfo = JMSUtils.getJMSInfo(project);
        } catch (ServerException | IllegalArgumentException e) {
            e.printStackTrace();
            Application.getInstance().getGUILog().log("[ERROR] MMS sync initialization failed. Message: " + e.getMessage());
            return false;
        }
        String url = jmsInfo != null ? jmsInfo.getUrl() : null;
        if (url == null) {
            Application.getInstance().getGUILog().log("[ERROR] MMS sync initialization failed. Cannot get server URL.");
            return false;
        }
        if (workspaceID == null) {
            Application.getInstance().getGUILog().log("[ERROR] MMS sync initialization failed. Cannot get the server workspace that corresponds to this project branch.");
            return false;
        }
        if (ProjectUtilities.isFromTeamworkServer(project.getPrimaryProject())) {
            String user = TeamworkUtils.getLoggedUserName();
            if (user == null) {
                Application.getInstance().getGUILog().log("[ERROR] You must be logged into Teamwork. MMS sync will not start.");
                return false;
            }
        }
        String username = ViewEditUtils.getUsername();
        if (username == null || username.isEmpty()) {
            Application.getInstance().getGUILog().log("[ERROR] MMS sync initialization failed. Could not login to MMS.");
            return false;
        }
        try {
            ConnectionFactory connectionFactory = JMSUtils.createConnectionFactory(JMSUtils.getJMSInfo(project));
            if (connectionFactory == null) {
                Application.getInstance().getGUILog().log("[ERROR] Failed to create MMS connection factory.");
                return false;
            }
            String subscriberId = projectID + "-" + workspaceID + "-" + username; // weblogic can't have '/' in id

            JMSMessageListener jmsMessageListener = new JMSMessageListener(project);

            Connection connection = connectionFactory.createConnection();
            //((WLConnection) connection).setReconnectPolicy(JMSConstants.RECONNECT_POLICY_ALL);
            connection.setExceptionListener(jmsMessageListener);
            connection.setClientID(subscriberId);
            Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

            Topic topic = null;
            try {
                if (JMSUtils.getInitialContext() != null) {
                    topic = (Topic) JMSUtils.getInitialContext().lookup(JMSUtils.JMS_TOPIC);
                }
            } catch (NameNotFoundException ignored) {
                // do nothing; just means topic hasn't been created yet
            }
            if (topic == null) {
                topic = session.createTopic(JMSUtils.JMS_TOPIC);
            }
            String messageSelector = JMSUtils.constructSelectorString(projectID, workspaceID);
            MessageConsumer consumer = session.createDurableSubscriber(topic, subscriberId, messageSelector, true);
            consumer.setMessageListener(jmsMessageListener);

            MessageProducer producer = session.createProducer(topic);

            connection.start();

            jmsSyncProjectMapping.setConnection(connection);
            jmsSyncProjectMapping.setSession(session);
            jmsSyncProjectMapping.setMessageConsumer(consumer);
            jmsSyncProjectMapping.setJmsMessageListener(jmsMessageListener);
            jmsSyncProjectMapping.setMessageProducer(producer);

            // get everything that's already in the queue without blocking startup
            /*new Thread() {
                @Override
                public void run() {
                    List<TextMessage> textMessages = jmsSyncProjectMapping.getAllTextMessages(false);
                    if (textMessages != null) {
                        for (TextMessage textMessage : textMessages) {
                            jmsSyncProjectMapping.getJmsMessageListener().onMessage(textMessage);
                        }
                    }
                }
            }.start();*/

            Application.getInstance().getGUILog().log("[INFO] MMS sync initiated.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            jmsSyncProjectMapping.setDisabled(true);
            Application.getInstance().getGUILog().log("[ERROR] MMS sync initialization failed: " + e.getMessage());
        }
        return false;
    }

    public static JMSSyncProjectMapping getProjectMapping(Project project) {
        JMSSyncProjectMapping JMSSyncProjectMapping = projectMappings.get(project.getID());
        if (JMSSyncProjectMapping == null) {
            projectMappings.put(project.getID(), JMSSyncProjectMapping = new JMSSyncProjectMapping());
        }
        return JMSSyncProjectMapping;
    }

    public static class JMSSyncProjectMapping {
        private Connection connection;
        private Session session;
        private MessageConsumer messageConsumer;
        private JMSMessageListener jmsMessageListener;
        private MessageProducer messageProducer;

        private volatile boolean disabled;

        public Connection getConnection() {
            return connection;
        }

        public void setConnection(Connection connection) {
            this.connection = connection;
        }

        public Session getSession() {
            return session;
        }

        public void setSession(Session session) {
            this.session = session;
        }

        public MessageConsumer getMessageConsumer() {
            return messageConsumer;
        }

        public void setMessageConsumer(MessageConsumer messageConsumer) {
            this.messageConsumer = messageConsumer;
        }

        public JMSMessageListener getJmsMessageListener() {
            return jmsMessageListener;
        }

        public void setJmsMessageListener(JMSMessageListener jmsMessageListener) {
            this.jmsMessageListener = jmsMessageListener;
        }

        public MessageProducer getMessageProducer() {
            return messageProducer;
        }

        public void setMessageProducer(MessageProducer messageProducer) {
            this.messageProducer = messageProducer;
        }

        public boolean isDisabled() {
            if (jmsMessageListener == null) {
                disabled = true;
            }
            return disabled;
        }

        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }

        @Deprecated
        public List<TextMessage> getAllTextMessages(boolean shouldAcknowledge) {
            boolean print = MDKOptionsGroup.getMDKOptions().isLogJson();
            List<TextMessage> textMessages = new ArrayList<>();
            Message message;
            try {
                while (getMessageConsumer() != null && (message = getMessageConsumer().receive(10000)) != null) {
                    if (!(message instanceof TextMessage)) {
                        continue;
                    }
                    TextMessage textMessage = (TextMessage) message;
                    if (print) {
                        System.out.println("From MMS: " + textMessage.getText());
                    }
                    textMessages.add(textMessage);

                    if (shouldAcknowledge) {
                        try {
                            message.acknowledge();
                        } catch (JMSException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (JMSException e) {
                e.printStackTrace();
                return null;
            }
            return textMessages;
        }
    }
}
