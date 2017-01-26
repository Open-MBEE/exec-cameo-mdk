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
import gov.nasa.jpl.mbee.mdk.ems.actions.EMSLogoutAction;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import org.apache.http.client.utils.URIBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.IOException;
import java.net.URISyntaxException;

public class TicketUtils {
    
    private static String username = "";
    private static String password = "";
    private static String ticket = "";

    /**
     * Accessor for username field. Will attempt to display the login dialog to acquire a username if the field is empty.
     *
     * @return username
     */
    public static String getUsername() {
        return username;
    }

    /**
     * Accessor for ticket field. Will attempt to acquire a new ticket if the field is empty.
     *
     * @return ticket
     */
    public static String getTicket() {
        return ticket;
    }

    /**
     * Convenience method for checking if ticket is non-empty. Used as a shorthand to verify that a user is logged in to MMS
     *
     * @return ticket exists and is non-empty.
     */
    public static boolean isTicketSet() {
        return ticket != null && !ticket.isEmpty();
    }

    /**
     * Logs in to MMS, using pre-specified credentials or prompting the user for new credentials.
     *
     * If username and password have been pre-specified, will not display the dialog even if popups are
     * enabled. Else will display the login dialog and use the returned value.
     *
     * @return TRUE if successfully logged in to MMS, FALSE otherwise.
     *         Will always return FALSE if popups are disabled and username/password are not pre-specified
     */
    public static boolean loginToMMS(Project project) {
        if (!username.isEmpty() && !password.isEmpty()) {
            return acquireTicket(project, password);
        }
        else if (!Utils.isPopupsDisabled()) {
            return acquireTicket(project, getUserCredentialsDialog());
        }
        else {
            Application.getInstance().getGUILog().log("[ERROR] Unable to login to MMS. No credentials have been specified, and dialog popups are disabled.");
            return false;
        }
    }

    /**
     * Shows a login dialog window and uses its filled in values to set the username and password.
     * Stores the entered username for future use / convenience, passes the entered password to acquireTicket().
     */
    private static String getUserCredentialsDialog() {
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
        int response = JOptionPane.showConfirmDialog(Application.getInstance().getMainFrame(), userPanel,
                "MMS Credentials", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (response == JOptionPane.OK_OPTION) {
            username = usernameFld.getText();
            String pass = new String(passwordFld.getPassword());
            return pass;
        }
        else if (response == JOptionPane.CANCEL_OPTION) {
            Application.getInstance().getGUILog().log("[INFO] MMS login has been cancelled.");
        }
        return null;
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
        username = user;
        password = pass;
    }

    /**
     * Clears username, password, and ticket
     */
    public static void clearUsernameAndPassword() {
        username = "";
        password = "";
        ticket = "";
    }

    /**
     * Uses the stored username and passed password to query MMS for a ticket. Will clear any stored password on attempt.
     *
     * Will first check to see if there is an existing ticket, and if so if it is valid. If valid, will not resend
     * for new ticket. If invalid or not present, will send for new ticket.
     *
     * Since it can only be called by logInToMMS(), assumes that the username and password were recently
     * acquired from the login dialogue or pre-specified if that's disabled.
     */
    private static boolean acquireTicket(Project project, String pass) {
        //curl -k https://cae-ems-origin.jpl.nasa.gov/alfresco/service/api/login -X POST -H Content-Type:application/json -d '{"username":"username", "password":"password"}'
        password = "";
        if (pass == null) {
            return false;
        }

        //ticket is invalid, clear it and re-attempt;
        ticket = "";

        // build request
        URIBuilder requestUri = MMSUtils.getServiceUri(project);
        if (requestUri == null) {
            return false;
        }
        requestUri.setPath(requestUri.getPath() + "/api/login" + ticket);
        requestUri.clearParameters();
        ObjectNode credentials = JacksonUtils.getObjectMapper().createObjectNode();
        credentials.put("username", username);
        credentials.put("password", pass);

        // do request
        ObjectNode response = null;
        try {
            response = MMSUtils.sendMMSRequest(MMSUtils.buildRequest(MMSUtils.HttpRequestType.POST, requestUri, credentials));
        } catch (IOException | URISyntaxException e) {
            Application.getInstance().getGUILog().log("[ERROR] Unexpected error while acquiring credentials. Log in failed. Reason: " + e.getMessage());
            e.printStackTrace();
        } catch (ServerException e) {
            if (!showErrorMessage(e.getCode())) {
                Application.getInstance().getGUILog().log("[ERROR] Unexpected server error while acquiring credentials. Log in failed. Reason: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // parse response
        JsonNode value;
        if (response != null && (value = response.get("data")) != null && (value = value.get("ticket")) != null && value.isTextual()) {
            ticket = value.asText();
            return true;
        }
        Application.getInstance().getGUILog().log("[ERROR] Unable to log in to MMS with the supplied credentials.");
        return false;
    }

    /**
     * Helper method to determine if the ticket currently stored is still valid.
     *
     * @return True if ticket is still valid and matches the currently stored username
     */
    public static boolean isTicketValid(Project project) {
        //curl -k https://cae-ems-origin.jpl.nasa.gov/alfresco/service//mms/login/ticket/${TICKET}
        if (ticket == null || ticket.isEmpty()) {
            return false;
        }

        // build request
        URIBuilder requestUri = MMSUtils.getServiceUri(project);
        if (requestUri == null) {
            return false;
        }
        requestUri.setPath(requestUri.getPath() + "/mms/login/ticket/" + ticket);
        requestUri.clearParameters();

        // do request
        ObjectNode response = JacksonUtils.getObjectMapper().createObjectNode();
        boolean invalidCredentials = false;
        try {
            response = MMSUtils.sendMMSRequest(MMSUtils.buildRequest(MMSUtils.HttpRequestType.GET, requestUri));
        } catch (ServerException se) {
            if (se.getCode() == 404) {
                invalidCredentials = true;
            }
            else {
                Application.getInstance().getGUILog().log("[ERROR] Unexpected error while checking credentials. Reason: " + se.getMessage());
                se.printStackTrace();
            }
        } catch (IOException | URISyntaxException e ) {
            Application.getInstance().getGUILog().log("[ERROR] Unexpected error while checking credentials. Reason: " + e.getMessage());
            e.printStackTrace();
        }

        // parse response
        JsonNode value;
        if (invalidCredentials || ((value = response.get("username")) == null) || !value.isTextual() || !value.asText().equals(username)) {
            Application.getInstance().getGUILog().log("[WARNING] Stored credentials are invalid. You will be logged out of MMS, and will need to log in again.");
            EMSLogoutAction.logoutAction();
            return false;
        }
        return true;
    }

    // TODO @donbot verify need for this block
    /**
     * Error handling for common server codes.
     *
     * @param code Server code
     * @return True if a popup/log has been displayed for the user for the message, false otherwise (which implies that
     * we need to do additional error dumping)
     */
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
