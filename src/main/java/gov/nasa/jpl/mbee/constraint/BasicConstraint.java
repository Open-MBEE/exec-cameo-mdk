/**
 * 
 */
package gov.nasa.jpl.mbee.constraint;

import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.EmfUtils;
import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.lib.MoreToString;
import gov.nasa.jpl.mbee.lib.Pair;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.DocGenUtils;
import gov.nasa.jpl.ocl.OclEvaluator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * A constraint in the context of a model defined by elements that act as
 * constraints and elements that are constrained.
 */
public class BasicConstraint implements Constraint {
    
    LinkedHashSet< Element > constrainingElements;
    //private LinkedHashSet< Element > constrainedElements;
    private LinkedHashSet< Object > constrainedObjects; // must contain constrainedElements
    Element violatedConstraintElement = null;
    Element violatedConstrainedElement = null;
    protected Boolean isConsistent = null;
    protected String errorMessage = null;
    
//    /**
//     * @param constrainingElement
//     * @param constrainedElement
//     */
//    public BasicConstraint( Element constrainingElement,
//                               Element constrainedElement ) {
//        addConstrainedElement( constrainedElement );
//        addConstrainingElement( constrainingElement );
//    }

    /**
     * @param constrainingElement
     * @param constrainedElement
     */
    public BasicConstraint( Object constraint,
                            Object constrained ) {
        addConstrainingObject( constraint );
        addConstrainedObject( constrained );
    }

//    /* (non-Javadoc)
//     * @see gov.nasa.jpl.mbee.constraint.Constraint#getConstrainedElements()
//     */
//    @Override
//    public Set< Element > getConstrainedElements() {
//        if ( constrainedElements == null ) {
//            constrainedElements = new LinkedHashSet< Element >();
//        }
//        return //Collections.unmodifiableList( Utils2.toList( constrainedElements ) );
//                constrainedElements;
//    }

