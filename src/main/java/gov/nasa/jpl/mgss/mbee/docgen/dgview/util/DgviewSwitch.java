/**
 * <copyright> </copyright>
 * 
 * $Id$
 */
package gov.nasa.jpl.mgss.mbee.docgen.dgview.util;

import gov.nasa.jpl.mgss.mbee.docgen.dgview.ColSpec;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.DgviewPackage;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.HasContent;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.Image;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.List;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.ListItem;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.MDEditableTable;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.Paragraph;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.Table;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.TableEntry;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.TableRow;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.Text;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.ViewElement;

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
 * @see gov.nasa.jpl.mgss.mbee.docgen.dgview.DgviewPackage
 * @generated
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
     * @parameter ePackage the package in question.
     * @return whether this is a switch for the given package.
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
     *         call.
     * @generated
     */
    @Override
    protected T doSwitch(int classifierID, EObject theEObject) {
        switch (classifierID) {
            case DgviewPackage.COL_SPEC: {
                ColSpec colSpec = (ColSpec)theEObject;
                T result = caseColSpec(colSpec);
                if (result == null)
                    result = caseViewElement(colSpec);
                if (result == null)
                    result = defaultCase(theEObject);
                return result;
            }
            case DgviewPackage.HAS_CONTENT: {
                HasContent hasContent = (HasContent)theEObject;
                T result = caseHasContent(hasContent);
                if (result == null)
                    result = caseViewElement(hasContent);
                if (result == null)
                    result = defaultCase(theEObject);
                return result;
            }
            case DgviewPackage.IMAGE: {
                Image image = (Image)theEObject;
                T result = caseImage(image);
                if (result == null)
                    result = caseViewElement(image);
                if (result == null)
                    result = defaultCase(theEObject);
                return result;
            }
            case DgviewPackage.LIST: {
                List list = (List)theEObject;
                T result = caseList(list);
                if (result == null)
                    result = caseHasContent(list);
                if (result == null)
                    result = caseViewElement(list);
                if (result == null)
                    result = defaultCase(theEObject);
                return result;
            }
            case DgviewPackage.LIST_ITEM: {
                ListItem listItem = (ListItem)theEObject;
                T result = caseListItem(listItem);
                if (result == null)
                    result = caseHasContent(listItem);
                if (result == null)
                    result = caseViewElement(listItem);
                if (result == null)
                    result = defaultCase(theEObject);
                return result;
            }
            case DgviewPackage.PARAGRAPH: {
                Paragraph paragraph = (Paragraph)theEObject;
                T result = caseParagraph(paragraph);
                if (result == null)
                    result = caseViewElement(paragraph);
                if (result == null)
                    result = defaultCase(theEObject);
                return result;
            }
            case DgviewPackage.TABLE: {
                Table table = (Table)theEObject;
                T result = caseTable(table);
                if (result == null)
                    result = caseViewElement(table);
                if (result == null)
                    result = defaultCase(theEObject);
                return result;
            }
            case DgviewPackage.TABLE_ENTRY: {
                TableEntry tableEntry = (TableEntry)theEObject;
                T result = caseTableEntry(tableEntry);
                if (result == null)
                    result = caseHasContent(tableEntry);
                if (result == null)
                    result = caseViewElement(tableEntry);
                if (result == null)
                    result = defaultCase(theEObject);
                return result;
            }
            case DgviewPackage.TEXT: {
                Text text = (Text)theEObject;
                T result = caseText(text);
                if (result == null)
                    result = caseViewElement(text);
                if (result == null)
                    result = defaultCase(theEObject);
                return result;
            }
            case DgviewPackage.VIEW_ELEMENT: {
                ViewElement viewElement = (ViewElement)theEObject;
                T result = caseViewElement(viewElement);
                if (result == null)
                    result = defaultCase(theEObject);
                return result;
            }
            case DgviewPackage.TABLE_ROW: {
                TableRow tableRow = (TableRow)theEObject;
                T result = caseTableRow(tableRow);
                if (result == null)
                    result = caseHasContent(tableRow);
                if (result == null)
                    result = caseViewElement(tableRow);
                if (result == null)
                    result = defaultCase(theEObject);
                return result;
            }
            case DgviewPackage.MD_EDITABLE_TABLE: {
                MDEditableTable mdEditableTable = (MDEditableTable)theEObject;
                T result = caseMDEditableTable(mdEditableTable);
                if (result == null)
                    result = caseTable(mdEditableTable);
                if (result == null)
                    result = caseViewElement(mdEditableTable);
                if (result == null)
                    result = defaultCase(theEObject);
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
     * @param object
     *            the target of the switch.
     * @return the result of interpreting the object as an instance of '
     *         <em>Col Spec</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
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
     * @param object
     *            the target of the switch.
     * @return the result of interpreting the object as an instance of '
     *         <em>Has Content</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
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
     * @param object
     *            the target of the switch.
     * @return the result of interpreting the object as an instance of '
     *         <em>Image</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
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
     * @param object
     *            the target of the switch.
     * @return the result of interpreting the object as an instance of '
     *         <em>List</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
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
     * @param object
     *            the target of the switch.
     * @return the result of interpreting the object as an instance of '
     *         <em>List Item</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
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
     * @param object
     *            the target of the switch.
     * @return the result of interpreting the object as an instance of '
     *         <em>Paragraph</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
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
     * @param object
     *            the target of the switch.
     * @return the result of interpreting the object as an instance of '
     *         <em>Table</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
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
     * @param object
     *            the target of the switch.
     * @return the result of interpreting the object as an instance of '
     *         <em>Table Entry</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
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
     * @param object
     *            the target of the switch.
     * @return the result of interpreting the object as an instance of '
     *         <em>Text</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
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
     * @param object
     *            the target of the switch.
     * @return the result of interpreting the object as an instance of '
     *         <em>View Element</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
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
     * @param object
     *            the target of the switch.
     * @return the result of interpreting the object as an instance of '
     *         <em>Table Row</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
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
     * @param object
     *            the target of the switch.
     * @return the result of interpreting the object as an instance of '
     *         <em>MD Editable Table</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
     * @generated
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
     * @param object
     *            the target of the switch.
     * @return the result of interpreting the object as an instance of '
     *         <em>EObject</em>'.
     * @see #doSwitch(org.eclipse.emf.ecore.EObject)
     * @generated
     */
    @Override
    public T defaultCase(EObject object) {
        return null;
    }

} // DgviewSwitch
