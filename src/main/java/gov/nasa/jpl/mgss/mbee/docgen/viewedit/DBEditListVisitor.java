package gov.nasa.jpl.mgss.mbee.docgen.viewedit;

import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBList;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBListItem;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBSimpleList;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTable;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;

import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class DBEditListVisitor extends DBEditDocwebVisitor {

	private JSONObject listjson;
	private JSONArray listelements;
	private JSONArray curitem;
	
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

	@Override
	public void visit(DBParagraph para) {
		JSONObject o = getJSONForDBParagraph(para);
		if (para.getFrom() != null && para.getFromProperty() != null) {
            this.listelements.add(para.getFrom().getID()); 
        } 
		curitem.add(o);
	}

	@Override
	public void visit(DBText text) {
		JSONObject o = getJSONForDBText(text);
		if (text.getFrom() != null && text.getFromProperty() != null) {
            this.listelements.add(text.getFrom().getID()); 
        } 
		curitem.add(o);
	}
	
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
