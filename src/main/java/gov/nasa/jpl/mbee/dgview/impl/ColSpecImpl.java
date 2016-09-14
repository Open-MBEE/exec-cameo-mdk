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
package gov.nasa.jpl.mbee.dgview.impl;

import gov.nasa.jpl.mbee.dgview.ColSpec;
import gov.nasa.jpl.mbee.dgview.DgviewPackage;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc --> An implementation of the model object '
 * <em><b>Col Spec</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link gov.nasa.jpl.mbee.dgview.impl.ColSpecImpl#getColname
 * <em>Colname</em>}</li>
 * <li>{@link gov.nasa.jpl.mbee.dgview.impl.ColSpecImpl#getColwidth
 * <em>Colwidth</em>}</li>
 * <li>{@link gov.nasa.jpl.mbee.dgview.impl.ColSpecImpl#getColnum
 * <em>Colnum</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ColSpecImpl extends ViewElementImpl implements ColSpec {
    /**
     * The default value of the '{@link #getColname() <em>Colname</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getColname()
     */
    protected static final String COLNAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getColname() <em>Colname</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getColname()
     */
    protected String colname = COLNAME_EDEFAULT;

    /**
     * The default value of the '{@link #getColwidth() <em>Colwidth</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getColwidth()
     */
    protected static final String COLWIDTH_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getColwidth() <em>Colwidth</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getColwidth()
     */
    protected String colwidth = COLWIDTH_EDEFAULT;

    /**
     * The default value of the '{@link #getColnum() <em>Colnum</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getColnum()
     */
    protected static final int COLNUM_EDEFAULT = 0;

    /**
     * The cached value of the '{@link #getColnum() <em>Colnum</em>}' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getColnum()
     */
    protected int colnum = COLNUM_EDEFAULT;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    protected ColSpecImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return DgviewPackage.Literals.COL_SPEC;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getColname() {
        return colname;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setColname(String newColname) {
        String oldColname = colname;
        colname = newColname;
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, DgviewPackage.COL_SPEC__COLNAME,
                    oldColname, colname));
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getColwidth() {
        return colwidth;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setColwidth(String newColwidth) {
        String oldColwidth = colwidth;
        colwidth = newColwidth;
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, DgviewPackage.COL_SPEC__COLWIDTH,
                    oldColwidth, colwidth));
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public int getColnum() {
        return colnum;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setColnum(int newColnum) {
        int oldColnum = colnum;
        colnum = newColnum;
        if (eNotificationRequired()) {
            eNotify(new ENotificationImpl(this, Notification.SET, DgviewPackage.COL_SPEC__COLNUM, oldColnum,
                    colnum));
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case DgviewPackage.COL_SPEC__COLNAME:
                return getColname();
            case DgviewPackage.COL_SPEC__COLWIDTH:
                return getColwidth();
            case DgviewPackage.COL_SPEC__COLNUM:
                return getColnum();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case DgviewPackage.COL_SPEC__COLNAME:
                setColname((String) newValue);
                return;
            case DgviewPackage.COL_SPEC__COLWIDTH:
                setColwidth((String) newValue);
                return;
            case DgviewPackage.COL_SPEC__COLNUM:
                setColnum((Integer) newValue);
                return;
        }
        super.eSet(featureID, newValue);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void eUnset(int featureID) {
        switch (featureID) {
            case DgviewPackage.COL_SPEC__COLNAME:
                setColname(COLNAME_EDEFAULT);
                return;
            case DgviewPackage.COL_SPEC__COLWIDTH:
                setColwidth(COLWIDTH_EDEFAULT);
                return;
            case DgviewPackage.COL_SPEC__COLNUM:
                setColnum(COLNUM_EDEFAULT);
                return;
        }
        super.eUnset(featureID);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public boolean eIsSet(int featureID) {
        switch (featureID) {
            case DgviewPackage.COL_SPEC__COLNAME:
                return COLNAME_EDEFAULT == null ? colname != null : !COLNAME_EDEFAULT.equals(colname);
            case DgviewPackage.COL_SPEC__COLWIDTH:
                return COLWIDTH_EDEFAULT == null ? colwidth != null : !COLWIDTH_EDEFAULT.equals(colwidth);
            case DgviewPackage.COL_SPEC__COLNUM:
                return colnum != COLNUM_EDEFAULT;
        }
        return super.eIsSet(featureID);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String toString() {
        if (eIsProxy()) {
            return super.toString();
        }

        StringBuffer result = new StringBuffer(super.toString());
        result.append(" (colname: ");
        result.append(colname);
        result.append(", colwidth: ");
        result.append(colwidth);
        result.append(", colnum: ");
        result.append(colnum);
        result.append(')');
        return result.toString();
    }

} // ColSpecImpl
