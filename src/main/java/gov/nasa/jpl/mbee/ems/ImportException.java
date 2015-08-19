package gov.nasa.jpl.mbee.ems;

import org.json.simple.JSONObject;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ImportException extends Exception {

    private Element e;
    private JSONObject json;
    public ImportException(Element e, JSONObject json, String message) {
        super(message);
        this.setElement(e);
        this.setJson(json);
    }
    public JSONObject getJson() {
        return json;
    }
    public void setJson(JSONObject json) {
        this.json = json;
    }
    public Element getElement() {
        return e;
    }
    public void setElement(Element e) {
        this.e = e;
    }
}
