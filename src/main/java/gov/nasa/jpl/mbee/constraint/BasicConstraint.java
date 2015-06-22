/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 * 
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.mbee.constraint;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.DocGenUtils;
import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.lib.MoreToString;
import gov.nasa.jpl.mbee.lib.Pair;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils2;

import java.util.ArrayList;
import java.util.Collection;
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

    public enum Type {
        UML, STATIC, DYNAMIC, CONSTRAINT_ST, VIEWPOINT_CONSTRAINT_ST, ANY
    };

    LinkedHashSet<Element>        constrainingElements;
    // private LinkedHashSet< Element > constrainedElements;
    private LinkedHashSet<Object> constrainedObjects;                // must
                                                                      // contain
                                                                      // constrainedElements
    Element                       violatedConstraintElement  = null;
    Element                       violatedConstrainedElement = null;
    protected Boolean             isConsistent               = null;
    protected String              errorMessage               = null;
    protected boolean             reported                   = false;

    // /**
    // * @param constrainingElement
    // * @param constrainedElement
    // */
    // public BasicConstraint( Element constrainingElement,
    // Element constrainedElement ) {
    // addConstrainedElement( constrainedElement );
    // addConstrainingElement( constrainingElement );
    // }

    /**
     * @param constrainingElement
     * @param constrainedElement
     */
    public BasicConstraint(Object constraint, Object constrained) {
        addConstrainingObject(constraint);
        addConstrainedObject(constrained);
    }

    /**
     * @param constrainingElement
     * @param constrainedElement
     */
    public BasicConstraint(Object constraint, Collection<Object> constrained) {
        addConstrainingObject(constraint);
        addConstrainedObjects(constrained);
    }

    // public static Boolean getBooleanPropertyValue( Element vpConstraint,
    // String stereotypeName, String propName ) {
    // // try to get default for the iterate property
    // Property prop = StereotypesHelper.getPropertyByName(
    // StereotypesHelper.getStereotype( Utils.getProject(), stereotypeName ),
    // propName );
    // ValueSpecification defaultVal = prop.getDefaultValue();
    // // now get the property value
    // Object propVal = GeneratorUtils.getObjectProperty( vpConstraint,
    // stereotypeName, propName, defaultVal );
    // Boolean boolVal = Utils.isTrue( DocGenUtils.fixString( propVal, false ),
    // true );
    // return boolVal;
    // }
    public static boolean iterateViewpointConstrraint(Element vpConstraint) {
        Boolean iterate = (Boolean)GeneratorUtils.getObjectProperty(vpConstraint,
                DocGen3Profile.viewpointConstraintStereotype, "iterate", true);
        // Boolean iterate = getBooleanPropertyValue( vpConstraint,
        // DocGen3Profile.expressionChoosable, "iterate" );
        boolean result = !Boolean.FALSE.equals(iterate);
        return result;
    }

    public static boolean reportedViewpointConstrraint(Element vpConstraint) {
        Boolean report = (Boolean)GeneratorUtils.getObjectProperty(vpConstraint,
                DocGen3Profile.viewpointConstraintStereotype, "validationReport", false);
        boolean result = Boolean.TRUE.equals(report);
        return result;
    }

    // /* (non-Javadoc)
    // * @see gov.nasa.jpl.mbee.constraint.Constraint#getConstrainedElements()
    // */
    // @Override
    // public Set< Element > getConstrainedElements() {
    // if ( constrainedElements == null ) {
    // constrainedElements = new LinkedHashSet< Element >();
    // }
    // return //Collections.unmodifiableList( Utils2.toList( constrainedElements
    // ) );
    // constrainedElements;
    // }

    /**
     * Determines whether the input Constraint, constraint element, or all
     * constraints in a collection are of the specified type. This establishes a
     * policy for how the different kinds of constraints can be used.
     * 
     * @param constraint
     * @param type
     * @return true iff the constraint is of the specified type
     */
    public static boolean constraintIsType(Object constraint, Type type) {
        if (constraint instanceof Constraint) {
            return constraintIsType(((Constraint)constraint).getConstrainingElements(), type);
        }
        if (constraint instanceof Collection) {
            for (Object c: (Collection<?>)constraint) {
                if (constraintIsType(c, type)) {
                    return true;
                }
            }
        }
        if (constraint instanceof Element) {
            Element elem = (Element)constraint;
            if (!elementIsConstraint(elem))
                return false;
            switch (type) {
                case ANY:
                    return true;

                case UML:
                    return elementIsUmlConstraint(elem);

                case CONSTRAINT_ST:
                    return elementIsDocGenConstraint(elem);

                case VIEWPOINT_CONSTRAINT_ST:
                case DYNAMIC:
                    return elementIsViewpointConstraint(elem);

                case STATIC:
                    return !elementIsViewpointConstraint(elem);
                default:
                    return false;
            }
        }
        return false;
    }

    @Override
    public Set<Object> getConstrainedObjects() {
        if (constrainedObjects == null) {
            constrainedObjects = new LinkedHashSet<Object>();
        }
        return constrainedObjects;
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.mbee.constraint.Constraint#getConstrainingElements()
     */
    @Override
    public Set<Element> getConstrainingElements() {
        return getConstrainingElements(Type.ANY);
    }

    public Set<Element> getConstrainingElements(Type type) {
        if (constrainingElements == null) {
            constrainingElements = new LinkedHashSet<Element>();
        }
        if (type == Type.ANY)
            return constrainingElements;
        Set<Element> filtered = new LinkedHashSet<Element>();
        for (Element c: constrainingElements) {
            if (constraintIsType(c, type)) {
                filtered.add(c);
            }
        }
        return filtered;
    }

    // /* (non-Javadoc)
    // * @see
    // gov.nasa.jpl.mbee.constraint.Constraint#addConstrainedElements(java.util.List)
    // */
    // @Override
    // public void addConstrainedElements( Collection< Element > elements ) {
    // getConstrainedElements().addAll( elements );
    // }
    //
    // /* (non-Javadoc)
    // * @see
    // gov.nasa.jpl.mbee.constraint.Constraint#addConstrainedElement(com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element)
    // */
    // @Override
    // public void addConstrainedElement( Element element ) {
    // getConstrainedElements().add( element );
    // }

    @Override
    public void addConstrainedObjects(Collection<Object> objects) {
        getConstrainedObjects().addAll(objects);
    }

    @Override
    public void addConstrainedObject(Object obj) {
        // // addConstrainedObject( obj, null );
        // // }
        // // public void addConstrainedObject( Object obj, Set<Object> seen ) {
        // // Pair< Boolean, Set< Object > > p = Utils2.seen( obj, true, seen );
        // // if ( p.first ) return;
        // // seen = p.second;
        getConstrainedObjects().add(obj);
        // if ( obj instanceof Element ) {
        // addConstrainedElement( (Element)obj );
        // getConstrainedObjects().add( obj );
        // } else if ( obj instanceof Collection ) {
        // boolean allElements = true;
        // for ( Object o : (Collection<?>)obj ) {
        // if ( !( o instanceof Element ) ) {
        // allElements = false;
        // break;
        // }
        // }
        // if ( allElements && !((Collection<?>)obj).isEmpty() ) {
        // for ( Object o : (Collection<?>)obj ) {
        // addConstrainedElement( (Element)o );
        // getConstrainedObjects().add( obj );
        // }
        // } else {
        // getConstrainedObjects().add( obj );
        // }
        // } else {
        // getConstrainedObjects().add( obj );
        // }
    }

    public void addConstrainingObject(Object obj) {
        addConstrainingObject(obj, null);
    }

    public void addConstrainingObject(Object obj, Set<Object> seen) {
        Pair<Boolean, Set<Object>> p = Utils2.seen(obj, true, seen);
        if (p.first)
            return;
        seen = p.second;
        if (obj instanceof Element) {
            addConstrainingElement((Element)obj);
        }
        if (obj instanceof Collection) {
            for (Object o: (Collection<?>)obj) {
                addConstrainingObject(o);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * gov.nasa.jpl.mbee.constraint.Constraint#addConstrainingElement(com.nomagic
     * .uml2.ext.magicdraw.classes.mdkernel.Element)
     */
    @Override
    public void addConstrainingElement(Element constrainingElement) {
        if (constrainingElements == null) {
            constrainingElements = new LinkedHashSet<Element>();
        }
        constrainingElements.add(constrainingElement);
        if (!reported && elementIsViewpointConstraint(constrainingElement)) {
            setReported(reportedViewpointConstrraint(constrainingElement));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * gov.nasa.jpl.mbee.constraint.Constraint#addConstrainingElements(java.
     * util.Collection)
     */
    @Override
    public void addConstrainingElements(Collection<Element> elements) {
        for (Element e: elements) {
            addConstrainingElement(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.mbee.constraint.Constraint#getExpression()
     */
    @Override
    public String getExpression() {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        boolean multiple = getConstrainingElements().size() > 1;
        for (Element e: getConstrainingElements()) {
            if (first)
                first = false;
            else
                sb.append(" and ");
            if (multiple)
                sb.append("(");
            String expr = getExpression(e);
            if (!Utils2.isNullOrEmpty(expr)) {
                sb.append(expr);
            } else if (!Utils2.isNullOrEmpty(e.getHumanName())) {
                sb.append(e.getHumanName());
            } else {
                sb.append(e.getHumanType());
            }
            if (multiple)
                sb.append(")");
        }
        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.nasa.jpl.mbee.constraint.Constraint#evaluate()
     */
    @Override
    public Boolean evaluate() {
        return evaluate(true);
    }

    public Boolean evaluate(boolean complainIfFails) {
        violatedConstraintElement = null;
        violatedConstrainedElement = null;

        // try evaluating constraint on elements as a collection
        Boolean satisfied = false;
        try {
            satisfied = evaluate(getConstrainedObjects(), false);
        } catch (Throwable e) {
            Debug.error(true, true, "Didn't work on elements as a collection:\n");
            e.printStackTrace();
        }
        if (Boolean.TRUE.equals(satisfied))
            return satisfied;

        Boolean oldSatisfied = satisfied;
        boolean oldIsConsistent = isConsistent();
        boolean newIsConsistent = !Utils2.isNullOrEmpty(getConstrainedObjects());
        String oldErrorMessage = errorMessage;
        if (newIsConsistent)
            satisfied = true;

        // try evaluating targets of a collection separately as a
        // conjunction of constraints
        boolean gotNull = false;
        for (Object target: getConstrainedObjects()) {
            satisfied = evaluate(target, false);
            if (!isConsistent()) {
                newIsConsistent = false;
                // if ( !Utils2.isNullOrEmpty( errorMessage ) ) {
                // newErrorMsg =
                // newErrorMsg + ( newErrorMsg.length() > 0
                // ? "" + Character.LINE_SEPARATOR
                // : "" ) + errorMessage;
                // }
            }
            if (satisfied == null) {
                gotNull = true;
            } else if (satisfied.equals(Boolean.FALSE)) {
                // isConsistent = newIsConsistent;
                // // errorMessage = newErrorMsg;
                // if ( !isConsistent() && !Utils2.isNullOrEmpty( errorMessage )
                // ) {
                // Debug.error( complainIfFails, false, errorMessage );
                // }
                // return false;
                break;
            }
        }
        isConsistent = newIsConsistent || oldIsConsistent;
        if (!isConsistent()) {
            errorMessage = oldErrorMessage;
            if (!Utils2.isNullOrEmpty(errorMessage)) {
                Debug.error(complainIfFails, false, errorMessage);
            }
        }
        // errorMessage = newErrorMsg;
        if (satisfied != null)
            return satisfied;
        if (oldIsConsistent)
            satisfied = oldSatisfied;
        return gotNull ? null : true;
    }

    protected Boolean evaluate(Object constrainedObject) {
        return evaluate(constrainedObject, true);
    }

    protected Boolean evaluate(Object constrainedObject, boolean complainIfFails) {
        boolean gotNull = false;
        isConsistent = true;
        errorMessage = null;
        for (Element constraint: getConstrainingElements()) {
            Object res = null;
            if (res == null) {
                gotNull = true;
            } else if (!Utils.isTrue(res, false)) {
                violatedConstraintElement = constraint;
                if (constrainedObject instanceof Element) {
                    violatedConstrainedElement = (Element)constrainedObject;
                }
                return false;
            }
        }
        return gotNull ? null : true;
    }

    @Override
    public Element getViolatedConstraintElement() {
        if (violatedConstraintElement == null) {
            evaluate();
        }
        return violatedConstraintElement;
    }

    public Element getViolatedConstrainedElement() {
        if (violatedConstrainedElement == null) {
            evaluate();
        }
        return violatedConstrainedElement;
    }

    public static String getExpression(Object constraint) {
        if (constraint instanceof Constraint)
            return ((Constraint)constraint).getExpression();
        String expr = null;
        if (constraint instanceof Element) {
            Element e = (Element)constraint;
            if (elementIsUmlConstraint(e)) {
                expr = DocGenUtils.fixString(asUmlConstraint(e).getSpecification());
            } else if (GeneratorUtils.hasStereotypeByString(e, DocGen3Profile.constraintStereotype, true)) {
                Object v = GeneratorUtils.getObjectProperty(e, DocGen3Profile.constraintStereotype,
                        "expression", null);
                expr = v.toString();
            }
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
    public static BasicConstraint makeConstraintFromAlternativeContexts(Object constraintElement,
            Object... candidateContexts) {
        BasicConstraint c = null;
        if (!Utils2.isNullOrEmpty(candidateContexts)) {
            BasicConstraint firstNull = null;
            Boolean result = null;
            for (Object constrained: candidateContexts) {
                c = new BasicConstraint(constraintElement, constrained);
                result = c.evaluate(false);
                if (result != null) {
                    break;
                } else if (firstNull == null
                        || (Utils2.isNullOrEmpty(firstNull.getConstrainedObjects()) && !Utils2
                                .isNullOrEmpty(c.getConstrainedObjects()))) {
                    firstNull = c;
                }
            }
            if (result == null)
                c = firstNull;
        }
        if (c == null) {
            Object constrained = Utils2.isNullOrEmpty(candidateContexts) ? null : candidateContexts[0];
            c = new BasicConstraint(constraintElement, constrained);
        }
        return c;
    }

    /**
     * Expression evaluation expects a list of targets. Make sure the targets
     * are in a list and not buried in an extra list.
     * 
     * @param targets
     * @return
     */
    public static Object fixTargets(Object targets) {// , Element vpConstraint )
                                                     // {
        if (targets == null)
            return null;
        // if ( vpConstraint == null ) return targets;

        Object constrained = targets;
        // See if the constraint is supposed to be iteratively applied to each
        // in a list or to the list as a whole.
        // // if ( iterateViewpointConstrraint( vpConstraint ) ) {
        // If iterating, be sure that list isn't buried in another list.
        if (constrained instanceof Collection) {
            Collection<?> coll = (Collection<?>)constrained;
            if (!coll.isEmpty()) {
                Object first = coll.iterator().next();
                if (first instanceof Collection && coll.size() == 1) {
                    constrained = first;
                }
            }
        }
        // // } else {
        // Expecting targets to be processed as a single list
        if (constrained instanceof Element) {
            constrained = Utils2.newList(constrained);
        }
        // if ( constrained instanceof Collection ) {
        // Collection< ? > coll = (Collection<?>)constrained;
        // if ( !coll.isEmpty() ) {
        // Object first = coll.iterator().next();
        // if ( first instanceof Element ) {
        // constrained = Utils2.newList(constrained);
        // }
        // }
        // }
        // // }
        return constrained;
    }

    public static BasicConstraint makeConstraint(Element constraintElement) {
        if (!elementIsConstraint(constraintElement))
            return null;
        List<Object> constrained = getConstrainedObjectsFromConstraintElement(constraintElement);
        BasicConstraint c = new BasicConstraint(constraintElement, constrained);
        return c;
    }

    public static Boolean evaluateAgainst(Object constraint, Object constrained, List<Object> targets) {
        BasicConstraint c = makeConstraintFromAlternativeContexts(constraint, targets, constrained);
        Boolean result = c.evaluate();
        return result;
    }

    @Override
    public String toString() {
        return toShortString();
    }

    public String toShortString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Constraint:\"" + this.getExpression() + "\" on "
                + Utils.toStringNameAndType(this.constrainedObjects, true, true));
        return sb.toString();
    }

    protected static String toString(Object o, boolean showElementId) {
        if (o instanceof Element)
            return toString((Element)o, showElementId);
        return MoreToString.Helper.toString(o);
    }

    protected static String toString(Element e, boolean showElementId) {
        return Utils.getName(e) + (showElementId ? "[" + e.getID() + "]" : "");
    }

    protected static String toString(Collection<? extends Object> coll, boolean showElementId) {
        return toString(coll, Integer.MAX_VALUE, showElementId);
    }

    protected static String toString(Collection<? extends Object> coll, int maxNumber, boolean showElementId) {
        if (maxNumber <= 0 || Utils2.isNullOrEmpty(coll))
            return "";
        if (coll.size() == 1)
            return toString(coll.iterator().next(), showElementId);
        StringBuffer sb = new StringBuffer();
        sb.append("( ");
        int ct = 0;
        for (Object o: coll) {
            String oStr = toString(o, showElementId);
            if (Utils2.isNullOrEmpty(oStr))
                continue;
            if (ct > 0)
                sb.append(", ");
            sb.append(oStr);
            ct++;
            if (ct >= maxNumber)
                break;
        }
        if (ct < coll.size()) {
            if (ct > 0)
                sb.append(", ");
            sb.append("and " + (coll.size() - ct) + " more");
        }
        sb.append(" )");
        return sb.toString();
    }

    public String toString(int maxNumber, boolean showElementIds) {
        StringBuffer sb = new StringBuffer();
        Element constrainingElement = (Utils2.isNullOrEmpty(getConstrainingElements()) ? null
                : getConstrainingElements().iterator().next());
        sb.append("Constraint " + toString(constrainingElement, showElementIds) + " with expression, \""
                + this.getExpression() + "\" on "
                + toString(this.constrainedObjects, maxNumber, showElementIds));
        return sb.toString();
    }

    public String toStringViolated(int maxNumberOfViolatingElementsToShow, boolean showElementIds) {
        Element violatedElement = this.getViolatedConstraintElement();
        Set<Object> target = this.getConstrainedObjects();
        StringBuffer comment = new StringBuffer();
        comment.append("constraint " + toString(violatedElement, showElementIds));
        comment.append(" with expression, \"" + getExpression() + "\"");
        comment.append(" is violated");
        if (maxNumberOfViolatingElementsToShow > 0 && !Utils2.isNullOrEmpty(target)) {
            comment.append(" for " + toString(target, maxNumberOfViolatingElementsToShow, showElementIds));
        }
        return comment.toString();
    }

    public static List<Element> getComments(Element source) {
        List<Element> results = new ArrayList<Element>();
        results.addAll(source.get_commentOfAnnotatedElement());
        if (results.size() > 0) {
            Debug.out("");
        }
        return results;
    }

    /**
     * Get the elements that constrain this element.
     * 
     * @param constrainedObject
     * @return a list of constraint elements that constrain the
     *         constrainedObject
     */
    public static List<Element> getConstraintElements(Object constrainedObject) {
        return getConstraintElements(constrainedObject, Type.ANY);
    }

    public static List<Element> getConstraintElements(Object constrainedObject, Type type) {

        LinkedHashSet<Element> constraintElements = new LinkedHashSet<Element>();
        if (constrainedObject instanceof Element) {
            Element constrainedElement = ((Element)constrainedObject);

            // Is the element stereotyped as <<Constraint>> or a UML Constraint?
            if (elementIsConstraintOnItself(constrainedElement)) {
                if (constraintIsType(constrainedElement, type)) {
                    constraintElements.add(constrainedElement);
                }
            }

            // Get the constraints MD finds
            Collection<com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint> constrs = constrainedElement
                    .get_constraintOfConstrainedElement();
            if (constrs != null) {
                for (Element c: constrs) {
                    if (constraintIsType(c, type)) {
                        constraintElements.add(c);
                    }
                }
            }

            // Add any comment constraints annotating this element
            for (Element comment: BasicConstraint.getComments(constrainedElement)) {
                if (elementIsDocGenConstraint(comment)) {
                    if (constraintIsType(comment, type)) {
                        constraintElements.add(comment);
                    }
                }
            }
        }
        // Collect constraints from each item in the object as a Collection
        // TODO -- infinite loop if element contains itself! Use Utils2.seen()
        if (constrainedObject instanceof Collection) {
            for (Object o: (Collection<?>)constrainedObject) {
                constraintElements.addAll(getConstraintElements(o, type));
            }
        }
        return Utils2.toList(constraintElements);
    }

    /**
     * Get the elements that the input element constrains, if a constraint.
     * 
     * @param elem
     * @return
     */
    public static List<Object> getConstrainedObjectsFromConstraintElement(Element elem) {
        Set<Object> constrained = new LinkedHashSet<Object>();
        if (elementIsDocGenConstraint(elem)) {
            if (elem instanceof Comment) {
                Collection<Element> annotatedElems = ((Comment)elem).getAnnotatedElement();
                if (Utils2.isNullOrEmpty(annotatedElems)) {
                    constrained.add(elem);
                } else {
                    constrained.addAll(annotatedElems);
                }
            }
        }
        if (elementIsUmlConstraint(elem)) {
            constrained.addAll(asUmlConstraint(elem).getConstrainedElement());
        }
        return Utils2.toList(constrained);
    }

    public static com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint asUmlConstraint(Element elem) {
        if (elementIsUmlConstraint(elem))
            return (com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint)elem;
        return null;
    }

    public static boolean elementIsUmlConstraint(Element elem) {
        return elem instanceof com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
    }

    public static boolean elementIsDocGenConstraint(Element elem) {
        if (elem == null)
            return false;
        if (StereotypesHelper.hasStereotypeOrDerived(elem, DocGen3Profile.constraintStereotype)) {
            return true;
        }
        return false;
    }

    public static boolean elementIsViewpointConstraint(Element elem) {
        if (elem == null)
            return false;
        if (StereotypesHelper.hasStereotypeOrDerived(elem, DocGen3Profile.viewpointConstraintStereotype)) {
            return true;
        }
        return false;
    }

    public static boolean elementIsConstraint(Element elem) {
        return (elementIsUmlConstraint(elem) || elementIsDocGenConstraint(elem) || elementIsViewpointConstraint(elem));
    }

    /**
     * Determines whether the element is a constraint on itself
     * 
     * @param elem
     * @return true iff it is not a constraint or constrains only other elements
     */
    public static boolean elementIsConstraintOnItself(Element elem) {
        // If a <<Constraint>>, then it constrains self if it is not a Comment,
        // or it is included in the elements it annotates (as a Comment).
        if (elementIsDocGenConstraint(elem)) {
            if (!(elem instanceof Comment))
                return true;
            Comment comment = (Comment)elem;
            return !Utils2.isNullOrEmpty(comment.getAnnotatedElement())
                    && comment.getAnnotatedElement().contains(elem);
        }
        if (elementIsUmlConstraint(elem)) {
            com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint umlConstr = (com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint)elem;
            List<Element> constrained = umlConstr.getConstrainedElement();
            return constrained.contains(elem);
        }
        if (elementIsViewpointConstraint(elem)) {
            return true;
        }
        return false;
    }

    /**
     * @param constrainedObject
     *            the object for which constraints are sought
     * 
     * @return constraints on the constrainedObject, only including the
     *         constrainedObject if it is a constraint on itself
     */
    public static List<Constraint> getConstraints(Object constrainedObject) {
        return getConstraints(constrainedObject, Type.ANY);
    }

    public static List<Constraint> getConstraints(Object constrainedObject, Type type) {
        List<Constraint> constraints = new ArrayList<Constraint>();
        List<Element> constraintElements = getConstraintElements(constrainedObject, type);
        for (Element constraint: constraintElements) {
            Constraint c = BasicConstraint.makeConstraint(constraint);// ,
                                                                      // constrainedObject
                                                                      // );
            if (c != null)
                constraints.add(c);
        }
        return constraints;
    }

    @Override
    public boolean isConsistent() {
        if (isConsistent == null)
            evaluate();
        return isConsistent;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean isReported() {
        return false;
    }

    public void setReported(boolean b) {
        reported = b;
    }

}
