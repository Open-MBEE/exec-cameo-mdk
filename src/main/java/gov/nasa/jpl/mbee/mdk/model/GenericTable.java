package gov.nasa.jpl.mbee.mdk.model;

import com.nomagic.generictable.GenericTableManager;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.dependencymatrix.configuration.MatrixDataHelper;
import com.nomagic.magicdraw.dependencymatrix.datamodel.MatrixData;
import com.nomagic.magicdraw.dependencymatrix.datamodel.cell.AbstractMatrixCell;
import com.nomagic.magicdraw.properties.*;
import com.nomagic.magicdraw.uml.DiagramType;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenProfile;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.*;
import gov.nasa.jpl.mbee.mdk.mms.sync.local.LocalSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.mdk.mms.sync.local.LocalSyncTransactionCommitListener;
import gov.nasa.jpl.mbee.mdk.util.DependencyMatrixTool;
import gov.nasa.jpl.mbee.mdk.util.GeneratorUtils;
import gov.nasa.jpl.mbee.mdk.util.MatrixUtil;
import gov.nasa.jpl.mbee.mdk.util.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GenericTable extends Table {

    public static final String INSTANCE_TABLE = "Instance Table";
    public static final String VERIFY_REQUIREMENTS_MATRIX = "Verify Requirement Matrix";
    public static final String ALLOCATION_MATRIX = "SysML Allocation Matrix";
    public static final String SATISFY_REQUIREMENTS_MATRIX = "Satisfy Requirement Matrix";
    public static final String REQUIREMENTS_TABLE = "Requirement Table";

    private List<String> headers;
    private boolean skipIfNoDoc;
    private static ArrayList<String> skipColumnIds = new ArrayList<String>() {{
        add("QPROP:Element:isEncapsulated");
        add("QPROP:Element:CUSTOM_IMAGE");
    }};
    private int numCols = 0;


    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        List<DocumentElement> res = new ArrayList<DocumentElement>();
        if (getIgnore()) {
            return res;
        }
        // We really shouldn't be using Application.getInstance().getProject() here, but the context is already lost somewhere upstream :/
        // dgElement won't always be there and there is no guarantee that it or the exposed element(s) aren't from a library
        LocalSyncTransactionCommitListener listener = LocalSyncProjectEventListenerAdapter.getProjectMapping(Application.getInstance().getProject()).getLocalSyncTransactionCommitListener();
        listener.setDisabled(true);
        try {
            int tableCount = 0;
            List<Object> targets = isSortElementsByName() ? Utils.sortByName(getTargets()) : getTargets();
            for (Object e : targets) {
                if (e instanceof Diagram) {
                    Diagram diagram = (Diagram) e;
                    DiagramType diagramType = Application.getInstance().getProject().getDiagram(diagram).getDiagramType();
                    if (diagramType.isTypeOf(DiagramType.GENERIC_TABLE) || diagramType.getType().equals(INSTANCE_TABLE) || diagramType.getType().equals(REQUIREMENTS_TABLE)) {
                        DBTable t = new DBTable();
                        List<String> columnIds = GenericTableManager.getVisibleColumnIds(diagram);
                        t.setHeaders(getHeaders(diagram, columnIds, false));
                        List<Element> rowElements = null;
                        try {
                            rowElements = GenericTableManager.getVisibleRowElements(diagram);
                        } catch (NullPointerException np) {
                            rowElements = GenericTableManager.getRowElements(diagram);
                        }
                        t.setBody(getBody(diagram, rowElements, columnIds, forViewEditor));
                        if (getTitles() != null && getTitles().size() > tableCount) {
                            t.setTitle(getTitlePrefix() + getTitles().get(tableCount) + getTitleSuffix());
                        }
                        else {
                            t.setTitle(getTitlePrefix() + (diagram).getName() + getTitleSuffix());
                        }
                        if (getCaptions() != null && getCaptions().size() > tableCount && isShowCaptions()) {
                            t.setCaption(getCaptions().get(tableCount));
                        }
                        else {
                            t.setCaption(ModelHelper.getComment(diagram));
                        }
                        t.setCols(numCols);
                        res.add(t);
                        t.setStyle(getStyle());
                        tableCount++;
                    }
                    else {
                        MatrixData matrixData;
                        if (MatrixDataHelper.isRebuildNeeded(diagram)) {
                            matrixData = MatrixDataHelper.buildMatrix(diagram);
                        }
                        else {
                            matrixData = MatrixDataHelper.getMatrixData(diagram);
                        }

                        DependencyMatrixTool tool = new DependencyMatrixTool();
                        MatrixUtil matrix = tool.getMatrix(diagram);
                        List<Element> rowElements = matrix.getRows();
                        List<Element> columnElements = matrix.getColumns();
                        DBTable t = new DBTable();
                        List<List<DocumentElement>> matrixResult = new ArrayList<>();
                        List<String> columnHeaders = new ArrayList<>();
                        for (Element rowElement : rowElements) {
                            List<DocumentElement> matrixcolumn = new ArrayList<>();
                            if (rowElement instanceof NamedElement) {
                                matrixcolumn.add(new DBText(((NamedElement) rowElement).getName()));
                            }
                            else {
                                matrixcolumn.add(new DBText(rowElement.getHumanName()));
                            }
                            for (Element columnElement : columnElements) {
                                AbstractMatrixCell val = matrixData.getValue(rowElement, columnElement);
                                if (val.getDescription() != null) {
                                    if (val.isEditable()) {
                                        matrixcolumn.add(new DBText("&#10004;")); // HTML Check mark
                                    }
                                    else {
                                        matrixcolumn.add(new DBText("&#10003;"));
                                    }
                                }
                                else {
                                    matrixcolumn.add(new DBText(""));
                                }
                            }
                            matrixResult.add(matrixcolumn);
                        }
                        for (Element element : columnElements) {
                            if (element instanceof NamedElement) {
                                columnHeaders.add(((NamedElement) element).getName());
                            }
                        }
                        t.setHeaders(getHeaders(diagram, columnHeaders, true));
                        t.setBody(matrixResult);
                        if (getTitles() != null && getTitles().size() > tableCount) {
                            t.setTitle(getTitlePrefix() + getTitles().get(tableCount) + getTitleSuffix());
                        }
                        else {
                            t.setTitle(getTitlePrefix() + (diagram).getName() + getTitleSuffix());
                        }
                        if (getCaptions() != null && getCaptions().size() > tableCount && isShowCaptions()) {
                            t.setCaption(getCaptions().get(tableCount));
                        }
                        else {
                            t.setCaption(ModelHelper.getComment(diagram));
                        }
                        t.setCols(numCols);
                        res.add(t);
                        t.setStyle(getStyle());
                        tableCount++;
                    }
                }
            }
        } finally {
            listener.setDisabled(false);
        }

        return res;
    }

    public List<List<DocumentElement>> getHeaders(Diagram genericTable, List<String> columnIds, boolean isMatrix) {
        List<List<DocumentElement>> res = new ArrayList<List<DocumentElement>>();
        if (this.headers != null && !this.headers.isEmpty()) {
            List<DocumentElement> row = new ArrayList<DocumentElement>();
            for (String h : this.headers) {
                row.add(new DBText(h));
            }
            res.add(row);
        }
        else {
            List<DocumentElement> row = new ArrayList<DocumentElement>();
            int count = 0;
            for (String columnId : columnIds) {
                if (isMatrix) {
                    if (count == 0) {
                        row.add(new DBText(""));
                        count++;
                        numCols++;
                    }
                    row.add(new DBText(columnId));
                    numCols++;
                }
                else {
                    if (count == 0) {
                        count++;
                        continue;
                    }
                    if (!skipColumnIds.contains(columnId)) {
                        row.add(new DBText(GenericTableManager.getColumnNameById(genericTable, columnId)));
                        numCols++;
                    }
                }
            }
            res.add(row);
        }
        return res;
    }


    public List<List<DocumentElement>> getBody(Diagram d, Collection<Element> rowElements, List<String> columnIds, boolean forViewEditor) {
        List<List<DocumentElement>> res = new ArrayList<>();
        for (Element e : rowElements) {
            if (skipIfNoDoc && ModelHelper.getComment(e).trim().isEmpty()) {
                continue;
            }
            List<DocumentElement> row = new ArrayList<>();
            int count = 0;
            for (String cid : columnIds) {
                if (count == 0) {
                    count++;
                    continue;
                }
                if (skipColumnIds.contains(cid)) {
                    continue;
                }
                DBTableEntry entry = new DBTableEntry();

                Property cellValue = GenericTableManager.getCellValue(d, e, cid);
                if (cellValue instanceof ElementProperty) {
                    Element cellelement = ((ElementProperty) cellValue).getElement();
                    if (cellelement instanceof NamedElement) {
                        entry.addElement(new DBParagraph(((NamedElement) cellelement).getName(), cellelement, From.NAME));
                    }
                }
                else if (cellValue instanceof StringProperty) {
                    if (cid.contains("documentation")) {
                        entry.addElement(new DBParagraph(cellValue.getValue(), e, From.DOCUMENTATION));

                    }
                    else {
                        entry.addElement(new DBParagraph(cellValue.getValue()));
                    }
                }
                else if (cellValue instanceof NumberProperty) {
                    entry.addElement(new DBParagraph(cellValue.getValue()));
                }
                else if (cellValue instanceof ElementListProperty) {
                    for (Element listEl : ((ElementListProperty) cellValue).getValue()) {
                        if (listEl instanceof NamedElement) {
                            entry.addElement(new DBParagraph(((NamedElement) listEl).getName(), listEl, From.NAME));
                        }
                    }
                }
                else if (cellValue instanceof ElementInstanceProperty) {
                    Object value = cellValue.getValue();

                    if (value instanceof List) {
                        for (Object o : (List) value) {
                            if (o instanceof InstanceSpecification) {
                                entry.addElement(new DBParagraph(((InstanceSpecification) o).getName(), (Element) o, From.NAME));
                            }
                        }
                    }
                }
                else if (cellValue instanceof AbstractChoiceProperty) {
                    if (cellValue instanceof ChoiceProperty) {
                        int index = ((ChoiceProperty) cellValue).getIndex();
                        if (index > -1) {
                            Object choice = ((ChoiceProperty) cellValue).getChoice().get(index);
                            entry.addElement(new DBParagraph(choice.toString()));
                        }
                    }
                    else {
                        for (Object choice : ((AbstractChoiceProperty) cellValue).getChoice()) {
                            if (choice instanceof String) {
                                entry.addElement(new DBParagraph(choice.toString()));
                            }
                        }
                    }
                }
                else {
                    Application.getInstance().getGUILog().log("[WARNING] Cell value omitted: " + cellValue.toString() + ".");
                }
                row.add(entry);
            }
            res.add(row);
        }
        return res;
    }

    public List<Object> getTableValues(Object o) {
        List<Object> res = new ArrayList<>();
        if (o instanceof Object[]) {
            Object[] a = (Object[]) o;
            for (int i = 0; i < a.length; i++) {
                res.addAll(getTableValues(a[i]));
            }
        }
        else if (o instanceof Collection) {
            for (Object oo : (Collection) o) {
                res.addAll(getTableValues(oo));
            }
        }
        else if (o != null) {
            res.add(o);
        }
        return res;
    }

    public void setSkipIfNoDoc(boolean b) {
        skipIfNoDoc = b;
    }

    public void setHeaders(List<String> h) {
        headers = h;
    }


    @SuppressWarnings("unchecked")
    @Override
    public void initialize() {
        super.initialize();
        setHeaders((List<String>) GeneratorUtils.getStereotypePropertyValue(dgElement, DocGenProfile.headersChoosable,
                "headers", DocGenProfile.PROFILE_NAME, new ArrayList<String>()));
        setSkipIfNoDoc((Boolean) GeneratorUtils.getStereotypePropertyFirst(dgElement, DocGenProfile.docSkippable,
                "skipIfNoDoc", DocGenProfile.PROFILE_NAME, false));
    }

}
