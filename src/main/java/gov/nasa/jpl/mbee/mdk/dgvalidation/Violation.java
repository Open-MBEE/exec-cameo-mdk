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
package gov.nasa.jpl.mbee.mdk.dgvalidation;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc --> A representation of the model object '
 * <em><b>Violation</b></em>'. <!-- end-user-doc -->
 * <p>
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link gov.nasa.jpl.mbee.mdk.dgvalidation.Violation#getElementId
 * <em>Element Id</em>}</li>
 * <li>{@link gov.nasa.jpl.mbee.mdk.dgvalidation.Violation#getComment
 * <em>Comment</em>}</li>
 * </ul>
 * </p>
 *
 * @model
 * @generated
 * @see gov.nasa.jpl.mbee.mdk.dgvalidation.DgvalidationPackage#getViolation()
 */
public interface Violation extends EObject {
    /**
     * Returns the value of the '<em><b>Element Id</b></em>' attribute. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Element Id</em>' attribute isn't clear, there
     * really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>Element Id</em>' attribute.
     * @model
     * @generated
     * @see #setElementId(String)
     * @see gov.nasa.jpl.mbee.mdk.dgvalidation.DgvalidationPackage#getViolation_ElementId()
     */
    String getElementId();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mbee.mdk.dgvalidation.Violation#getElementId
     * <em>Element Id</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @param value the new value of the '<em>Element Id</em>' attribute.
     * @generated
     * @see #getElementId()
     */
    void setElementId(String value);

    /**
     * Returns the value of the '<em><b>Comment</b></em>' attribute. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Comment</em>' attribute isn't clear, there
     * really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>Comment</em>' attribute.
     * @model
     * @generated
     * @see #setComment(String)
     * @see gov.nasa.jpl.mbee.mdk.dgvalidation.DgvalidationPackage#getViolation_Comment()
     */
    String getComment();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mbee.mdk.dgvalidation.Violation#getComment
     * <em>Comment</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     *
     * @param value the new value of the '<em>Comment</em>' attribute.
     * @generated
     * @see #getComment()
     */
    void setComment(String value);

} // Violation
