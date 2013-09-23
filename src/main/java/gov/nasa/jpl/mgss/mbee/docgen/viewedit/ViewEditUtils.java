package gov.nasa.jpl.mgss.mbee.docgen.viewedit;

import gov.nasa.jpl.mbee.lib.Utils;

import java.awt.GridLayout;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

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
	private static String alf_ticket = "";
	private static boolean passwordSet = false;
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

	@Deprecated
	public static void setCredentials(HttpClient client) {
		setCredentials(client, "http://127.0.0.1:8080");
	}
	
	/**
	 * Sets credentials for the client based on the actual URL string
	 * @param client
	 * @param urlstring
	 */
	public static void setCredentials(HttpClient client, String urlstring) {
		try {
			URL url = new URL(urlstring);
			
			if (url.getProtocol().toLowerCase().equals("https") && !passwordSet) {
				// Pop up one time dialog for logging into Alfresco
				JPanel userPanel = new JPanel();
				userPanel.setLayout(new GridLayout(2,2));
		
				JLabel usernameLbl = new JLabel("Username:");
				JLabel passwordLbl = new JLabel("Password:");
		
				JTextField usernameFld = new JTextField();
				JPasswordField passwordFld = new JPasswordField();
		
				userPanel.add(usernameLbl);
				userPanel.add(usernameFld);
				userPanel.add(passwordLbl);
				userPanel.add(passwordFld);
		
				JOptionPane.showConfirmDialog(null, userPanel, "Enter your username and password for ViewEditor:"
		                      ,JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				
				username = usernameFld.getText();
				password = new String(passwordFld.getPassword());
				passwordSet = true;
			}
			
			Credentials creds = new UsernamePasswordCredentials(username, password);
			client.getState().setCredentials(new AuthScope(url.getHost(), url.getPort(), AuthScope.ANY_REALM), creds);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
	}	
}
