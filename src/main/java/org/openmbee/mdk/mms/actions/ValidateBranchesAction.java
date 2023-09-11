package org.openmbee.mdk.mms.actions;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.ui.ProgressStatusRunner;
import org.openmbee.mdk.mms.validation.BranchValidator;
import org.openmbee.mdk.util.TicketUtils;

import java.awt.event.ActionEvent;

public class ValidateBranchesAction extends MMSAction {
    public static final String DEFAULT_ID = ValidateBranchesAction.class.getSimpleName();

    public ValidateBranchesAction() {
        super(DEFAULT_ID, "Branches", null, null);
    }

    public class ValidationRunner implements RunnableWithProgress {

        @Override
        public void run(ProgressStatus arg0) {
            BranchValidator branchValidator = new BranchValidator(Application.getInstance().getProject());
            branchValidator.validate(arg0, true);
            if (branchValidator.hasErrors()) {
                Application.getInstance().getGUILog().log("[ERROR] Unable to complete validate branches action.");
                return;
            }
            branchValidator.showWindow();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ProgressStatusRunner.runWithProgressStatus(new ValidationRunner(), "Validating Branches", true, 0);
    }

    @Override
    public void updateState() {
        setEnabled(TicketUtils.isTicketSet(Application.getInstance().getProject()) && !MMSAction.isDisabled());
    }

}
