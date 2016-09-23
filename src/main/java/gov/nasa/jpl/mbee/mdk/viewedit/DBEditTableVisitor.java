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
package gov.nasa.jpl.mbee.mdk.viewedit;

import gov.nasa.jpl.mbee.mdk.DocGenUtils;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

@Deprecated
public class DBEditTableVisitor extends DBEditDocwebVisitor {

    private JSONObject tablejson;
    private JSONArray curRow;
    private JSONArray tableelements;
    private int rowspan;
    private int colspan;

    // private GUILog gl = Application.getInstance().getGUILog();

    public DBEditTableVisitor(boolean recurse, Map<String, JSONObject> elements) {
        super(recurse, false);
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
        for (List<DocumentElement> row : table.getBody()) {
            curRow = new JSONArray();
            body.add(curRow);
            for (DocumentElement de : row) {
                rowspan = 1;
                colspan = 1;
                if (de != null) {
                    de.accept(this);
                }
            }
        }
        JSONArray headers = new JSONArray();
        tablejson.put("header", headers);
        for (List<DocumentElement> row : table.getHeaders()) {
            curRow = new JSONArray();
            headers.add(curRow);
            for (DocumentElement de : row) {
                rowspan = 1;
                colspan = 1;
                if (de != null) {
                    de.accept(this);
                }
            }
        }
        if (table.getStyle() == null) {
            tablejson.put("style", "normal");
        }
        else {
            tablejson.put("style", table.getStyle());
        }
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
        if (tableentry.getChildren().size() > 1) {
            DBHTMLVisitor html = new DBHTMLVisitor();
            for (DocumentElement de : tableentry.getChildren()) {
                de.accept(html);
            }
            JSONObject entry = new JSONObject();
            entry.put("source", "text");
            entry.put("text", html.getOut());
            addSpans(entry);
            curRow.add(entry);
        }
        else if (!tableentry.getChildren().isEmpty()) {
            tableentry.getChildren().get(0).accept(this);
        }
        else {
            JSONObject entry = new JSONObject();
            entry.put("source", "text");
            entry.put("text", "");
            addSpans(entry);
            curRow.add(entry);
        }

    }

    @Override
    public void visit(DBColSpec colspec) {

    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBList list) {
        DBHTMLVisitor html = new DBHTMLVisitor();
        list.accept(html);
        JSONObject entry = new JSONObject();
        entry.put("source", "text");
        entry.put("text", html.getOut());
        addSpans(entry);
        curRow.add(entry);
    }

    @Override
    public void visit(DBListItem listitem) {

    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBParagraph para) {
        JSONObject entry = new JSONObject();
        if (para.getFrom() != null && para.getFromProperty() != null) {
            this.addToElements(para.getFrom(), false);
            this.tableelements.add(para.getFrom().getID());
            entry.put("source", para.getFrom().getID());
            entry.put("useProperty", para.getFromProperty().toString());
        }
        else {
            entry.put("source", "text");
            entry.put("text", DocGenUtils.addP(DocGenUtils.fixString(para.getText(), false)));
        }
        addSpans(entry);
        curRow.add(entry);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBText text) {
        JSONObject entry = new JSONObject();
        if (text.getFrom() != null && text.getFromProperty() != null) {
            this.addToElements(text.getFrom(), false);
            this.tableelements.add(text.getFrom().getID());
            entry.put("source", text.getFrom().getID());
            entry.put("useProperty", text.getFromProperty().toString());
        }
        else {
            entry.put("source", "text");
            entry.put("text", DocGenUtils.fixString(text.getText(), false));
        }
        addSpans(entry);
        curRow.add(entry);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBSimpleList simplelist) {
        DBHTMLVisitor html = new DBHTMLVisitor();
        simplelist.accept(html);
        JSONObject entry = new JSONObject();
        entry.put("source", "text");
        entry.put("text", html.getOut());
        addSpans(entry);
        curRow.add(entry);
    }

    @SuppressWarnings("unchecked")
    private void addSpans(JSONObject entry) {
        if (rowspan > 0) {
            entry.put("rowspan", Integer.toString(rowspan));
        }
        if (colspan > 0) {
            entry.put("colspan", Integer.toString(colspan));
        }
    }
}
