package org.openmbee.mdk.docgen.validation;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc --> A representation of the model object '
 * <em><b>Suite</b></em>'. <!-- end-user-doc -->
 * <p>
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link Suite#isShowDetail <em>
 * Show Detail</em>}</li>
 * <li>{@link Suite#isShowSummary
 * <em>Show Summary</em>}</li>
 * <li>{@link Suite#isOwnSection <em>
 * Own Section</em>}</li>
 * <li>{@link Suite#getName <em>Name
 * </em>}</li>
 * <li>{@link Suite#getRules <em>
 * Rules</em>}</li>
 * </ul>
 * </p>
 *
 * @model
 * @generated
 * @see DocGenValidationPackage#getSuite()
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
     * @model
     * @generated
     * @see #setShowDetail(boolean)
     * @see DocGenValidationPackage#getSuite_ShowDetail()
     */
    boolean isShowDetail();

    /**
     * Sets the value of the '
     * {@link Suite#isShowDetail
     * <em>Show Detail</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @param value the new value of the '<em>Show Detail</em>' attribute.
     * @generated
     * @see #isShowDetail()
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
     * @model
     * @generated
     * @see #setShowSummary(boolean)
     * @see DocGenValidationPackage#getSuite_ShowSummary()
     */
    boolean isShowSummary();

    /**
     * Sets the value of the '
     * {@link Suite#isShowSummary
     * <em>Show Summary</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @param value the new value of the '<em>Show Summary</em>' attribute.
     * @generated
     * @see #isShowSummary()
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
     * @model
     * @generated
     * @see #setOwnSection(boolean)
     * @see DocGenValidationPackage#getSuite_OwnSection()
     */
    boolean isOwnSection();

    /**
     * Sets the value of the '
     * {@link Suite#isOwnSection
     * <em>Own Section</em>}' attribute. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @param value the new value of the '<em>Own Section</em>' attribute.
     * @generated
     * @see #isOwnSection()
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
     * @model
     * @generated
     * @see #setName(String)
     * @see DocGenValidationPackage#getSuite_Name()
     */
    String getName();

    /**
     * Sets the value of the '
     * {@link Suite#getName
     * <em>Name</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @param value the new value of the '<em>Name</em>' attribute.
     * @generated
     * @see #getName()
     */
    void setName(String value);

    /**
     * Returns the value of the '<em><b>Rules</b></em>' containment reference
     * list. The list contents are of type
     * {@link Rule}. <!--
     * begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Rules</em>' containment reference list isn't
     * clear, there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>Rules</em>' containment reference list.
     * @model containment="true"
     * @generated
     * @see DocGenValidationPackage#getSuite_Rules()
     */
    EList<Rule> getRules();

} // Suite
