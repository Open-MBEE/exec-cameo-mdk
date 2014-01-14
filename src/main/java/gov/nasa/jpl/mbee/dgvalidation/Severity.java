/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 * 
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.mbee.dgvalidation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

/**
 * <!-- begin-user-doc --> A representation of the literals of the enumeration '
 * <em><b>Severity</b></em>', and utility methods for working with them. <!--
 * end-user-doc -->
 * 
 * @see gov.nasa.jpl.mbee.dgvalidation.DgvalidationPackage#getSeverity()
 * @model
 * @generated
 */
public enum Severity implements Enumerator {
    /**
     * The '<em><b>DEBUG</b></em>' literal object. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @see #DEBUG_VALUE
     * @generated
     * @ordered
     */
    DEBUG(0, "DEBUG", "DEBUG"),

    /**
     * The '<em><b>INFO</b></em>' literal object. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @see #INFO_VALUE
     * @generated
     * @ordered
     */
    INFO(1, "INFO", "INFO"),

    /**
     * The '<em><b>WARNING</b></em>' literal object. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @see #WARNING_VALUE
     * @generated
     * @ordered
     */
    WARNING(2, "WARNING", "WARNING"),

    /**
     * The '<em><b>ERROR</b></em>' literal object. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @see #ERROR_VALUE
     * @generated
     * @ordered
     */
    ERROR(3, "ERROR", "ERROR"),

    /**
     * The '<em><b>FATAL</b></em>' literal object. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @see #FATAL_VALUE
     * @generated
     * @ordered
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
     * @see #DEBUG
     * @model
     * @generated
     * @ordered
     */
    public static final int            DEBUG_VALUE   = 0;

    /**
     * The '<em><b>INFO</b></em>' literal value. <!-- begin-user-doc -->
     * <p>
     * If the meaning of '<em><b>INFO</b></em>' literal object isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @see #INFO
     * @model
     * @generated
     * @ordered
     */
    public static final int            INFO_VALUE    = 1;

    /**
     * The '<em><b>WARNING</b></em>' literal value. <!-- begin-user-doc -->
     * <p>
     * If the meaning of '<em><b>WARNING</b></em>' literal object isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @see #WARNING
     * @model
     * @generated
     * @ordered
     */
    public static final int            WARNING_VALUE = 2;

    /**
     * The '<em><b>ERROR</b></em>' literal value. <!-- begin-user-doc -->
     * <p>
     * If the meaning of '<em><b>ERROR</b></em>' literal object isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @see #ERROR
     * @model
     * @generated
     * @ordered
     */
    public static final int            ERROR_VALUE   = 3;

    /**
     * The '<em><b>FATAL</b></em>' literal value. <!-- begin-user-doc -->
     * <p>
     * If the meaning of '<em><b>FATAL</b></em>' literal object isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * 
     * @see #FATAL
     * @model
     * @generated
     * @ordered
     */
    public static final int            FATAL_VALUE   = 4;

    /**
     * An array of all the '<em><b>Severity</b></em>' enumerators. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    private static final Severity[]    VALUES_ARRAY  = new Severity[] {DEBUG, INFO, WARNING, ERROR, FATAL,};

    /**
     * A public read-only list of all the '<em><b>Severity</b></em>'
     * enumerators. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public static final List<Severity> VALUES        = Collections.unmodifiableList(Arrays
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
    private final int    value;

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
    private Severity(int value, String name, String literal) {
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
