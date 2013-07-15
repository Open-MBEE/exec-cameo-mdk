package gov.nasa.jpl.ocl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.ocl.EvaluationEnvironment;
import org.eclipse.ocl.ecore.EcoreEvaluationEnvironment;

/**
 * Custom class for customizing environment, allows many operations to be added to evaluation context
 * @author cinyoung
 *
 */
public class DgEvaluationEnvironment extends EcoreEvaluationEnvironment {
	// keep track of all the added 
	private List<DgOperation> dgOperations = new ArrayList<DgOperation>();
	
	@SuppressWarnings("deprecation")
	DgEvaluationEnvironment() {
		super();
	}

	DgEvaluationEnvironment(
			EvaluationEnvironment<EClassifier, EOperation, EStructuralFeature, EClass, EObject> parent) {
		super(parent);
	}

	public Object callOperation(EOperation operation, int opcode,
			Object source, Object[] args) {
		boolean isAnnotationFound = false;

		for (DgOperation op: dgOperations) {
			if (operation.getEAnnotation(op.getAnnotationName()) != null) {
				if (op.getName().equals(operation.getName())) {
					return op.callOperation(source, args);
				}
			}
		}
		
		if (!isAnnotationFound) {
			return super.callOperation(operation, opcode, source, args);
		}
		
		throw new UnsupportedOperationException(); // unknown operation
	}
	
	public void addDgOperation(DgOperation dgOperation) {
		dgOperations.add(dgOperation);
	}
}
