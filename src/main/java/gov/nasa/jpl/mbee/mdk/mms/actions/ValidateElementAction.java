package gov.nasa.jpl.mbee.mdk.mms.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import gov.nasa.jpl.mbee.mdk.mms.sync.manual.ManualSyncRunner;

import java.awt.event.ActionEvent;
import java.util.Collection;

public class ValidateElementAction extends MDAction {

    private static final long serialVersionUID = 1L;
    private Collection<Element> start;
    private Project project;
    public static final String DEFAULT_ID = "ValidateElement";

    public ValidateElementAction(Collection<Element> e, String name) {
        super(DEFAULT_ID, name, null, null);
        this.start = e;
        this.project = Project.getProject(e.iterator().next());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ManualSyncRunner manualSyncRunner = new ManualSyncRunner(start, Application.getInstance().getProject(), 0);
        ProgressStatusRunner.runWithProgressStatus(manualSyncRunner, "Manual Sync", true, 0);
        if (manualSyncRunner.getValidationSuite() == null) {
            // if it's null, there was an error in processing that was already displayed
            return;
        }
        else if (manualSyncRunner.getValidationSuite().hasErrors()) {
            // not null, has errors, display for processing
            Utils.displayValidationWindow(project, manualSyncRunner.getValidationSuite(), manualSyncRunner.getValidationSuite().getName());
        }
        else {
            // not null, no errors, all fine
            Application.getInstance().getGUILog().log("[INFO] All validated elements are equivalent.");
        }
    }
}
