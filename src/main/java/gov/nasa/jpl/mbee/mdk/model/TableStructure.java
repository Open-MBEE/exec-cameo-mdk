package gov.nasa.jpl.mbee.mdk.model;

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
import gov.nasa.jpl.mbee.mdk.docgen.DocGenProfile;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.*;
import gov.nasa.jpl.mbee.mdk.generator.CollectFilterParser;
import gov.nasa.jpl.mbee.mdk.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.mdk.generator.DocumentValidator;
import gov.nasa.jpl.mbee.mdk.generator.GenerationContext;
import gov.nasa.jpl.mbee.mdk.ocl.OclEvaluator;
import gov.nasa.jpl.mbee.mdk.util.*;

import java.util.*;

/**
 * This class contains methods for parsing and visiting TableStructures and
 * their contents.
 *
 * @author bcompane
 */

public class TableStructure extends Table {

    private abstract class TableColumn {
        public InitialNode bnode;
        public ActivityNode activityNode;
        public GenerationContext context = null;
        public String name = "";
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
        public String expression;
        public Boolean iterate;
    }

    private class TableStructuredColumn extends TableColumn {
        //public Generatable generatable;
    }

    //private List<String>                headers      = new ArrayList<String>();
    private List<TableColumn> headers = new ArrayList<TableColumn>();
    private int headerDepth = 0; //1 based
    private List<TableColumn> columns = new ArrayList<TableColumn>();

    private Element ts;

    private InitialNode bnode;

    private String title;

    private List<List<List<Pair<Reference, Boolean>>>> tableContent = new ArrayList<>();

    private Map<TableColumn, Integer> columnIndex = new HashMap<TableStructure.TableColumn, Integer>(); //0 based

    protected DocumentValidator validator = null;

    public TableStructure(DocumentValidator validator) {
        super();
        this.validator = validator;
    }

    @Override
    public void initialize() {
        super.initialize();
        if (dgElement instanceof StructuredActivityNode) {
            ts = dgElement;
        }
        else if (dgElement instanceof CallBehaviorAction) {
            ts = ((CallBehaviorAction) dgElement).getBehavior();
        }
        else {
            ts = null;
        }
        if (ts != null) {
            bnode = GeneratorUtils.findInitialNode(ts);
        }
        title = ((NamedElement) dgElement).getName();
    }

    @Override
    public void parse() {
        if (bnode == null) {
            return;
        }
        parseColumns(bnode, null, 1);
    }

