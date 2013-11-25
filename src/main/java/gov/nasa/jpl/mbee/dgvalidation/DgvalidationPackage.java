/**
 * <copyright> </copyright>
 * 
 * $Id$
 */
package gov.nasa.jpl.mbee.dgvalidation;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc --> The <b>Package</b> for the model. It contains
 * accessors for the meta objects to represent
 * <ul>
 * <li>each class,</li>
 * <li>each feature of each class,</li>
 * <li>each enum,</li>
 * <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * 
 * @see gov.nasa.jpl.mbee.dgvalidation.DgvalidationFactory
 * @model kind="package"
 * @generated
 */
public interface DgvalidationPackage extends EPackage {
    /**
     * The package name. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    String              eNAME                   = "dgvalidation";

    /**
     * The package namespace URI. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    String              eNS_URI                 = "http://mbee.jpl.nasa.gov/docgen/dgvalidation";

    /**
     * The package namespace name. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    String              eNS_PREFIX              = "gov.nasa.jpl.mgss.mbee.docgen.dgvalidation";

    /**
     * The singleton instance of the package. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     */
    DgvalidationPackage eINSTANCE               = gov.nasa.jpl.mbee.dgvalidation.impl.DgvalidationPackageImpl
                                                        .init();

    /**
     * The meta object id for the '
     * {@link gov.nasa.jpl.mbee.dgvalidation.impl.RuleImpl
     * <em>Rule</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see gov.nasa.jpl.mbee.dgvalidation.impl.RuleImpl
     * @see gov.nasa.jpl.mbee.dgvalidation.impl.DgvalidationPackageImpl#getRule()
     * @generated
     */
    int                 RULE                    = 0;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int                 RULE__NAME              = 0;

    /**
     * The feature id for the '<em><b>Description</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int                 RULE__DESCRIPTION       = 1;

    /**
     * The feature id for the '<em><b>Severity</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int                 RULE__SEVERITY          = 2;

    /**
     * The feature id for the '<em><b>Violations</b></em>' containment reference
     * list. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int                 RULE__VIOLATIONS        = 3;

    /**
     * The number of structural features of the '<em>Rule</em>' class. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int                 RULE_FEATURE_COUNT      = 4;

    /**
     * The meta object id for the '
     * {@link gov.nasa.jpl.mbee.dgvalidation.impl.ViolationImpl
     * <em>Violation</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see gov.nasa.jpl.mbee.dgvalidation.impl.ViolationImpl
     * @see gov.nasa.jpl.mbee.dgvalidation.impl.DgvalidationPackageImpl#getViolation()
     * @generated
     */
    int                 VIOLATION               = 1;

    /**
     * The feature id for the '<em><b>Element Id</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int                 VIOLATION__ELEMENT_ID   = 0;

    /**
     * The feature id for the '<em><b>Comment</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int                 VIOLATION__COMMENT      = 1;

    /**
     * The number of structural features of the '<em>Violation</em>' class. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int                 VIOLATION_FEATURE_COUNT = 2;

    /**
     * The meta object id for the '
     * {@link gov.nasa.jpl.mbee.dgvalidation.impl.SuiteImpl
     * <em>Suite</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see gov.nasa.jpl.mbee.dgvalidation.impl.SuiteImpl
     * @see gov.nasa.jpl.mbee.dgvalidation.impl.DgvalidationPackageImpl#getSuite()
     * @generated
     */
    int                 SUITE                   = 2;

    /**
     * The feature id for the '<em><b>Show Detail</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int                 SUITE__SHOW_DETAIL      = 0;

    /**
     * The feature id for the '<em><b>Show Summary</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int                 SUITE__SHOW_SUMMARY     = 1;

    /**
     * The feature id for the '<em><b>Own Section</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int                 SUITE__OWN_SECTION      = 2;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int                 SUITE__NAME             = 3;

    /**
     * The feature id for the '<em><b>Rules</b></em>' containment reference
     * list. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int                 SUITE__RULES            = 4;

    /**
     * The number of structural features of the '<em>Suite</em>' class. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int                 SUITE_FEATURE_COUNT     = 5;

    /**
     * The meta object id for the '
     * {@link gov.nasa.jpl.mbee.dgvalidation.Severity
     * <em>Severity</em>}' enum. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see gov.nasa.jpl.mbee.dgvalidation.Severity
     * @see gov.nasa.jpl.mbee.dgvalidation.impl.DgvalidationPackageImpl#getSeverity()
     * @generated
     */
    int                 SEVERITY                = 3;

