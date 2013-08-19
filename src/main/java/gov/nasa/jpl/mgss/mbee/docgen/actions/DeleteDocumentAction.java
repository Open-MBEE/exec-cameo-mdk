package gov.nasa.jpl.mgss.mbee.docgen.actions;

import gov.nasa.jpl.mgss.mbee.docgen.DocWebProfile;
import gov.nasa.jpl.mgss.mbee.docgen.viewedit.ViewEditUtils;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class DeleteDocumentAction extends MDAction {
	private Element proj;
	public static final String actionid = "DeleteDocument";
	
	public DeleteDocumentAction(Element e) {
		super(actionid, "Remove From View Editor", null, null);
		proj = e;
	}
	
	public void actionPerformed(ActionEvent e) {
		GUILog gl = Application.getInstance().getGUILog();
		String docid = proj.getID();
		if (StereotypesHelper.hasStereotypeOrDerived(proj, DocWebProfile.document)) {
			docid = (String)StereotypesHelper.getStereotypePropertyFirst(proj, DocWebProfile.document, "documentId");
		}
		String url = ViewEditUtils.getUrl();
		if (url == null || url.equals(""))
			return;
		url += "/rest/projects/document/" + proj.getID() + "/delete";
		PostMethod pm = new PostMethod(url);
		try {
			//pm.setRequestHeader("Content-Type", "text/json");
			//pm.setRequestEntity(JsonRequestEntity.create(vol.getID()));
			//Protocol easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 443);
			//Protocol.registerProtocol("https", easyhttps);
			HttpClient client = new HttpClient();
			client.executeMethod(pm);
			String code = pm.getResponseBodyAsString();
			if (code.equals("ok"))
				gl.log("[INFO] Remove Successful.");
			else if (code.equals("NotFound"))
				gl.log("[ERROR] Document not found.");
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
