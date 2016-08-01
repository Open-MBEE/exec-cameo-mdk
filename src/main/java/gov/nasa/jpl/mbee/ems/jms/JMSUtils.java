package gov.nasa.jpl.mbee.ems.jms;

import com.nomagic.magicdraw.core.Project;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.ServerException;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.jms.ConnectionFactory;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;

/**
 * Created by igomes on 6/29/16.
 */
public class JMSUtils {
    public static final String MSG_SELECTOR_PROJECT_ID = "projectId",
            MSG_SELECTOR_WORKSPACE_ID = "workspace";

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
    public static JMSInfo getJMSInfo(Project project) throws ServerException {
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

        //selectorBuilder.append("(").append(MSG_SELECTOR_WORKSPACE_ID).append("='").append(workspaceID).append("')");

        selectorBuilder.append("(").append(MSG_SELECTOR_PROJECT_ID).append(" = '").append(projectID).append("')")
                .append(" AND ").append("((").append(MSG_SELECTOR_WORKSPACE_ID).append(" = '").append(workspaceID).append("') OR (").append(MSG_SELECTOR_WORKSPACE_ID).append(" = '").append(workspaceID).append("_mdk").append("'))");

        String outputMsgSelector = selectorBuilder.toString();
        selectorBuilder.delete(0, selectorBuilder.length());

        return outputMsgSelector;
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
}