    private void parseColumns(ActivityNode inNode, TableColumnGroup parent, int curDepth) {
        if (inNode == null) {
            return;
        }
        ActivityNode curNode = inNode;

        Collection<ActivityEdge> outs = curNode.getOutgoing();
        while (outs != null && outs.size() == 1) {
            curNode = outs.iterator().next().getTarget();
            TableColumn col = null;
            if (GeneratorUtils.hasStereotypeByString(curNode, DocGenProfile.tableAttributeColumnStereotype)) {
                col = new TableAttributeColumn();
                Object attr = GeneratorUtils.getStereotypePropertyFirst(curNode,
                        DocGenProfile.tableAttributeColumnStereotype, "desiredAttribute", DocGenProfile.PROFILE_NAME, null);
                ((TableAttributeColumn) col).attribute = (attr instanceof EnumerationLiteral)
                        ? Utils.AvailableAttribute.valueOf(((EnumerationLiteral) attr).getName()) : null;
            }
            else if (GeneratorUtils.hasStereotypeByString(curNode,
                    DocGenProfile.tableExpressionColumnStereotype)) {
                col = new TableExpressionColumn();
                ((TableExpressionColumn) col).expression = (String) GeneratorUtils.getStereotypePropertyFirst(curNode,
                        DocGenProfile.tableExpressionColumnStereotype, "expression", DocGenProfile.PROFILE_NAME, null);
                ((TableExpressionColumn) col).iterate = (Boolean) GeneratorUtils.getStereotypePropertyFirst(curNode,
                        DocGenProfile.tableExpressionColumnStereotype, "iterate", DocGenProfile.PROFILE_NAME, true);
            }
            else if (GeneratorUtils.hasStereotypeByString(curNode,
                    DocGenProfile.tablePropertyColumnStereotype)) {
                col = new TablePropertyColumn();
                ((TablePropertyColumn) col).property = (Property) GeneratorUtils.getStereotypePropertyFirst(curNode,
                        DocGenProfile.tablePropertyColumnStereotype, "desiredProperty", DocGenProfile.PROFILE_NAME, null);
            }
            else if (GeneratorUtils.hasStereotypeByString(curNode, "TableColumnGroup")) {
                col = new TableColumnGroup();
            }
            else if (GeneratorUtils.hasStereotypeByString(curNode, "StructuredQuery") || curNode instanceof CallBehaviorAction) {
                col = new TableStructuredColumn();
                //((TableStructuredColumn)col).structuredNode = curNode;
            }
            else {
                outs = curNode.getOutgoing();
                continue;
            }
            col.editable = (Boolean) GeneratorUtils.getStereotypePropertyFirst(curNode, DocGenProfile.editableChoosable, "editable", DocGenProfile.PROFILE_NAME, true);
            col.activityNode = curNode;
            if (curNode instanceof CallBehaviorAction && ((CallBehaviorAction) curNode).getBehavior() != null) {
                col.bnode = GeneratorUtils.findInitialNode(((CallBehaviorAction) curNode).getBehavior());
            }
            else if (curNode instanceof StructuredActivityNode) {
                col.bnode = GeneratorUtils.findInitialNode(curNode);
            }
            col.name = curNode.getName();
            //headers.add(curNode.getName());
            if (!(col instanceof TableColumnGroup)) {
                columns.add(col);
                columnIndex.put(col, columnIndex.size());
            }
            else {
                parseColumns(col.bnode, (TableColumnGroup) col, curDepth + 1);
            }
            if (parent != null) {
                parent.childColumns.add(col);
            }
            else {
                headers.add(col);
            }
            outs = curNode.getOutgoing();
            if (headerDepth < curDepth) {
                headerDepth = curDepth;
            }
        }
    }

