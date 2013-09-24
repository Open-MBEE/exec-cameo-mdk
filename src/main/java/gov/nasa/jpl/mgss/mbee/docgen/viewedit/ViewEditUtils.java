package gov.nasa.jpl.mgss.mbee.docgen.viewedit;

import gov.nasa.jpl.mbee.lib.Utils;

import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

public class ViewEditUtils {
	private static final String DEFAULT_EDITOR_CHOICE = "Community: http://docgen:8080/editor";
	private static String editorurl = null;
	private static String otherurl = "";
	private static String username = "admin";
	private static String password = "admin";
	private static final List<String> servers = Arrays.asList("http://docgen.jpl.nasa.gov:8080/editor", 
															  "http://docgen.jpl.nasa.gov:8080/europa", 
															  "http://docgen.jpl.nasa.gov:8080/staging", 
															  "http://localhost:8080/editor",
															  "http://localhost:8080/view-repo/service",
															  "Other");
	private static final List<String> displays = Arrays.asList("Community: http://docgen:8080/editor",
															   "Europa: http://docgen:8080/europa",
															   "Staging: http://docgen:8080/staging",
															   "Local-dev: http://localhost:8080/editor",
															   "Local-alfresco: http://localhost:8080/view-repo/service",
															   "Other");
	public static String getUrl() {
		String chosen = editorurl == null ? DEFAULT_EDITOR_CHOICE : editorurl;
		String url = Utils.getUserDropdownSelectionForString("Choose", "Choose View Editor Server", servers, displays, chosen);
		if (url == null) {
			return url;
		}
		editorurl = displays.get(servers.indexOf(url));
		if (url.equals("Other")) {
			String other = JOptionPane.showInputDialog("Enter the editor URL:", otherurl);
			if (other != null)
				otherurl = other;
			return other;
		}
		return url;
	}
	
	public static void setCredentials(HttpClient client) {
		Credentials defaultcreds = new UsernamePasswordCredentials(username, password);
		client.getState().setCredentials(new AuthScope("localhost", 8080, AuthScope.ANY_REALM), defaultcreds);
	}
}
