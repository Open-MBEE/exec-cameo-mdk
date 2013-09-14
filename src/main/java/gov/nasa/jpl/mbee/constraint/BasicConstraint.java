/**
 * 
 */
package gov.nasa.jpl.mbee.constraint;

import gov.nasa.jpl.mbee.lib.Pair;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mgss.mbee.docgen.generator.DocumentGenerator;
import gov.nasa.jpl.mgss.mbee.docgen.generator.GenerationContext;
import gov.nasa.jpl.ocl.OclEvaluator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * A constraint in the context of a model defined by elements that act as
 * constraints and elements that are constrained.
 */
public class BasicConstraint implements Constraint {

    LinkedHashSet< Element > constrainingElements;
    LinkedHashSet< Element > constrainedElements;
    Element violatedConstraintElement = null;
    
    /**
     * @param constrainingElement
     * @param constrainedElement
     */
    public BasicConstraint( Element constrainingElement,
                               Element constrainedElement ) {
        addConstrainedElement( constrainedElement );
        addConstrainingElement( constrainingElement );
    }

    /**
     * @param constrainingElement
     * @param constrainedElement
     */
    public BasicConstraint( Object constraint,
                               Object constrained ) {
        addConstrainingObject( constraint );
        addConstrainedObject( constrained );
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.mbee.constraint.Constraint#getConstrainedElements()
     */
    @Override
    public Set< Element > getConstrainedElements() {
        return //Collections.unmodifiableList( Utils2.toList( constrainedElements ) );
                constrainedElements;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.mbee.constraint.Constraint#getConstrainingElements()
     */
    @Override
    public Set< Element > getConstrainingElements() {
        return constrainingElements;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.mbee.constraint.Constraint#addConstrainedElements(java.util.List)
     */
    @Override
    public void addConstrainedElements( Collection< Element > elements ) {
        constrainedElements.addAll( elements );
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.mbee.constraint.Constraint#addConstrainedElement(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element)
     */
    @Override
    public void addConstrainedElement( Element element ) {
        constrainedElements.add( element );
    }

    public void addConstrainedObject( Object obj ) {
        addConstrainedObject( obj, null );
    }
    public void addConstrainedObject( Object obj, Set<Object> seen ) {
        Pair< Boolean, Set< Object >> p = Utils2.seen( obj, true, seen );
        if ( p.first ) return;
        seen = p.second;
        if ( obj instanceof Element ) {
            addConstrainedElement( (Element)obj );
        }
        if ( obj instanceof Collection ) {
            for ( Object o : (Collection<?>)obj ) {
                addConstrainedObject( o );
            }
        }
    }

    public void addConstrainingObject( Object obj ) {
        addConstrainingObject( obj, null );
    }
    public void addConstrainingObject( Object obj, Set<Object> seen ) {
        Pair< Boolean, Set< Object >> p = Utils2.seen( obj, true, seen );
        if ( p.first ) return;
        seen = p.second;
        if ( obj instanceof Element ) {
            addConstrainingElement( (Element)obj );
        }
        if ( obj instanceof Collection ) {
            for ( Object o : (Collection<?>)obj ) {
                addConstrainingObject( o );
            }
        }
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.mbee.constraint.Constraint#addConstrainingElement(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element)
     */
    @Override
    public void addConstrainingElement( Element constrainingElement ) {
        constrainingElements.add( constrainingElement );
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.mbee.constraint.Constraint#addConstrainingElements(java.util.Collection)
     */
    @Override
    public void addConstrainingElements( Collection< Element > elements ) {
        constrainingElements.addAll( elements );
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.mbee.constraint.Constraint#getExpression()
     */
    @Override
    public String getExpression() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.mbee.constraint.Constraint#evaluate()
     */
    @Override
    public Boolean evaluate() {
        // try to evaluate it as is first.
        violatedConstraintElement = null;
        
        // try evaluating constraint on elements as a collection
        Boolean satisfied = evaluate( getConstrainedElements() );
        if ( satisfied != null ) return satisfied;

        // try evaluating elements of a collection separately as a
        // conjunction of constraints
        boolean gotNull = false;
        for ( Element element : getConstrainedElements() ) {
            satisfied = evaluate( element );
            if ( satisfied == null ) {
                gotNull = true;
            } else if ( satisfied.equals( Boolean.FALSE ) ) {
                return false;
            }
        }
        return gotNull ? null : true;
    }

    protected Boolean evaluate( Object constrainedObject ) {
        boolean gotNull = false;
        for ( Element constraint : getConstrainingElements() ) {
            Object res = OclEvaluator.evaluateQuery( constrainedObject,
                                                     constraint );
            if ( res == null ) {
                gotNull = true;
            } else if ( !Utils.isTrue( res, false ) ) {
                violatedConstraintElement = constraint;
                return false;
            }
        }
        return gotNull ? null : true;
    }
    
    public Element getViolatedConstraintElement() {
        if ( violatedConstraintElement == null ) {
            evaluate();
        }
        return violatedConstraintElement;
    }

    public static String getExpression( Object constraint ) {
        String expr = OclEvaluator.queryObjectToStringExpression( constraint );
        return expr;
    }

    /**
     * Create a BasicConstraint on one of two Elements or Collections.
     * 
     * @param constraintElement
     *            the model element representing the constraint
     * @param constrained1
     *            the first candidate to be constrained
     * @param constrained2
     *            the second candidate to be constrained
     * @return a BasicConstraint on the first candidate if the evaluation works
     *         or the evaluation does not work with the second candidate;
     *         otherwise return a BasicConstraint on the second candidate.
     */
    public static BasicConstraint makeConstraint( Object constraintElement,
                                                  Object...candidateContexts) {
//                                                  Object constrained1,
//                                                  Object constrained2 ) {
//
        BasicConstraint c = null;
        for ( Object constrained : candidateContexts ) {
            c = new BasicConstraint( constraintElement, constrained );
            Boolean result = c.evaluate();
            if ( result != null ) {
                break;
            }
//            c.getConstrainedElements().clear();
//            c.addConstrainedObject(constrained2);
//            result = c.evaluate();
//            if ( result == null ) {
//                c.getConstrainedElements().clear();
//                c.addConstrainedObject(constrained1);
//            }
        }
        if ( c == null ) {
            Object constrained =
                    Utils2.isNullOrEmpty( candidateContexts ) ? null
                                                        : candidateContexts[ 0 ];
            c = new BasicConstraint( constraintElement, constrained );
        }
        return c;
    }
    
    public static Boolean evaluateAgainst( Object constraint, Object constrained,
                                           List<Element> targets ) {
        BasicConstraint c = makeConstraint( constraint, targets, constrained );
        Boolean result = c.evaluate();
        return result;
    }

}
