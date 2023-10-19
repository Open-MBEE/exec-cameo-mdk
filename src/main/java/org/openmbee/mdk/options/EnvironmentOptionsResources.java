package org.openmbee.mdk.options;

import com.nomagic.magicdraw.resources.ResourceManager;

public class EnvironmentOptionsResources {
    public static final String BUNDLE_NAME = "gov.nasa.jpl.mbee.mdk.options.EnvironmentOptionsResources";

    /**
     * Constructs this resource handler.
     */
    private EnvironmentOptionsResources() {
        // do nothing.
    }

    /**
     * Gets resource by key.
     *
     * @param key key by which to get the resource.
     * @return translated resource.
     */
    public static String getString(String key) {
        return ResourceManager.getStringFor(key, BUNDLE_NAME, EnvironmentOptionsResources.class.getClassLoader());
    }
}