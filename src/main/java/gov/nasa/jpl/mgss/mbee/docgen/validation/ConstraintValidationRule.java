/**
 * 
 */
package gov.nasa.jpl.mgss.mbee.docgen.validation;

import gov.nasa.jpl.mbee.constraint.BasicConstraint;
import gov.nasa.jpl.mbee.lib.CompareUtils;
import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.Utils2;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.magicdraw.validation.ElementValidationRuleImpl;
import com.nomagic.magicdraw.validation.SmartListenerConfigurationProvider;
import com.nomagic.uml2.ext.jmi.smartlistener.SmartListenerConfig;
import com.nomagic.uml2.ext.magicdraw.classes.mdinterfaces.Interface;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * A constraint in some context of the model, whose violation will be posted in
 * the MD validation results window.
 * 
 */
public class ConstraintValidationRule implements ElementValidationRuleImpl, SmartListenerConfigurationProvider {
    
    protected Element constraintElement = null;
    //protected List<Element> context = new ArrayList<Element>(); 
    protected Element context = null;
    protected Constraint constraint = null;

    protected Map< BaseElement, Set< gov.nasa.jpl.mbee.constraint.Constraint > > elementToConstraintMap =
            new TreeMap< BaseElement, Set< gov.nasa.jpl.mbee.constraint.Constraint > >(CompareUtils.GenericComparator.instance());
    protected Map< gov.nasa.jpl.mbee.constraint.Constraint, Set< BaseElement > > constraintToElementMap =
            new TreeMap< gov.nasa.jpl.mbee.constraint.Constraint, Set< BaseElement > >(CompareUtils.GenericComparator.instance());
    
    public ConstraintValidationRule() {
        super();

        boolean wasOn = Debug.isOn();
        Debug.turnOn();                
        Debug.outln( "ConstraintValidationRule()" );
        if ( !wasOn ) Debug.turnOff();
    }
    
//    public ConstraintValidationRule( Element constraintElement, Element context, Constraint constraint ) {
//        this.constraintElement = constraintElement;
//        this.context = context;
//        this.constraint = constraint;
//    }
////    public ConstraintValidationRule( Element constraint, Collection<Element> context ) {
////        this.constraint = constraint;
////        this.context.addAll( context );
////    }
    
    /* (non-Javadoc)
     * @see com.nomagic.magicdraw.validation.ElementValidationRuleImpl#init(com.nomagic.magicdraw.core.Project, com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint)
     */
    @Override
    public void init(Project paramProject, Constraint paramConstraint) {
        boolean wasOn = Debug.isOn();
        Debug.turnOn();                
        Debug.outln( "init(Project=" + paramProject + ", Constraint="
                     + paramConstraint + ")" );
        if ( constraintElement == null ) constraintElement = paramConstraint;
        // collect all constraints and the objects they constrain
        Collection< String > ids = paramProject.getAllIDS();
        for ( String id : ids ) {
            BaseElement elem = paramProject.getElementByID( id );
            if ( elem == null ) continue;
            List< gov.nasa.jpl.mbee.constraint.Constraint > constraints = 
                    BasicConstraint.getConstraints( elem );
            //Set< gov.nasa.jpl.mbee.constraint.Constraint > constrSet =
            //        new HashSet< gov.nasa.jpl.mbee.constraint.Constraint >( constraints );
            //elementToConstraintMap.put( elem, constrSet );
//            HashSet<BaseElement> seen = new HashSet< BaseElement >();
//            while ( elem != null ) {
//                if ( seen.contains( elem ) ) break;
                Utils2.addAllToSet( elementToConstraintMap, elem, constraints );
                for ( gov.nasa.jpl.mbee.constraint.Constraint constr : constraints ) {
                    Utils2.addToSet( constraintToElementMap, constr, elem );
                }
//                seen.add( elem );
//                elem = elem.getObjectParent();
//            }
        }
        if ( !wasOn ) Debug.turnOff();
    }

    public Collection< gov.nasa.jpl.mbee.constraint.Constraint >
            getAffectedConstraints( Collection<? extends Element> affectedElements ) {
        Set< gov.nasa.jpl.mbee.constraint.Constraint > constraints =
                new TreeSet< gov.nasa.jpl.mbee.constraint.Constraint >( CompareUtils.GenericComparator.instance() );
        for ( Element elem : affectedElements ) {
            constraints.addAll( elementToConstraintMap.get( elem ) );
        }
        return constraints;
    }
    
    /* (non-Javadoc)
     * @see com.nomagic.magicdraw.validation.ElementValidationRuleImpl#run(com.nomagic.magicdraw.core.Project, com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint, java.util.Collection)
     */
    @Override
    public Set<Annotation> run(Project paramProject,
                               Constraint paramConstraint,
                               Collection<? extends Element> paramCollection) {
        Set<Annotation> result = new HashSet<Annotation>();
        
        boolean wasOn = Debug.isOn();
        Debug.turnOn();
        
        Debug.outln( "run(Project, " + paramConstraint + " , "
                + paramCollection + ")" );
   
        Collection< gov.nasa.jpl.mbee.constraint.Constraint > constraints =(Collection<gov.nasa.jpl.mbee.constraint.Constraint>)
                ( Utils2.isNullOrEmpty( paramCollection ) ? (constraintToElementMap == null ? Utils2.newList() : constraintToElementMap.keySet() )
                                                  : getAffectedConstraints( paramCollection ) );
        
        for ( gov.nasa.jpl.mbee.constraint.Constraint constraint : constraints ) {
            Boolean satisfied = constraint.evaluate();
            if ( satisfied != null && satisfied.equals( Boolean.FALSE ) ) {
                //List<NMAction> actionList = new ArrayList<NMAction>();
                //actionList.add(styleAdd);
                Annotation annotation =
                        new Annotation( constraint.getViolatedConstraintElement(), paramConstraint );
                result.add(annotation);
            }
        }
        
        if ( !wasOn ) Debug.turnOff();
        return result;
    }

    /* (non-Javadoc)
     * @see com.nomagic.magicdraw.validation.ElementValidationRuleImpl#dispose()
     */
    @Override
    public void dispose() {
        boolean wasOn = Debug.isOn();
        Debug.turnOn();                
        Debug.outln( "init()" );
        if ( !wasOn ) Debug.turnOff();

        // TODO Auto-generated method stub
    }

    @Override
    public Map< Class< ? extends Element >, Collection< SmartListenerConfig > >
            getListenerConfigurations() {
        Map<Class<? extends Element>, Collection<SmartListenerConfig>> configMap = 
                new HashMap<Class<? extends Element>, Collection<SmartListenerConfig>>();
        SmartListenerConfig config = new SmartListenerConfig();
        //SmartListenerConfig nested = config.listenToNested(PropertyNames.ELEMENT);
        SmartListenerConfig nested = config.listenToNested("ownedOperation");
        nested.listenTo("name");
        nested.listenTo("ownedParameter");

        Collection<SmartListenerConfig> configs = Collections.singletonList(config);
        configMap.put(Element.class, configs);
        configMap.put(Interface.class, configs);
        return configMap;
    }

}
