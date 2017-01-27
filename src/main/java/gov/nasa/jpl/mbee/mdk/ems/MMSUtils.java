package gov.nasa.jpl.mbee.mdk.ems;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.nomagic.ci.persistence.IProject;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.task.ProgressStatus;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.ems.actions.EMSLogoutAction;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.lib.MDUtils;
import gov.nasa.jpl.mbee.mdk.lib.TicketUtils;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import gov.nasa.jpl.mbee.mdk.options.MDKOptionsGroup;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * Created by igomes on 9/26/16.
 * Expanded/refactored by ablack on 10/10/16
 */

// TODO Use URI builder or similar @donbot
public class MMSUtils {

    private static final int CHECK_CANCEL_DELAY = 100;

    private static String developerUrl = "";

    private static final Pattern CENSORED_PATTERN = Pattern.compile(".*password.*");

    public enum HttpRequestType {
        GET, POST, PUT, DELETE
    }

    public enum ThreadRequestExceptionType {
        IO_EXCEPTION, SERVER_EXCEPTION
    }

    public static ObjectNode getElement(Project project, String elementId, ProgressStatus progressStatus)
            throws IOException, ServerException, URISyntaxException {
        Collection<String> elementIds = new ArrayList<>(1);
        elementIds.add(elementId);
        ObjectNode response = getElementsRecursively(project, elementIds, 0, progressStatus);
        JsonNode value;
        if (((value = response.get("elements")) != null) && value.isArray()
                && (value = ((ArrayNode) value).remove(1)) != null && (value instanceof ObjectNode)) {
            return (ObjectNode) value;
        }
        return response;
    }

    public static ObjectNode getElementRecursively(Project project, String elementId, int depth, ProgressStatus progressStatus)
            throws IOException, ServerException, URISyntaxException {
        Collection<String> elementIds = new ArrayList<>(1);
        elementIds.add(elementId);
        return getElementsRecursively(project, elementIds, depth, progressStatus);
    }

    /**
     *
     * @param elementIds collection of elements to get mms data for
     * @param project project to check
     * @param progressStatus progress status object, can be null
     * @return object node response
     * @throws ServerException
     * @throws IOException
     * @throws URISyntaxException
     */
    public static ObjectNode getElements(Project project, Collection<String> elementIds, ProgressStatus progressStatus)
            throws IOException, ServerException, URISyntaxException {
        return getElementsRecursively(project, elementIds, 0, progressStatus);
    }

    /**
     *
     * @param elementIds collection of elements to get mms data for
     * @param depth depth to recurse through child elements. takes priority over recurse field
     * @param project project to check
     * @param progressStatus progress status object, can be null
     * @return object node response
     * @throws ServerException
     * @throws IOException
     * @throws URISyntaxException
     */
    public static ObjectNode getElementsRecursively(Project project, Collection<String> elementIds, int depth, ProgressStatus progressStatus)
            throws ServerException, IOException, URISyntaxException {
        // build uri
        URIBuilder requestUri = getServiceProjectsRefsElementsUri(project);
        if (requestUri == null) {
            return null;
        }
        if (depth == -1 || depth > 0) {
            requestUri.setParameter("depth", java.lang.Integer.toString(depth));
        }

        // verify and convert elements
        if (elementIds == null || elementIds.isEmpty()) {
            return null;
        }

        // create requests json
        final ObjectNode requests = JacksonUtils.getObjectMapper().createObjectNode();
        // put elements array inside request json, keep reference
        ArrayNode idsArrayNode = requests.putArray("elements");
        for (String id : elementIds) {
            // create json for id strings, add to request array
            ObjectNode element = JacksonUtils.getObjectMapper().createObjectNode();
            element.put(MDKConstants.SYSML_ID_KEY, id);
            idsArrayNode.add(element);
        }

        //do cancellable request if progressStatus exists
        Utils.guilog("[INFO] Searching for " + elementIds.size() + " elements from server...");
        if (progressStatus != null) {
            return sendCancellableMMSRequest(MMSUtils.buildRequest(MMSUtils.HttpRequestType.GET, requestUri, requests), progressStatus);
        }
        return sendMMSRequest(MMSUtils.buildRequest(MMSUtils.HttpRequestType.GET, requestUri, requests));
    }

