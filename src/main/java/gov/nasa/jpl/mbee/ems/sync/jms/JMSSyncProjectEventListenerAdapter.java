package gov.nasa.jpl.mbee.ems.sync.jms;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;

import gov.nasa.jpl.mbee.actions.ems.EMSLoginAction;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.ServerException;
import gov.nasa.jpl.mbee.ems.jms.JMSUtils;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.options.MDKOptionsGroup;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import javax.naming.NameNotFoundException;
import javax.swing.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by igomes on 6/28/16.
 */
public class JMSSyncProjectEventListenerAdapter extends ProjectEventListenerAdapter {
    private static final String ERROR_STRING = "Reverting to offline mode. All changes will be saved in the model until reconnected.";
    private static final Map<String, JMSSyncProjectMapping> projectMappings = new ConcurrentHashMap<>();

    @Override
    public void projectOpened(final Project project) {
        closeJMS(project);
        new Thread() {
            public void run() {
                if (shouldEnableJMS(project)) {
                    if (ViewEditUtils.getTicket() == null || ViewEditUtils.getTicket().isEmpty()) {
                        EMSLoginAction.loginAction("", "", true);
                        // loginAction contains a call back to initializeProject if appropriate
                    }
                    else {
                        initializeJMS(project);
                    }
                }
            }
        }.start();
    }

    @Override
    public void projectClosed(Project project) {
        closeJMS(project);
    }

    @Override
    public void projectReplaced(Project oldProject, Project newProject) {
        projectClosed(oldProject);
        projectOpened(newProject);
    }

    @Override
    public void projectSaved(Project project, boolean savedInServer) {
        JMSSyncProjectMapping jmsSyncProjectMapping = getProjectMapping(project);

        JMSMessageListener JMSMessageListener = jmsSyncProjectMapping.getJmsMessageListener();
        if (JMSMessageListener != null) {
            JMSMessageListener.getInMemoryJMSChangelog().clear();
        }
    }
    
    public static boolean shouldEnableJMS(Project project) {
        return ((project.getModel() != null) && project.isRemote() && MDKOptionsGroup.getMDKOptions().isChangeListenerEnabled()
                && StereotypesHelper.hasStereotype(project.getModel(), "ModelManagementSystem"));
    }
    
    public void initializeJMS(Project project) {
        JMSSyncProjectMapping jmsSyncProjectMapping = getProjectMapping(project);
        boolean initialized = initDurable(project);
        jmsSyncProjectMapping.setDisabled(!shouldEnableJMS(project) || !initialized);
    }
    
    public void closeJMS(Project project) {
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
        jmsSyncProjectMapping.setMessageConsumer(null);
        jmsSyncProjectMapping.setSession(null);
        jmsSyncProjectMapping.setConnection(null);
        jmsSyncProjectMapping.setMessageProducer(null);
        jmsSyncProjectMapping.setDisabled(true);

        // We want to keep the JMSMessageListener so exceptions are handled by the same ExceptionListener through disconnects.
        // Keeping it will also persist the changelog, which is the conservative approach albeit redundant since
        // all the same JMS messages will come through on reconnect.
        //projectMappings.remove(project.getID());
    }

    private boolean initDurable(Project project) {
        JMSSyncProjectMapping jmsSyncProjectMapping = getProjectMapping(project);
        String projectID = ExportUtility.getProjectId(project);
        String workspaceID = ExportUtility.getWorkspace();

        if (ViewEditUtils.getTicket() == null || ViewEditUtils.getTicket().isEmpty()) {
            Application.getInstance().getGUILog().log("[WARNING] " + project.getName() + " - " + ERROR_STRING + " Reason: You must be logged into MMS.");
            return false;
        }

        JMSUtils.JMSInfo jmsInfo;
        try {
            jmsInfo = JMSUtils.getJMSInfo(project);
        } catch (ServerException | IllegalArgumentException e) {
            e.printStackTrace();
            Application.getInstance().getGUILog().log("[WARNING] " + project.getName() + " - " + ERROR_STRING + " Reason: " + e.getMessage());
            return false;
        }
        String url = jmsInfo != null ? jmsInfo.getUrl() : null;
        if (url == null) {
            Application.getInstance().getGUILog().log("[WARNING] " + project.getName() + " - " + ERROR_STRING + " Reason: Cannot get server URL.");
            return false;
        }
        if (workspaceID == null) {
            Application.getInstance().getGUILog().log("[WARNING] " + project.getName() + " - " + ERROR_STRING + "Reason: Cannot get the server workspace that corresponds to this project branch.");
            return false;
        }
        if (ProjectUtilities.isFromTeamworkServer(project.getPrimaryProject())) {
            if (TeamworkUtils.getLoggedUserName() == null) {
                Application.getInstance().getGUILog().log("[WARNING] " + project.getName() + " - " + ERROR_STRING + " Reason: You must be logged into Teamwork.");
                return false;
            }
        }
        try {
            ConnectionFactory connectionFactory = JMSUtils.createConnectionFactory(JMSUtils.getJMSInfo(project));
            if (connectionFactory == null) {
                Application.getInstance().getGUILog().log("[WARNING] " + project.getName() + " - " + ERROR_STRING + " Reason: Failed to create JMS connection factory.");
                return false;
            }
            String subscriberId = projectID + "-" + workspaceID + "-" + ViewEditUtils.getUsername(); // weblogic can't have '/' in id

            // re-use existing JMSMessageListener
            JMSMessageListener jmsMessageListener = jmsSyncProjectMapping.getJmsMessageListener();
            if (jmsMessageListener == null) {
                jmsSyncProjectMapping.setJmsMessageListener(jmsMessageListener = new JMSMessageListener(project));
            }

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
            //jmsSyncProjectMapping.setJmsMessageListener(jmsMessageListener);
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

            Application.getInstance().getGUILog().log("[INFO] " + project.getName() + " - MMS sync initiated.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            jmsSyncProjectMapping.setDisabled(true);
            Application.getInstance().getGUILog().log("[WARNING] " + project.getName() + " - " + ERROR_STRING + " Reason: " + e.getMessage());
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

        private volatile boolean disabled = true;

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
