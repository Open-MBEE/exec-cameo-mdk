package org.openmbee.mdk.util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.task.ProgressStatus;

import org.openmbee.mdk.http.ServerException;
import org.openmbee.mdk.mms.MMSUtils;
import org.openmbee.mdk.mms.actions.MMSLogoutAction;
import org.openmbee.mdk.tickets.AcquireTicketChain;

public class TicketUtils {
    private static final int TICKET_RENEWAL_INTERVAL = 15 * 60; // seconds
    private static final Map<Project, TicketMapping> ticketMappings = Collections.synchronizedMap(new WeakHashMap<>());

    public static void putTicketMapping(Project project, String username, String ticket) {
        ticketMappings.put(project, new TicketMapping(project, username, ticket));
    }

    public static void removeTicketMapping(Project project) {
        ticketMappings.remove(project);
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
     * Convenience method for checking if ticket is non-empty. Used as a shorthand
     * to verify that a user is logged in to MMS
     *
     * @return ticket exists and is non-empty.
     */
    public static boolean isTicketSet(Project project) {
        String ticket = getTicket(project);
        return ticket != null && !ticket.isEmpty();
    }

    public static boolean isTicketValid(Project project, ProgressStatus progressStatus) throws ServerException, IOException, URISyntaxException, GeneralSecurityException {
        if (!isTicketSet(project)) {
            return false;
        }
        if (MMSUtils.validateJwtToken(project, progressStatus)) {
            return true;
        }
        String username = getUsername(project);
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
     * Clears username, password, and ticket
     */
    public static void clearTicket(Project project) {
        AcquireTicketChain chain = new AcquireTicketChain();
        chain.reset();
        TicketMapping removed = ticketMappings.remove(project);
        if (removed != null && removed.getScheduledFuture() != null) {
            removed.getScheduledFuture().cancel(true);
        }
    }

    private static class TicketMapping {
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
                    Application.getInstance().getGUILog().log(
                            "[ERROR] An error occurred while checking ticket validity. Ticket will be retained for re-validation. Reason: "
                                    + e.getMessage());
                    e.printStackTrace();
                } catch (Exception ignored) {
                }
            }, "Checking MMS ticket", false, TaskRunner.ThreadExecutionStrategy.NONE, false,
                    (runnable, service) -> service.scheduleAtFixedRate(runnable, TICKET_RENEWAL_INTERVAL,
                            TICKET_RENEWAL_INTERVAL, TimeUnit.SECONDS));
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