    private void buildTableReferences(boolean forViewEditor, String outputDir) {
        Set<Object> warnedError = new HashSet<Object>();
        for (Object e : targets) {
            List<List<Pair<Reference, Boolean>>> row = new ArrayList<>();
            List<Object> startElements = new ArrayList<Object>();
            startElements.add(e);
            for (TableColumn tc : columns) {
                List<Element> resultElements;
                GenerationContext context = tc.makeContext();
                if (!(tc instanceof TableStructuredColumn) && context.getCurrentNode() != null) { // should check next
                    // node is
                    // collect/filter node
                    CollectFilterParser.setContext(context);
                    resultElements = CollectFilterParser.startCollectAndFilterSequence(
                            context.getCurrentNode(), Utils2.asList(startElements, Element.class));
                }
                else {
                    resultElements = Utils2.asList(startElements, Element.class);
                }
                List<Pair<Reference, Boolean>> cell = new ArrayList<>();
                if (tc instanceof TableExpressionColumn && !((TableExpressionColumn) tc).iterate) {
                    String expr = ((TableExpressionColumn) tc).expression;
                    if (expr != null) {
                        Object result = DocumentValidator
                                .evaluate(expr, resultElements, getValidator(), true);
                        OclEvaluator evaluator = OclEvaluator.instance;
                        if (evaluator.isValid() && result != null) {
                            Debug.outln("valid result = " + result
                                    + " for expression " + expr + " on "
                                    + MoreToString.Helper.toLongString(resultElements));
                            cell.add(new Pair<>(new Reference(result), tc.editable));
                        }
                        else {
                            Debug.outln("invalid evaluation of expression "
                                    + expr + " on "
                                    + MoreToString.Helper.toLongString(resultElements));
                        }
                    }
                    else {
                        Debug.outln("attempted to evaluate null expression on "
                                + MoreToString.Helper.toLongString(resultElements));
                    }
                }
                else {
                    /*for (final Element ee : resultElements) {
                        Application.getInstance().getGUILog().log(e instanceof NamedElement ? ((NamedElement) ee).getQualifiedName() : ee.getHumanName());
                	}
                	Application.getInstance().getGUILog().log(resultElements.toString());*/
                    for (Element re : resultElements) {
                        if (tc instanceof TableAttributeColumn) {
                            Utils.AvailableAttribute at = ((TableAttributeColumn) tc).attribute;
                            if (at == null) {
                                continue;
                            }
                            Object attr = Utils.getElementAttribute(re, at); // attr can be string, value spec, or list of value spec
                            if (attr == null && tc.editable && at == Utils.AvailableAttribute.Value && re instanceof Property) {
                                cell.add(new Pair<>(new Reference(re, Utils.getFromAttribute(at), ""), tc.editable));
                            }
                            else if (attr != null) {
                                cell.add(new Pair<>(new Reference(re, Utils.getFromAttribute(((TableAttributeColumn) tc).attribute), attr), tc.editable));
                            }
                        }
                        else if (tc instanceof TablePropertyColumn) {
                            Property prop = ((TablePropertyColumn) tc).property;
                            if (prop == null) {
                                continue;
                            }
                            Element slotOrProperty = Utils.getElementProperty(re, prop);
                            List<Object> values = Utils.getElementPropertyValues(re, prop, true);
                            if (slotOrProperty != null) {
                                cell.add(new Pair<>(new Reference(slotOrProperty, From.DVALUE, values), tc.editable));
                            }
                            else {
                                cell.add(new Pair<>(new Reference(values), tc.editable));
                            }
                        }
                        else if (tc instanceof TableExpressionColumn) {
                            String expr = ((TableExpressionColumn) tc).expression;
                            if (expr == null) {
                                // cell.add(new Reference(empty));
                                Debug.outln("attempted to evaluate null expression on "
                                        + MoreToString.Helper.toLongString(re));
                                continue;
                            }
                            Object result = DocumentValidator.evaluate(expr, re, getValidator(), true);
                            OclEvaluator evaluator = OclEvaluator.instance;
                            if (evaluator.isValid() || result != null) {
                                Debug.outln("valid result = " + result
                                        + " for expression " + expr + " on "
                                        + MoreToString.Helper.toLongString(re));
                                cell.add(new Pair<>(new Reference(result), false));
                            }
                            else {
                                Debug.outln("invalid evaluation of expression "
                                        + expr + " on "
                                        + MoreToString.Helper.toLongString(re));

                            }
                        }
                        else if (tc instanceof TableStructuredColumn) {
                            final Container con = new Section();
                            final DocumentGenerator dg = new DocumentGenerator(tc.activityNode, getValidator(), null);
                            if (tc.bnode == null) {
                                if (tc.activityNode != null && !warnedError.contains(tc)) {
                                    Utils.guilog("[WARN] Table structure column is missing initial node, skipping: " + tc.activityNode.getQualifiedName());
                                    warnedError.add(tc);
                                }
                                break;
                            }
                            final Element a = tc.bnode.getOwner();

                            final GenerationContext nestedContext = new GenerationContext(new Stack<List<Object>>(), tc.activityNode, getValidator(), Application.getInstance().getGUILog());
                            //Application.getInstance().getGUILog().log(re instanceof NamedElement ? ((NamedElement) re).getQualifiedName() : re.getHumanName());
                            List<Object> newl = new ArrayList<Object>();
                            newl.add(re);
                            nestedContext.pushTargets(newl);
                            //context.pushTargets(new ArrayList<Object>(startElements));
                            dg.setContext(nestedContext);

                            dg.parseActivityOrStructuredNode(a, con);
                            //Application.getInstance().getGUILog().log(re instanceof NamedElement ? ((NamedElement) re).getQualifiedName() : re.getHumanName());
                            for (DocGenElement dge : con.getChildren()) {
                                cell.add(new Pair<>(new Reference(dge), tc.editable));
                            }
                        }
                    }
                }
                row.add(cell);

                // check constraints
                DocumentValidator.evaluateConstraints(tc.activityNode, getCellData(row, tc), context, true,
                        true);
            }
            Debug.outln("adding " + row.size() + " cells in row to table.");
            tableContent.add(row);
        }
    }

    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        List<DocumentElement> res = new ArrayList<DocumentElement>();
        if (ignore) {
            return res;
        }

