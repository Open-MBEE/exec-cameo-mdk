/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package gov.nasa.jpl.mgss.mbee.docgen.dgvalidation;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Violation</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Violation#getElementId <em>Element Id</em>}</li>
 *   <li>{@link gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Violation#getComment <em>Comment</em>}</li>
 * </ul>
 * </p>
 *
 * @see gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.DgvalidationPackage#getViolation()
 * @model
 * @generated
 */
public interface Violation extends EObject {
	/**
	 * Returns the value of the '<em><b>Element Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Element Id</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Element Id</em>' attribute.
	 * @see #setElementId(String)
	 * @see gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.DgvalidationPackage#getViolation_ElementId()
	 * @model
	 * @generated
	 */
	String getElementId();

	/**
	 * Sets the value of the '{@link gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Violation#getElementId <em>Element Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Element Id</em>' attribute.
	 * @see #getElementId()
	 * @generated
	 */
	void setElementId(String value);

	/**
	 * Returns the value of the '<em><b>Comment</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Comment</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Comment</em>' attribute.
	 * @see #setComment(String)
	 * @see gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.DgvalidationPackage#getViolation_Comment()
	 * @model
	 * @generated
	 */
	String getComment();

	/**
	 * Sets the value of the '{@link gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Violation#getComment <em>Comment</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Comment</em>' attribute.
	 * @see #getComment()
	 * @generated
	 */
	void setComment(String value);

} // Violation
