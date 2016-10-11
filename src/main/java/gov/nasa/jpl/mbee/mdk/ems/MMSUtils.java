package gov.nasa.jpl.mbee.mdk.ems;

import com.fasterxml.jackson.databind.JsonNode;
import com.nomagic.ci.persistence.IProject;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.task.ProgressStatus;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.ems.ExportUtility;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.lib.MDUtils;
import gov.nasa.jpl.mbee.mdk.lib.TicketUtils;
import gov.nasa.jpl.mbee.mdk.lib.Utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import gov.nasa.jpl.mbee.mdk.options.MDKOptionsGroup;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.server.Server;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Created by igomes on 9/26/16.
 * Expanded / refactored by ablack on 10/10/16
 */

// TODO Use URI builder or similar @donbot
public class MMSUtils {
    
    private static String developerUrl = "";
    private static String developerSite = "";

    public enum HttpRequestType {
        GET, POST, PUT, DELETE
    }
    
    public static ObjectNode getElement(Element element, Project project) throws IOException {
        return getElementById(Converters.getElementToIdConverter().apply(element), project);
    }

    public static ObjectNode getElementById(String id, Project project) throws IOException {
        if (id == null) {
            return null;
        }
        try {
            URIBuilder requestUri = getElementsUri(project);
            id = id.replace(".", "%2E");
            requestUri.setPath(requestUri.getPath() + "/" + id);

            ObjectNode response = sendMMSRequest(HttpRequestType.GET, requestUri, null);

            JsonNode value;
            if ((value = response.get("elements")) instanceof ArrayNode
                    && (value = ((ArrayNode)value).get(0)) instanceof ObjectNode) {
                return (ObjectNode) value;
            }
            return null;
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ObjectNode getElements(Collection<Element> elements, Project project, ProgressStatus ps)
            throws ServerException, IOException {
        return getElementsById(elements.stream().map(Converters.getElementToIdConverter())
                .filter(id -> id != null).collect(Collectors.toList()), project, ps);
    }

    public static ObjectNode getElementsById(Collection<String> ids, Project project, ProgressStatus ps)
            throws ServerException, IOException {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        
        // create requests json
        final ObjectNode requests = JacksonUtils.getObjectMapper().createObjectNode();
        // put elements array inside request json, keep reference
        ArrayNode idsArrayNode = requests.putArray("elements");
        for (String id : ids) {
            // create json for id strings, add to request array
            ObjectNode element = JacksonUtils.getObjectMapper().createObjectNode();
            element.put(MDKConstants.SYSML_ID_KEY, id);
            idsArrayNode.add(element);
        }

        try {
            URIBuilder requestUri = getElementsUri(project);


//        final String url = ExportUtility.getUrlWithWorkspace() + "/elements";
//        final String jsonRequest = JacksonUtils.getObjectMapper().writeValueAsString(requests);
            Utils.guilog("[INFO] Searching for " + ids.size() + " elements from server...");

            final AtomicReference<ObjectNode> resp = new AtomicReference<>();
            final AtomicReference<Integer> code = new AtomicReference<>();
            final AtomicReference<Boolean> errors = new AtomicReference<>();
            Thread t = new Thread(() -> {
                ObjectNode response;
                try {
                    response = sendMMSRequest(HttpRequestType.GET, requestUri, requests);
                    resp.set(response);
                    code.set(200);
                } catch (ServerException ex) {
                    code.set(ex.getCode());
                    if (ex.getCode() != 404) {
                        res.set(ex.getResponse());
                    }
                } catch (IOException e) {
                    errors.set(true);
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            });
            t.start();
            try {
                t.join(10000);
                while (t.isAlive()) {
                    if (ps.isCancel()) {
                        //clean up thread?
                        Utils.guilog("[INFO] Search for elements cancelled.");
                        code.set(500);
                        break;
                    }
                    t.join(10000);
                }
            } catch (Exception ignored) {
            }

            ObjectNode response = resp.get();
            if (code.get() != 404 && code.get() != 200) {
                //TODO error handling
                throw new ServerException(resp.get().asText(), code.get());
            }
            if (errors.get()) {
                //TODO error handling
                throw new IOException("");
            }
            Utils.guilog("[INFO] Finished getting elements.");
            return response;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convenience method for requests without body. Not difficult to remove, but the code is easier to read if you
     * don't have to pass 'null' when calling it
     *
     * @param type Type of request, as selected from one of the options in the inner enum.
     * @param requestUri URI to send the request to. Methods to generate this URI are available in the class.
     * @return response as JSON
     */
    public static ObjectNode sendMMSRequest(HttpRequestType type, URIBuilder requestUri)
            throws IOException, URISyntaxException, ServerException {
        return sendMMSRequest(type, requestUri, null);
    }

    /**
     * General purpose method for making http requests. Type of request is specified in method call.
     *
     * @param type Type of request, as selected from one of the options in the inner enum.
     * @param requestUri URI to send the request to. Methods to generate this URI are available in the class.
     * @param sendData Data to send as an entity/body along with the request, if desired. Support for GET and DELETE
     *                 with body is included.
     * @return
     * @throws IOException
     * @throws URISyntaxException
     * @throws ServerException
     */
    public static ObjectNode sendMMSRequest(HttpRequestType type, URIBuilder requestUri, ObjectNode sendData)
            throws IOException, URISyntaxException, ServerException {

        // build specified request type
        // assume that any request can have a body, and just build the appropriate one
        URI requestDest = requestUri.build();
        HttpRequestBase request = null;
        switch (type) {
            case DELETE:
                request = new HttpDeleteWithBody(requestDest);
                break;
            case GET:
                request = new HttpGetWithBody(requestDest);
                break;
            case POST:
                request = new HttpPost(requestDest);
                break;
            case PUT:
                request = new HttpPut(requestDest);
                break;
        }
        request.addHeader("Content-Type", "application/json");
        request.addHeader("charset", "utf-8");
        if (sendData != null) {
            String data = JacksonUtils.getObjectMapper().writeValueAsString(sendData);
            ((HttpPost)request).setEntity(new StringEntity(data, ContentType.APPLICATION_JSON));
        }

        // create client, execute request, parse response, store in thread safe buffer to return as string later
        // client, response, and reader are all auto closed after block
        ObjectNode responseJson;
        try (CloseableHttpClient httpclient = HttpClients.createDefault();
             CloseableHttpResponse response = httpclient.execute(request);
        ){
            responseJson = JacksonUtils.getObjectMapper().readValue(response.getEntity().getContent(), ObjectNode.class);
            int errorCode = response.getStatusLine().getStatusCode();
            if (errorCode != 200) {
                //TODO error processing
            }
        } catch (IOException e) {
            throw new IOException("[ERROR] Unable to parse server response.");
        }
        return responseJson;
    }

    /**
     * Method to check if the currently logged in user has permissions to edit the specified site on
     * the specified server.
     *
     * @param project The project containing the mms url to check against.
     * @param site Site name (sysmlid) of the site you are querying for. If empty or null, will use the site from the
     *             project parameter.
     * @return true if the site lists "editable":"true" for the logged in user, false otherwise
     * @throws ServerException
     */
    public static boolean isUserSiteEditor(Project project, String site)
            throws IOException, URISyntaxException, ServerException {
        boolean print = MDKOptionsGroup.getMDKOptions().isLogJson();
        if (site == null || site.equals("")) {
            site = getSiteName(project);
        }

        // configure request
        //https://cae-ems.jpl.nasa.gov/alfresco/service/workspaces/master/sites
        URIBuilder requestUri = getWorkspaceUri(project);
        requestUri.setPath(requestUri.getPath() + "/sites");

        // do request
        ObjectNode response = JacksonUtils.getObjectMapper().createObjectNode();
        try {
            response = sendMMSRequest(HttpRequestType.GET, requestUri);
        } catch (IOException e) {
            //TODO
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ServerException e) {
            e.printStackTrace();
        }

        // parse response
        JsonNode arrayNode;
        if ((arrayNode = response.get("sites")) != null && arrayNode instanceof ArrayNode) {
            JsonNode value;
            for (JsonNode node : (ArrayNode)arrayNode) {
                if ((value = node.get(MDKConstants.SYSML_ID_KEY)) != null
                        && value.isTextual() && value.asText().equals(site)
                        && (value = node.get("editable")) != null && value.isBoolean()) {
                    return value.asBoolean();
                }
            }
        }
        return false;
    }


    /*
    public static String sendGet(URI requestUri) {
        String response = "";
        BufferedReader in = null;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) requestUri.toURL().openConnection();
            connection.setRequestMethod("GET");

            connection.setRequestProperty("Authorization", "Basic " + TicketUtils.getAuthStringEnc());
//            connection.setRequestProperty("Content-Type", "application/json"); 
//            connection.setRequestProperty("charset", "utf-8");
            
            int responseCode = connection.getResponseCode();
           //printErrors(responseCode);
            
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer responseBuffer = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                responseBuffer.append(inputLine);
            }
            in.close();
            response = responseBuffer.toString();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {}
            }
        }
        return response;
    }
    
    public static String sendPost(URIBuilder uriBuild, String postData) {
        String response = "";
        URI requestUri = null;
        try {
            requestUri = uriBuild.build();
        } catch (URISyntaxException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        BufferedReader in = null;
        OutputStream os = null;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) requestUri.toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Basic " + TicketUtils.getAuthStringEnc());
            connection.setRequestProperty("Content-Type", "application/json"); 
            connection.setRequestProperty("charset", "utf-8");
            
            os = connection.getOutputStream();
            os.write(postData.getBytes());
            os.flush();
            
            int responseCode = connection.getResponseCode();
           //printErrors(responseCode);
            
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuffer responseBuffer = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                responseBuffer.append(inputLine);
            }
            in.close();
            response = responseBuffer.toString();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {}
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ignored) {}
            }
        }
        return response;
    }
    */

    /**
     * Returns a URIBuilder object with a path = "/alfresco/service". Used as the base for all of the rest of the
     * URIBuilder generating convenience classes.
     *
     * @param project The project to gather the mms url and site name information from
     * @return URIBuilder
     * @throws URISyntaxException
     */
    public static URIBuilder getBaseUri(Project project) throws URISyntaxException {
        Model primaryModel = project.getModel();
        if (project == null || primaryModel == null) {
            return null;
        }
        
        String urlString = getServerUrl(project);
        
        // [scheme:][//host][path][?query][#fragment]
        String uriPath = "/alfresco/service";
        String uriTicket = TicketUtils.getTicket(project);

        URIBuilder uri = new URIBuilder(urlString);
        uri.setPath(uriPath)
            .setParameter("alf_ticket", uriTicket);
        return uri;
    }

    /**
     * Returns a URIBuilder object with a path = "/alfresco/service/workspaces/{$WORKSPACE}".
     *
     * @param project The project to gather the mms url and site name information from
     * @return URIBuilder
     * @throws URISyntaxException
     */
    public static URIBuilder getWorkspaceUri(Project project) throws URISyntaxException {
        URIBuilder workspaceUri = getBaseUri(project);
        //TODO add support for non-master workspaces
//        String workspace = getSiteName(project);
        String workspace = "master";
        workspaceUri.setPath(workspaceUri.getPath() + "/workspaces/" + workspace);
        return workspaceUri;
    }

    /**
     * Returns a URIBuilder object with a path = "/alfresco/service/workspaces/{$WORKSPACE}/sites/{$SITE}".
     *
     * @param project The project to gather the mms url and site name information from
     * @return URIBuilder
     * @throws URISyntaxException
     */
    public static URIBuilder getSitesUri(Project project) throws URISyntaxException {
        URIBuilder siteUri = getWorkspaceUri(project);
        String sites = getSiteName(project);
        siteUri.setPath(siteUri.getPath() + "/sites/" + sites);
        return siteUri;
    }

    /**
     *
     * Returns a URIBuilder object with a path = "/alfresco/service/workspaces/{$WORKSPACE}/sites/{$SITE}/projects/{$PROJECTID}".
     *
     * @param project The project to gather the mms url and site name information from
     * @return URIBuilder
     * @throws URISyntaxException
     */
    public static URIBuilder getProjectUri(Project project) throws URISyntaxException {
        URIBuilder projectUri = getSitesUri(project);
        String projectId = project.getPrimaryProject().getProjectID();
        projectUri.setPath(projectUri.getPath() + "/projects/" + projectId);
        return projectUri;
    }

    /**
     * Returns a URIBuilder object with a path = "/alfresco/service/workspaces/{$WORKSPACE}/sites/{$SITE}/projects/{$PROJECTID}/elements".
     *
     * @param project The project to gather the mms url and site name information from
     * @return URIBuilder
     * @throws URISyntaxException
     */
    public static URIBuilder getElementsUri(Project project) throws URISyntaxException {
        URIBuilder elementsUri = getSitesUri(project);
        elementsUri.setPath(elementsUri.getPath() + "/elements");
        return elementsUri;
    }

    /**
     *
     * @param project
     * @return
     * @throws IllegalStateException
     */
    public static String getServerUrl(Project project) throws IllegalStateException {
        String urlString = null;
        if (project == null) {
            throw new IllegalStateException("Project is null.");
        }
        Element primaryModel = project.getModel();
        if (primaryModel == null) {
            throw new IllegalStateException("Model is null.");
        }

        if (StereotypesHelper.hasStereotype(primaryModel, "ModelManagementSystem")) {
            urlString = (String) StereotypesHelper.getStereotypePropertyFirst(primaryModel, "ModelManagementSystem", "MMS URL");
        }
        else {
            Utils.showPopupMessage("Your project root element doesn't have ModelManagementSystem Stereotype!");
        }
        if ((urlString == null || urlString.equals(""))) {
            if (!MDUtils.isDeveloperMode()) {
                Utils.showPopupMessage("Your project root element doesn't have ModelManagementSystem MMS URL stereotype property set!");
            } else {
                urlString = JOptionPane.showInputDialog("[DEVELOPER MODE] Enter the server URL:", developerUrl);
                developerUrl = urlString;
            }
        }
        if (urlString == null || urlString.equals("")) {
            throw new IllegalStateException("MMS URL is null or empty.");
        }
        return urlString;
    }
    
    public static String getSiteName(Project project) {
        String siteString = null;
        if (project == null) {
            throw new IllegalStateException("Project is null.");
        }
        Element primaryModel = project.getModel();
        if (primaryModel == null) {
            throw new IllegalStateException("Model is null.");
        }

        if (StereotypesHelper.hasStereotype(primaryModel, "ModelManagementSystem")) {
            siteString = (String) StereotypesHelper.getStereotypePropertyFirst(primaryModel, "ModelManagementSystem", "MMS Site");
        }
        else {
            Utils.showPopupMessage("Your project root element doesn't have ModelManagementSystem Stereotype!");
        }
        if ((siteString == null || siteString.equals(""))) {
            if (!MDUtils.isDeveloperMode()) {
                Utils.showPopupMessage("Your project root element doesn't have ModelManagementSystem MMS Site stereotype property set!");
            } else {
                siteString = JOptionPane.showInputDialog("[DEVELOPER MODE] Enter the site:", developerSite);
                developerSite = siteString;
            }
        }
        if (siteString == null || siteString.equals("")) {
            throw new IllegalStateException("MMS Site is null or empty.");
        }
        return siteString;
    }
    


}

