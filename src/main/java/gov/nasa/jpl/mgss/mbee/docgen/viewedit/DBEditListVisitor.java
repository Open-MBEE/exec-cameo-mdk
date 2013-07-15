package gov.nasa.jpl.mgss.mbee.docgen.viewedit;

import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBList;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBListItem;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBSimpleList;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;

import java.util.Map;

import org.json.simple.JSONObject;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class DBEditListVisitor extends DBEditDocwebVisitor {

	private JSONObject listjson;
	
	public DBEditListVisitor(boolean recurse, Map<String, JSONObject> e) {
		super(recurse);
		this.elements = e;
		listjson = new JSONObject();
	}

	public JSONObject getObject() {
		return listjson;
	}
	
	@Override
	public void visit(DBList list) {
		
	}

	@Override
	public void visit(DBListItem listitem) {
		
	}
	
	@Override
	public void visit(DBSimpleList simplelist) {
		
	}

	@Override
	public void visit(DBParagraph para) {
		
	}

	@Override
	public void visit(DBText text) {
		
	}
}
