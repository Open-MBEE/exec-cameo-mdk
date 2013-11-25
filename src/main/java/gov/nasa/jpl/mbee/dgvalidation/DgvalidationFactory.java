/**
 * <copyright> </copyright>
 * 
 * $Id$
 */
package gov.nasa.jpl.mbee.dgvalidation;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc --> The <b>Factory</b> for the model. It provides a
 * create method for each non-abstract class of the model. <!-- end-user-doc -->
 * 
 * @see gov.nasa.jpl.mbee.dgvalidation.DgvalidationPackage
 * @generated
 */
public interface DgvalidationFactory extends EFactory {
    /**
     * The singleton instance of the factory. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     */
    DgvalidationFactory eINSTANCE = gov.nasa.jpl.mbee.dgvalidation.impl.DgvalidationFactoryImpl
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
    DgvalidationPackage getDgvalidationPackage();

} // DgvalidationFactory
