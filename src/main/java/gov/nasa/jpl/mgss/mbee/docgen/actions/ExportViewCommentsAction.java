package gov.nasa.jpl.mgss.mbee.docgen.actions;

import gov.nasa.jpl.mbee.docweb.JsonRequestEntity;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.generator.DocumentGenerator;
import gov.nasa.jpl.mgss.mbee.docgen.generator.DocumentValidator;
import gov.nasa.jpl.mgss.mbee.docgen.model.Document;
import gov.nasa.jpl.mgss.mbee.docgen.viewedit.ViewCommentVisitor;
import gov.nasa.jpl.mgss.mbee.docgen.viewedit.ViewEditUtils;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * export view comments to view editor
 * @author dlam
 *
 */
@SuppressWarnings("serial")
public class ExportViewCommentsAction extends MDAction {
	private Element doc;
	public static final String actionid = "ExportViewComments";
	public ExportViewCommentsAction(Element e) {
		super(actionid, "Export View Comments", null, null);
		doc = e;
	}
	
	public void actionPerformed(ActionEvent e) {
		GUILog gl = Application.getInstance().getGUILog();
		
		try {
			String response = null;
			Boolean recurse = Utils.getUserYesNoAnswer("Publish view comments recursively?");
			if (recurse == null)
				return;
			DocumentValidator dv = new DocumentValidator(doc);
			dv.validateDocument();
			dv.printErrors();
			if (dv.isFatal())
				return;
			DocumentGenerator dg = new DocumentGenerator(doc, null);
			Document dge = dg.parseDocument(true, recurse);
			ViewCommentVisitor vcv = new ViewCommentVisitor();
			dge.accept(vcv);
			String json = vcv.getJSON();
			
			//gl.log(json);
			String url = ViewEditUtils.getUrl();
			if (url == null)
				return;
			url += "/rest/views/" + doc.getID() + "/comments";
			if (recurse) {
				url += "?recurse=true";
			}
			gl.log("*** Starting export view comments ***");
			PostMethod pm = new PostMethod(url);
			try {
				gl.log("[INFO] Sending...");
				pm.setRequestHeader("Content-Type", "text/json;charset=utf-8");
				pm.setRequestEntity(JsonRequestEntity.create(json));
				HttpClient client = new HttpClient();
				ViewEditUtils.setCredentials(client);
				client.executeMethod(pm);
				response = pm.getResponseBodyAsString();
				//gl.log(response);
				if (response.equals("NotFound")) 
					gl.log("[ERROR] There are some views that are not exported yet, export the views first, then the comments");
				else if (response.equals("ok"))
					gl.log("[INFO] Export Successful.");
				else
					gl.log(response);
			
			} finally {
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

}
