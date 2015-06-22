/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 * 
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.mbee.model;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.generator.CollectFilterParser;
import gov.nasa.jpl.mbee.generator.DocumentValidator;
import gov.nasa.jpl.mbee.generator.GenerationContext;
import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.lib.MoreToString;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBColSpec;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTable;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTableEntry;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.From;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.ActivityEdge;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.InitialNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdstructuredactivities.StructuredActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.EnumerationLiteral;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

/**
 * This class contains methods for parsing and visiting TableStructures and
 * their contents.
 * 
 * @author bcompane
 * 
 */

public class TableStructure extends Table {

    private abstract class TableColumn {
        public InitialNode       bnode;
        public ActivityNode      activityNode;
        public GenerationContext context    = null;
        public String            name       = "";
        public boolean editable = true;

        public GenerationContext makeContext() {
            ActivityNode n = null;
            if (bnode != null && bnode.getOutgoing().iterator().hasNext()) { // should
                                                                             // check
                                                                             // next
                                                                             // node
                                                                             // is
                                                                             // collect/filter
                                                                             // node
                n = bnode.getOutgoing().iterator().next().getTarget();
            }
            Stack<List<Object>> in = new Stack<List<Object>>();
            // in.add( targets );
            context = new GenerationContext(in, n, getValidator(), Application.getInstance().getGUILog());
            return context;
        }
    }

    private class TableColumnGroup extends TableColumn {
        public List<TableColumn> childColumns = new ArrayList<TableColumn>();
    }
    
    private class TableAttributeColumn extends TableColumn {
        public Utils.AvailableAttribute attribute;
    }

    private class TablePropertyColumn extends TableColumn {
        public Property property;
    }

    private class TableExpressionColumn extends TableColumn {
        public String  expression;
        public Boolean iterate;
    }

    //private List<String>                headers      = new ArrayList<String>();
    private List<TableColumn>           headers = new ArrayList<TableColumn>();
    private int headerDepth = 0; //1 based
    private List<TableColumn>           columns      = new ArrayList<TableColumn>();

    private Element                     ts;

    private InitialNode                 bnode;

    private String                      title;

    private List<List<List<Reference>>> tableContent = new ArrayList<List<List<Reference>>>();

    private Map<TableColumn, Integer>   columnIndex  = new HashMap<TableStructure.TableColumn, Integer>(); //0 based

    protected DocumentValidator         validator    = null;

    public TableStructure(DocumentValidator validator) {
        super();
        this.validator = validator;
    }

