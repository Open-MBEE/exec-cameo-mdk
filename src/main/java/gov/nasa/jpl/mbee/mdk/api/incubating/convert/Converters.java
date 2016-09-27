package gov.nasa.jpl.mbee.mdk.api.incubating.convert;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.ems.emf.EMFExporter;
import gov.nasa.jpl.mbee.mdk.ems.emf.EMFImporter2;
import org.json.simple.JSONObject;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by igomes on 9/15/16.
 */
// TODO Lombok? @donbot
public class Converters {

    private static BiFunction<Element, Project, JSONObject> ELEMENT_TO_JSON_CONVERTER;
    private static ElementToJsonFunction JSON_TO_ELEMENT_CONVERTER;
    private static Function<Element, String> ELEMENT_TO_ID_CONVERTER;
    private static BiFunction<String, Project, Element> ID_TO_ELEMENT_CONVERTER;

    public static BiFunction<Element, Project, JSONObject> getElementToJsonConverter() {
        if (ELEMENT_TO_JSON_CONVERTER == null) {
            ELEMENT_TO_JSON_CONVERTER = new EMFExporter();
        }
        return ELEMENT_TO_JSON_CONVERTER;
    }

    public static ElementToJsonFunction getJsonToElementConverter() {
        if (JSON_TO_ELEMENT_CONVERTER == null) {
            JSON_TO_ELEMENT_CONVERTER = new EMFImporter2();
        }
        return JSON_TO_ELEMENT_CONVERTER;
    }

    public static Function<Element, String> getElementToIdConverter() {
        if (ELEMENT_TO_ID_CONVERTER == null) {
            ELEMENT_TO_ID_CONVERTER = EMFExporter::getEID;
        }
        return ELEMENT_TO_ID_CONVERTER;
    }

    public static BiFunction<String, Project, Element> getIdToElementConverter() {
        if (ID_TO_ELEMENT_CONVERTER == null) {
            ID_TO_ELEMENT_CONVERTER = EMFImporter2.ELEMENT_LOOKUP_FUNCTION;
        }
        return ID_TO_ELEMENT_CONVERTER;
    }
}
