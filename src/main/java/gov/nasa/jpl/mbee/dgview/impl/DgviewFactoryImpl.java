/**
 * <copyright> </copyright>
 * 
 * $Id$
 */
package gov.nasa.jpl.mbee.dgview.impl;

import gov.nasa.jpl.mbee.dgview.ColSpec;
import gov.nasa.jpl.mbee.dgview.DgviewFactory;
import gov.nasa.jpl.mbee.dgview.DgviewPackage;
import gov.nasa.jpl.mbee.dgview.FromProperty;
import gov.nasa.jpl.mbee.dgview.Image;
import gov.nasa.jpl.mbee.dgview.List;
import gov.nasa.jpl.mbee.dgview.ListItem;
import gov.nasa.jpl.mbee.dgview.MDEditableTable;
import gov.nasa.jpl.mbee.dgview.Paragraph;
import gov.nasa.jpl.mbee.dgview.Table;
import gov.nasa.jpl.mbee.dgview.TableEntry;
import gov.nasa.jpl.mbee.dgview.TableRow;
import gov.nasa.jpl.mbee.dgview.Text;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Factory</b>. <!--
 * end-user-doc -->
 * 
 * @generated
 */
public class DgviewFactoryImpl extends EFactoryImpl implements DgviewFactory {
    /**
     * Creates the default factory implementation. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     */
    public static DgviewFactory init() {
        try {
            DgviewFactory theDgviewFactory = (DgviewFactory)EPackage.Registry.INSTANCE
                    .getEFactory("http://mbee.jpl.nasa.gov/docgen/dgview");
            if (theDgviewFactory != null) {
                return theDgviewFactory;
            }
        } catch (Exception exception) {
            EcorePlugin.INSTANCE.log(exception);
        }
        return new DgviewFactoryImpl();
    }

    /**
     * Creates an instance of the factory. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     */
    public DgviewFactoryImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EObject create(EClass eClass) {
        switch (eClass.getClassifierID()) {
            case DgviewPackage.COL_SPEC:
                return createColSpec();
            case DgviewPackage.IMAGE:
                return createImage();
            case DgviewPackage.LIST:
                return createList();
            case DgviewPackage.LIST_ITEM:
                return createListItem();
            case DgviewPackage.PARAGRAPH:
                return createParagraph();
            case DgviewPackage.TABLE:
                return createTable();
            case DgviewPackage.TABLE_ENTRY:
                return createTableEntry();
            case DgviewPackage.TEXT:
                return createText();
            case DgviewPackage.TABLE_ROW:
                return createTableRow();
            case DgviewPackage.MD_EDITABLE_TABLE:
                return createMDEditableTable();
            default:
                throw new IllegalArgumentException("The class '" + eClass.getName()
                        + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public Object createFromString(EDataType eDataType, String initialValue) {
        switch (eDataType.getClassifierID()) {
            case DgviewPackage.FROM_PROPERTY:
                return createFromPropertyFromString(eDataType, initialValue);
            default:
                throw new IllegalArgumentException("The datatype '" + eDataType.getName()
                        + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String convertToString(EDataType eDataType, Object instanceValue) {
        switch (eDataType.getClassifierID()) {
            case DgviewPackage.FROM_PROPERTY:
                return convertFromPropertyToString(eDataType, instanceValue);
            default:
                throw new IllegalArgumentException("The datatype '" + eDataType.getName()
                        + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public ColSpec createColSpec() {
        ColSpecImpl colSpec = new ColSpecImpl();
        return colSpec;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public Image createImage() {
        ImageImpl image = new ImageImpl();
        return image;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public List createList() {
        ListImpl list = new ListImpl();
        return list;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public ListItem createListItem() {
        ListItemImpl listItem = new ListItemImpl();
        return listItem;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public Paragraph createParagraph() {
        ParagraphImpl paragraph = new ParagraphImpl();
        return paragraph;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public Table createTable() {
        TableImpl table = new TableImpl();
        return table;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public TableEntry createTableEntry() {
        TableEntryImpl tableEntry = new TableEntryImpl();
        return tableEntry;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public Text createText() {
        TextImpl text = new TextImpl();
        return text;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public TableRow createTableRow() {
        TableRowImpl tableRow = new TableRowImpl();
        return tableRow;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public MDEditableTable createMDEditableTable() {
        MDEditableTableImpl mdEditableTable = new MDEditableTableImpl();
        return mdEditableTable;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public FromProperty createFromPropertyFromString(EDataType eDataType, String initialValue) {
        FromProperty result = FromProperty.get(initialValue);
        if (result == null)
            throw new IllegalArgumentException("The value '" + initialValue
                    + "' is not a valid enumerator of '" + eDataType.getName() + "'");
        return result;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    public String convertFromPropertyToString(EDataType eDataType, Object instanceValue) {
        return instanceValue == null ? null : instanceValue.toString();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public DgviewPackage getDgviewPackage() {
        return (DgviewPackage)getEPackage();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @deprecated
     * @generated
     */
    @Deprecated
    public static DgviewPackage getPackage() {
        return DgviewPackage.eINSTANCE;
    }

} // DgviewFactoryImpl
