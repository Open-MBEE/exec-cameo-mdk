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
package gov.nasa.jpl.mbee.mdk.docgen.view.impl;

import gov.nasa.jpl.mbee.mdk.docgen.view.*;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.impl.EPackageImpl;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Package</b>. <!--
 * end-user-doc -->
 *
 * @generated
 */
public class DocGenViewPackageImpl extends EPackageImpl implements DocGenViewPackage {
    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private EClass colSpecEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private EClass hasContentEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private EClass imageEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private EClass listEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private EClass listItemEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private EClass paragraphEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private EClass tableEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private EClass tableEntryEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private EClass textEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private EClass viewElementEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private EClass tableRowEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private EClass mdEditableTableEClass = null;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private EEnum fromPropertyEEnum = null;

    /**
     * Creates an instance of the model <b>Package</b>, registered with
     * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the
     * package package URI value.
     * <p>
     * Note: the correct way to create the package is via the static factory
     * method {@link #init init()}, which also performs initialization of the
     * package, or returns the registered package, if one already exists. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @see org.eclipse.emf.ecore.EPackage.Registry
     * @see DocGenViewPackage#eNS_URI
     * @see #init()
     */
    private DocGenViewPackageImpl() {
        super(eNS_URI, DocGenViewFactory.eINSTANCE);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private static boolean isInited = false;

    /**
     * Creates, registers, and initializes the <b>Package</b> for this model,
     * and for any others upon which it depends.
     * <p>
     * <p>
     * This method is used to initialize {@link DocGenViewPackage#eINSTANCE} when
     * that field is accessed. Clients should not invoke it directly. Instead,
     * they should simply access that field to obtain the package. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @see #eNS_URI
     * @see #createPackageContents()
     * @see #initializePackageContents()
     */
    public static DocGenViewPackage init() {
        if (isInited) {
            return (DocGenViewPackage) EPackage.Registry.INSTANCE.getEPackage(DocGenViewPackage.eNS_URI);
        }

        // Obtain or create and register package
        DocGenViewPackageImpl theDgviewPackage = (DocGenViewPackageImpl) (EPackage.Registry.INSTANCE.get(eNS_URI) instanceof DocGenViewPackageImpl
                ? EPackage.Registry.INSTANCE.get(eNS_URI) : new DocGenViewPackageImpl());

        isInited = true;

        // Create package meta-data objects
        theDgviewPackage.createPackageContents();

        // Initialize created meta-data
        theDgviewPackage.initializePackageContents();

        // Mark meta-data to indicate it can't be changed
        theDgviewPackage.freeze();

        // Update the registry and return the package
        EPackage.Registry.INSTANCE.put(DocGenViewPackage.eNS_URI, theDgviewPackage);
        return theDgviewPackage;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EClass getColSpec() {
        return colSpecEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EAttribute getColSpec_Colname() {
        return (EAttribute) colSpecEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EAttribute getColSpec_Colwidth() {
        return (EAttribute) colSpecEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EAttribute getColSpec_Colnum() {
        return (EAttribute) colSpecEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EClass getHasContent() {
        return hasContentEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EReference getHasContent_Children() {
        return (EReference) hasContentEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EClass getImage() {
        return imageEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EAttribute getImage_DiagramId() {
        return (EAttribute) imageEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EAttribute getImage_Caption() {
        return (EAttribute) imageEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EAttribute getImage_Gennew() {
        return (EAttribute) imageEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EAttribute getImage_DoNotShow() {
        return (EAttribute) imageEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EClass getList() {
        return listEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EAttribute getList_Ordered() {
        return (EAttribute) listEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EClass getListItem() {
        return listItemEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EClass getParagraph() {
        return paragraphEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EAttribute getParagraph_Text() {
        return (EAttribute) paragraphEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EClass getTable() {
        return tableEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EReference getTable_Body() {
        return (EReference) tableEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EAttribute getTable_Caption() {
        return (EAttribute) tableEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EAttribute getTable_Style() {
        return (EAttribute) tableEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EReference getTable_Headers() {
        return (EReference) tableEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EReference getTable_Colspecs() {
        return (EReference) tableEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EAttribute getTable_Cols() {
        return (EAttribute) tableEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EClass getTableEntry() {
        return tableEntryEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EAttribute getTableEntry_Morerows() {
        return (EAttribute) tableEntryEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EAttribute getTableEntry_Namest() {
        return (EAttribute) tableEntryEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EAttribute getTableEntry_Nameend() {
        return (EAttribute) tableEntryEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EClass getText() {
        return textEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EAttribute getText_Text() {
        return (EAttribute) textEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EClass getViewElement() {
        return viewElementEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EAttribute getViewElement_Id() {
        return (EAttribute) viewElementEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EAttribute getViewElement_Title() {
        return (EAttribute) viewElementEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EAttribute getViewElement_FromElementId() {
        return (EAttribute) viewElementEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EAttribute getViewElement_FromProperty() {
        return (EAttribute) viewElementEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EClass getTableRow() {
        return tableRowEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EClass getMDEditableTable() {
        return mdEditableTableEClass;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EAttribute getMDEditableTable_Precision() {
        return (EAttribute) mdEditableTableEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EAttribute getMDEditableTable_GuiHeaders() {
        return (EAttribute) mdEditableTableEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EAttribute getMDEditableTable_Editable() {
        return (EAttribute) mdEditableTableEClass.getEStructuralFeatures().get(2);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EAttribute getMDEditableTable_MergeCols() {
        return (EAttribute) mdEditableTableEClass.getEStructuralFeatures().get(3);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EAttribute getMDEditableTable_AddLineNum() {
        return (EAttribute) mdEditableTableEClass.getEStructuralFeatures().get(4);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EReference getMDEditableTable_GuiBody() {
        return (EReference) mdEditableTableEClass.getEStructuralFeatures().get(5);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EEnum getFromProperty() {
        return fromPropertyEEnum;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public DocGenViewFactory getDgviewFactory() {
        return (DocGenViewFactory) getEFactoryInstance();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private boolean isCreated = false;

    /**
     * Creates the meta-model objects for the package. This method is guarded to
     * have no affect on any invocation but its first. <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    public void createPackageContents() {
        if (isCreated) {
            return;
        }
        isCreated = true;

        // Create classes and their features
        colSpecEClass = createEClass(COL_SPEC);
        createEAttribute(colSpecEClass, COL_SPEC__COLNAME);
        createEAttribute(colSpecEClass, COL_SPEC__COLWIDTH);
        createEAttribute(colSpecEClass, COL_SPEC__COLNUM);

        hasContentEClass = createEClass(HAS_CONTENT);
        createEReference(hasContentEClass, HAS_CONTENT__CHILDREN);

        imageEClass = createEClass(IMAGE);
        createEAttribute(imageEClass, IMAGE__DIAGRAM_ID);
        createEAttribute(imageEClass, IMAGE__CAPTION);
        createEAttribute(imageEClass, IMAGE__GENNEW);
        createEAttribute(imageEClass, IMAGE__DO_NOT_SHOW);

        listEClass = createEClass(LIST);
        createEAttribute(listEClass, LIST__ORDERED);

        listItemEClass = createEClass(LIST_ITEM);

        paragraphEClass = createEClass(PARAGRAPH);
        createEAttribute(paragraphEClass, PARAGRAPH__TEXT);

        tableEClass = createEClass(TABLE);
        createEReference(tableEClass, TABLE__BODY);
        createEAttribute(tableEClass, TABLE__CAPTION);
        createEAttribute(tableEClass, TABLE__STYLE);
        createEReference(tableEClass, TABLE__HEADERS);
        createEReference(tableEClass, TABLE__COLSPECS);
        createEAttribute(tableEClass, TABLE__COLS);

        tableEntryEClass = createEClass(TABLE_ENTRY);
        createEAttribute(tableEntryEClass, TABLE_ENTRY__MOREROWS);
        createEAttribute(tableEntryEClass, TABLE_ENTRY__NAMEST);
        createEAttribute(tableEntryEClass, TABLE_ENTRY__NAMEEND);

        textEClass = createEClass(TEXT);
        createEAttribute(textEClass, TEXT__TEXT);

        viewElementEClass = createEClass(VIEW_ELEMENT);
        createEAttribute(viewElementEClass, VIEW_ELEMENT__ID);
        createEAttribute(viewElementEClass, VIEW_ELEMENT__TITLE);
        createEAttribute(viewElementEClass, VIEW_ELEMENT__FROM_ELEMENT_ID);
        createEAttribute(viewElementEClass, VIEW_ELEMENT__FROM_PROPERTY);

        tableRowEClass = createEClass(TABLE_ROW);

        mdEditableTableEClass = createEClass(MD_EDITABLE_TABLE);
        createEAttribute(mdEditableTableEClass, MD_EDITABLE_TABLE__PRECISION);
        createEAttribute(mdEditableTableEClass, MD_EDITABLE_TABLE__GUI_HEADERS);
        createEAttribute(mdEditableTableEClass, MD_EDITABLE_TABLE__EDITABLE);
        createEAttribute(mdEditableTableEClass, MD_EDITABLE_TABLE__MERGE_COLS);
        createEAttribute(mdEditableTableEClass, MD_EDITABLE_TABLE__ADD_LINE_NUM);
        createEReference(mdEditableTableEClass, MD_EDITABLE_TABLE__GUI_BODY);

        // Create enums
        fromPropertyEEnum = createEEnum(FROM_PROPERTY);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    private boolean isInitialized = false;

    /**
     * Complete the initialization of the package and its meta-model. This
     * method is guarded to have no affect on any invocation but its first. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     */
    public void initializePackageContents() {
        if (isInitialized) {
            return;
        }
        isInitialized = true;

        // Initialize package
        setName(eNAME);
        setNsPrefix(eNS_PREFIX);
        setNsURI(eNS_URI);

        // Create type parameters

        // Set bounds for type parameters

        // Add supertypes to classes
        colSpecEClass.getESuperTypes().add(this.getViewElement());
        hasContentEClass.getESuperTypes().add(this.getViewElement());
        imageEClass.getESuperTypes().add(this.getViewElement());
        listEClass.getESuperTypes().add(this.getHasContent());
        listItemEClass.getESuperTypes().add(this.getHasContent());
        paragraphEClass.getESuperTypes().add(this.getViewElement());
        tableEClass.getESuperTypes().add(this.getViewElement());
        tableEntryEClass.getESuperTypes().add(this.getHasContent());
        textEClass.getESuperTypes().add(this.getViewElement());
        tableRowEClass.getESuperTypes().add(this.getHasContent());
        mdEditableTableEClass.getESuperTypes().add(this.getTable());

        // Initialize classes and features; add operations and parameters
        initEClass(colSpecEClass, ColSpec.class, "ColSpec", !IS_ABSTRACT, !IS_INTERFACE,
                IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getColSpec_Colname(), ecorePackage.getEString(), "colname", null, 0, 1, ColSpec.class,
                !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED,
                IS_ORDERED);
        initEAttribute(getColSpec_Colwidth(), ecorePackage.getEString(), "colwidth", null, 0, 1,
                ColSpec.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
                !IS_DERIVED, IS_ORDERED);
        initEAttribute(getColSpec_Colnum(), ecorePackage.getEInt(), "colnum", null, 0, 1, ColSpec.class,
                !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED,
                IS_ORDERED);

        initEClass(hasContentEClass, HasContent.class, "HasContent", IS_ABSTRACT, !IS_INTERFACE,
                IS_GENERATED_INSTANCE_CLASS);
        initEReference(getHasContent_Children(), this.getViewElement(), null, "children", null, 0, -1,
                HasContent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE,
                !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(imageEClass, Image.class, "Image", !IS_ABSTRACT, !IS_INTERFACE,
                IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getImage_DiagramId(), ecorePackage.getEString(), "diagramId", null, 0, 1, Image.class,
                !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED,
                IS_ORDERED);
        initEAttribute(getImage_Caption(), ecorePackage.getEString(), "caption", null, 0, 1, Image.class,
                !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED,
                IS_ORDERED);
        initEAttribute(getImage_Gennew(), ecorePackage.getEBoolean(), "gennew", null, 0, 1, Image.class,
                !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED,
                IS_ORDERED);
        initEAttribute(getImage_DoNotShow(), ecorePackage.getEBoolean(), "doNotShow", null, 0, 1,
                Image.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE,
                !IS_DERIVED, IS_ORDERED);

        initEClass(listEClass, List.class, "List", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getList_Ordered(), ecorePackage.getEBoolean(), "ordered", null, 0, 1, List.class,
                !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED,
                IS_ORDERED);

        initEClass(listItemEClass, ListItem.class, "ListItem", !IS_ABSTRACT, !IS_INTERFACE,
                IS_GENERATED_INSTANCE_CLASS);

        initEClass(paragraphEClass, Paragraph.class, "Paragraph", !IS_ABSTRACT, !IS_INTERFACE,
                IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getParagraph_Text(), ecorePackage.getEString(), "text", null, 0, 1, Paragraph.class,
                !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED,
                IS_ORDERED);

        initEClass(tableEClass, Table.class, "Table", !IS_ABSTRACT, !IS_INTERFACE,
                IS_GENERATED_INSTANCE_CLASS);
        initEReference(getTable_Body(), this.getTableRow(), null, "body", null, 0, -1, Table.class,
                !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES,
                !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getTable_Caption(), ecorePackage.getEString(), "caption", null, 0, 1, Table.class,
                !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED,
                IS_ORDERED);
        initEAttribute(getTable_Style(), ecorePackage.getEString(), "style", null, 0, 1, Table.class,
                !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED,
                IS_ORDERED);
        initEReference(getTable_Headers(), this.getTableRow(), null, "headers", null, 0, -1, Table.class,
                !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES,
                !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getTable_Colspecs(), this.getColSpec(), null, "colspecs", null, 0, -1, Table.class,
                !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES,
                !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getTable_Cols(), ecorePackage.getEInt(), "cols", null, 0, 1, Table.class,
                !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED,
                IS_ORDERED);

        initEClass(tableEntryEClass, TableEntry.class, "TableEntry", !IS_ABSTRACT, !IS_INTERFACE,
                IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getTableEntry_Morerows(), ecorePackage.getEInt(), "morerows", null, 0, 1,
                TableEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
                IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getTableEntry_Namest(), ecorePackage.getEString(), "namest", null, 0, 1,
                TableEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
                IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getTableEntry_Nameend(), ecorePackage.getEString(), "nameend", null, 0, 1,
                TableEntry.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
                IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(textEClass, Text.class, "Text", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getText_Text(), ecorePackage.getEString(), "text", null, 0, 1, Text.class,
                !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED,
                IS_ORDERED);

        initEClass(viewElementEClass, ViewElement.class, "ViewElement", IS_ABSTRACT, !IS_INTERFACE,
                IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getViewElement_Id(), ecorePackage.getEString(), "id", null, 0, 1, ViewElement.class,
                !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED,
                IS_ORDERED);
        initEAttribute(getViewElement_Title(), ecorePackage.getEString(), "title", null, 0, 1,
                ViewElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
                IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getViewElement_FromElementId(), ecorePackage.getEString(), "fromElementId", null, 0,
                1, ViewElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
                IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getViewElement_FromProperty(), this.getFromProperty(), "fromProperty", null, 0, 1,
                ViewElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
                IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(tableRowEClass, TableRow.class, "TableRow", !IS_ABSTRACT, !IS_INTERFACE,
                IS_GENERATED_INSTANCE_CLASS);

        initEClass(mdEditableTableEClass, MDEditableTable.class, "MDEditableTable", !IS_ABSTRACT,
                !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getMDEditableTable_Precision(), ecorePackage.getEInt(), "precision", null, 0, 1,
                MDEditableTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
                IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getMDEditableTable_GuiHeaders(), ecorePackage.getEString(), "guiHeaders", null, 0, -1,
                MDEditableTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
                IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getMDEditableTable_Editable(), ecorePackage.getEBoolean(), "editable", null, 0, -1,
                MDEditableTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
                IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getMDEditableTable_MergeCols(), ecorePackage.getEInt(), "mergeCols", null, 0, -1,
                MDEditableTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
                IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getMDEditableTable_AddLineNum(), ecorePackage.getEBoolean(), "addLineNum", null, 0, 1,
                MDEditableTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID,
                IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEReference(getMDEditableTable_GuiBody(), this.getTableRow(), null, "guiBody", null, 0, -1,
                MDEditableTable.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE,
                !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        // Initialize enums and add enum literals
        initEEnum(fromPropertyEEnum, FromProperty.class, "FromProperty");
        addEEnumLiteral(fromPropertyEEnum, FromProperty.NAME);
        addEEnumLiteral(fromPropertyEEnum, FromProperty.DOCUMENTATION);
        addEEnumLiteral(fromPropertyEEnum, FromProperty.DVALUE);

        // Create resource
        createResource(eNS_URI);
    }

} // DocGenViewPackageImpl
