package gov.nasa.jpl.mgss.mbee.docgen.dgview;

import org.eclipse.emf.ecore.EObject;

/**
 * @model abstract="true"
 * @author dlam
 * 
 */
public interface ViewElement extends EObject {

    /**
     * @model
     * @return
     */
    String getId();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mgss.mbee.docgen.dgview.ViewElement#getId
     * <em>Id</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value
     *            the new value of the '<em>Id</em>' attribute.
     * @see #getId()
     * @generated
     */
    void setId(String value);

    /**
     * @model
     * @return
     */
    String getTitle();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mgss.mbee.docgen.dgview.ViewElement#getTitle
     * <em>Title</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value
     *            the new value of the '<em>Title</em>' attribute.
     * @see #getTitle()
     * @generated
     */
    void setTitle(String value);

    /**
     * @model
     * @return
     */
    String getFromElementId();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mgss.mbee.docgen.dgview.ViewElement#getFromElementId
     * <em>From Element Id</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @param value
     *            the new value of the '<em>From Element Id</em>' attribute.
     * @see #getFromElementId()
     * @generated
     */
    void setFromElementId(String value);

    /**
     * @model
     * @return
     */
    FromProperty getFromProperty();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mgss.mbee.docgen.dgview.ViewElement#getFromProperty
     * <em>From Property</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @param value
     *            the new value of the '<em>From Property</em>' attribute.
     * @see gov.nasa.jpl.mgss.mbee.docgen.dgview.FromProperty
     * @see #getFromProperty()
     * @generated
     */
    void setFromProperty(FromProperty value);
}
