package gov.nasa.jpl.mgss.mbee.docgen.dgview;

/**
 * @model
 * @author dlam
 */
public interface List extends HasContent {

    /**
     * @model
     * @return
     */
    boolean isOrdered();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mgss.mbee.docgen.dgview.List#isOrdered
     * <em>Ordered</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @param value
     *            the new value of the '<em>Ordered</em>' attribute.
     * @see #isOrdered()
     * @generated
     */
    void setOrdered(boolean value);
}
