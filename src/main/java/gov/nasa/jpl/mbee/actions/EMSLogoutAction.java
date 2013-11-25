package gov.nasa.jpl.mbee.actions;

import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;

import java.awt.event.ActionEvent;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;

public class EMSLogoutAction extends MDAction {
    public static final String actionid = "Logout";

    public EMSLogoutAction() {
        super(actionid, "Logout", null, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ViewEditUtils.clearCredentials();
        Application.getInstance().getGUILog().log("Logged out");
    }

}
