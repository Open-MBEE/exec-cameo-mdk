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
package gov.nasa.jpl.mbee.mdk.dgview;

import org.eclipse.emf.ecore.EObject;

/**
 * @author dlam
 * @model abstract="true"
 */
public interface ViewElement extends EObject {

    /**
     * @return
     * @model
     */
    String getId();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mbee.mdk.dgview.ViewElement#getId
     * <em>Id</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @param value the new value of the '<em>Id</em>' attribute.
     * @generated
     * @see #getId()
     */
    void setId(String value);

    /**
     * @return
     * @model
     */
    String getTitle();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mbee.mdk.dgview.ViewElement#getTitle
     * <em>Title</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @param value the new value of the '<em>Title</em>' attribute.
     * @generated
     * @see #getTitle()
     */
    void setTitle(String value);

    /**
     * @return
     * @model
     */
    String getFromElementId();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mbee.mdk.dgview.ViewElement#getFromElementId
     * <em>From Element Id</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @param value the new value of the '<em>From Element Id</em>' attribute.
     * @generated
     * @see #getFromElementId()
     */
    void setFromElementId(String value);

    /**
     * @return
     * @model
     */
    FromProperty getFromProperty();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mbee.mdk.dgview.ViewElement#getFromProperty
     * <em>From Property</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @param value the new value of the '<em>From Property</em>' attribute.
     * @generated
     * @see gov.nasa.jpl.mbee.mdk.dgview.FromProperty
     * @see #getFromProperty()
     */
    void setFromProperty(FromProperty value);
}
