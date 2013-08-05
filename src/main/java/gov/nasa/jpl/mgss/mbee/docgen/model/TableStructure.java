package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.ocl.OclEvaluator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Iterator;

import org.eclipse.emf.ecore.EObject;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.EnumerationLiteral;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralInteger;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class TableStructure extends Table implements Iterator<List<Object>>{

	//TODO decide tag options
	
	private List<String> headers;
	private List<Stereotype> outgoing;
	private List<Stereotype> incoming;
	private boolean skipIfNoDoc;
	private List<List<Object>> table;
	
	public void setSkipIfNoDoc(boolean b) {
		skipIfNoDoc = b;
	}
	
	public void setHeaders(List<String> d) {
		headers = d;
	}
	
	public void setOutgoing(List<Stereotype> s) {
		outgoing = s;
	}
	
	public void setIncoming(List<Stereotype> s) {
		incoming = s;
	}

	public List<String> getHeaders() {
		return headers;
	}

	public List<Stereotype> getOutgoing() {
		return outgoing;
	}

	public List<Stereotype> getIncoming() {
		return incoming;
	}

	public boolean isSkipIfNoDoc() {
		return skipIfNoDoc;
	}
	
	// Table Stuff

	private Object nullEntry = new String("no entry");
	private int sortCol; // column index+1, neg means inv. order, 0 is no sort
	private int cLen;
	
	public TableStructure() {
		cLen = 0;
		sortCol = 0;
		table = new ArrayList<List<Object>>();
	}
	
	public int getColumNum() {
		return cLen;
	}
	
	public List<List<Object>> getTable() {
		return table;
	}

	// Doesn't account for column length differences
	public void setTable(List<List<Object>> t) {
		table = t;
	}
	
	// Equalizes column lengths
	public void addColumn(List<Object> c) {
		if (c != null) {
			table.add(c);
			if (c.size() > cLen) 
				cLen = c.size();
			for (List<Object> x: table)
				while (x.size() < cLen)
					x.add(nullEntry);
		} else {
			table.add(new ArrayList<Object>());
		}
	}
	
	public List<Object> getColumn(int n) {
		if (n < table.size() && n >= 0) 
			return table.get(n); 
		else
			return null;
	}
	
	// IMPORTANT INVARIANT: Assumes all columns given are equally sized
	public void addRow(List<Object> r) {
		if (table.size() < r.size()) {
			List<Object> empty = new ArrayList<Object>();
			if (table.size() > 0)  						
				for (int i = 0; i < table.get(0).size(); i++)
					empty.add(nullEntry);
			for (int i = table.size(); i < r.size(); i++) 
				table.add(empty);
			
		} else if (r.size() < table.size()) {
			for (int i = r.size(); i < table.size(); i++) 
				r.add(0, nullEntry);
		}
		for (int i = 0; i < r.size(); i++) 
			table.get(i).add(r.get(i)); //?
		cLen++;
	}

	// Makes same assumption as addRow
	public List<Object> getRow(int n) {
		if (table.get(0) == null || n >= table.get(0).size())
			return null;
		List<Object> output = new ArrayList<Object>();
		for (int i = 0; i < table.size(); i++)
			output.add(table.get(i).get(n));
		return output;
	}

	// Fancy Table Operations

	String irrelevantEntry = new String("--");

	GUILog parseTS = Application.getInstance().getGUILog(); // debug! TODO: remove
	boolean debug = true;
	
  // TODO -- REVIEW -- Should parse*Column() and handle*Cell() methods not
  // evaluate expressions or find property values and do this in
  // DocBookOutputVisitor like other tables?
	
  public void parseExpressionColumn( ActivityNode curNode, Object expression,
                                     List< Element > rows ) {
    String exprString = null;
    if ( expression instanceof String ) {
      exprString = (String)expression;
    } else if ( expression instanceof List ) {
      List expList = (List)expression;
      if ( expList.size() == 1 ) {
        parseExpressionColumn( curNode, expList.get(0), rows );
        return;
      } else {
        Debug.error( "Error! TableExpressionColumn expression cannot be a list of multiple elements!" );
      }
    } else {
      exprString = (String)expression.toString();
    }
    
    List<Object> curCol = new ArrayList<Object>();
    
    for (Object r: rows) {
      if (r instanceof EObject) {
        curCol.add(OclEvaluator.evaluateQuery( (EObject)r, exprString ) );
      } else if (r instanceof List<?>) {
        for (Object c: (List<Object>)r) {
          Object result = c;
          if (c instanceof EObject) {
            result = OclEvaluator.evaluateQuery( (EObject)c, exprString );
          } else {
            // REVIEW -- TODO -- should be able to apply query to any object!
            Debug.error( "Error! TableExpressionColumn requires an EObject!" );
            result = c.toString();
          }
          if ( result instanceof List ) {
            curCol.add( result );
          } else {
            curCol.add( Utils2.newList( result ) );
          }
        }
      } else {
        curCol.add( Utils2.newList( r.toString() ) );
      }
    }
    addColumn(curCol);
  }

  public static final String propertyColumn = "PROPERTY";
  public static final String attributeColumn = "ATTRIBUTE";
  
  @SuppressWarnings("unchecked")
	public void parseColumn(Object dThing, List<?> rows, String colType) {
		List<Object> curCol = new ArrayList<Object>();
		if (dThing == null || colType == null) 
			curCol.addAll(rows); // REVIEW -- does this need to be a list of lists?
		else
			for (Object r: rows) 
				if (r instanceof Element)
					if (colType.equals(propertyColumn)) curCol.add(handlePropertyCell((Property)dThing, (Element)r));
					else if (colType.equals(attributeColumn)) curCol.add(handleAttributeCell(dThing, (Element)r));
				else if (r instanceof List<?>) {
					List<Object> superCell = new ArrayList<Object>();
					for (Object c: (List<Object>)r)
						if (c instanceof Element)
							if (colType.equals(propertyColumn)) superCell.addAll(handlePropertyCell((Property)dThing, (Element)c));
							else if (colType.equals(attributeColumn)) superCell.addAll(handleAttributeCell(dThing, (Element)c));
						else 
							superCell.addAll( Utils2.newList( c ) );
					curCol.add(superCell);
				} else {
					curCol.add( Utils2.newList( r ) );
				}
		
		addColumn(curCol);
	}
	
	private List<Object> handlePropertyCell(Property dProp, Element cell) {
		Element myOwner = dProp.getOwner();
		List<Object> rSlots = new ArrayList<Object>();
		if (myOwner instanceof Stereotype) {
			ValueSpecification pDefault = null;
			if (dProp != null) {
				rSlots = StereotypesHelper.getStereotypePropertyValue(cell, (Stereotype)myOwner, (Property)dProp);
				pDefault = dProp.getDefaultValue();
			}
			if (rSlots.isEmpty() && pDefault != null) 
				rSlots.add(pDefault);
			return rSlots;
		}
		Collection<Element> rOwned = cell.getOwnedElement();
		for (Object o: rOwned)
			if (((Element)o) instanceof Property && ((Property)o).getName().equals(dProp.getName()))
				rSlots.add((Object)((Property)o).getDefaultValue());
		return rSlots;
	}
	
	public List<Object> handleAttributeCell(Object dAttr, Element cell) { 
		List<Object> rSlots = new ArrayList<Object>();
		if (dAttr != null && ((EnumerationLiteral)dAttr).getName().equals("Name"))
			if (cell instanceof NamedElement)
				rSlots.add(((NamedElement)cell).getName());
			else
				rSlots.add(cell.getHumanName());
		if (dAttr != null && ((EnumerationLiteral)dAttr).getName().equals("Documentation"))
			rSlots.add(ModelHelper.getComment(cell));
		if (dAttr != null && ((EnumerationLiteral)dAttr).getName().equals("Value"))
			if (cell instanceof Property)
				rSlots.add(((Property)cell).getDefaultValue());
			else if (cell instanceof Slot)
				rSlots.add(((Slot)cell).getValue());
		// these don't work yet
//		if (((String)dProp).equals("Outgoing Relationships"))
//			for (Object o: cell.get_directedRelationshipOfSource())
//				if (o instanceof DirectedRelationship && ((DirectedRelationship)o).getSource().contains((Element)o))
//					rSlots.add(o);
//		if (((String)dProp).equals("Incoming Relationships"))
//			for (Object o:  cell.get_directedRelationshipOfTarget())
//				if (o instanceof DirectedRelationship && ((DirectedRelationship)o).getTarget().contains((Element)o))
//					rSlots.add(o);
		// Get all the properties or the default value
		return rSlots;
	}
	
	@SuppressWarnings("unchecked")
	public void addSumRow() {
		List<Object> sumRow = new ArrayList<Object>();
		double f;
		boolean foundSumable = false;
		for (List<Object> c: table) {
			f = 0;
			for (Object l: c)
				if (l instanceof List<?>)
					for (Object item: (List<Object>)l) {
						if (item instanceof Float || item instanceof Double || item instanceof Integer) {
							foundSumable = true;
							f += (Double)item;
						} else if (item instanceof LiteralInteger) {
							foundSumable = true;
							f += new Double(ModelHelper.getValueString((ValueSpecification)item));
						}
					}
			List<Object> bucket = new ArrayList<Object>();
			if (foundSumable) bucket.add(f);
			else bucket.add(irrelevantEntry);
			sumRow.add(bucket);
			foundSumable = false;
		}
		addRow(sumRow);
	}
	
//	public void addSortColumn()	
	
	// Iterator Stuff

	private int itercount = 0;
	
	public boolean hasNext() {
		return table.size()>0?(table.get(0).size() > itercount):false;
	}
	
	public List<Object> next() {
		List<Object> out = getRow(itercount);
		itercount++;
		return out;
	}
	
	public void remove() {
		return;
	}
	
	// Utility Stuff
	
	private Object getObjectProperty(Element e, String stereotype, String property, Object defaultt) {
		Object value = StereotypesHelper.getStereotypePropertyFirst(e, stereotype, property);
		if (value == null && e instanceof CallBehaviorAction && ((CallBehaviorAction)e).getBehavior() != null) {
			value = StereotypesHelper.getStereotypePropertyFirst(((CallBehaviorAction)e).getBehavior(), stereotype, property);
		}
		if (value == null)
			value = defaultt;
		return value;
	}
	
	// Debugging relevant stuff
	
	public String toString() {
		//determine longest row
		int biggest=0;
		if (table != null)
			for (List<Object> c: table)
				biggest = (c.size() > biggest) ? c.size() : biggest;
		//add lines to the string
		String output = new String();
		for (int n = 0; n < biggest; n++) {
			for (List<Object> c: table) {
				if (c.size() <= n)
					output.concat("| null\t");
				else
					output.concat("| " + c.toString() + "\t");
			}
			output.concat("||\n");
		}	
		return output;
	}
	
	// For DocBookOutputVisitor Stuff
	
	@Override
	public void accept(IModelVisitor v) {
		v.visit(this);
		
	}
}
