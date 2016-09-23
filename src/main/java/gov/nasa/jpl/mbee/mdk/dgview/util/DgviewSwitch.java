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
package gov.nasa.jpl.mbee.mdk.dgview.util;

import gov.nasa.jpl.mbee.mdk.dgview.*;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.Switch;

/**
 * <!-- begin-user-doc --> The <b>Switch</b> for the model's inheritance
 * hierarchy. It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object and proceeding up the
 * inheritance hierarchy until a non-null result is returned, which is the
 * result of the switch. <!-- end-user-doc -->
 *
 * @generated
 * @see gov.nasa.jpl.mbee.mdk.dgview.DgviewPackage
 */
public class DgviewSwitch<T> extends Switch<T> {
    /**
     * The cached model package <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    protected static DgviewPackage modelPackage;

    /**
     * Creates an instance of the switch. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @generated
     */
    public DgviewSwitch() {
        if (modelPackage == null) {
            modelPackage = DgviewPackage.eINSTANCE;
        }
    }

    /**
     * Checks whether this is a switch for the given package. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @return whether this is a switch for the given package.
     * @parameter ePackage the package in question.
     * @generated
     */
    @Override
    protected boolean isSwitchFor(EPackage ePackage) {
        return ePackage == modelPackage;
    }

    /**
     * Calls <code>caseXXX</code> for each class of the model until one returns
     * a non null result; it yields that result. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @return the first non-null result returned by a <code>caseXXX</code>
     * call.
     * @generated
     */
    @Override
    protected T doSwitch(int classifierID, EObject theEObject) {
        switch (classifierID) {
            case DgviewPackage.COL_SPEC: {
                ColSpec colSpec = (ColSpec) theEObject;
                T result = caseColSpec(colSpec);
                if (result == null) {
                    result = caseViewElement(colSpec);
                }
                if (result == null) {
                    result = defaultCase(theEObject);
                }
                return result;
            }
            case DgviewPackage.HAS_CONTENT: {
                HasContent hasContent = (HasContent) theEObject;
                T result = caseHasContent(hasContent);
                if (result == null) {
                    result = caseViewElement(hasContent);
                }
                if (result == null) {
                    result = defaultCase(theEObject);
                }
                return result;
            }
            case DgviewPackage.IMAGE: {
                Image image = (Image) theEObject;
                T result = caseImage(image);
                if (result == null) {
                    result = caseViewElement(image);
                }
                if (result == null) {
                    result = defaultCase(theEObject);
                }
                return result;
            }
            case DgviewPackage.LIST: {
                List list = (List) theEObject;
                T result = caseList(list);
                if (result == null) {
                    result = caseHasContent(list);
                }
                if (result == null) {
                    result = caseViewElement(list);
                }
                if (result == null) {
                    result = defaultCase(theEObject);
                }
                return result;
            }
            case DgviewPackage.LIST_ITEM: {
                ListItem listItem = (ListItem) theEObject;
                T result = caseListItem(listItem);
                if (result == null) {
                    result = caseHasContent(listItem);
                }
                if (result == null) {
                    result = caseViewElement(listItem);
                }
                if (result == null) {
                    result = defaultCase(theEObject);
                }
                return result;
            }
            case DgviewPackage.PARAGRAPH: {
                Paragraph paragraph = (Paragraph) theEObject;
                T result = caseParagraph(paragraph);
                if (result == null) {
                    result = caseViewElement(paragraph);
                }
                if (result == null) {
                    result = defaultCase(theEObject);
                }
                return result;
            }
            case DgviewPackage.TABLE: {
                Table table = (Table) theEObject;
                T result = caseTable(table);
                if (result == null) {
                    result = caseViewElement(table);
                }
                if (result == null) {
                    result = defaultCase(theEObject);
                }
                return result;
            }
            case DgviewPackage.TABLE_ENTRY: {
                TableEntry tableEntry = (TableEntry) theEObject;
                T result = caseTableEntry(tableEntry);
                if (result == null) {
                    result = caseHasContent(tableEntry);
                }
                if (result == null) {
                    result = caseViewElement(tableEntry);
                }
                if (result == null) {
                    result = defaultCase(theEObject);
                }
                return result;
            }
            case DgviewPackage.TEXT: {
                Text text = (Text) theEObject;
                T result = caseText(text);
                if (result == null) {
                    result = caseViewElement(text);
                }
                if (result == null) {
                    result = defaultCase(theEObject);
                }
                return result;
            }
            case DgviewPackage.VIEW_ELEMENT: {
                ViewElement viewElement = (ViewElement) theEObject;
                T result = caseViewElement(viewElement);
                if (result == null) {
                    result = defaultCase(theEObject);
                }
                return result;
            }
            case DgviewPackage.TABLE_ROW: {
                TableRow tableRow = (TableRow) theEObject;
                T result = caseTableRow(tableRow);
                if (result == null) {
                    result = caseHasContent(tableRow);
                }
                if (result == null) {
                    result = caseViewElement(tableRow);
                }
                if (result == null) {
                    result = defaultCase(theEObject);
                }
                return result;
            }
            case DgviewPackage.MD_EDITABLE_TABLE: {
                MDEditableTable mdEditableTable = (MDEditableTable) theEObject;
                T result = caseMDEditableTable(mdEditableTable);
                if (result == null) {
                    result = caseTable(mdEditableTable);
                }
                if (result == null) {
                    result = caseViewElement(mdEditableTable);
                }
                if (result == null) {
                    result = defaultCase(theEObject);
                }
                return result;
            }
            default:
                return defaultCase(theEObject);
        }
    }

