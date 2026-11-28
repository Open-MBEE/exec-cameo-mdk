package org.openmbee.mdk.hyperlinks;

import com.nomagic.magicdraw.resources.ResourceManager;

import java.text.MessageFormat;

public class TransclusionDialogResource {

    public static final String BUNDLE_NAME = "org.openmbee.mdk.dialogs.TransclusionDialogResources";

    /**
     * Constructs this resource handler.
     */
    private TransclusionDialogResource() {
        // do nothing.
    }

    /**
     * Gets resource by key.
     *
     * @param key key by which to get the resource.
     * @return translated resource.
     */
    public static String getString(String key) {
        return ResourceManager.getStringFor(key, BUNDLE_NAME, TransclusionDialogResource.class.getClassLoader());
    }

    public static String getStringFor(String var0, String... var1) {
        String var2 = getString(var0);
        var2 = var2.replaceAll("'", "''");
        var2 = MessageFormat.format(var2, (Object[])var1);
        return var2;
    }
}