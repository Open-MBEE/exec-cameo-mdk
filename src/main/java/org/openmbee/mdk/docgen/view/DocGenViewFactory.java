package org.openmbee.mdk.docgen.view;

import org.openmbee.mdk.docgen.view.impl.DocGenViewFactoryImpl;
import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc --> The <b>Factory</b> for the model. It provides a
 * create method for each non-abstract class of the model. <!-- end-user-doc -->
 *
 * @generated
 * @see DocGenViewPackage
 */
public interface DocGenViewFactory extends EFactory {
    /**
     * The singleton instance of the factory. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @generated
     */
    DocGenViewFactory eINSTANCE = DocGenViewFactoryImpl.init();

    /**
     * Returns a new object of class '<em>Col Spec</em>'. <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     *
     * @return a new object of class '<em>Col Spec</em>'.
     * @generated
     */
    ColSpec createColSpec();

    /**
     * Returns a new object of class '<em>Image</em>'. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @return a new object of class '<em>Image</em>'.
     * @generated
     */
    Image createImage();

    /**
     * Returns a new object of class '<em>List</em>'. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @return a new object of class '<em>List</em>'.
     * @generated
     */
    List createList();

    /**
     * Returns a new object of class '<em>List Item</em>'. <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     *
     * @return a new object of class '<em>List Item</em>'.
     * @generated
     */
    ListItem createListItem();

    /**
     * Returns a new object of class '<em>Paragraph</em>'. <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     *
     * @return a new object of class '<em>Paragraph</em>'.
     * @generated
     */
    Paragraph createParagraph();

    /**
     * Returns a new object of class '<em>Table</em>'. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @return a new object of class '<em>Table</em>'.
     * @generated
     */
    Table createTable();

    /**
     * Returns a new object of class '<em>Table Entry</em>'. <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     *
     * @return a new object of class '<em>Table Entry</em>'.
     * @generated
     */
    TableEntry createTableEntry();

    /**
     * Returns a new object of class '<em>Text</em>'. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @return a new object of class '<em>Text</em>'.
     * @generated
     */
    Text createText();

    /**
     * Returns a new object of class '<em>Table Row</em>'. <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     *
     * @return a new object of class '<em>Table Row</em>'.
     * @generated
     */
    TableRow createTableRow();

    /**
     * Returns a new object of class '<em>MD Editable Table</em>'. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @return a new object of class '<em>MD Editable Table</em>'.
     * @generated
     */
    MDEditableTable createMDEditableTable();

    /**
     * Returns the package supported by this factory. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @return the package supported by this factory.
     * @generated
     */
    DocGenViewPackage getDgviewPackage();

} // DocGenViewFactory
