package gov.nasa.jpl.mbee.mdk.docgen.view;

/**
 * @author dlam
 * @model
 */
public interface TableEntry extends HasContent {
    /**
     * @return
     * @model
     */
    int getMorerows();

    /**
     * Sets the value of the '
     * {@link TableEntry#getMorerows
     * <em>Morerows</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     *
     * @param value the new value of the '<em>Morerows</em>' attribute.
     * @generated
     * @see #getMorerows()
     */
    void setMorerows(int value);

    /**
     * @return
     * @model
     */
    String getNamest();

    /**
     * Sets the value of the '
     * {@link TableEntry#getNamest
     * <em>Namest</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     *
     * @param value the new value of the '<em>Namest</em>' attribute.
     * @generated
     * @see #getNamest()
     */
    void setNamest(String value);

    /**
     * @return
     * @model
     */
    String getNameend();

    /**
     * Sets the value of the '
     * {@link TableEntry#getNameend
     * <em>Nameend</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     *
     * @param value the new value of the '<em>Nameend</em>' attribute.
     * @generated
     * @see #getNameend()
     */
    void setNameend(String value);
}
