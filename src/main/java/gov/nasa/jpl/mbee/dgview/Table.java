package gov.nasa.jpl.mbee.dgview;

import org.eclipse.emf.common.util.EList;

/**
 * @model
 * @author dlam
 * 
 */
public interface Table extends ViewElement {

    /**
     * @model
     * @return
     */
    EList<TableRow> getBody();

    /**
     * @model
     * @return
     */
    String getCaption();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mbee.dgview.Table#getCaption
     * <em>Caption</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @param value
     *            the new value of the '<em>Caption</em>' attribute.
     * @see #getCaption()
     * @generated
     */
    void setCaption(String value);

    /**
     * @model
     * @return
     */
    String getStyle();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mbee.dgview.Table#getStyle
     * <em>Style</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value
     *            the new value of the '<em>Style</em>' attribute.
     * @see #getStyle()
     * @generated
     */
    void setStyle(String value);

    /**
     * @model
     * @return
     */
    EList<TableRow> getHeaders();

    /**
     * @model
     * @return
     */
    EList<ColSpec> getColspecs();

    /**
     * @model
     * @return
     */
    int getCols();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mbee.dgview.Table#getCols <em>Cols</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value
     *            the new value of the '<em>Cols</em>' attribute.
     * @see #getCols()
     * @generated
     */
    void setCols(int value);

}
