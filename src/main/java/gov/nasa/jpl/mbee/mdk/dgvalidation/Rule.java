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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc --> A representation of the model object '
 * <em><b>Rule</b></em>'. <!-- end-user-doc -->
 * <p>
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link gov.nasa.jpl.mbee.mdk.dgvalidation.Rule#getName <em>Name
 * </em>}</li>
 * <li>{@link gov.nasa.jpl.mbee.mdk.dgvalidation.Rule#getDescription
 * <em>Description</em>}</li>
 * <li>{@link gov.nasa.jpl.mbee.mdk.dgvalidation.Rule#getSeverity <em>
 * Severity</em>}</li>
 * <li>{@link gov.nasa.jpl.mbee.mdk.dgvalidation.Rule#getViolations <em>
 * Violations</em>}</li>
 * </ul>
 * </p>
 *
 * @model
 * @generated
 * @see gov.nasa.jpl.mbee.mdk.dgvalidation.DgvalidationPackage#getRule()
 */
public interface Rule extends EObject {
    /**
     * Returns the value of the '<em><b>Name</b></em>' attribute. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Name</em>' attribute isn't clear, there really
     * should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>Name</em>' attribute.
     * @model
     * @generated
     * @see #setName(String)
     * @see gov.nasa.jpl.mbee.mdk.dgvalidation.DgvalidationPackage#getRule_Name()
     */
    String getName();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mbee.mdk.dgvalidation.Rule#getName
     * <em>Name</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @param value the new value of the '<em>Name</em>' attribute.
     * @generated
     * @see #getName()
     */
    void setName(String value);

    /**
     * Returns the value of the '<em><b>Description</b></em>' attribute. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Description</em>' attribute isn't clear, there
     * really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>Description</em>' attribute.
     * @model
     * @generated
     * @see #setDescription(String)
     * @see gov.nasa.jpl.mbee.mdk.dgvalidation.DgvalidationPackage#getRule_Description()
     */
    String getDescription();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mbee.mdk.dgvalidation.Rule#getDescription
     * <em>Description</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @param value the new value of the '<em>Description</em>' attribute.
     * @generated
     * @see #getDescription()
     */
    void setDescription(String value);

    /**
     * Returns the value of the '<em><b>Severity</b></em>' attribute. The
     * literals are from the enumeration
     * {@link gov.nasa.jpl.mbee.mdk.dgvalidation.Severity}. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Severity</em>' attribute isn't clear, there
     * really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>Severity</em>' attribute.
     * @model
     * @generated
     * @see gov.nasa.jpl.mbee.mdk.dgvalidation.Severity
     * @see #setSeverity(Severity)
     * @see gov.nasa.jpl.mbee.mdk.dgvalidation.DgvalidationPackage#getRule_Severity()
     */
    Severity getSeverity();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mbee.mdk.dgvalidation.Rule#getSeverity
     * <em>Severity</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     *
     * @param value the new value of the '<em>Severity</em>' attribute.
     * @generated
     * @see gov.nasa.jpl.mbee.mdk.dgvalidation.Severity
     * @see #getSeverity()
     */
    void setSeverity(Severity value);

    /**
     * Returns the value of the '<em><b>Violations</b></em>' containment
     * reference list. The list contents are of type
     * {@link gov.nasa.jpl.mbee.mdk.dgvalidation.Violation}. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Violations</em>' containment reference list
     * isn't clear, there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>Violations</em>' containment reference
     * list.
     * @model containment="true"
     * @generated
     * @see gov.nasa.jpl.mbee.mdk.dgvalidation.DgvalidationPackage#getRule_Violations()
     */
    EList<Violation> getViolations();

} // Rule
