package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.DocGenUtils;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBColSpec;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTable;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTableEntry;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.generator.CollectFilterParser;
import gov.nasa.jpl.mgss.mbee.docgen.generator.Generatable;
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
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.ActivityEdge;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdstructuredactivities.StructuredActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.EnumerationLiteral;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralReal;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralInteger;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

/**
 * This class contains methods for parsing and visiting 
 * TableStructures and their contents.
 * 
 * @author bcompane
 *
 */

public class TableStructure extends Table implements Iterator<List<Object>> {

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

	// GENERATABLE INTERFACE CLASSES
	// ><><><><><><><><><><><><><><>
	
	private Element ts;
	private List<Element> rows;
	
	public void initialize(ActivityNode an, List<Element> in) {
		if (an instanceof StructuredActivityNode) {
			ts = (StructuredActivityNode)an;
		} else if (an instanceof CallBehaviorAction) {
			ts = ((CallBehaviorAction)an).getBehavior();
		} else {
			ts = null;
		}
		rows = in;
	}

	public void parse() {
		if (ts == null) return;
		
		List<String> hs = new ArrayList<String>();
		boolean hasBehavior = false;

		if (rows == null) return;
		ActivityNode curNode = GeneratorUtils.findInitialNode(ts);
		ActivityNode bNode = null;
		if (curNode == null) return;
		Collection<ActivityEdge> outs = curNode.getOutgoing();
		while (outs != null && outs.size() == 1) {
			curNode = outs.iterator().next().getTarget();
			// Find out if have a behavior
			if (curNode instanceof CallBehaviorAction && ((CallBehaviorAction)curNode).getBehavior() != null) {
				bNode = GeneratorUtils.findInitialNode(((CallBehaviorAction)curNode).getBehavior());
				hasBehavior = true;
			} else if (curNode instanceof StructuredActivityNode) {
				bNode = GeneratorUtils.findInitialNode(curNode);
				hasBehavior = true;
			}
			boolean hasTablePropColStereoType = StereotypesHelper.hasStereotype(curNode, DocGen3Profile.tablePropertyColumnStereotype );
			boolean hasTableExprColStereoType = StereotypesHelper.hasStereotype(curNode, DocGen3Profile.tableExpressionColumnStereotype);
			boolean hasTableAttrColStereoType = StereotypesHelper.hasStereotype(curNode, DocGen3Profile.tableAttributeColumnStereotype);
			boolean hasTableSumRowStereoType = StereotypesHelper.hasStereotype(curNode, DocGen3Profile.tableSumRowStereotype);
			if (hasTablePropColStereoType||hasTableExprColStereoType) {
				// TablePropertyColumn tags
				Object dProp = null;
				if (hasTablePropColStereoType) {
					dProp = GeneratorUtils.getObjectProperty(curNode, DocGen3Profile.tablePropertyColumnStereotype, "desiredProperty", null);
				} else if (hasTableExprColStereoType) {
					dProp = GeneratorUtils.getObjectProperty(curNode, DocGen3Profile.tableExpressionColumnStereotype, "expression", null);
				}
				// Headings
				hs.add(parseTableHeadings(curNode));
				// Parse stuff
				if ( hasTableExprColStereoType ) {
					parseExpressionColumn(curNode, dProp, rows);
				} else {
					if ( dProp instanceof Property ) {
						if (hasBehavior)parseColumn((Property)dProp, handleTableBehavior(bNode, rows), TableStructure.propertyColumn);
						else parseColumn((Property)dProp, rows, TableStructure.propertyColumn);
					} else if (dProp == null) {
						if (hasBehavior)parseColumn((Property)dProp, handleTableBehavior(bNode, rows), TableStructure.propertyColumn);
						else {
							Debug.error( false, "Expected Property but got null" );
						}
					} else {
						Debug.error( false, "Expected Property but got: "
								+ dProp
								+ ( dProp == null ? "" : " of type "
										+ dProp.getClass()
										.getName() ) );

					}
				}
			} else if (hasTableAttrColStereoType) {
				// TableAttributeColumn tags
				Object dAttr = GeneratorUtils.getObjectProperty(curNode, DocGen3Profile.tableAttributeColumnStereotype, "desiredAttribute", null);
				// Headings
				hs.add(parseTableHeadings(curNode));
				// Parse stuff
				if (hasBehavior)parseColumn((Object)dAttr, handleTableBehavior(bNode, rows), TableStructure.attributeColumn);
				else parseColumn(dAttr, rows, TableStructure.attributeColumn);
			} else if (hasTableSumRowStereoType) {
				addSumRow();
			}
			if (hasBehavior)
				hasBehavior = false;
			outs = curNode.getOutgoing();
		}

		setHeaders(hs);
	}
	
