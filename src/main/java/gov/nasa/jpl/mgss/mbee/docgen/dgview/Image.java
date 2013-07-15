package gov.nasa.jpl.mgss.mbee.docgen.dgview;

/**
 * @model
 * @author dlam
 *
 */
public interface Image extends ViewElement {

	/**
	 * @model
	 * @return
	 */
	String getDiagramId();
	/**
	 * Sets the value of the '{@link gov.nasa.jpl.mgss.mbee.docgen.dgview.Image#getDiagramId <em>Diagram Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Diagram Id</em>' attribute.
	 * @see #getDiagramId()
	 * @generated
	 */
	void setDiagramId(String value);
	/**
	 * @model
	 * @return
	 */
	String getCaption();
	/**
	 * Sets the value of the '{@link gov.nasa.jpl.mgss.mbee.docgen.dgview.Image#getCaption <em>Caption</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Caption</em>' attribute.
	 * @see #getCaption()
	 * @generated
	 */
	void setCaption(String value);
	/**
	 * @model
	 * @return
	 */
	boolean isGennew();
	/**
	 * Sets the value of the '{@link gov.nasa.jpl.mgss.mbee.docgen.dgview.Image#isGennew <em>Gennew</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Gennew</em>' attribute.
	 * @see #isGennew()
	 * @generated
	 */
	void setGennew(boolean value);
	/**
	 * @model
	 * @return
	 */
	boolean isDoNotShow();
	/**
	 * Sets the value of the '{@link gov.nasa.jpl.mgss.mbee.docgen.dgview.Image#isDoNotShow <em>Do Not Show</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Do Not Show</em>' attribute.
	 * @see #isDoNotShow()
	 * @generated
	 */
	void setDoNotShow(boolean value);
	
}
