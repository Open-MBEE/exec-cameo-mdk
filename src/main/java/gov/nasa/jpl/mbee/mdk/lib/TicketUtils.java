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
package gov.nasa.jpl.mbee.mdk.lib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;

import gov.nasa.jpl.mbee.mdk.ems.MMSUtils;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.options.MDKOptionsGroup;
import org.apache.http.client.utils.URIBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Base64;

public class TicketUtils {
    
    private static String username = "";
    private static boolean passwordSet = false;
    private static String encodedCredentials = "";
    private static String ticket = "";

    public static String getUsername() {
        return username;
    }

    public static String getTicket() {
        return ticket;
    }

    public static boolean isPasswordSet() {
        return passwordSet;
    }

    public static void showLoginDialog(Project project, String user, String pass) {
        if (!Utils.isPopupsDisabled()) {
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
            passwordFld.setText("");
            makeSureUserGetsFocus(usernameFld);
            JOptionPane.showConfirmDialog(Application.getInstance().getMainFrame(), userPanel,
                    "MMS Credentials", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            user = usernameFld.getText();
            pass = new String(passwordFld.getPassword());
        }
        setUsernameAndPassword(project, user, pass);
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
    public static void setUsernameAndPassword(Project project, String user, String pass) {
        if (user == null || user.equals("") || pass == null || pass.equals("")) {
            username = "";
            encodedCredentials = "";
            ticket = "";
            passwordSet = false;
            return;
        }
        if ( (ticket = getTicket(project, user, pass)).equals("") ) {
            setUsernameAndPassword(null, "", "");
        }
        username = user;
        String authString = user + ":" + pass;
        byte[] authEncBytes = Base64.getEncoder().encode(authString.getBytes());
        encodedCredentials = new String(authEncBytes);
        passwordSet = true;
    }

    public static void clearUsernameAndPassword() {
        setUsernameAndPassword(null, "", "");
    }

    public static String getTicket(Project project, String username, String password) {
        //curl -k https://cae-ems-origin.jpl.nasa.gov/alfresco/service/api/login -X POST -H Content-Type:application/json -d '{"username":"username", "password":"password"}'
        boolean print = MDKOptionsGroup.getMDKOptions().isLogJson();

        // build request
        URIBuilder requestUri = MMSUtils.getServiceUri(project);
        if (requestUri == null || username.equals("") || password.equals("")) {
            ticket = "";
            return ticket;
        }

        if (checkTicket(project)) {
            return ticket;
        }

        requestUri.setPath(requestUri.getPath() + "/api/login" + ticket);
        ObjectNode credentials = JacksonUtils.getObjectMapper().createObjectNode();
        credentials.put("username", username);
        credentials.put("password", password);

        // do request
        ObjectNode response = null;
        try {
            response = MMSUtils.sendMMSRequest(MMSUtils.buildRequest(MMSUtils.HttpRequestType.POST, requestUri, credentials));
        } catch (IOException | URISyntaxException e) {
            Application.getInstance().getGUILog().log("[ERROR] Unexpected error while getting credentials. Reason: " + e.getMessage());
            e.printStackTrace();
        } catch (ServerException e) {
            if (!showErrorMessage(e.getCode())) {
                e.printStackTrace();
            }
        }

        // parse response
        JsonNode value;
        if (response != null && (value = response.get("data")) != null
                && (value = value.get("ticket")) != null && value.isTextual()) {
            ticket = value.asText();
        }
        return ticket;
    }

    public static boolean checkTicket(Project project) {
        boolean print = MDKOptionsGroup.getMDKOptions().isLogJson();

        if (ticket == null || ticket.equals("")) {
            return false;
        }

        // build request
        URIBuilder requestUri = MMSUtils.getServiceUri(project);
        if (requestUri == null) {
            return false;
        }
        requestUri.setPath(requestUri.getPath() + "/mms/login/ticket/" + ticket);

        // do request
        ObjectNode response = JacksonUtils.getObjectMapper().createObjectNode();
        try {
            response = MMSUtils.sendMMSRequest(MMSUtils.buildRequest(MMSUtils.HttpRequestType.GET, requestUri));
        } catch (IOException | URISyntaxException e) {
            Application.getInstance().getGUILog().log("[ERROR] Unexpceted error while checking credentials. Reason: " + e.getMessage());
            e.printStackTrace();
        } catch (ServerException e) {
            if (!showErrorMessage(e.getCode())) {
                e.printStackTrace();
            }
        }

        // parse response
        JsonNode value;
        if ((value = response.get("username")) != null && value.isTextual()) {
            return value.asText().equals(username);
        }
        return false;
    }


    public static boolean showErrorMessage(int code) {
        if (code == 401) {
            Utils.showPopupMessage("[ERROR] You may have entered the wrong credentials: You've been logged out, try again");
            clearUsernameAndPassword();
        }
        else if (code == 500) {
            Utils.showPopupMessage("[ERROR] Server error occured, you may not have permission to modify view(s) or their contents");
        }
        else if (code == 404) {
            Utils.showPopupMessage("[ERROR] Some elements or views are not found on the server, export them first");
        }
        return (code == 401 || code == 500);
    }


}
