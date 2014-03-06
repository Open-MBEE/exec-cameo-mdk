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
package gov.nasa.jpl.mbee.dgview.util;

import gov.nasa.jpl.mbee.dgview.ColSpec;
import gov.nasa.jpl.mbee.dgview.DgviewPackage;
import gov.nasa.jpl.mbee.dgview.HasContent;
import gov.nasa.jpl.mbee.dgview.Image;
import gov.nasa.jpl.mbee.dgview.List;
import gov.nasa.jpl.mbee.dgview.ListItem;
import gov.nasa.jpl.mbee.dgview.MDEditableTable;
import gov.nasa.jpl.mbee.dgview.Paragraph;
import gov.nasa.jpl.mbee.dgview.Table;
import gov.nasa.jpl.mbee.dgview.TableEntry;
import gov.nasa.jpl.mbee.dgview.TableRow;
import gov.nasa.jpl.mbee.dgview.Text;
import gov.nasa.jpl.mbee.dgview.ViewElement;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;
import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc --> The <b>Adapter Factory</b> for the model. It provides
 * an adapter <code>createXXX</code> method for each class of the model. <!--
 * end-user-doc -->
 * 
 * @see gov.nasa.jpl.mbee.dgview.DgviewPackage
 * @generated
 */
public class DgviewAdapterFactory extends AdapterFactoryImpl {
    /**
     * The cached model package. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    protected static DgviewPackage modelPackage;

    /**
     * Creates an instance of the adapter factory. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     */
    public DgviewAdapterFactory() {
        if (modelPackage == null) {
            modelPackage = DgviewPackage.eINSTANCE;
        }
    }

    /**
     * Returns whether this factory is applicable for the type of the object.
     * <!-- begin-user-doc --> This implementation returns <code>true</code> if
     * the object is either the model's package or is an instance object of the
     * model. <!-- end-user-doc -->
     * 
     * @return whether this factory is applicable for the type of the object.
     * @generated
     */
    @Override
    public boolean isFactoryForType(Object object) {
        if (object == modelPackage) {
            return true;
        }
        if (object instanceof EObject) {
            return ((EObject)object).eClass().getEPackage() == modelPackage;
        }
        return false;
    }

    /**
     * The switch that delegates to the <code>createXXX</code> methods. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    protected DgviewSwitch<Adapter> modelSwitch = new DgviewSwitch<Adapter>() {
                                                    @Override
                                                    public Adapter caseColSpec(ColSpec object) {
                                                        return createColSpecAdapter();
                                                    }

                                                    @Override
                                                    public Adapter caseHasContent(HasContent object) {
                                                        return createHasContentAdapter();
                                                    }

                                                    @Override
                                                    public Adapter caseImage(Image object) {
                                                        return createImageAdapter();
                                                    }

                                                    @Override
                                                    public Adapter caseList(List object) {
                                                        return createListAdapter();
                                                    }

                                                    @Override
                                                    public Adapter caseListItem(ListItem object) {
                                                        return createListItemAdapter();
                                                    }

                                                    @Override
                                                    public Adapter caseParagraph(Paragraph object) {
                                                        return createParagraphAdapter();
                                                    }

                                                    @Override
                                                    public Adapter caseTable(Table object) {
                                                        return createTableAdapter();
                                                    }

                                                    @Override
                                                    public Adapter caseTableEntry(TableEntry object) {
                                                        return createTableEntryAdapter();
                                                    }

                                                    @Override
                                                    public Adapter caseText(Text object) {
                                                        return createTextAdapter();
                                                    }

                                                    @Override
                                                    public Adapter caseViewElement(ViewElement object) {
                                                        return createViewElementAdapter();
                                                    }

                                                    @Override
                                                    public Adapter caseTableRow(TableRow object) {
                                                        return createTableRowAdapter();
                                                    }

                                                    @Override
                                                    public Adapter caseMDEditableTable(MDEditableTable object) {
                                                        return createMDEditableTableAdapter();
                                                    }

                                                    @Override
                                                    public Adapter defaultCase(EObject object) {
                                                        return createEObjectAdapter();
                                                    }
                                                };

    /**
     * Creates an adapter for the <code>target</code>. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * 
     * @param target
     *            the object to adapt.
     * @return the adapter for the <code>target</code>.
     * @generated
     */
    @Override
    public Adapter createAdapter(Notifier target) {
        return modelSwitch.doSwitch((EObject)target);
    }

