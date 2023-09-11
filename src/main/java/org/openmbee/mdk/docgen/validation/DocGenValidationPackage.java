package org.openmbee.mdk.docgen.validation;

import org.openmbee.mdk.docgen.validation.impl.DocGenValidationPackageImpl;
import org.openmbee.mdk.docgen.validation.impl.RuleImpl;
import org.openmbee.mdk.docgen.validation.impl.SuiteImpl;
import org.openmbee.mdk.docgen.validation.impl.ViolationImpl;
import org.eclipse.emf.ecore.*;

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
 * @model kind="package"
 * @generated
 * @see DocGenValidationFactory
 */
public interface DocGenValidationPackage extends EPackage {
    /**
     * The package name. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    String eNAME = "dgvalidation";

    /**
     * The package namespace URI. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    String eNS_URI = "http://mbee.jpl.nasa.gov/docgen/dgvalidation";

    /**
     * The package namespace name. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    String eNS_PREFIX = "org.openmbee.mdk.dgvalidation";

    /**
     * The singleton instance of the package. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @generated
     */
    DocGenValidationPackage eINSTANCE = DocGenValidationPackageImpl
            .init();

    /**
     * The meta object id for the '
     * {@link RuleImpl
     * <em>Rule</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @see RuleImpl
     * @see DocGenValidationPackageImpl#getRule()
     */
    int RULE = 0;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int RULE__NAME = 0;

    /**
     * The feature id for the '<em><b>Description</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int RULE__DESCRIPTION = 1;

    /**
     * The feature id for the '<em><b>Severity</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int RULE__SEVERITY = 2;

    /**
     * The feature id for the '<em><b>Violations</b></em>' containment reference
     * list. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int RULE__VIOLATIONS = 3;

    /**
     * The number of structural features of the '<em>Rule</em>' class. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int RULE_FEATURE_COUNT = 4;

    /**
     * The meta object id for the '
     * {@link ViolationImpl
     * <em>Violation</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @see ViolationImpl
     * @see DocGenValidationPackageImpl#getViolation()
     */
    int VIOLATION = 1;

    /**
     * The feature id for the '<em><b>Element Id</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int VIOLATION__ELEMENT_ID = 0;

    /**
     * The feature id for the '<em><b>Comment</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int VIOLATION__COMMENT = 1;

    /**
     * The number of structural features of the '<em>Violation</em>' class. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int VIOLATION_FEATURE_COUNT = 2;

    /**
     * The meta object id for the '
     * {@link SuiteImpl
     * <em>Suite</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @see SuiteImpl
     * @see DocGenValidationPackageImpl#getSuite()
     */
    int SUITE = 2;

    /**
     * The feature id for the '<em><b>Show Detail</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int SUITE__SHOW_DETAIL = 0;

    /**
     * The feature id for the '<em><b>Show Summary</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int SUITE__SHOW_SUMMARY = 1;

    /**
     * The feature id for the '<em><b>Own Section</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int SUITE__OWN_SECTION = 2;

    /**
     * The feature id for the '<em><b>Name</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int SUITE__NAME = 3;

    /**
     * The feature id for the '<em><b>Rules</b></em>' containment reference
     * list. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int SUITE__RULES = 4;

    /**
     * The number of structural features of the '<em>Suite</em>' class. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     */
    int SUITE_FEATURE_COUNT = 5;

    /**
     * The meta object id for the '
     * {@link Severity
     * <em>Severity</em>}' enum. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @see Severity
     * @see DocGenValidationPackageImpl#getSeverity()
     */
    int SEVERITY = 3;

    /**
     * Returns the meta object for class '
     * {@link Rule <em>Rule</em>}'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for class '<em>Rule</em>'.
     * @generated
     * @see Rule
     */
    EClass getRule();

    /**
     * Returns the meta object for the attribute '
     * {@link Rule#getName
     * <em>Name</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for the attribute '<em>Name</em>'.
     * @generated
     * @see Rule#getName()
     * @see #getRule()
     */
    EAttribute getRule_Name();

    /**
     * Returns the meta object for the attribute '
     * {@link Rule#getDescription
     * <em>Description</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for the attribute '<em>Description</em>'.
     * @generated
     * @see Rule#getDescription()
     * @see #getRule()
     */
    EAttribute getRule_Description();

    /**
     * Returns the meta object for the attribute '
     * {@link Rule#getSeverity
     * <em>Severity</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for the attribute '<em>Severity</em>'.
     * @generated
     * @see Rule#getSeverity()
     * @see #getRule()
     */
    EAttribute getRule_Severity();

    /**
     * Returns the meta object for the containment reference list '
     * {@link Rule#getViolations
     * <em>Violations</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for the containment reference list '
     * <em>Violations</em>'.
     * @generated
     * @see Rule#getViolations()
     * @see #getRule()
     */
    EReference getRule_Violations();

    /**
     * Returns the meta object for class '
     * {@link Violation
     * <em>Violation</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for class '<em>Violation</em>'.
     * @generated
     * @see Violation
     */
    EClass getViolation();

    /**
     * Returns the meta object for the attribute '
     * {@link Violation#getElementId
     * <em>Element Id</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for the attribute '<em>Element Id</em>'.
     * @generated
     * @see Violation#getElementId()
     * @see #getViolation()
     */
    EAttribute getViolation_ElementId();

