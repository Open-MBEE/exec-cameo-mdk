package gov.nasa.jpl.mbee.mdk.mms.actions;

import com.nomagic.magicdraw.actions.ActionsStateUpdater;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import gov.nasa.jpl.mbee.mdk.MMSSyncPlugin;
import gov.nasa.jpl.mbee.mdk.util.TicketUtils;
import gov.nasa.jpl.mbee.mdk.mms.sync.jms.JMSSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.mdk.mms.sync.status.SyncStatusConfigurator;

import java.awt.event.ActionEvent;

public class MMSLogoutAction extends MMSAction {
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_ID = "Logout";

    public MMSLogoutAction() {
        super(DEFAULT_ID, "Logout", null, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        logoutAction(Application.getInstance().getProject());
    }

    public static void logoutAction(Project project) {
        TicketUtils.clearTicket(project);
        Application.getInstance().getGUILog().log("[INFO] MMS logout complete.");
        ActionsStateUpdater.updateActionsState();
        if (!JMSSyncProjectEventListenerAdapter.getProjectMapping(project).getJmsMessageListener().isDisabled()) {
            MMSSyncPlugin.getInstance().getJmsSyncProjectEventListenerAdapter().closeJMS(project);
            Application.getInstance().getGUILog().log("[WARNING] " + project.getName() + " - Reverting to offline mode. All changes will be saved in the model until reconnected. Reason: You must be logged into MMS.");
        }
        SyncStatusConfigurator.getSyncStatusAction().update();
    }

}
