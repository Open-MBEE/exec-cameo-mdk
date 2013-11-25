package gov.nasa.jpl.mbee.viewedit;

import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBColSpec;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBList;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBSimpleList;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTable;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTableEntry;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;

import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * table json exporter that allows multiple elements in cell editing and list
 * editing
 * 
 * @author dlam
 * 
 */
public class DBEditTableVisitor2 extends DBEditDocwebVisitor {

    private JSONObject tablejson;
    private JSONArray  curRow;
    private JSONArray  curCell;
    private JSONArray  tableelements;
    private int        rowspan;
    private int        colspan;

    // private GUILog gl = Application.getInstance().getGUILog();

    public DBEditTableVisitor2(boolean recurse, Map<String, JSONObject> elements) {
        super(recurse, true);
        this.elements = elements;
        tablejson = new JSONObject();
        tableelements = new JSONArray();
    }

    public JSONObject getObject() {
        return tablejson;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBTable table) {
        JSONArray body = new JSONArray();
        tablejson.put("body", body);
        for (List<DocumentElement> row: table.getBody()) {
            curRow = new JSONArray();
            body.add(curRow);
            for (DocumentElement de: row) {
                rowspan = 1;
                colspan = 1;
                if (de != null) {
                    if (de instanceof DBTableEntry)
                        de.accept(this);
                    else {
                        curCell = new JSONArray();
                        de.accept(this);
                        JSONObject entry = new JSONObject();
                        entry.put("content", curCell);
                        addSpans(entry);
                        curRow.add(entry);
                    }
                }
            }
        }
        JSONArray headers = new JSONArray();
        tablejson.put("header", headers);
        for (List<DocumentElement> row: table.getHeaders()) {
            curRow = new JSONArray();
            headers.add(curRow);
            for (DocumentElement de: row) {
                rowspan = 1;
                colspan = 1;
                if (de != null) {
                    if (de instanceof DBTableEntry)
                        de.accept(this);
                    else {
                        curCell = new JSONArray();
                        de.accept(this);
                        JSONObject entry = new JSONObject();
                        entry.put("content", curCell);
                        addSpans(entry);
                        curRow.add(entry);
                    }
                }
            }
        }
        if (table.getStyle() == null)
            tablejson.put("style", "normal");
        else
            tablejson.put("style", table.getStyle());
        tablejson.put("title", table.getTitle());
        tablejson.put("type", "Table");
        tablejson.put("sources", tableelements);
        // gl.log( "tablejson =\n" + tablejson );
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBTableEntry tableentry) { // TODO; check for
                                                 // informaltable and ignore it
        if (tableentry.getMorerows() > 0) {
            rowspan = tableentry.getMorerows() + 1;
        }
        if (tableentry.getNamest() != null && tableentry.getNameend() != null) {
            try {
                int startcol = Integer.parseInt(tableentry.getNamest());
                int endcol = Integer.parseInt(tableentry.getNameend());
                colspan = endcol - startcol + 1;
            } catch (Exception e) {

            }
        }
        curCell = new JSONArray();
        for (DocumentElement de: tableentry.getChildren()) {
            de.accept(this);
        }
        JSONObject entry = new JSONObject();
        entry.put("content", curCell);
        addSpans(entry);
        curRow.add(entry);
    }

    @Override
    public void visit(DBColSpec colspec) {

    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBList list) {
        DBEditListVisitor listv = new DBEditListVisitor(this.recurse, this.elements);
        list.accept(listv);
        tableelements.addAll(listv.getListElements());
        curCell.add(listv.getObject());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBParagraph para) {
        JSONObject entry = getJSONForDBParagraph(para);
        if (para.getFrom() != null && para.getFromProperty() != null) {
            this.tableelements.add(para.getFrom().getID());
        }
        curCell.add(entry);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBText text) {
        JSONObject entry = getJSONForDBText(text);
        if (text.getFrom() != null && text.getFromProperty() != null) {
            this.tableelements.add(text.getFrom().getID());
        }
        curCell.add(entry);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBSimpleList simplelist) {
        DBHTMLVisitor html = new DBHTMLVisitor();
        simplelist.accept(html);
        JSONObject entry = new JSONObject();
        entry.put("source", "text");
        entry.put("text", html.getOut());
        curCell.add(entry);
    }

    @SuppressWarnings("unchecked")
    private void addSpans(JSONObject entry) {
        if (rowspan > 0)
            entry.put("rowspan", Integer.toString(rowspan));
        if (colspan > 0)
            entry.put("colspan", Integer.toString(colspan));
    }

    public JSONArray getTableElements() {
        return tableelements;
    }
}
