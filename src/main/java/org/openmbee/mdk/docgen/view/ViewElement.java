package org.openmbee.mdk.docgen.view;

import org.eclipse.emf.ecore.EObject;

/**
 * @author dlam
 * @model abstract="true"
 */
public interface ViewElement extends EObject {

    /**
     * @return
     * @model
     */
    String getId();

    /**
     * Sets the value of the '
     * {@link ViewElement#getId
     * <em>Id</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @param value the new value of the '<em>Id</em>' attribute.
     * @generated
     * @see #getId()
     */
    void setId(String value);

    /**
     * @return
     * @model
     */
    String getTitle();

    /**
     * Sets the value of the '
     * {@link ViewElement#getTitle
     * <em>Title</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @param value the new value of the '<em>Title</em>' attribute.
     * @generated
     * @see #getTitle()
     */
    void setTitle(String value);

    /**
     * @return
     * @model
     */
    String getFromElementId();

    /**
     * Sets the value of the '
     * {@link ViewElement#getFromElementId
     * <em>From Element Id</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @param value the new value of the '<em>From Element Id</em>' attribute.
     * @generated
     * @see #getFromElementId()
     */
    void setFromElementId(String value);

    /**
     * @return
     * @model
     */
    FromProperty getFromProperty();

    /**
     * Sets the value of the '
     * {@link ViewElement#getFromProperty
     * <em>From Property</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @param value the new value of the '<em>From Property</em>' attribute.
     * @generated
     * @see FromProperty
     * @see #getFromProperty()
     */
    void setFromProperty(FromProperty value);
}
