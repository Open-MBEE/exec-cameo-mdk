package gov.nasa.jpl.mbee.mdk.mms.jms;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.http.client.utils.URIBuilder;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Hashtable;

/**
 * Created by igomes on 6/29/16.
 */
public class JMSUtils {

    private static final int JMS_PORT = 61616;
    private static final String JMS_S = "tcp";

    public static final String MSG_SELECTOR_PROJECT_ID = "projectId",
            MSG_SELECTOR_REF_ID = "refId";

    // Members to look up MMS using JNDI
    // TODO: If any other context factories are used, need to add those JARs into class path (e.g., for weblogic)
    public static String JMS_CTX_FACTORY = "org.apache.activemq.jndi.ActiveMQInitialContextFactory",
            JMS_CONN_FACTORY = "ConnectionFactory",
            JMS_USERNAME,
            JMS_PASSWORD,
            JMS_TOPIC = "master";

    private static InitialContext initialContext = null;

    /**
     * Gets MMS JNDI connection details from the MMS server
     *
     * @return JSONObject of the connection details
     * @throws JsonMappingException
     * @throws IOException
     * @throws JsonParseException
     */
    public static ObjectNode getJmsConnectionDetails(Project project)
            throws IOException, ServerException, URISyntaxException {
        ObjectNode responseJson = JacksonUtils.getObjectMapper().createObjectNode();
        URIBuilder requestUri = MMSUtils.getServiceUri(project);
        if (requestUri == null) {
            return responseJson;
        }
        requestUri.setPath(requestUri.getPath() + "/connection/jms");
        MMSUtils.sendMMSRequest(project, MMSUtils.buildRequest(MMSUtils.HttpRequestType.GET, requestUri), null, responseJson);
//        File responseFile = MMSUtils.sendMMSRequest(project, MMSUtils.buildRequest(MMSUtils.HttpRequestType.GET, requestUri));
//        try (JsonParser jsonParser = JacksonUtils.getJsonFactory().createParser(responseFile)) {
//           return JacksonUtils.parseJsonObject(jsonParser);
//        }
        return responseJson;
    }

    // Varies by current project
    public static JMSInfo getJMSInfo(Project project) throws ServerException {
        ObjectNode jmsJson = null;
        try {
            jmsJson = getJmsConnectionDetails(project);
        } catch (IOException e) {
            Application.getInstance().getGUILog().log("[ERROR]: Unable to acquire JMS Connection information.");
            e.printStackTrace();
        } catch (URISyntaxException e) {
            Application.getInstance().getGUILog().log("[ERROR]: Unexpected error occurred when trying to build MMS URL. Reason: " + e.getMessage());
            e.printStackTrace();
        }
        String url = ingestJson(jmsJson);
        boolean isFromService = (url != null);
        if (url == null) {
            url = MMSUtils.getServerUrl(project);
            try {
                URIBuilder uri = new URIBuilder(url);
                uri.setPort(JMS_PORT);
                uri.setScheme(JMS_S);
                uri.setPath("");
                url = uri.build().toString();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return new JMSInfo(url, isFromService);
    }

    /**
     * Ingests JSON data generated from MMS server and populates JNDI members
     *
     * @return URL string of connector
     */
    protected static String ingestJson(ObjectNode jsonInput) {
        if (jsonInput == null) {
            return null;
        }
        ArrayNode conns = null;
        JsonNode valueNode;
        if ((valueNode = jsonInput.get("connections")) != null && valueNode.isArray()) {
            conns = (ArrayNode) valueNode;
        }
        ObjectNode json = null;
        if (conns != null) {
            // just grab first connection
            for (int ii = 0; ii < conns.size(); ii++) {
                json = (ObjectNode) conns.get(ii);
                JsonNode value;
                if ((value = json.get("eventType")) != null && value.isTextual() && value.asText().equals("DELTA")) {
                    break;
                }
            }
        }
        else {
            json = jsonInput;
        }
        String result = null;
        if (json != null) {
            if ((valueNode = json.get("uri")) != null && valueNode.isTextual()) {
                result = valueNode.asText();
            }
            if ((valueNode = json.get("connFactory")) != null && valueNode.isTextual()) {
                JMS_CONN_FACTORY = valueNode.asText();
            }
            if ((valueNode = json.get("ctxFactory")) != null && valueNode.isTextual()) {
                JMS_CTX_FACTORY = valueNode.asText();
            }
            if ((valueNode = json.get("password")) != null && valueNode.isTextual()) {
                JMS_PASSWORD = valueNode.asText();
            }
            if ((valueNode = json.get("username")) != null && valueNode.isTextual()) {
                JMS_USERNAME = valueNode.asText();
            }
            if ((valueNode = json.get("topicName")) != null && valueNode.isTextual()) {
                JMS_TOPIC = valueNode.asText();
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

        //selectorBuilder.append("(").append(MSG_SELECTOR_REF_ID).append("='").append(workspaceID).append("')");

        selectorBuilder.append("(").append(MSG_SELECTOR_PROJECT_ID).append(" = '").append(projectID).append("')")
                .append(" AND ").append("((").append(MSG_SELECTOR_REF_ID).append(" = '").append(workspaceID).append("') OR (").append(MSG_SELECTOR_REF_ID).append(" = '").append(workspaceID).append("_mdk").append("'))");

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
