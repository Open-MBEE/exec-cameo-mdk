package gov.nasa.jpl.mbee.mdk.docgen.validation;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.Switch;

/**
 * <!-- begin-user-doc --> The <b>Switch</b> for the model's inheritance
 * hierarchy. It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object and proceeding up the
 * inheritance hierarchy until a non-null result is returned, which is the
 * result of the switch. <!-- end-user-doc -->
 *
 * @generated
 * @see DocGenValidationPackage
 */
public class DocGenValidationSwitch<T> extends Switch<T> {
    /**
     * The cached model package <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    protected static DocGenValidationPackage modelPackage;

    /**
     * Creates an instance of the switch. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @generated
     */
    public DocGenValidationSwitch() {
        if (modelPackage == null) {
            modelPackage = DocGenValidationPackage.eINSTANCE;
        }
    }

    /**
     * Checks whether this is a switch for the given package. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @return whether this is a switch for the given package.
     * @parameter ePackage the package in question.
     * @generated
     */
    @Override
    protected boolean isSwitchFor(EPackage ePackage) {
        return ePackage == modelPackage;
    }

    /**
     * Calls <code>caseXXX</code> for each class of the model until one returns
     * a non null result; it yields that result. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @return the first non-null result returned by a <code>caseXXX</code>
     * call.
     * @generated
     */
    @Override
    protected T doSwitch(int classifierID, EObject theEObject) {
        switch (classifierID) {
            case DocGenValidationPackage.RULE: {
                Rule rule = (Rule) theEObject;
                T result = caseRule(rule);
                if (result == null) {
                    result = defaultCase(theEObject);
                }
                return result;
            }
            case DocGenValidationPackage.VIOLATION: {
                Violation violation = (Violation) theEObject;
                T result = caseViolation(violation);
                if (result == null) {
                    result = defaultCase(theEObject);
                }
                return result;
            }
            case DocGenValidationPackage.SUITE: {
                Suite suite = (Suite) theEObject;
                T result = caseSuite(suite);
                if (result == null) {
                    result = defaultCase(theEObject);
                }
                return result;
            }
            default:
                return defaultCase(theEObject);
        }
    }

    /**
     * Returns the result of interpreting the object as an instance of '
     * <em>Rule</em>'. <!-- begin-user-doc --> This implementation returns null;
     * returning a non-null result will terminate the switch. <!-- end-user-doc
     * -->
     *
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '
     * <em>Rule</em>'.
     * @generated
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     */
    public T caseRule(Rule object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '
     * <em>Violation</em>'. <!-- begin-user-doc --> This implementation returns
     * null; returning a non-null result will terminate the switch. <!--
     * end-user-doc -->
     *
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '
     * <em>Violation</em>'.
     * @generated
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     */
    public T caseViolation(Violation object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '
     * <em>Suite</em>'. <!-- begin-user-doc --> This implementation returns
     * null; returning a non-null result will terminate the switch. <!--
     * end-user-doc -->
     *
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '
     * <em>Suite</em>'.
     * @generated
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     */
    public T caseSuite(Suite object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '
     * <em>EObject</em>'. <!-- begin-user-doc --> This implementation returns
     * null; returning a non-null result will terminate the switch, but this is
     * the last case anyway. <!-- end-user-doc -->
     *
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '
     * <em>EObject</em>'.
     * @generated
     * @see #doSwitch(org.eclipse.emf.ecore.EObject)
     */
    @Override
    public T defaultCase(EObject object) {
        return null;
    }

} // DocGenValidationSwitch
