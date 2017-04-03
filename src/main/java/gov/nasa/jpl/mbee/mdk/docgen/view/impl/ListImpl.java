package gov.nasa.jpl.mbee.mdk.docgen.view.impl;

import gov.nasa.jpl.mbee.mdk.docgen.view.DocGenViewPackage;
import gov.nasa.jpl.mbee.mdk.docgen.view.List;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc --> An implementation of the model object '
 * <em><b>List</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link gov.nasa.jpl.mbee.mdk.docgen.view.impl.ListImpl#isOrdered <em>
 * Ordered</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ListImpl extends HasContentImpl implements List {
    /**
     * The default value of the '{@link #isOrdered() <em>Ordered</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #isOrdered()
     */
    protected static final boolean ORDERED_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isOrdered() <em>Ordered</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #isOrdered()
     */
    protected boolean ordered = ORDERED_EDEFAULT;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    protected ListImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return DocGenViewPackage.Literals.LIST;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public boolean isOrdered() {
        return ordered;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setOrdered(boolean newOrdered) {
        boolean oldOrdered = ordered;
        ordered = newOrdered;
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, DocGenViewPackage.LIST__ORDERED, oldOrdered,
                    ordered));
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
            case DocGenViewPackage.LIST__ORDERED:
                return isOrdered();
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
            case DocGenViewPackage.LIST__ORDERED:
                setOrdered((Boolean) newValue);
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
            case DocGenViewPackage.LIST__ORDERED:
                setOrdered(ORDERED_EDEFAULT);
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
            case DocGenViewPackage.LIST__ORDERED:
                return ordered != ORDERED_EDEFAULT;
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
        result.append(" (ordered: ");
        result.append(ordered);
        result.append(')');
        return result.toString();
    }

} // ListImpl
