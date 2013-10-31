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

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ViewEditUtils {
	private static final String DEFAULT_EDITOR_CHOICE = "Community: http://docgen:8080/editor";
	private static String editorurl = null;
	private static String otherurl = "";
	private static String username = "admin";
	private static String password = "admin";
	private static String alf_ticket = "";
	private static boolean passwordSet = false;
	private static final List<String> servers = Arrays.asList("http://docgen.jpl.nasa.gov:8080/editor", 
															 // "https://europaems:8443/alfresco/service",
															  "http://docgen.jpl.nasa.gov:8080/europa",   
															  "Other");
	private static final List<String> displays = Arrays.asList("Community: http://docgen:8080/editor",
			                          						//   "Europa Alfresco: https://europaems:8443/alfresco/service",
			                          						   "Europa Old: http://docgen:8080/europa",	
															   "Other");
	public static String getUrl() {
	    Boolean old = Utils.getUserYesNoAnswer("Use old view editor?");
	    if (old == null)
	        return null;
	    String url = null;
	    if (old) {
	        String chosen = editorurl == null ? DEFAULT_EDITOR_CHOICE : editorurl;
	        url = Utils.getUserDropdownSelectionForString("Choose", "Choose View Editor Server", servers, displays, chosen);
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
	    } else {
	        Element model = Application.getInstance().getProject().getModel();
	        if (StereotypesHelper.hasStereotype(model, "AlfrescoViewEditor")) {
	            url = (String)StereotypesHelper.getStereotypePropertyFirst(model, "AlfrescoViewEditor", "url");
	            if (url == null || url.equals("")) {
	                JOptionPane.showMessageDialog(null, "Your project root element doesn't have AlfrescoViewEditor url stereotype property set!");
	                return null;
	            }
	        } else {
	            JOptionPane.showMessageDialog(null, "Your project root element doesn't have AlfrescoViewEditor url stereotype property set!");
	            return null;
	        }
	    }
		return url;
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
				
				if (username != null) {
					usernameFld.setText(username);
				}
				if (password != null) {
					passwordFld.setText(password);
				}
		
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
	
	public static void clearCredentials() {
	    passwordSet = false;
	    username = "";
	    password = "";
	}
}
