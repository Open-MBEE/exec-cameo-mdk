package gov.nasa.jpl.mbee.mdk.options;

import com.nomagic.magicdraw.properties.Property;
import com.nomagic.magicdraw.properties.PropertyResourceProvider;

public class MDKPropertyResourceProvider implements PropertyResourceProvider {
    private static final String ID = "MDK_PROPERTY_RESOURCE_PROVIDER";
    private static final MDKPropertyResourceProvider INSTANCE = new MDKPropertyResourceProvider();

    private MDKPropertyResourceProvider() {

    }

    public static MDKPropertyResourceProvider getInstance() {
        return INSTANCE;
    }

    public String getString(String string, Property property) {
        return MDKProjectOptionsResources.getString(string);
    }

    public String getUniqueID() { return ID; }

}
