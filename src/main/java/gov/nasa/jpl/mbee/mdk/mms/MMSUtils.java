package gov.nasa.jpl.mbee.mdk.mms;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.nomagic.ci.persistence.IProject;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.task.ProgressStatus;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.MDKPlugin;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.mms.actions.MMSLogoutAction;
import gov.nasa.jpl.mbee.mdk.mms.endpoints.*;
import gov.nasa.jpl.mbee.mdk.options.MDKOptionsGroup;
import gov.nasa.jpl.mbee.mdk.util.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class MMSUtils {

    private static final int CHECK_CANCEL_DELAY = 100;
    private static final AtomicReference<Exception> LAST_EXCEPTION = new AtomicReference<>();
    private static final Cache<Project, String> PROFILE_SERVER_CACHE = CacheBuilder.newBuilder().weakKeys().maximumSize(100).expireAfterAccess(10, TimeUnit.MINUTES).build();

    public enum HttpRequestType {
        GET, POST, PUT, DELETE
    }

    public enum JsonBlobType {
        ELEMENT_JSON, ELEMENT_ID, ARTIFACT_JSON, ARTIFACT_ID, PROJECT, REF, ORG, SEARCH
    }

    public static AtomicReference<Exception> getLastException() {
        return LAST_EXCEPTION;
    }

    public static ObjectNode getElement(Project project, String elementId, ProgressStatus progressStatus) throws IOException, ServerException, URISyntaxException {
        Collection<String> elementIds = new ArrayList<>(1);
        elementIds.add(elementId);
        File responseFile = getElementsRecursively(project, elementIds, 0, progressStatus);
        try (JsonParser responseParser = JacksonUtils.getJsonFactory().createParser(responseFile)) {
            ObjectNode response = JacksonUtils.parseJsonObject(responseParser);
            JsonNode value;
            if (((value = response.get("elements")) != null) && value.isArray()
                    && (value = ((ArrayNode) value).remove(1)) != null && (value instanceof ObjectNode)) {
                return (ObjectNode) value;
            }
        }
        return null;
    }

    public static File getElementRecursively(Project project, String elementId, int depth, ProgressStatus progressStatus) throws IOException, ServerException, URISyntaxException {
        Collection<String> elementIds = new ArrayList<>(1);
        elementIds.add(elementId);
        return getElementsRecursively(project, elementIds, depth, progressStatus);
    }

    /**
     * @param elementIds     collection of elements to get mms data for
     * @param project        project to check
     * @param progressStatus progress status object, can be null
     * @return object node response
     * @throws ServerException
     * @throws IOException
     * @throws URISyntaxException
     */
    public static File getElements(Project project, Collection<String> elementIds, ProgressStatus progressStatus) throws IOException, ServerException, URISyntaxException {
        return getElementsRecursively(project, elementIds, 0, progressStatus);
    }

    /**
     * @param elementIds     collection of elements to get mms data for
     * @param depth          depth to recurse through child elements. takes priority over recurse field
     * @param project        project to check
     * @param progressStatus progress status object, can be null
     * @return object node response
     * @throws ServerException
     * @throws IOException
     * @throws URISyntaxException
     */
    public static File getElementsRecursively(Project project, Collection<String> elementIds, int depth, ProgressStatus progressStatus) throws ServerException, IOException, URISyntaxException {
        // verify elements
        if (elementIds == null || elementIds.isEmpty()) {
            return null;
        }

        // build uri
        MMSEndpoint mmsEndpoint = getServiceProjectsRefsElementsUri(project);
        if (mmsEndpoint == null) {
            return null;
        }

        // create request file
        File sendData = createEntityFile(MMSUtils.class, ContentType.APPLICATION_JSON, elementIds, JsonBlobType.ELEMENT_ID);

        //do cancellable request if progressStatus exists
        return sendMMSRequest(project, mmsEndpoint.buildRequest(HttpRequestType.PUT, sendData, ContentType.APPLICATION_JSON, project), progressStatus);
    }

    public static File getArtifacts(Project project, Collection<String> artifactIds, ProgressStatus progressStatus) throws ServerException, IOException, URISyntaxException {
        if (artifactIds == null || artifactIds.isEmpty()) {
            return null;
        }
        MMSEndpoint mmsEndpoint = getServiceProjectsRefsArtifactsUri(project);
        if (mmsEndpoint == null) {
            return null;
        }
        File sendData = createEntityFile(MMSUtils.class, ContentType.APPLICATION_JSON, artifactIds, JsonBlobType.ARTIFACT_ID);
        return sendMMSRequest(project, mmsEndpoint.buildRequest(HttpRequestType.PUT, sendData, ContentType.APPLICATION_JSON, project), progressStatus);
    }

    public static String getCredentialsTicket(Project project, String username, String password, ProgressStatus progressStatus) throws ServerException, IOException, URISyntaxException {
        return getCredentialsTicket(project, null, username, password, progressStatus);
    }

    public static String getCredentialsTicket(String baseUrl, String username, String password, ProgressStatus progressStatus) throws ServerException, IOException, URISyntaxException {
        return getCredentialsTicket(null, baseUrl, username, password, progressStatus);
    }

    public static String getJwtToken(Project project, String twcServer, String authToken, ProgressStatus progressStatus) throws ServerException, IOException, URISyntaxException {
        MMSEndpoint endpoint = MMSEndpointFactory.getMMSEndpoint(MMSUtils.getServerUrl(project), MMSEndpointConstants.TWC_LOGIN_CASE);
        endpoint.prepareUriPath();
        if(endpoint instanceof MMSTWCLoginEndpoint) {
            return ((MMSTWCLoginEndpoint) endpoint).buildTWCLoginRequest(project, twcServer, authToken, progressStatus);
        }
        return null;
    }

    private static String getCredentialsTicket(Project project, String baseUrl, String username, String password, ProgressStatus progressStatus) throws ServerException, IOException, URISyntaxException {
        MMSEndpoint endpoint = MMSEndpointFactory.getMMSEndpoint(MMSUtils.getServerUrl(project), MMSEndpointConstants.LOGIN_CASE);
        endpoint.prepareUriPath();
        if(endpoint instanceof MMSLoginEndpoint) {
            return ((MMSLoginEndpoint) endpoint).buildLoginRequest(project, username, password, progressStatus);
        }
        return null;
    }

    public static boolean validateJwtToken(Project project, String jwtToken, ProgressStatus progressStatus) throws ServerException, IOException, URISyntaxException {
        MMSEndpoint mmsEndpoint = MMSEndpointFactory.getMMSEndpoint(getServerUrl(project), MMSEndpointConstants.TWC_VALIDATE_TOKEN_ENDPOINT);
        mmsEndpoint.prepareUriPath();

        //build request
        HttpRequestBase request = mmsEndpoint.buildRequest(HttpRequestType.GET, null, ContentType.APPLICATION_JSON, project);

        // do request
        ObjectNode responseJson = JacksonUtils.getObjectMapper().createObjectNode();
        sendMMSRequest(project, request, progressStatus, responseJson);

        // parse response
        JsonNode value;
        if (responseJson != null && (value = responseJson.get("isTokenValid")) != null && value.isBoolean()) {
            return value.asBoolean();
        }
        return false;
    }

    public static String validateCredentialsTicket(Project project, String ticket, ProgressStatus progressStatus) throws ServerException, IOException, URISyntaxException {
        MMSEndpoint mmsEndpoint = MMSEndpointFactory.getMMSEndpoint(getServerUrl(project), MMSEndpointConstants.LOGIN_CASE);
        mmsEndpoint.prepareUriPath();

        //build request
        HttpRequestBase request = mmsEndpoint.buildRequest(HttpRequestType.GET, null, ContentType.APPLICATION_JSON, project);

        // do request
        ObjectNode responseJson = JacksonUtils.getObjectMapper().createObjectNode();
        sendMMSRequest(project, request, progressStatus, responseJson);

        // parse response
        JsonNode value;
        if (responseJson != null && (value = responseJson.get("username")) != null && value.isTextual() && !value.asText().isEmpty()) {
            return value.asText();
        }
        return "";
    }

    public static File createEntityFile(Class<?> clazz, ContentType contentType, Collection<?> nodes, JsonBlobType jsonBlobType) throws IOException {
        File requestFile = File.createTempFile(clazz.getSimpleName() + "-" + contentType.getMimeType().replace('/', '-') + "-", null);
        if (MDKOptionsGroup.getMDKOptions().isLogJson()) {
            System.out.println("[INFO] Request Body: " + requestFile.getPath());
            Application.getInstance().getGUILog().log("[INFO] Request Body: " + requestFile.getPath());
        }
        else {
            if(!requestFile.delete()) { // if we cannot immediately delete we'll get it later
                requestFile.deleteOnExit();
            }
        }

        String arrayName = null;
        switch (jsonBlobType) {
            case ELEMENT_ID:
            case ELEMENT_JSON:
                arrayName = "elements";
                break;
            case ARTIFACT_ID:
            case ARTIFACT_JSON:
                arrayName = "artifacts";
                break;
            case ORG:
                arrayName = "orgs";
                break;
            case PROJECT:
                arrayName = "projects";
                break;
            case REF:
                arrayName = "refs";
                break;
        }

        try (FileOutputStream outputStream = new FileOutputStream(requestFile);
             JsonGenerator jsonGenerator = JacksonUtils.getJsonFactory().createGenerator(outputStream)) {

            jsonGenerator.writeStartObject();
            if(jsonBlobType != JsonBlobType.SEARCH) {
                jsonGenerator.writeArrayFieldStart(arrayName);
            }

            for (Object node : nodes) {
                if (node instanceof ObjectNode && jsonBlobType == JsonBlobType.ELEMENT_JSON || jsonBlobType == JsonBlobType.ORG || jsonBlobType == JsonBlobType.PROJECT || jsonBlobType == JsonBlobType.REF) {
                    jsonGenerator.writeObject(node);
                } else if (node instanceof String && jsonBlobType == JsonBlobType.ELEMENT_ID || jsonBlobType == JsonBlobType.ARTIFACT_ID) {
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeStringField(MDKConstants.ID_KEY, (String) node);
                    jsonGenerator.writeEndObject();
                } else if (node instanceof String && jsonBlobType == JsonBlobType.SEARCH) {
                    jsonGenerator.writeObjectFieldStart(MDKConstants.PARAMS_FIELD);
                    jsonGenerator.writeStringField(MDKConstants.OWNER_ID_KEY, (String) node);
                    jsonGenerator.writeEndObject();
                    jsonGenerator.writeObjectFieldStart(MDKConstants.RECURSE_FIELD);
                    jsonGenerator.writeStringField(MDKConstants.ID_KEY, MDKConstants.OWNER_ID_KEY);
                    jsonGenerator.writeEndObject();
                }
                else {
                    throw new IOException("Unsupported collection type for entity file.");
                }
            }

            if(jsonBlobType != JsonBlobType.SEARCH) {
                jsonGenerator.writeEndArray();
            }

            jsonGenerator.writeStringField("source", "magicdraw");
            jsonGenerator.writeStringField("mdkVersion", MDKPlugin.getVersion());
            jsonGenerator.writeEndObject();
        }

        return requestFile;
    }

    /**
     * General purpose method for sending a constructed http request via http client. For streaming reasons, defaults to writing to a file.
     * When the file is written, it is temp unless the logJSON environment variable is enabled (or DEVELOPER mode is on). This file IS NOT
     * written to when the responseJson object is non-null. In that instance, the response is parsed into this object instead, keeping
     * the results entirely in memory. For large results this could be extremely memory intensive, so it is not advised for general use.
     *
     * @param request
     * @return
     * @throws IOException
     * @throws ServerException
     */
    public static File sendMMSRequest(Project project, HttpRequestBase request, ProgressStatus progressStatus, final ObjectNode responseJson) throws IOException, ServerException, URISyntaxException {
        final File responseFile = (responseJson == null ? File.createTempFile("Response-", null) : null);
        final AtomicReference<String> responseBody = new AtomicReference<>();
        final AtomicReference<Integer> responseCode = new AtomicReference<>();

        String requestSummary = "[INFO] MMS Request [" + request.getMethod() + "] " + request.getURI().toString();
        System.out.println(requestSummary);
        if (MDUtils.isDeveloperMode()) {
            Application.getInstance().getGUILog().log(requestSummary);
        }

        // create client, execute request, parse response, store in thread safe buffer to return as string later
        // client, response, and reader are all auto closed after block
        if (progressStatus == null) {
            try (CloseableHttpClient httpclient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpclient.execute(request);
                 InputStream inputStream = response.getEntity().getContent()) {
                responseCode.set(response.getStatusLine().getStatusCode());
                String responseSummary = "[INFO] MMS Response [" + request.getMethod() + "]: " + responseCode.get() + " " + request.getURI().toString();
                System.out.println(responseSummary);
                if (MDUtils.isDeveloperMode()) {
                    Application.getInstance().getGUILog().log(responseSummary);
                }
                if (inputStream != null) {
                    responseBody.set(generateMmsOutput(inputStream, responseFile));
                }
            }
        }
        else {
            LAST_EXCEPTION.set(null);
            progressStatus.setIndeterminate(true);
            Future<?> future = TaskRunner.runWithProgressStatus(() -> {
                try (CloseableHttpClient httpclient = HttpClients.createDefault();
                     CloseableHttpResponse response = httpclient.execute(request);
                     InputStream inputStream = response.getEntity().getContent()) {
                    responseCode.set(response.getStatusLine().getStatusCode());
                    if (MDKOptionsGroup.getMDKOptions().isLogJson()) {
                        System.out.println("[INFO] MMS Response [" + request.getMethod() + "]: " + responseCode.get() + " " + request.getURI().toString());
                    }
                    if (inputStream != null) {
                        responseBody.set(generateMmsOutput(inputStream, responseFile));
                    }
                } catch (Exception e) {
                    LAST_EXCEPTION.set(e);
                    e.printStackTrace();
                }
            }, null, TaskRunner.ThreadExecutionStrategy.NONE, true);
            try {
                while (!future.isDone() && !future.isCancelled()) {
                    try {
                        future.get(CHECK_CANCEL_DELAY, TimeUnit.MILLISECONDS);
                    } catch (ExecutionException | TimeoutException ignored) {

                    } catch (InterruptedException e2) {
                        Thread.currentThread().interrupt();
                    }
                    if (progressStatus.isCancel() && future.cancel(true)) {
                        Application.getInstance().getGUILog().log("[INFO] MMS request was manually cancelled.");
                        return null;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (LAST_EXCEPTION.get() instanceof IOException) {
                throw (IOException) LAST_EXCEPTION.get();
            }
        }
        if (responseFile == null) {
            try (InputStream inputStream = new ByteArrayInputStream(responseBody.get().getBytes())) {
                if (!processResponse(responseCode.get(), inputStream, project)) {
                    throw new ServerException(responseBody.get(), responseCode.get());
                }
                ObjectNode json = JacksonUtils.getObjectMapper().readValue(responseBody.get(), ObjectNode.class);
                Iterator<Map.Entry<String, JsonNode>> jsonFields = json.fields();
                while (jsonFields.hasNext()) {
                    Map.Entry<String, JsonNode> currentField = jsonFields.next();
                    responseJson.put(currentField.getKey(), currentField.getValue());
                }
            }
        }
        else {
            try (InputStream inputStream = new FileInputStream(responseFile)) {
                if (!processResponse(responseCode.get(), inputStream, project)) {
                    throw new ServerException(responseFile.getAbsolutePath(), responseCode.get());
                }
            }
        }

        return responseFile;
    }

    public static File sendMMSRequest(Project project, HttpRequestBase request) throws IOException, ServerException, URISyntaxException {
        return sendMMSRequest(project, request, null, null);
    }

    public static File sendMMSRequest(Project project, HttpRequestBase request, ProgressStatus progressStatus) throws IOException, ServerException, URISyntaxException {
        return sendMMSRequest(project, request, progressStatus, null);
    }

    private static String generateMmsOutput(InputStream inputStream, final File responseFile) throws IOException {
        if (responseFile != null) {
            try (OutputStream outputStream = new FileOutputStream(responseFile)) {
                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                if (MDKOptionsGroup.getMDKOptions().isLogJson()) {
                    System.out.println("[INFO] Response Body: " + responseFile.getPath());
                    Application.getInstance().getGUILog().log("[INFO] Response Body: " + responseFile.getPath());
                }
                else {
                    if(!responseFile.delete()) { // if we cannot immediately delete we'll get it later
                        responseFile.deleteOnExit();
                    }
                }
            }
            return "";
        }
        else {
            return IOUtils.toString(inputStream);
        }
    }

    private static boolean processResponse(int responseCode, InputStream responseStream, Project project) {
        boolean throwServerException = false;
        JsonFactory jsonFactory = JacksonUtils.getJsonFactory();
        try (JsonParser jsonParser = jsonFactory.createParser(responseStream)) {
            while (jsonParser.nextFieldName() != null && !jsonParser.nextFieldName().equals("message")) {
                // spin until we find message
            }
            if (jsonParser.getCurrentToken() == JsonToken.FIELD_NAME) {
                jsonParser.nextToken();
                Application.getInstance().getGUILog().log("[SERVER MESSAGE] " + jsonParser.getText());
            }
        } catch (IOException e) {
            Application.getInstance().getGUILog().log("[WARNING] Unable to retrieve messages from server response.");
            throwServerException = true;
        }

        if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            Application.getInstance().getGUILog().log("[ERROR] MMS authentication is missing or invalid. Closing connections. Please log in again and your request will be retried.");
            if (project != null) {
                MMSLogoutAction.logoutAction(project);
            }
            throwServerException = true;
        }
        // if we got messages out, we hit a valid endpoint and got a valid response and either a 200 or a 404 is an acceptable response code. If not, throw is already true.
        else if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_NOT_FOUND) {
            throwServerException = true;
        }

        /*
        if (throwServerException) {
            // big flashing red letters that the action failed, or as close as we're going to get
            Application.getInstance().getGUILog().log("<span style=\"color:#FF0000; font-weight:bold\">[ERROR] Operation failed due to server error. Server code: " + responseCode + "</span>" +
                    "<span style=\"color:#FFFFFF; font-weight:bold\"> !!!!!</span>"); // hidden characters for easy search
        }
        */
        return !throwServerException;
    }

    /**
     * @param project
     * @return
     * @throws IllegalStateException
     */
    public static String getServerUrl(Project project) throws IllegalStateException {
        String urlString;
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
        else if (ProjectUtilities.isStandardSystemProfile(project.getPrimaryProject())) {
            urlString = PROFILE_SERVER_CACHE.getIfPresent(project);
            if (urlString == null) {
                urlString = JOptionPane.showInputDialog("Specify server URL for standard profile.", null);
            }
            if (urlString == null || urlString.trim().isEmpty()) {
                return null;
            }
            PROFILE_SERVER_CACHE.put(project, urlString);
        }
        else {
            Utils.showPopupMessage("The root element does not have the ModelManagementSystem stereotype.\nPlease apply it and specify the server information.");
            return null;
        }
        if (urlString == null || urlString.isEmpty()) {
            return null;
        }
        return urlString.trim();
    }

    public static String getMmsOrg(Project project) throws IOException, URISyntaxException, ServerException {
        MMSEndpoint mmsEndpoint = MMSEndpointFactory.getMMSEndpoint(getServerUrl(project), MMSEndpointConstants.PROJECTS_CASE);
        mmsEndpoint.prepareUriPath();

        File responseFile = sendMMSRequest(project, mmsEndpoint.buildRequest(HttpRequestType.GET, null, null, project));
        try (JsonParser responseParser = JacksonUtils.getJsonFactory().createParser(responseFile)) {
            ObjectNode response = JacksonUtils.parseJsonObject(responseParser);
            JsonNode arrayNode;
            if (((arrayNode = response.get("projects")) != null) && arrayNode.isArray()) {
                JsonNode projectId, orgId;
                for (JsonNode projectNode : arrayNode) {
                    if (((projectId = projectNode.get(MDKConstants.ID_KEY)) != null) && projectId.isTextual() && projectId.asText().equals(Converters.getIProjectToIdConverter().apply(project.getPrimaryProject()))
                            && ((orgId = projectNode.get(MDKConstants.ORG_ID_KEY)) != null) && orgId.isTextual() && !orgId.asText().isEmpty()) {
                        return orgId.asText();
                    }
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
        return getServiceUri(project, null);
    }

    public static URIBuilder getServiceUri(String baseUrl) {
        return getServiceUri(null, baseUrl);
    }

    private static URIBuilder getServiceUri(Project project, String baseUrl) {
        String urlString = project == null ? baseUrl : getServerUrl(project);
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
        if (project != null && TicketUtils.isTicketSet(project)) {
            uri.setParameter("alf_ticket", TicketUtils.getTicket(project));
        }
        return uri;

    }

    /**
     *
     *
     * @param project The project to gather the mms url and site name information from
     * @return URIBuilder
     */
    public static MMSEndpoint getServiceOrgsUri(Project project) {
        return getServiceOrgsUri(project, null);
    }

    public static MMSEndpoint getServiceOrgsUri(String baseUrl) {
        return getServiceOrgsUri(null, baseUrl);
    }

    private static MMSEndpoint getServiceOrgsUri(Project project, String baseUrl) {
        MMSEndpoint mmsEndpoint;
        if((baseUrl == null || baseUrl.isEmpty()) && project != null) {
            mmsEndpoint = MMSEndpointFactory.getMMSEndpoint(getServerUrl(project), MMSEndpointConstants.ORGS_CASE);
        } else {
            mmsEndpoint = MMSEndpointFactory.getMMSEndpoint(baseUrl, MMSEndpointConstants.ORGS_CASE);
        }

        mmsEndpoint.prepareUriPath();
        return mmsEndpoint;
    }

    /**
     *
     *
     * @param project The project to gather the mms url and site name information from
     * @return URIBuilder
     */
    public static MMSEndpoint getServiceProjectsUri(Project project) {
        return getServiceProjectsUri(project, null);
    }

    public static MMSEndpoint getServiceProjectsUri(String baseUrl) {
        return getServiceProjectsUri(null, baseUrl);
    }

    private static MMSEndpoint getServiceProjectsUri(Project project, String baseUrl) {
        MMSEndpoint mmsEndpoint;
        if((baseUrl == null || baseUrl.isEmpty()) && project != null) {
            mmsEndpoint = MMSEndpointFactory.getMMSEndpoint(getServerUrl(project), MMSEndpointConstants.PROJECTS_CASE);
        } else {
            mmsEndpoint = MMSEndpointFactory.getMMSEndpoint(baseUrl, MMSEndpointConstants.PROJECTS_CASE);
        }

        mmsEndpoint.prepareUriPath();
        return mmsEndpoint;
    }

    /**
     *
     *
     * @param project The project to gather the mms url and site name information from
     * @return URIBuilder
     */
    public static MMSEndpoint getServiceProjectsRefsUri(Project project) {
        return getServiceProjectsRefsUri(project, null, null);
    }

    public static MMSEndpoint getServiceProjectsRefsUri(String baseUrl, String projectId) {
        return getServiceProjectsRefsUri(null, baseUrl, projectId);
    }

    private static MMSEndpoint getServiceProjectsRefsUri(Project project, String baseUrl, String projectId) {
        MMSEndpoint mmsEndpoint;
        if((baseUrl == null || baseUrl.isEmpty()) && project != null) {
            mmsEndpoint = MMSEndpointFactory.getMMSEndpoint(getServerUrl(project), MMSEndpointConstants.REFS_CASE);
        } else {
            mmsEndpoint = MMSEndpointFactory.getMMSEndpoint(baseUrl, MMSEndpointConstants.REFS_CASE);
        }
        mmsEndpoint.prepareUriPath();
        ((MMSRefsEndpoint) mmsEndpoint).setProjectId(project == null ? projectId : Converters.getIProjectToIdConverter().apply(project.getPrimaryProject()));

        return mmsEndpoint;
    }

    /**
     *
     *
     * @param project The project to gather the mms url and site name information from
     * @return URIBuilder
     */
    public static MMSEndpoint getServiceProjectsRefsElementsUri(Project project) {
        MMSEndpoint mmsEndpoint = MMSEndpointFactory.getMMSEndpoint(getServerUrl(project), MMSEndpointConstants.ELEMENTS_CASE);
        mmsEndpoint.prepareUriPath();
        ((MMSElementsEndpoint) mmsEndpoint).setProjectId(Converters.getIProjectToIdConverter().apply(project.getPrimaryProject()));
        ((MMSElementsEndpoint) mmsEndpoint).setRefId(MDUtils.getBranchId(project));

        return mmsEndpoint;
    }

    public static MMSEndpoint getServiceProjectsRefsArtifactsUri(Project project) {
        return null; // TODO should this be unimplemented for MMS4?
    }

    public static String getDefaultSiteName(IProject iProject) {
        String name = iProject.getName().trim().replaceAll("\\W+", "-");
        if (name.endsWith("-")) {
            name = name.substring(0, name.length() - 1);
        }
        return name;
    }

}
