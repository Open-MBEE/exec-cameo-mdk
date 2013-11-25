package gov.nasa.jpl.ocl;

import java.util.List;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EParameter;

/**
 * Simple interface defining what needs to be specified for a DgOperation used
 * to populate the OCL environment and evaluation environment
 * 
 * @author cinyoung
 * 
 */
public interface DgOperation extends Comparable<DgOperation> {
    /**
     * Add a parameter argument to the custom operation
     * 
     * @param parameter
     */
    public void addParameter(EParameter parameter, EClassifier type);

    public void addParameter(EParameter parameter);

    /**
     * Executes the operation
     * 
     * @param source
     * @param args
     * @return
     */
    public Object callOperation(Object source, Object[] args);

    /**
     * Checks if the internal operation name matches the external name
     * 
     * @param operationName
     * @return
     */
    public boolean checkOperationName(String operationName);

    public String getAnnotationName();

    public String getName();

    public void setName(String name);

    public List<EParameter> getParameters();

    public void setAnnotationName(String annotationName);

    public void setOperation(CallOperation operation);

    public EClassifier getReturnType();

    public void setReturnType(EClassifier classifier);

    public EClassifier getCallerType();

    public void setCallerType(EClassifier classifier);
}
