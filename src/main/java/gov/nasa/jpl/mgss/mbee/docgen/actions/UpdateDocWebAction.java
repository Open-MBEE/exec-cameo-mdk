package gov.nasa.jpl.mgss.mbee.docgen.actions;

import gov.nasa.jpl.mbee.docweb.DocWebUpdater;
import gov.nasa.jpl.mbee.docweb.HttpsUtils;
import gov.nasa.jpl.mbee.lib.Utils;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

/**
 * this updates the category/project/module/document on docweb, should change to use ids in the future instead of name matching
 * @author dlam
 *
 */
@SuppressWarnings("serial")
public class UpdateDocWebAction extends MDAction {
	
	private Package root;
	public static final String actionid = "UpdateDocWeb";
	
	public UpdateDocWebAction(Package root) {
		super(actionid, "Update DocWeb", null, null);
		this.root = root;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent e) {
		HttpsUtils.allowSelfSignedCertificates();
		GUILog log = Application.getInstance().getGUILog();
		String url = JOptionPane.showInputDialog("Input/confirm the url of docweb without trailing slash (ex. https://docweb.jpl.nasa.gov/app", "https://docweb.jpl.nasa.gov/app");
		JSONObject servers = null;
		JSONArray projects = null;
		String chosenServer = null;
		String chosenProject = "";
		if (url != null && !url.equals("")) {
			GetMethod pm = new GetMethod(url + "/servers/");
			GetMethod pm2 = new GetMethod(url + "/projects/");
			try {
				HttpClient client = new HttpClient();
				int code = client.executeMethod(pm);
				if (code != 200) {
					log.log("bad: " + pm.getResponseBodyAsString());
					return;
				}
				String response = pm.getResponseBodyAsString();
				servers = (JSONObject)JSONValue.parse(response);
				List<String> names = new ArrayList<String>();
				List<String> display = new ArrayList<String>();
				for (Object server: servers.keySet()) {
					Object twport = servers.get(server);
					names.add((String)server);
					display.add((String)server + ": " + (String)twport);
				}
				chosenServer = Utils.getUserDropdownSelectionForString("Choose the server to update", "Choose the teamwork server to update", names, display);
				if (chosenServer == null)
					return;
				
				code = client.executeMethod(pm2);
				if (code != 200) {
					log.log("bad: " + pm2.getResponseBodyAsString());
					return;
				}
				response = pm2.getResponseBodyAsString();
				projects = (JSONArray)JSONValue.parse(response);
				chosenProject = Utils.getUserDropdownSelectionForString("Choose the project to update", "Choose the project to update", projects, projects);
				if (chosenProject == null)
					return; 
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
				log.log("url doesn't work");
				return;
			} catch (IOException e1) {
				e1.printStackTrace();
				log.log("cannot connect");
				return;
			} finally{
				pm.releaseConnection();
			}
		} else {
			log.log("url not good");
			return;
		}
		Application.getInstance().getGUILog().log(DocWebUpdater.publish(root, chosenServer, chosenProject, url + "/update/"));
		//below is for syncing with mdid and with category order, uncomment when ready
		//Application.getInstance().getGUILog().log(new DocWebUpdater().publish2(root, chosenServer, chosenProject, url + "/update/"));
	}
}
