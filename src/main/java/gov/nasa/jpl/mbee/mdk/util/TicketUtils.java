package gov.nasa.jpl.mbee.mdk.util;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.task.ProgressStatus;
import com.nomagic.ui.ProgressStatusRunner;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.mms.actions.MMSLogoutAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TicketUtils extends AbstractAcquireTicketProcessor{
    public TicketUtils(AbstractAcquireTicketProcessor processor) {
        super(processor);
    }
    private static String username = "";
    private static String password = "";
    private static final int TICKET_RENEWAL_INTERVAL = 15 * 60; //seconds
    private static final Map<Project, TicketMapping> ticketMappings = Collections.synchronizedMap(new WeakHashMap<>());


    public static void putTicketMapping(Project project, String username, String ticket) {
        ticketMappings.put(project, new TicketMapping(project, username, ticket));
    }

    /**
     * Accessor for stored username.
     *
     * @return username
     */
    public static String getUsername(Project project) {
        TicketMapping ticketMapping = ticketMappings.get(project);
        return ticketMapping != null ? ticketMapping.getUsername() : null;
    }

    /**
     * Convenience method for checking if ticket is non-empty. Used as a shorthand to verify that a user is logged in to MMS
     *
     * @return ticket exists and is non-empty.
     */
    public static boolean isTicketSet(Project project) {
        String ticket = getTicket(project);
        return ticket != null && !ticket.isEmpty();
    }
    
    public static boolean isTicketValid(Project project, ProgressStatus progressStatus) throws ServerException, IOException, URISyntaxException {
        if (!isTicketSet(project)) {
            return false;
        }
        if(TWCUtils.getConnectedUser() != null && MMSUtils.validateJwtToken(project, progressStatus).equalsIgnoreCase(TWCUtils.getConnectedUser())) {
            return true;
        }
        return MMSUtils.validateCredentialsTicket(project, getTicket(project), progressStatus).equals(username);
    }

    /**
     * Accessor for ticket field.
     *
     * @return ticket string
     */
    public static String getTicket(Project project) {
        TicketMapping ticketMapping = ticketMappings.get(project);
        if (ticketMapping == null) {
            return null;
        }
        return ticketMapping.getTicket();
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
    @Override
    public boolean acquireMmsTicket(Project project) {
        if (!username.isEmpty() && !password.isEmpty()) {
            if(acquireTicket(project, password)) {
                return true;
            }
            return super.acquireMmsTicket(project);
        }
        else if (!Utils.isPopupsDisabled()) {
            String password = getUserCredentialsDialog();
            if (password == null) {
                return super.acquireMmsTicket(project);
            }
            if(acquireTicket(project, password)) {
                return true;
            }
            return super.acquireMmsTicket(project);
        }
        else {
            Application.getInstance().getGUILog().log("[ERROR] No credentials have been specified and dialog popups are disabled. Skipping login.");
            return super.acquireMmsTicket(project);
        }
    }

    /**
     * Shows a login dialog window and uses its filled in values to set the username and password.
     * Stores the entered username for future use / convenience, passes the entered password to acquireTicket().
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
        if (removed != null && removed.getScheduledFuture() != null) {
            removed.getScheduledFuture().cancel(true);
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
            Application.getInstance().getGUILog().log("[ERROR] Unable to log in to MMS without a username.");
            return false;
        }
        if (pass == null) {
            return false;
        }
        if (isTicketSet(project)) {
            Application.getInstance().getGUILog().log("[INFO] Clearing previous credentials.");
            MMSLogoutAction.logoutAction(project);
        }

        //ensure ticket is cleared in case of failure
        ticketMappings.remove(project);

        // do request
        ProgressStatusRunner.runWithProgressStatus(progressStatus -> {
            String ticket;
            try {
                ticket = MMSUtils.getCredentialsTicket(project, username, pass, progressStatus);
            } catch (IOException | URISyntaxException | ServerException e) {
                Application.getInstance().getGUILog().log("[ERROR] An error occurred while acquiring credentials. Reason: " + e.getMessage());
                e.printStackTrace();
                return;
            }
            // parse response
            if (ticket != null) {
                ticketMappings.put(project, new TicketMapping(project, username, ticket));
            }
        }, "Logging in to MMS", true, 0);

        // parse response
        password = "";
        if (isTicketSet(project)) {
            return true;
        }
        Application.getInstance().getGUILog().log("[ERROR] Unable to log in to MMS with the supplied credentials.");
        return false;
    }

    public static class TicketMapping {
        private final String ticket;
        private final String username;
        private final ScheduledFuture<?> scheduledFuture;

        TicketMapping(Project project, String username, String ticket) {
            this.ticket = ticket;
            this.username = username;
            this.scheduledFuture = TaskRunner.scheduleWithProgressStatus(progressStatus -> {
                try {
                    boolean isValid = isTicketValid(project, progressStatus);
                    if (!isValid) {
                        Application.getInstance().getGUILog().log("[INFO] MMS credentials are expired or invalid.");
                        MMSLogoutAction.logoutAction(project);
                    }
                } catch (IOException | URISyntaxException | ServerException e) {
                    Application.getInstance().getGUILog().log("[ERROR] An error occurred while checking ticket validity. Ticket will be retained for re-validation. Reason: " + e.getMessage());
                    e.printStackTrace();
                } catch (Exception ignored) {
                }
            }, "Checking MMS ticket", false, TaskRunner.ThreadExecutionStrategy.NONE, false, (runnable, service) -> service.scheduleAtFixedRate(runnable, TICKET_RENEWAL_INTERVAL, TICKET_RENEWAL_INTERVAL, TimeUnit.SECONDS));
        }

        public String getTicket() {
            return ticket;
        }

        public String getUsername() {
            return username;
        }

        public ScheduledFuture<?> getScheduledFuture() {
            return scheduledFuture;
        }
    }
}
