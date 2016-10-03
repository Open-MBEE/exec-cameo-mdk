package gov.nasa.jpl.mbee.mdk.ems;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ImportException extends Exception {

    private final Element element;
    private final ObjectNode objectNode;

    public ImportException(Element element, ObjectNode objectNode, String message) {
        super(message);
        this.element = element;
        this.objectNode = objectNode;
    }

    public ImportException(Element element, ObjectNode objectNode, String message, Throwable cause) {
        super(message, cause);
        this.element = element;
        this.objectNode = objectNode;
    }

    public ObjectNode getObjectNode() {
        return objectNode;
    }

    public Element getElement() {
        return element;
    }
}
