/**
 * <copyright> </copyright>
 * 
 * $Id$
 */
package gov.nasa.jpl.mgss.mbee.docgen.dgvalidation;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc --> A representation of the model object '
 * <em><b>Rule</b></em>'. <!-- end-user-doc -->
 * 
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Rule#getName <em>Name
 * </em>}</li>
 * <li>{@link gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Rule#getDescription
 * <em>Description</em>}</li>
 * <li>{@link gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Rule#getSeverity <em>
 * Severity</em>}</li>
 * <li>{@link gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Rule#getViolations <em>
 * Violations</em>}</li>
 * </ul>
 * </p>
 * 
 * @see gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.DgvalidationPackage#getRule()
 * @model
 * @generated
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
     * @see #setName(String)
     * @see gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.DgvalidationPackage#getRule_Name()
     * @model
     * @generated
     */
    String getName();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Rule#getName
     * <em>Name</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value
     *            the new value of the '<em>Name</em>' attribute.
     * @see #getName()
     * @generated
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
     * @see #setDescription(String)
     * @see gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.DgvalidationPackage#getRule_Description()
     * @model
     * @generated
     */
    String getDescription();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Rule#getDescription
     * <em>Description</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @param value
     *            the new value of the '<em>Description</em>' attribute.
     * @see #getDescription()
     * @generated
     */
    void setDescription(String value);

    /**
     * Returns the value of the '<em><b>Severity</b></em>' attribute. The
     * literals are from the enumeration
     * {@link gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Severity}. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Severity</em>' attribute isn't clear, there
     * really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Severity</em>' attribute.
     * @see gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Severity
     * @see #setSeverity(Severity)
     * @see gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.DgvalidationPackage#getRule_Severity()
     * @model
     * @generated
     */
    Severity getSeverity();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Rule#getSeverity
     * <em>Severity</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @param value
     *            the new value of the '<em>Severity</em>' attribute.
     * @see gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Severity
     * @see #getSeverity()
     * @generated
     */
    void setSeverity(Severity value);

    /**
     * Returns the value of the '<em><b>Violations</b></em>' containment
     * reference list. The list contents are of type
     * {@link gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Violation}. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Violations</em>' containment reference list
     * isn't clear, there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Violations</em>' containment reference
     *         list.
     * @see gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.DgvalidationPackage#getRule_Violations()
     * @model containment="true"
     * @generated
     */
    EList<Violation> getViolations();

} // Rule
