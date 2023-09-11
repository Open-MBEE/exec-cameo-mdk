package org.openmbee.mdk.mms.actions;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import org.openmbee.mdk.mms.sync.manual.ManualSyncRunner;
import org.openmbee.mdk.util.Utils;

import java.awt.event.ActionEvent;
import java.util.Collection;

public class ValidateElementRecursivelyAction extends MMSAction {

    private static final long serialVersionUID = 1L;
    private Collection<Element> start;
    private Project project;
    public static final String DEFAULT_ID = "ValidateElementRecursively";

    public ValidateElementRecursivelyAction(Collection<Element> e, String name) {
        super(DEFAULT_ID, name, null, null);
        this.start = e;
        this.project = Project.getProject(e.iterator().next());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ManualSyncRunner manualSyncRunner = new ManualSyncRunner(start, Application.getInstance().getProject(), -1);
        ProgressStatusRunner.runWithProgressStatus(manualSyncRunner, "Validate Models", true, 0);
        if (manualSyncRunner.getValidationSuite() != null && manualSyncRunner.getValidationSuite().hasErrors()) {
            Utils.displayValidationWindow(project, manualSyncRunner.getValidationSuite(), manualSyncRunner.getValidationSuite().getName());
        }
    }
}
