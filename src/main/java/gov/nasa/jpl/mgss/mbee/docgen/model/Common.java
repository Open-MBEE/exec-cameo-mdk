package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.DocGenUtils;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBList;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBSimpleList;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTableEntry;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;

public class Common {
  public static HashSet<Object> seen = new HashSet< Object >(); 

	public static DBTableEntry getStereotypePropertyEntry(Element e, Property p, boolean forViewEditor) {
		List<Object> results = Utils.getStereotypePropertyValues(e, p);
		return getTableEntryFromList(results, false, forViewEditor);
	}
	
	public static DBTableEntry getTableEntryFromList(List<Object> results, boolean simple, boolean forViewEditor) {
		DBTableEntry entry = new DBTableEntry();
		if (simple) {
			DBSimpleList sl = new DBSimpleList();
			sl.setContent(results);
			entry.addElement(sl);
		} else {
			DBList parent = new DBList();
			for (Object o: results) {
			  parent.addElement( getDBTextFromObject( o, simple, forViewEditor ) );
			}
			entry.addElement(parent);
		}
		
		return entry;
		
	}

  public static DBText getDBTextFromObject( Object result, boolean simple,
                                            boolean forViewEditor ) {
    if ( simple && false ) { // TODO -- make always false since simple case is
                             // not implemented
      // TODO -- REVIEW -- Should we do something different here for simple ==
      // true?
    }
    if ( forViewEditor ) return new DBText( DocGenUtils.fixString( result,
                                                                   false ) );
    else return new DBText( DocGenUtils.addDocbook( DocGenUtils.fixString( result ) ) );
  }
	
  public static DBTableEntry getTableEntryFromObject( Object result, boolean simple,
                                                 boolean forViewEditor ) {
    // TODO -- REVIEW -- could make this recursive to get lists of lists by
    // having getEntryFromList() above call this method for each list element.

    boolean saw = seen.contains( result );

    // Check to see if the result is a Collection.
    Collection<?> c = null;
    if ( result instanceof Collection ) {
      c = (Collection<?>)result;
    } else if ( !saw && result instanceof Slot ) {
      c = Utils.getSlotValues( (Slot)result );
    }
    if ( c != null ) {
      // If there's only one object in the collection, forget that it's a list,
      // and create the entry for the one object.
      if (c.size() == 1) {
        Object newResult = c.iterator().next();
        seen.add( result );
        DBTableEntry entry = getTableEntryFromObject( newResult, simple, forViewEditor );
        seen.remove( result );
        return entry;
      } else if ( !saw ) {
        // Get a list entry.
        seen.add( result );
        ArrayList<Object> results = new ArrayList< Object >(c);
        DBTableEntry entry = getTableEntryFromList( results, simple, forViewEditor );
        seen.remove( result );
        return entry;
      }
    }
    
    
    // Object is not a collection.
    DBTableEntry entry = new DBTableEntry();
    if ( result instanceof DocumentElement ) {
      entry.addElement( (DocumentElement)result ); 
    } else if ( result instanceof DocGenElement && false) { // TODO -- remove false
      // TODO -- need to push a table entry && call docBookOutputVisitor.visit(result);
    } else {
      if ( simple && false ) { // TODO -- make always false since simple case is
                               // not implemented
        // TODO -- REVIEW -- Should we do something different here for simple ==
        // true?
      } else {
        if ( forViewEditor ) entry.addElement( new DBText( DocGenUtils.fixString( result,
                                                                                  false ) ) );
        else entry.addElement( new DBText( DocGenUtils.addDocbook( DocGenUtils.fixString( result ) ) ) );
      }
    }
    return entry;
  }
}
