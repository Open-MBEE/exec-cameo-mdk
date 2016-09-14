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
package gov.nasa.jpl.mbee.dgview.impl;

import gov.nasa.jpl.mbee.dgview.DgviewPackage;
import gov.nasa.jpl.mbee.dgview.FromProperty;
import gov.nasa.jpl.mbee.dgview.ViewElement;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

/**
 * <!-- begin-user-doc --> An implementation of the model object '
 * <em><b>View Element</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link gov.nasa.jpl.mbee.dgview.impl.ViewElementImpl#getId
 * <em>Id</em>}</li>
 * <li>
 * {@link gov.nasa.jpl.mbee.dgview.impl.ViewElementImpl#getTitle
 * <em>Title</em>}</li>
 * <li>
 * {@link gov.nasa.jpl.mbee.dgview.impl.ViewElementImpl#getFromElementId
 * <em>From Element Id</em>}</li>
 * <li>
 * {@link gov.nasa.jpl.mbee.dgview.impl.ViewElementImpl#getFromProperty
 * <em>From Property</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public abstract class ViewElementImpl extends EObjectImpl implements ViewElement {
    /**
     * The default value of the '{@link #getId() <em>Id</em>}' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getId()
     */
    protected static final String ID_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getId() <em>Id</em>}' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getId()
     */
    protected String id = ID_EDEFAULT;

    /**
     * The default value of the '{@link #getTitle() <em>Title</em>}' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getTitle()
     */
    protected static final String TITLE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getTitle() <em>Title</em>}' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getTitle()
     */
    protected String title = TITLE_EDEFAULT;

    /**
     * The default value of the '{@link #getFromElementId()
     * <em>From Element Id</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getFromElementId()
     */
    protected static final String FROM_ELEMENT_ID_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getFromElementId()
     * <em>From Element Id</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getFromElementId()
     */
    protected String fromElementId = FROM_ELEMENT_ID_EDEFAULT;

    /**
     * The default value of the '{@link #getFromProperty()
     * <em>From Property</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getFromProperty()
     */
    protected static final FromProperty FROM_PROPERTY_EDEFAULT = FromProperty.NAME;

    /**
     * The cached value of the '{@link #getFromProperty()
     * <em>From Property</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getFromProperty()
     */
    protected FromProperty fromProperty = FROM_PROPERTY_EDEFAULT;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    protected ViewElementImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return DgviewPackage.Literals.VIEW_ELEMENT;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setId(String newId) {
        String oldId = id;
        id = newId;
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, DgviewPackage.VIEW_ELEMENT__ID, oldId, id));
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setTitle(String newTitle) {
        String oldTitle = title;
        title = newTitle;
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, DgviewPackage.VIEW_ELEMENT__TITLE,
                    oldTitle, title));
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getFromElementId() {
        return fromElementId;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setFromElementId(String newFromElementId) {
        String oldFromElementId = fromElementId;
        fromElementId = newFromElementId;
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET,
                    DgviewPackage.VIEW_ELEMENT__FROM_ELEMENT_ID, oldFromElementId, fromElementId));
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public FromProperty getFromProperty() {
        return fromProperty;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setFromProperty(FromProperty newFromProperty) {
        FromProperty oldFromProperty = fromProperty;
        fromProperty = newFromProperty == null ? FROM_PROPERTY_EDEFAULT : newFromProperty;
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, DgviewPackage.VIEW_ELEMENT__FROM_PROPERTY,
                    oldFromProperty, fromProperty));
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case DgviewPackage.VIEW_ELEMENT__ID:
                return getId();
            case DgviewPackage.VIEW_ELEMENT__TITLE:
                return getTitle();
            case DgviewPackage.VIEW_ELEMENT__FROM_ELEMENT_ID:
                return getFromElementId();
            case DgviewPackage.VIEW_ELEMENT__FROM_PROPERTY:
                return getFromProperty();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case DgviewPackage.VIEW_ELEMENT__ID:
                setId((String) newValue);
                return;
            case DgviewPackage.VIEW_ELEMENT__TITLE:
                setTitle((String) newValue);
                return;
            case DgviewPackage.VIEW_ELEMENT__FROM_ELEMENT_ID:
                setFromElementId((String) newValue);
                return;
            case DgviewPackage.VIEW_ELEMENT__FROM_PROPERTY:
                setFromProperty((FromProperty) newValue);
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
            case DgviewPackage.VIEW_ELEMENT__ID:
                setId(ID_EDEFAULT);
                return;
            case DgviewPackage.VIEW_ELEMENT__TITLE:
                setTitle(TITLE_EDEFAULT);
                return;
            case DgviewPackage.VIEW_ELEMENT__FROM_ELEMENT_ID:
                setFromElementId(FROM_ELEMENT_ID_EDEFAULT);
                return;
            case DgviewPackage.VIEW_ELEMENT__FROM_PROPERTY:
                setFromProperty(FROM_PROPERTY_EDEFAULT);
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
            case DgviewPackage.VIEW_ELEMENT__ID:
                return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
            case DgviewPackage.VIEW_ELEMENT__TITLE:
                return TITLE_EDEFAULT == null ? title != null : !TITLE_EDEFAULT.equals(title);
            case DgviewPackage.VIEW_ELEMENT__FROM_ELEMENT_ID:
                return FROM_ELEMENT_ID_EDEFAULT == null ? fromElementId != null : !FROM_ELEMENT_ID_EDEFAULT
                        .equals(fromElementId);
            case DgviewPackage.VIEW_ELEMENT__FROM_PROPERTY:
                return fromProperty != FROM_PROPERTY_EDEFAULT;
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
        result.append(" (id: ");
        result.append(id);
        result.append(", title: ");
        result.append(title);
        result.append(", fromElementId: ");
        result.append(fromElementId);
        result.append(", fromProperty: ");
        result.append(fromProperty);
        result.append(')');
        return result.toString();
    }

} // ViewElementImpl