    /**
     * Returns the meta object for class '
     * {@link gov.nasa.jpl.mbee.dgvalidation.Rule <em>Rule</em>}'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for class '<em>Rule</em>'.
     * @see gov.nasa.jpl.mbee.dgvalidation.Rule
     * @generated
     */
    EClass getRule();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgvalidation.Rule#getName
     * <em>Name</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see gov.nasa.jpl.mbee.dgvalidation.Rule#getName()
     * @see #getRule()
     * @generated
     */
    EAttribute getRule_Name();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgvalidation.Rule#getDescription
     * <em>Description</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Description</em>'.
     * @see gov.nasa.jpl.mbee.dgvalidation.Rule#getDescription()
     * @see #getRule()
     * @generated
     */
    EAttribute getRule_Description();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgvalidation.Rule#getSeverity
     * <em>Severity</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Severity</em>'.
     * @see gov.nasa.jpl.mbee.dgvalidation.Rule#getSeverity()
     * @see #getRule()
     * @generated
     */
    EAttribute getRule_Severity();

    /**
     * Returns the meta object for the containment reference list '
     * {@link gov.nasa.jpl.mbee.dgvalidation.Rule#getViolations
     * <em>Violations</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the containment reference list '
     *         <em>Violations</em>'.
     * @see gov.nasa.jpl.mbee.dgvalidation.Rule#getViolations()
     * @see #getRule()
     * @generated
     */
    EReference getRule_Violations();

    /**
     * Returns the meta object for class '
     * {@link gov.nasa.jpl.mbee.dgvalidation.Violation
     * <em>Violation</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for class '<em>Violation</em>'.
     * @see gov.nasa.jpl.mbee.dgvalidation.Violation
     * @generated
     */
    EClass getViolation();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgvalidation.Violation#getElementId
     * <em>Element Id</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Element Id</em>'.
     * @see gov.nasa.jpl.mbee.dgvalidation.Violation#getElementId()
     * @see #getViolation()
     * @generated
     */
    EAttribute getViolation_ElementId();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgvalidation.Violation#getComment
     * <em>Comment</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Comment</em>'.
     * @see gov.nasa.jpl.mbee.dgvalidation.Violation#getComment()
     * @see #getViolation()
     * @generated
     */
    EAttribute getViolation_Comment();

    /**
     * Returns the meta object for class '
     * {@link gov.nasa.jpl.mbee.dgvalidation.Suite <em>Suite</em>}'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for class '<em>Suite</em>'.
     * @see gov.nasa.jpl.mbee.dgvalidation.Suite
     * @generated
     */
    EClass getSuite();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgvalidation.Suite#isShowDetail
     * <em>Show Detail</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Show Detail</em>'.
     * @see gov.nasa.jpl.mbee.dgvalidation.Suite#isShowDetail()
     * @see #getSuite()
     * @generated
     */
    EAttribute getSuite_ShowDetail();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgvalidation.Suite#isShowSummary
     * <em>Show Summary</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Show Summary</em>'.
     * @see gov.nasa.jpl.mbee.dgvalidation.Suite#isShowSummary()
     * @see #getSuite()
     * @generated
     */
    EAttribute getSuite_ShowSummary();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgvalidation.Suite#isOwnSection
     * <em>Own Section</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Own Section</em>'.
     * @see gov.nasa.jpl.mbee.dgvalidation.Suite#isOwnSection()
     * @see #getSuite()
     * @generated
     */
    EAttribute getSuite_OwnSection();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgvalidation.Suite#getName
     * <em>Name</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Name</em>'.
     * @see gov.nasa.jpl.mbee.dgvalidation.Suite#getName()
     * @see #getSuite()
     * @generated
     */
    EAttribute getSuite_Name();

    /**
     * Returns the meta object for the containment reference list '
     * {@link gov.nasa.jpl.mbee.dgvalidation.Suite#getRules
     * <em>Rules</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the containment reference list '
     *         <em>Rules</em>'.
     * @see gov.nasa.jpl.mbee.dgvalidation.Suite#getRules()
     * @see #getSuite()
     * @generated
     */
    EReference getSuite_Rules();

    /**
     * Returns the meta object for enum '
     * {@link gov.nasa.jpl.mbee.dgvalidation.Severity
     * <em>Severity</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for enum '<em>Severity</em>'.
     * @see gov.nasa.jpl.mbee.dgvalidation.Severity
     * @generated
     */
    EEnum getSeverity();

