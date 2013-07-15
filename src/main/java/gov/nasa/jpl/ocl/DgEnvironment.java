package gov.nasa.jpl.ocl;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EStructuralFeature;

import org.eclipse.ocl.Environment;
import org.eclipse.ocl.EnvironmentFactory;
import org.eclipse.ocl.ecore.CallOperationAction;
import org.eclipse.ocl.ecore.Constraint;
import org.eclipse.ocl.ecore.EcoreEnvironment;
import org.eclipse.ocl.ecore.SendSignalAction;

public class DgEnvironment extends EcoreEnvironment {
	Set<String> operationNames = new HashSet<String>();
	
	// this constructor is used to initialize the root environment
	@SuppressWarnings("deprecation")
	DgEnvironment(EPackage.Registry registry) {
		super(registry);
	}

	// this constructor is used to initialize child environments
	DgEnvironment(DgEnvironment parent) {
		super(parent);
	}

	public DgEnvironment(
			Environment<EPackage, EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint, EClass, EObject> createEnvironment) {
		super(createEnvironment);
	}

	// override this to provide visibility of the inherited protected method
	@SuppressWarnings("deprecation")
	@Override
	protected void setFactory(
			EnvironmentFactory<EPackage, EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint, EClass, EObject> factory) {
		super.setFactory(factory);
	}


	/**
	 * Utility for adding custom OCL operations (defined by a DgOperation)
	 * @param dgOperation
	 */
	@SuppressWarnings("deprecation")
	public void addDgOperation(DgOperation dgOperation) {
		// check that the operation has not already been added
		if (!operationNames.contains(dgOperation.getName())) {
			EOperation eoperation = EcoreFactory.eINSTANCE.createEOperation();
			eoperation.setName(dgOperation.getName());
			eoperation.setEType(getOCLStandardLibrary().getString());
			for (EParameter parm: dgOperation.getParameters()) {
				eoperation.getEParameters().add(parm);
			}
			EAnnotation annotation = EcoreFactory.eINSTANCE.createEAnnotation();
			annotation.setSource(dgOperation.getAnnotationName());
			eoperation.getEAnnotations().add(annotation);
			
			addOperation(getOCLStandardLibrary().getString(), eoperation);
			
			operationNames.add(dgOperation.getName());
		}
	}
}
