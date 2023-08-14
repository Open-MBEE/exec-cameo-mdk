package org.openmbee.mdk.docgen.view;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc --> A representation of the model object '
 * <em><b>MD Editable Table</b></em>'. <!-- end-user-doc -->
 * <p>
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link MDEditableTable#getPrecision
 * <em>Precision</em>}</li>
 * <li>
 * {@link MDEditableTable#getGuiHeaders
 * <em>Gui Headers</em>}</li>
 * <li>{@link MDEditableTable#getEditable
 * <em>Editable</em>}</li>
 * <li>{@link MDEditableTable#getMergeCols
 * <em>Merge Cols</em>}</li>
 * <li>{@link MDEditableTable#isAddLineNum
 * <em>Add Line Num</em>}</li>
 * <li>{@link MDEditableTable#getGuiBody
 * <em>Gui Body</em>}</li>
 * </ul>
 * </p>
 *
 * @model
 * @generated
 * @see DocGenViewPackage#getMDEditableTable()
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
     * @see DocGenViewPackage#getMDEditableTable_Precision()
     */
    int getPrecision();

    /**
     * Sets the value of the '
     * {@link MDEditableTable#getPrecision
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
     * @see DocGenViewPackage#getMDEditableTable_GuiHeaders()
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
     * @see DocGenViewPackage#getMDEditableTable_Editable()
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
     * @see DocGenViewPackage#getMDEditableTable_MergeCols()
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
     * @see DocGenViewPackage#getMDEditableTable_AddLineNum()
     */
    boolean isAddLineNum();

    /**
     * Sets the value of the '
     * {@link MDEditableTable#isAddLineNum
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
     * {@link TableRow}. <!--
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
     * @see DocGenViewPackage#getMDEditableTable_GuiBody()
     */
    EList<TableRow> getGuiBody();

} // MDEditableTable
