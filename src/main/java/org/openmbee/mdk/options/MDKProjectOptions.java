package org.openmbee.mdk.options;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.core.options.ProjectOptions;
import com.nomagic.magicdraw.properties.BooleanProperty;
import com.nomagic.magicdraw.properties.ChoiceProperty;
import com.nomagic.magicdraw.properties.Property;
import com.nomagic.magicdraw.properties.StringProperty;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import org.apache.http.client.utils.URIBuilder;
import org.openmbee.mdk.fileexport.ContextExportLevel;

import javax.swing.*;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MDKProjectOptions {

    public static final String ID = "options.project.mdk";
    public static final String GROUP = ProjectOptions.PROJECT_GENERAL_PROPERTIES;
    public static final String MMS_HOST_URL="MMS_HOST_URL",
            MMS_BASE_PATH="MMS_BASE_PATH",
            VE_HOST_URL="VE_HOST_URL",
            VE_BASE_PATH="VE_BASE_PATH",
            MDK_PROJECT_OPTIONS_GROUP= "MDK_PROJECT_OPTIONS_GROUP",
            ENABLE_OPENMBEE_INTEGRATION = "ENABLE_OPENMBEE_INTEGRATION",
            MDK_MMS_URL = "MDK_MMS_URL",
            MDK_VE_URL = "MDK_VE_URL",
            MDK_MIGRATE_STEREOTYPE = "MDK_MIGRATE_STEREOTYPE",
            PROPERTY_AUTOSAVE_MDKMODEL = "PROPERTY_AUTOSAVE_MDKMODEL",
            PROPERTY_AUTOSAVE_MDKZIP = "PROPERTY_AUTOSAVE_MDKZIP",
            PROPERTY_CONTEXT_EXPORT_LEVEL = "PROPERTY_CONTEXT_EXPORT_LEVEL";

    public MDKProjectOptions() {
    }

    public static void init(ProjectOptions var0) {
        MDKProjectOptions.setOption(var0, ENABLE_OPENMBEE_INTEGRATION, false);
        MDKProjectOptions.setOption(var0, MMS_HOST_URL, "");
        MDKProjectOptions.setOption(var0, MMS_BASE_PATH,"");
        MDKProjectOptions.setOption(var0, VE_HOST_URL,"");
        MDKProjectOptions.setOption(var0, VE_BASE_PATH,"");
        MDKProjectOptions.setOption(var0, MDK_MIGRATE_STEREOTYPE, true);

        //below were added by LieberLieber's JSON export but doesn't seem to be used/called anywhere
        //MDKProjectOptions.setOption(var0, PROPERTY_AUTOSAVE_MDKMODEL, false);
        //MDKProjectOptions.setOption(var0, PROPERTY_AUTOSAVE_MDKZIP, false);
        //MDKProjectOptions.setOption(var0, PROPERTY_CONTEXT_EXPORT_LEVEL, Arrays.asList(ContextExportLevel.values()), ContextExportLevel.Containment);
    }

    public static void setOption(ProjectOptions var0, String projectOption, String newValue) {
        Property var1 = var0.getProperty(MDKProjectOptions.GROUP, projectOption);
        if (var1 == null) {
            StringProperty var2 = new StringProperty(projectOption, newValue);
            var2.setGroup(MDKProjectOptions.MDK_PROJECT_OPTIONS_GROUP);
            var2.setResourceProvider(MDKPropertyResourceProvider.getInstance());
            var0.addProperty(MDKProjectOptions.GROUP, var2);
        }
        else if (!var1.getValue().equals(newValue)) {
            var1.setValue(newValue);
        }
    }

    public static void setOption(ProjectOptions var0, String projectOption, boolean newValue) {
        Property var1 = var0.getProperty(MDKProjectOptions.GROUP, projectOption);
        if (var1 == null) {
            BooleanProperty var2 = new BooleanProperty(projectOption, newValue);
            var2.setGroup(MDKProjectOptions.MDK_PROJECT_OPTIONS_GROUP);
            var2.setResourceProvider(MDKPropertyResourceProvider.getInstance());
            var0.addProperty(MDKProjectOptions.GROUP, var2);
        }
        else if ((boolean)var1.getValue() != newValue) {
            var1.setValue(newValue);
        }
    }

    public static <T> void setOption(ProjectOptions var0, String projectOption, List<T> choices, T newValue) {
        Property var1 = var0.getProperty(MDKProjectOptions.GROUP, projectOption);
        if (var1 == null) {
            ChoiceProperty var2 = new ChoiceProperty(projectOption, "", choices);
            var2.setGroup(MDKProjectOptions.MDK_PROJECT_OPTIONS_GROUP);
            var2.setResourceProvider(MDKPropertyResourceProvider.getInstance());
            var2.setValue(newValue);
            var0.addProperty(MDKProjectOptions.GROUP, var2);
        }
        else if (!var1.getValue().equals(newValue)) {
            var1.setValue(newValue);
        }
    }

    public static boolean isAutosaveMDKModel(Project project) {
        Property property = project.getOptions().getProperty(ProjectOptions.PROJECT_GENERAL_PROPERTIES, PROPERTY_AUTOSAVE_MDKMODEL);
        if (property != null) {
            return (boolean) property.getValue();
        } else {
            return false;
        }

    }

    public static boolean isAutosaveMDKZip(Project project) {
        Property property = project.getOptions().getProperty(ProjectOptions.PROJECT_GENERAL_PROPERTIES, PROPERTY_AUTOSAVE_MDKZIP);
        if (property != null) {
            return (boolean) property.getValue();
        } else {
            return false;
        }

    }

    public static ContextExportLevel getContextExportLevel(Project project) {
        Property property = project.getOptions().getProperty(ProjectOptions.PROJECT_GENERAL_PROPERTIES, PROPERTY_CONTEXT_EXPORT_LEVEL);
        if (property != null) {
            return ContextExportLevel.valueOf(property.getValueStringRepresentation());
        } else {
            return ContextExportLevel.None;
        }
    }

    public static boolean getMbeeEnabled(Project project) {
        if (project != null) {
            Property mmsEnabledProperty = project.getOptions().getProperty(ProjectOptions.PROJECT_GENERAL_PROPERTIES, ENABLE_OPENMBEE_INTEGRATION);
            if (mmsEnabledProperty instanceof BooleanProperty) {
                return ((BooleanProperty) mmsEnabledProperty).getBoolean();
            }
        }
        return false;
    }

    public static String getMmsHost(Project project) {
        if (project != null) {
            Property mmsHostProperty = project.getOptions().getProperty(ProjectOptions.PROJECT_GENERAL_PROPERTIES, MMS_HOST_URL);
            if (mmsHostProperty instanceof StringProperty) {
                return ((StringProperty) mmsHostProperty).getString();
            }
        }
        return null;
    }

    public static String getMmsBasePath(Project project) {
        if (project != null) {
            Property mmsBasePathProperty = project.getOptions().getProperty(ProjectOptions.PROJECT_GENERAL_PROPERTIES, MMS_BASE_PATH);
            if (mmsBasePathProperty instanceof StringProperty) {
                return ((StringProperty) mmsBasePathProperty).getString();
            }
        }
        return null;
    }

    public static String getVeHost(Project project) {
        if (project != null) {
            Property veHostProperty = project.getOptions().getProperty(ProjectOptions.PROJECT_GENERAL_PROPERTIES, VE_HOST_URL);
            if (veHostProperty instanceof StringProperty) {
                return ((StringProperty) veHostProperty).getString();
            }
        }
        return null;
    }

    public static String getVeBasePath(Project project) {
        if (project != null) {
            Property veBasePathProperty = project.getOptions().getProperty(ProjectOptions.PROJECT_GENERAL_PROPERTIES, VE_BASE_PATH);
            if (veBasePathProperty instanceof StringProperty) {
                return ((StringProperty) veBasePathProperty).getString();
            }
        }
        return null;
    }

    public static URIBuilder getMmsUrl(Project project) {
        if (project == null) {
            return null;
        }
        URIBuilder uri;
        if (getMbeeEnabled(project)) {
            String mmsHostUrl = getMmsHost(project);
            String mmsBasePath = getMmsBasePath(project);
            uri = getUriBuilder(mmsHostUrl,mmsBasePath);;
            if (uri != null) {
                return uri;
            }
            Application.getInstance().getGUILog().log("[ERROR] You must specify MMS URL before enabling MBEE Integration.");
            setOption(project.getOptions(), ENABLE_OPENMBEE_INTEGRATION, false);
            return null;
        } else if (ProjectUtilities.isStandardSystemProfile(project.getPrimaryProject())) {
            Property var1 = project.getOptions().getInvisibleProperty(MDK_MMS_URL);
            if (var1 != null) {
                return getUriBuilder((String)var1.getValue(), null);
            } else if (project.getOptions().getInvisibleProperty("DISABLE_MMS") == null) {
                String urlString = JOptionPane.showInputDialog("Specify server URL for standard profile.", null);
                if (urlString == null || urlString.trim().isEmpty()) {
                    Application.getInstance().getGUILog().log("[WARNING] You must specify an MMS URL for the profile to use MMS");
                    int confirm = JOptionPane.showConfirmDialog(null,"Skip adding MMS Url for this session?", "Skip MMS?", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        Property var2 = new BooleanProperty("DISABLE_MMS", true);
                        project.getOptions().addInvisibleProperty(var2);
                        return null;
                    } else {
                        return getMmsUrl(project);
                    }
                }
                uri = getUriBuilder(urlString,null);
                if (uri != null) {
                    return uri;
                }
                Application.getInstance().getGUILog().log("[WARNING] Invalid URL Syntax");
                return getMmsUrl(project);
            }
        }
        Application.getInstance().getGUILog().log("[ERROR] You must enable MBEE Integration before continuing.");
        return null;
    }

    public static URIBuilder getVeUrl(Project project) {
        if (project == null) {
            return null;
        }
        URIBuilder uri;
        String veHostUrl = getVeHost(project);
        if (veHostUrl.equals("")) {
            veHostUrl = getMmsHost(project);
        }
        String veBasePath = getVeBasePath(project);
        uri = getUriBuilder(veHostUrl,veBasePath);
        if (uri != null) {
            uri.setFragment("");
            return uri;
        }
        Application.getInstance().getGUILog().log("[ERROR] Unexpected error in generation of VE URL for project.");
        return null;
    }

    public static boolean isMigrationAllowed(Project project) {
        if (project != null) {
            Property migrate = project.getOptions().getProperty(ProjectOptions.PROJECT_GENERAL_PROPERTIES, MDK_MIGRATE_STEREOTYPE);
            if (migrate instanceof BooleanProperty) {
                return ((BooleanProperty) migrate).getBoolean();
            }
        }
        return false;
    }

    /**
     * @description
     * @param project
     * @throws IllegalStateException
     */
    public static void validate(Project project) throws IllegalStateException {
        String urlString;
        URIBuilder uri;
        if (project == null) {
            return;
        }
        Element primaryModel = project.getPrimaryModel();
        if (primaryModel == null) {
            return;
        }
        if (StereotypesHelper.hasStereotype(primaryModel, "ModelManagementSystem")) {
            if (!getMbeeEnabled(project)) {
                setOption(project.getOptions(), ENABLE_OPENMBEE_INTEGRATION, true);
                Application.getInstance().getGUILog().log("[INFO] ModelManagementSystem stereotype detected. Automatically migrating and re-enabling MBEE integration");
            }
            urlString = (String) StereotypesHelper.getStereotypePropertyFirst(primaryModel, "ModelManagementSystem", "MMS URL");
            if (urlString != null && !urlString.equals("") && isMigrationAllowed(project)) {
                uri = getUriBuilder(urlString, null);
                if (uri == null) {
                    Application.getInstance().getGUILog().log("[ERROR] Unable to migrate MMS URL to project options. Please manually re-enable via Options>Project>General>MBEE");
                    return;
                }
                setOption(project.getOptions(), MMS_HOST_URL, uri.getScheme() + "://" + uri.getHost());
                setOption(project.getOptions(), MMS_BASE_PATH, uri.getPath());
                int deleteSlot = JOptionPane.showConfirmDialog(null,"MMS URL Migration Complete! The MMS Stereotype is no longer needed, would you like to remove it?", "Migration Complete", JOptionPane.YES_NO_OPTION);
                if (deleteSlot == JOptionPane.YES_OPTION) {
                    StereotypesHelper.removeStereotypeByString(primaryModel,"ModelManagementSystem");
                } else if (deleteSlot == JOptionPane.NO_OPTION) {
                    setOption(project.getOptions(), MDK_MIGRATE_STEREOTYPE, false);
                }
            }
        } else if (getMbeeEnabled(project)) {
            if (ProjectUtilities.findAttachedProjectByName(project,"SysML Extensions") == null) {
                Application.getInstance().getGUILog().log("[WARNING] SysML Extensions must be mounted before using MBEE Integration.");
                setOption(project.getOptions(), ENABLE_OPENMBEE_INTEGRATION, false);
                Application.getInstance().getGUILog().log("[WARNING] Mount SysML Extensions and re-enable MBEE integration before continuing");
            }
        }
        }

    /**
     * Returns a URIBuilder object with a path. Used as the base for all of the rest of the
     * URIBuilder generating convenience classes.
     *
     * @param urlString: URL for the OpenMBEE Service being requested
     * @param basePath: base path string for the desired service
     * @return URIBuilder
     */
    public static URIBuilder getUriBuilder(String urlString, String basePath) {
        URIBuilder uri;
        try {
            uri = new URIBuilder(urlString);
            if (basePath != null)
                uri.setPath(basePath);
        } catch (URISyntaxException e) {
            Application.getInstance().getGUILog().log("[ERROR] Unexpected error in generation of URL for project. Reason: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        String path = Optional.ofNullable(uri.getPath()).orElse("");
        if (path.endsWith("#") || path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        uri.setPath(path);
        return uri;

    }

}
