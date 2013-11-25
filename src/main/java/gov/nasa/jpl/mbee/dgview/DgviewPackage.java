/**
 * <copyright> </copyright>
 * 
 * $Id$
 */
package gov.nasa.jpl.mbee.dgview;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc --> The <b>Package</b> for the model. It contains
 * accessors for the meta objects to represent
 * <ul>
 * <li>each class,</li>
 * <li>each feature of each class,</li>
 * <li>each enum,</li>
 * <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * 
 * @see gov.nasa.jpl.mbee.dgview.DgviewFactory
 * @model kind="package"
 * @generated
 */
public interface DgviewPackage extends EPackage {
    /**
     * The package name. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    String        eNAME                              = "dgview";

    /**
     * The package namespace URI. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    String        eNS_URI                            = "http://mbee.jpl.nasa.gov/docgen/dgview";

    /**
     * The package namespace name. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    String        eNS_PREFIX                         = "gov.nasa.jpl.mgss.mbee.docgen.dgview";

    /**
     * The singleton instance of the package. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     */
    DgviewPackage eINSTANCE                          = gov.nasa.jpl.mbee.dgview.impl.DgviewPackageImpl
                                                             .init();

    /**
     * The meta object id for the '
     * {@link gov.nasa.jpl.mbee.dgview.impl.ViewElementImpl
     * <em>View Element</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @see gov.nasa.jpl.mbee.dgview.impl.ViewElementImpl
     * @see gov.nasa.jpl.mbee.dgview.impl.DgviewPackageImpl#getViewElement()
     * @generated
     */
    int           VIEW_ELEMENT                       = 9;

    /**
     * The feature id for the '<em><b>Id</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           VIEW_ELEMENT__ID                   = 0;

    /**
     * The feature id for the '<em><b>Title</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           VIEW_ELEMENT__TITLE                = 1;

    /**
     * The feature id for the '<em><b>From Element Id</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           VIEW_ELEMENT__FROM_ELEMENT_ID      = 2;

    /**
     * The feature id for the '<em><b>From Property</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           VIEW_ELEMENT__FROM_PROPERTY        = 3;

    /**
     * The number of structural features of the '<em>View Element</em>' class.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           VIEW_ELEMENT_FEATURE_COUNT         = 4;

    /**
     * The meta object id for the '
     * {@link gov.nasa.jpl.mbee.dgview.impl.ColSpecImpl
     * <em>Col Spec</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see gov.nasa.jpl.mbee.dgview.impl.ColSpecImpl
     * @see gov.nasa.jpl.mbee.dgview.impl.DgviewPackageImpl#getColSpec()
     * @generated
     */
    int           COL_SPEC                           = 0;

    /**
     * The feature id for the '<em><b>Id</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           COL_SPEC__ID                       = VIEW_ELEMENT__ID;

    /**
     * The feature id for the '<em><b>Title</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           COL_SPEC__TITLE                    = VIEW_ELEMENT__TITLE;

    /**
     * The feature id for the '<em><b>From Element Id</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           COL_SPEC__FROM_ELEMENT_ID          = VIEW_ELEMENT__FROM_ELEMENT_ID;

    /**
     * The feature id for the '<em><b>From Property</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           COL_SPEC__FROM_PROPERTY            = VIEW_ELEMENT__FROM_PROPERTY;

    /**
     * The feature id for the '<em><b>Colname</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           COL_SPEC__COLNAME                  = VIEW_ELEMENT_FEATURE_COUNT + 0;

    /**
     * The feature id for the '<em><b>Colwidth</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           COL_SPEC__COLWIDTH                 = VIEW_ELEMENT_FEATURE_COUNT + 1;

    /**
     * The feature id for the '<em><b>Colnum</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           COL_SPEC__COLNUM                   = VIEW_ELEMENT_FEATURE_COUNT + 2;

    /**
     * The number of structural features of the '<em>Col Spec</em>' class. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           COL_SPEC_FEATURE_COUNT             = VIEW_ELEMENT_FEATURE_COUNT + 3;

    /**
     * The meta object id for the '
     * {@link gov.nasa.jpl.mbee.dgview.impl.HasContentImpl
     * <em>Has Content</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @see gov.nasa.jpl.mbee.dgview.impl.HasContentImpl
     * @see gov.nasa.jpl.mbee.dgview.impl.DgviewPackageImpl#getHasContent()
     * @generated
     */
    int           HAS_CONTENT                        = 1;

    /**
     * The feature id for the '<em><b>Id</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           HAS_CONTENT__ID                    = VIEW_ELEMENT__ID;

    /**
     * The feature id for the '<em><b>Title</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           HAS_CONTENT__TITLE                 = VIEW_ELEMENT__TITLE;

    /**
     * The feature id for the '<em><b>From Element Id</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           HAS_CONTENT__FROM_ELEMENT_ID       = VIEW_ELEMENT__FROM_ELEMENT_ID;

    /**
     * The feature id for the '<em><b>From Property</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           HAS_CONTENT__FROM_PROPERTY         = VIEW_ELEMENT__FROM_PROPERTY;

    /**
     * The feature id for the '<em><b>Children</b></em>' containment reference
     * list. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           HAS_CONTENT__CHILDREN              = VIEW_ELEMENT_FEATURE_COUNT + 0;

    /**
     * The number of structural features of the '<em>Has Content</em>' class.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           HAS_CONTENT_FEATURE_COUNT          = VIEW_ELEMENT_FEATURE_COUNT + 1;

    /**
     * The meta object id for the '
     * {@link gov.nasa.jpl.mbee.dgview.impl.ImageImpl
     * <em>Image</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see gov.nasa.jpl.mbee.dgview.impl.ImageImpl
     * @see gov.nasa.jpl.mbee.dgview.impl.DgviewPackageImpl#getImage()
     * @generated
     */
    int           IMAGE                              = 2;

