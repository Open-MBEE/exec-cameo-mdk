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
package gov.nasa.jpl.mbee.mdk.dgvalidation.util;

import gov.nasa.jpl.mbee.mdk.dgvalidation.DgvalidationPackage;
import gov.nasa.jpl.mbee.mdk.dgvalidation.Rule;
import gov.nasa.jpl.mbee.mdk.dgvalidation.Suite;
import gov.nasa.jpl.mbee.mdk.dgvalidation.Violation;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.Switch;

/**
 * <!-- begin-user-doc --> The <b>Switch</b> for the model's inheritance
 * hierarchy. It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object and proceeding up the
 * inheritance hierarchy until a non-null result is returned, which is the
 * result of the switch. <!-- end-user-doc -->
 *
 * @generated
 * @see gov.nasa.jpl.mbee.mdk.dgvalidation.DgvalidationPackage
 */
public class DgvalidationSwitch<T> extends Switch<T> {
    /**
     * The cached model package <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    protected static DgvalidationPackage modelPackage;

    /**
     * Creates an instance of the switch. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @generated
     */
    public DgvalidationSwitch() {
        if (modelPackage == null) {
            modelPackage = DgvalidationPackage.eINSTANCE;
        }
    }

    /**
     * Checks whether this is a switch for the given package. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @return whether this is a switch for the given package.
     * @parameter ePackage the package in question.
     * @generated
     */
    @Override
    protected boolean isSwitchFor(EPackage ePackage) {
        return ePackage == modelPackage;
    }

    /**
     * Calls <code>caseXXX</code> for each class of the model until one returns
     * a non null result; it yields that result. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @return the first non-null result returned by a <code>caseXXX</code>
     * call.
     * @generated
     */
    @Override
    protected T doSwitch(int classifierID, EObject theEObject) {
        switch (classifierID) {
            case DgvalidationPackage.RULE: {
                Rule rule = (Rule) theEObject;
                T result = caseRule(rule);
                if (result == null) {
                    result = defaultCase(theEObject);
                }
                return result;
            }
            case DgvalidationPackage.VIOLATION: {
                Violation violation = (Violation) theEObject;
                T result = caseViolation(violation);
                if (result == null) {
                    result = defaultCase(theEObject);
                }
                return result;
            }
            case DgvalidationPackage.SUITE: {
                Suite suite = (Suite) theEObject;
                T result = caseSuite(suite);
                if (result == null) {
                    result = defaultCase(theEObject);
                }
                return result;
            }
            default:
                return defaultCase(theEObject);
        }
    }

    /**
     * Returns the result of interpreting the object as an instance of '
     * <em>Rule</em>'. <!-- begin-user-doc --> This implementation returns null;
     * returning a non-null result will terminate the switch. <!-- end-user-doc
     * -->
     *
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '
     * <em>Rule</em>'.
     * @generated
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     */
    public T caseRule(Rule object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '
     * <em>Violation</em>'. <!-- begin-user-doc --> This implementation returns
     * null; returning a non-null result will terminate the switch. <!--
     * end-user-doc -->
     *
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '
     * <em>Violation</em>'.
     * @generated
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     */
    public T caseViolation(Violation object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '
     * <em>Suite</em>'. <!-- begin-user-doc --> This implementation returns
     * null; returning a non-null result will terminate the switch. <!--
     * end-user-doc -->
     *
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '
     * <em>Suite</em>'.
     * @generated
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     */
    public T caseSuite(Suite object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '
     * <em>EObject</em>'. <!-- begin-user-doc --> This implementation returns
     * null; returning a non-null result will terminate the switch, but this is
     * the last case anyway. <!-- end-user-doc -->
     *
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '
     * <em>EObject</em>'.
     * @generated
     * @see #doSwitch(org.eclipse.emf.ecore.EObject)
     */
    @Override
    public T defaultCase(EObject object) {
        return null;
    }

} // DgvalidationSwitch
