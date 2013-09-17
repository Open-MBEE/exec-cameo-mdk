/**
 * 
 */
package gov.nasa.jpl.mgss.mbee.docgen.validation;

import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;
import gov.nasa.jpl.ocl.OclEvaluator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.validation.ElementValidationRuleImpl;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * A constraint in some context of the model, whose violation will be posted in
 * the MD validation results window.
 * 
 */
public class ConstraintValidationRule implements ElementValidationRuleImpl {
    
    protected Element constraintElement = null;
    //protected List<Element> context = new ArrayList<Element>(); 
    protected Element context = null;
    protected Constraint constraint = null;

    
    public ConstraintValidationRule( Element constraintElement, Element context, Constraint constraint ) {
        this.constraintElement = constraintElement;
        this.context = context;
        this.constraint = constraint;
    }
//    public ConstraintValidationRule( Element constraint, Collection<Element> context ) {
//        this.constraint = constraint;
//        this.context.addAll( context );
//    }

    /* (non-Javadoc)
     * @see com.nomagic.magicdraw.validation.ElementValidationRuleImpl#init(com.nomagic.magicdraw.core.Project, com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint)
     */
    @Override
    public void init(Project paramProject, Constraint paramConstraint) {
        if ( constraintElement == null ) constraintElement = paramConstraint;
    }

    /* (non-Javadoc)
     * @see com.nomagic.magicdraw.validation.ElementValidationRuleImpl#run(com.nomagic.magicdraw.core.Project, com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint, java.util.Collection)
     */
    @Override
    public Set<Annotation> run(Project paramProject,
                               Constraint paramConstraint,
                               Collection<? extends Element> paramCollection) {
        Set<Annotation> result = new HashSet<Annotation>();
        
        // check constraint unless there is a collection of changed elements
        // that does not include a non-null context
        boolean check = context == null ||
                        Utils2.isNullOrEmpty(paramCollection) ||
                        paramCollection.contains(context);
        if (!check) return result;

        // TODO -- put these statements in accessor functions
        if ( constraintElement == null ) constraintElement = paramConstraint;
        if ( constraint == null ) constraint = paramConstraint;
        if ( constraint == null && constraintElement instanceof Constraint ) {
            constraint = (Constraint) constraintElement;
        }

        Object query = null;
        if (constraintElement != null &&
            StereotypesHelper.hasStereotypeOrDerived(constraintElement,
                                                     DocGen3Profile.expressionChoosable)) {
            query = GeneratorUtils.getObjectProperty(constraintElement,
                                                     DocGen3Profile.expressionChoosable,
                                                     "expression", null);
        } else if (constraint != null) {
            query = constraint.getSpecification();
        } 
        
        Object evalResult = null;
        if ( query != null ) {
            evalResult = OclEvaluator.evaluateQuery(context, query);
        }
        if ( !Utils.isTrue(evalResult, false) ) {
            // create the annotation
            List<NMAction> actionList = new ArrayList<NMAction>();
            //actionList.add(styleAdd);
            Annotation annotation = new Annotation(context, constraint, actionList);
            result.add(annotation);
        }

        
        return result;
    }

    /* (non-Javadoc)
     * @see com.nomagic.magicdraw.validation.ElementValidationRuleImpl#dispose()
     */
    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

}