    /**
     * The feature id for the '<em><b>Id</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           IMAGE__ID                          = VIEW_ELEMENT__ID;

    /**
     * The feature id for the '<em><b>Title</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           IMAGE__TITLE                       = VIEW_ELEMENT__TITLE;

    /**
     * The feature id for the '<em><b>From Element Id</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           IMAGE__FROM_ELEMENT_ID             = VIEW_ELEMENT__FROM_ELEMENT_ID;

    /**
     * The feature id for the '<em><b>From Property</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           IMAGE__FROM_PROPERTY               = VIEW_ELEMENT__FROM_PROPERTY;

    /**
     * The feature id for the '<em><b>Diagram Id</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           IMAGE__DIAGRAM_ID                  = VIEW_ELEMENT_FEATURE_COUNT + 0;

    /**
     * The feature id for the '<em><b>Caption</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           IMAGE__CAPTION                     = VIEW_ELEMENT_FEATURE_COUNT + 1;

    /**
     * The feature id for the '<em><b>Gennew</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           IMAGE__GENNEW                      = VIEW_ELEMENT_FEATURE_COUNT + 2;

    /**
     * The feature id for the '<em><b>Do Not Show</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           IMAGE__DO_NOT_SHOW                 = VIEW_ELEMENT_FEATURE_COUNT + 3;

    /**
     * The number of structural features of the '<em>Image</em>' class. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           IMAGE_FEATURE_COUNT                = VIEW_ELEMENT_FEATURE_COUNT + 4;

    /**
     * The meta object id for the '
     * {@link gov.nasa.jpl.mbee.dgview.impl.ListImpl <em>List</em>}'
     * class. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see gov.nasa.jpl.mbee.dgview.impl.ListImpl
     * @see gov.nasa.jpl.mbee.dgview.impl.DgviewPackageImpl#getList()
     * @generated
     */
    int           LIST                               = 3;

    /**
     * The feature id for the '<em><b>Id</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           LIST__ID                           = HAS_CONTENT__ID;

    /**
     * The feature id for the '<em><b>Title</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           LIST__TITLE                        = HAS_CONTENT__TITLE;

    /**
     * The feature id for the '<em><b>From Element Id</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           LIST__FROM_ELEMENT_ID              = HAS_CONTENT__FROM_ELEMENT_ID;

    /**
     * The feature id for the '<em><b>From Property</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           LIST__FROM_PROPERTY                = HAS_CONTENT__FROM_PROPERTY;

    /**
     * The feature id for the '<em><b>Children</b></em>' containment reference
     * list. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           LIST__CHILDREN                     = HAS_CONTENT__CHILDREN;

    /**
     * The feature id for the '<em><b>Ordered</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           LIST__ORDERED                      = HAS_CONTENT_FEATURE_COUNT + 0;

    /**
     * The number of structural features of the '<em>List</em>' class. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           LIST_FEATURE_COUNT                 = HAS_CONTENT_FEATURE_COUNT + 1;

    /**
     * The meta object id for the '
     * {@link gov.nasa.jpl.mbee.dgview.impl.ListItemImpl
     * <em>List Item</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see gov.nasa.jpl.mbee.dgview.impl.ListItemImpl
     * @see gov.nasa.jpl.mbee.dgview.impl.DgviewPackageImpl#getListItem()
     * @generated
     */
    int           LIST_ITEM                          = 4;

    /**
     * The feature id for the '<em><b>Id</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           LIST_ITEM__ID                      = HAS_CONTENT__ID;

    /**
     * The feature id for the '<em><b>Title</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           LIST_ITEM__TITLE                   = HAS_CONTENT__TITLE;

    /**
     * The feature id for the '<em><b>From Element Id</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           LIST_ITEM__FROM_ELEMENT_ID         = HAS_CONTENT__FROM_ELEMENT_ID;

    /**
     * The feature id for the '<em><b>From Property</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           LIST_ITEM__FROM_PROPERTY           = HAS_CONTENT__FROM_PROPERTY;

    /**
     * The feature id for the '<em><b>Children</b></em>' containment reference
     * list. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           LIST_ITEM__CHILDREN                = HAS_CONTENT__CHILDREN;

    /**
     * The number of structural features of the '<em>List Item</em>' class. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           LIST_ITEM_FEATURE_COUNT            = HAS_CONTENT_FEATURE_COUNT + 0;

    /**
     * The meta object id for the '
     * {@link gov.nasa.jpl.mbee.dgview.impl.ParagraphImpl
     * <em>Paragraph</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see gov.nasa.jpl.mbee.dgview.impl.ParagraphImpl
     * @see gov.nasa.jpl.mbee.dgview.impl.DgviewPackageImpl#getParagraph()
     * @generated
     */
    int           PARAGRAPH                          = 5;

    /**
     * The feature id for the '<em><b>Id</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           PARAGRAPH__ID                      = VIEW_ELEMENT__ID;

    /**
     * The feature id for the '<em><b>Title</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           PARAGRAPH__TITLE                   = VIEW_ELEMENT__TITLE;

    /**
     * The feature id for the '<em><b>From Element Id</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           PARAGRAPH__FROM_ELEMENT_ID         = VIEW_ELEMENT__FROM_ELEMENT_ID;

    /**
     * The feature id for the '<em><b>From Property</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           PARAGRAPH__FROM_PROPERTY           = VIEW_ELEMENT__FROM_PROPERTY;

    /**
     * The feature id for the '<em><b>Text</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           PARAGRAPH__TEXT                    = VIEW_ELEMENT_FEATURE_COUNT + 0;

    /**
     * The number of structural features of the '<em>Paragraph</em>' class. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           PARAGRAPH_FEATURE_COUNT            = VIEW_ELEMENT_FEATURE_COUNT + 1;

    /**
     * The meta object id for the '
     * {@link gov.nasa.jpl.mbee.dgview.impl.TableImpl
     * <em>Table</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see gov.nasa.jpl.mbee.dgview.impl.TableImpl
     * @see gov.nasa.jpl.mbee.dgview.impl.DgviewPackageImpl#getTable()
     * @generated
     */
    int           TABLE                              = 6;

