package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.DocGenUtils;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBColSpec;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBHasContent;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTable;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTableEntry;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.From;
import gov.nasa.jpl.mgss.mbee.docgen.generator.CollectFilterParser;
import gov.nasa.jpl.mgss.mbee.docgen.generator.GenerationContext;
import gov.nasa.jpl.ocl.OclEvaluator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Iterator;
import java.util.Stack;

import org.eclipse.emf.ecore.EObject;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.ActivityEdge;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.InitialNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdstructuredactivities.StructuredActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.EnumerationLiteral;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;

/**
 * This class contains methods for parsing and visiting 
 * TableStructures and their contents.
 * 
 * @author bcompane
 *
 */

public class TableStructure extends Table {

	private abstract class TableColumn {
		public String name = "";
		public boolean simpleList = false; //not implemented whether a cell should display simple list if content is a list
		public InitialNode bnode;
	}
	
	private class TableAttributeColumn extends TableColumn {
		public Utils.AvailableAttribute attribute;
	}
	
	private class TablePropertyColumn extends TableColumn {
		public Property property;
	}
	
	private class TableExpressionColumn extends TableColumn {
		public String expression;
	}
		
	private List<String> headers = new ArrayList<String>();

	private List<TableColumn> columns = new ArrayList<TableColumn>();
		
	private Element ts;
	
	private InitialNode bnode;
	
	private String title;
	
	private String empty = "no entry";
	
	private List<List<List<Reference>>> tableContent = new ArrayList<List<List<Reference>>>();
	
	@Override
	public void initialize() {
		super.initialize();
		if (dgElement instanceof StructuredActivityNode) {
			ts = (StructuredActivityNode)dgElement;
		} else if (dgElement instanceof CallBehaviorAction) {
			ts = ((CallBehaviorAction)dgElement).getBehavior();
		} else {
			ts = null;
		}
		if (ts != null)
			bnode = GeneratorUtils.findInitialNode(ts);
		title = ((NamedElement)dgElement).getName();
	}

	@Override
	public void parse() {
		if (bnode == null)
			return;
		ActivityNode curNode = bnode;
		
		Collection<ActivityEdge> outs = curNode.getOutgoing();
		while (outs != null && outs.size() == 1) {
			curNode = outs.iterator().next().getTarget();
			TableColumn col = null;
			if (GeneratorUtils.hasStereotypeByString(curNode, DocGen3Profile.tableAttributeColumnStereotype)) {
				col = new TableAttributeColumn();
				Object attr = GeneratorUtils.getObjectProperty(curNode, DocGen3Profile.tableAttributeColumnStereotype, "desiredAttribute", null);
				((TableAttributeColumn)col).attribute = (attr instanceof EnumerationLiteral) ? Utils.AvailableAttribute.valueOf(((EnumerationLiteral)attr).getName()) : null;
			} else if (GeneratorUtils.hasStereotypeByString(curNode, DocGen3Profile.tableExpressionColumnStereotype)) {
				col = new TableExpressionColumn();
				((TableExpressionColumn)col).expression = (String)GeneratorUtils.getObjectProperty(curNode, DocGen3Profile.tableExpressionColumnStereotype, "expression", null);
			} else if (GeneratorUtils.hasStereotypeByString(curNode, DocGen3Profile.tablePropertyColumnStereotype)) {
				col = new TablePropertyColumn();
				((TablePropertyColumn)col).property = (Property)GeneratorUtils.getObjectProperty(curNode, DocGen3Profile.tablePropertyColumnStereotype, "desiredProperty", null);
			} else {
				outs = curNode.getOutgoing();
				continue;
			}
			if (curNode instanceof CallBehaviorAction && ((CallBehaviorAction)curNode).getBehavior() != null) {
				col.bnode = GeneratorUtils.findInitialNode(((CallBehaviorAction)curNode).getBehavior());
			} else if (curNode instanceof StructuredActivityNode) {
				col.bnode = GeneratorUtils.findInitialNode(curNode);
			}
			col.name = curNode.getName();
			headers.add(curNode.getName());
			columns.add(col);
			outs = curNode.getOutgoing();
		}
	}
	
	private void buildTableReferences() {
		for (Element e: targets) {
			List<List<Reference>> row = new ArrayList<List<Reference>>();
			for (TableColumn tc: columns) {
				List<Element> startElements = new ArrayList<Element>();
				startElements.add(e);
				List<Element> resultElements;
				if (tc.bnode != null && tc.bnode.getOutgoing().iterator().hasNext()) { //should check next node is collect/filter node
					ActivityNode n = tc.bnode.getOutgoing().iterator().next().getTarget();
					Stack<List<Element>> in = new Stack<List<Element>>();
					CollectFilterParser.setContext(new GenerationContext(in, n, null, Application.getInstance().getGUILog()));
					resultElements = CollectFilterParser.startCollectAndFilterSequence(n, startElements);
				} else
					resultElements = startElements;
				
				List<Reference> cell = new ArrayList<Reference>();
				for (Element re: resultElements) {
					if (tc instanceof TableAttributeColumn) {
						Utils.AvailableAttribute at = ((TableAttributeColumn)tc).attribute;
						if (at == null) {
							//cell.add(new Reference(empty));
							continue;
						}
						Object attr = Utils.getElementAttribute(re, at); //attr can be string, value spec, or list of value spec if element is a slot
						if (attr != null)
							cell.add(new Reference(re, Utils.getFromAttribute(((TableAttributeColumn)tc).attribute), attr));
						//else
							//cell.add(new Reference(empty));
					} else if (tc instanceof TablePropertyColumn) {
						Property prop = ((TablePropertyColumn)tc).property;
						if (prop == null) {
							//cell.add(new Reference(empty));
							continue;
						}
						Element slotOrProperty = Utils.getElementProperty(re, prop);
						List<Object> values = Utils.getElementPropertyValues(re, prop, true);
						if (slotOrProperty != null) {
							cell.add(new Reference(slotOrProperty, From.DVALUE, values));
						} else
							cell.add(new Reference(values));
					} else {
						String expr = ((TableExpressionColumn)tc).expression;
						if (expr == null) {
							//cell.add(new Reference(empty));
							continue;
						}
						cell.add(new Reference(OclEvaluator.evaluateQuery((EObject)re, expr)));
					}
				}
				row.add(cell);
			}
			tableContent.add(row);
		}
	}
	
	@Override
	public void visit(boolean forViewEditor, DBHasContent parent, String outputDir) {
		if (ignore)
			return;
		
		buildTableReferences();
		DBTable table = new DBTable();

		List<List<DocumentElement>> tableheaders = new ArrayList<List<DocumentElement>>();
		List<DocumentElement> header = new ArrayList<DocumentElement>();
		for (String head: this.headers) {
			header.add(new DBText(head));
		}
		tableheaders.add(header);
		table.setHeaders(tableheaders);
		
		List<List<DocumentElement>> body = new ArrayList<List<DocumentElement>>();
		for (List<List<Reference>> row: tableContent) {
			List<DocumentElement> tableRow = new ArrayList<DocumentElement>();
			for (List<Reference> cell: row) {
				DBTableEntry entry = new DBTableEntry();
				for (Reference cellPart: cell) {
					Common.addReferenceToDBHasContent(cellPart, entry);
				}
				tableRow.add(entry);
			}
			body.add(tableRow);
		}
		table.setBody(body);
		if (title != null && !title.equals("") && titles.isEmpty()) {
			titles.add(title);
		}
		setTableThings(table);
		parent.addElement(table);
	}


	/*
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
	
*/

	@Override
	public void accept(IModelVisitor v) {
		v.visit(this);
		
	}
}
