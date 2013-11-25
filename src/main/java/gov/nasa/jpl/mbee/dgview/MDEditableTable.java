/**
 * <copyright> </copyright>
 * 
 * $Id$
 */
package gov.nasa.jpl.mbee.dgview;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc --> A representation of the model object '
 * <em><b>MD Editable Table</b></em>'. <!-- end-user-doc -->
 * 
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
 * @see gov.nasa.jpl.mbee.dgview.DgviewPackage#getMDEditableTable()
 * @model
 * @generated
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
     * @see #setPrecision(int)
     * @see gov.nasa.jpl.mbee.dgview.DgviewPackage#getMDEditableTable_Precision()
     * @model
     * @generated
     */
    int getPrecision();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mbee.dgview.MDEditableTable#getPrecision
     * <em>Precision</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @param value
     *            the new value of the '<em>Precision</em>' attribute.
     * @see #getPrecision()
     * @generated
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
     * @see gov.nasa.jpl.mbee.dgview.DgviewPackage#getMDEditableTable_GuiHeaders()
     * @model
     * @generated
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
     * @see gov.nasa.jpl.mbee.dgview.DgviewPackage#getMDEditableTable_Editable()
     * @model
     * @generated
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
     * @see gov.nasa.jpl.mbee.dgview.DgviewPackage#getMDEditableTable_MergeCols()
     * @model
     * @generated
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
     * @see #setAddLineNum(boolean)
     * @see gov.nasa.jpl.mbee.dgview.DgviewPackage#getMDEditableTable_AddLineNum()
     * @model
     * @generated
     */
    boolean isAddLineNum();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mbee.dgview.MDEditableTable#isAddLineNum
     * <em>Add Line Num</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @param value
     *            the new value of the '<em>Add Line Num</em>' attribute.
     * @see #isAddLineNum()
     * @generated
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
     * @see gov.nasa.jpl.mbee.dgview.DgviewPackage#getMDEditableTable_GuiBody()
     * @model containment="true"
     * @generated
     */
    EList<TableRow> getGuiBody();

} // MDEditableTable
