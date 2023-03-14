package org.openmbee.mdk.ocl;

/**
 * Following example from
 * http://help.eclipse.org/indigo/index.jsp?topic=%2Forg.eclipse
 * .ocl.doc%2Fhelp%2FCustomizingtheEnvironment.html
 * <p>
 * This allows customization of the EcoreEnvironment, need to be able to
 * register operations with this
 */

import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.ocl.Environment;
import org.eclipse.ocl.EvaluationEnvironment;
import org.eclipse.ocl.ecore.CallOperationAction;
import org.eclipse.ocl.ecore.Constraint;
import org.eclipse.ocl.ecore.EcoreEnvironmentFactory;
import org.eclipse.ocl.ecore.SendSignalAction;

public class DocGenEnvironmentFactory extends EcoreEnvironmentFactory {
    private DocGenEnvironment env;
    private DocGenEvaluationEnvironment evalEnv;

    /**
     *
     */
    public DocGenEnvironmentFactory() {
        super();
    }

    /**
     * @param reg
     */
    public DocGenEnvironmentFactory(Registry reg) {
        super(reg);
    }

    @Override
    public Environment<EPackage, EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint, EClass, EObject> createEnvironment() {
        if (env == null) {
            env = new DocGenEnvironment(getEPackageRegistry());
            env.setFactory(this);
        }
        return env;
    }

    @Override
    public Environment<EPackage, EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint, EClass, EObject> createEnvironment(
            Environment<EPackage, EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint, EClass, EObject> parent) {
        if (!(parent instanceof DocGenEnvironment)) {
            throw new IllegalArgumentException("Parent environment must be DG environment: " + parent);
        }

        // just use the default environment
        if (env == null) {
            env = new DocGenEnvironment((DocGenEnvironment) parent);
        }
        env.setFactory(this);
        return env;
    }

    @Override
    public EvaluationEnvironment<EClassifier, EOperation, EStructuralFeature, EClass, EObject> createEvaluationEnvironment() {
        if (evalEnv == null) {
            evalEnv = new DocGenEvaluationEnvironment(this);
        }
        return evalEnv;
    }

    @Override
    public EvaluationEnvironment<EClassifier, EOperation, EStructuralFeature, EClass, EObject> createEvaluationEnvironment(
            EvaluationEnvironment<EClassifier, EOperation, EStructuralFeature, EClass, EObject> parent) {
        if (evalEnv == null) {
            evalEnv = new DocGenEvaluationEnvironment(parent);
        }
        return evalEnv;
    }

    public DocGenEnvironment getDgEnvironment() {
        if (env == null) {
            env = (DocGenEnvironment) createEnvironment();
        }
        return env;
    }

    public DocGenEvaluationEnvironment getDgEvaluationEnvironment() {
        if (evalEnv == null) {
            evalEnv = (DocGenEvaluationEnvironment) createEvaluationEnvironment();
        }
        return evalEnv;
    }

    public void reset() {
        env = null;
        evalEnv = null;
    }
}
