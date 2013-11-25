package gov.nasa.jpl.ocl;

/**
 * Following example from
 * http://help.eclipse.org/indigo/index.jsp?topic=%2Forg.eclipse
 * .ocl.doc%2Fhelp%2FCustomizingtheEnvironment.html
 * 
 * This allows customization of the EcoreEnvironment, need to be able to
 * register operations with this
 */

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.ocl.Environment;
import org.eclipse.ocl.EvaluationEnvironment;
import org.eclipse.ocl.ecore.CallOperationAction;
import org.eclipse.ocl.ecore.Constraint;
import org.eclipse.ocl.ecore.EcoreEnvironmentFactory;
import org.eclipse.ocl.ecore.SendSignalAction;

public class DgEnvironmentFactory extends EcoreEnvironmentFactory {
    private DgEnvironment           env;
    private DgEvaluationEnvironment evalEnv;

    /**
   * 
   */
    public DgEnvironmentFactory() {
        super();
    }

    /**
     * @param reg
     */
    public DgEnvironmentFactory(Registry reg) {
        super(reg);
    }

    @Override
    public Environment<EPackage, EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint, EClass, EObject> createEnvironment() {
        if (env == null) {
            env = new DgEnvironment(getEPackageRegistry());
            env.setFactory(this);
        }
        return env;
    }

    @Override
    public Environment<EPackage, EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint, EClass, EObject> createEnvironment(
            Environment<EPackage, EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint, EClass, EObject> parent) {
        if (!(parent instanceof DgEnvironment)) {
            throw new IllegalArgumentException("Parent environment must be DG environment: " + parent);
        }

        // just use the default environment
        if (env == null) {
            env = new DgEnvironment((DgEnvironment)parent);
        }
        env.setFactory(this);
        return env;
    }

    @Override
    public EvaluationEnvironment<EClassifier, EOperation, EStructuralFeature, EClass, EObject> createEvaluationEnvironment() {
        if (evalEnv == null) {
            evalEnv = new DgEvaluationEnvironment(this);
        }
        return evalEnv;
    }

    @Override
    public EvaluationEnvironment<EClassifier, EOperation, EStructuralFeature, EClass, EObject> createEvaluationEnvironment(
            EvaluationEnvironment<EClassifier, EOperation, EStructuralFeature, EClass, EObject> parent) {
        if (evalEnv == null) {
            evalEnv = new DgEvaluationEnvironment(parent);
        }
        return evalEnv;
    }

    public DgEnvironment getDgEnvironment() {
        if (env == null)
            env = (DgEnvironment)createEnvironment();
        return env;
    }

    public DgEvaluationEnvironment getDgEvaluationEnvironment() {
        if (evalEnv == null)
            evalEnv = (DgEvaluationEnvironment)createEvaluationEnvironment();
        return evalEnv;
    }

    public void reset() {
        env = null;
        evalEnv = null;
    }
}
