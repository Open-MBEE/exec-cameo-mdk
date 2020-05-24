package gov.nasa.jpl.mbee.mdk.util;

import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.magicdraw.teamwork2.esi.EsiServerActionsExecuter;

import java.net.ConnectException;

public class TWCUtils {
    public static String getSecondaryAuthToken() {
        String secondaryAuthToken = null;
        try {
            secondaryAuthToken = EsiServerActionsExecuter.getSecondaryAuthToken("MAGICDRAW");
        } catch (ConnectException e) {
            e.printStackTrace();
        }
        return secondaryAuthToken != null ? "Token :" + secondaryAuthToken : null;
    }

    public static String getTeamworkCloudServer() {
        if (EsiUtils.getTeamworkService() == null || EsiUtils.getTeamworkService().getLastUsedLoginInfo() == null) {
            Utils.showPopupMessage("Please login in to Teamwork Cloud.");
            return null;
        }
        return EsiUtils.getTeamworkService().getLastUsedLoginInfo().server;
    }

    public static String getConnectedUser() {
        if(EsiUtils.getTeamworkService() == null || EsiUtils.getTeamworkService().getConnectedUser() == null) {
            Utils.showPopupMessage("Please login in to Teamwork Cloud.");
            return null;
        }
        return EsiUtils.getTeamworkService().getConnectedUser();
    }
}