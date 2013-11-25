package gov.nasa.jpl.mbee.dgview;

/**
 * @model
 * @author dlam
 * 
 */
public interface ColSpec extends ViewElement {

    /**
     * @model
     * @return
     */
    String getColname();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mbee.dgview.ColSpec#getColname
     * <em>Colname</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @param value
     *            the new value of the '<em>Colname</em>' attribute.
     * @see #getColname()
     * @generated
     */
    void setColname(String value);

    /**
     * @model
     * @return
     */
    String getColwidth();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mbee.dgview.ColSpec#getColwidth
     * <em>Colwidth</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @param value
     *            the new value of the '<em>Colwidth</em>' attribute.
     * @see #getColwidth()
     * @generated
     */
    void setColwidth(String value);

    /**
     * @model
     * @return
     */
    int getColnum();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mbee.dgview.ColSpec#getColnum
     * <em>Colnum</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @param value
     *            the new value of the '<em>Colnum</em>' attribute.
     * @see #getColnum()
     * @generated
     */
    void setColnum(int value);
}
