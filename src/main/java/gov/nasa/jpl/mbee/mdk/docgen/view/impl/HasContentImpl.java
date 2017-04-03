package gov.nasa.jpl.mbee.mdk.docgen.view.impl;

import gov.nasa.jpl.mbee.mdk.docgen.view.DocGenViewPackage;
import gov.nasa.jpl.mbee.mdk.docgen.view.HasContent;
import gov.nasa.jpl.mbee.mdk.docgen.view.ViewElement;
import org.eclipse.emf.common.notify.NotificationChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import java.util.Collection;

/**
 * <!-- begin-user-doc --> An implementation of the model object '
 * <em><b>Has Content</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>
 * {@link gov.nasa.jpl.mbee.mdk.docgen.view.impl.HasContentImpl#getChildren
 * <em>Children</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public abstract class HasContentImpl extends ViewElementImpl implements HasContent {
    /**
     * The cached value of the '{@link #getChildren() <em>Children</em>}'
     * containment reference list. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getChildren()
     */
    protected EList<ViewElement> children;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    protected HasContentImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return DocGenViewPackage.Literals.HAS_CONTENT;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EList<ViewElement> getChildren() {
        if (children == null) {
            children = new EObjectContainmentEList<ViewElement>(ViewElement.class, this,
                    DocGenViewPackage.HAS_CONTENT__CHILDREN);
        }
        return children;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case DocGenViewPackage.HAS_CONTENT__CHILDREN:
                return ((InternalEList<?>) getChildren()).basicRemove(otherEnd, msgs);
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
            case DocGenViewPackage.HAS_CONTENT__CHILDREN:
                return getChildren();
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
            case DocGenViewPackage.HAS_CONTENT__CHILDREN:
                getChildren().clear();
                getChildren().addAll((Collection<? extends ViewElement>) newValue);
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
            case DocGenViewPackage.HAS_CONTENT__CHILDREN:
                getChildren().clear();
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
            case DocGenViewPackage.HAS_CONTENT__CHILDREN:
                return children != null && !children.isEmpty();
        }
        return super.eIsSet(featureID);
    }

} // HasContentImpl
