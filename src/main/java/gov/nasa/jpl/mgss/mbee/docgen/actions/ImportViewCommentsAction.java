package gov.nasa.jpl.mgss.mbee.docgen.actions;

import gov.nasa.jpl.mbee.docweb.JsonRequestEntity;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.sync.CommentChangeListener;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;
import gov.nasa.jpl.mgss.mbee.docgen.viewedit.ViewEditUtils;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.impl.ElementsFactory;

/**
 * import changed comments from view editor
 * @author dlam
 *
 */
@SuppressWarnings("serial")
public class ImportViewCommentsAction extends MDAction {

	private Element doc;
	public static final String actionid = "ImportViewComments";
	
	public ImportViewCommentsAction(Element e) {
		super(actionid, "Import View Comments", null, null);
		doc = e;
	}
	
	public void actionPerformed(ActionEvent e) {
		//get changed elements for this view (optionally recursive) from database
		//try to change the model, ask users?
		//for ones that are changed, tell database they're committed (assume user will commit)
		GUILog gl = Application.getInstance().getGUILog();
		try {
			Boolean recurse = Utils.getUserYesNoAnswer("Import view comments recursively?");
			if (recurse == null)
				return;
			String url = ViewEditUtils.getUrl();
			if (url == null)
				return;
			String geturl = url + "/rest/views/" + doc.getID() + "/comments";
			if (recurse)
				geturl += "?recurse=true";
			gl.log("*** Starting import view comments ***");
			GetMethod gm = new GetMethod(geturl);
			PostMethod pm = new PostMethod(url + "/rest/comments/committed");
			try {	
				HttpClient client = new HttpClient();
				client.executeMethod(gm);
				ViewEditUtils.setCredentials(client);
				String json = gm.getResponseBodyAsString();	
				if (json.equals("{}")) {
					gl.log("[INFO] There are no comments to import.");
					return;
				}
				if (json.equals("NotFound")) {
					gl.log("[ERROR] This view is not on view editor yet.");
					return;
				}
				JSONObject changed = change(json);
				gl.log("[INFO] Notifying view editor of imported comments");
				pm.setRequestHeader("Content-Type", "text/json");
				pm.setRequestEntity(JsonRequestEntity.create(changed.toJSONString()));
				//gl.log(changed.toJSONString());
				client.executeMethod(pm);
				String res = pm.getResponseBodyAsString();
				if (res.equals("ok"))
					gl.log("[INFO] View editor notified of imported comments");
				else
					gl.log(res);
			} finally {
				gm.releaseConnection();
				pm.releaseConnection();
			}	
		} catch (Exception ex) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			gl.log(sw.toString()); // stack trace as a string
			ex.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject change(String json) {
		JSONObject res = new JSONObject(); //this is the mapping from non imported comments to new created comment ids, or just ids whose changes have been imported
		JSONObject changed = (JSONObject)JSONValue.parse(json);
		GUILog gl = Application.getInstance().getGUILog();
		Project p = Application.getInstance().getProject();
		ElementsFactory ef = p.getElementsFactory();
		Stereotype vc = StereotypesHelper.getStereotype(p, "ViewComment");
		CommentChangeListener.disable(); //stereotypes are handled within this action, listener would double process it which we don't want
		int created = 0;
		int modified = 0;
		int numdeleted = 0;
		int cannot = 0;
		int notfound = 0;
		try {
			ValidationSuite vs = new ValidationSuite("Import View Comments");
			ValidationRule createdR = new ValidationRule("Create", "Comment created", ViolationSeverity.INFO);
			ValidationRule modifiedR = new ValidationRule("Modified", "Comment modified", ViolationSeverity.INFO);
			ValidationRule skippedR = new ValidationRule("Skipped", "Comment not editable", ViolationSeverity.INFO);
			vs.addValidationRule(createdR);
			vs.addValidationRule(modifiedR);
			vs.addValidationRule(skippedR);
			SessionManager.getInstance().createSession("import view comment");
			JSONArray comments = (JSONArray)changed.get("comments");
			for (Object comment: comments) {
				JSONObject commento = (JSONObject)comment;
				String id = (String)commento.get("id");
				String body = (String)commento.get("body");
				String user = (String)commento.get("author");
				String time = (String)commento.get("modified");
				Boolean deleted = (Boolean)commento.get("deleted");
				if (id.startsWith("comment")) {
					if (deleted) {
						res.put(id, id);
						continue; //this was never imported but was deleted before it can be imported, ignore
					}
					Comment newc = ef.createCommentInstance();
					newc.setOwner(doc);
					newc.setBody(Utils.addHtmlWrapper(body));
					StereotypesHelper.addStereotype(newc, vc);
					StereotypesHelper.setStereotypePropertyValue(newc, vc, "author", user);
					StereotypesHelper.setStereotypePropertyValue(newc, vc, "timestamp", time);
					created++;
					createdR.addViolation(newc, "Comment Created");
					res.put(id, newc.getID());
				} else {
					BaseElement be = p.getElementByID(id);
					if (be == null || !(be instanceof Comment)) {
						if (deleted)
							continue; //comment is deleted on web and not found in model...
						gl.log("[ERROR] Comment with id " + id + " not found in model! May have been deleted or you need to update your project.");
						notfound++;
						//res.put(id, id); //this is so future imports won't get this again....but maybe user just didn't update project? this will set the database side to committed
						continue;
					}
					
					Comment ec = (Comment)be;
					if (deleted) {
						if (!ec.isEditable()) {
							skippedR.addViolation(ec, "Cannot be deleted because not editable");
							cannot++;
							continue;
						}
						ModelElementsManager.getInstance().removeElement(ec);
						numdeleted++;
						res.put(id, id);
						continue;
					}
					if (!Utils.stripHtmlWrapper(ec.getBody()).equals(body)) {
						if (!ec.isEditable()) {
							skippedR.addViolation(ec, "Cannot be modified because not editable");
							//gl.log("[ERROR] The comment with id " + ec.getID() + " is not editable.");
							cannot++;
							continue;
						}
						String modeltime = (String)StereotypesHelper.getStereotypePropertyFirst(ec, vc, "timestamp");
						if (modeltime == null || modeltime.equals("") || modeltime.compareTo(time) < 0) {
							ec.setBody(Utils.addHtmlWrapper(body));
							StereotypesHelper.setStereotypePropertyValue(ec, vc, "author", user);
							StereotypesHelper.setStereotypePropertyValue(ec, vc, "timestamp", time);
							modified++;
							modifiedR.addViolation(ec, "Comment updated");
							res.put(id, id);
						}
					}
				}
			}
			JSONObject view2comment = (JSONObject)changed.get("view2comment");
			for (String viewid: (Set<String>)view2comment.keySet()) {
				BaseElement view = p.getElementByID(viewid);
				if (view == null) {
					gl.log("[ERROR] Cannot find view with id " + viewid);
					continue;
				}
				JSONArray commentids = (JSONArray)view2comment.get(viewid);
				if (view.isEditable() && ModelHelper.getComment((Element)view).equals(""))
					ModelHelper.setComment((Element)view, " "); //if an element doesn't already have documentation, any comment we annotate it with becomes its documentation, which we don't want.
				for (String commentid: (List<String>)commentids) {
					BaseElement comment = p.getElementByID(commentid);
					if (commentid.startsWith("comment") && res.containsKey(commentid)) {
						comment = p.getElementByID((String)res.get(commentid));
					}
					if (comment == null || !(comment instanceof Comment)) {
						continue;
					} else {
						Element owner = ((Element)view).getOwner();
						if (comment.isEditable()) //put comment as the sibling of the view it's commented on (instead of inside) so it has no chance of becoming a documentation
							((Comment)comment).setOwner(owner);
					}
					if (!((Comment)comment).getAnnotatedElement().contains(view)) {
						if (!comment.isEditable()) {
							//? if cannot attach comment, should this count as committed?
						} else
							((Comment)comment).getAnnotatedElement().add((Element)view);
					}
				}
			}
			SessionManager.getInstance().closeSession();
			Collection<ValidationSuite> cvs = new ArrayList<ValidationSuite>();
			cvs.add(vs);
			Utils.displayValidationWindow(cvs, "Import View Comments Results");
			gl.log("[INFO] Created: " + created);
			gl.log("[INFO] Modified: " + modified);
			gl.log("[INFO] Deleted: " + numdeleted);
			gl.log("[INFO] Not Found: " + notfound);
			gl.log("[INFO] Skipped (not editable): " + cannot);
		} catch (Exception ex) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			gl.log(sw.toString()); // stack trace as a string
			ex.printStackTrace();
			SessionManager.getInstance().cancelSession();
			res.clear();
		}
		CommentChangeListener.enable();
		return res;
	}
}
