/**
 * 
 */
package gov.nasa.jpl.mbee.constraint;

import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.lib.Pair;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.DocGenUtils;
import gov.nasa.jpl.ocl.OclEvaluator;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * A constraint in the context of a model defined by elements that act as
 * constraints and elements that are constrained.
 */
public class BasicConstraint implements Constraint {

    LinkedHashSet< Element > constrainingElements;
    LinkedHashSet< Element > constrainedElements;
    Element violatedConstraintElement = null;
    Element violatedConstrainedElement = null;
    
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
        if ( constrainedElements == null ) {
            constrainedElements = new LinkedHashSet< Element >();
        }
        return //Collections.unmodifiableList( Utils2.toList( constrainedElements ) );
                constrainedElements;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.mbee.constraint.Constraint#getConstrainingElements()
     */
    @Override
    public Set< Element > getConstrainingElements() {
        if ( constrainingElements == null ) {
            constrainingElements = new LinkedHashSet< Element >();
        }
        return constrainingElements;
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.mbee.constraint.Constraint#addConstrainedElements(java.util.List)
     */
    @Override
    public void addConstrainedElements( Collection< Element > elements ) {
        if ( constrainedElements == null ) {
            constrainedElements = new LinkedHashSet< Element >();
        }
        constrainedElements.addAll( elements );
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.mbee.constraint.Constraint#addConstrainedElement(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element)
     */
    @Override
    public void addConstrainedElement( Element element ) {
        if ( constrainedElements == null ) {
            constrainedElements = new LinkedHashSet< Element >();
        }
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
        if ( constrainingElements == null ) {
            constrainingElements = new LinkedHashSet< Element >();
        }
        constrainingElements.add( constrainingElement );
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.mbee.constraint.Constraint#addConstrainingElements(java.util.Collection)
     */
    @Override
    public void addConstrainingElements( Collection< Element > elements ) {
        if ( constrainingElements == null ) {
            constrainingElements = new LinkedHashSet< Element >();
        }
        constrainingElements.addAll( elements );
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.mbee.constraint.Constraint#getExpression()
     */
    @Override
    public String getExpression() {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        boolean multiple = getConstrainingElements().size() > 1;
        for ( Element e : getConstrainingElements() ) {
            if ( first ) first = false;
            else sb.append(" and ");
            if ( multiple ) sb.append( "(" );
            String expr = getExpression( e );
            if ( !Utils2.isNullOrEmpty( expr ) ) {
                sb.append( expr );
            } else if ( !Utils2.isNullOrEmpty( e.getHumanName() ) ) {
                sb.append( e.getHumanName() );
            } else {
                sb.append( e.getHumanType() );
            }
            if ( multiple ) sb.append( ")" );
        }
        return sb.toString();
    }

    /* (non-Javadoc)
     * @see gov.nasa.jpl.mbee.constraint.Constraint#evaluate()
     */
    @Override
    public Boolean evaluate() {
        // try to evaluate it as is first.
        violatedConstraintElement = null;
        violatedConstrainedElement = null;
        
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
                if ( constrainedObject instanceof Element ) {
                    violatedConstrainedElement = (Element)constrainedObject;
                }
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

    public Element getViolatedConstrainedElement() {
        if ( violatedConstrainedElement == null ) {
            evaluate();
        }
        return violatedConstrainedElement;
    }

    public static String getExpression( Object constraint ) {
        if ( constraint instanceof Constraint ) return ((Constraint)constraint).getExpression();
        String expr = null;
        if ( constraint instanceof Element ) {
            Element e = (Element)constraint;
            if ( e instanceof com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint ) {
                com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint c = 
                        (com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint)e;
                expr = DocGenUtils.fixString( c.getSpecification() );
            } else if ( GeneratorUtils.hasStereotypeByString(e, DocGen3Profile.constraintStereotype, true) ) {
                Object v = GeneratorUtils.getObjectProperty( e, DocGen3Profile.constraintStereotype, "expression", null);
                expr = v.toString();
            }
        }
        if ( Utils2.isNullOrEmpty( expr ) ) {
            expr = OclEvaluator.queryObjectToStringExpression( constraint );
        }
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
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Constraint:" + this.getExpression() + ",on:" + this.constrainedElements );
        return sb.toString();
    }
    

}
