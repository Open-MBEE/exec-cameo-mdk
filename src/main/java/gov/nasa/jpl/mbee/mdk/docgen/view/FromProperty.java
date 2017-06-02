package gov.nasa.jpl.mbee.mdk.docgen.view;

import org.eclipse.emf.common.util.Enumerator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <!-- begin-user-doc --> A representation of the literals of the enumeration '
 * <em><b>From Property</b></em>', and utility methods for working with them.
 * <!-- end-user-doc -->
 *
 * @model
 * @generated
 * @see DocGenViewPackage#getFromProperty()
 */
public enum FromProperty implements Enumerator {
    /**
     * The '<em><b>NAME</b></em>' literal object. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #NAME_VALUE
     */
    NAME(0, "NAME", "NAME"),

    /**
     * The '<em><b>DOCUMENTATION</b></em>' literal object. <!-- begin-user-doc
     * --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #DOCUMENTATION_VALUE
     */
    DOCUMENTATION(1, "DOCUMENTATION", "DOCUMENTATION"),

    /**
     * The '<em><b>DVALUE</b></em>' literal object. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #DVALUE_VALUE
     */
    DVALUE(2, "DVALUE", "DVALUE");

    /**
     * The '<em><b>NAME</b></em>' literal value. <!-- begin-user-doc -->
     * <p>
     * If the meaning of '<em><b>NAME</b></em>' literal object isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @model
     * @generated
     * @ordered
     * @see #NAME
     */
    public static final int NAME_VALUE = 0;

    /**
     * The '<em><b>DOCUMENTATION</b></em>' literal value. <!-- begin-user-doc
     * -->
     * <p>
     * If the meaning of '<em><b>DOCUMENTATION</b></em>' literal object isn't
     * clear, there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @model
     * @generated
     * @ordered
     * @see #DOCUMENTATION
     */
    public static final int DOCUMENTATION_VALUE = 1;

    /**
     * The '<em><b>DVALUE</b></em>' literal value. <!-- begin-user-doc -->
     * <p>
     * If the meaning of '<em><b>DVALUE</b></em>' literal object isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     *
     * @model
     * @generated
     * @ordered
     * @see #DVALUE
     */
    public static final int DVALUE_VALUE = 2;

    /**
     * An array of all the '<em><b>From Property</b></em>' enumerators. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private static final FromProperty[] VALUES_ARRAY = new FromProperty[]{NAME, DOCUMENTATION,
            DVALUE,};

    /**
     * A public read-only list of all the '<em><b>From Property</b></em>'
     * enumerators. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    public static final List<FromProperty> VALUES = Collections.unmodifiableList(Arrays
            .asList(VALUES_ARRAY));

    /**
     * Returns the '<em><b>From Property</b></em>' literal with the specified
     * literal value. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    public static FromProperty get(String literal) {
        for (int i = 0; i < VALUES_ARRAY.length; ++i) {
            FromProperty result = VALUES_ARRAY[i];
            if (result.toString().equals(literal)) {
                return result;
            }
        }
        return null;
    }

    /**
     * Returns the '<em><b>From Property</b></em>' literal with the specified
     * name. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    public static FromProperty getByName(String name) {
        for (int i = 0; i < VALUES_ARRAY.length; ++i) {
            FromProperty result = VALUES_ARRAY[i];
            if (result.getName().equals(name)) {
                return result;
            }
        }
        return null;
    }

    /**
     * Returns the '<em><b>From Property</b></em>' literal with the specified
     * integer value. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    public static FromProperty get(int value) {
        switch (value) {
            case NAME_VALUE:
                return NAME;
            case DOCUMENTATION_VALUE:
                return DOCUMENTATION;
            case DVALUE_VALUE:
                return DVALUE;
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
    FromProperty(int value, String name, String literal) {
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

} // FromProperty
