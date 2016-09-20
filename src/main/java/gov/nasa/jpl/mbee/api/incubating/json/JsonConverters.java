package gov.nasa.jpl.mbee.api.incubating.json;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.ems.emf.EMFExporter;
import gov.nasa.jpl.mbee.ems.emf.EMFImporter2;
import org.json.simple.JSONObject;

import java.util.function.Function;

/**
 * Created by igomes on 9/15/16.
 */
// TODO Lombok? @donbot
public class JsonConverters {

    private static Function<Element, JSONObject> TO_JSON_CONVERTER;
    private static ToJsonFunction FROM_JSON_CONVERTER;

    public static Function<Element, JSONObject> getToJsonConverter() {
        if (TO_JSON_CONVERTER == null) {
            TO_JSON_CONVERTER = new EMFExporter();
        }
        return TO_JSON_CONVERTER;
    }

    public static ToJsonFunction getFromJsonConverter() {
        if (FROM_JSON_CONVERTER == null) {
            FROM_JSON_CONVERTER = new EMFImporter2();
        }
        return FROM_JSON_CONVERTER;
    }
}
