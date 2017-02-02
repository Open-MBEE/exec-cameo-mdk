package gov.nasa.jpl.mbee.mdk.options;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.options.AbstractPropertyOptionsGroup;
import com.nomagic.magicdraw.properties.BooleanProperty;
import com.nomagic.magicdraw.properties.Property;
import com.nomagic.magicdraw.properties.PropertyResourceProvider;
import com.nomagic.magicdraw.properties.StringProperty;
import com.nomagic.magicdraw.resources.ResourceManager;

import gov.nasa.jpl.mbee.mdk.lib.MDUtils;
import gov.nasa.jpl.mbee.mdk.options.resources.EnvironmentOptionsResources;

import javax.annotation.CheckForNull;
import java.io.File;

public class MDKOptionsGroup extends AbstractPropertyOptionsGroup {

    public static final String ID = "options.mdk";
    public static final String LOG_JSON_ID="LOG_JSON_ID",
            SHOW_ADVANCED_OPTIONS_ID="SHOW_ADVANCED_OPTIONS_ID",
            USER_SCRIPT_DIRECTORIES_ID="USER_SCRIPT_DIRECTORIES_ID",
            PERSIST_CHANGELOG_ID="PERSIST_CHANGELOG_ID",
            CHANGE_LISTENER_ID="CHANGE_LISTENER_ID",
            COORDINATED_SYNC_ID="COORDINATED_SYNC_ID",
            MDK_OPTIONS_NAME="MDK_OPTIONS_NAME",
            GROUP_ID="GROUP_ID";

    public static final PropertyResourceProvider MDK_PROPERTY_RESOURCE_PROVIDER = new PropertyResourceProvider() {
        @Override
        public String getString(@CheckForNull String s, @CheckForNull Property property) {
            return EnvironmentOptionsResources.getString(s);
        }
    };

    public MDKOptionsGroup() {
        super(ID);
    }

    public static MDKOptionsGroup getMDKOptions() {
        return (MDKOptionsGroup) Application.getInstance().getEnvironmentOptions().getGroup(ID);
    }

    public boolean isLogJson() {
        Property p = getProperty(LOG_JSON_ID);
        return (Boolean) p.getValue();
    }

    public void setLogJson(boolean value) {
        BooleanProperty property = new BooleanProperty(LOG_JSON_ID, value);
        property.setResourceProvider(MDK_PROPERTY_RESOURCE_PROVIDER);
        property.setGroup(GROUP_ID);
        addProperty(property);
    }

    public boolean isPersistChangelog() {
        Property p = getProperty(PERSIST_CHANGELOG_ID);
        return (Boolean) p.getValue();
    }

    public void setPersistChangelog(boolean value) {
        BooleanProperty property = new BooleanProperty(PERSIST_CHANGELOG_ID, value);
        property.setResourceProvider(MDK_PROPERTY_RESOURCE_PROVIDER);
        property.setGroup(GROUP_ID);
        if (MDUtils.isDeveloperMode()) {
            addProperty(property);
        }
        else {
            addInvisibleProperty(property);
        }
    }

    public boolean isChangeListenerEnabled() {
        Property p = getProperty(CHANGE_LISTENER_ID);
        return (Boolean) p.getValue();
    }

    public void setChangeListenerEnabled(boolean value) {
        BooleanProperty property = new BooleanProperty(CHANGE_LISTENER_ID, value);
        property.setResourceProvider(MDK_PROPERTY_RESOURCE_PROVIDER);
        property.setGroup(GROUP_ID);
        if (MDUtils.isDeveloperMode()) {
            addProperty(property);
        }
        else {
            addInvisibleProperty(property);
        }
    }

    public boolean isCoordinatedSyncEnabled() {
        Property p = getProperty(COORDINATED_SYNC_ID);
        return (Boolean) p.getValue();
    }

    public void setCoordinatedSyncEnabled(boolean value) {
        BooleanProperty property = new BooleanProperty(COORDINATED_SYNC_ID, value);
        property.setResourceProvider(MDK_PROPERTY_RESOURCE_PROVIDER);
        property.setGroup(GROUP_ID);
        if (MDUtils.isDeveloperMode()) {
            addProperty(property);
        }
        else {
            addInvisibleProperty(property);
        }
    }

    public boolean isMDKAdvancedOptions() {
        Property p = getProperty(SHOW_ADVANCED_OPTIONS_ID);
        if((Boolean) p.getValue()) {
            Application.getInstance().getGUILog().log("--- MAGICDRAW RESTART REQUIRED TO ENABLE MDK ADVANCED OPTIONS! ---  ");
        }
        return (Boolean) p.getValue();
    }

    public void setMDKAdvancedOptions(boolean value) {
        BooleanProperty property = new BooleanProperty(SHOW_ADVANCED_OPTIONS_ID, value);
        property.setResourceProvider(MDK_PROPERTY_RESOURCE_PROVIDER);
        property.setGroup(GROUP_ID);
        addProperty(property);
    }

    public File[] getCustomUserScriptDirectories(){
        Property p = getProperty(USER_SCRIPT_DIRECTORIES_ID);
        String val =  p.getValueStringRepresentation();
        if(val == null || val.isEmpty()){
            return null;
        }
        File[] dirs = new File[getNumberOfCustomUserScriptDirectories()];
        for(int i = 0; i < getNumberOfCustomUserScriptDirectories(); i++){
            dirs[i] = new File(val.split(File.pathSeparator)[i]);
        }
        return dirs;
    }
    public int getNumberOfCustomUserScriptDirectories(){
        Property p = getProperty(USER_SCRIPT_DIRECTORIES_ID);
        String val =  p.getValueStringRepresentation();
        if(val == null || val.isEmpty()){
            return 0;
        }
        return val.split(File.pathSeparator).length;

    }

    public void setUserScriptDirectory(String path) {
        StringProperty property = new StringProperty(USER_SCRIPT_DIRECTORIES_ID, path);
        property.setResourceProvider(MDK_PROPERTY_RESOURCE_PROVIDER);
        property.setGroup(GROUP_ID);
        addProperty(property);
    }

    @Override
    public void setDefaultValues() {
        setLogJson(MDUtils.isDeveloperMode());
        setPersistChangelog(true);
        setChangeListenerEnabled(true);
        setCoordinatedSyncEnabled(true);
        setUserScriptDirectory("");
        setMDKAdvancedOptions(false);
    }

    @Override
    public String getName() {
        //return EnvironmentOptionsResources.getString(MDK_OPTIONS_NAME);
        return MDK_OPTIONS_NAME;
    }

}
