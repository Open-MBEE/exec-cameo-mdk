package gov.nasa.jpl.mbee.mdk.options;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.options.AbstractPropertyOptionsGroup;
import com.nomagic.magicdraw.properties.BooleanProperty;
import com.nomagic.magicdraw.properties.Property;
import com.nomagic.magicdraw.properties.PropertyResourceProvider;
import gov.nasa.jpl.mbee.mdk.lib.MDUtils;
//import com.nomagic.magicdraw.ui.ImageMap16;
//import com.nomagic.ui.SwingImageIcon;

public class MDKOptionsGroup extends AbstractPropertyOptionsGroup {

    public static final String ID = "options.mdk";
    public static final String GROUP = "MDK";

    public static final String LOG_JSON_ID = "LOG_JSON",
            PERSIST_CHANGELOG = "PERSIST_CHANGELOG_ON_SAVE",
            CHANGE_LISTENER = "ENABLE_CHANGE_LISTENER",
            COORDINATED_SYNC = "ENABLE_COORDINATED_SYNC",
            MMS_ADVANCED_OPTIONS = "MMS_ADVANCED_OPTIONS";




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
        property.setResourceProvider(PROPERTY_RESOURCE_PROVIDER);
        property.setGroup(GROUP);
        addProperty(property);

    }

    public boolean isPersistChangelog() {
        Property p = getProperty(PERSIST_CHANGELOG);
        return (Boolean) p.getValue();
    }

    public void setPersistChangelog(boolean value) {
        BooleanProperty property = new BooleanProperty(PERSIST_CHANGELOG, value);
        property.setResourceProvider(PROPERTY_RESOURCE_PROVIDER);
        property.setGroup(GROUP);
        addProperty(property);
    }

    public boolean isChangeListenerEnabled() {
        Property p = getProperty(CHANGE_LISTENER);
        return (Boolean) p.getValue();
    }

    public void setChangeListenerEnabled(boolean value) {
        BooleanProperty property = new BooleanProperty(CHANGE_LISTENER, value);
        property.setResourceProvider(PROPERTY_RESOURCE_PROVIDER);
        property.setGroup(GROUP);
        addProperty(property);
    }

    public boolean isCoordinatedSyncEnabled() {
        Property p = getProperty(COORDINATED_SYNC);
        return (Boolean) p.getValue();
    }

    public void setCoordinatedSyncEnabled(boolean value) {
        BooleanProperty property = new BooleanProperty(COORDINATED_SYNC, value);
        property.setResourceProvider(PROPERTY_RESOURCE_PROVIDER);
        property.setGroup(GROUP);
        addProperty(property);
    }

    public boolean isMMSAdvancedOptions() {
        Property p = getProperty(MMS_ADVANCED_OPTIONS);
        if((Boolean) p.getValue()) {
            Application.getInstance().getGUILog().log("-------------------------------------------------- MAGICDRAW RESTART REQUIRED TO ENABLE MMS ADVANCED OPTIONS! --------------------------------------------------  ");
        }
        return (Boolean) p.getValue();
    }

    public void setisMMSAdvancedOptions(boolean value) {
        BooleanProperty property = new BooleanProperty(MMS_ADVANCED_OPTIONS, value);
        property.setResourceProvider(PROPERTY_RESOURCE_PROVIDER);
        property.setGroup(GROUP);
        addProperty(property);

    }

    public static final PropertyResourceProvider PROPERTY_RESOURCE_PROVIDER = new PropertyResourceProvider() {
        @Override
        public String getString(String key, Property property) {
            return EnvironmentOptionsResources.getString(key);
        }
    };

    @Override
    public void setDefaultValues() {
        setLogJson(MDUtils.isDeveloperMode());
        setPersistChangelog(true);
        setChangeListenerEnabled(true);
        setCoordinatedSyncEnabled(true);
        setisMMSAdvancedOptions(false);

    }

    private static final String MDK_OPTIONS_NAME = "MDK";

    @Override
    public String getName() {
        //return EnvironmentOptionsResources.getString(MDK_OPTIONS_NAME);
        return MDK_OPTIONS_NAME;
    }


    //@Override
    //public SwingImageIcon getIcon() {
    //    return (SwingImageIcon) ImageMap16.SYSTEM_BOUNDARY;
    //}


}
