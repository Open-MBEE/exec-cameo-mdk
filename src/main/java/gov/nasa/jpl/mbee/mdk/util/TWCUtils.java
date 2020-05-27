package gov.nasa.jpl.mbee.mdk.util;

import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.magicdraw.teamwork2.esi.EsiServerActionsExecuter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;

public class TWCUtils {
    private static final Logger logger = LoggerFactory.getLogger(TWCUtils.class);

    public static String getConnectedUser() {
        return (EsiUtils.getTeamworkService() == null || EsiUtils.getTeamworkService().getConnectedUser() == null)
                ? null
                : EsiUtils.getTeamworkService().getConnectedUser();
    }

    public static String getTeamworkCloudServer() {
        return (EsiUtils.getTeamworkService() == null || EsiUtils.getTeamworkService().getLastUsedLoginInfo() == null)
                ? null
                : EsiUtils.getTeamworkService().getLastUsedLoginInfo().server;
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
}