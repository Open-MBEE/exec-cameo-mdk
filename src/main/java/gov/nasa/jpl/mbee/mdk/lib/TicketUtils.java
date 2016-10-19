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
    private static String password = "";
    private static boolean passwordSet = false;
    private static String encodedCredentials = "";
    private static String ticket = "";

    /**
     * Accessor for username field. Will attempt to display the login dialog to acquire a username if the field is empty.
     *
     * @return username
     */
    public static String getUsername() {
        if ((username == null || username.isEmpty()) && !Utils.isPopupsDisabled()) {
            showLoginDialog();
        }
        return username;
    }

    /**
     * Accessor for ticket field. Will attempt to acquire a new ticket if the field is empty.
     *
     * @return
     */
    public static String getTicket() {
        if (ticket == null || ticket.isEmpty()) {
            acquireTicket();
        }
        return ticket;
    }

    public static boolean isPasswordSet() {
        return passwordSet;
    }

    /**
     *
     * @return True if successfully logged in to MMS
     */
    public static boolean loginToMMS() {
        if (!Utils.isPopupsDisabled()) {
            showLoginDialog();
        }
        acquireTicket();
        return !ticket.isEmpty();
    }

    /**
     * Shows a login dialog window and uses its filled in values to set the username and password.
     */
    private static void showLoginDialog() {
        // Pop up dialog for logging into Alfresco
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
        String user = usernameFld.getText();
        String pass = new String(passwordFld.getPassword());
        setUsernameAndPassword(user, pass);
    }

    /**
     * Forces focus to a particular JTextField in a displayed dialog
     *
     * @param field The field to force into focus
     */
    private static void makeSureUserGetsFocus(final JTextField field) {
        //from http://stackoverflow.com/questions/14096140/how-to-set-default-input-field-in-joptionpane
        field.addHierarchyListener(new HierarchyListener() {
            HierarchyListener hierarchyListener = this;

            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                JRootPane rootPane = SwingUtilities.getRootPane(field);
                if (rootPane != null) {
                    final JButton okButton = rootPane.getDefaultButton();
                    if (okButton != null) {
                        okButton.addFocusListener(new FocusAdapter() {
                            @Override
                            public void focusGained(FocusEvent e) {
                                if (!e.isTemporary()) {
                                    field.requestFocusInWindow();
                                    field.removeHierarchyListener(hierarchyListener);
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
     * Method to set username and password for logging in to MMS. Can be called directly to pre-set the credential
     * information for automation, but should be used with Utils.disablePopups(true) to prevent display of the standard
     * log in window. Use without disabling popups will cause these values to be overwritten by the values obtained
     * from the popup window call.
     *
     */
    public static void setUsernameAndPassword(String user, String pass) {
        if (user == null)
            user = "";
        if (pass == null) {
            pass = "";
        }
        passwordSet = false;
        username = user;
        password = pass;
        String authString = user + ":" + pass;
        byte[] authEncBytes = Base64.getEncoder().encode(authString.getBytes());
        encodedCredentials = new String(authEncBytes);
    }

    /**
     * Convenience method for clearing username and password.
     */
    public static void clearUsernameAndPassword() {
        setUsernameAndPassword("", "");
    }

    /**
     * Uses the stored username and password to query MMS for a ticket. Will first check to see if an existing ticket is
     * still valid, and will not resend for the ticket if it remains valid.
     *
     * Since it can only be called by logInToMMS(), assumes that the username and password were recently
     * acquired from the login dialogue or pre-specified if that's disabled.
     */
    private static void acquireTicket() {
        //curl -k https://cae-ems-origin.jpl.nasa.gov/alfresco/service/api/login -X POST -H Content-Type:application/json -d '{"username":"username", "password":"password"}'
        if ((ticket != null) && !ticket.isEmpty() && checkAcquiredTicket()) {
            return;
        }

        //ticket is invalid, clear it and re-attempt;
        ticket = "";
        passwordSet = false;

        // build request
        // @donbot retained Application.getInstance().getProject() instead of making project agnostic because you can only
        // log in to the currently opening project
        Project project = Application.getInstance().getProject();
        URIBuilder requestUri = MMSUtils.getServiceUri(project);
        if (requestUri == null || username.isEmpty() || password.isEmpty()) {
            return;
        }
        requestUri.setPath(requestUri.getPath() + "/api/login" + ticket);
        requestUri.clearParameters();
        ObjectNode credentials = JacksonUtils.getObjectMapper().createObjectNode();
        credentials.put("username", username);
        credentials.put("password", password);

        // do request
        ObjectNode response = null;
        try {
            response = MMSUtils.sendMMSRequest(MMSUtils.buildRequest(MMSUtils.HttpRequestType.POST, requestUri, credentials));
        } catch (IOException | URISyntaxException e) {
            Application.getInstance().getGUILog().log("[ERROR] Unexpected error while acquiring credentials. Reason: " + e.getMessage());
            e.printStackTrace();
        } catch (ServerException e) {
            if (!showErrorMessage(e.getCode())) {
                Application.getInstance().getGUILog().log("[ERROR] Unexpected server error while acquiring credentials. Reason: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // parse response
        JsonNode value;
        if (response != null && (value = response.get("data")) != null
                && (value = value.get("ticket")) != null && value.isTextual()) {
            ticket = value.asText();
            passwordSet = true;
        }
        return;
    }

    /**
     * Helper method to determine if the ticket currently stored is still valid.
     *
     * @return True if ticket is still valid and matches the currently stored username
     */
    private static boolean checkAcquiredTicket() {
        //curl -k https://cae-ems-origin.jpl.nasa.gov/alfresco/service//mms/login/ticket/${TICKET}
        boolean print = MDKOptionsGroup.getMDKOptions().isLogJson();

        if (ticket == null || ticket.isEmpty()) {
            return false;
        }

        // build request
        // @donbot retained Application.getInstance().getProject() instead of making project agnostic because you can only
        // log in to the currently opening project
        Project project = Application.getInstance().getProject();
        URIBuilder requestUri = MMSUtils.getServiceUri(project);
        if (requestUri == null) {
            return false;
        }
        requestUri.setPath(requestUri.getPath() + "/mms/login/ticket/" + ticket);
        requestUri.clearParameters();

        // do request
        ObjectNode response = JacksonUtils.getObjectMapper().createObjectNode();
        try {
            response = MMSUtils.sendMMSRequest(MMSUtils.buildRequest(MMSUtils.HttpRequestType.GET, requestUri));
        } catch (IOException | URISyntaxException | ServerException e) {
            // this should never throw a server error, so we're not going to pass it through standard handling
            Application.getInstance().getGUILog().log("[ERROR] Unexpceted error while checking credentials. Reason: " + e.getMessage());
            e.printStackTrace();
        }

        // parse response
        JsonNode value;
        if (((value = response.get("username")) != null) && value.isTextual() && value.asText().equals(username)) {
            return true;
        }
        return false;
    }

    /**
     * Error handling for common server codes.
     *
     * @param code Server code
     * @return True if a popup/log has been displayed for the user for the message, false otherwise (which implies that
     * we need to do additional error dumping)
     */
    // @donbot - consider merging with MMSUtils.DisplayErrors thing
    private static boolean showErrorMessage(int code) {
        if (code == 400 || code == 401 || code == 403) {
            Utils.showPopupMessage("[ERROR] You could not be logged in, and may have entered the wrong credentials. Please again.");
            clearUsernameAndPassword();
        }
        else if (code == 500) {
            Utils.showPopupMessage("[ERROR] Server error occured, you may not have permission to modify view(s) or their contents");
        }
        else if (code == 404) {
            Utils.showPopupMessage("[ERROR] Some elements or views are not found on the server, export them first");
        }
        return (code == 400 || code == 401 || code == 403 || code == 500);
    }


}
