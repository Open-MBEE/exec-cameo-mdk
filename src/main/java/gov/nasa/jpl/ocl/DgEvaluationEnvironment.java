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

import gov.nasa.jpl.mbee.lib.Debug;
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
public class DgEvaluationEnvironment extends EcoreEvaluationEnvironment {
    // keep track of all the added
    private List<DgOperation> dgOperations = new ArrayList<DgOperation>();

    DgEvaluationEnvironment() {
        super();
    }

    /**
     * @param factory
     */
    public DgEvaluationEnvironment(EcoreEnvironmentFactory factory) {
        super(factory);
    }

    DgEvaluationEnvironment(
            EvaluationEnvironment<EClassifier, EOperation, EStructuralFeature, EClass, EObject> parent) {
        super(parent);
    }

    @Override
    public Object callOperation(EOperation operation, int opcode, Object source, Object[] args) {

        for (DgOperation op : dgOperations) {
            if (operation.getEAnnotation(op.getAnnotationName()) != null) {
                if (op.getName().equals(operation.getName())
                        && op.getParameters().size() == (args == null ? 0 : args.length)) {
                    return op.callOperation(source, args);
                }
            }
        }

        return super.callOperation(operation, opcode, source, args);
    }

    public void addDgOperation(DgOperation dgOperation) {
        dgOperations.add(dgOperation);
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