        buildTableReferences(forViewEditor, outputDir);
        DBTable table = new DBTable();

        List<List<DocumentElement>> tableheaders = makeTableHeaders();
        table.setHeaders(tableheaders);
        if (headerDepth > 1) {
            List<DBColSpec> colspecs = new ArrayList<DBColSpec>();
            for (int i = 0; i < columns.size(); i++) {
                DBColSpec cs = new DBColSpec(i + 1);
                colspecs.add(cs);
            }
            table.setColspecs(colspecs);
        }
        List<List<DocumentElement>> body = new ArrayList<List<DocumentElement>>();
        for (List<List<Pair<Reference, Boolean>>> row : tableContent) {
            List<DocumentElement> tableRow = new ArrayList<DocumentElement>();
            for (List<Pair<Reference, Boolean>> cell : row) {
                DBTableEntry entry = new DBTableEntry();
                for (Pair<Reference, Boolean> pair : cell) {
                    Reference cellPart = pair.getKey();
                    //Common.addReferenceToDBHasContent(cellPart, entry);
                    if (cellPart.result instanceof DocGenElement) {
                        DocBookOutputVisitor nested = new DocBookOutputVisitor(forViewEditor, outputDir);
                        nested.getParent().push(entry);
                        ((DocGenElement) cellPart.result).accept(nested);
                    }
                    else {
                        Common.addReferenceToDBHasContent(cellPart, entry, pair.getValue());
                    }
                }
                tableRow.add(entry);
            }
            body.add(tableRow);
        }
        table.setBody(body);
        if (title != null && !title.isEmpty() && titles.isEmpty()) {
            titles.add(title);
        }
        setTableThings(table);
        res.add(table);
        return res;
    }

    public List<Pair<Reference, Boolean>> getCellReferences(List<List<Pair<Reference, Boolean>>> row, TableColumn col) {
        List<Pair<Reference, Boolean>> colData = row.get(getColumnIndex(col));
        return colData;
    }

    public List<Object> getCellData(List<List<Pair<Reference, Boolean>>> row, TableColumn col) {
        List<Pair<Reference, Boolean>> colRefs = getCellReferences(row, col);
        List<Object> colData = new ArrayList<Object>();
        for (Pair<Reference, Boolean> pair : colRefs) {
            colData.add(pair.getKey().result);
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
        for (TableColumn tc : headers) {
            start = addHeader(tc, result, 1, start + 1);
        }
        return result;
    }

    private int addHeader(TableColumn tc, List<List<DocumentElement>> header, int curDepth, int startIndex) {
        if (tc instanceof TableColumnGroup) {
            int start = startIndex;
            DBTableEntry entry = new DBTableEntry();
            entry.addElement(new DBText(tc.name));
            int i = 0;
            for (TableColumn ctc : ((TableColumnGroup) tc).childColumns) {
                if (i == 0) {
                    start = addHeader(ctc, header, curDepth + 1, start);
                }
                else {
                    start = addHeader(ctc, header, curDepth + 1, start + 1);
                }
                i++;
            }
            entry.setNamest(Integer.toString(startIndex));
            entry.setNameend(Integer.toString(start));
            header.get(curDepth - 1).add(entry);
            return start;
        }
        else {
            DBTableEntry entry = new DBTableEntry();
            entry.addElement(new DBText(tc.name));
            header.get(curDepth - 1).add(entry);
            if (curDepth < headerDepth) {
                entry.setMorerows(headerDepth - curDepth);
            }
            return columnIndex.get(tc) + 1;
        }
    }
}