    /**
     * The feature id for the '<em><b>Id</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TABLE__ID                          = VIEW_ELEMENT__ID;

    /**
     * The feature id for the '<em><b>Title</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TABLE__TITLE                       = VIEW_ELEMENT__TITLE;

    /**
     * The feature id for the '<em><b>From Element Id</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TABLE__FROM_ELEMENT_ID             = VIEW_ELEMENT__FROM_ELEMENT_ID;

    /**
     * The feature id for the '<em><b>From Property</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TABLE__FROM_PROPERTY               = VIEW_ELEMENT__FROM_PROPERTY;

    /**
     * The feature id for the '<em><b>Body</b></em>' containment reference list.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TABLE__BODY                        = VIEW_ELEMENT_FEATURE_COUNT + 0;

    /**
     * The feature id for the '<em><b>Caption</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TABLE__CAPTION                     = VIEW_ELEMENT_FEATURE_COUNT + 1;

    /**
     * The feature id for the '<em><b>Style</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TABLE__STYLE                       = VIEW_ELEMENT_FEATURE_COUNT + 2;

    /**
     * The feature id for the '<em><b>Headers</b></em>' containment reference
     * list. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TABLE__HEADERS                     = VIEW_ELEMENT_FEATURE_COUNT + 3;

    /**
     * The feature id for the '<em><b>Colspecs</b></em>' containment reference
     * list. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TABLE__COLSPECS                    = VIEW_ELEMENT_FEATURE_COUNT + 4;

    /**
     * The feature id for the '<em><b>Cols</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TABLE__COLS                        = VIEW_ELEMENT_FEATURE_COUNT + 5;

    /**
     * The number of structural features of the '<em>Table</em>' class. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TABLE_FEATURE_COUNT                = VIEW_ELEMENT_FEATURE_COUNT + 6;

    /**
     * The meta object id for the '
     * {@link gov.nasa.jpl.mbee.dgview.impl.TableEntryImpl
     * <em>Table Entry</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @see gov.nasa.jpl.mbee.dgview.impl.TableEntryImpl
     * @see gov.nasa.jpl.mbee.dgview.impl.DgviewPackageImpl#getTableEntry()
     * @generated
     */
    int           TABLE_ENTRY                        = 7;

    /**
     * The feature id for the '<em><b>Id</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TABLE_ENTRY__ID                    = HAS_CONTENT__ID;

    /**
     * The feature id for the '<em><b>Title</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TABLE_ENTRY__TITLE                 = HAS_CONTENT__TITLE;

    /**
     * The feature id for the '<em><b>From Element Id</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TABLE_ENTRY__FROM_ELEMENT_ID       = HAS_CONTENT__FROM_ELEMENT_ID;

    /**
     * The feature id for the '<em><b>From Property</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TABLE_ENTRY__FROM_PROPERTY         = HAS_CONTENT__FROM_PROPERTY;

    /**
     * The feature id for the '<em><b>Children</b></em>' containment reference
     * list. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TABLE_ENTRY__CHILDREN              = HAS_CONTENT__CHILDREN;

    /**
     * The feature id for the '<em><b>Morerows</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TABLE_ENTRY__MOREROWS              = HAS_CONTENT_FEATURE_COUNT + 0;

    /**
     * The feature id for the '<em><b>Namest</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TABLE_ENTRY__NAMEST                = HAS_CONTENT_FEATURE_COUNT + 1;

    /**
     * The feature id for the '<em><b>Nameend</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TABLE_ENTRY__NAMEEND               = HAS_CONTENT_FEATURE_COUNT + 2;

    /**
     * The number of structural features of the '<em>Table Entry</em>' class.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TABLE_ENTRY_FEATURE_COUNT          = HAS_CONTENT_FEATURE_COUNT + 3;

    /**
     * The meta object id for the '
     * {@link gov.nasa.jpl.mbee.dgview.impl.TextImpl <em>Text</em>}'
     * class. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see gov.nasa.jpl.mbee.dgview.impl.TextImpl
     * @see gov.nasa.jpl.mbee.dgview.impl.DgviewPackageImpl#getText()
     * @generated
     */
    int           TEXT                               = 8;

    /**
     * The feature id for the '<em><b>Id</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TEXT__ID                           = VIEW_ELEMENT__ID;

    /**
     * The feature id for the '<em><b>Title</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TEXT__TITLE                        = VIEW_ELEMENT__TITLE;

    /**
     * The feature id for the '<em><b>From Element Id</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TEXT__FROM_ELEMENT_ID              = VIEW_ELEMENT__FROM_ELEMENT_ID;

    /**
     * The feature id for the '<em><b>From Property</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TEXT__FROM_PROPERTY                = VIEW_ELEMENT__FROM_PROPERTY;

    /**
     * The feature id for the '<em><b>Text</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TEXT__TEXT                         = VIEW_ELEMENT_FEATURE_COUNT + 0;

    /**
     * The number of structural features of the '<em>Text</em>' class. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TEXT_FEATURE_COUNT                 = VIEW_ELEMENT_FEATURE_COUNT + 1;

    /**
     * The meta object id for the '
     * {@link gov.nasa.jpl.mbee.dgview.impl.TableRowImpl
     * <em>Table Row</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see gov.nasa.jpl.mbee.dgview.impl.TableRowImpl
     * @see gov.nasa.jpl.mbee.dgview.impl.DgviewPackageImpl#getTableRow()
     * @generated
     */
    int           TABLE_ROW                          = 10;