	public DocumentElement visit(boolean forViewEditor) {
		DBTable t = new DBTable();
		
		List<List<DocumentElement>> body = new ArrayList<List<DocumentElement>>();
		// want to add things to body by rows
		while (hasNext()) {
			List<Object> tsRow = next();
			if (tsRow == null)
				continue;
			List<DocumentElement> row = new ArrayList<DocumentElement>();
			for (Object e: tsRow) {
				// TODO: Think about any problem that could arise from the following casting...
				// Note assumption that all Objects in TS are either Lists of Properties or empty list
				DBTableEntry item = Common.getTableEntryFromObject( e, false, forViewEditor );
				row.add(item);
			}
			body.add(row);
		}
		// set DBTable headers
		List<List<DocumentElement>> hs = new ArrayList<List<DocumentElement>>();
		if (!getHeaders().isEmpty()) {
			List<DocumentElement> first = new ArrayList<DocumentElement>();
			hs.add(first);
			for (String h: getHeaders())
				first.add(new DBText(h));
			t.setCols(first.size());
		} 
		// otherwise, take the names of each element, in which case this would
		// parseTableStructure loop to automatcally add the name of columns to the headers variable in 
		// TableStructure.j
		else {
			List<DocumentElement> first = new ArrayList<DocumentElement>();
			hs.add(first);
			for (int i = 0; i < getColumNum(); i++)
				first.add(new DBText());
		}
		t.setHeaders(hs);
		// set DBTable and add column specification stuff
		t.setBody(body);
		List<DBColSpec> cslist = new ArrayList<DBColSpec>();
		if (getColwidths() != null && !getColwidths().isEmpty()) {
			int i = 1;
			for (String s: getColwidths()) {
				DBColSpec cs = new DBColSpec(i);
				cs.setColwidth(s);
				cslist.add(cs);
				i++;
			}
		} else {
			DBColSpec cs = new DBColSpec(1);
			cs.setColwidth(".4*");
			cslist.add(cs);
		}
		t.setColspecs(cslist);
		t.setStyle(getStyle());
		
		return t;
	}
	
	// PARSING HELPERS
	// ><><><><><><><>
	
	private String parseTableHeadings(ActivityNode curNode) {
		String cName = ((NamedElement)curNode).getName();
		// Heading choice branch
		if (cName != null && !cName.equals(""))
			return cName;
		else 
			return new String();
	}
	
	private List<Object> handleTableBehavior(ActivityNode bNode, List<Element> rowsIn) {
		List<Object> rowsOut = new ArrayList<Object>();
		
		// Find the first collect/filter activitynode in the behavior
		// IMPORTANT INVARIANT: assumes the activity whose initial node is passed as bNode ONLY
		//   contains collect or filter action first!
		if (bNode != null) {
			if (bNode.getOutgoing() != null) {
				bNode = bNode.getOutgoing().iterator().next().getTarget();
			}
		} 
	
		// Loop through row elements, passing each one as a target to the startCollectandFilter thing
		for (Element r: rowsIn) {
			List<Element> rAsTargets = new ArrayList<Element>();
			rAsTargets.add(r);
//			targets.push(rAsTargets);
			rowsOut.add(CollectFilterParser.startCollectAndFilterSequence(bNode, rAsTargets));
//			rowsOut.add(DocumentGenerator.)
		}
		
		return rowsOut; // TODO: for some reason, this is always an empty list. Why?
	}

