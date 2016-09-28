package gov.nasa.jpl.mbee.mdk.ems;

import com.nomagic.task.ProgressStatus;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.ems.ExportUtility;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Created by igomes on 9/26/16.
 */
// TODO Use URI builder or similar @donbot
public class MMSUtils {
    public static JSONObject getElement(Element element) {
        return getElementById(Converters.getElementToIdConverter().apply(element));
    }

    public static JSONObject getElementById(String id) {
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
        JSONObject result = (JSONObject) JSONValue.parse(response);
        JSONArray elements = (JSONArray) result.get("elements");
        if (elements == null || elements.isEmpty()) {
            return null;
        }
        return (JSONObject) elements.get(0);
    }

    public static JSONObject getElements(Collection<Element> elements, ProgressStatus ps) throws ServerException {
        return getElementsById(elements.stream().map(Converters.getElementToIdConverter()).filter(id -> id != null).collect(Collectors.toList()), ps);
    }

    public static JSONObject getElementsById(Collection<String> ids, ProgressStatus ps) throws ServerException {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        JSONArray idsJSONArray = new JSONArray();
        for (String id : ids) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("sysmlId", id);
            idsJSONArray.add(jsonObject);
        }
        final JSONObject request = new JSONObject();
        request.put("elements", idsJSONArray);
        final String url = ExportUtility.getUrlWithWorkspace() + "/elements";
        Utils.guilog("[INFO] Searching for " + ids.size() + " elements from server...");

        final AtomicReference<String> res = new AtomicReference<>();
        final AtomicReference<Integer> code = new AtomicReference<>();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
            /*try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
                String response;
                try {
                    response = ExportUtility.getWithBody(url, request.toJSONString());
                    res.set(response);
                    code.set(200);
                } catch (ServerException ex) {
                    code.set(ex.getCode());
                    if (ex.getCode() != 404) {
                        res.set(ex.getResponse());
                    }
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
            JSONObject reso = new JSONObject();
            reso.put("elements", new JSONArray());
            return reso;
        }
        return (JSONObject) JSONValue.parse(response);
    }
}