    /**
     * Returns the meta object for the attribute '
     * {@link Violation#getComment
     * <em>Comment</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for the attribute '<em>Comment</em>'.
     * @generated
     * @see Violation#getComment()
     * @see #getViolation()
     */
    EAttribute getViolation_Comment();

    /**
     * Returns the meta object for class '
     * {@link Suite <em>Suite</em>}'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for class '<em>Suite</em>'.
     * @generated
     * @see Suite
     */
    EClass getSuite();

    /**
     * Returns the meta object for the attribute '
     * {@link Suite#isShowDetail
     * <em>Show Detail</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for the attribute '<em>Show Detail</em>'.
     * @generated
     * @see Suite#isShowDetail()
     * @see #getSuite()
     */
    EAttribute getSuite_ShowDetail();

    /**
     * Returns the meta object for the attribute '
     * {@link Suite#isShowSummary
     * <em>Show Summary</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for the attribute '<em>Show Summary</em>'.
     * @generated
     * @see Suite#isShowSummary()
     * @see #getSuite()
     */
    EAttribute getSuite_ShowSummary();

    /**
     * Returns the meta object for the attribute '
     * {@link Suite#isOwnSection
     * <em>Own Section</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for the attribute '<em>Own Section</em>'.
     * @generated
     * @see Suite#isOwnSection()
     * @see #getSuite()
     */
    EAttribute getSuite_OwnSection();

    /**
     * Returns the meta object for the attribute '
     * {@link Suite#getName
     * <em>Name</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for the attribute '<em>Name</em>'.
     * @generated
     * @see Suite#getName()
     * @see #getSuite()
     */
    EAttribute getSuite_Name();

    /**
     * Returns the meta object for the containment reference list '
     * {@link Suite#getRules
     * <em>Rules</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for the containment reference list '
     * <em>Rules</em>'.
     * @generated
     * @see Suite#getRules()
     * @see #getSuite()
     */
    EReference getSuite_Rules();

    /**
     * Returns the meta object for enum '
     * {@link Severity
     * <em>Severity</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the meta object for enum '<em>Severity</em>'.
     * @generated
     * @see Severity
     */
    EEnum getSeverity();

    /**
     * Returns the factory that creates the instances of the model. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @return the factory that creates the instances of the model.
     * @generated
     */
    DocGenValidationFactory getDgvalidationFactory();

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
         * {@link RuleImpl
         * <em>Rule</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
         *
         * @generated
         * @see RuleImpl
         * @see DocGenValidationPackageImpl#getRule()
         */
        EClass RULE = eINSTANCE.getRule();

        /**
         * The meta object literal for the '<em><b>Name</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         *
         * @generated
         */
        EAttribute RULE__NAME = eINSTANCE.getRule_Name();

        /**
         * The meta object literal for the '<em><b>Description</b></em>'
         * attribute feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         *
         * @generated
         */
        EAttribute RULE__DESCRIPTION = eINSTANCE.getRule_Description();

        /**
         * The meta object literal for the '<em><b>Severity</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         *
         * @generated
         */
        EAttribute RULE__SEVERITY = eINSTANCE.getRule_Severity();

        /**
         * The meta object literal for the '<em><b>Violations</b></em>'
         * containment reference list feature. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         *
         * @generated
         */
        EReference RULE__VIOLATIONS = eINSTANCE.getRule_Violations();

        /**
         * The meta object literal for the '
         * {@link ViolationImpl
         * <em>Violation</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc
         * -->
         *
         * @generated
         * @see ViolationImpl
         * @see DocGenValidationPackageImpl#getViolation()
         */
        EClass VIOLATION = eINSTANCE.getViolation();

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
        EAttribute VIOLATION__COMMENT = eINSTANCE.getViolation_Comment();

        /**
         * The meta object literal for the '
         * {@link SuiteImpl
         * <em>Suite</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
         *
         * @generated
         * @see SuiteImpl
         * @see DocGenValidationPackageImpl#getSuite()
         */
        EClass SUITE = eINSTANCE.getSuite();

        /**
         * The meta object literal for the '<em><b>Show Detail</b></em>'
         * attribute feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         *
         * @generated
         */
        EAttribute SUITE__SHOW_DETAIL = eINSTANCE.getSuite_ShowDetail();

        /**
         * The meta object literal for the '<em><b>Show Summary</b></em>'
         * attribute feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         *
         * @generated
         */
        EAttribute SUITE__SHOW_SUMMARY = eINSTANCE.getSuite_ShowSummary();

        /**
         * The meta object literal for the '<em><b>Own Section</b></em>'
         * attribute feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         *
         * @generated
         */
        EAttribute SUITE__OWN_SECTION = eINSTANCE.getSuite_OwnSection();

        /**
         * The meta object literal for the '<em><b>Name</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         *
         * @generated
         */
        EAttribute SUITE__NAME = eINSTANCE.getSuite_Name();

        /**
         * The meta object literal for the '<em><b>Rules</b></em>' containment
         * reference list feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         *
         * @generated
         */
        EReference SUITE__RULES = eINSTANCE.getSuite_Rules();

        /**
         * The meta object literal for the '
         * {@link Severity
         * <em>Severity</em>}' enum. <!-- begin-user-doc --> <!-- end-user-doc
         * -->
         *
         * @generated
         * @see Severity
         * @see DocGenValidationPackageImpl#getSeverity()
         */
        EEnum SEVERITY = eINSTANCE.getSeverity();

    }

} // DocGenValidationPackage
