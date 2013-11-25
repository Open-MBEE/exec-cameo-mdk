/**
 * <copyright> </copyright>
 * 
 * $Id$
 */
package gov.nasa.jpl.mgss.mbee.docgen.dgview.impl;

import gov.nasa.jpl.mgss.mbee.docgen.dgview.DgviewPackage;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.Image;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc --> An implementation of the model object '
 * <em><b>Image</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link gov.nasa.jpl.mgss.mbee.docgen.dgview.impl.ImageImpl#getDiagramId
 * <em>Diagram Id</em>}</li>
 * <li>{@link gov.nasa.jpl.mgss.mbee.docgen.dgview.impl.ImageImpl#getCaption
 * <em>Caption</em>}</li>
 * <li>{@link gov.nasa.jpl.mgss.mbee.docgen.dgview.impl.ImageImpl#isGennew <em>
 * Gennew</em>}</li>
 * <li>{@link gov.nasa.jpl.mgss.mbee.docgen.dgview.impl.ImageImpl#isDoNotShow
 * <em>Do Not Show</em>}</li>
 * </ul>
 * </p>
 * 
 * @generated
 */
public class ImageImpl extends ViewElementImpl implements Image {
    /**
     * The default value of the '{@link #getDiagramId() <em>Diagram Id</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getDiagramId()
     * @generated
     * @ordered
     */
    protected static final String  DIAGRAM_ID_EDEFAULT  = null;

    /**
     * The cached value of the '{@link #getDiagramId() <em>Diagram Id</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getDiagramId()
     * @generated
     * @ordered
     */
    protected String               diagramId            = DIAGRAM_ID_EDEFAULT;

    /**
     * The default value of the '{@link #getCaption() <em>Caption</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getCaption()
     * @generated
     * @ordered
     */
    protected static final String  CAPTION_EDEFAULT     = null;

    /**
     * The cached value of the '{@link #getCaption() <em>Caption</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getCaption()
     * @generated
     * @ordered
     */
    protected String               caption              = CAPTION_EDEFAULT;

    /**
     * The default value of the '{@link #isGennew() <em>Gennew</em>}' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #isGennew()
     * @generated
     * @ordered
     */
    protected static final boolean GENNEW_EDEFAULT      = false;

    /**
     * The cached value of the '{@link #isGennew() <em>Gennew</em>}' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #isGennew()
     * @generated
     * @ordered
     */
    protected boolean              gennew               = GENNEW_EDEFAULT;

    /**
     * The default value of the '{@link #isDoNotShow() <em>Do Not Show</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #isDoNotShow()
     * @generated
     * @ordered
     */
    protected static final boolean DO_NOT_SHOW_EDEFAULT = false;

    /**
     * The cached value of the '{@link #isDoNotShow() <em>Do Not Show</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #isDoNotShow()
     * @generated
     * @ordered
     */
    protected boolean              doNotShow            = DO_NOT_SHOW_EDEFAULT;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    protected ImageImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return DgviewPackage.Literals.IMAGE;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String getDiagramId() {
        return diagramId;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void setDiagramId(String newDiagramId) {
        String oldDiagramId = diagramId;
        diagramId = newDiagramId;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DgviewPackage.IMAGE__DIAGRAM_ID,
                    oldDiagramId, diagramId));
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
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DgviewPackage.IMAGE__CAPTION, oldCaption,
                    caption));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public boolean isGennew() {
        return gennew;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void setGennew(boolean newGennew) {
        boolean oldGennew = gennew;
        gennew = newGennew;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DgviewPackage.IMAGE__GENNEW, oldGennew,
                    gennew));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public boolean isDoNotShow() {
        return doNotShow;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void setDoNotShow(boolean newDoNotShow) {
        boolean oldDoNotShow = doNotShow;
        doNotShow = newDoNotShow;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DgviewPackage.IMAGE__DO_NOT_SHOW,
                    oldDoNotShow, doNotShow));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case DgviewPackage.IMAGE__DIAGRAM_ID:
                return getDiagramId();
            case DgviewPackage.IMAGE__CAPTION:
                return getCaption();
            case DgviewPackage.IMAGE__GENNEW:
                return isGennew();
            case DgviewPackage.IMAGE__DO_NOT_SHOW:
                return isDoNotShow();
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
            case DgviewPackage.IMAGE__DIAGRAM_ID:
                setDiagramId((String)newValue);
                return;
            case DgviewPackage.IMAGE__CAPTION:
                setCaption((String)newValue);
                return;
            case DgviewPackage.IMAGE__GENNEW:
                setGennew((Boolean)newValue);
                return;
            case DgviewPackage.IMAGE__DO_NOT_SHOW:
                setDoNotShow((Boolean)newValue);
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
            case DgviewPackage.IMAGE__DIAGRAM_ID:
                setDiagramId(DIAGRAM_ID_EDEFAULT);
                return;
            case DgviewPackage.IMAGE__CAPTION:
                setCaption(CAPTION_EDEFAULT);
                return;
            case DgviewPackage.IMAGE__GENNEW:
                setGennew(GENNEW_EDEFAULT);
                return;
            case DgviewPackage.IMAGE__DO_NOT_SHOW:
                setDoNotShow(DO_NOT_SHOW_EDEFAULT);
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
            case DgviewPackage.IMAGE__DIAGRAM_ID:
                return DIAGRAM_ID_EDEFAULT == null ? diagramId != null : !DIAGRAM_ID_EDEFAULT
                        .equals(diagramId);
            case DgviewPackage.IMAGE__CAPTION:
                return CAPTION_EDEFAULT == null ? caption != null : !CAPTION_EDEFAULT.equals(caption);
            case DgviewPackage.IMAGE__GENNEW:
                return gennew != GENNEW_EDEFAULT;
            case DgviewPackage.IMAGE__DO_NOT_SHOW:
                return doNotShow != DO_NOT_SHOW_EDEFAULT;
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
        if (eIsProxy())
            return super.toString();

        StringBuffer result = new StringBuffer(super.toString());
        result.append(" (diagramId: ");
        result.append(diagramId);
        result.append(", caption: ");
        result.append(caption);
        result.append(", gennew: ");
        result.append(gennew);
        result.append(", doNotShow: ");
        result.append(doNotShow);
        result.append(')');
        return result.toString();
    }

} // ImageImpl
