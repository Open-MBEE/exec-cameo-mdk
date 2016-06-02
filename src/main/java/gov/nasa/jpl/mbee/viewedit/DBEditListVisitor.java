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
package gov.nasa.jpl.mbee.viewedit;

import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBList;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBListItem;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTable;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;

import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
@Deprecated
public class DBEditListVisitor extends DBEditDocwebVisitor {

    private JSONObject listjson;
    private JSONArray  listelements;
    private JSONArray  curitem;

    public DBEditListVisitor(boolean recurse, Map<String, JSONObject> e) {
        super(recurse, true);
        this.elements = e;
        listjson = new JSONObject();
        listelements = new JSONArray();
    }

    public JSONObject getObject() {
        return listjson;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBList list) {
        if (listjson.containsKey("type")) {
            DBEditListVisitor inner = new DBEditListVisitor(recurse, this.elements);
            list.accept(inner);
            curitem.add(inner.getObject());
            listelements.addAll(inner.getListElements());
        } else {
            listjson.put("type", "List");
            if (list.isOrdered())
                listjson.put("ordered", true);
            else
                listjson.put("ordered", false);
            listjson.put("bulleted", true);
            JSONArray l = new JSONArray();
            listjson.put("list", l);
            for (DocumentElement de: list.getChildren()) {
                curitem = new JSONArray();
                de.accept(this);
                l.add(curitem);
            }
            listjson.put("sources", listelements);
        }
    }

    @Override
    public void visit(DBListItem listitem) {
        for (DocumentElement de: listitem.getChildren()) {
            de.accept(this);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBParagraph para) {
        JSONObject o = getJSONForDBParagraph(para);
        if (para.getFrom() != null && para.getFromProperty() != null) {
            this.listelements.add(para.getFrom().getID());
        }
        curitem.add(o);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBText text) {
        JSONObject o = getJSONForDBText(text);
        if (text.getFrom() != null && text.getFromProperty() != null) {
            this.listelements.add(text.getFrom().getID());
        }
        curitem.add(o);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void visit(DBTable table) {
        DBEditTableVisitor2 v = new DBEditTableVisitor2(this.recurse, this.elements);
        table.accept(v);
        listelements.addAll(v.getTableElements());
        curitem.add(v.getObject());
    }

    public JSONArray getListElements() {
        return listelements;
    }
}
