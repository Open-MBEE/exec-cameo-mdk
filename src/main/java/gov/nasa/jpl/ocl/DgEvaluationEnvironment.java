package gov.nasa.jpl.ocl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.ocl.EvaluationEnvironment;
import org.eclipse.ocl.ecore.EcoreEnvironmentFactory;
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

	/**
   * @param factory
   */
  public DgEvaluationEnvironment( EcoreEnvironmentFactory factory ) {
    super( factory );
  }

  DgEvaluationEnvironment(
			EvaluationEnvironment<EClassifier, EOperation, EStructuralFeature, EClass, EObject> parent) {
		super(parent);
	}

	public Object callOperation(EOperation operation, int opcode,
			Object source, Object[] args) {

		for (DgOperation op: dgOperations) {
			if (operation.getEAnnotation(op.getAnnotationName()) != null) {
        if ( op.getName().equals( operation.getName() )
             && op.getParameters().size() == ( args == null ? 0 : args.length ) ) {
					return op.callOperation(source, args);
				}
			}
		}
		
		return super.callOperation(operation, opcode, source, args);
	}
	
	public void addDgOperation(DgOperation dgOperation) {
		dgOperations.add(dgOperation);
	}
}
