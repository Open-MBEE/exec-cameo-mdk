package gov.nasa.jpl.mbee.mdk.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.ems.MMSUtils;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

//import org.json.simple.JSONObject;
//import org.json.simple.JSONValue;

public class TimeQueryUtil {
    private static ObjectNode result;

    // query server for timestamp version:
    // https://fn-cae-ems.jpl.nasa.gov/alfresco/services/workspaces/master/tmt/elements/_18_0_2_baa02e2_1422996003330_165733_91914?timestamp=2015-12-13T16:21:06.797-0700
    // compare elements

    public static ObjectNode getHistoryOfElement(Project project, Element elementToQuery, Date compareToTime) {
        ArrayList<Element> elementsToQuery = new ArrayList<Element>();
        elementsToQuery.add(elementToQuery);

        result = JacksonUtils.getObjectMapper().createObjectNode();
        ArrayNode elements = result.putArray("elements");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        URIBuilder requestUri = MMSUtils.getServiceWorkspacesElementsUri(project);
        if (requestUri == null) {
            return null;
        }
        String basePath = requestUri.getPath();
        requestUri.setParameter("timestamp", "");

        GUILog log = Application.getInstance().getGUILog();
        Utils.guilog("[INFO] Getting elements from server...");

        for (Element elem : elementsToQuery) {
            String id = elem.getID();
            if (elem == project) {
                id = Application.getInstance().getProject().getPrimaryProject().getProjectID();
            }
            id = id.replace(".", "%2E");
            requestUri.setPath(basePath + "/" + id);
            requestUri.setParameter("timestamp", sdf.format(compareToTime));

            ObjectNode partialResponse = null;
            try {
                partialResponse = MMSUtils.sendMMSRequest(MMSUtils.buildRequest(MMSUtils.HttpRequestType.GET, requestUri));
            } catch (IOException | ServerException | URISyntaxException e) {
                //TODO add exception handling for partial element returns?
                e.printStackTrace();
            }
            JsonNode value;
            if (partialResponse != null && (value = partialResponse.get("elements")) != null && value.isArray()) {
                elements.addAll((ArrayNode) value);
            }
        }
        return result;
    }

}
