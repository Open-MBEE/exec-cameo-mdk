package gov.nasa.jpl.ocl;

/** 
 * Following example from http://help.eclipse.org/indigo/index.jsp?topic=%2Forg.eclipse.ocl.doc%2Fhelp%2FCustomizingtheEnvironment.html
 * 
 * This allows customization of the EcoreEnvironment, need to be able to register operations with this
 */

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.ocl.Environment;
import org.eclipse.ocl.EvaluationEnvironment;
import org.eclipse.ocl.ecore.CallOperationAction;
import org.eclipse.ocl.ecore.Constraint;
import org.eclipse.ocl.ecore.EcoreEnvironmentFactory;
import org.eclipse.ocl.ecore.SendSignalAction;

public class DgEnvironmentFactory extends EcoreEnvironmentFactory {
	private static DgEnvironment			env = new DgEnvironment(EcoreEnvironmentFactory.INSTANCE.createEnvironment());
	private static DgEvaluationEnvironment	evalEnv = new DgEvaluationEnvironment();
	
	public Environment<EPackage, EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint, EClass, EObject> createEnvironment() {
		DgEnvironment result = new DgEnvironment(getEPackageRegistry());
		result.setFactory(this);
		return result;
	}

	public Environment<EPackage, EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint, EClass, EObject> createEnvironment(
			Environment<EPackage, EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint, EClass, EObject> parent) {
		if (!(parent instanceof DgEnvironment)) {
			throw new IllegalArgumentException(
					"Parent environment must be DG environment: " + parent);
		}

		// just use the default environment
//		env = new DgEnvironment((DgEnvironment) parent);
		env.setFactory(this);
		return env;
	}

	public EvaluationEnvironment<EClassifier, EOperation, EStructuralFeature, EClass, EObject> createEvaluationEnvironment() {
		return evalEnv;
	}

	public EvaluationEnvironment<EClassifier, EOperation, EStructuralFeature, EClass, EObject> createEvaluationEnvironment(
			EvaluationEnvironment<EClassifier, EOperation, EStructuralFeature, EClass, EObject> parent) {
		return new DgEvaluationEnvironment(parent);
	}
	
	public DgEnvironment getDgEnvironment() {
		return env;
	}
	
	public DgEvaluationEnvironment getDgEvaluationEnvironment() {
		return evalEnv;
	}
}
