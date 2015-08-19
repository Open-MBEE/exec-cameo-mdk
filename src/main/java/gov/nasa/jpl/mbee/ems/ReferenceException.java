package gov.nasa.jpl.mbee.ems;

import org.json.simple.JSONObject;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ReferenceException extends ImportException {

    public ReferenceException(Element e, JSONObject json, String message) {
        super(e, json, message);
    }
}