    /**
     * Returns the result of interpreting the object as an instance of '
     * <em>Col Spec</em>'. <!-- begin-user-doc --> This implementation returns
     * null; returning a non-null result will terminate the switch. <!--
     * end-user-doc -->
     *
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '
     * <em>Col Spec</em>'.
     * @generated
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     */
    public T caseColSpec(ColSpec object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '
     * <em>Has Content</em>'. <!-- begin-user-doc --> This implementation
     * returns null; returning a non-null result will terminate the switch. <!--
     * end-user-doc -->
     *
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '
     * <em>Has Content</em>'.
     * @generated
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     */
    public T caseHasContent(HasContent object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '
     * <em>Image</em>'. <!-- begin-user-doc --> This implementation returns
     * null; returning a non-null result will terminate the switch. <!--
     * end-user-doc -->
     *
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '
     * <em>Image</em>'.
     * @generated
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     */
    public T caseImage(Image object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '
     * <em>List</em>'. <!-- begin-user-doc --> This implementation returns null;
     * returning a non-null result will terminate the switch. <!-- end-user-doc
     * -->
     *
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '
     * <em>List</em>'.
     * @generated
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     */
    public T caseList(List object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '
     * <em>List Item</em>'. <!-- begin-user-doc --> This implementation returns
     * null; returning a non-null result will terminate the switch. <!--
     * end-user-doc -->
     *
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '
     * <em>List Item</em>'.
     * @generated
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     */
    public T caseListItem(ListItem object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '
     * <em>Paragraph</em>'. <!-- begin-user-doc --> This implementation returns
     * null; returning a non-null result will terminate the switch. <!--
     * end-user-doc -->
     *
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '
     * <em>Paragraph</em>'.
     * @generated
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     */
    public T caseParagraph(Paragraph object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '
     * <em>Table</em>'. <!-- begin-user-doc --> This implementation returns
     * null; returning a non-null result will terminate the switch. <!--
     * end-user-doc -->
     *
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '
     * <em>Table</em>'.
     * @generated
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     */
    public T caseTable(Table object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '
     * <em>Table Entry</em>'. <!-- begin-user-doc --> This implementation
     * returns null; returning a non-null result will terminate the switch. <!--
     * end-user-doc -->
     *
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '
     * <em>Table Entry</em>'.
     * @generated
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     */
    public T caseTableEntry(TableEntry object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '
     * <em>Text</em>'. <!-- begin-user-doc --> This implementation returns null;
     * returning a non-null result will terminate the switch. <!-- end-user-doc
     * -->
     *
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '
     * <em>Text</em>'.
     * @generated
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     */
    public T caseText(Text object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '
     * <em>View Element</em>'. <!-- begin-user-doc --> This implementation
     * returns null; returning a non-null result will terminate the switch. <!--
     * end-user-doc -->
     *
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '
     * <em>View Element</em>'.
     * @generated
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     */
    public T caseViewElement(ViewElement object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '
     * <em>Table Row</em>'. <!-- begin-user-doc --> This implementation returns
     * null; returning a non-null result will terminate the switch. <!--
     * end-user-doc -->
     *
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '
     * <em>Table Row</em>'.
     * @generated
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     */
    public T caseTableRow(TableRow object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '
     * <em>MD Editable Table</em>'. <!-- begin-user-doc --> This implementation
     * returns null; returning a non-null result will terminate the switch. <!--
     * end-user-doc -->
     *
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '
     * <em>MD Editable Table</em>'.
     * @generated
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     */
    public T caseMDEditableTable(MDEditableTable object) {
        return null;
    }

    /**
     * Returns the result of interpreting the object as an instance of '
     * <em>EObject</em>'. <!-- begin-user-doc --> This implementation returns
     * null; returning a non-null result will terminate the switch, but this is
     * the last case anyway. <!-- end-user-doc -->
     *
     * @param object the target of the switch.
     * @return the result of interpreting the object as an instance of '
     * <em>EObject</em>'.
     * @generated
     * @see #doSwitch(org.eclipse.emf.ecore.EObject)
     */
    @Override
    public T defaultCase(EObject object) {
        return null;
    }

} // DgviewSwitch