    @Override
    public Set< Object > getConstrainedObjects() {
        if ( constrainedObjects == null ) {
            constrainedObjects = new LinkedHashSet< Object >();
        }
        return constrainedObjects;
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

//    /* (non-Javadoc)
//     * @see gov.nasa.jpl.mbee.constraint.Constraint#addConstrainedElements(java.util.List)
//     */
//    @Override
//    public void addConstrainedElements( Collection< Element > elements ) {
//        getConstrainedElements().addAll( elements );
//    }
//
//    /* (non-Javadoc)
//     * @see gov.nasa.jpl.mbee.constraint.Constraint#addConstrainedElement(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element)
//     */
//    @Override
//    public void addConstrainedElement( Element element ) {
//        getConstrainedElements().add( element );
//    }

    @Override
    public void addConstrainedObjects( Collection< Object > objects ) {
        getConstrainedObjects().addAll( objects );
    }

    @Override
    public void addConstrainedObject( Object obj ) {
////        addConstrainedObject( obj, null );
////    }
////    public void addConstrainedObject( Object obj, Set<Object> seen ) {
////        Pair< Boolean, Set< Object > > p = Utils2.seen( obj, true, seen );
////        if ( p.first ) return;
////        seen = p.second;
        getConstrainedObjects().add( obj );
//        if ( obj instanceof Element ) {
//            addConstrainedElement( (Element)obj );
//            getConstrainedObjects().add( obj );
//        } else if ( obj instanceof Collection ) {
//            boolean allElements = true;
//            for ( Object o : (Collection<?>)obj ) {
//                if ( !( o instanceof Element ) ) {
//                    allElements = false;
//                    break;
//                }
//            }
//            if ( allElements && !((Collection<?>)obj).isEmpty() ) {
//                for ( Object o : (Collection<?>)obj ) {
//                    addConstrainedElement( (Element)o );
//                    getConstrainedObjects().add( obj );
//                }
//            } else {
//                getConstrainedObjects().add( obj );
//            }
//        } else {
//            getConstrainedObjects().add( obj );
//        }
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
        return evaluate(true);
    }

    public Boolean evaluate( boolean complainIfFails ) {
        // try to evaluate it as is first.
        violatedConstraintElement = null;
        violatedConstrainedElement = null;
        
        // try evaluating constraint on elements as a collection
        Boolean satisfied = evaluate( getConstrainedObjects(), false );
        if ( Boolean.TRUE.equals(satisfied) ) return satisfied;

        Boolean oldSatisfied = satisfied;
        boolean oldIsConsistent = isConsistent();
        boolean newIsConsistent = !Utils2.isNullOrEmpty( getConstrainedObjects() );
        String oldErrorMessage = errorMessage; 
        if ( newIsConsistent ) satisfied = true;
        
        // try evaluating targets of a collection separately as a
        // conjunction of constraints
        boolean gotNull = false;
        for ( Object target : getConstrainedObjects() ) {
            satisfied = evaluate( target, false );
            if ( !isConsistent() ) { 
                newIsConsistent = false;
//                if ( !Utils2.isNullOrEmpty( errorMessage ) ) {
//                    newErrorMsg =
//                            newErrorMsg + ( newErrorMsg.length() > 0
//                                            ? "" + Character.LINE_SEPARATOR
//                                            : "" ) + errorMessage;
//                }
            }
            if ( satisfied == null ) {
                gotNull = true;
            } else if ( satisfied.equals( Boolean.FALSE ) ) {
//                isConsistent = newIsConsistent;
////                errorMessage = newErrorMsg;
//                if ( !isConsistent() && !Utils2.isNullOrEmpty( errorMessage ) ) {
//                    Debug.error( complainIfFails, false, errorMessage );
//                }
//                return false;
                break;
            }
        }
        isConsistent = newIsConsistent || oldIsConsistent;
        if ( !isConsistent() ) {
            errorMessage = oldErrorMessage;
            if ( !Utils2.isNullOrEmpty( errorMessage ) ) {
                Debug.error( complainIfFails, false, errorMessage );
            }
        }
        //        errorMessage = newErrorMsg;
        if ( satisfied != null ) return satisfied;
        if ( oldIsConsistent ) satisfied = oldSatisfied;
        return gotNull ? null : true;
    }

    protected Boolean evaluate( Object constrainedObject ) {
        return evaluate( constrainedObject, true );
    }
    protected Boolean evaluate( Object constrainedObject, boolean complainIfFails ) {
        boolean gotNull = false;
        isConsistent = true;
        errorMessage = null;
        for ( Element constraint : getConstrainingElements() ) {
            Object res = null;
            try {
                res = OclEvaluator.evaluateQuery( constrainedObject,
                                                         constraint );
                OclEvaluator evaluator = OclEvaluator.instance;
                if ( isConsistent ) isConsistent = evaluator.isValid();
            } catch ( Exception e ) {
                this.errorMessage = e.getLocalizedMessage() + " for OCL query \""
                        + getExpression( constraint ) //OclEvaluator.queryObjectToStringExpression( constraint )
                        + "\" on " + EmfUtils.toString( constrainedObject );
                try {
                    Debug.error( complainIfFails, false, this.errorMessage );
                } catch ( Exception ex ) {
                    System.err.println(this.errorMessage);
                }
                isConsistent = false;
            }
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
        BasicConstraint c = null;
        if ( !Utils2.isNullOrEmpty( candidateContexts ) ) {
            BasicConstraint firstNull = null;
            Boolean result = null;
            for ( Object constrained : candidateContexts ) {
                c = new BasicConstraint( constraintElement, constrained );
                result = c.evaluate(false);
                if ( result != null ) {
                    break;
                } else if ( firstNull == null ||
                            ( Utils2.isNullOrEmpty( firstNull.getConstrainedObjects() ) &&
                              !Utils2.isNullOrEmpty( c.getConstrainedObjects() ) ) ) {
                    firstNull = c;
                }
            }
            if ( result == null ) c = firstNull;
        }
        if ( c == null ) {
            Object constrained =
                    Utils2.isNullOrEmpty( candidateContexts ) ? null // REVIEW -- isn't this always true?
                                                        : candidateContexts[ 0 ];
            c = new BasicConstraint( constraintElement, constrained );
        }
        return c;
    }
    
    public static Boolean evaluateAgainst( Object constraint, Object constrained,
                                           List<Object> targets ) {
        BasicConstraint c = makeConstraint( constraint, targets, constrained );
        Boolean result = c.evaluate();
        return result;
    }
    
    public String toString() {
        return toShortString();
    }
    
    public String toShortString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Constraint:\"" + this.getExpression() + "\" on " +
                  EmfUtils.toString( this.constrainedObjects ) );
        return sb.toString();
    }
    
