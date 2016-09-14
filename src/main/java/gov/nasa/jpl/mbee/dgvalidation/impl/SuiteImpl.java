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
package gov.nasa.jpl.mbee.dgvalidation.impl;

import gov.nasa.jpl.mbee.dgvalidation.DgvalidationPackage;
import gov.nasa.jpl.mbee.dgvalidation.Rule;
import gov.nasa.jpl.mbee.dgvalidation.Suite;
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
 * <em><b>Suite</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>
 * {@link gov.nasa.jpl.mbee.dgvalidation.impl.SuiteImpl#isShowDetail
 * <em>Show Detail</em>}</li>
 * <li>
 * {@link gov.nasa.jpl.mbee.dgvalidation.impl.SuiteImpl#isShowSummary
 * <em>Show Summary</em>}</li>
 * <li>
 * {@link gov.nasa.jpl.mbee.dgvalidation.impl.SuiteImpl#isOwnSection
 * <em>Own Section</em>}</li>
 * <li>{@link gov.nasa.jpl.mbee.dgvalidation.impl.SuiteImpl#getName
 * <em>Name</em>}</li>
 * <li>
 * {@link gov.nasa.jpl.mbee.dgvalidation.impl.SuiteImpl#getRules
 * <em>Rules</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class SuiteImpl extends EObjectImpl implements Suite {
    /**
     * The default value of the '{@link #isShowDetail() <em>Show Detail</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #isShowDetail()
     */
    protected static final boolean SHOW_DETAIL_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isShowDetail() <em>Show Detail</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #isShowDetail()
     */
    protected boolean showDetail = SHOW_DETAIL_EDEFAULT;

    /**
     * The default value of the '{@link #isShowSummary() <em>Show Summary</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #isShowSummary()
     */
    protected static final boolean SHOW_SUMMARY_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isShowSummary() <em>Show Summary</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #isShowSummary()
     */
    protected boolean showSummary = SHOW_SUMMARY_EDEFAULT;

    /**
     * The default value of the '{@link #isOwnSection() <em>Own Section</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #isOwnSection()
     */
    protected static final boolean OWN_SECTION_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isOwnSection() <em>Own Section</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #isOwnSection()
     */
    protected boolean ownSection = OWN_SECTION_EDEFAULT;

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
     * The cached value of the '{@link #getRules() <em>Rules</em>}' containment
     * reference list. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getRules()
     */
    protected EList<Rule> rules;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    protected SuiteImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return DgvalidationPackage.Literals.SUITE;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public boolean isShowDetail() {
        return showDetail;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setShowDetail(boolean newShowDetail) {
        boolean oldShowDetail = showDetail;
        showDetail = newShowDetail;
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, DgvalidationPackage.SUITE__SHOW_DETAIL,
                    oldShowDetail, showDetail));
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public boolean isShowSummary() {
        return showSummary;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setShowSummary(boolean newShowSummary) {
        boolean oldShowSummary = showSummary;
        showSummary = newShowSummary;
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, DgvalidationPackage.SUITE__SHOW_SUMMARY,
                    oldShowSummary, showSummary));
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public boolean isOwnSection() {
        return ownSection;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setOwnSection(boolean newOwnSection) {
        boolean oldOwnSection = ownSection;
        ownSection = newOwnSection;
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, DgvalidationPackage.SUITE__OWN_SECTION,
                    oldOwnSection, ownSection));
        }
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
            eNotify(new ENotificationImpl(this, Notification.SET, DgvalidationPackage.SUITE__NAME, oldName,
                    name));
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EList<Rule> getRules() {
        if (rules == null) {
            rules = new EObjectContainmentEList<Rule>(Rule.class, this, DgvalidationPackage.SUITE__RULES);
        }
        return rules;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case DgvalidationPackage.SUITE__RULES:
                return ((InternalEList<?>) getRules()).basicRemove(otherEnd, msgs);
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
            case DgvalidationPackage.SUITE__SHOW_DETAIL:
                return isShowDetail();
            case DgvalidationPackage.SUITE__SHOW_SUMMARY:
                return isShowSummary();
            case DgvalidationPackage.SUITE__OWN_SECTION:
                return isOwnSection();
            case DgvalidationPackage.SUITE__NAME:
                return getName();
            case DgvalidationPackage.SUITE__RULES:
                return getRules();
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
            case DgvalidationPackage.SUITE__SHOW_DETAIL:
                setShowDetail((Boolean) newValue);
                return;
            case DgvalidationPackage.SUITE__SHOW_SUMMARY:
                setShowSummary((Boolean) newValue);
                return;
            case DgvalidationPackage.SUITE__OWN_SECTION:
                setOwnSection((Boolean) newValue);
                return;
            case DgvalidationPackage.SUITE__NAME:
                setName((String) newValue);
                return;
            case DgvalidationPackage.SUITE__RULES:
                getRules().clear();
                getRules().addAll((Collection<? extends Rule>) newValue);
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
            case DgvalidationPackage.SUITE__SHOW_DETAIL:
                setShowDetail(SHOW_DETAIL_EDEFAULT);
                return;
            case DgvalidationPackage.SUITE__SHOW_SUMMARY:
                setShowSummary(SHOW_SUMMARY_EDEFAULT);
                return;
            case DgvalidationPackage.SUITE__OWN_SECTION:
                setOwnSection(OWN_SECTION_EDEFAULT);
                return;
            case DgvalidationPackage.SUITE__NAME:
                setName(NAME_EDEFAULT);
                return;
            case DgvalidationPackage.SUITE__RULES:
                getRules().clear();
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
            case DgvalidationPackage.SUITE__SHOW_DETAIL:
                return showDetail != SHOW_DETAIL_EDEFAULT;
            case DgvalidationPackage.SUITE__SHOW_SUMMARY:
                return showSummary != SHOW_SUMMARY_EDEFAULT;
            case DgvalidationPackage.SUITE__OWN_SECTION:
                return ownSection != OWN_SECTION_EDEFAULT;
            case DgvalidationPackage.SUITE__NAME:
                return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
            case DgvalidationPackage.SUITE__RULES:
                return rules != null && !rules.isEmpty();
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
        result.append(" (showDetail: ");
        result.append(showDetail);
        result.append(", showSummary: ");
        result.append(showSummary);
        result.append(", ownSection: ");
        result.append(ownSection);
        result.append(", name: ");
        result.append(name);
        result.append(')');
        return result.toString();
    }

} // SuiteImpl
