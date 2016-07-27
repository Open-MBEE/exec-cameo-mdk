package gov.nasa.jpl.mbee.ems.sync.jms;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.jms.JMSUtils;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.options.MDKOptionsGroup;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;
import org.apache.activemq.ActiveMQConnection;
import org.netbeans.lib.cvsclient.commandLine.command.log;
import weblogic.jms.common.JMSConstants;
import weblogic.jms.extensions.WLConnection;

import javax.jms.*;
import javax.naming.NameNotFoundException;
import javax.swing.*;
import java.util.*;
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
        jmsSyncProjectMapping.setDisabled(!initDurable(project, jmsSyncProjectMapping));
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
    public void projectSaved(Project project, boolean savedInServer) {
        JMSSyncProjectMapping JMSSyncProjectMapping = JMSSyncProjectEventListenerAdapter.getProjectMapping(project);

        JMSMessageListener JMSMessageListener = JMSSyncProjectMapping.getJmsMessageListener();
        if (JMSMessageListener != null) {
            JMSMessageListener.getInMemoryJMSChangelog().clear();
        }
    }

    public static boolean initDurable(Project project, final JMSSyncProjectMapping jmsSyncProjectMapping) {
        String projectID = ExportUtility.getProjectId(project);
        String workspaceID = ExportUtility.getWorkspace();

        JMSUtils.JMSInfo jmsInfo = JMSUtils.getJMSInfo(project);
        String url = jmsInfo.getUrl();
        if (url == null) {
            Application.getInstance().getGUILog().log("[ERROR] JMS sync initialization failed. Cannot get server URL.");
            return false;
        }
        if (workspaceID == null) {
            Application.getInstance().getGUILog().log("[ERROR] JMS sync initialization failed. Cannot get the server workspace that corresponds to this project branch.");
            return false;
        }
        if (ProjectUtilities.isFromTeamworkServer(project.getPrimaryProject())) {
            String user = TeamworkUtils.getLoggedUserName();
            if (user == null) {
                Utils.guilog("[ERROR] You must be logged into Teamwork. JMS sync will not start.");
                return false;
            }
        }
        String username = ViewEditUtils.getUsername();
        if (username == null || username.equals("")) {
            Utils.guilog("[ERROR] You must be logged into MMS first. JMS sync will not start.");
            return false;
        }
        try {
            ConnectionFactory connectionFactory = JMSUtils.createConnectionFactory(JMSUtils.getJMSInfo(project));
            if (connectionFactory == null) {
                Utils.guilog("[ERROR] Failed to create JMS connection factory.");
                return false;
            }
            String subscriberId = projectID + "-" + workspaceID + "-" + username; // weblogic can't have '/' in id
            Connection connection = connectionFactory.createConnection();
            //((WLConnection) connection).setReconnectPolicy(JMSConstants.RECONNECT_POLICY_ALL);
            connection.setExceptionListener(new ExceptionListener() {
                @Override
                public void onException(JMSException e) {
                    e.printStackTrace();
                    jmsSyncProjectMapping.setDisabled(true);
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            // TODO Implement auto-reconnect attempt
                            Application.getInstance().getGUILog().log("[WARNING] JMS sync interrupted. Restart MagicDraw to reconnect.");
                        }
                    });
                }
            });
            connection.setClientID(subscriberId);
            Session session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

            Topic topic = null;
            try {
                if (JMSUtils.getInitialContext() != null) {
                    topic = (Topic) JMSUtils.getInitialContext().lookup(JMSUtils.JMS_TOPIC);
                }
            } catch (NameNotFoundException ignored) {
                // do nothing (just means topic hasn't been created yet
            }
            if (topic == null) {
                topic = session.createTopic(JMSUtils.JMS_TOPIC);
            }

            String messageSelector = JMSUtils.constructSelectorString(projectID, workspaceID);

            MessageConsumer consumer = session.createDurableSubscriber(topic, subscriberId, messageSelector, true);

            JMSMessageListener jmsMessageListener = new JMSMessageListener(project);
            consumer.setMessageListener(jmsMessageListener);
            connection.start();

            jmsSyncProjectMapping.setConnection(connection);
            jmsSyncProjectMapping.setSession(session);
            jmsSyncProjectMapping.setMessageConsumer(consumer);
            jmsSyncProjectMapping.setJmsMessageListener(jmsMessageListener);

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

            Utils.guilog("[INFO] JMS sync initiated.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Utils.guilog("[ERROR] JMS sync initialization failed: " + e.getMessage());
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

        public boolean isDisabled() {
            return disabled;
        }

        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }

        public List<TextMessage> getAllTextMessages(boolean shouldAcknowledge) {
            boolean print = MDKOptionsGroup.getMDKOptions().isLogJson();
            List<TextMessage> textMessages = new ArrayList<>();
            Message message;
            try {
                while ((message = getMessageConsumer().receive(10000)) != null) {
                    if (!(message instanceof TextMessage)) {
                        continue;
                    }
                    TextMessage textMessage = (TextMessage) message;
                    if (print) {
                        System.out.println("From JMS: " + textMessage.getText());
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
