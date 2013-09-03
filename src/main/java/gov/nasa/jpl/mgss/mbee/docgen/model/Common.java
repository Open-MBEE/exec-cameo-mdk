package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.DocGenUtils;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBList;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBSimpleList;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTableEntry;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.From;
import gov.nasa.jpl.mgss.mbee.docgen.model.Common.Reference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;

public class Common {
 
  public static class Reference {
    		public Element element = null;
    		public From from = null;
            public Object result = null;
            public DocumentElement dbElement = null;
    
    		/**
    		 * @param element the source of the entry
             * @param from what aspect of the source to show
             * @param result the value to be shown
    		 * @param dbElement the DocBook entry
    		 */
    		public Reference(Element element, From from, Object result, DocumentElement dbElement) {
    			this.element = element;
                this.from = from;
                this.result = result;
    			this.dbElement = dbElement;
    		}
    		
            /**
             * @param element the value to be shown
             */
            public Reference(Element element) {
                this(element, From.DVALUE, element, null);
            }

            /**
             * @param object the value to be shown
             */
            public Reference(Object object) {
                if ( object instanceof Element ) {
                    this.element = (Element)object;
                }
                if ( object instanceof Slot ) {
                    Slot slot = (Slot)object;
                    this.element = slot.getOwner();
                    this.result = Utils.getSlotValues( (Slot)result );
                } else {
                    this.result = object;
                }
                this.from = From.DVALUE;
                this.dbElement = null;
            }

            @Override
            public String toString() {
                StringBuffer sb = new StringBuffer();
                sb.append( "(" );
                sb.append( "elem=" + ( element == null ? "null" : element.getHumanName() ) + ", "  );
                sb.append( "from=" + from + ", "  );
                sb.append( "result=" + result + ", "  );
                sb.append( "db=" + dbElement );
                sb.append( ")" );
                return sb.toString();
            }
            //		/**
    //		 * @param elements the source of the entry
    //		 * @param dbElement the DocBook entry
    //		 * @param from what aspect of the source to show
    //		 */
    //		public Entry(List<Object> elements, DocumentElement dbElement, From from) {
    //			super();
    //			this.elements = elements;
    //			this.dbElement = dbElement;
    //			this.from = from;
    //		}
    		
    		public static List<Reference> getEntries(Element element, From from, List<Object> results) {
    		    List<Reference> entries = new ArrayList<Reference>();
    		    for ( Object result : results ) {
    		        Element e = element;
    		        //if ( result instanceof Element ) e = (Element)result;
    		        List<Reference> c;
    		        List<Object> v;
                    if ( result instanceof Slot ) {
    		            Slot slot = (Slot)result;
    		            v = Utils.getSlotValues( slot );
    		            c = Common.Reference.getEntries( slot, From.DVALUE, v );
    		            entries.addAll(c);
    		        } else if ( result instanceof Property ) {
    		            Property property = (Property)result;
    		            v = Utils.getElementPropertyValues(element, property);
    		            c = Common.Reference.getEntries( property, From.DVALUE, v );
                        entries.addAll(c);
    		        } else {
    		            Reference ref = new Reference(e, from, result, null);
    		            entries.add(ref);
    		        }    		        
    		    }
    		    return entries;
    		}
    	}

    public static HashSet<Object> seen = new HashSet< Object >(); 

	public static DBTableEntry getStereotypePropertyEntry(Element e, Property p, boolean forViewEditor) {
		List<Object> results = Utils.getStereotypeProperty(e, p);
//		return getTableEntryFromList(results, false, forViewEditor);
//	    Slot slot = StereotypesHelper.getSlot(e, p, false);
        return getTableEntryFromObject(results, false, forViewEditor);
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

  public static DocumentElement getDBTextFromReference( Common.Reference reference,
                                                        boolean simple,
                                                        boolean forViewEditor ) {
      if ( reference == null ) reference = new Reference( null );
      String text = DocGenUtils.fixString( reference.result, !forViewEditor );
      DocumentElement dbText = null;
      if ( forViewEditor ) {
          if ( reference.element != null ) {
              dbText = new DBParagraph(text, reference.element, reference.from );
          } else {
              dbText = new DBParagraph(text);
          }
      } else {
          dbText = new DBText( DocGenUtils.addDocbook( text ) );
      }
      reference.dbElement = dbText;
      return dbText;
  }
  
  public static DocumentElement getDBTextFromObject( Object result, boolean simple,
                                                     boolean forViewEditor ) {
    if ( simple && false ) { // TODO -- make always false since simple case is
                             // not implemented
      // TODO -- REVIEW -- Should we do something different here for simple ==
      // true?
    }
    
    Common.Reference entry = (result instanceof Common.Reference ? (Common.Reference)result : null );
    if ( entry == null ) {
        entry = new Common.Reference( result );
    }
    return getDBTextFromReference(entry, simple, forViewEditor);
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
        Slot slot = (Slot)result;
        c = Common.Reference.getEntries( slot, From.DVALUE,
                                         Utils.getSlotValues( slot ) );
    } else if ( !saw && result instanceof Property ) {
        Property property = (Property)result;
        c = Common.Reference.getEntries( property, From.DVALUE,
                                         Utils.getElementPropertyValues(property.getOwner(), property) );
    }
    DBTableEntry entry = null;
    if ( c != null ) {
      // If there's only one object in the collection, forget that it's a list,
      // and create the entry for the one object.
      
      if (c.size() == 1) {
        Object newResult = c.iterator().next();
        seen.add( result );
        entry = getTableEntryFromObject( newResult, simple, forViewEditor );
        seen.remove( result );
      } else if ( !saw ) {
        // Get a list entry.
        seen.add( result );
        ArrayList<Object> results = new ArrayList< Object >(c);
        entry = getTableEntryFromList( results, simple, forViewEditor );
        seen.remove( result );
      }
      System.out.println("getTableEntryFromObject(" + result + ",...) = " + entry );
      return entry;
    }
    
    
    // Object is not a collection.
    entry = new DBTableEntry();
    if ( result instanceof DocumentElement ) {
      entry.addElement( (DocumentElement)result ); 
    } else if ( result instanceof DocGenElement && false) { // TODO -- remove false
      // TODO -- need to push a table entry && call docBookOutputVisitor.visit(result);
    } else {
      entry.addElement( getDBTextFromObject( result, simple, forViewEditor ) );
    }
    return entry;
  }
}