    /**
     * The feature id for the '<em><b>Id</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TABLE_ROW__ID                      = HAS_CONTENT__ID;

    /**
     * The feature id for the '<em><b>Title</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TABLE_ROW__TITLE                   = HAS_CONTENT__TITLE;

    /**
     * The feature id for the '<em><b>From Element Id</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TABLE_ROW__FROM_ELEMENT_ID         = HAS_CONTENT__FROM_ELEMENT_ID;

    /**
     * The feature id for the '<em><b>From Property</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TABLE_ROW__FROM_PROPERTY           = HAS_CONTENT__FROM_PROPERTY;

    /**
     * The feature id for the '<em><b>Children</b></em>' containment reference
     * list. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TABLE_ROW__CHILDREN                = HAS_CONTENT__CHILDREN;

    /**
     * The number of structural features of the '<em>Table Row</em>' class. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           TABLE_ROW_FEATURE_COUNT            = HAS_CONTENT_FEATURE_COUNT + 0;

    /**
     * The meta object id for the '
     * {@link gov.nasa.jpl.mbee.dgview.impl.MDEditableTableImpl
     * <em>MD Editable Table</em>}' class. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @see gov.nasa.jpl.mbee.dgview.impl.MDEditableTableImpl
     * @see gov.nasa.jpl.mbee.dgview.impl.DgviewPackageImpl#getMDEditableTable()
     * @generated
     */
    int           MD_EDITABLE_TABLE                  = 11;

    /**
     * The feature id for the '<em><b>Id</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           MD_EDITABLE_TABLE__ID              = TABLE__ID;

    /**
     * The feature id for the '<em><b>Title</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           MD_EDITABLE_TABLE__TITLE           = TABLE__TITLE;

    /**
     * The feature id for the '<em><b>From Element Id</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           MD_EDITABLE_TABLE__FROM_ELEMENT_ID = TABLE__FROM_ELEMENT_ID;

    /**
     * The feature id for the '<em><b>From Property</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           MD_EDITABLE_TABLE__FROM_PROPERTY   = TABLE__FROM_PROPERTY;

    /**
     * The feature id for the '<em><b>Body</b></em>' containment reference list.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           MD_EDITABLE_TABLE__BODY            = TABLE__BODY;

    /**
     * The feature id for the '<em><b>Caption</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           MD_EDITABLE_TABLE__CAPTION         = TABLE__CAPTION;

    /**
     * The feature id for the '<em><b>Style</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           MD_EDITABLE_TABLE__STYLE           = TABLE__STYLE;

    /**
     * The feature id for the '<em><b>Headers</b></em>' containment reference
     * list. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           MD_EDITABLE_TABLE__HEADERS         = TABLE__HEADERS;

    /**
     * The feature id for the '<em><b>Colspecs</b></em>' containment reference
     * list. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           MD_EDITABLE_TABLE__COLSPECS        = TABLE__COLSPECS;

    /**
     * The feature id for the '<em><b>Cols</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           MD_EDITABLE_TABLE__COLS            = TABLE__COLS;

    /**
     * The feature id for the '<em><b>Precision</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           MD_EDITABLE_TABLE__PRECISION       = TABLE_FEATURE_COUNT + 0;

    /**
     * The feature id for the '<em><b>Gui Headers</b></em>' attribute list. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           MD_EDITABLE_TABLE__GUI_HEADERS     = TABLE_FEATURE_COUNT + 1;

    /**
     * The feature id for the '<em><b>Editable</b></em>' attribute list. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           MD_EDITABLE_TABLE__EDITABLE        = TABLE_FEATURE_COUNT + 2;

    /**
     * The feature id for the '<em><b>Merge Cols</b></em>' attribute list. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           MD_EDITABLE_TABLE__MERGE_COLS      = TABLE_FEATURE_COUNT + 3;

    /**
     * The feature id for the '<em><b>Add Line Num</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           MD_EDITABLE_TABLE__ADD_LINE_NUM    = TABLE_FEATURE_COUNT + 4;

    /**
     * The feature id for the '<em><b>Gui Body</b></em>' containment reference
     * list. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           MD_EDITABLE_TABLE__GUI_BODY        = TABLE_FEATURE_COUNT + 5;

    /**
     * The number of structural features of the '<em>MD Editable Table</em>'
     * class. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     * @ordered
     */
    int           MD_EDITABLE_TABLE_FEATURE_COUNT    = TABLE_FEATURE_COUNT + 6;

    /**
     * The meta object id for the '
     * {@link gov.nasa.jpl.mbee.dgview.FromProperty
     * <em>From Property</em>}' enum. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @see gov.nasa.jpl.mbee.dgview.FromProperty
     * @see gov.nasa.jpl.mbee.dgview.impl.DgviewPackageImpl#getFromProperty()
     * @generated
     */
    int           FROM_PROPERTY                      = 12;

    /**
     * Returns the meta object for class '
     * {@link gov.nasa.jpl.mbee.dgview.ColSpec <em>Col Spec</em>}'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for class '<em>Col Spec</em>'.
     * @see gov.nasa.jpl.mbee.dgview.ColSpec
     * @generated
     */
    EClass getColSpec();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgview.ColSpec#getColname
     * <em>Colname</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Colname</em>'.
     * @see gov.nasa.jpl.mbee.dgview.ColSpec#getColname()
     * @see #getColSpec()
     * @generated
     */
    EAttribute getColSpec_Colname();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgview.ColSpec#getColwidth
     * <em>Colwidth</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Colwidth</em>'.
     * @see gov.nasa.jpl.mbee.dgview.ColSpec#getColwidth()
     * @see #getColSpec()
     * @generated
     */
    EAttribute getColSpec_Colwidth();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgview.ColSpec#getColnum
     * <em>Colnum</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Colnum</em>'.
     * @see gov.nasa.jpl.mbee.dgview.ColSpec#getColnum()
     * @see #getColSpec()
     * @generated
     */
    EAttribute getColSpec_Colnum();

    /**
     * Returns the meta object for class '
     * {@link gov.nasa.jpl.mbee.dgview.HasContent
     * <em>Has Content</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for class '<em>Has Content</em>'.
     * @see gov.nasa.jpl.mbee.dgview.HasContent
     * @generated
     */
    EClass getHasContent();

