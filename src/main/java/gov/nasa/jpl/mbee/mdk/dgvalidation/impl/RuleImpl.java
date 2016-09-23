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
package gov.nasa.jpl.mbee.mdk.dgvalidation.impl;

import gov.nasa.jpl.mbee.mdk.dgvalidation.DgvalidationPackage;
import gov.nasa.jpl.mbee.mdk.dgvalidation.Rule;
import gov.nasa.jpl.mbee.mdk.dgvalidation.Severity;
import gov.nasa.jpl.mbee.mdk.dgvalidation.Violation;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import java.util.Collection;

/**
 * <!-- begin-user-doc --> An implementation of the model object '
 * <em><b>Rule</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link gov.nasa.jpl.mbee.mdk.dgvalidation.impl.RuleImpl#getName
 * <em>Name</em>}</li>
 * <li>
 * {@link gov.nasa.jpl.mbee.mdk.dgvalidation.impl.RuleImpl#getDescription
 * <em>Description</em>}</li>
 * <li>
 * {@link gov.nasa.jpl.mbee.mdk.dgvalidation.impl.RuleImpl#getSeverity
 * <em>Severity</em>}</li>
 * <li>
 * {@link gov.nasa.jpl.mbee.mdk.dgvalidation.impl.RuleImpl#getViolations
 * <em>Violations</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class RuleImpl extends EObjectImpl implements Rule {
    /**
     * The default value of the '{@link #getName() <em>Name</em>}' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getName()
     */
    protected static final String NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getName()
     */
    protected String name = NAME_EDEFAULT;

    /**
     * The default value of the '{@link #getDescription() <em>Description</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getDescription()
     */
    protected static final String DESCRIPTION_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getDescription() <em>Description</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getDescription()
     */
    protected String description = DESCRIPTION_EDEFAULT;

    /**
     * The default value of the '{@link #getSeverity() <em>Severity</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getSeverity()
     */
    protected static final Severity SEVERITY_EDEFAULT = Severity.DEBUG;

    /**
     * The cached value of the '{@link #getSeverity() <em>Severity</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getSeverity()
     */
    protected Severity severity = SEVERITY_EDEFAULT;

    /**
     * The cached value of the '{@link #getViolations() <em>Violations</em>}'
     * containment reference list. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getViolations()
     */
    protected EList<Violation> violations;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    protected RuleImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return DgvalidationPackage.Literals.RULE;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setName(String newName) {
        String oldName = name;
        name = newName;
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, DgvalidationPackage.RULE__NAME, oldName,
                    name));
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setDescription(String newDescription) {
        String oldDescription = description;
        description = newDescription;
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, DgvalidationPackage.RULE__DESCRIPTION,
                    oldDescription, description));
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public Severity getSeverity() {
        return severity;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setSeverity(Severity newSeverity) {
        Severity oldSeverity = severity;
        severity = newSeverity == null ? SEVERITY_EDEFAULT : newSeverity;
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, DgvalidationPackage.RULE__SEVERITY,
                    oldSeverity, severity));
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EList<Violation> getViolations() {
        if (violations == null) {
            violations = new EObjectContainmentEList<Violation>(Violation.class, this,
                    DgvalidationPackage.RULE__VIOLATIONS);
        }
        return violations;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case DgvalidationPackage.RULE__VIOLATIONS:
                return ((InternalEList<?>) getViolations()).basicRemove(otherEnd, msgs);
        }
        return super.eInverseRemove(otherEnd, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case DgvalidationPackage.RULE__NAME:
                return getName();
            case DgvalidationPackage.RULE__DESCRIPTION:
                return getDescription();
            case DgvalidationPackage.RULE__SEVERITY:
                return getSeverity();
            case DgvalidationPackage.RULE__VIOLATIONS:
                return getViolations();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @SuppressWarnings("unchecked")
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case DgvalidationPackage.RULE__NAME:
                setName((String) newValue);
                return;
            case DgvalidationPackage.RULE__DESCRIPTION:
                setDescription((String) newValue);
                return;
            case DgvalidationPackage.RULE__SEVERITY:
                setSeverity((Severity) newValue);
                return;
            case DgvalidationPackage.RULE__VIOLATIONS:
                getViolations().clear();
                getViolations().addAll((Collection<? extends Violation>) newValue);
                return;
        }
        super.eSet(featureID, newValue);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void eUnset(int featureID) {
        switch (featureID) {
            case DgvalidationPackage.RULE__NAME:
                setName(NAME_EDEFAULT);
                return;
            case DgvalidationPackage.RULE__DESCRIPTION:
                setDescription(DESCRIPTION_EDEFAULT);
                return;
            case DgvalidationPackage.RULE__SEVERITY:
                setSeverity(SEVERITY_EDEFAULT);
                return;
            case DgvalidationPackage.RULE__VIOLATIONS:
                getViolations().clear();
                return;
        }
        super.eUnset(featureID);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public boolean eIsSet(int featureID) {
        switch (featureID) {
            case DgvalidationPackage.RULE__NAME:
                return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
            case DgvalidationPackage.RULE__DESCRIPTION:
                return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT
                        .equals(description);
            case DgvalidationPackage.RULE__SEVERITY:
                return severity != SEVERITY_EDEFAULT;
            case DgvalidationPackage.RULE__VIOLATIONS:
                return violations != null && !violations.isEmpty();
        }
        return super.eIsSet(featureID);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String toString() {
        if (eIsProxy()) {
            return super.toString();
        }

        StringBuffer result = new StringBuffer(super.toString());
        result.append(" (name: ");
        result.append(name);
        result.append(", description: ");
        result.append(description);
        result.append(", severity: ");
        result.append(severity);
        result.append(')');
        return result.toString();
    }

} // RuleImpl
