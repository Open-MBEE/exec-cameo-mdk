package gov.nasa.jpl.mbee.mdk.ems.actions;

import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.ui.ProgressStatusRunner;
import gov.nasa.jpl.mbee.mdk.ems.validation.ModuleValidator;

import java.awt.event.ActionEvent;

public class ValidateModulesAction extends MMSAction {
    private static final long serialVersionUID = 1L;
    public static final String actionid = "ValidateModules";

    public ValidateModulesAction() {
        super(actionid, "Modules", null, null);
    }

    public class ValidationRunner implements RunnableWithProgress {

        @Override
        public void run(ProgressStatus arg0) {
            ModuleValidator v = new ModuleValidator();
            v.validate(arg0);
            v.showWindow();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ProgressStatusRunner.runWithProgressStatus(new ValidationRunner(), "Validating Modules", true, 0);
    }

}