    /**
     * Returns the meta object for the containment reference list '
     * {@link gov.nasa.jpl.mbee.dgview.HasContent#getChildren
     * <em>Children</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the containment reference list '
     *         <em>Children</em>'.
     * @see gov.nasa.jpl.mbee.dgview.HasContent#getChildren()
     * @see #getHasContent()
     * @generated
     */
    EReference getHasContent_Children();

    /**
     * Returns the meta object for class '
     * {@link gov.nasa.jpl.mbee.dgview.Image <em>Image</em>}'. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for class '<em>Image</em>'.
     * @see gov.nasa.jpl.mbee.dgview.Image
     * @generated
     */
    EClass getImage();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgview.Image#getDiagramId
     * <em>Diagram Id</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Diagram Id</em>'.
     * @see gov.nasa.jpl.mbee.dgview.Image#getDiagramId()
     * @see #getImage()
     * @generated
     */
    EAttribute getImage_DiagramId();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgview.Image#getCaption
     * <em>Caption</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Caption</em>'.
     * @see gov.nasa.jpl.mbee.dgview.Image#getCaption()
     * @see #getImage()
     * @generated
     */
    EAttribute getImage_Caption();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgview.Image#isGennew
     * <em>Gennew</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Gennew</em>'.
     * @see gov.nasa.jpl.mbee.dgview.Image#isGennew()
     * @see #getImage()
     * @generated
     */
    EAttribute getImage_Gennew();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgview.Image#isDoNotShow
     * <em>Do Not Show</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Do Not Show</em>'.
     * @see gov.nasa.jpl.mbee.dgview.Image#isDoNotShow()
     * @see #getImage()
     * @generated
     */
    EAttribute getImage_DoNotShow();

    /**
     * Returns the meta object for class '
     * {@link gov.nasa.jpl.mbee.dgview.List <em>List</em>}'. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for class '<em>List</em>'.
     * @see gov.nasa.jpl.mbee.dgview.List
     * @generated
     */
    EClass getList();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgview.List#isOrdered
     * <em>Ordered</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Ordered</em>'.
     * @see gov.nasa.jpl.mbee.dgview.List#isOrdered()
     * @see #getList()
     * @generated
     */
    EAttribute getList_Ordered();

    /**
     * Returns the meta object for class '
     * {@link gov.nasa.jpl.mbee.dgview.ListItem <em>List Item</em>}
     * '. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for class '<em>List Item</em>'.
     * @see gov.nasa.jpl.mbee.dgview.ListItem
     * @generated
     */
    EClass getListItem();

    /**
     * Returns the meta object for class '
     * {@link gov.nasa.jpl.mbee.dgview.Paragraph <em>Paragraph</em>}
     * '. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for class '<em>Paragraph</em>'.
     * @see gov.nasa.jpl.mbee.dgview.Paragraph
     * @generated
     */
    EClass getParagraph();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgview.Paragraph#getText
     * <em>Text</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Text</em>'.
     * @see gov.nasa.jpl.mbee.dgview.Paragraph#getText()
     * @see #getParagraph()
     * @generated
     */
    EAttribute getParagraph_Text();

    /**
     * Returns the meta object for class '
     * {@link gov.nasa.jpl.mbee.dgview.Table <em>Table</em>}'. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for class '<em>Table</em>'.
     * @see gov.nasa.jpl.mbee.dgview.Table
     * @generated
     */
    EClass getTable();

    /**
     * Returns the meta object for the containment reference list '
     * {@link gov.nasa.jpl.mbee.dgview.Table#getBody <em>Body</em>}
     * '. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the containment reference list '<em>Body</em>
     *         '.
     * @see gov.nasa.jpl.mbee.dgview.Table#getBody()
     * @see #getTable()
     * @generated
     */
    EReference getTable_Body();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgview.Table#getCaption
     * <em>Caption</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Caption</em>'.
     * @see gov.nasa.jpl.mbee.dgview.Table#getCaption()
     * @see #getTable()
     * @generated
     */
    EAttribute getTable_Caption();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgview.Table#getStyle
     * <em>Style</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Style</em>'.
     * @see gov.nasa.jpl.mbee.dgview.Table#getStyle()
     * @see #getTable()
     * @generated
     */
    EAttribute getTable_Style();

    /**
     * Returns the meta object for the containment reference list '
     * {@link gov.nasa.jpl.mbee.dgview.Table#getHeaders
     * <em>Headers</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the containment reference list '
     *         <em>Headers</em>'.
     * @see gov.nasa.jpl.mbee.dgview.Table#getHeaders()
     * @see #getTable()
     * @generated
     */
    EReference getTable_Headers();

    /**
     * Returns the meta object for the containment reference list '
     * {@link gov.nasa.jpl.mbee.dgview.Table#getColspecs
     * <em>Colspecs</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the containment reference list '
     *         <em>Colspecs</em>'.
     * @see gov.nasa.jpl.mbee.dgview.Table#getColspecs()
     * @see #getTable()
     * @generated
     */
    EReference getTable_Colspecs();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgview.Table#getCols <em>Cols</em>}
     * '. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Cols</em>'.
     * @see gov.nasa.jpl.mbee.dgview.Table#getCols()
     * @see #getTable()
     * @generated
     */
    EAttribute getTable_Cols();

    /**
     * Returns the meta object for class '
     * {@link gov.nasa.jpl.mbee.dgview.TableEntry
     * <em>Table Entry</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for class '<em>Table Entry</em>'.
     * @see gov.nasa.jpl.mbee.dgview.TableEntry
     * @generated
     */
    EClass getTableEntry();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgview.TableEntry#getMorerows
     * <em>Morerows</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Morerows</em>'.
     * @see gov.nasa.jpl.mbee.dgview.TableEntry#getMorerows()
     * @see #getTableEntry()
     * @generated
     */
    EAttribute getTableEntry_Morerows();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgview.TableEntry#getNamest
     * <em>Namest</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Namest</em>'.
     * @see gov.nasa.jpl.mbee.dgview.TableEntry#getNamest()
     * @see #getTableEntry()
     * @generated
     */
    EAttribute getTableEntry_Namest();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgview.TableEntry#getNameend
     * <em>Nameend</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Nameend</em>'.
     * @see gov.nasa.jpl.mbee.dgview.TableEntry#getNameend()
     * @see #getTableEntry()
     * @generated
     */
    EAttribute getTableEntry_Nameend();

