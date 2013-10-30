package gov.nasa.jpl.mgss.mbee.docgen.actions;

import gov.nasa.jpl.mbee.lib.MDUtils;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ConstraintValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.ocl.OclEvaluator;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ValidateConstraints extends MDAction {
    /**
     * 
     */
    private static final long serialVersionUID = 2202161655434764023L;

    protected List< Element > context = new ArrayList< Element >(); // REVIEW -- Is this being used?

    public static final String actionid = "ValidateConstraints";
    
    public static String actionText = "Validate constraints";
    
    private ConstraintValidationRule constraintRule = new ConstraintValidationRule();//new ValidationRule("Constraint", "Model constraint violation", ViolationSeverity.WARNING);

    private ValidationSuite validationUi = new ValidationSuite("sweet");
    private Collection<ValidationSuite> validationOutput = new ArrayList<ValidationSuite>();

    
    public ValidateConstraints( Element context ) {
        super(actionid, actionText, null, null);
        if ( context != null ) getContext().add( context ); 
        validationUi.addValidationRule(constraintRule);
        //Need Collection to use the utils.DisplayValidationWindow method
        validationOutput.add(validationUi);
      }
      public ValidateConstraints() {
        this(null);
      }

    @Override
    public void actionPerformed( ActionEvent e ) {
        Collection< Element > selectedElements = MDUtils.getSelection( e );
//        for ( Element elem : new ArrayList<Element>( selectedElements ) ) {
//            if ( elem instanceof Package ) {
//                selectedElements.addAll( Utils.collectOwnedElements( elem, 0 ) );
//            }
//        }
        setContext( selectedElements );

        // Ensure user-defined shortcut functions are updated
        OclEvaluator.resetEnvironment();
        
        //ConstraintValidationRule rule = new ConstraintValidationRule();
        constraintRule.init( Utils.getProject(), null );
        Set< Annotation > annotations = constraintRule.run( Utils.getProject(), null, selectedElements );
//        RunnableSessionWrapperWithResult< Boolean > checkForRepairs =
//                new RunnableSessionWrapperWithResult<Boolean>(String.format("%s - (iteration=%d)", message, iterations)) {
//
//                    @Override
//                    public Boolean run() {
//                        
//                    }
//                };
        Utils.displayValidationWindow(validationOutput, "User Validation Script Results");  
    }

    @Override
    public void updateState() {
        // TODO Auto-generated method stub
        super.updateState();
    }

    /**
     * @return the context
     */
    public List< Element > getContext() {
      if ( context == null ) context = new ArrayList< Element >();
      return context;
    }

    /**
     * @param context the context to set
     */
    public void setContext( List< Element > context ) {
      this.context = context;
    }

    /**
     * @param context the context to set
     */
    public void setContext( Collection< Element > context ) {
      getContext().clear();
      getContext().addAll( context );
    }
}
