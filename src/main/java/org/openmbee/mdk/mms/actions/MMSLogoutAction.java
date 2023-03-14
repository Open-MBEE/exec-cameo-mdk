package org.openmbee.mdk.mms.actions;

import com.nomagic.magicdraw.actions.ActionsStateUpdater;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import org.openmbee.mdk.mms.sync.status.SyncStatusConfigurator;
import org.openmbee.mdk.util.TicketUtils;

import java.awt.event.ActionEvent;

public class MMSLogoutAction extends MDAction {
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
        SyncStatusConfigurator.getSyncStatusAction().update();
    }

    @Override
    public void updateState() {
        setEnabled(TicketUtils.isTicketSet(Application.getInstance().getProject()));
    }

}
