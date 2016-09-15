package gov.nasa.jpl.mbee.api.incubating.json;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.ems.EMFExporter;
import gov.nasa.jpl.mbee.ems.EMFImporter;
import org.json.simple.JSONObject;

import java.util.function.Function;

/**
 * Created by igomes on 9/15/16.
 */
// TODO Lombok? @donbot
public class JsonConverters {

    private static Function<Element, JSONObject> TO_JSON_CONVERTER;
    private static Function<JSONObject, Element> FROM_JSON_CONVERTER;

    public static Function<Element, JSONObject> getToJsonConverter() {
        if (TO_JSON_CONVERTER == null) {
            TO_JSON_CONVERTER = new EMFExporter();
        }
        return TO_JSON_CONVERTER;
    }

    public static Function<JSONObject, Element> getFromJsonConverter() {
        if (FROM_JSON_CONVERTER == null) {
            FROM_JSON_CONVERTER = new EMFImporter();
        }
        return FROM_JSON_CONVERTER;
    }
}
