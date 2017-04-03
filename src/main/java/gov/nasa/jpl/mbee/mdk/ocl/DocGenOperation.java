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
