package gov.nasa.jpl.mbee.actions.ems;

import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.ui.ProgressStatusRunner;
import gov.nasa.jpl.mbee.ems.validation.BranchValidator;

import java.awt.event.ActionEvent;

public class ValidateBranchesAction extends MMSAction {
    private static final long serialVersionUID = 1L;
    public static final String actionid = "ValidateBranches";

    public ValidateBranchesAction() {
        super(actionid, "Branches", null, null);
    }

    public class ValidationRunner implements RunnableWithProgress {

        @Override
        public void run(ProgressStatus arg0) {
            BranchValidator v = new BranchValidator();
            v.validate(arg0);
            v.showWindow();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ProgressStatusRunner.runWithProgressStatus(new ValidationRunner(), "Validating Branches", true, 0);
    }

}
