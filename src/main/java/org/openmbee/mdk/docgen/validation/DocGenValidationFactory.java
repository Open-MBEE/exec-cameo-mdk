package org.openmbee.mdk.docgen.validation;

import org.openmbee.mdk.docgen.validation.impl.DocGenValidationFactoryImpl;
import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc --> The <b>Factory</b> for the model. It provides a
 * create method for each non-abstract class of the model. <!-- end-user-doc -->
 *
 * @generated
 * @see DocGenValidationPackage
 */
public interface DocGenValidationFactory extends EFactory {
    /**
     * The singleton instance of the factory. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @generated
     */
    DocGenValidationFactory eINSTANCE = DocGenValidationFactoryImpl
            .init();

    /**
     * Returns a new object of class '<em>Rule</em>'. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @return a new object of class '<em>Rule</em>'.
     * @generated
     */
    Rule createRule();

    /**
     * Returns a new object of class '<em>Violation</em>'. <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     *
     * @return a new object of class '<em>Violation</em>'.
     * @generated
     */
    Violation createViolation();

    /**
     * Returns a new object of class '<em>Suite</em>'. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @return a new object of class '<em>Suite</em>'.
     * @generated
     */
    Suite createSuite();

    /**
     * Returns the package supported by this factory. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @return the package supported by this factory.
     * @generated
     */
    DocGenValidationPackage getDgvalidationPackage();

} // DocGenValidationFactory
