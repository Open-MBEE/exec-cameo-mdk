package gov.nasa.jpl.mbee.mdk.docgen.view.impl;

import gov.nasa.jpl.mbee.mdk.docgen.view.*;
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
public class DocGenViewFactoryImpl extends EFactoryImpl implements DocGenViewFactory {
    /**
     * Creates the default factory implementation. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @generated
     */
    public static DocGenViewFactory init() {
        try {
            DocGenViewFactory theDocGenViewFactory = (DocGenViewFactory) EPackage.Registry.INSTANCE
                    .getEFactory("http://mbee.jpl.nasa.gov/docgen/dgview");
            if (theDocGenViewFactory != null) {
                return theDocGenViewFactory;
            }
        } catch (Exception exception) {
            EcorePlugin.INSTANCE.log(exception);
        }
        return new DocGenViewFactoryImpl();
    }

    /**
     * Creates an instance of the factory. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     *
     * @generated
     */
    public DocGenViewFactoryImpl() {
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
            case DocGenViewPackage.COL_SPEC:
                return createColSpec();
            case DocGenViewPackage.IMAGE:
                return createImage();
            case DocGenViewPackage.LIST:
                return createList();
            case DocGenViewPackage.LIST_ITEM:
                return createListItem();
            case DocGenViewPackage.PARAGRAPH:
                return createParagraph();
            case DocGenViewPackage.TABLE:
                return createTable();
            case DocGenViewPackage.TABLE_ENTRY:
                return createTableEntry();
            case DocGenViewPackage.TEXT:
                return createText();
            case DocGenViewPackage.TABLE_ROW:
                return createTableRow();
            case DocGenViewPackage.MD_EDITABLE_TABLE:
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
            case DocGenViewPackage.FROM_PROPERTY:
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
            case DocGenViewPackage.FROM_PROPERTY:
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
        if (result == null) {
            throw new IllegalArgumentException("The value '" + initialValue
                    + "' is not a valid enumerator of '" + eDataType.getName() + "'");
        }
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
    public DocGenViewPackage getDgviewPackage() {
        return (DocGenViewPackage) getEPackage();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     *
     * @generated
     * @deprecated
     */
    @Deprecated
    public static DocGenViewPackage getPackage() {
        return DocGenViewPackage.eINSTANCE;
    }

} // DocGenViewFactoryImpl
