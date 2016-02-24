package gov.nasa.jpl.mbee.options;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.options.AbstractPropertyOptionsGroup;
import com.nomagic.magicdraw.properties.BooleanProperty;
import com.nomagic.magicdraw.properties.Property;
import com.nomagic.magicdraw.properties.PropertyResourceProvider;
//import com.nomagic.magicdraw.ui.ImageMap16;
//import com.nomagic.ui.SwingImageIcon;

public class MDKOptionsGroup extends AbstractPropertyOptionsGroup {

    public static final String ID = "options.mdk";
    public static final String GROUP = "MDK";
    
    public static final String LOG_JSON_ID = "LOG_JSON";
    public static final String SAVE_CHANGES_ID = "SAVE_LOCAL_CHANGES_ON_PROJECT_SAVE";
    public static final String LISTENER_ID = "ENABLE_COMMIT_LISTENER";
    
    public MDKOptionsGroup() {
        super(ID);
    }
    
    public static MDKOptionsGroup getMDKOptions() {
        return (MDKOptionsGroup)Application.getInstance().getEnvironmentOptions().getGroup(ID);
    }
    
    public boolean isLogJson() {
        Property p = getProperty(LOG_JSON_ID);
        return (Boolean)p.getValue();
    }
    
    public void setLogJson(boolean value) {
        BooleanProperty property = new BooleanProperty(LOG_JSON_ID, value);
        property.setResourceProvider(PROPERTY_RESOURCE_PROVIDER);
        property.setGroup(GROUP);
        addProperty(property);

    }
    
    public boolean isSaveChanges() {
        Property p = getProperty(SAVE_CHANGES_ID);
        return (Boolean)p.getValue();
    }
    
    public void setSaveChanges(boolean value) {
        BooleanProperty property = new BooleanProperty(SAVE_CHANGES_ID, value);
        property.setResourceProvider(PROPERTY_RESOURCE_PROVIDER);
        property.setGroup(GROUP);
        addProperty(property);
    }
    
    public boolean isCommitListener() {
        Property p = getProperty(LISTENER_ID);
        return (Boolean)p.getValue();
    }
    
    public void setCommitListener(boolean value) {
        BooleanProperty property = new BooleanProperty(LISTENER_ID, value);
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
        setLogJson(true);
        setSaveChanges(true);
        setCommitListener(true);
    }
    
    private static final String MDK_OPTIONS_NAME = "MDK";
    
    @Override
    public String getName() {
        return EnvironmentOptionsResources.getString(MDK_OPTIONS_NAME);
    }

    
    
    //@Override
    //public SwingImageIcon getIcon() {
    //    return (SwingImageIcon) ImageMap16.SYSTEM_BOUNDARY;
    //}



}
