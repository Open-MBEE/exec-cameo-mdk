package gov.nasa.jpl.mbee.mdk.ems.actions;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.ui.ProgressStatusRunner;
import gov.nasa.jpl.mbee.mdk.ems.validation.BranchValidator;

import java.awt.event.ActionEvent;

public class ValidateBranchesAction extends MMSAction {
    private static final long serialVersionUID = 1L;
    public static final String DEFAULT_ID = "ValidateBranches";

    public ValidateBranchesAction() {
        super(DEFAULT_ID, "Branches", null, null);
    }

    public class ValidationRunner implements RunnableWithProgress {

        @Override
        public void run(ProgressStatus arg0) {
            BranchValidator v = new BranchValidator(Application.getInstance().getProject());
            v.validate(arg0);
            v.showWindow();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // branchId = EsiUtils.getBranchID(Project.getProject(element).getPrimaryProject().getLocationURI()).toString();
        ProgressStatusRunner.runWithProgressStatus(new ValidationRunner(), "Validating Branches", true, 0);
    }

}
