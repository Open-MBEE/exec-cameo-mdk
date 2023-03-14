package org.openmbee.mdk.docgen.validation.impl;

import org.openmbee.mdk.docgen.validation.DocGenValidationPackage;
import org.openmbee.mdk.docgen.validation.Violation;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

/**
 * <!-- begin-user-doc --> An implementation of the model object '
 * <em><b>Violation</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>
 * {@link ViolationImpl#getElementId
 * <em>Element Id</em>}</li>
 * <li>
 * {@link ViolationImpl#getComment
 * <em>Comment</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ViolationImpl extends EObjectImpl implements Violation {
    /**
     * The default value of the '{@link #getElementId() <em>Element Id</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getElementId()
     */
    protected static final String ELEMENT_ID_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getElementId() <em>Element Id</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getElementId()
     */
    protected String elementId = ELEMENT_ID_EDEFAULT;

    /**
     * The default value of the '{@link #getComment() <em>Comment</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getComment()
     */
    protected static final String COMMENT_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getComment() <em>Comment</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getComment()
     */
    protected String comment = COMMENT_EDEFAULT;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    protected ViolationImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return DocGenValidationPackage.Literals.VIOLATION;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getElementId() {
        return elementId;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setElementId(String newElementId) {
        String oldElementId = elementId;
        elementId = newElementId;
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, DocGenValidationPackage.VIOLATION__ELEMENT_ID,
                    oldElementId, elementId));
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getComment() {
        return comment;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setComment(String newComment) {
        String oldComment = comment;
        comment = newComment;
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, DocGenValidationPackage.VIOLATION__COMMENT,
                    oldComment, comment));
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
            case DocGenValidationPackage.VIOLATION__ELEMENT_ID:
                return getElementId();
            case DocGenValidationPackage.VIOLATION__COMMENT:
                return getComment();
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
            case DocGenValidationPackage.VIOLATION__ELEMENT_ID:
                setElementId((String) newValue);
                return;
            case DocGenValidationPackage.VIOLATION__COMMENT:
                setComment((String) newValue);
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
            case DocGenValidationPackage.VIOLATION__ELEMENT_ID:
                setElementId(ELEMENT_ID_EDEFAULT);
                return;
            case DocGenValidationPackage.VIOLATION__COMMENT:
                setComment(COMMENT_EDEFAULT);
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
            case DocGenValidationPackage.VIOLATION__ELEMENT_ID:
                return ELEMENT_ID_EDEFAULT == null ? elementId != null : !ELEMENT_ID_EDEFAULT
                        .equals(elementId);
            case DocGenValidationPackage.VIOLATION__COMMENT:
                return COMMENT_EDEFAULT == null ? comment != null : !COMMENT_EDEFAULT.equals(comment);
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
        result.append(" (elementId: ");
        result.append(elementId);
        result.append(", comment: ");
        result.append(comment);
        result.append(')');
        return result.toString();
    }

} // ViolationImpl
