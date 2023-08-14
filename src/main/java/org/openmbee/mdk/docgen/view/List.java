package org.openmbee.mdk.docgen.view;

/**
 * @author dlam
 * @model
 */
public interface List extends HasContent {

    /**
     * @return
     * @model
     */
    boolean isOrdered();

    /**
     * Sets the value of the '
     * {@link List#isOrdered
     * <em>Ordered</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     *
     * @param value the new value of the '<em>Ordered</em>' attribute.
     * @generated
     * @see #isOrdered()
     */
    void setOrdered(boolean value);
}
