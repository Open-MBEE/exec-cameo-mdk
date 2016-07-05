package gov.nasa.jpl.mbee.ems.jms;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.ServerException;
import gov.nasa.jpl.mbee.lib.Changelog;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.function.Predicate;
import gov.nasa.jpl.mbee.options.MDKOptionsGroup;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.netbeans.lib.cvsclient.commandLine.command.log;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import java.text.ParseException;
import java.util.*;

/**
 * Created by igomes on 6/29/16.
 */
public class JMSUtils {
    private static final String MSG_SELECTOR_PROJECT_ID = "projectId",
            MSG_SELECTOR_WS_ID = "workspace";

    // Members to look up JMS using JNDI
    // TODO: If any other context factories are used, need to add those JARs into class path (e.g., for weblogic)
    public static String JMS_CTX_FACTORY = "org.apache.activemq.jndi.ActiveMQInitialContextFactory",
            JMS_CONN_FACTORY = "ConnectionFactory",
            JMS_USERNAME,
            JMS_PASSWORD,
            JMS_TOPIC = "master";

    private static InitialContext initialContext = null;

    /**
     * Gets JMS JNDI connection details from the MMS server
     *
     * @return JSONObject of the connection details
     */
    public static JSONObject getJmsConnectionDetails(Project project) {
        String url = ExportUtility.getUrl(project) + "/connection/jms";
        String jsonString = null;
        try {
            jsonString = ExportUtility.get(url, false);
        } catch (ServerException ignored) {
        }
        if (jsonString == null) {
            return null;
        }
        return (JSONObject) JSONValue.parse(jsonString);
    }

    // Varies by current project
    public static JMSInfo getJMSInfo(Project project) {
        JSONObject jmsJson = getJmsConnectionDetails(project);
        String url = ingestJson(jmsJson);
        boolean isFromService = url != null;
        if (url == null) {
            url = ExportUtility.getUrl(project);
            if (url != null) {
                if (url.startsWith("https://")) {
                    url = url.substring(8);
                }
                else if (url.startsWith("http://")) {
                    url = url.substring(7);
                }
                int index = url.indexOf(":");
                if (index != -1) {
                    url = url.substring(0, index);
                }
                if (url.endsWith("/alfresco/service")) {
                    url = url.substring(0, url.length() - 17);
                }
                url = "tcp://" + url + ":61616";
            }
        }
        return new JMSInfo(url, isFromService);
    }

    /**
     * Ingests JSON data generated from MMS server and populates JNDI members
     *
     * @return URL string of connector
     */
    protected static String ingestJson(JSONObject jsonInput) {
        if (jsonInput == null) {
            return null;
        }
        JSONObject json = null;
        if (jsonInput.containsKey("connections")) {
            // just grab first connection
            JSONArray conns = (JSONArray) jsonInput.get("connections");
            for (int ii = 0; ii < conns.size(); ii++) {
                json = (JSONObject) conns.get(ii);
                if (json.containsKey("eventType")) {
                    if (json.get("eventType").equals("DELTA")) {
                        break;
                    }
                }
            }
        }
        else {
            json = jsonInput;
        }
        String result = null;
        if (json != null) {
            if (json.containsKey("uri")) {
                result = (String) json.get("uri");
            }
            if (json.containsKey("connFactory")) {
                JMS_CONN_FACTORY = (String) json.get("connFactory");
            }
            if (json.containsKey("ctxFactory")) {
                JMS_CTX_FACTORY = (String) json.get("ctxFactory");
            }
            if (json.containsKey("password")) {
                JMS_PASSWORD = (String) json.get("password");
            }
            if (json.containsKey("username")) {
                JMS_USERNAME = (String) json.get("username");
            }
            if (json.containsKey("topicName")) {
                JMS_TOPIC = (String) json.get("topicName");
            }
        }
        return result;
    }


    public static InitialContext getInitialContext() {
        return initialContext;
    }

