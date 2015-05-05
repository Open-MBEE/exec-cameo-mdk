/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 * 
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.mbee.viewedit;

import gov.nasa.jpl.mbee.lib.Utils;

import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.params.HttpMethodParams;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ViewEditUtils {
    private static final String       DEFAULT_EDITOR_CHOICE = "Community: http://docgen:8080/editor";
    private static String             editorurl             = null;
    private static String             otherurl              = "";
    private static String             username              = "";
    private static String             password              = "";
    private static boolean            passwordSet           = false;
    private static final List<String> servers               = Arrays.asList(
                                                                    "http://docgen.jpl.nasa.gov:8080/editor",
                                                                    // "https://europaems:8443/alfresco/service",
                                                                    "http://docgen.jpl.nasa.gov:8080/europa",
                                                                    "Other");
    private static final List<String> displays              = Arrays.asList(
                                                                    "Community: http://docgen:8080/editor",
                                                                    // "Europa Alfresco: https://europaems:8443/alfresco/service",
                                                                    "Europa Old: http://docgen:8080/europa",
                                                                    "Other");

    public static String getUrl() {
        return getUrl(true, false);
    }
    
    public static String getUrl(boolean choice) {
        return getUrl(choice, false);
    }
    
    public static String getUrl(boolean choice, boolean addsite) {
        //return null; 
        //return "https://sheldon.jpl.nasa.gov/alfresco/service";
        Boolean old = false;
        if (choice)
            old = Utils.getUserYesNoAnswer("Use old view editor?");
        if (old == null)
            return null;
        String url = null;
        if (old) {
            String chosen = editorurl == null ? DEFAULT_EDITOR_CHOICE : editorurl;
            url = Utils.getUserDropdownSelectionForString("Choose", "Choose View Editor Server", servers,
                    displays, chosen);
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
            if (StereotypesHelper.hasStereotype(model, "ModelManagementSystem")) {
                url = (String)StereotypesHelper.getStereotypePropertyFirst(model, "ModelManagementSystem",
                        "url");
                String site = (String)StereotypesHelper.getStereotypePropertyFirst(model, "ModelManagementSystem", "site");
                if (url == null || url.equals("")) {
                    JOptionPane
                            .showMessageDialog(null,
                                    "Your project root element doesn't have ModelManagementSystem url stereotype property set!");
                    return null;
                }
                if (addsite) {
                    if (site == null || site.equals("")) {
                        JOptionPane.showMessageDialog(null,
                            "Your project root element doesn't have ModelManagementSystem site stereotype property set!");
                        return null;
                    }
                    return url + "/javawebscripts/sites/" + site;
                }
            } else {
                JOptionPane
                        .showMessageDialog(null,
                                "Your project root element doesn't have ModelManagementSystem url stereotype property set!");
                return null;
            }
        }
        return url; 
    }

    /**
     * Sets credentials for the client based on the actual URL string
     * 
     * @param client
     * @param urlstring
     */
    public static void setCredentials(HttpClient client, String urlstring) {
        try {
            URL url = new URL(urlstring);

            if (!passwordSet) {
                // Pop up one time dialog for logging into Alfresco
                JPanel userPanel = new JPanel();
                userPanel.setLayout(new GridLayout(2, 2));

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
                    usernameFld.requestFocus();
                }
                if (password != null) {
                    passwordFld.setText(password);
                }
                makeSureUserGetsFocus(usernameFld);
                JOptionPane.showConfirmDialog(null, userPanel,
                        "Enter your username and password for ViewEditor:", JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE);

                username = usernameFld.getText();
                password = new String(passwordFld.getPassword());
                passwordSet = true;
            }

            Credentials creds = new UsernamePasswordCredentials(username, password);
            client.getState().setCredentials(
                    new AuthScope(url.getHost(), url.getPort(), AuthScope.ANY_REALM), creds);
            client.setTimeout(0);
            client.setConnectionTimeout(0);
            client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                    new DefaultHttpMethodRetryHandler(0, false));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }
    
    private static void makeSureUserGetsFocus(final JTextField user) {
        //from http://stackoverflow.com/questions/14096140/how-to-set-default-input-field-in-joptionpane
        user.addHierarchyListener(new HierarchyListener()
        {
            HierarchyListener hierarchyListener = this;

            @Override
            public void hierarchyChanged(HierarchyEvent e)
            {
                JRootPane rootPane = SwingUtilities.getRootPane(user);
                if (rootPane != null)
                {
                    final JButton okButton = rootPane.getDefaultButton();
                    if (okButton != null)
                    {
                        okButton.addFocusListener(new FocusAdapter()
                        {
                            @Override
                            public void focusGained(FocusEvent e)
                            {
                                if (!e.isTemporary())
                                {
                                    user.requestFocusInWindow();
                                    user.removeHierarchyListener(hierarchyListener);
                                    okButton.removeFocusListener(this);
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    public static void clearCredentials() {
        passwordSet = false;
        username = "";
        password = "";
    }

    public static boolean showErrorMessage(int code) {
        if (code == 401) {
            Utils.showPopupMessage("[ERROR] You may have entered the wrong credentials: You've been logged out, try again");
            ViewEditUtils.clearCredentials();
        } else if (code == 500)
            Utils.showPopupMessage("[ERROR] Server error occured, you may not have permission to modify view(s) or their contents");
        else if (code == 404)
            Utils.showPopupMessage("[ERROR] Some elements or views are not found on the server, export them first");
        if (code == 401 || code == 500)
            return true;
        return false;
    }
    
    public static boolean isPasswordSet() {
        return passwordSet;
    }
}