    /**
     * Returns the factory that creates the instances of the model. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the factory that creates the instances of the model.
     * @generated
     */
    DgvalidationFactory getDgvalidationFactory();

    /**
     * <!-- begin-user-doc --> Defines literals for the meta objects that
     * represent
     * <ul>
     * <li>each class,</li>
     * <li>each feature of each class,</li>
     * <li>each enum,</li>
     * <li>and each data type</li>
     * </ul>
     * <!-- end-user-doc -->
     * 
     * @generated
     */
    interface Literals {
        /**
         * The meta object literal for the '
         * {@link gov.nasa.jpl.mbee.dgvalidation.impl.RuleImpl
         * <em>Rule</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @see gov.nasa.jpl.mbee.dgvalidation.impl.RuleImpl
         * @see gov.nasa.jpl.mbee.dgvalidation.impl.DgvalidationPackageImpl#getRule()
         * @generated
         */
        EClass     RULE                  = eINSTANCE.getRule();

        /**
         * The meta object literal for the '<em><b>Name</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute RULE__NAME            = eINSTANCE.getRule_Name();

        /**
         * The meta object literal for the '<em><b>Description</b></em>'
         * attribute feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute RULE__DESCRIPTION     = eINSTANCE.getRule_Description();

        /**
         * The meta object literal for the '<em><b>Severity</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute RULE__SEVERITY        = eINSTANCE.getRule_Severity();

        /**
         * The meta object literal for the '<em><b>Violations</b></em>'
         * containment reference list feature. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * 
         * @generated
         */
        EReference RULE__VIOLATIONS      = eINSTANCE.getRule_Violations();

        /**
         * The meta object literal for the '
         * {@link gov.nasa.jpl.mbee.dgvalidation.impl.ViolationImpl
         * <em>Violation</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc
         * -->
         * 
         * @see gov.nasa.jpl.mbee.dgvalidation.impl.ViolationImpl
         * @see gov.nasa.jpl.mbee.dgvalidation.impl.DgvalidationPackageImpl#getViolation()
         * @generated
         */
        EClass     VIOLATION             = eINSTANCE.getViolation();

        /**
         * The meta object literal for the '<em><b>Element Id</b></em>'
         * attribute feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute VIOLATION__ELEMENT_ID = eINSTANCE.getViolation_ElementId();

        /**
         * The meta object literal for the '<em><b>Comment</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute VIOLATION__COMMENT    = eINSTANCE.getViolation_Comment();

        /**
         * The meta object literal for the '
         * {@link gov.nasa.jpl.mbee.dgvalidation.impl.SuiteImpl
         * <em>Suite</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @see gov.nasa.jpl.mbee.dgvalidation.impl.SuiteImpl
         * @see gov.nasa.jpl.mbee.dgvalidation.impl.DgvalidationPackageImpl#getSuite()
         * @generated
         */
        EClass     SUITE                 = eINSTANCE.getSuite();

        /**
         * The meta object literal for the '<em><b>Show Detail</b></em>'
         * attribute feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute SUITE__SHOW_DETAIL    = eINSTANCE.getSuite_ShowDetail();

        /**
         * The meta object literal for the '<em><b>Show Summary</b></em>'
         * attribute feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute SUITE__SHOW_SUMMARY   = eINSTANCE.getSuite_ShowSummary();

        /**
         * The meta object literal for the '<em><b>Own Section</b></em>'
         * attribute feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute SUITE__OWN_SECTION    = eINSTANCE.getSuite_OwnSection();

        /**
         * The meta object literal for the '<em><b>Name</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute SUITE__NAME           = eINSTANCE.getSuite_Name();

        /**
         * The meta object literal for the '<em><b>Rules</b></em>' containment
         * reference list feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EReference SUITE__RULES          = eINSTANCE.getSuite_Rules();

        /**
         * The meta object literal for the '
         * {@link gov.nasa.jpl.mbee.dgvalidation.Severity
         * <em>Severity</em>}' enum. <!-- begin-user-doc --> <!-- end-user-doc
         * -->
         * 
         * @see gov.nasa.jpl.mbee.dgvalidation.Severity
         * @see gov.nasa.jpl.mbee.dgvalidation.impl.DgvalidationPackageImpl#getSeverity()
         * @generated
         */
        EEnum      SEVERITY              = eINSTANCE.getSeverity();

    }

} // DgvalidationPackage
