package gov.nasa.jpl.mbee.mdk.docgen.validation;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc --> A representation of the model object '
 * <em><b>Rule</b></em>'. <!-- end-user-doc -->
 * <p>
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link Rule#getName <em>Name
 * </em>}</li>
 * <li>{@link Rule#getDescription
 * <em>Description</em>}</li>
 * <li>{@link Rule#getSeverity <em>
 * Severity</em>}</li>
 * <li>{@link Rule#getViolations <em>
 * Violations</em>}</li>
 * </ul>
 * </p>
 *
 * @model
 * @generated
 * @see DocGenValidationPackage#getRule()
 */
public interface Rule extends EObject {
    /**
     * Returns the value of the '<em><b>Name</b></em>' attribute. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Name</em>' attribute isn't clear, there really
     * should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>Name</em>' attribute.
     * @model
     * @generated
     * @see #setName(String)
     * @see DocGenValidationPackage#getRule_Name()
     */
    String getName();

    /**
     * Sets the value of the '
     * {@link Rule#getName
     * <em>Name</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @param value the new value of the '<em>Name</em>' attribute.
     * @generated
     * @see #getName()
     */
    void setName(String value);

    /**
     * Returns the value of the '<em><b>Description</b></em>' attribute. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Description</em>' attribute isn't clear, there
     * really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>Description</em>' attribute.
     * @model
     * @generated
     * @see #setDescription(String)
     * @see DocGenValidationPackage#getRule_Description()
     */
    String getDescription();

    /**
     * Sets the value of the '
     * {@link Rule#getDescription
     * <em>Description</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @param value the new value of the '<em>Description</em>' attribute.
     * @generated
     * @see #getDescription()
     */
    void setDescription(String value);

    /**
     * Returns the value of the '<em><b>Severity</b></em>' attribute. The
     * literals are from the enumeration
     * {@link Severity}. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Severity</em>' attribute isn't clear, there
     * really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>Severity</em>' attribute.
     * @model
     * @generated
     * @see Severity
     * @see #setSeverity(Severity)
     * @see DocGenValidationPackage#getRule_Severity()
     */
    Severity getSeverity();

    /**
     * Sets the value of the '
     * {@link Rule#getSeverity
     * <em>Severity</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     *
     * @param value the new value of the '<em>Severity</em>' attribute.
     * @generated
     * @see Severity
     * @see #getSeverity()
     */
    void setSeverity(Severity value);

    /**
     * Returns the value of the '<em><b>Violations</b></em>' containment
     * reference list. The list contents are of type
     * {@link Violation}. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Violations</em>' containment reference list
     * isn't clear, there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>Violations</em>' containment reference
     * list.
     * @model containment="true"
     * @generated
     * @see DocGenValidationPackage#getRule_Violations()
     */
    EList<Violation> getViolations();

} // Rule
