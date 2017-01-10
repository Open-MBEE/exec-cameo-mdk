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
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
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
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by igomes on 9/26/16.
 * Expanded/refactored by ablack on 10/10/16
 */

// TODO Use URI builder or similar @donbot
public class MMSUtils {

    private static final int CHECK_CANCEL_DELAY = 100;

    private static String developerUrl = "";
    private static String developerSite = "";

    private static final Pattern CENSORED_PATTERN = Pattern.compile(".*password.*");

    public enum HttpRequestType {
        GET, POST, PUT, DELETE
    }

    public enum ThreadRequestExceptionType {
        IO_EXCEPTION, SERVER_EXCEPTION
    }

    public static ObjectNode getElement(Element element, Project project)
            throws IOException, ServerException, URISyntaxException {
        return getElementById(Converters.getElementToIdConverter().apply(element), project);
    }

    public static ObjectNode getElementById(String id, Project project)
            throws IOException, ServerException, URISyntaxException {
        // build request
        if (id == null) {
            return null;
        }
        URIBuilder requestUri = getServiceWorkspacesSitesElementsUri(project);
        id = id.replace(".", "%2E");
        requestUri.setPath(requestUri.getPath() + "/" + id);

        // do request
        ObjectNode response = JacksonUtils.getObjectMapper().createObjectNode();
        try {
            response = sendMMSRequest(buildRequest(HttpRequestType.GET, requestUri));
        } catch (ServerException e) {
            if (e.getCode() != 404) {
                throw e;
            }
        }
        return response;

        // parse response
//        JsonNode elementsJsonNode;
//        if ((elementsJsonNode = response.get("elements")) != null && elementsJsonNode.isArray() && elementsJsonNode.size() > 0 && (elementsJsonNode = elementsJsonNode.get(0)).isObject()) {
//            return (ObjectNode) elementsJsonNode;
//        }
//        return null;
    }

    public static ObjectNode getElements(Collection<Element> elements, Project project, ProgressStatus ps)
            throws IOException, ServerException, URISyntaxException {
        return getElementsById(elements.stream().map(Converters.getElementToIdConverter())
                .filter(id -> id != null).collect(Collectors.toList()), project, ps);
    }

    public static ObjectNode getElementsById(Collection<String> ids, Project project, ProgressStatus progressStatus)
            throws IOException, ServerException, URISyntaxException {
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

        URIBuilder requestUri = getServiceWorkspacesElementsUri(project);
//        URIBuilder requestUri = getServiceWorkspacesSitesElementsUri(project);
        if (requestUri == null) {
            return null;
        }

        //do cancellable request
        Utils.guilog("[INFO] Searching for " + ids.size() + " elements from server...");
        ObjectNode response = JacksonUtils.getObjectMapper().createObjectNode();
        try {
            response = sendCancellableMMSRequest(buildRequest(HttpRequestType.GET, requestUri, requests), progressStatus);
        } catch (ServerException e) {
            if (e.getCode() != 404) {
                throw e;
            }
        }
        return response;
    }

    /**
     *
     * @param project
     * @param elementId
     * @param recurse
     * @param depth
     * @param progressStatus
     * @return
     * @throws ServerException
     * @throws IOException
     * @throws URISyntaxException
     */
    public static ObjectNode getServerElementsRecursively(Project project, String elementId, boolean recurse, int depth,
                                                          ProgressStatus progressStatus)
            throws ServerException, IOException, URISyntaxException {
        URIBuilder requestUri = getServiceWorkspacesSitesElementsUri(project);
        if (requestUri == null) {
            return null;
        }
        requestUri = MMSUtils.getServiceWorkspacesUri(project);
        requestUri.setPath(requestUri.getPath() + "/elements/" + elementId);
        if (depth > 0) {
            requestUri.setParameter("depth", java.lang.Integer.toString(depth));
        }
        else {
            requestUri.setParameter("recurse", java.lang.Boolean.toString(recurse));
        }

        // do request in cancellable thread
        ObjectNode response = JacksonUtils.getObjectMapper().createObjectNode();
        try {
            response = sendCancellableMMSRequest(buildRequest(HttpRequestType.GET, requestUri, null), progressStatus);
        } catch (ServerException e) {
            if (e.getCode() != 404) {
                throw e;
            }
        }
        return response;
    }

