package gov.nasa.jpl.mbee.mdk.docgen.validation.impl;

import gov.nasa.jpl.mbee.mdk.docgen.validation.DocGenValidationPackage;
import gov.nasa.jpl.mbee.mdk.docgen.validation.Rule;
import gov.nasa.jpl.mbee.mdk.docgen.validation.Severity;
import gov.nasa.jpl.mbee.mdk.docgen.validation.Violation;
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
 * <li>{@link RuleImpl#getName
 * <em>Name</em>}</li>
 * <li>
 * {@link RuleImpl#getDescription
 * <em>Description</em>}</li>
 * <li>
 * {@link RuleImpl#getSeverity
 * <em>Severity</em>}</li>
 * <li>
 * {@link RuleImpl#getViolations
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
        return DocGenValidationPackage.Literals.RULE;
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
            eNotify(new ENotificationImpl(this, Notification.SET, DocGenValidationPackage.RULE__NAME, oldName,
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
            eNotify(new ENotificationImpl(this, Notification.SET, DocGenValidationPackage.RULE__DESCRIPTION,
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
            eNotify(new ENotificationImpl(this, Notification.SET, DocGenValidationPackage.RULE__SEVERITY,
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
                    DocGenValidationPackage.RULE__VIOLATIONS);
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
            case DocGenValidationPackage.RULE__VIOLATIONS:
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
            case DocGenValidationPackage.RULE__NAME:
                return getName();
            case DocGenValidationPackage.RULE__DESCRIPTION:
                return getDescription();
            case DocGenValidationPackage.RULE__SEVERITY:
                return getSeverity();
            case DocGenValidationPackage.RULE__VIOLATIONS:
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
            case DocGenValidationPackage.RULE__NAME:
                setName((String) newValue);
                return;
            case DocGenValidationPackage.RULE__DESCRIPTION:
                setDescription((String) newValue);
                return;
            case DocGenValidationPackage.RULE__SEVERITY:
                setSeverity((Severity) newValue);
                return;
            case DocGenValidationPackage.RULE__VIOLATIONS:
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
            case DocGenValidationPackage.RULE__NAME:
                setName(NAME_EDEFAULT);
                return;
            case DocGenValidationPackage.RULE__DESCRIPTION:
                setDescription(DESCRIPTION_EDEFAULT);
                return;
            case DocGenValidationPackage.RULE__SEVERITY:
                setSeverity(SEVERITY_EDEFAULT);
                return;
            case DocGenValidationPackage.RULE__VIOLATIONS:
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
            case DocGenValidationPackage.RULE__NAME:
                return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
            case DocGenValidationPackage.RULE__DESCRIPTION:
                return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT
                        .equals(description);
            case DocGenValidationPackage.RULE__SEVERITY:
                return severity != SEVERITY_EDEFAULT;
            case DocGenValidationPackage.RULE__VIOLATIONS:
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
