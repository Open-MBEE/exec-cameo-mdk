package gov.nasa.jpl.mbee.mdk.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ImportException extends Exception {

    private final Element element;
    private final JsonNode jsonNode;

    public ImportException(Element element, JsonNode jsonNode, String message) {
        super(message);
        this.element = element;
        this.jsonNode = jsonNode;
    }

    public ImportException(Element element, JsonNode jsonNode, String message, Throwable cause) {
        super(message, cause);
        this.element = element;
        this.jsonNode = jsonNode;
    }

    public JsonNode getJsonNode() {
        return jsonNode;
    }

    public Element getElement() {
        return element;
    }

    @Override
    public void printStackTrace() {
        super.printStackTrace();
        System.err.println("Element: " + element);
        System.err.println("ObjectNode: " + jsonNode);
    }
}
