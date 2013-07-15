package gov.nasa.jpl.mgss.mbee.docgen.viewedit;

import java.util.Stack;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import gov.nasa.jpl.mgss.mbee.docgen.model.AbstractModelVisitor;
import gov.nasa.jpl.mgss.mbee.docgen.model.Document;
import gov.nasa.jpl.mgss.mbee.docgen.model.Section;

public class ViewHierarchyVisitor extends AbstractModelVisitor {

	private JSONObject result;
	private Stack<JSONArray> curChildren;
	private JSONArray nosections;
	
	public ViewHierarchyVisitor() {
		result = new JSONObject();
		curChildren = new Stack<JSONArray>();
		nosections = new JSONArray();
	}
	
	public JSONObject getResult() {
		JSONObject res = new JSONObject();
		res.put("views", result);
		res.put("noSections", nosections);
		return res;
	}
	
	@Override
	public void visit(Document doc) {
		if (doc.getDgElement() != null) {
			curChildren.push(new JSONArray());
		}
		visitChildren(doc);
		if (doc.getDgElement() != null) {
			result.put(doc.getDgElement().getID(), curChildren.pop());
		}
	}
	
	@Override
	public void visit(Section sec) {
		if (sec.isView()) {
			if (sec.isNoSection())
				nosections.add(sec.getDgElement().getID());
			if (!curChildren.isEmpty())
				curChildren.peek().add(sec.getDgElement().getID());
			curChildren.push(new JSONArray());
		}
		visitChildren(sec);
		if (sec.isView()) {
			result.put(sec.getDgElement().getID(), curChildren.pop());
		}
	}
}
