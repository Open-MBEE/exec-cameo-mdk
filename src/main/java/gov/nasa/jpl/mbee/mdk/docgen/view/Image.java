package gov.nasa.jpl.mbee.mdk.docgen.view;

/**
 * @author dlam
 * @model
 */
public interface Image extends ViewElement {

    /**
     * @return
     * @model
     */
    String getDiagramId();

    /**
     * Sets the value of the '
     * {@link Image#getDiagramId
     * <em>Diagram Id</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @param value the new value of the '<em>Diagram Id</em>' attribute.
     * @generated
     * @see #getDiagramId()
     */
    void setDiagramId(String value);

    /**
     * @return
     * @model
     */
    String getCaption();

    /**
     * Sets the value of the '
     * {@link Image#getCaption
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
    boolean isGennew();

    /**
     * Sets the value of the '
     * {@link Image#isGennew
     * <em>Gennew</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     *
     * @param value the new value of the '<em>Gennew</em>' attribute.
     * @generated
     * @see #isGennew()
     */
    void setGennew(boolean value);

    /**
     * @return
     * @model
     */
    boolean isDoNotShow();

    /**
     * Sets the value of the '
     * {@link Image#isDoNotShow
     * <em>Do Not Show</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @param value the new value of the '<em>Do Not Show</em>' attribute.
     * @generated
     * @see #isDoNotShow()
     */
    void setDoNotShow(boolean value);

}
