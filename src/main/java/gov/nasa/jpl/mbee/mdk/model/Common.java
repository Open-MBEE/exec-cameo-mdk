package gov.nasa.jpl.mbee.mdk.model;

import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenProfile;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DBHasContent;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DBTableEntry;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.stereotypes.EditableChoosable;

import java.util.*;

/**
 * Common is a collection of utility functions for creating DocumentElements
 * from model Elements.
 */
public class Common {

    public static void addReferenceToDBHasContent(Reference ref, DBHasContent parent, Query query) {
        parent.addElements(getReferenceAsDocumentElements(ref, query));
    }

    public static void addReferenceToDBHasContent(Reference ref, DBHasContent parent, Boolean editable) {
        parent.addElements(getReferenceAsDocumentElements(ref, editable));
    }

    public static List<DocumentElement> getReferenceAsDocumentElements(Reference ref, Query query) {
        Boolean editable = null;
        Object o;
        if (query != null && query.getDgElement() != null && (o = StereotypesHelper.getStereotypePropertyFirst(query.getDgElement(), DocGenProfile.editableChoosable, "editable")) instanceof Boolean) {
            editable = (Boolean) o;
        }
        return getReferenceAsDocumentElements(ref, editable);
    }

    public static List<DocumentElement> getReferenceAsDocumentElements(Reference ref, Boolean editable) {
        List<DocumentElement> res = new ArrayList<DocumentElement>();
        if (ref.result == null) {
            return res;
        }
        if (!ref.isResultEditable()) {
            if (ref.result instanceof Collection) {
                for (Object r : (Collection<?>) ref.result) {
                    DocumentElement documentElement = new DBParagraph(r);
                    initEditable(documentElement, editable);
                    res.add(documentElement);
                }
            }
            else {
                DocumentElement documentElement = new DBParagraph(ref.result);
                initEditable(documentElement, editable);
                res.add(documentElement);
            }
        }
        else {
            //if (ref.result instanceof Collection && !((Collection<?>)ref.result).isEmpty()) {
            //    res.add(new DBParagraph(((Collection<?>)ref.result).iterator().next(), ref.element, ref.from));
            //} else {
            DocumentElement documentElement = new DBParagraph(ref.result, ref.element, ref.from);
            initEditable(documentElement, editable);
            res.add(documentElement);
            //}
        }
        return res;
    }

    /**
     * This set is used to prevent infinite recursion while traversing nested
     * collections of model elements.
     */
    public static Set<Object> seen = Collections.synchronizedSet(new HashSet<Object>());

    public static DBTableEntry getStereotypePropertyEntry(Element e, Property p, Query query) {
        DBTableEntry res = new DBTableEntry();
        addReferenceToDBHasContent(Reference.getPropertyReference(e, p), res, query);
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
            for (Object r : (Collection<?>) o) {
                res.addElement(new DBParagraph(r));
            }
        }
        else {
            res.addElement(new DBParagraph(o));
        }
        return res;
    }

    private static void initEditable(DocumentElement documentElement, Boolean editable) {
        if (documentElement instanceof EditableChoosable && editable != null) {
            ((EditableChoosable) documentElement).setEditable(editable);
        }
    }
}
