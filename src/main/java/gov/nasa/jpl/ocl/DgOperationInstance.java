/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 * 
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.ocl;

import gov.nasa.jpl.mbee.lib.CompareUtils;
import gov.nasa.jpl.mbee.lib.MoreToString;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.ocl.ecore.internal.OCLStandardLibraryImpl;

public class DgOperationInstance implements DgOperation {
    private String           name;
    private String           annotationName;
    private CallOperation    operation;
    private List<EParameter> parameters = new ArrayList<EParameter>();
    private EClassifier      callerType, returnType;

    public DgOperationInstance() {
    }

    public DgOperationInstance(String name, String annotationName, DgEnvironmentFactory envFactory,
            CallOperation operation, EParameter... parameters) {
        this.name = name;
        this.annotationName = annotationName;
        this.operation = operation;
        for (EParameter ep: parameters) {
            addParameter(ep);
        }
        // this.parameters.addAll(Arrays.asList( parameters ) );
        addToEnvironment(envFactory);
    }

    public DgOperationInstance(String name, String annotationName, DgEnvironmentFactory envFactory,
            EClassifier callerType, EClassifier returnType, CallOperation operation, EParameter[] parameters) {
        this.name = name;
        this.annotationName = annotationName;
        this.operation = operation;
        this.callerType = callerType;
        this.returnType = returnType;
        for (EParameter ep: parameters) {
            addParameter(ep);
        }
        addToEnvironment(envFactory);
    }

    public DgOperationInstance(DgOperationInstance dgi, DgEnvironmentFactory envFactory) {

        this.name = dgi.name;
        this.annotationName = dgi.annotationName;
        this.operation = dgi.operation;
        this.callerType = dgi.callerType;
        this.returnType = dgi.returnType;
        for (EParameter ep: dgi.parameters) {
            addParameter(ep);
        }
        addToEnvironment(envFactory);
    }

    public static DgOperationInstance addOperation(DgOperationInstance dgi, DgEnvironmentFactory envFactory) {
        return new DgOperationInstance(dgi, envFactory);
    }

    public static DgOperationInstance addOperation(String name, String annotationName,
            DgEnvironmentFactory envFactory, CallOperation operation, EParameter... parameters) {
        return new DgOperationInstance(name, annotationName, envFactory, operation, parameters);

    }

    public static DgOperationInstance addOperation(String name, String annotationName,
            DgEnvironmentFactory envFactory, EClassifier callerType, EClassifier returnType,
            CallOperation operation, EParameter... parameters) {
        System.out.println( "addOperation(name=" + name + ", annotationName"
                            //+ annotationName 
                            + ", envFactory, callerType="
                            + callerType.getName() + ", returnType=" + returnType.getName()
                            + ", operation"// + operation
                            + ", parameters="
                            + MoreToString.Helper.toString( parameters ) + ")" );
        return new DgOperationInstance(name, annotationName, envFactory, callerType, returnType, operation,
                parameters);
    }

    /**
     * Add this operation to the environment through the EnvironemntFactory
     * 
     * @param envFactory
     * @param callOp
     */
    public void addToEnvironment(DgEnvironmentFactory envFactory) {
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
        if (name.equals(operationName)) {
            return true;
        }
        return false;
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
    public int compareTo(DgOperation o) {
        int compare = CompareUtils.compare(this, o);
        return compare;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getName() + "(");
        boolean first = true;
        for (EParameter p: parameters) {
            if (first)
                first = false;
            else
                sb.append(", ");
            sb.append(p.getName() + " : " + p.getEType());
        }
        sb.append(") : " + this.returnType);
        sb.append(" (" + this.callerType + ")");
        // sb.append( this.annotationName );
        return sb.toString();
    }

}