    @Override
    public void initialize() {
        super.initialize();
        if (dgElement instanceof StructuredActivityNode) {
            ts = dgElement;
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
        parseColumns(bnode, null, 1);
    }

    private void parseColumns(ActivityNode inNode, TableColumnGroup parent, int curDepth) {
        if (inNode == null)
            return;
        ActivityNode curNode = inNode;

        Collection<ActivityEdge> outs = curNode.getOutgoing();
        while (outs != null && outs.size() == 1) {
            curNode = outs.iterator().next().getTarget();
            TableColumn col = null;
            if (GeneratorUtils.hasStereotypeByString(curNode, DocGen3Profile.tableAttributeColumnStereotype)) {
                col = new TableAttributeColumn();
                Object attr = GeneratorUtils.getObjectProperty(curNode,
                        DocGen3Profile.tableAttributeColumnStereotype, "desiredAttribute", null);
                ((TableAttributeColumn)col).attribute = (attr instanceof EnumerationLiteral)
                        ? Utils.AvailableAttribute.valueOf(((EnumerationLiteral)attr).getName()) : null;
            } else if (GeneratorUtils.hasStereotypeByString(curNode,
                    DocGen3Profile.tableExpressionColumnStereotype)) {
                col = new TableExpressionColumn();
                ((TableExpressionColumn)col).expression = (String)GeneratorUtils.getObjectProperty(curNode,
                        DocGen3Profile.tableExpressionColumnStereotype, "expression", null);
                ((TableExpressionColumn)col).iterate = (Boolean)GeneratorUtils.getObjectProperty(curNode,
                        DocGen3Profile.tableExpressionColumnStereotype, "iterate", true);
            } else if (GeneratorUtils.hasStereotypeByString(curNode,
                    DocGen3Profile.tablePropertyColumnStereotype)) {
                col = new TablePropertyColumn();
                ((TablePropertyColumn)col).property = (Property)GeneratorUtils.getObjectProperty(curNode,
                        DocGen3Profile.tablePropertyColumnStereotype, "desiredProperty", null);
            } else if (GeneratorUtils.hasStereotypeByString(curNode, "TableColumnGroup")) {
                col = new TableColumnGroup();
            } else {
                outs = curNode.getOutgoing();
                continue;
            }
            col.editable = (Boolean)GeneratorUtils.getObjectProperty(curNode, DocGen3Profile.tableColumnStereotype, "editable", true);
            col.activityNode = curNode;
            if (curNode instanceof CallBehaviorAction && ((CallBehaviorAction)curNode).getBehavior() != null) {
                col.bnode = GeneratorUtils.findInitialNode(((CallBehaviorAction)curNode).getBehavior());
            } else if (curNode instanceof StructuredActivityNode) {
                col.bnode = GeneratorUtils.findInitialNode(curNode);
            }
            col.name = curNode.getName();
            //headers.add(curNode.getName());
            if (!(col instanceof TableColumnGroup)) {
                columns.add(col);
                columnIndex.put(col, columnIndex.size());
            } else {
                parseColumns(col.bnode, (TableColumnGroup)col, curDepth+1);
            }
            if (parent != null)
                parent.childColumns.add(col);
            else
                headers.add(col);
            outs = curNode.getOutgoing();
            if (headerDepth < curDepth)
                headerDepth = curDepth;
        }
    }
    
    private void buildTableReferences() {
        for (Object e: targets) {
            List<List<Reference>> row = new ArrayList<List<Reference>>();
            List<Object> startElements = new ArrayList<Object>();
            startElements.add(e);
            for (TableColumn tc: columns) {
                List<Element> resultElements;
                GenerationContext context = tc.makeContext();
                if (context.getCurrentNode() != null) { // should check next
                                                        // node is
                                                        // collect/filter node
                    CollectFilterParser.setContext(context);
                    resultElements = CollectFilterParser.startCollectAndFilterSequence(
                            context.getCurrentNode(), Utils2.asList( startElements, Element.class) );
                } else {
                    resultElements = Utils2.asList( startElements, Element.class);
                }
                List<Reference> cell = new ArrayList<Reference>();
                if (tc instanceof TableExpressionColumn && !((TableExpressionColumn)tc).iterate) {
                    String expr = ((TableExpressionColumn)tc).expression;
                    if (expr != null) {
                        Object result = DocumentValidator
                                .evaluate(expr, resultElements, getValidator(), true);
                        if (result != null) {
                            Debug.outln( "valid result = " + result
                                         + " for expression " + expr + " on "
                                         + MoreToString.Helper.toLongString( resultElements ) );
                            cell.add(new Reference(result));
                        } else {
                            Debug.outln( "invalid evaluation of expression "
                                         + expr + " on "
                                         + MoreToString.Helper.toLongString( resultElements ) );
                        }
                    } else {
                        Debug.outln( "attempted to evaluate null expression on "
                                + MoreToString.Helper.toLongString( resultElements ) );
                    }
                } else {
                    for (Element re: resultElements) {
                        if (tc instanceof TableAttributeColumn) {
                            Utils.AvailableAttribute at = ((TableAttributeColumn)tc).attribute;
                            if (at == null) {
                                continue;
                            }
                            Object attr = Utils.getElementAttribute(re, at); // attr can be string, value spec, or list of value spec
                            if (attr == null && tc.editable && at == Utils.AvailableAttribute.Value && re instanceof Property)
                                cell.add(new Reference(re, Utils.getFromAttribute(at), ""));
                            else if (attr != null) {
                                if (tc.editable)
                                    cell.add(new Reference(re, Utils.getFromAttribute(((TableAttributeColumn)tc).attribute), attr));
                                else
                                    cell.add(new Reference(attr));
                            }
                        } else if (tc instanceof TablePropertyColumn) {
                            Property prop = ((TablePropertyColumn)tc).property;
                            if (prop == null) {
                                continue;
                            }
                            Element slotOrProperty = Utils.getElementProperty(re, prop);
                            List<Object> values = Utils.getElementPropertyValues(re, prop, true);
                            if (slotOrProperty != null && tc.editable) {
                                cell.add(new Reference(slotOrProperty, From.DVALUE, values));
                            } else
                                cell.add(new Reference(values));
                        } else {
                            String expr = ((TableExpressionColumn)tc).expression;
                            if (expr == null) {
                                // cell.add(new Reference(empty));
                                Debug.outln( "attempted to evaluate null expression on "
                                        + MoreToString.Helper.toLongString( re ) );
                                continue;
                            }
                            Object result = DocumentValidator.evaluate(expr, re, getValidator(), true);
                            if (result != null) {
                                Debug.outln( "valid result = " + result
                                             + " for expression " + expr + " on "
                                             + MoreToString.Helper.toLongString( re ) );
                                cell.add(new Reference(result));
                            } else {
                                Debug.outln( "invalid evaluation of expression "
                                        + expr + " on "
                                        + MoreToString.Helper.toLongString( re ) );
   
                            }
                        }
                    }
                }
                row.add(cell);

                // check constraints
                DocumentValidator.evaluateConstraints(tc.activityNode, getCellData(row, tc), context, true,
                        true);
            }
            Debug.outln( "adding " + row.size() + " cells in row to table." );
            tableContent.add(row);
        }
    }

    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        List<DocumentElement> res = new ArrayList<DocumentElement>();
        if (ignore)
            return res;

        buildTableReferences();
        DBTable table = new DBTable();

        List<List<DocumentElement>> tableheaders = makeTableHeaders();
        table.setHeaders(tableheaders);
        if (headerDepth > 1) {
            List<DBColSpec> colspecs = new ArrayList<DBColSpec>();
            for (int i = 0; i < columns.size(); i++) {
                DBColSpec cs = new DBColSpec(i+1);
                colspecs.add(cs);
            }
            table.setColspecs(colspecs);
        }
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
        res.add(table);
        return res;
    }

