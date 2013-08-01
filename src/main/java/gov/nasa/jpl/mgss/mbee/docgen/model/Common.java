package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.DocGenUtils;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBList;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBSimpleList;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTableEntry;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

public class Common {

	public static DBTableEntry getStereotypePropertyEntry(Element e, Property p, boolean forViewEditor) {
		List<Object> results = Utils.getStereotypePropertyValues(e, p);
		return getEntryFromList(results, false, forViewEditor);
	}
	
	public static DBTableEntry getEntryFromList(List<Object> results, boolean simple, boolean forViewEditor) {
		DBTableEntry entry = new DBTableEntry();
		if (simple) {
			DBSimpleList sl = new DBSimpleList();
			sl.setContent(results);
			entry.addElement(sl);
		} else {
			DBList parent = new DBList();
			for (Object o: results) {
		    // TODO -- REVIEW -- could make this recursive to get lists of lists by
		    // calling getEntryFromObject() instead of below.
				if (forViewEditor)
					parent.addElement(new DBText(DocGenUtils.fixString(o, false)));
				else
					parent.addElement(new DBText(DocGenUtils.addDocbook(DocGenUtils.fixString(o))));
			}
			entry.addElement(parent);
		}
		
		return entry;
		
	}
	
  public static DBTableEntry getEntryFromObject( Object result, boolean simple,
                                                 boolean forViewEditor ) {
    // TODO -- REVIEW -- could make this recursive to get lists of lists by
    // having getEntryFromList() above call this method for each list element.

    // Check to see if the result is a Collection.
    if ( result instanceof Collection ) {
      Collection<?> c = (Collection<?>)result;
      // If there's only one object in the collection, forget that it's a list,
      // and create the entry for the one object.
      if (c.size() == 1) {
        result = c.iterator().next();
      } else {
        // Get a list entry.
        ArrayList<Object> results = new ArrayList< Object >(c);
        return getEntryFromList( results, simple, forViewEditor );
      }
    }
    
    // Object is not a collection.
    DBTableEntry entry = new DBTableEntry();
    if (simple && false) { // TODO -- make always false since simple case is not implemented
      // TODO -- REVIEW -- Should we do something different here for simple == true?
    } else {
      if (forViewEditor)
        entry.addElement(new DBText(DocGenUtils.fixString(result, false)));
      else
        entry.addElement(new DBText(DocGenUtils.addDocbook(DocGenUtils.fixString(result))));
    }
    return entry;
  }
}
