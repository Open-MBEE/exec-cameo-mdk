package gov.nasa.jpl.mbee.actions.ems;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.ModelExporter;
import gov.nasa.jpl.mbee.ems.validation.BranchAndModulesValidator;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;

import java.awt.event.ActionEvent;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.ui.ProgressStatusRunner;

public class ValidateMountStructureAction extends MDAction {
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