    public List<Reference> getCellReferences(List<List<Reference>> row, TableColumn col) {
        List<Reference> colData = row.get(getColumnIndex(col));
        return colData;
    }

    public List<Object> getCellData(List<List<Reference>> row, TableColumn col) {
        List<Reference> colRefs = getCellReferences(row, col);
        List<Object> colData = new ArrayList<Object>();
        for (Reference r: colRefs) {
            colData.add(r.result);
        }
        return colData;
    }

    protected int getColumnIndex(TableColumn col) {
        return columnIndex.get(col);
    }

    /*
     * @SuppressWarnings("unchecked") public void addSumRow() { List<Object>
     * sumRow = new ArrayList<Object>(); double f; boolean foundSumable = false;
     * for (List<Object> c: table) { f = 0; for (Object l: c) { if (l instanceof
     * List<?>) { for (Object item: (List<Object>)l) { if (item instanceof Float
     * || item instanceof Double || item instanceof Integer) { foundSumable =
     * true; f += (Double)item; } else if
     * (Utils2.toDouble(DocGenUtils.fixString(item, false)) != null) {
     * foundSumable = true; f += new
     * Double(ModelHelper.getValueString((ValueSpecification)item)); } } } }
     * List<Object> bucket = new ArrayList<Object>(); if (foundSumable) {
     * bucket.add(f); } else { bucket.add(irrelevantEntry); }
     * sumRow.add(bucket); foundSumable = false; } addRow(sumRow); }
     */

    public DocumentValidator getValidator() {
        return validator;
    }

    public void setValidator(DocumentValidator validator) {
        this.validator = validator;
    }

    private List<List<DocumentElement>> makeTableHeaders() {
        List<List<DocumentElement>> result = new ArrayList<List<DocumentElement>>();
        for (int i = 0; i < headerDepth; i++) { //add in rows of the headers
            result.add(new ArrayList<DocumentElement>()); 
        }
        int start = 0;
        for (TableColumn tc: headers) {
            start = addHeader(tc, result, 1, start+1);
        }
        return result;
    }
    
    private int addHeader(TableColumn tc, List<List<DocumentElement>> header, int curDepth, int startIndex) {
        if (tc instanceof TableColumnGroup) {
            int start = startIndex;
            DBTableEntry entry = new DBTableEntry();
            entry.addElement(new DBText(tc.name));
            int i = 0;
            for (TableColumn ctc: ((TableColumnGroup)tc).childColumns) {
                if (i == 0)
                    start = addHeader(ctc, header, curDepth+1, start);
                else
                    start = addHeader(ctc, header, curDepth+1, start+1);
                i++;
            }
            entry.setNamest(Integer.toString(startIndex));
            entry.setNameend(Integer.toString(start));
            header.get(curDepth-1).add(entry);
            return start;
        } else {
            DBTableEntry entry = new DBTableEntry();
            entry.addElement(new DBText(tc.name));
            header.get(curDepth-1).add(entry);
            if (curDepth < headerDepth) {
                entry.setMorerows(headerDepth-curDepth);
            }
            return columnIndex.get(tc)+1;
        }
    }
}
