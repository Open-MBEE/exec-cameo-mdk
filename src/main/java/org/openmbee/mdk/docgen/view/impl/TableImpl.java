package org.openmbee.mdk.docgen.view.impl;

import org.openmbee.mdk.docgen.view.ColSpec;
import org.openmbee.mdk.docgen.view.DocGenViewPackage;
import org.openmbee.mdk.docgen.view.Table;
import org.openmbee.mdk.docgen.view.TableRow;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import java.util.Collection;

/**
 * <!-- begin-user-doc --> An implementation of the model object '
 * <em><b>Table</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link TableImpl#getBody <em>
 * Body</em>}</li>
 * <li>{@link TableImpl#getCaption
 * <em>Caption</em>}</li>
 * <li>{@link TableImpl#getStyle <em>
 * Style</em>}</li>
 * <li>{@link TableImpl#getHeaders
 * <em>Headers</em>}</li>
 * <li>{@link TableImpl#getColspecs
 * <em>Colspecs</em>}</li>
 * <li>{@link TableImpl#getCols <em>
 * Cols</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class TableImpl extends ViewElementImpl implements Table {
    /**
     * The cached value of the '{@link #getBody() <em>Body</em>}' containment
     * reference list. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getBody()
     */
    protected EList<TableRow> body;

    /**
     * The default value of the '{@link #getCaption() <em>Caption</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getCaption()
     */
    protected static final String CAPTION_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getCaption() <em>Caption</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getCaption()
     */
    protected String caption = CAPTION_EDEFAULT;

    /**
     * The default value of the '{@link #getStyle() <em>Style</em>}' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getStyle()
     */
    protected static final String STYLE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getStyle() <em>Style</em>}' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getStyle()
     */
    protected String style = STYLE_EDEFAULT;

    /**
     * The cached value of the '{@link #getHeaders() <em>Headers</em>}'
     * containment reference list. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getHeaders()
     */
    protected EList<TableRow> headers;

    /**
     * The cached value of the '{@link #getColspecs() <em>Colspecs</em>}'
     * containment reference list. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getColspecs()
     */
    protected EList<ColSpec> colspecs;

    /**
     * The default value of the '{@link #getCols() <em>Cols</em>}' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getCols()
     */
    protected static final int COLS_EDEFAULT = 0;

    /**
     * The cached value of the '{@link #getCols() <em>Cols</em>}' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getCols()
     */
    protected int cols = COLS_EDEFAULT;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    protected TableImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return DocGenViewPackage.Literals.TABLE;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EList<TableRow> getBody() {
        if (body == null) {
            body = new EObjectContainmentEList<TableRow>(TableRow.class, this, DocGenViewPackage.TABLE__BODY);
        }
        return body;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getCaption() {
        return caption;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setCaption(String newCaption) {
        String oldCaption = caption;
        caption = newCaption;
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, DocGenViewPackage.TABLE__CAPTION, oldCaption,
                    caption));
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getStyle() {
        return style;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setStyle(String newStyle) {
        String oldStyle = style;
        style = newStyle;
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, DocGenViewPackage.TABLE__STYLE, oldStyle, style));
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EList<TableRow> getHeaders() {
        if (headers == null) {
            headers = new EObjectContainmentEList<TableRow>(TableRow.class, this,
                    DocGenViewPackage.TABLE__HEADERS);
        }
        return headers;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EList<ColSpec> getColspecs() {
        if (colspecs == null) {
            colspecs = new EObjectContainmentEList<ColSpec>(ColSpec.class, this,
                    DocGenViewPackage.TABLE__COLSPECS);
        }
        return colspecs;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public int getCols() {
        return cols;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setCols(int newCols) {
        int oldCols = cols;
        cols = newCols;
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, DocGenViewPackage.TABLE__COLS, oldCols, cols));
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case DocGenViewPackage.TABLE__BODY:
                return ((InternalEList<?>) getBody()).basicRemove(otherEnd, msgs);
            case DocGenViewPackage.TABLE__HEADERS:
                return ((InternalEList<?>) getHeaders()).basicRemove(otherEnd, msgs);
            case DocGenViewPackage.TABLE__COLSPECS:
                return ((InternalEList<?>) getColspecs()).basicRemove(otherEnd, msgs);
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
            case DocGenViewPackage.TABLE__BODY:
                return getBody();
            case DocGenViewPackage.TABLE__CAPTION:
                return getCaption();
            case DocGenViewPackage.TABLE__STYLE:
                return getStyle();
            case DocGenViewPackage.TABLE__HEADERS:
                return getHeaders();
            case DocGenViewPackage.TABLE__COLSPECS:
                return getColspecs();
            case DocGenViewPackage.TABLE__COLS:
                return getCols();
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
            case DocGenViewPackage.TABLE__BODY:
                getBody().clear();
                getBody().addAll((Collection<? extends TableRow>) newValue);
                return;
            case DocGenViewPackage.TABLE__CAPTION:
                setCaption((String) newValue);
                return;
            case DocGenViewPackage.TABLE__STYLE:
                setStyle((String) newValue);
                return;
            case DocGenViewPackage.TABLE__HEADERS:
                getHeaders().clear();
                getHeaders().addAll((Collection<? extends TableRow>) newValue);
                return;
            case DocGenViewPackage.TABLE__COLSPECS:
                getColspecs().clear();
                getColspecs().addAll((Collection<? extends ColSpec>) newValue);
                return;
            case DocGenViewPackage.TABLE__COLS:
                setCols((Integer) newValue);
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
            case DocGenViewPackage.TABLE__BODY:
                getBody().clear();
                return;
            case DocGenViewPackage.TABLE__CAPTION:
                setCaption(CAPTION_EDEFAULT);
                return;
            case DocGenViewPackage.TABLE__STYLE:
                setStyle(STYLE_EDEFAULT);
                return;
            case DocGenViewPackage.TABLE__HEADERS:
                getHeaders().clear();
                return;
            case DocGenViewPackage.TABLE__COLSPECS:
                getColspecs().clear();
                return;
            case DocGenViewPackage.TABLE__COLS:
                setCols(COLS_EDEFAULT);
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
            case DocGenViewPackage.TABLE__BODY:
                return body != null && !body.isEmpty();
            case DocGenViewPackage.TABLE__CAPTION:
                return CAPTION_EDEFAULT == null ? caption != null : !CAPTION_EDEFAULT.equals(caption);
            case DocGenViewPackage.TABLE__STYLE:
                return STYLE_EDEFAULT == null ? style != null : !STYLE_EDEFAULT.equals(style);
            case DocGenViewPackage.TABLE__HEADERS:
                return headers != null && !headers.isEmpty();
            case DocGenViewPackage.TABLE__COLSPECS:
                return colspecs != null && !colspecs.isEmpty();
            case DocGenViewPackage.TABLE__COLS:
                return cols != COLS_EDEFAULT;
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
        result.append(" (caption: ");
        result.append(caption);
        result.append(", style: ");
        result.append(style);
        result.append(", cols: ");
        result.append(cols);
        result.append(')');
        return result.toString();
    }

} // TableImpl