	String irrelevantEntry = new String("--");

	// TODO -- REVIEW -- Should parse*Column() and handle*Cell() methods not
	// evaluate expressions or find property values and do this in
	// DocBookOutputVisitor like other tables?

	private void parseExpressionColumn( ActivityNode curNode, Object expression,
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
	private void parseColumn(Object dThing, List<?> rows, String colType) {
		List<Object> curCol = new ArrayList<Object>();
		if (dThing == null || colType == null) {
			curCol.addAll(rows); // REVIEW -- does this need to be a list of lists?
		} else {
			for (Object r: rows) {
				if (r instanceof Element) {
					if (colType.equals(propertyColumn)) {
						curCol.add(Utils.getElementProperty((Element)r, (Property)dThing));
					} else if (colType.equals(attributeColumn)) {
						curCol.add(handleAttributeCell(dThing, (Element)r));
					}
				} else if (r instanceof List<?>) {
					List<Object> superCell = new ArrayList<Object>();
					for (Object c: (List<Object>)r) {
						if (c instanceof Element) { 
							if (colType.equals(propertyColumn)) {
								superCell.addAll(Utils.getElementProperty((Element)c, (Property)dThing));
							} else if (colType.equals(attributeColumn)) {
								superCell.addAll(handleAttributeCell(dThing, (Element)c));
							}
						} else {
							superCell.addAll( Utils2.newList( c ) );
						}
					}	
					curCol.add(superCell);
				} else {
					curCol.add( Utils2.newList( r ) );
				}
			}
		}
		addColumn(curCol);
	}

	
	public List<Object> handleAttributeCell(Object dAttr, Element cell) { 
		List<Object> rSlots = new ArrayList<Object>();
		if (dAttr != null) {
			Object cellAttr = Utils.getElementAttribute(cell, ((EnumerationLiteral)dAttr).getName());
			if (cellAttr != null) {
				if (cellAttr instanceof Collection<?>) {
					rSlots.addAll((Collection<? extends Object>)cellAttr);
				} else {
					rSlots.add(cellAttr);
				}
			}
		}
		return rSlots;
	}
	
	@SuppressWarnings("unchecked")
	public void addSumRow() {
		List<Object> sumRow = new ArrayList<Object>();
		double f;
		boolean foundSumable = false;
		for (List<Object> c: table) {
			f = 0;
			for (Object l: c) {
				if (l instanceof List<?>) {
					for (Object item: (List<Object>)l) {
						if (item instanceof Float || item instanceof Double || item instanceof Integer) {
							foundSumable = true;
							f += (Double)item;
						} else if (Utils2.toDouble(DocGenUtils.fixString(item, false)) != null) {
							foundSumable = true;
							f += new Double(ModelHelper.getValueString((ValueSpecification)item));
						}
					}
				}
			}
			List<Object> bucket = new ArrayList<Object>();
			if (foundSumable) {
				bucket.add(f);
			} else {
				bucket.add(irrelevantEntry);
			}
			sumRow.add(bucket);
			foundSumable = false;
		}
		addRow(sumRow);
	}
	
	// SORTING STUFF
	// ><><><><><><>
	
	private int sortCol; // column index+1, neg means inv. order, 0 is no sort

	private void setSort() {
		
	}
	
	// ITERATOR STUFF
	// ><><><><><><><

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
		
	// FOR DOCBOOKOUTPUTVISITOR STUFF
	// ><><><><><><><><><><><><><><><
	
	@Override
	public void accept(IModelVisitor v) {
		v.visit(this);
		
	}
}
