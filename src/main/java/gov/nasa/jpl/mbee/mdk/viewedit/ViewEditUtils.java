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
package gov.nasa.jpl.mbee.mdk.viewedit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.params.HttpMethodParams;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

public class ViewEditUtils {
    
    private static String username = "";
    private static String password = "";
    private static boolean passwordSet = false;
    private static boolean loginDialogDisabled = false;
    private static String authStringEnc = "";
    private static String ticket = "";

    public static void showLoginDialog() {
        if (!loginDialogDisabled) {
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
            JOptionPane.showConfirmDialog(Application.getInstance().getMainFrame(), userPanel,
                    "MMS Credentials", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            setUsernameAndPassword(usernameFld.getText(), new String(passwordFld.getPassword()));
        }
    }

    private static void makeSureUserGetsFocus(final JTextField user) {
        //from http://stackoverflow.com/questions/14096140/how-to-set-default-input-field-in-joptionpane
        user.addHierarchyListener(new HierarchyListener() {
            HierarchyListener hierarchyListener = this;

            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                JRootPane rootPane = SwingUtilities.getRootPane(user);
                if (rootPane != null) {
                    final JButton okButton = rootPane.getDefaultButton();
                    if (okButton != null) {
                        okButton.addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusGained(FocusEvent e) {
                                if (!e.isTemporary()) {
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

    /**
     * utility for setting authorization header encoding at same time as username and password.
     */
    public static void setUsernameAndPassword(String uname, String pword) {
        if (uname == null || uname.equals("") 
                || uname == null || uname.equals("")) {
            username = "";
            password = "";
            authStringEnc = "";
            passwordSet = false;
            return;
        }
        username = uname;
        password = pword;
        String authString = username + ":" + password;
        byte[] authEncBytes = Base64.getEncoder().encode(authString.getBytes());
        authStringEnc = new String(authEncBytes);
        passwordSet = true;
    }

    /**
     * Sets credentials for the client based on the actual URL string and supplied strings.
     * Intended to bypass the confirmDialog generated in the primary method
     *
     * @param client
     * @param urlstring
     * @param method
     * @param username
     * @param password
     */
    @Deprecated
    public static void setCredentials(HttpClient client, String urlstring, HttpMethodBase method, String username, String password) {
        if (!passwordSet) {
            // setting the password here will cause us to skip the confirmDialog in the main setCredentials method
            setUsernameAndPassword(username, password);
        }
        setCredentials(client, urlstring, method);
    }

    public static void clearUsernameAndPassword() {
        setUsernameAndPassword("", "");
        setTicket("");
    }

    /**
     * Sets credentials for the client based on the actual URL string
     *
     * @param client
     * @param urlstring
     */
    public static String getEncodedCredentials() {
        if (!passwordSet) {
            showLoginDialog();
        }
        return authStringEnc;
    }

    /**
     * Sets credentials for the client based on the actual URL string
     *
     * @param client
     * @param urlstring
     */
    //TODO this modifies the passed in apache httpmethod. migrating to java HttpURLConnection, so this needs to go
    @Deprecated
    public static void setCredentials(HttpClient client, String urlstring, HttpMethodBase method) {
        try {
            URL url = new URL(urlstring);

            if (!passwordSet) {
                showLoginDialog();
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

        // proxy cache needs Authorization header
        method.addRequestHeader(new Header("Authorization", getAuthStringEnc()));
    }

    public static boolean showErrorMessage(int code) {
        if (code == 401) {
            Utils.showPopupMessage("[ERROR] You may have entered the wrong credentials: You've been logged out, try again");
            ViewEditUtils.clearUsernameAndPassword();
        }
        else if (code == 500) {
            Utils.showPopupMessage("[ERROR] Server error occured, you may not have permission to modify view(s) or their contents");
        }
        else if (code == 404) {
            Utils.showPopupMessage("[ERROR] Some elements or views are not found on the server, export them first");
        }
        return (code == 401 || code == 500);
    }

    public static boolean isPasswordSet() {
        return passwordSet;
    }

    public static String getAuthStringEnc() {
        return authStringEnc;
    }

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }

    public static String getTicket() {
        return ticket;
    }

    public static void setTicket(String _ticket) {
        ticket = _ticket;
    }

    public static boolean isLoginDialogDisabled() {
        return loginDialogDisabled;
    }

    public static void setLoginDialogDisabled(boolean loginDialogDisabled) {
        ViewEditUtils.loginDialogDisabled = loginDialogDisabled;
    }
}
