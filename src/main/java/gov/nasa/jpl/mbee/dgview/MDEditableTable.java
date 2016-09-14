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
 * <!-- begin-user-doc --> A representation of the model object '
 * <em><b>MD Editable Table</b></em>'. <!-- end-user-doc -->
 * <p>
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link gov.nasa.jpl.mbee.dgview.MDEditableTable#getPrecision
 * <em>Precision</em>}</li>
 * <li>
 * {@link gov.nasa.jpl.mbee.dgview.MDEditableTable#getGuiHeaders
 * <em>Gui Headers</em>}</li>
 * <li>{@link gov.nasa.jpl.mbee.dgview.MDEditableTable#getEditable
 * <em>Editable</em>}</li>
 * <li>{@link gov.nasa.jpl.mbee.dgview.MDEditableTable#getMergeCols
 * <em>Merge Cols</em>}</li>
 * <li>{@link gov.nasa.jpl.mbee.dgview.MDEditableTable#isAddLineNum
 * <em>Add Line Num</em>}</li>
 * <li>{@link gov.nasa.jpl.mbee.dgview.MDEditableTable#getGuiBody
 * <em>Gui Body</em>}</li>
 * </ul>
 * </p>
 *
 * @model
 * @generated
 * @see gov.nasa.jpl.mbee.dgview.DgviewPackage#getMDEditableTable()
 */
public interface MDEditableTable extends Table {
    /**
     * Returns the value of the '<em><b>Precision</b></em>' attribute. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Precision</em>' attribute isn't clear, there
     * really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>Precision</em>' attribute.
     * @model
     * @generated
     * @see #setPrecision(int)
     * @see gov.nasa.jpl.mbee.dgview.DgviewPackage#getMDEditableTable_Precision()
     */
    int getPrecision();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mbee.dgview.MDEditableTable#getPrecision
     * <em>Precision</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     *
     * @param value the new value of the '<em>Precision</em>' attribute.
     * @generated
     * @see #getPrecision()
     */
    void setPrecision(int value);

    /**
     * Returns the value of the '<em><b>Gui Headers</b></em>' attribute list.
     * The list contents are of type {@link java.lang.String}. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Gui Headers</em>' attribute list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>Gui Headers</em>' attribute list.
     * @model
     * @generated
     * @see gov.nasa.jpl.mbee.dgview.DgviewPackage#getMDEditableTable_GuiHeaders()
     */
    EList<String> getGuiHeaders();

    /**
     * Returns the value of the '<em><b>Editable</b></em>' attribute list. The
     * list contents are of type {@link java.lang.Boolean}. <!-- begin-user-doc
     * -->
     * <p>
     * If the meaning of the '<em>Editable</em>' attribute list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>Editable</em>' attribute list.
     * @model
     * @generated
     * @see gov.nasa.jpl.mbee.dgview.DgviewPackage#getMDEditableTable_Editable()
     */
    EList<Boolean> getEditable();

    /**
     * Returns the value of the '<em><b>Merge Cols</b></em>' attribute list. The
     * list contents are of type {@link java.lang.Integer}. <!-- begin-user-doc
     * -->
     * <p>
     * If the meaning of the '<em>Merge Cols</em>' attribute list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>Merge Cols</em>' attribute list.
     * @model
     * @generated
     * @see gov.nasa.jpl.mbee.dgview.DgviewPackage#getMDEditableTable_MergeCols()
     */
    EList<Integer> getMergeCols();

    /**
     * Returns the value of the '<em><b>Add Line Num</b></em>' attribute. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Add Line Num</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>Add Line Num</em>' attribute.
     * @model
     * @generated
     * @see #setAddLineNum(boolean)
     * @see gov.nasa.jpl.mbee.dgview.DgviewPackage#getMDEditableTable_AddLineNum()
     */
    boolean isAddLineNum();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mbee.dgview.MDEditableTable#isAddLineNum
     * <em>Add Line Num</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @param value the new value of the '<em>Add Line Num</em>' attribute.
     * @generated
     * @see #isAddLineNum()
     */
    void setAddLineNum(boolean value);

    /**
     * Returns the value of the '<em><b>Gui Body</b></em>' containment reference
     * list. The list contents are of type
     * {@link gov.nasa.jpl.mbee.dgview.TableRow}. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Gui Body</em>' containment reference list
     * isn't clear, there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>Gui Body</em>' containment reference list.
     * @model containment="true"
     * @generated
     * @see gov.nasa.jpl.mbee.dgview.DgviewPackage#getMDEditableTable_GuiBody()
     */
    EList<TableRow> getGuiBody();

} // MDEditableTable
