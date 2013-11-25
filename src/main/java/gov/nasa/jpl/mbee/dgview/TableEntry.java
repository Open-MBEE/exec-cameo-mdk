package gov.nasa.jpl.mbee.dgview;

/**
 * @model
 * @author dlam
 * 
 */
public interface TableEntry extends HasContent {
    /**
     * @model
     * @return
     */
    int getMorerows();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mbee.dgview.TableEntry#getMorerows
     * <em>Morerows</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @param value
     *            the new value of the '<em>Morerows</em>' attribute.
     * @see #getMorerows()
     * @generated
     */
    void setMorerows(int value);

    /**
     * @model
     * @return
     */
    String getNamest();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mbee.dgview.TableEntry#getNamest
     * <em>Namest</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @param value
     *            the new value of the '<em>Namest</em>' attribute.
     * @see #getNamest()
     * @generated
     */
    void setNamest(String value);

    /**
     * @model
     * @return
     */
    String getNameend();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mbee.dgview.TableEntry#getNameend
     * <em>Nameend</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @param value
     *            the new value of the '<em>Nameend</em>' attribute.
     * @see #getNameend()
     * @generated
     */
    void setNameend(String value);
}
