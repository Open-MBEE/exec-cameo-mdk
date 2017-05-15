package gov.nasa.jpl.mbee.mdk.docgen.view;

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
     * {@link Table#getCaption
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
     * {@link Table#getStyle
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
     * {@link Table#getCols <em>Cols</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @param value the new value of the '<em>Cols</em>' attribute.
     * @generated
     * @see #getCols()
     */
    void setCols(int value);

}
