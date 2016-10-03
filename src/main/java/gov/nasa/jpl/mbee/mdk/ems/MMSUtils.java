package gov.nasa.jpl.mbee.mdk.ems;

import com.nomagic.task.ProgressStatus;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.ems.ExportUtility;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.lib.Utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Created by igomes on 9/26/16.
 */
// TODO Use URI builder or similar @donbot
public class MMSUtils {
    
    private static final ObjectMapper mapper = JacksonUtils.getObjectMapper();
    
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
        
        ObjectNode responseJson = mapper.readValue(response, ObjectNode.class);
        ArrayNode elements = (ArrayNode) responseJson.get("elements");
        if (elements == null || elements.size() == 0  ) {
            return null;
        }
        
        return (ObjectNode) elements.get(0);
    }

    public static ObjectNode getElements(Collection<Element> elements, ProgressStatus ps) throws ServerException, JsonProcessingException, JsonParseException, JsonMappingException, IOException {
        return getElementsById(elements.stream().map(Converters.getElementToIdConverter()).filter(id -> id != null).collect(Collectors.toList()), ps);
    }

    public static ObjectNode getElementsById(Collection<String> ids, ProgressStatus ps) throws ServerException, JsonProcessingException, JsonParseException, JsonMappingException, IOException {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        
        // create requests json
        final ObjectNode requests = mapper.createObjectNode();
        // put elements array inside request json, keep reference
        ArrayNode idsArrayNode = requests.putArray("elements");
        for (String id : ids) {
            // create json for id strings, add to request array
            ObjectNode element = mapper.createObjectNode();
            element.put(MDKConstants.SYSML_ID_KEY, id);
            idsArrayNode.add(element);
        }
        
        final String url = ExportUtility.getUrlWithWorkspace() + "/elements";
        final String jsonRequest = mapper.writeValueAsString(requests);
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
                    response = ExportUtility.getWithBody(url, jsonRequest);
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
            ObjectNode empty = mapper.createObjectNode();
            empty.putArray("elements");
            return empty;
        }

        ObjectNode responseJson = null;
        responseJson = mapper.readValue(response, ObjectNode.class);
        
        if (responseJson == null) {
            responseJson = mapper.createObjectNode();
            responseJson.putArray("elements");
        }
        return responseJson;

    }

}

