/**
 * <copyright> </copyright>
 * 
 * $Id$
 */
package gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.impl;

import gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.DgvalidationFactory;
import gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.DgvalidationPackage;
import gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Rule;
import gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Severity;
import gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Suite;
import gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Violation;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.impl.EPackageImpl;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Package</b>. <!--
 * end-user-doc -->
 * 
 * @generated
 */
public class DgvalidationPackageImpl extends EPackageImpl implements DgvalidationPackage {
    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    private EClass ruleEClass      = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    private EClass violationEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    private EClass suiteEClass     = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    private EEnum  severityEEnum   = null;

    /**
     * Creates an instance of the model <b>Package</b>, registered with
     * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the
     * package package URI value.
     * <p>
     * Note: the correct way to create the package is via the static factory
     * method {@link #init init()}, which also performs initialization of the
     * package, or returns the registered package, if one already exists. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see org.eclipse.emf.ecore.EPackage.Registry
     * @see gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.DgvalidationPackage#eNS_URI
     * @see #init()
     * @generated
     */
    private DgvalidationPackageImpl() {
        super(eNS_URI, DgvalidationFactory.eINSTANCE);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    private static boolean isInited = false;

    /**
     * Creates, registers, and initializes the <b>Package</b> for this model,
     * and for any others upon which it depends.
     * 
     * <p>
     * This method is used to initialize {@link DgvalidationPackage#eINSTANCE}
     * when that field is accessed. Clients should not invoke it directly.
     * Instead, they should simply access that field to obtain the package. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #eNS_URI
     * @see #createPackageContents()
     * @see #initializePackageContents()
     * @generated
     */
    public static DgvalidationPackage init() {
        if (isInited)
            return (DgvalidationPackage)EPackage.Registry.INSTANCE.getEPackage(DgvalidationPackage.eNS_URI);

        // Obtain or create and register package
        DgvalidationPackageImpl theDgvalidationPackage = (DgvalidationPackageImpl)(EPackage.Registry.INSTANCE
                .get(eNS_URI) instanceof DgvalidationPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI)
                : new DgvalidationPackageImpl());

        isInited = true;

        // Create package meta-data objects
        theDgvalidationPackage.createPackageContents();

        // Initialize created meta-data
        theDgvalidationPackage.initializePackageContents();

        // Mark meta-data to indicate it can't be changed
        theDgvalidationPackage.freeze();

        // Update the registry and return the package
        EPackage.Registry.INSTANCE.put(DgvalidationPackage.eNS_URI, theDgvalidationPackage);
        return theDgvalidationPackage;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EClass getRule() {
        return ruleEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getRule_Name() {
        return (EAttribute)ruleEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getRule_Description() {
        return (EAttribute)ruleEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getRule_Severity() {
        return (EAttribute)ruleEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EReference getRule_Violations() {
        return (EReference)ruleEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EClass getViolation() {
        return violationEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getViolation_ElementId() {
        return (EAttribute)violationEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getViolation_Comment() {
        return (EAttribute)violationEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EClass getSuite() {
        return suiteEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getSuite_ShowDetail() {
        return (EAttribute)suiteEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getSuite_ShowSummary() {
        return (EAttribute)suiteEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getSuite_OwnSection() {
        return (EAttribute)suiteEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EAttribute getSuite_Name() {
        return (EAttribute)suiteEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EReference getSuite_Rules() {
        return (EReference)suiteEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EEnum getSeverity() {
        return severityEEnum;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public DgvalidationFactory getDgvalidationFactory() {
        return (DgvalidationFactory)getEFactoryInstance();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    private boolean isCreated = false;

    /**
     * Creates the meta-model objects for the package. This method is guarded to
     * have no affect on any invocation but its first. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @generated
     */
    public void createPackageContents() {
        if (isCreated)
            return;
        isCreated = true;

        // Create classes and their features
        ruleEClass = createEClass(RULE);
        createEAttribute(ruleEClass, RULE__NAME);
        createEAttribute(ruleEClass, RULE__DESCRIPTION);
        createEAttribute(ruleEClass, RULE__SEVERITY);
        createEReference(ruleEClass, RULE__VIOLATIONS);

        violationEClass = createEClass(VIOLATION);
        createEAttribute(violationEClass, VIOLATION__ELEMENT_ID);
        createEAttribute(violationEClass, VIOLATION__COMMENT);

        suiteEClass = createEClass(SUITE);
        createEAttribute(suiteEClass, SUITE__SHOW_DETAIL);
        createEAttribute(suiteEClass, SUITE__SHOW_SUMMARY);
        createEAttribute(suiteEClass, SUITE__OWN_SECTION);
        createEAttribute(suiteEClass, SUITE__NAME);
        createEReference(suiteEClass, SUITE__RULES);

        // Create enums
        severityEEnum = createEEnum(SEVERITY);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    private boolean isInitialized = false;

    /**
     * Complete the initialization of the package and its meta-model. This
     * method is guarded to have no affect on any invocation but its first. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public void initializePackageContents() {
        if (isInitialized)
            return;
        isInitialized = true;

        // Initialize package
        setName(eNAME);
        setNsPrefix(eNS_PREFIX);
        setNsURI(eNS_URI);

        // Create type parameters

        // Set bounds for type parameters

        // Add supertypes to classes

        // Initialize classes and features; add operations and parameters
        initEClass(ruleEClass, Rule.class, "Rule", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getRule_Name(), ecorePackage.getEString(), "name", null, 0, 1, Rule.class,
                !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED,
                IS_ORDERED);
        initEAttribute(getRule_Description(), ecorePackage.getEString(), "description", null, 0, 1,
                Rule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
                !IS_DERIVED, IS_ORDERED);
        initEAttribute(getRule_Severity(), this.getSeverity(), "severity", null, 0, 1, Rule.class,
                !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED,
                IS_ORDERED);
        initEReference(getRule_Violations(), this.getViolation(), null, "violations", null, 0, -1,
                Rule.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES,
                !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(violationEClass, Violation.class, "Violation", !IS_ABSTRACT, !IS_INTERFACE,
                IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getViolation_ElementId(), ecorePackage.getEString(), "elementId", null, 0, 1,
                Violation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
                IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getViolation_Comment(), ecorePackage.getEString(), "comment", null, 0, 1,
                Violation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
                IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(suiteEClass, Suite.class, "Suite", !IS_ABSTRACT, !IS_INTERFACE,
                IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getSuite_ShowDetail(), ecorePackage.getEBoolean(), "showDetail", null, 0, 1,
                Suite.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
                !IS_DERIVED, IS_ORDERED);
        initEAttribute(getSuite_ShowSummary(), ecorePackage.getEBoolean(), "showSummary", null, 0, 1,
                Suite.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
                !IS_DERIVED, IS_ORDERED);
        initEAttribute(getSuite_OwnSection(), ecorePackage.getEBoolean(), "ownSection", null, 0, 1,
                Suite.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
                !IS_DERIVED, IS_ORDERED);
        initEAttribute(getSuite_Name(), ecorePackage.getEString(), "name", null, 0, 1, Suite.class,
                !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED,
                IS_ORDERED);
        initEReference(getSuite_Rules(), this.getRule(), null, "rules", null, 0, -1, Suite.class,
                !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES,
                !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        // Initialize enums and add enum literals
        initEEnum(severityEEnum, Severity.class, "Severity");
        addEEnumLiteral(severityEEnum, Severity.DEBUG);
        addEEnumLiteral(severityEEnum, Severity.INFO);
        addEEnumLiteral(severityEEnum, Severity.WARNING);
        addEEnumLiteral(severityEEnum, Severity.ERROR);
        addEEnumLiteral(severityEEnum, Severity.FATAL);

        // Create resource
        createResource(eNS_URI);
    }

} // DgvalidationPackageImpl
