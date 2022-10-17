package gov.nasa.jpl.mbee.mdk.tickets;

import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.task.ProgressStatus;
import com.nomagic.ui.ProgressStatusRunner;

import gov.nasa.jpl.mbee.mdk.settings.ProjectSettings;
import org.apache.http.client.methods.HttpRequestBase;

import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.mms.actions.MMSLogoutAction;
import gov.nasa.jpl.mbee.mdk.mms.endpoints.MMSEndpointBuilderConstants;
import gov.nasa.jpl.mbee.mdk.mms.endpoints.MMSEndpointType;
import gov.nasa.jpl.mbee.mdk.mms.endpoints.MMSLoginEndpoint;
import gov.nasa.jpl.mbee.mdk.util.TicketUtils;
import gov.nasa.jpl.mbee.mdk.util.Utils;

public class BasicAuthAcquireTicketProcessor extends AbstractAcquireTicketProcessor {
    public BasicAuthAcquireTicketProcessor(AbstractAcquireTicketProcessor processor) {
        super(processor);
    }

    private static String username = "";
    private static String password = "";

    public static void clearPassword() {
        password = "";
    }

    /**
     * Method to set username and password for logging in to MMS. Can be called
     * directly to pre-set the credential information for automation, but should be
     * used with Utils.disablePopups(true) to prevent display of the standard log in
     * window. Use without disabling popups will cause these values to be
     * overwritten by the values obtained from the popup window call.
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

    public static String getCredentialsTicket(Project project, String username, String password,
            ProgressStatus progressStatus) throws ServerException, IOException, URISyntaxException, GeneralSecurityException {
        return getCredentialsTicket(project, null, username, password, progressStatus);
    }

    public static String getCredentialsTicket(String baseUrl, String username, String password,
            ProgressStatus progressStatus) throws ServerException, IOException, URISyntaxException, GeneralSecurityException {
        return getCredentialsTicket(null, baseUrl, username, password, progressStatus);
    }

    private static String getCredentialsTicket(Project project, String baseUrl, String username, String password,
            ProgressStatus progressStatus) throws ServerException, IOException, URISyntaxException, GeneralSecurityException {
        HttpRequestBase request = null;
        if (project != null) {
            request = MMSLoginEndpoint.builder()
                    .addParam(MMSEndpointBuilderConstants.URI_BASE_PATH, MMSUtils.getMmsUrl(project))
                    .addParam("username", username).addParam("password", password).build();
        } else if (baseUrl != null) {
            request = MMSLoginEndpoint.builder().addParam(MMSEndpointBuilderConstants.URI_BASE_PATH, baseUrl)
                    .addParam("username", username).addParam("password", password).build();
        }

        if (request != null) {
            // do request
            ObjectNode responseJson = JacksonUtils.getObjectMapper().createObjectNode();
            MMSUtils.sendMMSRequest(project, request, progressStatus, responseJson);
            if (responseJson.get(MMSEndpointType.AUTHENTICATION_RESPONSE_JSON_KEY) != null
                    && responseJson.get(MMSEndpointType.AUTHENTICATION_RESPONSE_JSON_KEY).isTextual()) {
                return responseJson.get(MMSEndpointType.AUTHENTICATION_RESPONSE_JSON_KEY).asText();
            }
        }

        return null;
    }

    /**
     * Uses the stored username and passed password to query MMS for a ticket. Will
     * clear any stored password on attempt.
     * <p>
     * Will first check to see if there is an existing ticket, and if so if it is
     * valid. If valid, will not resend for new ticket. If invalid or not present,
     * will send for new ticket.
     * <p>
     * Since it can only be called by logInToMMS(), assumes that the username and
     * password were recently acquired from the login dialogue or pre-specified if
     * that's disabled.
     */
    private static boolean acquireTicket(Project project, String pass) {
        if (username == null || username.isEmpty()) {
            Application.getInstance().getGUILog().log("[ERROR] Unable to log in to MMS without a username.");
            return false;
        }
        if (pass == null) {
            return false;
        }
        if (TicketUtils.isTicketSet(project)) {
            Application.getInstance().getGUILog().log("[INFO] Clearing previous credentials.");
            MMSLogoutAction.logoutAction(project);
        }

        // ensure ticket is cleared in case of failure
        TicketUtils.removeTicketMapping(project);

        // do request
        ProgressStatusRunner.runWithProgressStatus(progressStatus -> {
            String ticket;
            try {
                ticket = getCredentialsTicket(project, username, pass, progressStatus);
            } catch (IOException | URISyntaxException | ServerException | GeneralSecurityException e) {
                Application.getInstance().getGUILog()
                        .log("[ERROR] An error occurred while acquiring credentials. Reason: " + e.getMessage());
                e.printStackTrace();
                return;
            }
            // parse response
            if (ticket != null) {
                TicketUtils.putTicketMapping(project, username, ticket);
            }
        }, "Logging in to MMS", true, 0);

        // parse response
        password = "";
        if (TicketUtils.isTicketSet(project)) {
            return true;
        }
        Application.getInstance().getGUILog().log("[ERROR] Unable to log in to MMS with the supplied credentials.");
        return false;
    }

    /**
     * Forces focus to a particular JTextField in a displayed dialog
     *
     * @param field The field to force into focus
     */
    private static void makeSureUserGetsFocus(final JTextField field) {
        // from
        // http://stackoverflow.com/questions/14096140/how-to-set-default-input-field-in-joptionpane
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
     * Shows a login dialog window and uses its filled in values to set the username
     * and password. Stores the entered username for future use / convenience,
     * passes the entered password to acquireTicket().
     */
    private static String getUserCredentialsDialog() {
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
            return new String(passwordFld.getPassword());
        } else if (response == JOptionPane.CANCEL_OPTION || response == JOptionPane.CLOSED_OPTION) {
            Application.getInstance().getGUILog().log("[INFO] MMS login has been cancelled.");
        }
        return null;
    }

    /**
     * Logs in to MMS, using pre-specified credentials or prompting the user for new
     * credentials.
     * <p>
     * If username and password have been pre-specified, will not display the dialog
     * even if popups are enabled. Else will display the login dialog and use the
     * returned value.
     *
     * @return TRUE if successfully logged in to MMS, FALSE otherwise. Will always
     *         return FALSE if popups are disabled and username/password are not
     *         pre-specified
     */
    @Override
    public boolean acquireMmsTicket(Project project) {
        if (!username.isEmpty() && !password.isEmpty()) {
            if (acquireTicket(project, password)) {
                return true;
            }
            return super.acquireMmsTicket(project);
        } else if (!Utils.isPopupsDisabled()) {
            String pass = getUserCredentialsDialog();
            if (pass == null) {
                return super.acquireMmsTicket(project);
            }
            if (acquireTicket(project, pass)) {
                return true;
            }
            return super.acquireMmsTicket(project);
        } else {
            Application.getInstance().getGUILog()
                    .log("[ERROR] No credentials have been specified and dialog popups are disabled. Skipping login.");
            return super.acquireMmsTicket(project);
        }
    }

    @Override
    public void reset() {
        clearPassword();
        super.reset();
    }
}