    protected static String toString( Object o, boolean showElementId ) {
        if ( o instanceof Element ) return toString( (Element)o, showElementId );
        return MoreToString.Helper.toString( o );
    }
    protected static String toString( Element e, boolean showElementId ) {
        return Utils.getName( e ) + (showElementId ? "[" + e.getID() + "]" : "" );
    }
    protected static String toString( Collection<? extends Object> coll, boolean showElementId ) {
        return toString( coll, Integer.MAX_VALUE, showElementId );
    }
    protected static String toString( Collection<? extends Object> coll, int maxNumber, boolean showElementId ) {
        if ( maxNumber <= 0 || Utils2.isNullOrEmpty( coll ) ) return "";
        if ( coll.size() == 1 ) return toString(coll.iterator().next(), showElementId );
        StringBuffer sb = new StringBuffer();
        sb.append("( ");
        int ct = 0;
        for ( Object o : coll ) {
            String oStr = toString( o, showElementId );
            if ( Utils2.isNullOrEmpty( oStr ) ) continue;
            if ( ct > 0 ) sb.append(", ");
            sb.append( oStr );
            ct++;
            if ( ct >= maxNumber ) break;
        }
        if ( ct < coll.size() ) {
            if ( ct > 0 ) sb.append(", ");
            sb.append( "and " + (coll.size() - ct ) + " more" );
        }
        sb.append(" )");
        return sb.toString();
    }
    
    public String toString( int maxNumber, boolean showElementIds ) {
        StringBuffer sb = new StringBuffer();
        Element constrainingElement =
                ( Utils2.isNullOrEmpty( getConstrainingElements() )
                        ? null : getConstrainingElements().iterator().next() );
        sb.append( "Constraint "
                   + toString( constrainingElement, showElementIds )
                   + " with expression, \"" + this.getExpression() + "\" on "
                   + toString( this.constrainedObjects, maxNumber, showElementIds ) );
        return sb.toString();
    }
    
    public String toStringViolated(int maxNumberOfViolatingElementsToShow,
                                   boolean showElementIds) {
        Element violatedElement = this.getViolatedConstraintElement();
        Set<Object> target = this.getConstrainedObjects();
        StringBuffer comment = new StringBuffer();
        comment.append( "constraint " + toString( violatedElement, showElementIds ) );
        comment.append( " with expression, \"" + getExpression() + "\"" );
        comment.append( " is violated" );
        if ( maxNumberOfViolatingElementsToShow > 0 &&
             !Utils2.isNullOrEmpty( target ) ) {
            comment.append( " for " +
                            toString( target, maxNumberOfViolatingElementsToShow,
                                      showElementIds ) );
        }
        return comment.toString();
    }

    public static List<Element> getComments( Element source ) {
        List<Element> results = new ArrayList< Element >();
        results.addAll(source.get_commentOfAnnotatedElement());
        if ( results.size() > 0 ) {
            Debug.out("");
        }
        return results;
    }

