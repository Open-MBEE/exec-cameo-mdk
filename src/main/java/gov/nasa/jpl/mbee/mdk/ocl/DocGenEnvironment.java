package gov.nasa.jpl.mbee.mdk.ocl;

import gov.nasa.jpl.mbee.mdk.lib.Debug;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.ocl.Environment;
import org.eclipse.ocl.EnvironmentFactory;
import org.eclipse.ocl.ecore.*;
import org.eclipse.ocl.ecore.internal.OCLStandardLibraryImpl;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class DocGenEnvironment extends EcoreEnvironment {
    Set<String> operationNames = new HashSet<String>();
    Set<DocGenOperationInstance> operations = new LinkedHashSet<DocGenOperationInstance>();

    // this constructor is used to initialize the root environment
    DocGenEnvironment(EPackage.Registry registry) {
        super(registry);
    }

    public DocGenEnvironment(EcoreEnvironmentFactory fac, Resource resource) {
        super(fac, resource);
    }

    // this constructor is used to initialize child environments
    DocGenEnvironment(DocGenEnvironment parent) {
        super(parent);
    }

    public DocGenEnvironment(
            Environment<EPackage, EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint, EClass, EObject> createEnvironment) {
        super(createEnvironment);
    }

    // override this to provide visibility of the inherited protected method
    @Override
    protected void setFactory(
            EnvironmentFactory<EPackage, EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint, EClass, EObject> factory) {
        super.setFactory(factory);
    }

    // override this to provide visibility of the inherited protected method
    @Override
    public EnvironmentFactory<EPackage, EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint, EClass, EObject> getFactory() {
        return super.getFactory();
    }

    /**
     * Utility for adding custom OCL operations (defined by a DocGenOperation)
     *
     * @param dgOperation
     */
    public void addDgOperation(DocGenOperationInstance dgOperation) {
        // check that the operation has not already been added
        if (!operations.contains(dgOperation)) {
            EOperation eoperation = EcoreFactory.eINSTANCE.createEOperation();
            eoperation.setName(dgOperation.getName());
            EClassifier type = dgOperation.getReturnType();
            if (type == null) {
                type = OCLStandardLibraryImpl.INSTANCE.getOclAny();
            }
            eoperation.setEType(type);
            for (EParameter parm : dgOperation.getParameters()) {
                eoperation.getEParameters().add(parm);
            }
            EAnnotation annotation = EcoreFactory.eINSTANCE.createEAnnotation();
            annotation.setSource(dgOperation.getAnnotationName());
            eoperation.getEAnnotations().add(annotation);

            type = dgOperation.getCallerType();
            if (type == null) {
                type = OCLStandardLibraryImpl.INSTANCE.getOclAny();
            }
            try {
                if (dgOperation.getCallerType() == null) {
                    Debug.error(false, "Error! Null callerType for DocGenOperation " + dgOperation
                            + "! Setting to OclAny.");
                    dgOperation.setCallerType(OCLStandardLibraryImpl.INSTANCE.getOclAny());
                }
                addHelperOperation(dgOperation.getCallerType(), eoperation);
                operationNames.add(dgOperation.getName());
                operations.add(dgOperation);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
