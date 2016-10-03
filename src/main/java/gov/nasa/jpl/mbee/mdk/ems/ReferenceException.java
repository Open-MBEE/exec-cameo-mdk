package gov.nasa.jpl.mbee.mdk.ems;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ReferenceException extends ImportException {

    public ReferenceException(Element e, ObjectNode objectNode, String message) {
        super(e, objectNode, message);
    }
}