    /**
     * Return the constraints on this element.
     * 
     * Below, C is a constraint (UML or <<Constraint>>), and X is an element.
     * <ol>
     * <li>Not dealing with connectors for parameterizing constraints (such as
     * in parametric diagrams), so these may be processed incorrectly.
     * <li>If C has an association A with X, and A’s member end, M, is C, or
     * M.type is C, then C constrains X.
     * <li>If C is a comment or owned by a comment, and X is an annotated
     * element of the comment, then C constrains X.
     * <li>Otherwise, C does not constrain X.
     * 
     * <li>C is evaluated on collection X both as a whole and on each element.
     * <ol>
     * <li>C is evaluated against the collection of constrained elements first.
     * <li>If C is not satisfied, it is evaluated against each element and
     * returns false if any return false, true if all return true, and null
     * (invalid) otherwise.
     * </ol>
     * <li>If X is a DocGen action/activity, C is evaluated against the output
     * of X (as a single list or as individuals as stated above).
     * <ul>
     * <li>There is no means, yet, of distinguishing whether C is intended for X
     * or its DocGen output, so it is evaluated for both contexts.
     * <li>Since these contexts (DocGen and “static”) are evaluated separately,
     * it is likely to fail in the unintended context.
     * </ul>
     * </ol>
     * 
     * @param constrainedObject
     * @return a list of constraint elements that constrain the
     *         constrainedObject
     */
    public static List< Element > getConstraintElements( Object constrainedObject ) {
        LinkedHashSet< Element > constraintElements = new LinkedHashSet< Element >();
        if ( constrainedObject instanceof Element ) {
            Element constrainedElement = ((Element)constrainedObject);
            
            // Is the element stereotyped as <<Constraint>> or a UML Constraint?
            if ( StereotypesHelper.hasStereotypeOrDerived( constrainedElement,
                                                           DocGen3Profile.constraintStereotype ) ||
                 constrainedElement instanceof com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint ) {
                // Add the element as a constraint on itself if it is not a
                // constraint on another element, to which it is anchored or
                // associated, possibly inside a Constraint block.
                if ( !( constrainedElement instanceof Comment ) ||
                     Utils2.isNullOrEmpty( ( (Comment)constrainedElement ).getAnnotatedElement() ) ) {
                    constraintElements.add( constrainedElement );
                }
                
            }
//            if ( constrainedElement instanceof com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint ) {
//                constraintElements.add( constrainedElement );
//            }
            Collection< com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint > constrs =
                    constrainedElement.get_constraintOfConstrainedElement();
            if ( constrs != null ) constraintElements.addAll( constrs );

//            constraintElements.addAll( Utils.collectRelatedElementsByStereotypeString( constrainedElement, DocGen3Profile.constraintStereotype, 0, true, 1 ) );
            for ( Element comment : BasicConstraint.getComments( constrainedElement ) ) {
                if (StereotypesHelper.hasStereotypeOrDerived(comment, DocGen3Profile.constraintStereotype) ) {
                    constraintElements.add( comment );
                }
            }
        }
        if ( constrainedObject instanceof Collection ) {
            for ( Object o : (Collection<?>)constrainedObject ) {
                constraintElements.addAll( getConstraintElements( o ) );
            }
        }
        return Utils2.toList( constraintElements );
    }
    
    public static List< Constraint > getConstraints( Object constrainedObject ) {
        List<Constraint> constraints = new ArrayList< Constraint >();
        List< Element > constraintElements = getConstraintElements( constrainedObject );
        for ( Element constraint : constraintElements  ) {
            Constraint c = BasicConstraint.makeConstraint( constraint, constrainedObject );
            constraints.add( c );
        }
        return constraints;
    }

    @Override
    public boolean isConsistent() {
        if ( isConsistent == null ) evaluate();
        return isConsistent ;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage( String errorMessage ) {
        this.errorMessage = errorMessage;
    }



}
