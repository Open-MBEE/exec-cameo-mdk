package gov.nasa.jpl.mbee.mdk.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.Configurator;
import gov.nasa.jpl.mbee.mdk.constraint.BasicConstraint.Type;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ConstraintValidationRule;
import gov.nasa.jpl.mbee.mdk.util.MDUtils;
import gov.nasa.jpl.mbee.mdk.util.Utils;
import gov.nasa.jpl.mbee.mdk.ocl.OclEvaluator;
import gov.nasa.jpl.mbee.mdk.validation.ValidationSuite;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ValidateConstraints extends MDAction {
    /**
     *
     */
    private static final long serialVersionUID = 2202161655434764023L;

    protected List<Element> context = new ArrayList<Element>();        // REVIEW
    // --
    // Is
    // this
    // being
    // used?

    public static final String DEFAULT_ID = "ValidateConstraints";

    public static String actionText = "Validate constraints";

    private ConstraintValidationRule constraintRule = new ConstraintValidationRule();   // new
    // ValidationRule("Constraint",
    // "Model constraint violation",
    // ViolationSeverity.WARNING);

    private ValidationSuite validationUi = new ValidationSuite("Constraint Validation");
    private Collection<ValidationSuite> validationOutput = new ArrayList<ValidationSuite>();

    public ValidateConstraints(Element context) {
        super(DEFAULT_ID, actionText, null, null);
        if (context != null) {
            getContext().add(context);
        }
        validationUi.addValidationRule(constraintRule);
        // Need Collection to use the utils.DisplayValidationWindow method
        validationOutput.add(validationUi);
    }

    public ValidateConstraints() {
        this(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Collection<Element> selectedElements = MDUtils.getSelection(e, Configurator.isLastContextDiagram());
        Project project = Project.getProject(selectedElements.iterator().next());
        // for ( Element elem : new ArrayList<Element>( selectedElements ) ) {
        // if ( elem instanceof Package ) {
        // selectedElements.addAll( Utils.collectOwnedElements( elem, 0 ) );
        // }
        // }
        setContext(selectedElements);

        // Ensure user-defined shortcut functions are updated
        OclEvaluator.resetEnvironment();

        // ConstraintValidationRule rule = new ConstraintValidationRule();
        constraintRule.constraintType = Type.STATIC;
        constraintRule.init(project, null);
        constraintRule.run(project, null, selectedElements);
        // RunnableSessionWrapperWithResult< Boolean > checkForRepairs =
        // new
        // RunnableSessionWrapperWithResult<Boolean>(String.format("%s - (iteration=%d)",
        // message, iterations)) {
        //
        // @Override
        // public Boolean run() {
        //
        // }
        // };
        Utils.displayValidationWindow(project, validationOutput, "User Validation Script Results");
    }

    @Override
    public void updateState() {
        // TODO Auto-generated method stub
        super.updateState();
    }

    /**
     * @return the context
     */
    public List<Element> getContext() {
        if (context == null) {
            context = new ArrayList<Element>();
        }
        return context;
    }

    /**
     * @param context the context to set
     */
    public void setContext(List<Element> context) {
        this.context = context;
    }

    /**
     * @param context the context to set
     */
    public void setContext(Collection<Element> context) {
        getContext().clear();
        getContext().addAll(context);
    }
}
