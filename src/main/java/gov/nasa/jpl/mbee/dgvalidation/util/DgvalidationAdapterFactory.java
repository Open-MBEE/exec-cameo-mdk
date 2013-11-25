/**
 * <copyright> </copyright>
 * 
 * $Id$
 */
package gov.nasa.jpl.mbee.dgvalidation.util;

import gov.nasa.jpl.mbee.dgvalidation.DgvalidationPackage;
import gov.nasa.jpl.mbee.dgvalidation.Rule;
import gov.nasa.jpl.mbee.dgvalidation.Suite;
import gov.nasa.jpl.mbee.dgvalidation.Violation;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc --> The <b>Adapter Factory</b> for the model. It provides
 * an adapter <code>createXXX</code> method for each class of the model. <!--
 * end-user-doc -->
 * 
 * @see gov.nasa.jpl.mbee.dgvalidation.DgvalidationPackage
 * @generated
 */
public class DgvalidationAdapterFactory extends AdapterFactoryImpl {
    /**
     * The cached model package. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    protected static DgvalidationPackage modelPackage;

    /**
     * Creates an instance of the adapter factory. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     */
    public DgvalidationAdapterFactory() {
        if (modelPackage == null) {
            modelPackage = DgvalidationPackage.eINSTANCE;
        }
    }

    /**
     * Returns whether this factory is applicable for the type of the object.
     * <!-- begin-user-doc --> This implementation returns <code>true</code> if
     * the object is either the model's package or is an instance object of the
     * model. <!-- end-user-doc -->
     * 
     * @return whether this factory is applicable for the type of the object.
     * @generated
     */
    @Override
    public boolean isFactoryForType(Object object) {
        if (object == modelPackage) {
            return true;
        }
        if (object instanceof EObject) {
            return ((EObject)object).eClass().getEPackage() == modelPackage;
        }
        return false;
    }

    /**
     * The switch that delegates to the <code>createXXX</code> methods. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    protected DgvalidationSwitch<Adapter> modelSwitch = new DgvalidationSwitch<Adapter>() {
                                                          @Override
                                                          public Adapter caseRule(Rule object) {
                                                              return createRuleAdapter();
                                                          }

                                                          @Override
                                                          public Adapter caseViolation(Violation object) {
                                                              return createViolationAdapter();
                                                          }

                                                          @Override
                                                          public Adapter caseSuite(Suite object) {
                                                              return createSuiteAdapter();
                                                          }

                                                          @Override
                                                          public Adapter defaultCase(EObject object) {
                                                              return createEObjectAdapter();
                                                          }
                                                      };

    /**
     * Creates an adapter for the <code>target</code>. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @param target
     *            the object to adapt.
     * @return the adapter for the <code>target</code>.
     * @generated
     */
    @Override
    public Adapter createAdapter(Notifier target) {
        return modelSwitch.doSwitch((EObject)target);
    }

    /**
     * Creates a new adapter for an object of class '
     * {@link gov.nasa.jpl.mbee.dgvalidation.Rule <em>Rule</em>}'.
     * <!-- begin-user-doc --> This default implementation returns null so that
     * we can easily ignore cases; it's useful to ignore a case when inheritance
     * will catch all the cases anyway. <!-- end-user-doc -->
     * 
     * @return the new adapter.
     * @see gov.nasa.jpl.mbee.dgvalidation.Rule
     * @generated
     */
    public Adapter createRuleAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '
     * {@link gov.nasa.jpl.mbee.dgvalidation.Violation
     * <em>Violation</em>}'. <!-- begin-user-doc --> This default implementation
     * returns null so that we can easily ignore cases; it's useful to ignore a
     * case when inheritance will catch all the cases anyway. <!-- end-user-doc
     * -->
     * 
     * @return the new adapter.
     * @see gov.nasa.jpl.mbee.dgvalidation.Violation
     * @generated
     */
    public Adapter createViolationAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '
     * {@link gov.nasa.jpl.mbee.dgvalidation.Suite <em>Suite</em>}'.
     * <!-- begin-user-doc --> This default implementation returns null so that
     * we can easily ignore cases; it's useful to ignore a case when inheritance
     * will catch all the cases anyway. <!-- end-user-doc -->
     * 
     * @return the new adapter.
     * @see gov.nasa.jpl.mbee.dgvalidation.Suite
     * @generated
     */
    public Adapter createSuiteAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for the default case. <!-- begin-user-doc --> This
     * default implementation returns null. <!-- end-user-doc -->
     * 
     * @return the new adapter.
     * @generated
     */
    public Adapter createEObjectAdapter() {
        return null;
    }

} // DgvalidationAdapterFactory
