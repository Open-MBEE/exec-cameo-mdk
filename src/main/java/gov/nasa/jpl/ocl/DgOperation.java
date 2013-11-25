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
