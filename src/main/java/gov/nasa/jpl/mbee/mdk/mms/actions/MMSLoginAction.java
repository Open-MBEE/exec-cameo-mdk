package gov.nasa.jpl.mbee.mdk.mms.actions;

import com.nomagic.magicdraw.actions.ActionsStateUpdater;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import gov.nasa.jpl.mbee.mdk.MMSSyncPlugin;
import gov.nasa.jpl.mbee.mdk.util.TicketUtils;
import gov.nasa.jpl.mbee.mdk.util.Utils;

import java.awt.event.ActionEvent;

public class MMSLoginAction extends MDAction {
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_ID = "Login";

    public MMSLoginAction() {
        super(DEFAULT_ID, "Login", null, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        loginAction(Application.getInstance().getProject());
    }

    public static boolean loginAction(Project project) {
        if (project == null) {
            Utils.showPopupMessage("You need to have a project open first!");
            return false;
        }
        if (project.isRemote() && (TeamworkUtils.getLoggedUserName() == null && EsiUtils.getTeamworkService().getConnectedUser() == null)) {
            Utils.showPopupMessage("You need to be logged in to Teamwork Cloud first!");
            return false;
        }
        if (!TicketUtils.acquireMmsTicket(project)) {
            return false;
        }
        ActionsStateUpdater.updateActionsState();
        Application.getInstance().getGUILog().log("[INFO] MMS login complete.");
        MMSSyncPlugin.getInstance().getJmsSyncProjectEventListenerAdapter().closeJMS(project);
        MMSSyncPlugin.getInstance().getJmsSyncProjectEventListenerAdapter().initializeJMS(project);
        return true;
    }

}
