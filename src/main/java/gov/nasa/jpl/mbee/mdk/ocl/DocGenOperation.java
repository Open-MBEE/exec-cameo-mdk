package gov.nasa.jpl.mbee.mdk.ocl;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EParameter;

import java.util.List;

/**
 * Simple interface defining what needs to be specified for a DocGenOperation used
 * to populate the OCL environment and evaluation environment
 *
 * @author cinyoung
 */
public interface DocGenOperation extends Comparable<DocGenOperation> {
    /**
     * Add a parameter argument to the custom operation
     *
     * @param parameter
     */
    void addParameter(EParameter parameter, EClassifier type);

    void addParameter(EParameter parameter);

    /**
     * Executes the operation
     *
     * @param source
     * @param args
     * @return
     */
    Object callOperation(Object source, Object[] args);

    /**
     * Checks if the internal operation name matches the external name
     *
     * @param operationName
     * @return
     */
    boolean checkOperationName(String operationName);

    String getAnnotationName();

    String getName();

    void setName(String name);

    List<EParameter> getParameters();

    void setAnnotationName(String annotationName);

    void setOperation(CallOperation operation);

    EClassifier getReturnType();

    void setReturnType(EClassifier classifier);

    EClassifier getCallerType();

    void setCallerType(EClassifier classifier);
}
