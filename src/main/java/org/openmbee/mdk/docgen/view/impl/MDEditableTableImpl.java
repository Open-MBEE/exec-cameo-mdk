package org.openmbee.mdk.docgen.view.impl;

import org.openmbee.mdk.docgen.view.DocGenViewPackage;
import org.openmbee.mdk.docgen.view.MDEditableTable;
import org.openmbee.mdk.docgen.view.TableRow;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import java.util.Collection;

/**
 * <!-- begin-user-doc --> An implementation of the model object '
 * <em><b>MD Editable Table</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>
 * {@link MDEditableTableImpl#getPrecision
 * <em>Precision</em>}</li>
 * <li>
 * {@link MDEditableTableImpl#getGuiHeaders
 * <em>Gui Headers</em>}</li>
 * <li>
 * {@link MDEditableTableImpl#getEditable
 * <em>Editable</em>}</li>
 * <li>
 * {@link MDEditableTableImpl#getMergeCols
 * <em>Merge Cols</em>}</li>
 * <li>
 * {@link MDEditableTableImpl#isAddLineNum
 * <em>Add Line Num</em>}</li>
 * <li>
 * {@link MDEditableTableImpl#getGuiBody
 * <em>Gui Body</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class MDEditableTableImpl extends TableImpl implements MDEditableTable {
    /**
     * The default value of the '{@link #getPrecision() <em>Precision</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getPrecision()
     */
    protected static final int PRECISION_EDEFAULT = 0;

    /**
     * The cached value of the '{@link #getPrecision() <em>Precision</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getPrecision()
     */
    protected int precision = PRECISION_EDEFAULT;

    /**
     * The cached value of the '{@link #getGuiHeaders() <em>Gui Headers</em>}'
     * attribute list. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getGuiHeaders()
     */
    protected EList<String> guiHeaders;

    /**
     * The cached value of the '{@link #getEditable() <em>Editable</em>}'
     * attribute list. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getEditable()
     */
    protected EList<Boolean> editable;

    /**
     * The cached value of the '{@link #getMergeCols() <em>Merge Cols</em>}'
     * attribute list. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getMergeCols()
     */
    protected EList<Integer> mergeCols;

    /**
     * The default value of the '{@link #isAddLineNum() <em>Add Line Num</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #isAddLineNum()
     */
    protected static final boolean ADD_LINE_NUM_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isAddLineNum() <em>Add Line Num</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #isAddLineNum()
     */
    protected boolean addLineNum = ADD_LINE_NUM_EDEFAULT;

    /**
     * The cached value of the '{@link #getGuiBody() <em>Gui Body</em>}'
     * containment reference list. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getGuiBody()
     */
    protected EList<TableRow> guiBody;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    protected MDEditableTableImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return DocGenViewPackage.Literals.MD_EDITABLE_TABLE;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public int getPrecision() {
        return precision;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setPrecision(int newPrecision) {
        int oldPrecision = precision;
        precision = newPrecision;
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, DocGenViewPackage.MD_EDITABLE_TABLE__PRECISION,
                    oldPrecision, precision));
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EList<String> getGuiHeaders() {
        if (guiHeaders == null) {
            guiHeaders = new EDataTypeUniqueEList<String>(String.class, this,
                    DocGenViewPackage.MD_EDITABLE_TABLE__GUI_HEADERS);
        }
        return guiHeaders;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EList<Boolean> getEditable() {
        if (editable == null) {
            editable = new EDataTypeUniqueEList<Boolean>(Boolean.class, this,
                    DocGenViewPackage.MD_EDITABLE_TABLE__EDITABLE);
        }
        return editable;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EList<Integer> getMergeCols() {
        if (mergeCols == null) {
            mergeCols = new EDataTypeUniqueEList<Integer>(Integer.class, this,
                    DocGenViewPackage.MD_EDITABLE_TABLE__MERGE_COLS);
        }
        return mergeCols;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public boolean isAddLineNum() {
        return addLineNum;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setAddLineNum(boolean newAddLineNum) {
        boolean oldAddLineNum = addLineNum;
        addLineNum = newAddLineNum;
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET,
                    DocGenViewPackage.MD_EDITABLE_TABLE__ADD_LINE_NUM, oldAddLineNum, addLineNum));
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EList<TableRow> getGuiBody() {
        if (guiBody == null) {
            guiBody = new EObjectContainmentEList<TableRow>(TableRow.class, this,
                    DocGenViewPackage.MD_EDITABLE_TABLE__GUI_BODY);
        }
        return guiBody;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case DocGenViewPackage.MD_EDITABLE_TABLE__GUI_BODY:
                return ((InternalEList<?>) getGuiBody()).basicRemove(otherEnd, msgs);
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
            case DocGenViewPackage.MD_EDITABLE_TABLE__PRECISION:
                return getPrecision();
            case DocGenViewPackage.MD_EDITABLE_TABLE__GUI_HEADERS:
                return getGuiHeaders();
            case DocGenViewPackage.MD_EDITABLE_TABLE__EDITABLE:
                return getEditable();
            case DocGenViewPackage.MD_EDITABLE_TABLE__MERGE_COLS:
                return getMergeCols();
            case DocGenViewPackage.MD_EDITABLE_TABLE__ADD_LINE_NUM:
                return isAddLineNum();
            case DocGenViewPackage.MD_EDITABLE_TABLE__GUI_BODY:
                return getGuiBody();
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
            case DocGenViewPackage.MD_EDITABLE_TABLE__PRECISION:
                setPrecision((Integer) newValue);
                return;
            case DocGenViewPackage.MD_EDITABLE_TABLE__GUI_HEADERS:
                getGuiHeaders().clear();
                getGuiHeaders().addAll((Collection<? extends String>) newValue);
                return;
            case DocGenViewPackage.MD_EDITABLE_TABLE__EDITABLE:
                getEditable().clear();
                getEditable().addAll((Collection<? extends Boolean>) newValue);
                return;
            case DocGenViewPackage.MD_EDITABLE_TABLE__MERGE_COLS:
                getMergeCols().clear();
                getMergeCols().addAll((Collection<? extends Integer>) newValue);
                return;
            case DocGenViewPackage.MD_EDITABLE_TABLE__ADD_LINE_NUM:
                setAddLineNum((Boolean) newValue);
                return;
            case DocGenViewPackage.MD_EDITABLE_TABLE__GUI_BODY:
                getGuiBody().clear();
                getGuiBody().addAll((Collection<? extends TableRow>) newValue);
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
            case DocGenViewPackage.MD_EDITABLE_TABLE__PRECISION:
                setPrecision(PRECISION_EDEFAULT);
                return;
            case DocGenViewPackage.MD_EDITABLE_TABLE__GUI_HEADERS:
                getGuiHeaders().clear();
                return;
            case DocGenViewPackage.MD_EDITABLE_TABLE__EDITABLE:
                getEditable().clear();
                return;
            case DocGenViewPackage.MD_EDITABLE_TABLE__MERGE_COLS:
                getMergeCols().clear();
                return;
            case DocGenViewPackage.MD_EDITABLE_TABLE__ADD_LINE_NUM:
                setAddLineNum(ADD_LINE_NUM_EDEFAULT);
                return;
            case DocGenViewPackage.MD_EDITABLE_TABLE__GUI_BODY:
                getGuiBody().clear();
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
            case DocGenViewPackage.MD_EDITABLE_TABLE__PRECISION:
                return precision != PRECISION_EDEFAULT;
            case DocGenViewPackage.MD_EDITABLE_TABLE__GUI_HEADERS:
                return guiHeaders != null && !guiHeaders.isEmpty();
            case DocGenViewPackage.MD_EDITABLE_TABLE__EDITABLE:
                return editable != null && !editable.isEmpty();
            case DocGenViewPackage.MD_EDITABLE_TABLE__MERGE_COLS:
                return mergeCols != null && !mergeCols.isEmpty();
            case DocGenViewPackage.MD_EDITABLE_TABLE__ADD_LINE_NUM:
                return addLineNum != ADD_LINE_NUM_EDEFAULT;
            case DocGenViewPackage.MD_EDITABLE_TABLE__GUI_BODY:
                return guiBody != null && !guiBody.isEmpty();
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
        result.append(" (precision: ");
        result.append(precision);
        result.append(", guiHeaders: ");
        result.append(guiHeaders);
        result.append(", editable: ");
        result.append(editable);
        result.append(", mergeCols: ");
        result.append(mergeCols);
        result.append(", addLineNum: ");
        result.append(addLineNum);
        result.append(')');
        return result.toString();
    }

} // MDEditableTableImpl