    /**
     * Returns the meta object for class '
     * {@link gov.nasa.jpl.mbee.dgview.Text <em>Text</em>}'. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for class '<em>Text</em>'.
     * @see gov.nasa.jpl.mbee.dgview.Text
     * @generated
     */
    EClass getText();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgview.Text#getText <em>Text</em>}'.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Text</em>'.
     * @see gov.nasa.jpl.mbee.dgview.Text#getText()
     * @see #getText()
     * @generated
     */
    EAttribute getText_Text();

    /**
     * Returns the meta object for class '
     * {@link gov.nasa.jpl.mbee.dgview.ViewElement
     * <em>View Element</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for class '<em>View Element</em>'.
     * @see gov.nasa.jpl.mbee.dgview.ViewElement
     * @generated
     */
    EClass getViewElement();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgview.ViewElement#getId
     * <em>Id</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Id</em>'.
     * @see gov.nasa.jpl.mbee.dgview.ViewElement#getId()
     * @see #getViewElement()
     * @generated
     */
    EAttribute getViewElement_Id();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgview.ViewElement#getTitle
     * <em>Title</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Title</em>'.
     * @see gov.nasa.jpl.mbee.dgview.ViewElement#getTitle()
     * @see #getViewElement()
     * @generated
     */
    EAttribute getViewElement_Title();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgview.ViewElement#getFromElementId
     * <em>From Element Id</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>From Element Id</em>'.
     * @see gov.nasa.jpl.mbee.dgview.ViewElement#getFromElementId()
     * @see #getViewElement()
     * @generated
     */
    EAttribute getViewElement_FromElementId();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgview.ViewElement#getFromProperty
     * <em>From Property</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>From Property</em>'.
     * @see gov.nasa.jpl.mbee.dgview.ViewElement#getFromProperty()
     * @see #getViewElement()
     * @generated
     */
    EAttribute getViewElement_FromProperty();

    /**
     * Returns the meta object for class '
     * {@link gov.nasa.jpl.mbee.dgview.TableRow <em>Table Row</em>}
     * '. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for class '<em>Table Row</em>'.
     * @see gov.nasa.jpl.mbee.dgview.TableRow
     * @generated
     */
    EClass getTableRow();

    /**
     * Returns the meta object for class '
     * {@link gov.nasa.jpl.mbee.dgview.MDEditableTable
     * <em>MD Editable Table</em>}'. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @return the meta object for class '<em>MD Editable Table</em>'.
     * @see gov.nasa.jpl.mbee.dgview.MDEditableTable
     * @generated
     */
    EClass getMDEditableTable();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgview.MDEditableTable#getPrecision
     * <em>Precision</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Precision</em>'.
     * @see gov.nasa.jpl.mbee.dgview.MDEditableTable#getPrecision()
     * @see #getMDEditableTable()
     * @generated
     */
    EAttribute getMDEditableTable_Precision();

    /**
     * Returns the meta object for the attribute list '
     * {@link gov.nasa.jpl.mbee.dgview.MDEditableTable#getGuiHeaders
     * <em>Gui Headers</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute list '<em>Gui Headers</em>'.
     * @see gov.nasa.jpl.mbee.dgview.MDEditableTable#getGuiHeaders()
     * @see #getMDEditableTable()
     * @generated
     */
    EAttribute getMDEditableTable_GuiHeaders();

    /**
     * Returns the meta object for the attribute list '
     * {@link gov.nasa.jpl.mbee.dgview.MDEditableTable#getEditable
     * <em>Editable</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute list '<em>Editable</em>'.
     * @see gov.nasa.jpl.mbee.dgview.MDEditableTable#getEditable()
     * @see #getMDEditableTable()
     * @generated
     */
    EAttribute getMDEditableTable_Editable();

    /**
     * Returns the meta object for the attribute list '
     * {@link gov.nasa.jpl.mbee.dgview.MDEditableTable#getMergeCols
     * <em>Merge Cols</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute list '<em>Merge Cols</em>'.
     * @see gov.nasa.jpl.mbee.dgview.MDEditableTable#getMergeCols()
     * @see #getMDEditableTable()
     * @generated
     */
    EAttribute getMDEditableTable_MergeCols();

    /**
     * Returns the meta object for the attribute '
     * {@link gov.nasa.jpl.mbee.dgview.MDEditableTable#isAddLineNum
     * <em>Add Line Num</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the attribute '<em>Add Line Num</em>'.
     * @see gov.nasa.jpl.mbee.dgview.MDEditableTable#isAddLineNum()
     * @see #getMDEditableTable()
     * @generated
     */
    EAttribute getMDEditableTable_AddLineNum();

    /**
     * Returns the meta object for the containment reference list '
     * {@link gov.nasa.jpl.mbee.dgview.MDEditableTable#getGuiBody
     * <em>Gui Body</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for the containment reference list '
     *         <em>Gui Body</em>'.
     * @see gov.nasa.jpl.mbee.dgview.MDEditableTable#getGuiBody()
     * @see #getMDEditableTable()
     * @generated
     */
    EReference getMDEditableTable_GuiBody();

    /**
     * Returns the meta object for enum '
     * {@link gov.nasa.jpl.mbee.dgview.FromProperty
     * <em>From Property</em>}'. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the meta object for enum '<em>From Property</em>'.
     * @see gov.nasa.jpl.mbee.dgview.FromProperty
     * @generated
     */
    EEnum getFromProperty();

    /**
     * Returns the factory that creates the instances of the model. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the factory that creates the instances of the model.
     * @generated
     */
    DgviewFactory getDgviewFactory();

