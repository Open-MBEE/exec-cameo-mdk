package org.openmbee.mdk.options;

import com.nomagic.magicdraw.resources.ResourceManager;

public class MDKEnvironmentOptionsGroupResources {
    public static final String BUNDLE_NAME = "org.openmbee.mdk.options.EnvironmentOptionsResources";

    /**
     * Constructs this resource handler.
     */
    private MDKEnvironmentOptionsGroupResources() {
        // do nothing.
    }

    /**
     * Gets resource by key.
     *
     * @param key key by which to get the resource.
     * @return translated resource.
     */
    public static String getString(String key) {
        return ResourceManager.getStringFor(key, BUNDLE_NAME, MDKEnvironmentOptionsGroupResources.class.getClassLoader());
    }
}
