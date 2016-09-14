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

/**
 * Following example from
 * http://help.eclipse.org/indigo/index.jsp?topic=%2Forg.eclipse
 * .ocl.doc%2Fhelp%2FCustomizingtheEnvironment.html
 * <p>
 * This allows customization of the EcoreEnvironment, need to be able to
 * register operations with this
 */

import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.ocl.Environment;
import org.eclipse.ocl.EvaluationEnvironment;
import org.eclipse.ocl.ecore.CallOperationAction;
import org.eclipse.ocl.ecore.Constraint;
import org.eclipse.ocl.ecore.EcoreEnvironmentFactory;
import org.eclipse.ocl.ecore.SendSignalAction;

public class DgEnvironmentFactory extends EcoreEnvironmentFactory {
    private DgEnvironment env;
    private DgEvaluationEnvironment evalEnv;

    /**
     *
     */
    public DgEnvironmentFactory() {
        super();
    }

    /**
     * @param reg
     */
    public DgEnvironmentFactory(Registry reg) {
        super(reg);
    }

    @Override
    public Environment<EPackage, EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint, EClass, EObject> createEnvironment() {
        if (env == null) {
            env = new DgEnvironment(getEPackageRegistry());
            env.setFactory(this);
        }
        return env;
    }

    @Override
    public Environment<EPackage, EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint, EClass, EObject> createEnvironment(
            Environment<EPackage, EClassifier, EOperation, EStructuralFeature, EEnumLiteral, EParameter, EObject, CallOperationAction, SendSignalAction, Constraint, EClass, EObject> parent) {
        if (!(parent instanceof DgEnvironment)) {
            throw new IllegalArgumentException("Parent environment must be DG environment: " + parent);
        }

        // just use the default environment
        if (env == null) {
            env = new DgEnvironment((DgEnvironment) parent);
        }
        env.setFactory(this);
        return env;
    }

    @Override
    public EvaluationEnvironment<EClassifier, EOperation, EStructuralFeature, EClass, EObject> createEvaluationEnvironment() {
        if (evalEnv == null) {
            evalEnv = new DgEvaluationEnvironment(this);
        }
        return evalEnv;
    }

    @Override
    public EvaluationEnvironment<EClassifier, EOperation, EStructuralFeature, EClass, EObject> createEvaluationEnvironment(
            EvaluationEnvironment<EClassifier, EOperation, EStructuralFeature, EClass, EObject> parent) {
        if (evalEnv == null) {
            evalEnv = new DgEvaluationEnvironment(parent);
        }
        return evalEnv;
    }

    public DgEnvironment getDgEnvironment() {
        if (env == null) {
            env = (DgEnvironment) createEnvironment();
        }
        return env;
    }

    public DgEvaluationEnvironment getDgEvaluationEnvironment() {
        if (evalEnv == null) {
            evalEnv = (DgEvaluationEnvironment) createEvaluationEnvironment();
        }
        return evalEnv;
    }

    public void reset() {
        env = null;
        evalEnv = null;
    }
}
