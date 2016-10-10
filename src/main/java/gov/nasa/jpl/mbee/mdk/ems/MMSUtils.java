package gov.nasa.jpl.mbee.mdk.ems;

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
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import gov.nasa.jpl.mbee.mdk.options.MDKOptionsGroup;
import gov.nasa.jpl.mbee.mdk.viewedit.ViewEditUtils;

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
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.client.utils.URIBuilder;

/**
 * Created by igomes on 9/26/16.
 */
// TODO Use URI builder or similar @donbot
public class MMSUtils {
    
    private static String developerUrl = "";
    private static String developerSite = "";
    
    public static ObjectNode getElement(Element element) throws JsonParseException, JsonMappingException, IOException {
        return getElementById(Converters.getElementToIdConverter().apply(element));
    }

    public static ObjectNode getElementById(String id) throws JsonParseException, JsonMappingException, IOException {
        if (id == null) {
            return null;
        }
        String url = ExportUtility.getUrlWithWorkspace();
        if (url == null) {
            return null;
        }
        id = id.replace(".", "%2E");
        url += "/elements/" + id;
        String response = null;
        try {
            response = ExportUtility.get(url, false);
        } catch (ServerException ex) {

        }
        if (response == null) {
            return null;
        }
        
        ObjectNode responseJson = JacksonUtils.getObjectMapper().readValue(response, ObjectNode.class);
        ArrayNode elements = (ArrayNode) responseJson.get("elements");
        if (elements == null || elements.size() == 0  ) {
            return null;
        }
        
        return (ObjectNode) elements.get(0);
    }

    public static ObjectNode getElements(Collection<Element> elements, ProgressStatus ps) throws ServerException, IOException {
        return getElementsById(elements.stream().map(Converters.getElementToIdConverter()).filter(id -> id != null).collect(Collectors.toList()), ps);
    }

    public static ObjectNode getElementsById(Collection<String> ids, ProgressStatus ps) throws ServerException, IOException {
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
        
        final String url = ExportUtility.getUrlWithWorkspace() + "/elements";
        final String jsonRequest = JacksonUtils.getObjectMapper().writeValueAsString(requests);
        Utils.guilog("[INFO] Searching for " + ids.size() + " elements from server...");

        final AtomicReference<String> res = new AtomicReference<>();
        final AtomicReference<Integer> code = new AtomicReference<>();
        Thread t = new Thread(() -> {
        /*try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
            String response;
            try {
                response = ExportUtility.getWithBody(url, jsonRequest);
                res.set(response);
                code.set(200);
            } catch (ServerException ex) {
                code.set(ex.getCode());
                if (ex.getCode() != 404) {
                    res.set(ex.getResponse());
                }
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

        String response = res.get();
        if (code.get() != 404 && code.get() != 200) {
            throw new ServerException(response, code.get());
        }
        Utils.guilog("[INFO] Finished getting elements.");
        
        if (response == null) {
            ObjectNode empty = JacksonUtils.getObjectMapper().createObjectNode();
            empty.putArray("elements");
            return empty;
        }

        ObjectNode responseJson = null;
        responseJson = JacksonUtils.getObjectMapper().readValue(response, ObjectNode.class);
        
        if (responseJson == null) {
            responseJson = JacksonUtils.getObjectMapper().createObjectNode();
            responseJson.putArray("elements");
        }
        return responseJson;
    }
    
    public static String sendGet(URI requestUri) {
        String response = "";
        BufferedReader in = null;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) requestUri.toURL().openConnection();
            connection.setRequestMethod("GET");

            connection.setRequestProperty("Authorization", "Basic " + ViewEditUtils.getAuthStringEnc());
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
            connection.setRequestProperty("Authorization", "Basic " + ViewEditUtils.getAuthStringEnc());
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
        
    public static URIBuilder getBaseUri(Project project) {
        URIBuilder uri = new URIBuilder();
        
        Model primaryModel = project.getModel();
        if (project == null || primaryModel == null) {
            return uri;
        }
        
        String urlString = getServerUrl(project);
        
        // [scheme:][//authority][path][?query][#fragment]
        String uriScheme = urlString.substring(0, urlString.indexOf(':'));
        String uriHost = urlString.substring(urlString.indexOf("://") + 3);
        String uriPath = "/alfresco/service";
        
        uri.setScheme(uriScheme);
        uri.setHost(uriHost.trim());
        uri.setPath(uriPath);
        return uri;
    }
    
    public static URIBuilder getSiteUri(Project project) {
        URIBuilder siteUri = getBaseUri(project);
        String siteString = getSiteName(project);
        siteUri.setPath(siteUri.getPath() + "/" + siteString);
        return siteUri;
    }
    
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

