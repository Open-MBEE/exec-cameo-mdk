package gov.nasa.jpl.mbee.mdk.docgen.validation;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc --> A representation of the model object '
 * <em><b>Violation</b></em>'. <!-- end-user-doc -->
 * <p>
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link Violation#getElementId
 * <em>Element Id</em>}</li>
 * <li>{@link Violation#getComment
 * <em>Comment</em>}</li>
 * </ul>
 * </p>
 *
 * @model
 * @generated
 * @see DocGenValidationPackage#getViolation()
 */
public interface Violation extends EObject {
    /**
     * Returns the value of the '<em><b>Element Id</b></em>' attribute. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Element Id</em>' attribute isn't clear, there
     * really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>Element Id</em>' attribute.
     * @model
     * @generated
     * @see #setElementId(String)
     * @see DocGenValidationPackage#getViolation_ElementId()
     */
    String getElementId();

    /**
     * Sets the value of the '
     * {@link Violation#getElementId
     * <em>Element Id</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @param value the new value of the '<em>Element Id</em>' attribute.
     * @generated
     * @see #getElementId()
     */
    void setElementId(String value);

    /**
     * Returns the value of the '<em><b>Comment</b></em>' attribute. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Comment</em>' attribute isn't clear, there
     * really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>Comment</em>' attribute.
     * @model
     * @generated
     * @see #setComment(String)
     * @see DocGenValidationPackage#getViolation_Comment()
     */
    String getComment();

    /**
     * Sets the value of the '
     * {@link Violation#getComment
     * <em>Comment</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     *
     * @param value the new value of the '<em>Comment</em>' attribute.
     * @generated
     * @see #getComment()
     */
    void setComment(String value);

} // Violation
