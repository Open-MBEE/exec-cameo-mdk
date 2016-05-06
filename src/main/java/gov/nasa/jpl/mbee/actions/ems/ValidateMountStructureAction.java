package gov.nasa.jpl.mbee.actions.ems;

import gov.nasa.jpl.mbee.ems.validation.BranchAndModulesValidator;

import java.awt.event.ActionEvent;

import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.ui.ProgressStatusRunner;

public class ValidateMountStructureAction extends MMSAction {
    private static final long serialVersionUID = 1L;
    public static final String actionid = "ValidateStuff";

    public ValidateMountStructureAction() {
        super(actionid, "Check Modules and Branches", null, null);
    }

    public class ValidationRunner implements RunnableWithProgress {

        @Override
        public void run(ProgressStatus arg0) {
            BranchAndModulesValidator v = new BranchAndModulesValidator();
            v.validate(arg0);
            v.showWindow();
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        ProgressStatusRunner.runWithProgressStatus(new ValidationRunner(), "Validating Structures", true, 0);
    }

}
