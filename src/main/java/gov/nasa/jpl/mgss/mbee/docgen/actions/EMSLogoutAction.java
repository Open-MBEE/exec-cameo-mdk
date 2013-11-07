package gov.nasa.jpl.mgss.mbee.docgen.actions;

import gov.nasa.jpl.mgss.mbee.docgen.viewedit.ViewEditUtils;

import java.awt.event.ActionEvent;


import org.apache.commons.httpclient.HttpClient;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class EMSLogoutAction extends MDAction {
    public static final String actionid = "Logout";
    
    public EMSLogoutAction() {
        super(actionid, "Logout", null, null);
    }
    
    public void actionPerformed(ActionEvent e) {
        ViewEditUtils.clearCredentials();
        Application.getInstance().getGUILog().log("Logged out");
    }

}
