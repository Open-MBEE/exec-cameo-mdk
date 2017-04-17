package gov.nasa.jpl.mbee.mdk;

import com.nomagic.actions.AMConfigurator;
import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.actions.MDActionsCategory;
import gov.nasa.jpl.mbee.mdk.mms.actions.MMSLoginAction;
import gov.nasa.jpl.mbee.mdk.mms.actions.MMSLogoutAction;
import gov.nasa.jpl.mbee.mdk.mms.actions.GenerateAllViewsAction;
import gov.nasa.jpl.mbee.mdk.mms.actions.ValidateBranchesAction;
import gov.nasa.jpl.mbee.mdk.options.MDKOptionsGroup;

public class MMSConfigurator implements AMConfigurator {

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void configure(ActionsManager manager) {
        NMAction category = manager.getActionFor("MMSMAIN");
        if (category == null) {
            category = new MDActionsCategory("MMSMAIN", "MMS");
        }
        ((ActionsCategory) category).setNested(true);
        manager.addCategory((ActionsCategory) category);

        MMSLoginAction mmsLoginAction = new MMSLoginAction();
        category.addAction(mmsLoginAction);

        MMSLogoutAction mmsLogoutAction = new MMSLogoutAction();
        category.addAction(mmsLogoutAction);

        GenerateAllViewsAction generateAllViewsAction = new GenerateAllViewsAction();
        category.addAction(generateAllViewsAction);

        if (MDKOptionsGroup.getMDKOptions() != null && MDKOptionsGroup.getMDKOptions().isMDKAdvancedOptions()) {
            MDActionsCategory validateCategory = new MDActionsCategory("MMSMAINVALIDATE", "Validate");
            validateCategory.setNested(true);
            category.addAction(validateCategory);
            ValidateBranchesAction validateBranchesAction = new ValidateBranchesAction();
            validateCategory.addAction(validateBranchesAction);
        }
    }

}
