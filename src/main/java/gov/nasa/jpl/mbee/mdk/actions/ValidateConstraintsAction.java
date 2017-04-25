package gov.nasa.jpl.mbee.mdk.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.constraint.BasicConstraint.Type;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ConstraintValidationRule;
import gov.nasa.jpl.mbee.mdk.ocl.OclEvaluator;
import gov.nasa.jpl.mbee.mdk.util.MDUtils;
import gov.nasa.jpl.mbee.mdk.util.Utils;
import gov.nasa.jpl.mbee.mdk.validation.ValidationSuite;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;

public class ValidateConstraintsAction extends MDAction {
    public static final String DEFAULT_ID = "ValidateConstraints";

    public static String actionText = "Validate constraints";

    private ConstraintValidationRule constraintRule = new ConstraintValidationRule();

    private ValidationSuite validationUi = new ValidationSuite("Constraint Validation");
    private Collection<ValidationSuite> validationOutput = new ArrayList<>();

    public ValidateConstraintsAction() {
        super(DEFAULT_ID, actionText, null, null);
        validationUi.addValidationRule(constraintRule);
        // Need Collection to use the utils.DisplayValidationWindow method
        validationOutput.add(validationUi);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Collection<Element> selectedElements = MDUtils.getSelection(e);
        if (selectedElements.isEmpty()) {
            return;
        }
        Project project = Project.getProject(selectedElements.iterator().next());

        // Ensure user-defined shortcut functions are updated
        OclEvaluator.resetEnvironment();

        constraintRule.constraintType = Type.STATIC;
        constraintRule.init(project, null);
        constraintRule.run(project, null, selectedElements);
        Utils.displayValidationWindow(project, validationOutput, "User Validation Script Results");
    }
}