    /**
     * Create a connection factory based on JNDI values
     *
     * @return
     */
    public static ConnectionFactory createConnectionFactory(JMSInfo jmsInfo) {
        boolean isFromService = jmsInfo.isFromService();
        String url = jmsInfo.getUrl();
        Hashtable<String, String> properties = new Hashtable<>();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, JMS_CTX_FACTORY);
        properties.put(Context.PROVIDER_URL, url);
        if (JMS_USERNAME != null && JMS_PASSWORD != null) {
            properties.put(Context.SECURITY_PRINCIPAL, JMS_USERNAME);
            properties.put(Context.SECURITY_CREDENTIALS, JMS_PASSWORD);
        }
        initialContext = null;
        try {
            initialContext = new InitialContext(properties);
        } catch (NamingException ne) {
            // FIXME: getting java.lang.ClassNotFoundException: org.apache.activemq.jndi.ActiveMQInitialContextFactory
            //        works in debugging from Eclipse - somehow classpath doesn't work
            //        plugin has the activemq-all reference, as workaround set to false for now
            isFromService = false;
        }

        if (!isFromService) {
            return new ActiveMQConnectionFactory(url);
        }
        else {
            try {
                return (ConnectionFactory) initialContext.lookup(JMS_CONN_FACTORY);
            } catch (NamingException ne) {
                ne.printStackTrace(System.err);
                return null;
            }
        }
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

