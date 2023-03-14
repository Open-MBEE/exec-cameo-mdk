package org.openmbee.mdk.docgen.validation;

import org.eclipse.emf.common.util.Enumerator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <!-- begin-user-doc --> A representation of the literals of the enumeration '
 * <em><b>Severity</b></em>', and utility methods for working with them. <!--
 * end-user-doc -->
 *
 * @model
 * @generated
 * @see DocGenValidationPackage#getSeverity()
 */
public enum Severity implements Enumerator {
    /**
     * The '<em><b>DEBUG</b></em>' literal object. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #DEBUG_VALUE
     */
    DEBUG(0, "DEBUG", "DEBUG"),

    /**
     * The '<em><b>INFO</b></em>' literal object. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #INFO_VALUE
     */
    INFO(1, "INFO", "INFO"),

    /**
     * The '<em><b>WARNING</b></em>' literal object. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #WARNING_VALUE
     */
    WARNING(2, "WARNING", "WARNING"),

    /**
     * The '<em><b>ERROR</b></em>' literal object. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #ERROR_VALUE
     */
    ERROR(3, "ERROR", "ERROR"),

    /**
     * The '<em><b>FATAL</b></em>' literal object. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #FATAL_VALUE
     */
    FATAL(4, "FATAL", "FATAL");

    /**
     * The '<em><b>DEBUG</b></em>' literal value. <!-- begin-user-doc -->
     * <p>
     * If the meaning of '<em><b>DEBUG</b></em>' literal object isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @model
     * @generated
     * @ordered
     * @see #DEBUG
     */
    public static final int DEBUG_VALUE = 0;

    /**
     * The '<em><b>INFO</b></em>' literal value. <!-- begin-user-doc -->
     * <p>
     * If the meaning of '<em><b>INFO</b></em>' literal object isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @model
     * @generated
     * @ordered
     * @see #INFO
     */
    public static final int INFO_VALUE = 1;

    /**
     * The '<em><b>WARNING</b></em>' literal value. <!-- begin-user-doc -->
     * <p>
     * If the meaning of '<em><b>WARNING</b></em>' literal object isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @model
     * @generated
     * @ordered
     * @see #WARNING
     */
    public static final int WARNING_VALUE = 2;

    /**
     * The '<em><b>ERROR</b></em>' literal value. <!-- begin-user-doc -->
     * <p>
     * If the meaning of '<em><b>ERROR</b></em>' literal object isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @model
     * @generated
     * @ordered
     * @see #ERROR
     */
    public static final int ERROR_VALUE = 3;

    /**
     * The '<em><b>FATAL</b></em>' literal value. <!-- begin-user-doc -->
     * <p>
     * If the meaning of '<em><b>FATAL</b></em>' literal object isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @model
     * @generated
     * @ordered
     * @see #FATAL
     */
    public static final int FATAL_VALUE = 4;

    /**
     * An array of all the '<em><b>Severity</b></em>' enumerators. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private static final Severity[] VALUES_ARRAY = new Severity[]{DEBUG, INFO, WARNING, ERROR, FATAL,};

    /**
     * A public read-only list of all the '<em><b>Severity</b></em>'
     * enumerators. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    public static final List<Severity> VALUES = Collections.unmodifiableList(Arrays
            .asList(VALUES_ARRAY));

    /**
     * Returns the '<em><b>Severity</b></em>' literal with the specified literal
     * value. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    public static Severity get(String literal) {
        for (int i = 0; i < VALUES_ARRAY.length; ++i) {
            Severity result = VALUES_ARRAY[i];
            if (result.toString().equals(literal)) {
                return result;
            }
        }
        return null;
    }

    /**
     * Returns the '<em><b>Severity</b></em>' literal with the specified name.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    public static Severity getByName(String name) {
        for (int i = 0; i < VALUES_ARRAY.length; ++i) {
            Severity result = VALUES_ARRAY[i];
            if (result.getName().equals(name)) {
                return result;
            }
        }
        return null;
    }

    /**
     * Returns the '<em><b>Severity</b></em>' literal with the specified integer
     * value. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    public static Severity get(int value) {
        switch (value) {
            case DEBUG_VALUE:
                return DEBUG;
            case INFO_VALUE:
                return INFO;
            case WARNING_VALUE:
                return WARNING;
            case ERROR_VALUE:
                return ERROR;
            case FATAL_VALUE:
                return FATAL;
        }
        return null;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private final int value;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private final String name;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private final String literal;

    /**
     * Only this class can construct instances. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @generated
     */
    Severity(int value, String name, String literal) {
        this.value = value;
        this.name = name;
        this.literal = literal;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public int getValue() {
        return value;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getLiteral() {
        return literal;
    }

    /**
     * Returns the literal value of the enumerator, which is its string
     * representation. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String toString() {
        return literal;
    }

} // Severity
