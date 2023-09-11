package org.openmbee.mdk.constraint;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import java.util.Collection;
import java.util.Set;

/**
 * A constraint in the context of a model.
 */
public interface Constraint {

    /**
     * @return the constrained elements and any other other constrained objects.
     */
    Set<Object> getConstrainedObjects();

    Set<Element> getConstrainingElements();

    void addConstrainedObjects(Collection<Object> objects);

    void addConstrainedObject(Object object);

    void addConstrainingElements(Collection<Element> elements);

    void addConstrainingElement(Element element);

    Element getViolatedConstraintElement();

    boolean isReported();

    /**
     * @return a text expression of the constraint
     */
    String getExpression();

    Boolean evaluate();

    /**
     * @return whether the constraint is properly specified and not
     * self-contradictory
     */
    boolean isConsistent();
}
