package gov.nasa.jpl.mbee.mdk.ocl;

import gov.nasa.jpl.mbee.mdk.lib.Debug;
import org.eclipse.emf.ecore.*;
import org.eclipse.ocl.EvaluationEnvironment;
import org.eclipse.ocl.ecore.EcoreEnvironmentFactory;
import org.eclipse.ocl.ecore.EcoreEvaluationEnvironment;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom class for customizing environment, allows many operations to be added
 * to evaluation context
 *
 * @author cinyoung
 */
public class DocGenEvaluationEnvironment extends EcoreEvaluationEnvironment {
    // keep track of all the added
    private List<DocGenOperation> docGenOperations = new ArrayList<DocGenOperation>();

    DocGenEvaluationEnvironment() {
        super();
    }

    /**
     * @param factory
     */
    public DocGenEvaluationEnvironment(EcoreEnvironmentFactory factory) {
        super(factory);
    }

    DocGenEvaluationEnvironment(
            EvaluationEnvironment<EClassifier, EOperation, EStructuralFeature, EClass, EObject> parent) {
        super(parent);
    }

    @Override
    public Object callOperation(EOperation operation, int opcode, Object source, Object[] args) {

        for (DocGenOperation op : docGenOperations) {
            if (operation.getEAnnotation(op.getAnnotationName()) != null) {
                if (op.getName().equals(operation.getName())
                        && op.getParameters().size() == (args == null ? 0 : args.length)) {
                    return op.callOperation(source, args);
                }
            }
        }

        return super.callOperation(operation, opcode, source, args);
    }

    public void addDgOperation(DocGenOperation docGenOperation) {
        docGenOperations.add(docGenOperation);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ocl.ecore.EcoreEvaluationEnvironment#getJavaMethodFor(org
     * .eclipse.emf.ecore.EOperation, java.lang.Object)
     * 
     * This is overridden because the EString EClass instance wasn't mapping
     * it's Java instance class to String.class as it should, resulting in a
     * NullPointer exception
     */
    @Override
    protected Method getJavaMethodFor(EOperation operation, Object receiver) {
        return getJavaMethodFor(operation, receiver, false);
    }

    protected Method getJavaMethodFor(EOperation operation, Object receiver, boolean recursing) {

        Method result = null;
        try {
            result = super.getJavaMethodFor(operation, receiver);
        } catch (Throwable e) {
            if (!recursing && operation != null && operation.getEContainingClass() != null) {
                EClass container = operation.getEContainingClass();
                Class<?> containerClass = container.getInstanceClass();
                if (containerClass == null) {
                    if (container.getName().startsWith("String")) {
                        container.setInstanceClass(String.class);
                    }
                    return getJavaMethodFor(operation, receiver, true);
                }
            }
            Debug.error(true, true, "Couldn't get java method for EOperation, " + operation);
        }

        return result;
    }

}
