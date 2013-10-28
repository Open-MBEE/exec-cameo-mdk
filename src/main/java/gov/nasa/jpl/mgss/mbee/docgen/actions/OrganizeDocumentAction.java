package gov.nasa.jpl.mgss.mbee.docgen.actions;

import gov.nasa.jpl.mbee.docweb.JsonRequestEntity;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.DocWebProfile;
import gov.nasa.jpl.mgss.mbee.docgen.generator.DocumentGenerator;
import gov.nasa.jpl.mgss.mbee.docgen.generator.DocumentValidator;
import gov.nasa.jpl.mgss.mbee.docgen.model.Document;
import gov.nasa.jpl.mgss.mbee.docgen.viewedit.ViewEditUtils;
import gov.nasa.jpl.mgss.mbee.docgen.viewedit.ViewHierarchyVisitor;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class OrganizeDocumentAction extends MDAction {

	private Element doc;
	public static final String actionid = "OrganizeDocument";
	
	public OrganizeDocumentAction(Element e) {
		super(actionid, "Add To View Editor", null, null);
		doc = e;
	}
	
	public void actionPerformed(ActionEvent e) {
		GUILog gl = Application.getInstance().getGUILog();
		List<Element> vols = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(doc, DocWebProfile.inVolume, 1, false, 1);
		if (vols.isEmpty()) {
			gl.log("[ERROR] This document is not attached to any volume");
			return;
		}
		if (vols.size() > 1) {
			gl.log("[ERROR] A document cannot have more than 1 parent volume");
			return;
		}
		Element vol = vols.get(0);
		if (!StereotypesHelper.hasStereotypeOrDerived(vol, DocWebProfile.volume)) {
			gl.log("[ERROR] The related element is not a volume");
			return;
		}
		String url = ViewEditUtils.getUrl();
		if (url == null || url.equals(""))
			return;
		url += "/rest/projects/document/" + doc.getID();
		PostMethod pm = new PostMethod(url);
		try {
			pm.setRequestHeader("Content-Type", "text/plain");
			pm.setRequestEntity(JsonRequestEntity.create(vol.getID()));
			//Protocol easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 443);
			//Protocol.registerProtocol("https", easyhttps);
			HttpClient client = new HttpClient();
			ViewEditUtils.setCredentials(client, url);
			client.executeMethod(pm);
			String code = pm.getResponseBodyAsString();
			if (code.equals("ok"))
				gl.log("[INFO] Export Successful.");
			else if (code.equals("NotFound"))
				gl.log("[ERROR] Volume not found, export project hierarchy first");
			else
				gl.log(code);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally{
			pm.releaseConnection();
		}
	}
}
