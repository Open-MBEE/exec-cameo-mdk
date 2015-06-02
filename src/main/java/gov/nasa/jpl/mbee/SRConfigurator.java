package gov.nasa.jpl.mbee;

import gov.nasa.jpl.mbee.actions.ems.CloseAutoSyncAction;
import gov.nasa.jpl.mbee.actions.ems.EMSLoginAction;
import gov.nasa.jpl.mbee.actions.ems.EMSLogoutAction;
import gov.nasa.jpl.mbee.actions.ems.SendProjectVersionAction;
import gov.nasa.jpl.mbee.actions.ems.StartAutoSyncAction;
import gov.nasa.jpl.mbee.actions.ems.UpdateFromJMS;
import gov.nasa.jpl.mbee.actions.ems.UpdateWorkspacesAction;
import gov.nasa.jpl.mbee.actions.ems.ValidateMountStructureAction;
import gov.nasa.jpl.mbee.lib.MDUtils;

import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.actions.MDActionsCategory;
import com.nomagic.magicdraw.actions.BrowserContextAMConfigurator;
import com.nomagic.magicdraw.ui.browser.Tree;

public class SRConfigurator implements BrowserContextAMConfigurator {

    @Override
    public int getPriority() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void configure(ActionsManager manager, Tree tree) {
        NMAction category = (ActionsCategory)manager.getActionFor("SRMain");
        System.out.println("Configuring Systems Reasoner");
        if (category == null) {
            category = new MDActionsCategory("SRMain", "Reason Systemer");
            ((ActionsCategory)category).setNested(true);
            manager.addCategory((ActionsCategory)category);
//            EMSLogoutAction logout = new EMSLogoutAction();
//            EMSLoginAction login = new EMSLoginAction();
//            login.setLogoutAction(logout);
//            logout.setLoginAction(login);
//            category.addAction(logout);
//            category.addAction(login);
//            category.addAction(new ValidateMountStructureAction());
//            category.addAction(new StartAutoSyncAction());
//            category.addAction(new CloseAutoSyncAction());
//            category.addAction(new UpdateFromJMS(false));
//            category.addAction(new UpdateFromJMS(true));
//            category.addAction(new SendProjectVersionAction());
//            if (MDUtils.isDeveloperMode()) {
//                category.addAction(new UpdateWorkspacesAction());
//            }
        }
    }
}
