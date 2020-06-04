package gov.nasa.jpl.mbee.mdk.util;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.magicdraw.teamwork2.esi.EsiServerActionsExecuter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.util.TicketUtils.TicketMapping;

import java.net.ConnectException;
import java.io.IOException;
import java.net.URISyntaxException;
import gov.nasa.jpl.mbee.mdk.http.ServerException;

public class TWCUtils extends AbstractAcquireTicketProcessor {
    public TWCUtils(AbstractAcquireTicketProcessor processor) {
        super(processor);
    }

    private static final Logger logger = LoggerFactory.getLogger(TWCUtils.class);

    public static String getConnectedUser() {
        return (EsiUtils.getTeamworkService() == null || EsiUtils.getTeamworkService().getConnectedUser() == null)
                ? null
                : EsiUtils.getTeamworkService().getConnectedUser();
    }

    public static String getTeamworkCloudServer() {
        String twcServer = null;
        if (EsiUtils.getTeamworkService() != null || EsiUtils.getTeamworkService().getLastUsedLoginInfo() != null
                || EsiUtils.getTeamworkService().getLastUsedLoginInfo().server != null) {
            twcServer = EsiUtils.getTeamworkService().getLastUsedLoginInfo().server;
            if (twcServer.indexOf(':') > -1) {
                twcServer = twcServer.substring(0, twcServer.indexOf(':'));
            }
        }
        return twcServer;
    }

    public static String getSecondaryAuthToken() {
        String secondaryAuthToken = null;
        try {
            secondaryAuthToken = EsiServerActionsExecuter.getSecondaryAuthToken("MAGICDRAW");
        } catch (ConnectException e) {
            logger.error("Error while generating secondary auth token", e.getMessage());
        }
        return secondaryAuthToken != null ? "Token :" + secondaryAuthToken : null;
    }

    @Override
    public boolean acquireMmsTicket(Project project) {
        if (getConnectedUser() == null || getTeamworkCloudServer() == null || getSecondaryAuthToken() == null) {
            return super.acquireMmsTicket(project);
        }

        // do request
        ProgressStatusRunner.runWithProgressStatus(progressStatus -> {
            String ticket;
            try {
                ticket = MMSUtils.getTicketUsingTWCToken(project, getTeamworkCloudServer(), getSecondaryAuthToken(),
                        progressStatus);
            } catch (IOException | URISyntaxException | ServerException e) {
                Application.getInstance().getGUILog()
                        .log("[ERROR] An error occurred while acquiring credentials. Reason: " + e.getMessage());
                e.printStackTrace();
                return;
            }
            if (ticket != null) {
                TicketUtils.ticketMappings.put(project, new TicketMapping(project, getConnectedUser(), ticket));
            }
        }, "Logging in to MMS", true, 0);
        if (TicketUtils.isTicketSet(project)) {
            return true;
        }
        Application.getInstance().getGUILog().log("[ERROR] Unable to log in to MMS with TWC.");
        return super.acquireMmsTicket(project);
    }
}