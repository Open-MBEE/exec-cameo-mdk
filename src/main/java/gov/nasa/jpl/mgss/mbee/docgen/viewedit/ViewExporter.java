package gov.nasa.jpl.mgss.mbee.docgen.viewedit;

import gov.nasa.jpl.mbee.docweb.JsonRequestEntity;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.actions.ImportViewAction;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBBook;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.model.DocBookOutputVisitor;
import gov.nasa.jpl.mgss.mbee.docgen.model.Document;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

public class ViewExporter implements RunnableWithProgress{

	private Document dge;
	private Element doc;
	private boolean recurse;
	private boolean force;
	private String url;
	
	public ViewExporter(Document dge, Element doc, boolean recurse, boolean force, String url) {
		this.dge = dge;
		this.doc = doc;
		this.recurse = recurse;
		this.force = force;
		this.url = url;
	}
	
	/**
	 * Private utility for dumping the stack trace out to GUILog
	 */
	private void printStackTrace(Exception ex, GUILog gl) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		gl.log(sw.toString()); // stack trace as a string
		ex.printStackTrace();
	}
	
	@Override
	public void run(ProgressStatus arg0) {
		GUILog gl = Application.getInstance().getGUILog();
		arg0.setIndeterminate(true);
		boolean document = false;
		if (url == null)
			return;
		String baseurl = url;

		// first post view information View Editor
		baseurl += "/rest/views/" + doc.getID();
		
		if (StereotypesHelper.hasStereotypeOrDerived(doc, DocGen3Profile.documentViewStereotype))
			document = true;		
		
		DocBookOutputVisitor visitor = new DocBookOutputVisitor(true);
		dge.accept(visitor);
		DBBook book = visitor.getBook();
		if (book == null)
			return;
		String user = Utils.getUsername();
		baseurl += "?user=" + user;
		DBEditDocwebVisitor v = new DBEditDocwebVisitor(recurse);
		book.accept(v);
		String json = v.getJSON();
		//gl.log(json);
		if (recurse || document || force) {
			baseurl += "&";
			List<String> params = new ArrayList<String>();
			if (recurse)
				params.add("recurse=true");
			if (document)
				params.add("doc=true");
			if (force)
				params.add("force=true");
			baseurl += Utils.join(params, "&");
		}
		PostMethod pm = new PostMethod(baseurl);
		try {
			//gl.log(json);
			pm.setRequestHeader("Content-Type", "application/json;charset=utf-8");
			pm.setRequestEntity(JsonRequestEntity.create(json));
			HttpClient client = new HttpClient();
			ViewEditUtils.setCredentials(client);
			gl.log("[INFO] Sending...");
			client.executeMethod(pm);
			String response = pm.getResponseBodyAsString();
			if (response.equals("ok"))
				gl.log("[INFO] Export Successful.");
			else if (response.startsWith("[")) {
				ValidationSuite vs = new ValidationSuite("Changed Elements");
				ValidationRule vr = new ValidationRule("Changed Name", "Name of element has been changed on VE", ViolationSeverity.INFO);
				ValidationRule vr2 = new ValidationRule("Changed Doc", "Doc of element has been changed on VE", ViolationSeverity.INFO);
				ValidationRule vr3 = new ValidationRule("Changed Value", "Default Value of element has been changed on VE", ViolationSeverity.INFO);
				vs.addValidationRule(vr);
				vs.addValidationRule(vr2);
				vs.addValidationRule(vr3);
				for (Object o: (JSONArray)JSONValue.parse(response)) {
					String mdid = (String)((JSONObject)o).get("mdid");
					String type = (String)((JSONObject)o).get("type");
					BaseElement be = Application.getInstance().getProject().getElementByID(mdid);
					if (be != null && be instanceof Element) {
						if (type.equals("name"))
							vr.addViolation((Element)be, "name changed on VE");
						else if (type.equals("doc"))
							vr.addViolation((Element)be, "doc changed on VE");
						else
							vr.addViolation((Element)be, "default value changed on VE");
					}
				}
				Collection<ValidationSuite> cvs = new ArrayList<ValidationSuite>();
				cvs.add(vs);
				gl.log("[INFO] Export Successful.");
				if (vs.hasErrors()) {
					Utils.displayValidationWindow(cvs, "View Export Results (Changed Elements)");
					gl.log("[INFO] See changed element info in validation window.");
				}
			} else
				gl.log(response);
		} catch (Exception ex) {
			printStackTrace(ex, gl);
		} finally {
			pm.releaseConnection();
		}
/*
		// Upload images to view editor (JSON keys are specified in DBEditDocwebVisitor
		gl.log("[INFO] Updating Images...");
		Map<String, JSONObject> images = v.getImages();
		for (String key: images.keySet()) {
			String filename = (String)images.get(key).get("abspath");
			String cs = (String)images.get(key).get("cs");
			String extension = (String)images.get(key).get("extension");
			
			File imageFile = new File(filename);
			baseurl = url + "/rest/images/" + key + "?cs=" + cs + "&extension=" + extension;
			
			// check whether the image already exists
			GetMethod get = new GetMethod(baseurl);
			int status = 0;
			try {
				HttpClient client = new HttpClient();
				ViewEditUtils.setCredentials(client);
				gl.log("[INFO] Checking if imagefile exists... " + key + "_cs" + cs + extension);
				client.executeMethod(get);
				
				status= get.getStatusCode();
			} catch (Exception ex) {
				printStackTrace(ex, gl);
			} finally {
				get.releaseConnection();
			}
			
			if (status == HttpURLConnection.HTTP_OK) {
				gl.log("[INFO] Image file already exists, not uploading");
			} else {
				PostMethod post = new PostMethod(baseurl);
				try {
					post.setRequestEntity(new InputStreamRequestEntity(new FileInputStream(imageFile), imageFile.length()));
					HttpClient client = new HttpClient();
					gl.log("[INFO] Did not find image, uploading file... " + key + "_cs" + cs + extension);
					client.executeMethod(post);
					
					status = post.getStatusCode();
					if (status != HttpURLConnection.HTTP_OK) {
						gl.log("[ERROR] Could not upload image file to view editor");
					}
				} catch (Exception ex) {
					printStackTrace(ex, gl);
				} finally {
					post.releaseConnection();
				}
			}
		}
		
		// clean up the local images
		v.removeImages();
		*/
		//if synchronizing views
		if (!force) {
			ImportViewAction.doImportView(doc, true, recurse, url);
		}
	}
}
