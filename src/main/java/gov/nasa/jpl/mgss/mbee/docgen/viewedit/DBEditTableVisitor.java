package gov.nasa.jpl.mgss.mbee.docgen.viewedit;

import gov.nasa.jpl.mgss.mbee.docgen.DocGenUtils;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBColSpec;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBImage;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBList;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBListItem;
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

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;

public class DBEditTableVisitor extends DBEditDocwebVisitor {
	
	private JSONObject tablejson;
	private JSONArray curRow;
	private JSONArray tableelements;
	private int rowspan;
	private int colspan;
	
	//private GUILog gl = Application.getInstance().getGUILog();
	
	public DBEditTableVisitor(boolean recurse, Map<String, JSONObject> elements) {
		super(recurse);
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
				if (de != null)
					de.accept(this);
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
				if (de != null)
					de.accept(this);
			}
		}
		if (table.getStyle() == null)
			tablejson.put("style", "normal");
		else
			tablejson.put("style", table.getStyle());
		tablejson.put("title", table.getTitle());
		tablejson.put("type", "Table");
		tablejson.put("sources", tableelements);
		//gl.log( "tablejson =\n" + tablejson );
	}

	@SuppressWarnings("unchecked")
	@Override
	public void visit(DBTableEntry tableentry) {
		if (tableentry.getMorerows() > 0) {
			rowspan = tableentry.getMorerows() + 1;
		}
		if (tableentry.getNamest() != null && tableentry.getNameend() != null) {
			try {
				int startcol = Integer.parseInt(tableentry.getNamest());
				int endcol = Integer.parseInt(tableentry.getNameend());
				colspan = endcol-startcol+1;
			} catch (Exception e) {
				
			}
		}
		if (tableentry.getChildren().size() > 1) {
			DBHTMLVisitor html = new DBHTMLVisitor();
			for (DocumentElement de: tableentry.getChildren()) {
				de.accept(html);
			}
			JSONObject entry = new JSONObject();
			entry.put("source", "text");
			entry.put("text", html.getOut());
			addSpans(entry);
			curRow.add(entry);
		} else if (!tableentry.getChildren().isEmpty()){
			tableentry.getChildren().get(0).accept(this);
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
		} else {
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
		} else {
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
		if (rowspan > 0)
			entry.put("rowspan", Integer.toString(rowspan));
		if (colspan > 0)
			entry.put("colspan", Integer.toString(colspan));
	}
}
