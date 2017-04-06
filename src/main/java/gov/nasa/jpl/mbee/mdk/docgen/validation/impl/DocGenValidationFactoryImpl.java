package gov.nasa.jpl.mbee.mdk.docgen.validation.impl;

import gov.nasa.jpl.mbee.mdk.docgen.validation.*;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Factory</b>. <!--
 * end-user-doc -->
 *
 * @generated
 */
public class DocGenValidationFactoryImpl extends EFactoryImpl implements DocGenValidationFactory {
    /**
     * Creates the default factory implementation. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @generated
     */
    public static DocGenValidationFactory init() {
        try {
            DocGenValidationFactory theDocGenValidationFactory = (DocGenValidationFactory) EPackage.Registry.INSTANCE
                    .getEFactory("http://mbee.jpl.nasa.gov/docgen/dgvalidation");
            if (theDocGenValidationFactory != null) {
                return theDocGenValidationFactory;
            }
        } catch (Exception exception) {
            EcorePlugin.INSTANCE.log(exception);
        }
        return new DocGenValidationFactoryImpl();
    }

    /**
     * Creates an instance of the factory. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @generated
     */
    public DocGenValidationFactoryImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EObject create(EClass eClass) {
        switch (eClass.getClassifierID()) {
            case DocGenValidationPackage.RULE:
                return createRule();
            case DocGenValidationPackage.VIOLATION:
                return createViolation();
            case DocGenValidationPackage.SUITE:
                return createSuite();
            default:
                throw new IllegalArgumentException("The class '" + eClass.getName()
                        + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public Object createFromString(EDataType eDataType, String initialValue) {
        switch (eDataType.getClassifierID()) {
            case DocGenValidationPackage.SEVERITY:
                return createSeverityFromString(eDataType, initialValue);
            default:
                throw new IllegalArgumentException("The datatype '" + eDataType.getName()
                        + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String convertToString(EDataType eDataType, Object instanceValue) {
        switch (eDataType.getClassifierID()) {
            case DocGenValidationPackage.SEVERITY:
                return convertSeverityToString(eDataType, instanceValue);
            default:
                throw new IllegalArgumentException("The datatype '" + eDataType.getName()
                        + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public Rule createRule() {
        RuleImpl rule = new RuleImpl();
        return rule;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public Violation createViolation() {
        ViolationImpl violation = new ViolationImpl();
        return violation;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public Suite createSuite() {
        SuiteImpl suite = new SuiteImpl();
        return suite;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    public Severity createSeverityFromString(EDataType eDataType, String initialValue) {
        Severity result = Severity.get(initialValue);
        if (result == null) {
            throw new IllegalArgumentException("The value '" + initialValue
                    + "' is not a valid enumerator of '" + eDataType.getName() + "'");
        }
        return result;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    public String convertSeverityToString(EDataType eDataType, Object instanceValue) {
        return instanceValue == null ? null : instanceValue.toString();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public DocGenValidationPackage getDgvalidationPackage() {
        return (DocGenValidationPackage) getEPackage();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @deprecated
     */
    @Deprecated
    public static DocGenValidationPackage getPackage() {
        return DocGenValidationPackage.eINSTANCE;
    }

} // DocGenValidationFactoryImpl
