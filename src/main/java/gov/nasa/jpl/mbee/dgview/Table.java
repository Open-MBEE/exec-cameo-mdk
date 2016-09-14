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
package gov.nasa.jpl.mbee.dgview;

import org.eclipse.emf.common.util.EList;

/**
 * @author dlam
 * @model
 */
public interface Table extends ViewElement {

    /**
     * @return
     * @model
     */
    EList<TableRow> getBody();

    /**
     * @return
     * @model
     */
    String getCaption();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mbee.dgview.Table#getCaption
     * <em>Caption</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     *
     * @param value the new value of the '<em>Caption</em>' attribute.
     * @generated
     * @see #getCaption()
     */
    void setCaption(String value);

    /**
     * @return
     * @model
     */
    String getStyle();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mbee.dgview.Table#getStyle
     * <em>Style</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @param value the new value of the '<em>Style</em>' attribute.
     * @generated
     * @see #getStyle()
     */
    void setStyle(String value);

    /**
     * @return
     * @model
     */
    EList<TableRow> getHeaders();

    /**
     * @return
     * @model
     */
    EList<ColSpec> getColspecs();

    /**
     * @return
     * @model
     */
    int getCols();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mbee.dgview.Table#getCols <em>Cols</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @param value the new value of the '<em>Cols</em>' attribute.
     * @generated
     * @see #getCols()
     */
    void setCols(int value);

}
