package gov.nasa.jpl.mbee.mdk.docgen.view;

/**
 * @author dlam
 * @model
 */
public interface ColSpec extends ViewElement {

    /**
     * @return
     * @model
     */
    String getColname();

    /**
     * Sets the value of the '
     * {@link ColSpec#getColname
     * <em>Colname</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     *
     * @param value the new value of the '<em>Colname</em>' attribute.
     * @generated
     * @see #getColname()
     */
    void setColname(String value);

    /**
     * @return
     * @model
     */
    String getColwidth();

    /**
     * Sets the value of the '
     * {@link ColSpec#getColwidth
     * <em>Colwidth</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     *
     * @param value the new value of the '<em>Colwidth</em>' attribute.
     * @generated
     * @see #getColwidth()
     */
    void setColwidth(String value);

    /**
     * @return
     * @model
     */
    int getColnum();

    /**
     * Sets the value of the '
     * {@link ColSpec#getColnum
     * <em>Colnum</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     *
     * @param value the new value of the '<em>Colnum</em>' attribute.
     * @generated
     * @see #getColnum()
     */
    void setColnum(int value);
}
