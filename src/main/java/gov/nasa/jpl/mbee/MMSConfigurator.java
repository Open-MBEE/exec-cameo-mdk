package gov.nasa.jpl.mbee;

import gov.nasa.jpl.mbee.actions.ems.EMSLogoutAction;

import com.nomagic.actions.AMConfigurator;
import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.actions.MDActionsCategory;

public class MMSConfigurator implements AMConfigurator {

    @Override
    public int getPriority() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void configure(ActionsManager manager) {
        NMAction category = (ActionsCategory)manager.getActionFor("MMSMAIN");
        if (category == null) {
            category = new MDActionsCategory("MMSMAIN", "MMS");
            ((ActionsCategory)category).setNested(true);
            manager.addCategory((ActionsCategory)category);
            category.addAction(new EMSLogoutAction());
        }
        
    }
    

}