    /**
     * <!-- begin-user-doc --> Defines literals for the meta objects that
     * represent
     * <ul>
     * <li>each class,</li>
     * <li>each feature of each class,</li>
     * <li>each enum,</li>
     * <li>and each data type</li>
     * </ul>
     * <!-- end-user-doc -->
     * 
     * @generated
     */
    interface Literals {
        /**
         * The meta object literal for the '
         * {@link gov.nasa.jpl.mbee.dgview.impl.ColSpecImpl
         * <em>Col Spec</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc
         * -->
         * 
         * @see gov.nasa.jpl.mbee.dgview.impl.ColSpecImpl
         * @see gov.nasa.jpl.mbee.dgview.impl.DgviewPackageImpl#getColSpec()
         * @generated
         */
        EClass     COL_SPEC                        = eINSTANCE.getColSpec();

        /**
         * The meta object literal for the '<em><b>Colname</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute COL_SPEC__COLNAME               = eINSTANCE.getColSpec_Colname();

        /**
         * The meta object literal for the '<em><b>Colwidth</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute COL_SPEC__COLWIDTH              = eINSTANCE.getColSpec_Colwidth();

        /**
         * The meta object literal for the '<em><b>Colnum</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute COL_SPEC__COLNUM                = eINSTANCE.getColSpec_Colnum();

        /**
         * The meta object literal for the '
         * {@link gov.nasa.jpl.mbee.dgview.impl.HasContentImpl
         * <em>Has Content</em>}' class. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * 
         * @see gov.nasa.jpl.mbee.dgview.impl.HasContentImpl
         * @see gov.nasa.jpl.mbee.dgview.impl.DgviewPackageImpl#getHasContent()
         * @generated
         */
        EClass     HAS_CONTENT                     = eINSTANCE.getHasContent();

        /**
         * The meta object literal for the '<em><b>Children</b></em>'
         * containment reference list feature. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * 
         * @generated
         */
        EReference HAS_CONTENT__CHILDREN           = eINSTANCE.getHasContent_Children();

        /**
         * The meta object literal for the '
         * {@link gov.nasa.jpl.mbee.dgview.impl.ImageImpl
         * <em>Image</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @see gov.nasa.jpl.mbee.dgview.impl.ImageImpl
         * @see gov.nasa.jpl.mbee.dgview.impl.DgviewPackageImpl#getImage()
         * @generated
         */
        EClass     IMAGE                           = eINSTANCE.getImage();

        /**
         * The meta object literal for the '<em><b>Diagram Id</b></em>'
         * attribute feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute IMAGE__DIAGRAM_ID               = eINSTANCE.getImage_DiagramId();

        /**
         * The meta object literal for the '<em><b>Caption</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute IMAGE__CAPTION                  = eINSTANCE.getImage_Caption();

        /**
         * The meta object literal for the '<em><b>Gennew</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute IMAGE__GENNEW                   = eINSTANCE.getImage_Gennew();

        /**
         * The meta object literal for the '<em><b>Do Not Show</b></em>'
         * attribute feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute IMAGE__DO_NOT_SHOW              = eINSTANCE.getImage_DoNotShow();

        /**
         * The meta object literal for the '
         * {@link gov.nasa.jpl.mbee.dgview.impl.ListImpl
         * <em>List</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @see gov.nasa.jpl.mbee.dgview.impl.ListImpl
         * @see gov.nasa.jpl.mbee.dgview.impl.DgviewPackageImpl#getList()
         * @generated
         */
        EClass     LIST                            = eINSTANCE.getList();

        /**
         * The meta object literal for the '<em><b>Ordered</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute LIST__ORDERED                   = eINSTANCE.getList_Ordered();

        /**
         * The meta object literal for the '
         * {@link gov.nasa.jpl.mbee.dgview.impl.ListItemImpl
         * <em>List Item</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc
         * -->
         * 
         * @see gov.nasa.jpl.mbee.dgview.impl.ListItemImpl
         * @see gov.nasa.jpl.mbee.dgview.impl.DgviewPackageImpl#getListItem()
         * @generated
         */
        EClass     LIST_ITEM                       = eINSTANCE.getListItem();

        /**
         * The meta object literal for the '
         * {@link gov.nasa.jpl.mbee.dgview.impl.ParagraphImpl
         * <em>Paragraph</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc
         * -->
         * 
         * @see gov.nasa.jpl.mbee.dgview.impl.ParagraphImpl
         * @see gov.nasa.jpl.mbee.dgview.impl.DgviewPackageImpl#getParagraph()
         * @generated
         */
        EClass     PARAGRAPH                       = eINSTANCE.getParagraph();

        /**
         * The meta object literal for the '<em><b>Text</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute PARAGRAPH__TEXT                 = eINSTANCE.getParagraph_Text();

        /**
         * The meta object literal for the '
         * {@link gov.nasa.jpl.mbee.dgview.impl.TableImpl
         * <em>Table</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @see gov.nasa.jpl.mbee.dgview.impl.TableImpl
         * @see gov.nasa.jpl.mbee.dgview.impl.DgviewPackageImpl#getTable()
         * @generated
         */
        EClass     TABLE                           = eINSTANCE.getTable();

        /**
         * The meta object literal for the '<em><b>Body</b></em>' containment
         * reference list feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EReference TABLE__BODY                     = eINSTANCE.getTable_Body();

        /**
         * The meta object literal for the '<em><b>Caption</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute TABLE__CAPTION                  = eINSTANCE.getTable_Caption();

        /**
         * The meta object literal for the '<em><b>Style</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute TABLE__STYLE                    = eINSTANCE.getTable_Style();

        /**
         * The meta object literal for the '<em><b>Headers</b></em>' containment
         * reference list feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EReference TABLE__HEADERS                  = eINSTANCE.getTable_Headers();

        /**
         * The meta object literal for the '<em><b>Colspecs</b></em>'
         * containment reference list feature. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * 
         * @generated
         */
        EReference TABLE__COLSPECS                 = eINSTANCE.getTable_Colspecs();

