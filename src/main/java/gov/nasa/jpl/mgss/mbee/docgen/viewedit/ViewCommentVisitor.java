package gov.nasa.jpl.mgss.mbee.docgen.viewedit;

import static gov.nasa.jpl.mgss.mbee.docgen.sync.CommentUtil.TIME_FORMAT;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.model.AbstractModelVisitor;
import gov.nasa.jpl.mgss.mbee.docgen.model.Document;
import gov.nasa.jpl.mgss.mbee.docgen.model.Section;
import gov.nasa.jpl.mgss.mbee.docgen.model.TableStructure;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * gets view comments and exports to view editor
 * @author dlam
 *
 */
public class ViewCommentVisitor extends AbstractModelVisitor {
	/*
{
	view2comment: {viewid:[mdid], ...} 
  	comments: [{id: mdid, body: body, author: user, modified: datetime}],
  	comment2comment: {commentid: [childids], ...} //not supported right now (for comment replies)
}
		
	 */
	private Map<String, JSONArray> view2comment;
	private JSONArray comments;
	private String user;
	
	public ViewCommentVisitor() {
		view2comment = new HashMap<String, JSONArray>();
		comments = new JSONArray();
		user = getCurrentUser();
	}
	
	@SuppressWarnings("unchecked")
	public String getJSON() {
		JSONObject out = new JSONObject();
		out.put("view2comment", view2comment);
		out.put("comments", comments);
		return out.toJSONString();
	}
	
	@Override
	public void visit(Document doc) {
		if (doc.getDgElement() != null)
			handleView(doc.getDgElement());
		visitChildren(doc);
	}
	
	@Override
	public void visit(Section sec) {
		if (sec.isView())
			handleView(sec.getDgElement());
		visitChildren(sec);
	}
	
	@SuppressWarnings("unchecked")
	private void handleView(Element view) {
		JSONArray commentIds = new JSONArray();
		for (Comment c: view.get_commentOfAnnotatedElement()) {
			if (StereotypesHelper.hasStereotypeOrDerived(c, "DocumentComment")) {
				addComment(c);
				commentIds.add(c.getID());
			}
		}
		view2comment.put(view.getID(), commentIds);
	}
	
	@SuppressWarnings("unchecked")
	private void addComment(Comment comment) {
		JSONObject c = new JSONObject();
		c.put("body", Utils.stripHtmlWrapper(comment.getBody()));
		c.put("id", comment.getID());
		String user = (String)StereotypesHelper.getStereotypePropertyFirst(comment, "DocumentComment", "author");
		String time = (String)StereotypesHelper.getStereotypePropertyFirst(comment, "DocumentComment", "timestamp");
		if (user == null || user.equals(""))
			user = this.user;
		if (time == null || time.equals(""))
			time = getCurrentTime();
		c.put("modified", time);
		c.put("author", user);
		comments.add(c);
	}
	
	private String getCurrentTime() {
		return TIME_FORMAT.format(new Date());
	}
	
	private String getCurrentUser() {
		String username;
		String teamworkUsername = TeamworkUtils.getLoggedUserName();
		if (teamworkUsername != null) {
			username = teamworkUsername;
		} else {
			username = System.getProperty("user.name", "");
		}
		return username;
	}

}