    /**
     * General purpose method for making http requests for file upload.
     *
     * @param requestUri URI to send the request to. Methods to generate this URI are available in the class.
     * @param sendFile   File to send as an entity/body along with the request
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static HttpRequestBase buildRequest(URIBuilder requestUri, File sendFile)
            throws IOException, URISyntaxException {
        URI requestDest = requestUri.build();
        HttpPost requestUpload = new HttpPost(requestDest);
        MultipartEntityBuilder uploadBuilder = MultipartEntityBuilder.create();
        uploadBuilder.addBinaryBody(
                "file",
                new FileInputStream(sendFile),
                ContentType.APPLICATION_OCTET_STREAM,
                sendFile.getName()
        );
        HttpEntity multiPart = uploadBuilder.build();
        requestUpload.setEntity(multiPart);
        return requestUpload;
    }

    /**
     * General purpose method for making http requests for JSON objects. Type of request is specified in method call.
     *
     * @param type       Type of request, as selected from one of the options in the inner enum.
     * @param requestUri URI to send the request to. Methods to generate this URI are available in the class.
     * @param sendData   Data to send as an entity/body along with the request, if desired. Support for GET and DELETE
     *                   with body is included.
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static HttpRequestBase buildRequest(HttpRequestType type, URIBuilder requestUri, JsonNode sendData)
            throws IOException, URISyntaxException {
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
        request.addHeader("charset", "utf-8");
        if (sendData != null) {
            request.addHeader("Content-Type", "application/json");
            String data = JacksonUtils.getObjectMapper().writeValueAsString(sendData);
            ((HttpEntityEnclosingRequest) request).setEntity(new StringEntity(data, ContentType.APPLICATION_JSON));
        }
        return request;
    }

    /**
     * Convenience / clarity method for making http requests for JSON objects withoout body. Type of request is
     * specified in method call.
     *
     * @param type       Type of request, as selected from one of the options in the inner enum.
     * @param requestUri URI to send the request to. Methods to generate this URI are available in the class.
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static HttpRequestBase buildRequest(HttpRequestType type, URIBuilder requestUri)
            throws IOException, URISyntaxException {
        return buildRequest(type, requestUri, null);
    }

    public static ObjectNode sendMMSRequest(HttpRequestBase request)
            throws IOException, ServerException {
        return sendMMSRequest(request, false);
    }

        /**
         * General purpose method for sending a constructed http request via http client.
         *
         * @param request
         * @return
         * @throws IOException
         * @throws ServerException
         */
    public static ObjectNode sendMMSRequest(HttpRequestBase request, boolean bypassTicketCheck)
            throws IOException, ServerException {
        // if not bypassing ticket check and ticket invalid, attempt to get new login credentials
        if (!bypassTicketCheck && !TicketUtils.isTicketValid()) {
            // if new login credentials fail, logout and terminal jms sync;
            // 403 exception should already be thrown by failed credentials acquisition attempt
            if (!TicketUtils.loginToMMS()) {
                new EMSLogoutAction().logoutAction();
            }
        }
        HttpEntityEnclosingRequest httpEntityEnclosingRequest = null;
        boolean logBody = MDKOptionsGroup.getMDKOptions().isLogJson() && request instanceof HttpEntityEnclosingRequest
                && ((httpEntityEnclosingRequest = (HttpEntityEnclosingRequest) request).getEntity() != null)
                && httpEntityEnclosingRequest.getEntity().isRepeatable();
        System.out.println("MMS Request [" + request.getMethod() + "] " + request.getURI().toString());
        if (logBody) {
            try (InputStream inputStream = httpEntityEnclosingRequest.getEntity().getContent()) {
                String requestBody = IOUtils.toString(inputStream);
                if (CENSORED_PATTERN.matcher(requestBody).find()) {
                    requestBody = "--- Censored ---";
                }
                System.out.println(" - Body:" + requestBody);
            }
        }
        // create client, execute request, parse response, store in thread safe buffer to return as string later
        // client, response, and reader are all auto closed after block
        ObjectNode responseJson = JacksonUtils.getObjectMapper().createObjectNode();
        try (CloseableHttpClient httpclient = HttpClients.createDefault();
                CloseableHttpResponse response = httpclient.execute(request);
                InputStream inputStream = response.getEntity().getContent()) {

            // get data out of the response
            int responseCode = response.getStatusLine().getStatusCode();
            String responseBody = ((inputStream != null) ? IOUtils.toString(inputStream) : null);
            String responseType = ((response.getEntity().getContentType() != null) ? response.getEntity().getContentType().getValue() : "");

            // debug / logging output from response
            System.out.println("MMS Response [" + request.getMethod() + "] " + request.getURI().toString() + " - Code: " + responseCode);
            if (MDKOptionsGroup.getMDKOptions().isLogJson()) {
                System.out.println(" - Body:"  + responseBody);
                Utils.guilog("Server response: " + responseBody);
            }

            if (responseBody != null && !responseBody.isEmpty()) {
                responseJson = JacksonUtils.getObjectMapper().readValue(responseBody, ObjectNode.class);
                // NOTE: not disabling popup messages locally. it's handled by Utils, which will redirect them to the GUILog if disabled
                JsonNode value;
                // display single response message
                if (responseJson != null && (value = responseJson.get("message")) != null && value.isTextual() && !value.asText().isEmpty()) {
                    Utils.guilog("[SERVER MESSAGE] " + value.asText());
                }
                // display multiple response messages
                if (responseJson != null && (value = responseJson.get("messages")) != null && value.isArray()) {
                    ArrayNode msgs = (ArrayNode) value;
                    for (JsonNode msg : msgs) {
                        if (msg != null && (value = msg.get("message")) != null && value.isTextual() && !value.asText().isEmpty()) {
                            Utils.guilog("[SERVER MESSAGE] " + value.asText());
                        }
                    }
                }
            }

            // try to handle response codes at this level, throw them if it makes sense.
            // note that furtherProcessing == true means we will throw a ServerException and NOT return any JSON
            boolean furtherProcessing = false;
            if (responseCode < 200 || responseCode >= 300) {
                // check for single target 404s that still returned properly, and check before warning popup
                furtherProcessing = true;
                if (responseCode >= 500) {
                    Utils.guilog("[ERROR] Operation failed due to server error. Server code: " + responseCode);
                } else if (responseCode == 404 && responseType.equals("application/json;charset=UTF-8")) {
                    // TODO @donbot block this check when we've migrated to bulk calls only
                    // do nothing
                    furtherProcessing = false;
                } else if (responseCode == 404) {
                    // because we're using bulk get targets for operations, this should only happen on an invalid endpoint
                    Application.getInstance().getGUILog().log("[ERROR] Target URL for operation was invalid. Server code: " + responseCode);
                } else if (responseCode == 403) {
                    Utils.guilog("[ERROR] You do not have sufficient permissions to one or more elements in the project to complete this operation. Server code: " + responseCode);
                } else if (responseCode == 401) {
                    Utils.guilog("[ERROR] Authentication is required to utilize MMS functions. Please log in before trying again. Server code: " + responseCode);
                    TicketUtils.clearUsernameAndPassword();
                } else if (responseCode == 400) {
                    // missing username code. display of server message covers informing the user
                    TicketUtils.clearUsernameAndPassword();
                } else {
                    Utils.guilog("[ERROR] Unexpected server response. Server code: " + responseCode);
                }
//                }
            }
            if (furtherProcessing) {
                // big flashing red letters that the action failed, or as close as we're going to get
                Utils.showPopupMessage("Action failed. See notification window for details.");
                throw new ServerException(responseBody, responseCode);
            }
        }
        return responseJson;
    }