    /**
     *
     * @param project
     * @param element
     * @param recurse
     * @param depth
     * @param progressStatus
     * @return
     * @throws ServerException
     * @throws IOException
     * @throws URISyntaxException
     */
    // TODO Add both ?recurse and element list gets @donbot
    public static ObjectNode getServerElementsRecursively(Project project, Element element, boolean recurse, int depth,
                                                          ProgressStatus progressStatus)
            throws ServerException, IOException, URISyntaxException {
        // configure request
        String id = Converters.getElementToIdConverter().apply(element);
        return getServerElementsRecursively(project, id, recurse, depth, progressStatus);
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
        if (sendData != null && request instanceof HttpEntityEnclosingRequest) {
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

    /**
     * General purpose method for sending a constructed http request via http client.
     *
     * @param request
     * @return
     * @throws IOException
     * @throws ServerException
     */
    public static ObjectNode sendMMSRequest(HttpRequestBase request)
            throws IOException, ServerException {
//        new RuntimeException("trace").printStackTrace();
        HttpEntityEnclosingRequest httpEntityEnclosingRequest = null;
        boolean logBody = MDKOptionsGroup.getMDKOptions().isLogJson() && request instanceof HttpEntityEnclosingRequest && (httpEntityEnclosingRequest = (HttpEntityEnclosingRequest) request).getEntity() != null && httpEntityEnclosingRequest.getEntity().isRepeatable();
        System.out.println("MMS Request [" + request.getMethod() + "] " + request.getURI().toString() + (logBody ? " - Body:" : ""));
        if (logBody) {
            try (InputStream inputStream = httpEntityEnclosingRequest.getEntity().getContent()) {
                String requestBody = IOUtils.toString(inputStream);
                if (CENSORED_PATTERN.matcher(requestBody).find()) {
                    requestBody = "--- Censored ---";
                }
                System.out.println(requestBody);
            }
        }
        // create client, execute request, parse response, store in thread safe buffer to return as string later
        // client, response, and reader are all auto closed after block
        ObjectNode responseJson;
        try (CloseableHttpClient httpclient = HttpClients.createDefault();
                CloseableHttpResponse response = httpclient.execute(request);
                InputStream inputStream = response.getEntity().getContent()) {
            int responseCode = response.getStatusLine().getStatusCode();
            String responseBody = inputStream != null ? IOUtils.toString(inputStream) : null;
            //TODO error processing
            System.out.println("MMS Response [" + request.getMethod() + "] " + request.getURI().toString() + " - Code: " + responseCode + (MDKOptionsGroup.getMDKOptions().isLogJson() ? " - Body:" : ""));
            if (MDKOptionsGroup.getMDKOptions().isLogJson()) {
                System.out.println(responseBody);
            }
            if (processRequestErrors(responseBody, responseCode)) {
                throw new ServerException(responseBody, responseCode);
            }
            responseJson = JacksonUtils.getObjectMapper().readValue(responseBody, ObjectNode.class);
        }
        return responseJson;
    }

    /**
     * General purpose method for running a cancellable request. Builds a new thread to run the request, and passes
     * any relevant exception information back out via atomic references and generates new exceptions in calling thread
     *
     * @param request
     * @param progressStatus
     * @return
     * @throws IOException
     * @throws URISyntaxException
     * @throws ServerException    contains both response code and response body
     */
    public static ObjectNode sendCancellableMMSRequest(HttpRequestBase request, ProgressStatus progressStatus)
            throws IOException, ServerException {
        final AtomicReference<ObjectNode> resp = new AtomicReference<>();
        final AtomicReference<Integer> ecode = new AtomicReference<>();
        final AtomicReference<ThreadRequestExceptionType> etype = new AtomicReference<>();
        final AtomicReference<String> emsg = new AtomicReference<>();
        Thread t = new Thread(() -> {
            ObjectNode response = JacksonUtils.getObjectMapper().createObjectNode();
            try {
                response = sendMMSRequest(request);
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
     * @param code
     * @param response
     * @return
     */
    public static boolean processRequestErrors(String response, int code) {
        // display server message if possible, prepare additional data for display if needed
        try {
            ObjectNode responseJson = JacksonUtils.getObjectMapper().readValue(response, ObjectNode.class);
            JsonNode value;
            if (responseJson != null && (value = responseJson.get("message")) != null
                    && value.isTextual() && !value.asText().isEmpty()) {
                Utils.guilog("[SERVER MESSAGE] " + value.asText());
            }
        } catch (IOException e) {
            Utils.guilog("[ERROR] Unexpected error processing MMS response.");
            if (MDKOptionsGroup.getMDKOptions().isLogJson()) {
                Utils.guilog("Server response: " + code + " " + response);
            }
            e.printStackTrace();
            return true;
        }

        if (MDKOptionsGroup.getMDKOptions().isLogJson()) {
            Utils.guilog("Server response: " + response);
        }

        // handle response codes
        if (code == 200) {
            return false;
        }
        Utils.showPopupMessage("An error occurred while communicating with the MMS. Your operation may not have completed successfully. See the notification window for details.");
        boolean furtherProcessing = false;
        if (code >= 500) {
            Utils.guilog("[ERROR] Operation failed due to server error.");
            furtherProcessing = true;
        }
        else if (code == 404) {
            // TODO @donbot verify 404 response cases
            furtherProcessing = true;
        }
        else if (code == 403) {
            Utils.guilog("[ERROR] You do not have sufficient permissions to one or more elements in the project to complete this operation.");
        }
        else if (code == 401) {
            Utils.guilog("[ERROR] Authentication is required to utilize MMS functions. Please log in before trying again.");
            TicketUtils.clearUsernameAndPassword();
        }
        else {
            Utils.guilog("[ERROR] Unexpected server response - code: " + code + ".");
            furtherProcessing = true;
        }
        return furtherProcessing;
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
        if ((urlString == null || urlString.equals(""))) {
            if (!MDUtils.isDeveloperMode()) {
                Utils.showPopupMessage("Your project root element doesn't have ModelManagementSystem MMS URL stereotype property set!");
            }
            else {
                urlString = JOptionPane.showInputDialog("[DEVELOPER MODE] Enter the server URL:", developerUrl);
                developerUrl = urlString;
            }
        }
        if (urlString == null || urlString.equals("")) {
            throw new IllegalStateException("MMS URL is null or empty.");
        }
        return urlString.trim();
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
            }
            else {
                siteString = JOptionPane.showInputDialog("[DEVELOPER MODE] Enter the site:", developerSite);
                developerSite = siteString;
            }
        }
        if (siteString == null || siteString.equals("")) {
            throw new IllegalStateException("MMS Site is null or empty.");
        }
        return siteString.trim();
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
    public static boolean isSiteEditable(Project project, String site)
            throws IOException, URISyntaxException, ServerException {
        if (site == null || site.equals("")) {
            site = getSiteName(project);
        }

        // configure request
        //https://cae-ems.jpl.nasa.gov/alfresco/service/workspaces/master/sites
        URIBuilder requestUri = getServiceWorkspacesUri(project);
        if (requestUri == null) {
            return false;
        }
        requestUri.setPath(requestUri.getPath() + "/sites");

        // do request
        ObjectNode response = JacksonUtils.getObjectMapper().createObjectNode();
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
     * Returns a URIBuilder object with a path = "/alfresco/service/workspaces/{$WORKSPACE}".
     *
     * @param project The project to gather the mms url and site name information from
     * @return URIBuilder
     */
    public static URIBuilder getServiceWorkspacesUri(Project project) {
        URIBuilder workspaceUri = getServiceUri(project);
        if (workspaceUri == null) {
            return null;
        }
        String workspace = MDUtils.getWorkspace(project);
        workspaceUri.setPath(workspaceUri.getPath() + "/workspaces/" + workspace);
        return workspaceUri;
    }

    /**
     * Returns a URIBuilder object with a path = "/alfresco/service/workspaces/{$WORKSPACE}/sites/{$SITE}".
     *
     * @param project The project to gather the mms url and site name information from
     * @return URIBuilder
     */
    public static URIBuilder getServiceWorkspacesSitesUri(Project project) {
        URIBuilder siteUri = getServiceWorkspacesUri(project);
        if (siteUri == null) {
            return null;
        }
        String sites = getSiteName(project);
        siteUri.setPath(siteUri.getPath() + "/sites/" + sites);
        return siteUri;
    }

    /**
     * Returns a URIBuilder object with a path = "/alfresco/service/workspaces/{$WORKSPACE}/sites/{$SITE}/projects/{$PROJECTID}".
     *
     * @param project The project to gather the mms url and site name information from
     * @return URIBuilder
     */
    public static URIBuilder getSerivceWorkspacesSitesProjectsUri(Project project) {
        URIBuilder projectUri = getServiceWorkspacesSitesUri(project);
        if (projectUri == null) {
            return null;
        }
        String projectId = project.getPrimaryProject().getProjectID();
        projectUri.setPath(projectUri.getPath() + "/projects/" + projectId);
        return projectUri;
    }

    /**
     * Returns a URIBuilder object with a path = "/alfresco/service/workspaces/{$WORKSPACE}/elements".
     * This is supported in MMS, but is substantially slower than alternative ServiceWorkspacesSitesElements
     *
     * @param project The project to gather the mms url and site name information from
     * @return URIBuilder
     */
    @Deprecated
    public static URIBuilder getServiceWorkspacesElementsUri(Project project) {
        URIBuilder siteUri = getServiceWorkspacesUri(project);
        if (siteUri == null) {
            return null;
        }
        siteUri.setPath(siteUri.getPath() + "/elements");
        return siteUri;
    }

    /**
     * Returns a URIBuilder object with a path = "/alfresco/service/workspaces/{$WORKSPACE}/sites/{$SITE}/elements".
     *
     * @param project The project to gather the mms url and site name information from
     * @return URIBuilder
     */
    public static URIBuilder getServiceWorkspacesSitesElementsUri(Project project) {
        URIBuilder elementsUri = getServiceWorkspacesSitesUri(project);
        if (elementsUri == null) {
            return null;
        }
        elementsUri.setPath(elementsUri.getPath() + "/elements");
        return elementsUri;
    }

    public static String getDefaultSiteName(IProject iProject) {
        String name = iProject.getName().trim().replaceAll("\\W+", "-");
        if (name.endsWith("-")) {
            name = name.substring(0, name.length() - 1);
        }
        return name;
    }

    public static ObjectNode getProjectObjectNode(Project project) {
        return getProjectObjectNode(project.getPrimaryProject().getName(), project.getPrimaryProject().getProjectID(), project.getPrimaryProject().getProjectDescriptor().getLocationUri().toString());
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

