package org.openmbee.mdk.docgen.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import org.openmbee.mdk.docgen.ViewViewpointValidator;
import org.openmbee.mdk.util.Utils;

import java.awt.event.ActionEvent;
import java.util.Collections;

public class ValidateViewAction extends MDAction {
    private final Class view;
    private final boolean recurse;

    public static final String DEFAULT_ID = ValidateViewAction.class.getSimpleName();
    public static final String RECURSIVE_DEFAULT_ID = DEFAULT_ID + "R";

    public ValidateViewAction(Class view) {
        this(view, false);
    }

    public ValidateViewAction(Class view, boolean recurse) {
        super(recurse ? RECURSIVE_DEFAULT_ID : DEFAULT_ID, "Validate View" + (recurse ? "s Recursively" : ""), null, null);
        this.view = view;
        this.recurse = recurse;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Project project = Application.getInstance().getProject();
        try {
            ViewViewpointValidator viewViewpointValidator = new ViewViewpointValidator(Collections.singleton(view), project, recurse);
            viewViewpointValidator.run();
            if (!viewViewpointValidator.getValidationSuite().hasErrors()) {
                Application.getInstance().getGUILog().log("[INFO] View validation yielded no errors.");
                return;
            }
            Utils.displayValidationWindow(project, viewViewpointValidator.getValidationSuite(), viewViewpointValidator.getValidationSuite().getName());
        } catch (Exception e) {
            e.printStackTrace();
            Application.getInstance().getGUILog().log("[ERROR] An unexpected error occurred while validating views. View validation aborted. Reason: " + e.getMessage());
        }
    }
}