    /**
     * Creates a new adapter for an object of class '
     * {@link gov.nasa.jpl.mbee.dgview.ColSpec <em>Col Spec</em>}'.
     * <!-- begin-user-doc --> This default implementation returns null so that
     * we can easily ignore cases; it's useful to ignore a case when inheritance
     * will catch all the cases anyway. <!-- end-user-doc -->
     * 
     * @return the new adapter.
     * @see gov.nasa.jpl.mbee.dgview.ColSpec
     * @generated
     */
    public Adapter createColSpecAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '
     * {@link gov.nasa.jpl.mbee.dgview.HasContent
     * <em>Has Content</em>}'. <!-- begin-user-doc --> This default
     * implementation returns null so that we can easily ignore cases; it's
     * useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * 
     * @return the new adapter.
     * @see gov.nasa.jpl.mbee.dgview.HasContent
     * @generated
     */
    public Adapter createHasContentAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '
     * {@link gov.nasa.jpl.mbee.dgview.Image <em>Image</em>}'. <!--
     * begin-user-doc --> This default implementation returns null so that we
     * can easily ignore cases; it's useful to ignore a case when inheritance
     * will catch all the cases anyway. <!-- end-user-doc -->
     * 
     * @return the new adapter.
     * @see gov.nasa.jpl.mbee.dgview.Image
     * @generated
     */
    public Adapter createImageAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '
     * {@link gov.nasa.jpl.mbee.dgview.List <em>List</em>}'. <!--
     * begin-user-doc --> This default implementation returns null so that we
     * can easily ignore cases; it's useful to ignore a case when inheritance
     * will catch all the cases anyway. <!-- end-user-doc -->
     * 
     * @return the new adapter.
     * @see gov.nasa.jpl.mbee.dgview.List
     * @generated
     */
    public Adapter createListAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '
     * {@link gov.nasa.jpl.mbee.dgview.ListItem <em>List Item</em>}
     * '. <!-- begin-user-doc --> This default implementation returns null so
     * that we can easily ignore cases; it's useful to ignore a case when
     * inheritance will catch all the cases anyway. <!-- end-user-doc -->
     * 
     * @return the new adapter.
     * @see gov.nasa.jpl.mbee.dgview.ListItem
     * @generated
     */
    public Adapter createListItemAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '
     * {@link gov.nasa.jpl.mbee.dgview.Paragraph <em>Paragraph</em>}
     * '. <!-- begin-user-doc --> This default implementation returns null so
     * that we can easily ignore cases; it's useful to ignore a case when
     * inheritance will catch all the cases anyway. <!-- end-user-doc -->
     * 
     * @return the new adapter.
     * @see gov.nasa.jpl.mbee.dgview.Paragraph
     * @generated
     */
    public Adapter createParagraphAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '
     * {@link gov.nasa.jpl.mbee.dgview.Table <em>Table</em>}'. <!--
     * begin-user-doc --> This default implementation returns null so that we
     * can easily ignore cases; it's useful to ignore a case when inheritance
     * will catch all the cases anyway. <!-- end-user-doc -->
     * 
     * @return the new adapter.
     * @see gov.nasa.jpl.mbee.dgview.Table
     * @generated
     */
    public Adapter createTableAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '
     * {@link gov.nasa.jpl.mbee.dgview.TableEntry
     * <em>Table Entry</em>}'. <!-- begin-user-doc --> This default
     * implementation returns null so that we can easily ignore cases; it's
     * useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * 
     * @return the new adapter.
     * @see gov.nasa.jpl.mbee.dgview.TableEntry
     * @generated
     */
    public Adapter createTableEntryAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '
     * {@link gov.nasa.jpl.mbee.dgview.Text <em>Text</em>}'. <!--
     * begin-user-doc --> This default implementation returns null so that we
     * can easily ignore cases; it's useful to ignore a case when inheritance
     * will catch all the cases anyway. <!-- end-user-doc -->
     * 
     * @return the new adapter.
     * @see gov.nasa.jpl.mbee.dgview.Text
     * @generated
     */
    public Adapter createTextAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '
     * {@link gov.nasa.jpl.mbee.dgview.ViewElement
     * <em>View Element</em>}'. <!-- begin-user-doc --> This default
     * implementation returns null so that we can easily ignore cases; it's
     * useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * 
     * @return the new adapter.
     * @see gov.nasa.jpl.mbee.dgview.ViewElement
     * @generated
     */
    public Adapter createViewElementAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '
     * {@link gov.nasa.jpl.mbee.dgview.TableRow <em>Table Row</em>}
     * '. <!-- begin-user-doc --> This default implementation returns null so
     * that we can easily ignore cases; it's useful to ignore a case when
     * inheritance will catch all the cases anyway. <!-- end-user-doc -->
     * 
     * @return the new adapter.
     * @see gov.nasa.jpl.mbee.dgview.TableRow
     * @generated
     */
    public Adapter createTableRowAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for an object of class '
     * {@link gov.nasa.jpl.mbee.dgview.MDEditableTable
     * <em>MD Editable Table</em>}'. <!-- begin-user-doc --> This default
     * implementation returns null so that we can easily ignore cases; it's
     * useful to ignore a case when inheritance will catch all the cases anyway.
     * <!-- end-user-doc -->
     * 
     * @return the new adapter.
     * @see gov.nasa.jpl.mbee.dgview.MDEditableTable
     * @generated
     */
    public Adapter createMDEditableTableAdapter() {
        return null;
    }

    /**
     * Creates a new adapter for the default case. <!-- begin-user-doc --> This
     * default implementation returns null. <!-- end-user-doc -->
     * 
     * @return the new adapter.
     * @generated
     */
    public Adapter createEObjectAdapter() {
        return null;
    }

} // DgviewAdapterFactory
