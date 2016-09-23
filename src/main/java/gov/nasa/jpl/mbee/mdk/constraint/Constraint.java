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
package gov.nasa.jpl.mbee.mdk.constraint;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import java.util.Collection;
import java.util.Set;

/**
 * A constraint in the context of a model.
 */
public interface Constraint {
    // // public abstract class Constraint {
    // // public Constraint( Element constrainingElement, Element
    // constrainedElement ) {
    // // addConstrainedElement( constrainedElement );
    // // addConstrainingElement( constrainingElement );
    // // }
    // public Set< Element > getConstrainedElements();

    /**
     * @return the constrained elements and any other other constrained objects.
     */
    Set<Object> getConstrainedObjects();

    Set<Element> getConstrainingElements();

    // public void addConstrainedElements( Collection< Element > elements );
    // public void addConstrainedElement( Element element );
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

    // public void setExpression();

    Boolean evaluate();

    /**
     * @return whether the constraint is properly specified and not
     * self-contradictory
     */
    boolean isConsistent();
    // public Boolean isGrounded();
    // public static class Helper {
    // public static Constraint makeConstraint( Element constraint, Object
    // constrainedObject ) {
    //
    // }
    // public static void evaluateAgainst( Element constraint, Object
    // constrainedObject ) {
    //
    // }
    // }
}
