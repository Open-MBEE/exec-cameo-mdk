package gov.nasa.jpl.mbee.mdk.api.incubating;

import com.nomagic.uml2.ext.magicdraw.metadata.UMLPackage;

/**
 * Created by igomes on 9/26/16.
 */
public class MDKConstants {
    public static final String
            HIDDEN_ID_PREFIX = "_hidden_",
            DERIVED_KEY_PREFIX = "_",
            HOLDING_BIN_ID_PREFIX = "holding_bin_",
            VIEW_INSTANCES_BIN_PREFIX = "view_instances_bin_",
            SYNC_SYSML_ID_SUFFIX = "_sync",
            PRIMARY_MODEL_ID_SUFFIX = "_pm",
            APPLIED_STEREOTYPE_INSTANCE_ID_SUFFIX = "_asi",
            VIEW_CONSTRAINT_SYSML_ID_SUFFIX = "_vc",
            ID_KEY_SUFFIX = "Id",
            IDS_KEY_SUFFIX = ID_KEY_SUFFIX + "s",
            SLOT_ID_SEPARATOR = "-slot-",
            SLOT_VALUE_ID_SEPARATOR = SLOT_ID_SEPARATOR.substring(0, SLOT_ID_SEPARATOR.length() - 1) + "value-",
            TYPE_KEY = "type",
            NAME_KEY = "name",
            ID_KEY = "id",
            PARENT_REF_ID_KEY = "parentRef" + ID_KEY_SUFFIX,
            OWNER_ID_KEY = UMLPackage.Literals.ELEMENT__OWNER.getName() + ID_KEY_SUFFIX,
            INSTANCE_ID_KEY = UMLPackage.Literals.INSTANCE_VALUE__INSTANCE.getName() + ID_KEY_SUFFIX,
            APPLIED_STEREOTYPE_IDS_KEY = "_appliedStereotypeIds",
            DOCUMENTATION_KEY = "documentation",
            IS_SITE_KEY = "_isSite",
            PROPERTY_PATH_IDS_KEY = "_propertyPathIds",
            CONTENTS_KEY = DERIVED_KEY_PREFIX + "contents",
            DISPLAYED_ELEMENT_IDS_KEY = DERIVED_KEY_PREFIX + "displayedElement" + IDS_KEY_SUFFIX,
            DIAGRAM_TYPE_KEY = DERIVED_KEY_PREFIX + "diagramType",
            CATEGORY_ID_KEY = "category" + ID_KEY_SUFFIX,
            MOUNTED_ELEMENT_ID_KEY = "mountedElement" + ID_KEY_SUFFIX,
            MOUNTED_ELEMENT_PROJECT_ID_KEY = "mountedElementProject" + ID_KEY_SUFFIX,
            MOUNTED_REF_ID_KEY = "mountedRef" + ID_KEY_SUFFIX,
            QUALIFIED_NAME_KEY = "qualifiedName",
            QUALIFIED_ID_KEY = "qualified" + ID_KEY_SUFFIX,
            STATUS_KEY = "status",
            REF_CREATED_STATUS = "created",
            ORG_KEY = "org",
            ORG_ID_KEY = ORG_KEY + ID_KEY_SUFFIX,
            TWC_ID_KEY = "twc" + ID_KEY_SUFFIX,
            TWC_VERSION_KEY = "twcVersion";
}
