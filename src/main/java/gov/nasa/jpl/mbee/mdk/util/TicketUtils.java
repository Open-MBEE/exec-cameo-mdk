package gov.nasa.jpl.mbee.mdk.util;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.mms.actions.MMSLogoutAction;
import org.apache.http.client.utils.URIBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TicketUtils {

    private static String username = "";
    private static String password = "";
    private static final int TICKET_RENEWAL_INTERVAL = 1800; //seconds
    private static final HashMap<Project, TicketMapping> ticketMappings = new HashMap<>();

    /**
     * Accessor for stored username.
     *
     * @return username
     */
    public static String getUsername() {
        return username;
    }

    /**
     * Convenience method for checking if ticket is non-empty. Used as a shorthand to verify that a user is logged in to MMS
     *
     * @return ticket exists and is non-empty.
     */
    public static boolean isTicketSet(Project project) {
        TicketMapping ticketMap = ticketMappings.get(project);
        return ticketMap != null && ticketMap.getTicket() != null && !ticketMap.getTicket().isEmpty();
    }

    /**
     * Accessor for ticket field.
     *
     * @return ticket string
     */
    public static String getTicket(Project project) {
        if (isTicketSet(project)) {
            return ticketMappings.get(project).getTicket();
        }
        return null;
    }

    /**
     * Logs in to MMS, using pre-specified credentials or prompting the user for new credentials.
     * <p>
     * If username and password have been pre-specified, will not display the dialog even if popups are
     * enabled. Else will display the login dialog and use the returned value.
     *
     * @return TRUE if successfully logged in to MMS, FALSE otherwise.
     * Will always return FALSE if popups are disabled and username/password are not pre-specified
     */
    public static boolean acquireMmsTicket(Project project) {
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
//        isDisplayed = true;
        int response = JOptionPane.showConfirmDialog(Application.getInstance().getMainFrame(), userPanel,
                "MMS Credentials", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
//        isDisplayed = false;
        if (response == JOptionPane.OK_OPTION) {
            username = usernameFld.getText();
            String pass = new String(passwordFld.getPassword());
            return pass;
        }
        else if (response == JOptionPane.CANCEL_OPTION || response == JOptionPane.CLOSED_OPTION) {
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
     */
    public static void setUsernameAndPassword(String user, String pass) {
        if (user == null) {
            user = "";
        }
        if (pass == null) {
            pass = "";
        }
        username = user;
        password = pass;
    }

    /**
     * Clears username, password, and ticket
     */
    public static void clearTicket(Project project) {
        password = "";
        TicketMapping removed = ticketMappings.remove(project);
        // kill auto-renewal in removed
        if (removed != null) {
            removed.getTicketRenewer().shutdown();
        }
    }

    /**
     * Uses the stored username and passed password to query MMS for a ticket. Will clear any stored password on attempt.
     * <p>
     * Will first check to see if there is an existing ticket, and if so if it is valid. If valid, will not resend
     * for new ticket. If invalid or not present, will send for new ticket.
     * <p>
     * Since it can only be called by logInToMMS(), assumes that the username and password were recently
     * acquired from the login dialogue or pre-specified if that's disabled.
     */
    private static boolean acquireTicket(Project project, String pass) {
        if (username == null || username.isEmpty()) {
            Application.getInstance().getGUILog().log("[ERROR] Unable to log in to MMS without a username");
            return false;
        }
        if (pass == null) {
            return false;
        }

        //ensure ticket is cleared in case of failure
        ticketMappings.remove(project);

        // build request
        URIBuilder requestUri = MMSUtils.getServiceUri(project);
        if (requestUri == null) {
            return false;
        }
        requestUri.setPath(requestUri.getPath() + "/api/login");
        requestUri.clearParameters();
        ObjectNode credentials = JacksonUtils.getObjectMapper().createObjectNode();
        credentials.put("username", username);
        credentials.put("password", pass);

        // do request
        String ticket;
        try {
            ticket = MMSUtils.sendCredentials(project, username, pass);
        } catch (ServerException | IOException | URISyntaxException e) {
            Application.getInstance().getGUILog().log("[ERROR] Unexpected error while acquiring credentials. Reason: " + e.getMessage());
            e.printStackTrace();
            return false;
        }

        // parse response
        if (ticket != null) {
            password = "";
            ticketMappings.put(project, new TicketMapping(project, ticket));
            return true;
        }
        Application.getInstance().getGUILog().log("[ERROR] Unable to log in to MMS with the supplied credentials.");
        password = "";
        return false;
    }

    private static class TicketMapping {
        private String ticket;
        private ScheduledExecutorService ticketRenewer;

        TicketMapping(final Project project, String ticket) {
            this.ticket = ticket;
            this.ticketRenewer = Executors.newScheduledThreadPool(1);
            // intentionally catching exceptions here, to avoid scheduled thread suspension
            final Runnable renewTicket = () -> {
                // try/catching here to prevent service being disabled for future calls
                try {
                    try {
                        boolean isValid = MMSUtils.validateCredentials(project, ticketMappings.get(project).getTicket()).equals(username);
                        if (!isValid) {
                            Application.getInstance().getGUILog().log("[INFO] MMS credentials are expired or invalid.");
                            MMSLogoutAction.logoutAction(project);
                        }
                    } catch (ServerException | IOException | URISyntaxException e) {
                        Application.getInstance().getGUILog().log("[ERROR] Unexpected error checking ticket validity (ticket will be retained). Reason: " + e.getMessage());
                        e.printStackTrace();
                    }
                } catch (Exception ignored) {
                }
            };
            this.ticketRenewer.scheduleAtFixedRate(renewTicket, TICKET_RENEWAL_INTERVAL, TICKET_RENEWAL_INTERVAL, TimeUnit.SECONDS);
        }

        public String getTicket() {
            return this.ticket;
        }

        public ScheduledExecutorService getTicketRenewer() {
            return this.ticketRenewer;
        }

    }
}
