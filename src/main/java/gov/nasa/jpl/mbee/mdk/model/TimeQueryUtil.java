package gov.nasa.jpl.mbee.mdk.model;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.ems.ExportUtility;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
import gov.nasa.jpl.mbee.mdk.ems.validation.ResultHolder;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TimeQueryUtil {
    private static JSONObject result;

    // query server for timestamp version:
    // https://fn-cae-ems.jpl.nasa.gov/alfresco/services/workspaces/master/tmt/elements/_18_0_2_baa02e2_1422996003330_165733_91914?timestamp=2015-12-13T16:21:06.797-0700
    // compare elements

    public static JSONObject getHistoryOfElement(Element elementToQuery, Date compareToTime) {
        ArrayList<Element> elementsToQuery = new ArrayList<Element>();
        elementsToQuery.add(elementToQuery);

        String url = ExportUtility.getUrlWithWorkspace();
        if (url == null) {
            return null;
        }
        String response = null;
        JSONArray elements = new JSONArray();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        for (Element start : elementsToQuery) {
            String id = start.getID();
            id = "testtimestamps_cover";
            if (start == Application.getInstance().getProject().getModel()) {
                id = Application.getInstance().getProject().getPrimaryProject().getProjectID();
            }
            id = id.replace(".", "%2E");
            final String url2;
            String time = sdf.format(compareToTime);
            url2 = url + "/elements/" + id + "?timestamp=" + time + "&qualified=false";

            GUILog log = Application.getInstance().getGUILog();
            Utils.guilog("[INFO] Getting elements from server...");

            String tres = null;
            try {
                tres = ExportUtility.get(url2, false);
            } catch (ServerException ex) {
            }

            JSONObject partialResult = (JSONObject) JSONValue.parse(tres);
            if (partialResult != null && partialResult.containsKey("elements")) {
                elements.addAll((JSONArray) partialResult.get("elements"));
            }
        }
        result = new JSONObject();
        result.put("elements", elements);
        ResultHolder.lastResults = result;
        return result;
    }

}
