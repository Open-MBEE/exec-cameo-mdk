package org.openmbee.mdk.docgen.view.impl;

import org.openmbee.mdk.docgen.view.DocGenViewPackage;
import org.openmbee.mdk.docgen.view.TableEntry;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc --> An implementation of the model object '
 * <em><b>Table Entry</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>
 * {@link TableEntryImpl#getMorerows
 * <em>Morerows</em>}</li>
 * <li>
 * {@link TableEntryImpl#getNamest
 * <em>Namest</em>}</li>
 * <li>
 * {@link TableEntryImpl#getNameend
 * <em>Nameend</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class TableEntryImpl extends HasContentImpl implements TableEntry {
    /**
     * The default value of the '{@link #getMorerows() <em>Morerows</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getMorerows()
     */
    protected static final int MOREROWS_EDEFAULT = 0;

    /**
     * The cached value of the '{@link #getMorerows() <em>Morerows</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getMorerows()
     */
    protected int morerows = MOREROWS_EDEFAULT;

    /**
     * The default value of the '{@link #getNamest() <em>Namest</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getNamest()
     */
    protected static final String NAMEST_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getNamest() <em>Namest</em>}' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getNamest()
     */
    protected String namest = NAMEST_EDEFAULT;

    /**
     * The default value of the '{@link #getNameend() <em>Nameend</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getNameend()
     */
    protected static final String NAMEEND_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getNameend() <em>Nameend</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getNameend()
     */
    protected String nameend = NAMEEND_EDEFAULT;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    protected TableEntryImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return DocGenViewPackage.Literals.TABLE_ENTRY;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public int getMorerows() {
        return morerows;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setMorerows(int newMorerows) {
        int oldMorerows = morerows;
        morerows = newMorerows;
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, DocGenViewPackage.TABLE_ENTRY__MOREROWS,
                    oldMorerows, morerows));
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getNamest() {
        return namest;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setNamest(String newNamest) {
        String oldNamest = namest;
        namest = newNamest;
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, DocGenViewPackage.TABLE_ENTRY__NAMEST,
                    oldNamest, namest));
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getNameend() {
        return nameend;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setNameend(String newNameend) {
        String oldNameend = nameend;
        nameend = newNameend;
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, DocGenViewPackage.TABLE_ENTRY__NAMEEND,
                    oldNameend, nameend));
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
            case DocGenViewPackage.TABLE_ENTRY__MOREROWS:
                return getMorerows();
            case DocGenViewPackage.TABLE_ENTRY__NAMEST:
                return getNamest();
            case DocGenViewPackage.TABLE_ENTRY__NAMEEND:
                return getNameend();
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
            case DocGenViewPackage.TABLE_ENTRY__MOREROWS:
                setMorerows((Integer) newValue);
                return;
            case DocGenViewPackage.TABLE_ENTRY__NAMEST:
                setNamest((String) newValue);
                return;
            case DocGenViewPackage.TABLE_ENTRY__NAMEEND:
                setNameend((String) newValue);
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
            case DocGenViewPackage.TABLE_ENTRY__MOREROWS:
                setMorerows(MOREROWS_EDEFAULT);
                return;
            case DocGenViewPackage.TABLE_ENTRY__NAMEST:
                setNamest(NAMEST_EDEFAULT);
                return;
            case DocGenViewPackage.TABLE_ENTRY__NAMEEND:
                setNameend(NAMEEND_EDEFAULT);
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
            case DocGenViewPackage.TABLE_ENTRY__MOREROWS:
                return morerows != MOREROWS_EDEFAULT;
            case DocGenViewPackage.TABLE_ENTRY__NAMEST:
                return NAMEST_EDEFAULT == null ? namest != null : !NAMEST_EDEFAULT.equals(namest);
            case DocGenViewPackage.TABLE_ENTRY__NAMEEND:
                return NAMEEND_EDEFAULT == null ? nameend != null : !NAMEEND_EDEFAULT.equals(nameend);
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
        result.append(" (morerows: ");
        result.append(morerows);
        result.append(", namest: ");
        result.append(namest);
        result.append(", nameend: ");
        result.append(nameend);
        result.append(')');
        return result.toString();
    }

} // TableEntryImpl