        /**
         * The meta object literal for the '<em><b>Cols</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute TABLE__COLS                     = eINSTANCE.getTable_Cols();

        /**
         * The meta object literal for the '
         * {@link gov.nasa.jpl.mbee.dgview.impl.TableEntryImpl
         * <em>Table Entry</em>}' class. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * 
         * @see gov.nasa.jpl.mbee.dgview.impl.TableEntryImpl
         * @see gov.nasa.jpl.mbee.dgview.impl.DgviewPackageImpl#getTableEntry()
         * @generated
         */
        EClass     TABLE_ENTRY                     = eINSTANCE.getTableEntry();

        /**
         * The meta object literal for the '<em><b>Morerows</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute TABLE_ENTRY__MOREROWS           = eINSTANCE.getTableEntry_Morerows();

        /**
         * The meta object literal for the '<em><b>Namest</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute TABLE_ENTRY__NAMEST             = eINSTANCE.getTableEntry_Namest();

        /**
         * The meta object literal for the '<em><b>Nameend</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute TABLE_ENTRY__NAMEEND            = eINSTANCE.getTableEntry_Nameend();

        /**
         * The meta object literal for the '
         * {@link gov.nasa.jpl.mbee.dgview.impl.TextImpl
         * <em>Text</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @see gov.nasa.jpl.mbee.dgview.impl.TextImpl
         * @see gov.nasa.jpl.mbee.dgview.impl.DgviewPackageImpl#getText()
         * @generated
         */
        EClass     TEXT                            = eINSTANCE.getText();

        /**
         * The meta object literal for the '<em><b>Text</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute TEXT__TEXT                      = eINSTANCE.getText_Text();

        /**
         * The meta object literal for the '
         * {@link gov.nasa.jpl.mbee.dgview.impl.ViewElementImpl
         * <em>View Element</em>}' class. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * 
         * @see gov.nasa.jpl.mbee.dgview.impl.ViewElementImpl
         * @see gov.nasa.jpl.mbee.dgview.impl.DgviewPackageImpl#getViewElement()
         * @generated
         */
        EClass     VIEW_ELEMENT                    = eINSTANCE.getViewElement();

        /**
         * The meta object literal for the '<em><b>Id</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute VIEW_ELEMENT__ID                = eINSTANCE.getViewElement_Id();

        /**
         * The meta object literal for the '<em><b>Title</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute VIEW_ELEMENT__TITLE             = eINSTANCE.getViewElement_Title();

        /**
         * The meta object literal for the '<em><b>From Element Id</b></em>'
         * attribute feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute VIEW_ELEMENT__FROM_ELEMENT_ID   = eINSTANCE.getViewElement_FromElementId();

        /**
         * The meta object literal for the '<em><b>From Property</b></em>'
         * attribute feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute VIEW_ELEMENT__FROM_PROPERTY     = eINSTANCE.getViewElement_FromProperty();

        /**
         * The meta object literal for the '
         * {@link gov.nasa.jpl.mbee.dgview.impl.TableRowImpl
         * <em>Table Row</em>}' class. <!-- begin-user-doc --> <!-- end-user-doc
         * -->
         * 
         * @see gov.nasa.jpl.mbee.dgview.impl.TableRowImpl
         * @see gov.nasa.jpl.mbee.dgview.impl.DgviewPackageImpl#getTableRow()
         * @generated
         */
        EClass     TABLE_ROW                       = eINSTANCE.getTableRow();

        /**
         * The meta object literal for the '
         * {@link gov.nasa.jpl.mbee.dgview.impl.MDEditableTableImpl
         * <em>MD Editable Table</em>}' class. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * 
         * @see gov.nasa.jpl.mbee.dgview.impl.MDEditableTableImpl
         * @see gov.nasa.jpl.mbee.dgview.impl.DgviewPackageImpl#getMDEditableTable()
         * @generated
         */
        EClass     MD_EDITABLE_TABLE               = eINSTANCE.getMDEditableTable();

        /**
         * The meta object literal for the '<em><b>Precision</b></em>' attribute
         * feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute MD_EDITABLE_TABLE__PRECISION    = eINSTANCE.getMDEditableTable_Precision();

        /**
         * The meta object literal for the '<em><b>Gui Headers</b></em>'
         * attribute list feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute MD_EDITABLE_TABLE__GUI_HEADERS  = eINSTANCE.getMDEditableTable_GuiHeaders();

        /**
         * The meta object literal for the '<em><b>Editable</b></em>' attribute
         * list feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute MD_EDITABLE_TABLE__EDITABLE     = eINSTANCE.getMDEditableTable_Editable();

        /**
         * The meta object literal for the '<em><b>Merge Cols</b></em>'
         * attribute list feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute MD_EDITABLE_TABLE__MERGE_COLS   = eINSTANCE.getMDEditableTable_MergeCols();

        /**
         * The meta object literal for the '<em><b>Add Line Num</b></em>'
         * attribute feature. <!-- begin-user-doc --> <!-- end-user-doc -->
         * 
         * @generated
         */
        EAttribute MD_EDITABLE_TABLE__ADD_LINE_NUM = eINSTANCE.getMDEditableTable_AddLineNum();

        /**
         * The meta object literal for the '<em><b>Gui Body</b></em>'
         * containment reference list feature. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * 
         * @generated
         */
        EReference MD_EDITABLE_TABLE__GUI_BODY     = eINSTANCE.getMDEditableTable_GuiBody();

        /**
         * The meta object literal for the '
         * {@link gov.nasa.jpl.mbee.dgview.FromProperty
         * <em>From Property</em>}' enum. <!-- begin-user-doc --> <!--
         * end-user-doc -->
         * 
         * @see gov.nasa.jpl.mbee.dgview.FromProperty
         * @see gov.nasa.jpl.mbee.dgview.impl.DgviewPackageImpl#getFromProperty()
         * @generated
         */
        EEnum      FROM_PROPERTY                   = eINSTANCE.getFromProperty();

    }

} // DgviewPackage
