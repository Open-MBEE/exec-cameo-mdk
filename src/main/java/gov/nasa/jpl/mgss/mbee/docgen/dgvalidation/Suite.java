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
 * <em><b>Suite</b></em>'. <!-- end-user-doc -->
 * 
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Suite#isShowDetail <em>
 * Show Detail</em>}</li>
 * <li>{@link gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Suite#isShowSummary
 * <em>Show Summary</em>}</li>
 * <li>{@link gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Suite#isOwnSection <em>
 * Own Section</em>}</li>
 * <li>{@link gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Suite#getName <em>Name
 * </em>}</li>
 * <li>{@link gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Suite#getRules <em>
 * Rules</em>}</li>
 * </ul>
 * </p>
 * 
 * @see gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.DgvalidationPackage#getSuite()
 * @model
 * @generated
 */
public interface Suite extends EObject {
    /**
     * Returns the value of the '<em><b>Show Detail</b></em>' attribute. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Show Detail</em>' attribute isn't clear, there
     * really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Show Detail</em>' attribute.
     * @see #setShowDetail(boolean)
     * @see gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.DgvalidationPackage#getSuite_ShowDetail()
     * @model
     * @generated
     */
    boolean isShowDetail();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Suite#isShowDetail
     * <em>Show Detail</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @param value
     *            the new value of the '<em>Show Detail</em>' attribute.
     * @see #isShowDetail()
     * @generated
     */
    void setShowDetail(boolean value);

    /**
     * Returns the value of the '<em><b>Show Summary</b></em>' attribute. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Show Summary</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Show Summary</em>' attribute.
     * @see #setShowSummary(boolean)
     * @see gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.DgvalidationPackage#getSuite_ShowSummary()
     * @model
     * @generated
     */
    boolean isShowSummary();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Suite#isShowSummary
     * <em>Show Summary</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @param value
     *            the new value of the '<em>Show Summary</em>' attribute.
     * @see #isShowSummary()
     * @generated
     */
    void setShowSummary(boolean value);

    /**
     * Returns the value of the '<em><b>Own Section</b></em>' attribute. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Own Section</em>' attribute isn't clear, there
     * really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Own Section</em>' attribute.
     * @see #setOwnSection(boolean)
     * @see gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.DgvalidationPackage#getSuite_OwnSection()
     * @model
     * @generated
     */
    boolean isOwnSection();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Suite#isOwnSection
     * <em>Own Section</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @param value
     *            the new value of the '<em>Own Section</em>' attribute.
     * @see #isOwnSection()
     * @generated
     */
    void setOwnSection(boolean value);

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
     * @see gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.DgvalidationPackage#getSuite_Name()
     * @model
     * @generated
     */
    String getName();

    /**
     * Sets the value of the '
     * {@link gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Suite#getName
     * <em>Name</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value
     *            the new value of the '<em>Name</em>' attribute.
     * @see #getName()
     * @generated
     */
    void setName(String value);

    /**
     * Returns the value of the '<em><b>Rules</b></em>' containment reference
     * list. The list contents are of type
     * {@link gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Rule}. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Rules</em>' containment reference list isn't
     * clear, there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Rules</em>' containment reference list.
     * @see gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.DgvalidationPackage#getSuite_Rules()
     * @model containment="true"
     * @generated
     */
    EList<Rule> getRules();

} // Suite
