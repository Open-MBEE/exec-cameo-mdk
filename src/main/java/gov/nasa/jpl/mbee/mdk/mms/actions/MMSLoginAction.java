package gov.nasa.jpl.mbee.mdk.mms.actions;

import com.nomagic.magicdraw.actions.ActionsStateUpdater;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.mms.sync.mms.MMSDeltaProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.mdk.options.MDKOptionsGroup;
import gov.nasa.jpl.mbee.mdk.util.TWCUtils;
import gov.nasa.jpl.mbee.mdk.util.TaskRunner;
import gov.nasa.jpl.mbee.mdk.util.TicketUtils;
import gov.nasa.jpl.mbee.mdk.util.Utils;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;

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
            Utils.showPopupMessage("Please open the project you would like to log in to MMS for first.");
            return false;
        }
        /*
        if (project.isRemote() && EsiUtils.getTeamworkService().getConnectedUser() == null) {
            Utils.showPopupMessage("Please login in to Teamwork Cloud before logging in to MMS.");
            return false;
        }
        */
        if (MDKOptionsGroup.getMDKOptions().isTWCAuthEnabled() && project.isRemote()
                && TWCUtils.getConnectedUser() == null) {
            Utils.showPopupMessage("Please login in to Teamwork Cloud before logging in to MMS.");
            return false;
        }
        if (!TicketUtils.acquireMmsTicket(project)) {
            return false;
        }
        ActionsStateUpdater.updateActionsState();
        TaskRunner.runWithProgressStatus(progressStatus -> {
            try {
                MMSDeltaProjectEventListenerAdapter.getProjectMapping(project).update();
            } catch (URISyntaxException | IOException | ServerException e) {
                e.printStackTrace();
            }
        }, "MMS Fetch", false, TaskRunner.ThreadExecutionStrategy.POOLED, false);
        Application.getInstance().getGUILog().log("[INFO] MMS login complete.");
        return true;
    }

}
