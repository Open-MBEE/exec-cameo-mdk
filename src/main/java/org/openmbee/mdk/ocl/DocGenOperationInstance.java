package org.openmbee.mdk.ocl;

import org.openmbee.mdk.util.CompareUtils;
import org.openmbee.mdk.util.Debug;
import org.openmbee.mdk.util.MoreToString;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.ocl.ecore.internal.OCLStandardLibraryImpl;

import java.util.ArrayList;
import java.util.List;

public class DocGenOperationInstance implements DocGenOperation {
    private String name;
    private String annotationName;
    private CallOperation operation;
    private List<EParameter> parameters = new ArrayList<EParameter>();
    private EClassifier callerType, returnType;

    public DocGenOperationInstance() {
    }

    public DocGenOperationInstance(String name, String annotationName, DocGenEnvironmentFactory envFactory,
                                   CallOperation operation, EParameter... parameters) {
        this.name = name;
        this.annotationName = annotationName;
        this.operation = operation;
        for (EParameter ep : parameters) {
            addParameter(ep);
        }
        // this.parameters.addAll(Arrays.asList( parameters ) );
        addToEnvironment(envFactory);
    }

    public DocGenOperationInstance(String name, String annotationName, DocGenEnvironmentFactory envFactory,
                                   EClassifier callerType, EClassifier returnType, CallOperation operation, EParameter[] parameters) {
        this.name = name;
        this.annotationName = annotationName;
        this.operation = operation;
        this.callerType = callerType;
        this.returnType = returnType;
        for (EParameter ep : parameters) {
            addParameter(ep);
        }
        addToEnvironment(envFactory);
    }

    public DocGenOperationInstance(DocGenOperationInstance dgi, DocGenEnvironmentFactory envFactory) {

        this.name = dgi.name;
        this.annotationName = dgi.annotationName;
        this.operation = dgi.operation;
        this.callerType = dgi.callerType;
        this.returnType = dgi.returnType;
        for (EParameter ep : dgi.parameters) {
            addParameter(ep);
        }
        addToEnvironment(envFactory);
    }

    public static DocGenOperationInstance addOperation(DocGenOperationInstance dgi, DocGenEnvironmentFactory envFactory) {
        return new DocGenOperationInstance(dgi, envFactory);
    }

    public static DocGenOperationInstance addOperation(String name, String annotationName,
                                                       DocGenEnvironmentFactory envFactory, CallOperation operation, EParameter... parameters) {
        return new DocGenOperationInstance(name, annotationName, envFactory, operation, parameters);

    }

    public static DocGenOperationInstance addOperation(String name, String annotationName,
                                                       DocGenEnvironmentFactory envFactory, EClassifier callerType, EClassifier returnType,
                                                       CallOperation operation, EParameter... parameters) {
        Debug.outln("addOperation(name=" + name
                + ", annotationName"
                // + annotationName
                + ", envFactory, callerType=" + callerType.getName()
                + ", returnType=" + returnType.getName()
                + ", operation"// + operation
                + ", parameters="
                + MoreToString.Helper.toString(parameters) + ")");
        return new DocGenOperationInstance(name, annotationName, envFactory, callerType, returnType, operation,
                parameters);
    }

    /**
     * Add this operation to the environment through the EnvironemntFactory
     *
     * @param envFactory
     * @param callOp
     */
    public void addToEnvironment(DocGenEnvironmentFactory envFactory) {
        // add custom operation to environment and evaluation environment
        envFactory.getDgEnvironment().addDgOperation(this);
        envFactory.getDgEvaluationEnvironment().addDgOperation(this);
    }

    public void addStringParameter(EParameter parameter) {
        addParameter(parameter, OCLStandardLibraryImpl.INSTANCE.getString());
    }

    @Override
    public void addParameter(EParameter parameter, EClassifier type) {
        parameter.setEType(type);
        this.parameters.add(parameter);
    }

    @Override
    public void addParameter(EParameter parameter) {
        this.parameters.add(parameter);
    }

    @Override
    public Object callOperation(Object source, Object[] args) {
        return operation.callOperation(source, args);
    }

    @Override
    public boolean checkOperationName(String operationName) {
        return name.equals(operationName);
    }

    @Override
    public String getAnnotationName() {
        return annotationName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<EParameter> getParameters() {
        return parameters;
    }

    @Override
    public void setAnnotationName(String annotationName) {
        this.annotationName = annotationName;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setOperation(CallOperation operation) {
        this.operation = operation;
    }

    @Override
    public EClassifier getReturnType() {
        return returnType;
    }

    @Override
    public EClassifier getCallerType() {
        return callerType;
    }

    @Override
    public void setCallerType(EClassifier callerType) {
        this.callerType = callerType;
    }

    @Override
    public void setReturnType(EClassifier returnType) {
        this.returnType = returnType;
    }

    @Override
    public int compareTo(DocGenOperation o) {
        int compare = CompareUtils.compare(this, o);
        return compare;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getName() + "(");
        boolean first = true;
        for (EParameter p : parameters) {
            if (first) {
                first = false;
            }
            else {
                sb.append(", ");
            }
            sb.append(p.getName() + " : " + p.getEType());
        }
        sb.append(") : " + this.returnType);
        sb.append(" (" + this.callerType + ")");
        // sb.append( this.annotationName );
        return sb.toString();
    }

}