    public static ObjectNode sendCancellableMMSRequest(HttpRequestBase request, ProgressStatus progressStatus)
            throws IOException, ServerException {
        return sendCancellableMMSRequest(request, progressStatus, false);
    }

    /**
     * General purpose method for running a cancellable request. Builds a new thread to run the request, and passes
     * any relevant exception information back out via atomic references and generates new exceptions in calling thread
     *
     * @param request
     * @param progressStatus
     * @param bypassTicketCheck
     * @return
     * @throws IOException
     * @throws URISyntaxException
     * @throws ServerException    contains both response code and response body
     */
    public static ObjectNode sendCancellableMMSRequest(HttpRequestBase request, ProgressStatus progressStatus, final boolean bypassTicketCheck)
            throws IOException, ServerException {
        // if not bypassing ticket check and ticket invalid, attempt to get new login credentials
        if (!bypassTicketCheck && !TicketUtils.isTicketValid()) {
            // if new login credentials fail, logout and terminal jms sync;
            // 403 exception should already be thrown by failed credentials acquisition attempt
            if (!TicketUtils.loginToMMS()) {
                new EMSLogoutAction().logoutAction();
            }
        }
        final AtomicReference<ObjectNode> resp = new AtomicReference<>();
        final AtomicReference<Integer> ecode = new AtomicReference<>();
        final AtomicReference<ThreadRequestExceptionType> etype = new AtomicReference<>();
        final AtomicReference<String> emsg = new AtomicReference<>();
        Thread t = new Thread(() -> {
            ObjectNode response = JacksonUtils.getObjectMapper().createObjectNode();
            try {
                response = sendMMSRequest(request, bypassTicketCheck);
                etype.set(null);
                ecode.set(200);
                emsg.set("");
            } catch (ServerException ex) {
                etype.set(ThreadRequestExceptionType.SERVER_EXCEPTION);
                ecode.set(ex.getCode());
                emsg.set(ex.getMessage());
                ex.printStackTrace();
            } catch (IOException e) {
                etype.set(ThreadRequestExceptionType.IO_EXCEPTION);
                emsg.set(e.getMessage());
                e.printStackTrace();
            }
            resp.set(response);
        });
        t.start();
        try {
            t.join(CHECK_CANCEL_DELAY);
            while (t.isAlive()) {
                if (progressStatus != null && progressStatus.isCancel()) {
                    Application.getInstance().getGUILog().log("[INFO] Request to server for elements cancelled.");
                    //clean up thread?
                    return null;
                }
                t.join(CHECK_CANCEL_DELAY);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (etype.get() == ThreadRequestExceptionType.SERVER_EXCEPTION) {
            throw new ServerException(emsg.get(), ecode.get());
        }
        else if (etype.get() == ThreadRequestExceptionType.IO_EXCEPTION) {
            throw new IOException(emsg.get());
        }
        return resp.get();
    }

    /**
     * @param project
     * @return
     * @throws IllegalStateException
     */
    public static String getServerUrl(Project project) throws IllegalStateException {
        String urlString = null;
        if (project == null) {
            throw new IllegalStateException("Project is null.");
        }
        Element primaryModel = project.getPrimaryModel();
        if (primaryModel == null) {
            throw new IllegalStateException("Model is null.");
        }

        if (StereotypesHelper.hasStereotype(primaryModel, "ModelManagementSystem")) {
            urlString = (String) StereotypesHelper.getStereotypePropertyFirst(primaryModel, "ModelManagementSystem", "MMS URL");
        }
        else {
            Utils.showPopupMessage("Your project root element doesn't have ModelManagementSystem Stereotype!");
        }
        if ((urlString == null || urlString.isEmpty())) {
            if (!MDUtils.isDeveloperMode()) {
                Utils.showPopupMessage("Your project root element doesn't have ModelManagementSystem MMS URL stereotype property set!");
            }
            else {
                urlString = JOptionPane.showInputDialog("[DEVELOPER MODE] Enter the server URL:", developerUrl);
                developerUrl = urlString;
            }
        }
        if (urlString == null || urlString.isEmpty()) {
            throw new IllegalStateException("MMS URL is null or empty.");
        }
        return urlString.trim();
    }

    public static String getProjectOrg(Project project)
            throws IOException, URISyntaxException, ServerException {
        URIBuilder uriBuilder = getServiceProjectsUri(project);
        // create requests json
        final ObjectNode requests = JacksonUtils.getObjectMapper().createObjectNode();
        // put elements array inside request json, keep reference
        ArrayNode idsArrayNode = requests.putArray("elements");
        ObjectNode element = JacksonUtils.getObjectMapper().createObjectNode();
        element.put(MDKConstants.SYSML_ID_KEY, project.getID());
        idsArrayNode.add(element);

        ObjectNode response = sendMMSRequest(buildRequest(HttpRequestType.GET, uriBuilder, requests));
        JsonNode arrayNode;
        if (((arrayNode = response.get("elements")) != null) && arrayNode.isArray()) {
            JsonNode value;
            for (JsonNode projectNode : arrayNode) {
                if (((value = projectNode.get(MDKConstants.SYSML_ID_KEY)) != null ) && value.isTextual() && value.asText().equals(project.getID())
                        && ((value = projectNode.get("org")) != null ) && value.isTextual() && !value.asText().isEmpty()) {
                    return value.asText();
                }
            }
        }
        return "";
    }

    /**
     * Method to check if the currently logged in user has permissions to edit the specified site on
     * the specified server.
     *
     * @param project The project containing the mms url to check against.
     * @param site    Site name (sysmlid) of the site you are querying for. If empty or null, will use the site from the
     *                project parameter.
     * @return true if the site lists "editable":"true" for the logged in user, false otherwise
     * @throws ServerException
     */
    // TODO update for orgs / projects instead of site, then remove deprecation
    @Deprecated
    public static boolean isSiteEditable(Project project, String site)
            throws IOException, URISyntaxException, ServerException {
        if (site == null || site.isEmpty()) {
//            site = getSiteName(project);
            site = project.getName();
        }

        // configure request
        //https://cae-ems.jpl.nasa.gov/alfresco/service/refs/master/sites
        URIBuilder requestUri = getServiceProjectsUri(project);
        if (requestUri == null) {
            return false;
        }
        requestUri.setPath(requestUri.getPath() + "/sites");

        // do request
        ObjectNode response;
        try {
            response = sendMMSRequest(buildRequest(HttpRequestType.GET, requestUri));
        } catch (IOException | URISyntaxException | ServerException e) {
            Application.getInstance().getGUILog().log("[ERROR] Unable to query site permissions");
            //TODO @donbot
            e.printStackTrace();
            return false;
        }

        // parse response
        JsonNode arrayNode;
        if ((arrayNode = response.get("sites")) != null && arrayNode instanceof ArrayNode) {
            JsonNode value, boolValue;
            for (JsonNode node : arrayNode) {
                if ((value = node.get(MDKConstants.SYSML_ID_KEY)) != null && value.isTextual() && value.asText().equals(site)
                        && (boolValue = node.get("_editable")) != null && boolValue.isBoolean()) {
                    return boolValue.asBoolean();
                }
            }
        }
        return false;
    }

    /**
     * Returns a URIBuilder object with a path = "/alfresco/service". Used as the base for all of the rest of the
     * URIBuilder generating convenience classes.
     *
     * @param project The project to gather the mms url and site name information from
     * @return URIBuilder
     * @throws URISyntaxException
     */
    public static URIBuilder getServiceUri(Project project) {
        Model primaryModel = project.getModel();
        if (project == null || primaryModel == null) {
            return null;
        }

        String urlString = getServerUrl(project);

        // [scheme:][//host][path][?query][#fragment]
        String uriPath = "/alfresco/service";
        String uriTicket = TicketUtils.getTicket();

        URIBuilder uri;
        try {
            uri = new URIBuilder(urlString);
            uri.setPath(uriPath);
            if (!uriTicket.isEmpty()) {
                uri.setParameter("alf_ticket", uriTicket);
            }
            return uri;
        } catch (URISyntaxException e) {
            Application.getInstance().getGUILog().log("[ERROR] Unexpected error in generation of MMS URL for " +
                    "project. Reason: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Returns a URIBuilder object with a path = "/alfresco/service/orgs"
     *
     * @param project The project to gather the mms url and site name information from
     * @return URIBuilder
     */
    public static URIBuilder getServiceOrgsUri(Project project) {
        URIBuilder siteUri = getServiceUri(project);
        if (siteUri == null) {
            return null;
        }
        siteUri.setPath(siteUri.getPath() + "/orgs");
        return siteUri;
    }

    /**
     * Returns a URIBuilder object with a path = "/alfresco/service/projects/{$PROJECT_ID}"
     *
     * @param project The project to gather the mms url and site name information from
     * @return URIBuilder
     */
    public static URIBuilder getServiceProjectsUri (Project project) {
        URIBuilder projectUri = getServiceUri(project);
        if (projectUri == null) {
            return null;
        }
        String projectId = project.getPrimaryProject().getProjectID();
        projectUri.setPath(projectUri.getPath() + "/projects/" + projectId);
        return projectUri;
    }

    /**
     * Returns a URIBuilder object with a path = "/alfresco/service/projects/{$PROJECT_ID}/refs/{$WORKSPACE_ID}"
     *
     * @param project The project to gather the mms url and site name information from
     * @return URIBuilder
     */
    public static URIBuilder getServiceProjectsRefsUri (Project project) {
        URIBuilder workspaceUri = getServiceProjectsUri(project);
        if (workspaceUri == null) {
            return null;
        }
        String workspace = MDUtils.getWorkspace(project);
        workspaceUri.setPath(workspaceUri.getPath() + "/refs/" + workspace);
        return workspaceUri;
    }

    /**
     * Returns a URIBuilder object with a path = "/alfresco/service/projects/{$PROJECT_ID}/refs/{$WORKSPACE_ID}/elements/${ELEMENT_ID}"
     * if element is not null
     *
     * @param project The project to gather the mms url and site name information from
     * @return URIBuilder
     *
     */
    public static URIBuilder getServiceProjectsRefsElementsUri(Project project) {
        URIBuilder elementUri = getServiceProjectsRefsUri(project);
        if (elementUri == null) {
            return null;
        }
        elementUri.setPath(elementUri.getPath() + "/elements");
        return elementUri;
    }

    public static String getDefaultSiteName(IProject iProject) {
        String name = iProject.getName().trim().replaceAll("\\W+", "-");
        if (name.endsWith("-")) {
            name = name.substring(0, name.length() - 1);
        }
        return name;
    }

    public static ObjectNode getProjectObjectNode(Project project) {
        String descriptor = (project.isRemote() ? project.getPrimaryProject().getProjectDescriptor().getLocationUri().toString() : "local");
        return getProjectObjectNode(project.getPrimaryProject().getName(), project.getPrimaryProject().getProjectID(), descriptor);
    }

    public static ObjectNode getProjectObjectNode(IProject project) {
        return getProjectObjectNode(project.getName(), project.getProjectID(), null);
    }

    private static ObjectNode getProjectObjectNode(String name, String projId, String descId) {

        ObjectNode projectObjectNode = JacksonUtils.getObjectMapper().createObjectNode();
        projectObjectNode.put(MDKConstants.TYPE_KEY, "Project");
        projectObjectNode.put(MDKConstants.SYSML_ID_KEY, projId);
        if (name != null && !name.isEmpty()) {
            projectObjectNode.put(MDKConstants.NAME_KEY, name);
        }
        if (descId != null && !descId.isEmpty()) {
            projectObjectNode.put(MDKConstants.DESCRIPTOR_ID, descId);
        }
        return projectObjectNode;
    }

}

