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

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EParameter;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.ocl.Environment;
import org.eclipse.ocl.EnvironmentFactory;
import org.eclipse.ocl.ecore.CallOperationAction;
import org.eclipse.ocl.ecore.Constraint;
import org.eclipse.ocl.ecore.EcoreEnvironment;
import org.eclipse.ocl.ecore.EcoreEnvironmentFactory;
import org.eclipse.ocl.ecore.SendSignalAction;
import org.eclipse.ocl.ecore.internal.OCLStandardLibraryImpl;

public class DgEnvironment extends EcoreEnvironment {
    Set<String>              operationNames = new HashSet<String>();
    Set<DgOperationInstance> operations     = new LinkedHashSet<DgOperationInstance>();

    // this constructor is used to initialize the root environment
    DgEnvironment(EPackage.Registry registry) {
        super(registry);
    }

    public DgEnvironment(EcoreEnvironmentFactory fac, Resource resource) {
        super(fac, resource);
    }

    // this constructor is used to initialize child environments
    DgEnvironment(DgEnvironment parent) {
        super(parent);
    }

    public DgEnvironment(
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
     * Utility for adding custom OCL operations (defined by a DgOperation)
     * 
     * @param dgOperation
     */
    public void addDgOperation(DgOperationInstance dgOperation) {
        // check that the operation has not already been added
        if (!operations.contains(dgOperation)) {
            EOperation eoperation = EcoreFactory.eINSTANCE.createEOperation();
            eoperation.setName(dgOperation.getName());
            EClassifier type = dgOperation.getReturnType();
            if (type == null)
                type = OCLStandardLibraryImpl.INSTANCE.getOclAny();
            eoperation.setEType(type);
            for (EParameter parm: dgOperation.getParameters()) {
                eoperation.getEParameters().add(parm);
            }
            EAnnotation annotation = EcoreFactory.eINSTANCE.createEAnnotation();
            annotation.setSource(dgOperation.getAnnotationName());
            eoperation.getEAnnotations().add(annotation);

            type = dgOperation.getCallerType();
            if (type == null)
                type = OCLStandardLibraryImpl.INSTANCE.getOclAny();
            try {
                if (dgOperation.getCallerType() == null) {
                    Debug.error(false, "Error! Null callerType for DgOperation " + dgOperation
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
