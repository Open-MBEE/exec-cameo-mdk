package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mgss.mbee.docgen.DocGenUtils;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBHasContent;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBList;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBSimpleList;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTableEntry;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.From;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;

/**
 * Common is a collection of utility functions for creating DocumentElements
 * from model Elements.
 */
public class Common {
 
    public static void addReferenceToDBHasContent(Reference ref, DBHasContent parent) {
		if (ref.result == null)
			return;
		//view editor currently does not support editing values where multiplicity > 1
		if (ref.element == null || ref.from == null || (ref.result instanceof Collection && ((Collection)ref.result).size() > 1)) { 
			if (ref.result instanceof Collection) {
				for (Object res: (Collection)ref.result) {
					parent.addElement(new DBParagraph(res));
				}
			} else {
				parent.addElement(new DBParagraph(ref.result));
			}
		} else {
			if (ref.result instanceof Collection && !((Collection)ref.result).isEmpty()) {
				parent.addElement(new DBParagraph(((Collection)ref.result).iterator().next(), ref.element, ref.from));
			} else {
				parent.addElement(new DBParagraph(ref.result, ref.element, ref.from));
			}
		}
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
     * This assumes no context for what object is or how it can be editable, should not be used except in old queries like GenericTable
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
