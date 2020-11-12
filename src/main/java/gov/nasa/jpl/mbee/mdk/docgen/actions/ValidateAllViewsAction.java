package gov.nasa.jpl.mbee.mdk.docgen.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.docgen.ViewViewpointValidator;
import gov.nasa.jpl.mbee.mdk.mms.actions.ValidateBranchesAction;
import gov.nasa.jpl.mbee.mdk.util.Utils;

import javax.annotation.CheckForNull;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidateAllViewsAction extends MDAction {
    public static final String DEFAULT_ID = ValidateBranchesAction.class.getSimpleName();

    public ValidateAllViewsAction() {
        super(DEFAULT_ID, "Views", null, null);
    }

    @Override
    public void actionPerformed(@CheckForNull ActionEvent actionEvent) {
        Project project = Application.getInstance().getProject();
        if (project == null) {
            Application.getInstance().getGUILog().log("[ERROR] No project open. Skipping view validation.");
        }
        Set<Element> views = getViews(project);
        if (views.isEmpty()) {
            Application.getInstance().getGUILog().log("[INFO] No views found. Skipping view validation.");
            return;
        }
        try {
            ViewViewpointValidator viewViewpointValidator = new ViewViewpointValidator(views, project, false);
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

    private Set<Element> getViews(Project project) {
        Stereotype viewStereotype = Utils.getViewStereotype(project);
        if (viewStereotype == null) {
            return Collections.emptySet();
        }
        return StereotypesHelper.getExtendedElementsIncludingDerived(viewStereotype).stream().filter(view -> !ProjectUtilities.isElementInAttachedProject(view)).collect(Collectors.toSet());
    }

    @Override
    public void updateState() {
        this.setEnabled(Application.getInstance().getProject() != null);
    }
}