    public static boolean initializeJms(Project project) {
        String projectID = ExportUtility.getProjectId(project);
        String wsID = ExportUtility.getWorkspace();
        JMSInfo jmsInfo = getJMSInfo(project);
        String url = jmsInfo.getUrl();
        if (url == null) {
            Utils.guilog("[ERROR] Cannot get server url");
            return false;
        }
        if (wsID == null) {
            Utils.guilog("[ERROR] Cannot get server workspace that corresponds to this project branch");
            return false;
        }
        String username = ViewEditUtils.getUsername();
        if (username == null || username.equals("")) {
            Utils.guilog("[ERROR] You must be logged into MMS first");
            return false;
        }
        Connection connection = null;
        Session session = null;
        MessageConsumer consumer = null;
        try {
            ConnectionFactory connectionFactory = createConnectionFactory(jmsInfo);
            String subscriberId = projectID + "-" + wsID + "-" + username; // weblogic can't have '/' in id
            connection = connectionFactory.createConnection();
            connection.setClientID(subscriberId);// + (new Date()).toString());
            session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            // weblogic createTopic doesn't work if it already exists, unlike activemq
            Topic topic = null;
            try {
                if (initialContext != null) {
                    topic = (Topic) initialContext.lookup(JMS_TOPIC);
                }
            } catch (NameNotFoundException nnfe) {
                // do nothing (just means topic hasnt been created yet
            } finally {
                if (topic == null) {
                    topic = session.createTopic(JMS_TOPIC);
                }
            }
            String messageSelector = constructSelectorString(projectID, wsID);
            consumer = session.createDurableSubscriber(topic, subscriberId, messageSelector, true);
            connection.start();
        } catch (Exception e) {
            e.printStackTrace();
            Utils.guilog("[ERROR] MMS Message Queue initialization failed: " + e.getMessage());
            return false;
        } finally {
            try {
                if (consumer != null) {
                    consumer.close();
                }
                if (session != null) {
                    session.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (JMSException e) {
            }
        }
        return true;
    }

    public static class JMSInfo {
        private final String url;
        private final boolean isFromService;

        public JMSInfo(String url, boolean isFromService) {
            this.url = url;
            this.isFromService = isFromService;
        }

        public String getUrl() {
            return url;
        }

        public boolean isFromService() {
            return isFromService;
        }
    }

    // TODO Figure out how these need to be post-filtered based on lastDeltaTimestamp
    @SuppressWarnings("unchecked")
    public static List<String> getJMSTextMessages(Project project, boolean shouldAcknowledge) {
        String projectID = ExportUtility.getProjectId(project);
        String wsID = ExportUtility.getWorkspace();
        if (wsID == null) {
            Utils.guilog("[ERROR] Cannot get server workspace that corresponds to this project branch.");
            return null;
        }
        JMSUtils.JMSInfo jmsInfo = JMSUtils.getJMSInfo(Application.getInstance().getProject());
        String url = jmsInfo.getUrl();
        if (url == null) {
            Utils.guilog("[ERROR] Cannot get server URL.");
            return null;
        }
        String username = ViewEditUtils.getUsername();
        if (username == null || username.isEmpty()) {
            Utils.guilog("[ERROR] You must be logged into MMS first.");
            return null;
        }
        ConnectionFactory connectionFactory = JMSUtils.createConnectionFactory(jmsInfo);
        if (connectionFactory == null) {
            Utils.guilog("[ERROR] Failed to build connection factory.");
            return null;
        }
        String subscriberId = projectID + "-" + wsID + "-" + username; // weblogic can't have '/' in id
        Connection connection = null;
        Session session = null;
        MessageConsumer consumer = null;
        try {
            connection = connectionFactory.createConnection();
            connection.setClientID(subscriberId);// + (new Date()).toString());
            session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            // weblogic createTopic doesn't work if it already exists, unlike activemq
            Topic topic = null;
            try {
                if (JMSUtils.getInitialContext() != null) {
                    topic = (Topic) JMSUtils.getInitialContext().lookup(JMSUtils.JMS_TOPIC);
                }
            } catch (NameNotFoundException ignored) {
                // do nothing (just means topic hasnt been created yet
            } finally {
                if (topic == null) {
                    try {
                        topic = session.createTopic(JMSUtils.JMS_TOPIC);
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            }
            String messageSelector = JMSUtils.constructSelectorString(projectID, wsID);
            consumer = session.createDurableSubscriber(topic, subscriberId, messageSelector, true);
            connection.start();
            boolean print = MDKOptionsGroup.getMDKOptions().isLogJson();
            List<String> textMessages = new ArrayList<>();
            Message message;
            while ((message = consumer.receive(10000)) != null) {
                if (shouldAcknowledge) {
                    try {
                        message.acknowledge();
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
                if (!(message instanceof TextMessage)) {
                    continue;
                }
                TextMessage textMessage = (TextMessage) message;
                if (print) {
                    System.out.println("From JMS: " + textMessage.getText());
                }
                textMessages.add(textMessage.getText());

                /*
                JSONObject ob = (JSONObject) JSONValue.parse(textMessage.getText());
                boolean fromMagicDraw = ob.get("source") != null && ob.get("source").equals("magicdraw");
                JSONObject workspace2 = (JSONObject) ob.get("workspace2");
                if (workspace2 == null) {
                    continue;
                }

                JSONArray updated = (JSONArray) workspace2.get("updatedElements");
                JSONArray added = (JSONArray) workspace2.get("addedElements");
                JSONArray deleted = (JSONArray) workspace2.get("deletedElements");
                JSONArray moved = (JSONArray) workspace2.get("movedElements");

                Changelog<String, Void> changelog = new Changelog<>();
                Map<String, Void> addedChanges = changelog.get(Changelog.ChangeType.CREATED),
                        modifiedChanges = changelog.get(Changelog.ChangeType.UPDATED),
                        deletedChanges = changelog.get(Changelog.ChangeType.DELETED);

                for (Object e : updated) {
                    String id = (String) ((JSONObject) e).get("sysmlid");
                    if (!fromMagicDraw) {
                        modifiedChanges.put(id, null);
                    }
                    deletedChanges.remove(id);
                }
                for (Object e : added) {
                    String id = (String) ((JSONObject) e).get("sysmlid");
                    if (!fromMagicDraw) {
                        addedChanges.put(id, null);
                    }
                    deletedChanges.remove(id);
                }
                for (Object e : moved) {
                    String id = (String) ((JSONObject) e).get("sysmlid");
                    if (!fromMagicDraw) {
                        modifiedChanges.put(id, null);
                    }
                    deletedChanges.remove(id);
                }
                for (Object e : deleted) {
                    String id = (String) ((JSONObject) e).get("sysmlid");
                    if (!fromMagicDraw) {
                        modifiedChanges.put(id, null);
                    }
                    addedChanges.remove(id);
                    modifiedChanges.remove(id);
                }
                */
            }
            return textMessages;
        } catch (JMSException | NamingException e) {
            Utils.printException(e);
            Utils.guilog("[ERROR] Getting changes from MMS failed. Someone else may already be connected. Please try again later (or check your internet connection). If the error persists, please submit a JIRA on https://cae-jira.jpl.nasa.gov/projects/SSCAES/summary");
            Utils.guilog("[ERROR] Server message: " + e.getMessage());
            return null;
        } finally {
            try {
                if (consumer != null) {
                    consumer.close();
                }
                if (session != null) {
                    session.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }
}
