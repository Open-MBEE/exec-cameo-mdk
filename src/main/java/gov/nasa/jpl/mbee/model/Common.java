package gov.nasa.jpl.mbee.model;

import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBHasContent;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTableEntry;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

/**
 * Common is a collection of utility functions for creating DocumentElements
 * from model Elements.
 */
public class Common {

    public static void addReferenceToDBHasContent(Reference ref, DBHasContent parent) {
        parent.addElements(getReferenceAsDocumentElements(ref));
    }

    public static List<DocumentElement> getReferenceAsDocumentElements(Reference ref) {
        List<DocumentElement> res = new ArrayList<DocumentElement>();
        if (ref.result == null)
            return res;
        if (!ref.isResultEditable()) {
            if (ref.result instanceof Collection) {
                for (Object r: (Collection)ref.result) {
                    res.add(new DBParagraph(r));
                }
            } else {
                res.add(new DBParagraph(ref.result));
            }
        } else {
            if (ref.result instanceof Collection && !((Collection)ref.result).isEmpty()) {
                res.add(new DBParagraph(((Collection)ref.result).iterator().next(), ref.element, ref.from));
            } else {
                res.add(new DBParagraph(ref.result, ref.element, ref.from));
            }
        }
        return res;
    }

    /**
     * This set is used to prevent infinite recursion while traversing nested
     * collections of model elements.
     */
    public static Set<Object> seen = Collections.synchronizedSet(new HashSet<Object>());

    public static DBTableEntry getStereotypePropertyEntry(Element e, Property p) {
        DBTableEntry res = new DBTableEntry();
        addReferenceToDBHasContent(Reference.getPropertyReference(e, p), res);
        return res;
    }

    /**
     * This assumes no context for what object is or how it can be editable,
     * should not be used except in old queries like GenericTable
     * 
     * @param o
     * @return
     */
    public static DBTableEntry getTableEntryFromObject(Object o) {
        DBTableEntry res = new DBTableEntry();
        if (o instanceof Collection) {
            for (Object r: (Collection)o) {
                res.addElement(new DBParagraph(r));
            }
        } else
            res.addElement(new DBParagraph(o));
        return res;

    }
}
