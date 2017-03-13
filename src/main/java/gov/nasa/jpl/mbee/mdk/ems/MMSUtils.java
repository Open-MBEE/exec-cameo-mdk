package gov.nasa.jpl.mbee.mdk.ems;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.nomagic.ci.persistence.IProject;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.task.ProgressStatus;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.ems.actions.MMSLoginAction;
import gov.nasa.jpl.mbee.mdk.ems.actions.MMSLogoutAction;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.lib.MDUtils;
import gov.nasa.jpl.mbee.mdk.lib.TicketUtils;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import gov.nasa.jpl.mbee.mdk.options.MDKOptionsGroup;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.*;
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

    public enum HttpRequestType {
        GET, POST, PUT, DELETE
    }

    public enum ThreadRequestExceptionType {
        IO_EXCEPTION, SERVER_EXCEPTION, URI_SYNTAX_EXCEPTION
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
        // verify elements
        if (elementIds == null || elementIds.isEmpty()) {
            return JacksonUtils.getObjectMapper().createObjectNode();
        }

        // build uri
        URIBuilder requestUri = getServiceProjectsRefsElementsUri(project);
        if (requestUri == null) {
            return null;
        }
        if (depth == -1 || depth > 0) {
            requestUri.setParameter("depth", java.lang.Integer.toString(depth));
        }

        // create requests json
        final ObjectNode requests = JacksonUtils.getObjectMapper().createObjectNode();
        // put elements array inside request json, keep reference
        ArrayNode idsArrayNode = requests.putArray("elements");
        for (String id : elementIds) {
            // create json for id strings, add to request array
            ObjectNode element = JacksonUtils.getObjectMapper().createObjectNode();
            element.put(MDKConstants.ID_KEY, id);
            idsArrayNode.add(element);
        }

        //do cancellable request if progressStatus exists
        Utils.guilog("[INFO] Searching for " + elementIds.size() + " elements from server...");
        if (progressStatus != null) {
            return sendCancellableMMSRequest(project, MMSUtils.buildRequest(MMSUtils.HttpRequestType.PUT, requestUri, requests), progressStatus);
        }
        return sendMMSRequest(project, MMSUtils.buildRequest(MMSUtils.HttpRequestType.PUT, requestUri, requests));
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
        // bulk GETs are not supported in MMS, but bulk PUTs are. checking and and throwing error here in case
        if (type == HttpRequestType.GET && sendData != null) {
            throw new IOException("GETs with body are not supported");
        }
        switch (type) {
            case DELETE:
                request = new HttpDeleteWithBody(requestDest);
                break;
            case GET:
//                request = new HttpGetWithBody(requestDest);
                request = new HttpGet(requestDest);
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

    /**
     * General purpose method for sending a constructed http request via http client.
     *
     * @param request
     * @return
     * @throws IOException
     * @throws ServerException
     */
    public static ObjectNode sendMMSRequest(Project project, HttpRequestBase request)
            throws IOException, ServerException, URISyntaxException {
        HttpEntityEnclosingRequest httpEntityEnclosingRequest = null;
        boolean logBody = MDKOptionsGroup.getMDKOptions().isLogJson() && request instanceof HttpEntityEnclosingRequest
                && ((httpEntityEnclosingRequest = (HttpEntityEnclosingRequest) request).getEntity() != null)
                && httpEntityEnclosingRequest.getEntity().isRepeatable();
        System.out.println("MMS Request [" + request.getMethod() + "] " + request.getURI().toString());
        if (logBody) {
            try (InputStream inputStream = httpEntityEnclosingRequest.getEntity().getContent()) {
                String requestBody = IOUtils.toString(inputStream);
                if (request.getURI().getPath().contains("alfresco/service/api/login")) {
                    requestBody = "--- Censored ---";
                }
                System.out.println(" - Body: " + requestBody);
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
            String responseBody = ((inputStream != null) ? IOUtils.toString(inputStream) : "");
            String responseType = ((response.getEntity().getContentType() != null) ? response.getEntity().getContentType().getValue() : "");

            // debug / logging output from response
            System.out.println("MMS Response [" + request.getMethod() + "] " + request.getURI().toString() + " - Code: " + responseCode);
            if (MDKOptionsGroup.getMDKOptions().isLogJson()) {
                if (!responseBody.isEmpty() && !responseType.equals("application/json;charset=UTF-8")) {
                    responseBody = "<span>" + responseBody + "</span>";
                }
                System.out.println(" - Body: "  + responseBody);
            }

            // flag for later server exceptions; they will be thrown after printing any available server messages to the gui log
            boolean throwServerException = false;

            // assume that 404s with json response bodies are "missing resource" 404s, which are expected for some cases and should not break normal execution flow
            if (responseCode == 404 && responseType.equals("application/json;charset=UTF-8")) {
                // do nothing, note in log
                System.out.println("[INFO] \"Missing Resource\" 404 processed.");
            }
            // allow re-attempt of request if credentials have expired or are invalid
            else if (responseCode == 401) {
                Utils.guilog("[ERROR] MMS authentication is missing or invalid. Closing connections. Please log in again and your request will be retried. Server code: " + responseCode);
                MMSLogoutAction.logoutAction(project);
                if (MMSLoginAction.loginAction(project)) {
                    URIBuilder newRequestUri = new URIBuilder(request.getURI());
                    newRequestUri.setParameter("alf_ticket", TicketUtils.getTicket(project));
                    request.setURI(newRequestUri.build());
                    return sendMMSRequest(project, request);
                }
                else {
                    throwServerException = true;
                }
            }
            // if it's anything else outside of the 200 range, assume failure and break normal flow
            else if (responseCode < 200 || responseCode >= 300) {
                Utils.guilog("[ERROR] Operation failed due to server error. Server code: " + responseCode);
                throwServerException = true;
            }

            // print server message if possible
            if (!responseBody.isEmpty() && responseType.equals("application/json;charset=UTF-8")) {
                responseJson = JacksonUtils.getObjectMapper().readValue(responseBody, ObjectNode.class);
                JsonNode value;
                // display single response message
                if (responseJson != null && (value = responseJson.get("message")) != null && value.isTextual() && !value.asText().isEmpty()) {
                    Application.getInstance().getGUILog().log("[SERVER MESSAGE] " + value.asText());
                }
                // display multiple response messages
                if (responseJson != null && (value = responseJson.get("messages")) != null && value.isArray()) {
                    ArrayNode msgs = (ArrayNode) value;
                    for (JsonNode msg : msgs) {
                        if (msg != null && (value = msg.get("message")) != null && value.isTextual() && !value.asText().isEmpty()) {
                            Application.getInstance().getGUILog().log("[SERVER MESSAGE] " + value.asText());
                        }
                    }
                }
            }

            if (throwServerException) {
                // big flashing red letters that the action failed, or as close as we're going to get
                Utils.showPopupMessage("Action failed. See notification window for details.");
                // throw is done last, after printing the error and any messages that might have been returned
                throw new ServerException(responseBody, responseCode);
            }
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
    public static ObjectNode sendCancellableMMSRequest(final Project project, HttpRequestBase request, ProgressStatus progressStatus)
            throws IOException, ServerException, URISyntaxException {
        final AtomicReference<ObjectNode> resp = new AtomicReference<>();
        final AtomicReference<Integer> ecode = new AtomicReference<>();
        final AtomicReference<ThreadRequestExceptionType> etype = new AtomicReference<>();
        final AtomicReference<String> emsg = new AtomicReference<>();
        Thread t = new Thread(() -> {
            ObjectNode response = JacksonUtils.getObjectMapper().createObjectNode();
            try {
                response = sendMMSRequest(project, request);
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
            } catch (URISyntaxException e) {
                etype.set(ThreadRequestExceptionType.URI_SYNTAX_EXCEPTION);
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
        else if (etype.get() == ThreadRequestExceptionType.URI_SYNTAX_EXCEPTION) {
            throw new URISyntaxException("", emsg.get());
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
            return null;
        }
        if ((urlString == null || urlString.isEmpty())) {
            Utils.showPopupMessage("Your project root element doesn't have ModelManagementSystem MMS URL stereotype property set!");
            if (MDUtils.isDeveloperMode()) {
                urlString = JOptionPane.showInputDialog("[DEVELOPER MODE] Enter the server URL:", developerUrl);
                developerUrl = urlString;
            }
        }
        if (urlString == null || urlString.isEmpty()) {
            return null;
        }
        return urlString.trim();
    }

    public static String getOrg(Project project)
            throws IOException, URISyntaxException, ServerException {

//        String siteString = "";
//        if (StereotypesHelper.hasStereotype(project.getPrimaryModel(), "ModelManagementSystem")) {
//            siteString = (String) StereotypesHelper.getStereotypePropertyFirst(project.getPrimaryModel(), "ModelManagementSystem", "MMS Org");
//        }
//        return siteString;

        URIBuilder uriBuilder = getServiceProjectsUri(project);
        ObjectNode response = sendMMSRequest(project, buildRequest(HttpRequestType.GET, uriBuilder));
        JsonNode arrayNode;
        if (((arrayNode = response.get("projects")) != null) && arrayNode.isArray()) {
            JsonNode value;
            for (JsonNode projectNode : arrayNode) {
                if (((value = projectNode.get(MDKConstants.ID_KEY)) != null ) && value.isTextual() && value.asText().equals(project.getID())
                        && ((value = projectNode.get(MDKConstants.ORG_ID_KEY)) != null ) && value.isTextual() && !value.asText().isEmpty()) {
                    return value.asText();
                }
            }
        }
        return null;
    }

    public static boolean isProjectOnMms(Project project) throws IOException, URISyntaxException, ServerException {
        // build request for bulk project GET
        URIBuilder requestUri = MMSUtils.getServiceProjectsUri(project);
        if (requestUri == null) {
            return false;
        }

        // do request, check return for project
        ObjectNode response;
        response = MMSUtils.sendMMSRequest(project, MMSUtils.buildRequest(MMSUtils.HttpRequestType.GET, requestUri));
        JsonNode projectsJson;
        if ((projectsJson = response.get("projects")) != null && projectsJson.isArray()) {
            JsonNode value;
            for (JsonNode projectJson : projectsJson) {
                if ((value = projectJson.get(MDKConstants.ID_KEY)) != null && value.isTextual()
                        && value.asText().equals(Converters.getIProjectToIdConverter().apply(project.getPrimaryProject()))) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isBranchOnMms(Project project, String branch) throws IOException, URISyntaxException, ServerException {
        // build request for project element
        URIBuilder requestUri = MMSUtils.getServiceProjectsRefsUri(project);
        if (requestUri == null) {
            return false;
        }

        // do request for ref element
        ObjectNode response;
        response = MMSUtils.sendMMSRequest(project, MMSUtils.buildRequest(MMSUtils.HttpRequestType.GET, requestUri));
        JsonNode projectsJson;
        if ((projectsJson = response.get("refs")) != null && projectsJson.isArray()) {
            JsonNode value;
            for (JsonNode projectJson : projectsJson) {
                if ((value = projectJson.get(MDKConstants.NAME_KEY)) != null && value.isTextual() && value.asText().equals(branch)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String getProjectOrg(Project project)
            throws IOException, URISyntaxException, ServerException {
        URIBuilder uriBuilder = getServiceProjectsUri(project);
        ObjectNode response = sendMMSRequest(project, buildRequest(HttpRequestType.GET, uriBuilder));
        JsonNode arrayNode;
        if (((arrayNode = response.get("projects")) != null) && arrayNode.isArray()) {
            JsonNode value;
            for (JsonNode projectNode : arrayNode) {
                if (((value = projectNode.get(MDKConstants.ID_KEY)) != null ) && value.isTextual() && value.asText().equals(project.getID())
                        && ((value = projectNode.get(MDKConstants.ORG_ID_KEY)) != null ) && value.isTextual() && !value.asText().isEmpty()) {
                    return value.asText();
                }
            }
        }
        return null;
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
        String urlString = getServerUrl(project);
        if (urlString == null) {
            return null;
        }

        // [scheme:][//host][path][?query][#fragment]

        URIBuilder uri;
        try {
            uri = new URIBuilder(urlString);
        } catch (URISyntaxException e) {
            Application.getInstance().getGUILog().log("[ERROR] Unexpected error in generation of MMS URL for project. Reason: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        uri.setPath("/alfresco/service");
        if (TicketUtils.isTicketSet(project)) {
            uri.setParameter("alf_ticket", TicketUtils.getTicket(project));
        }
        return uri;

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
        projectUri.setPath(projectUri.getPath() + "/projects");
        return projectUri;
    }

    /**
     * Returns a URIBuilder object with a path = "/alfresco/service/projects/{$PROJECT_ID}/refs/{$WORKSPACE_ID}"
     *
     * @param project The project to gather the mms url and site name information from
     * @return URIBuilder
     */
    public static URIBuilder getServiceProjectsRefsUri (Project project) {
        URIBuilder refsUri = getServiceProjectsUri(project);
        if (refsUri == null) {
            return null;
        }
        refsUri.setPath(refsUri.getPath() + "/" + Converters.getIProjectToIdConverter().apply(project.getPrimaryProject()) + "/refs");
        return refsUri;
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
        // TODO review MDUtils.getWorkspace() to make sure it's returning the appropriate thing for branches
        elementUri.setPath(elementUri.getPath() + "/" + MDUtils.getWorkspace(project) + "/elements");
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
        return getProjectObjectNode(project.getPrimaryProject());
    }

    public static ObjectNode getProjectObjectNode(IProject iProject) {
        ObjectNode projectObjectNode = JacksonUtils.getObjectMapper().createObjectNode();
        projectObjectNode.put(MDKConstants.TYPE_KEY, "Project");
        projectObjectNode.put(MDKConstants.NAME_KEY, iProject.getName());
        projectObjectNode.put(MDKConstants.ID_KEY, Converters.getIProjectToIdConverter().apply(iProject));
        String resourceId = "";
        if (ProjectUtilities.getProject(iProject).isRemote()) {
            resourceId = ProjectUtilities.getResourceID(iProject.getLocationURI());
        }
        projectObjectNode.put(MDKConstants.TWC_ID, resourceId);
        String categoryId = "";
        if (ProjectUtilities.getProject(iProject).getPrimaryProject() == iProject && !resourceId.isEmpty()) {
            categoryId = EsiUtils.getCategoryID(resourceId);
        }
        projectObjectNode.put(MDKConstants.CATEGORY_ID_KEY, categoryId);
        projectObjectNode.put(MDKConstants.PROJECT_URI, iProject.getProjectDescriptor().getLocationUri().toString());
        return projectObjectNode;
    }

}

