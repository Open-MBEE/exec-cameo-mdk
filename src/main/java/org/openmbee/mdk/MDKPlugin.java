package org.openmbee.mdk;

import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.plugins.Plugin;
import com.nomagic.magicdraw.plugins.PluginUtils;
import com.nomagic.magicdraw.plugins.ResourceDependentPlugin;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MDKPlugin extends Plugin implements ResourceDependentPlugin {
    public static final String MAIN_TOOLBAR_CATEGORY_NAME = "MDK";

    private static MDKPlugin INSTANCE;
    private static boolean JAVAFX_SUPPORTED;

    public static ClassLoader extensionsClassloader;
    public static ActionsManager MAIN_TOOLBAR_ACTIONS_MANAGER;

    public static MDKPlugin getInstance() {
        if (INSTANCE == null) {
            INSTANCE = PluginUtils.getPlugins().stream()
                    .filter(plugin -> Objects.equals(plugin.getDescriptor().getName(), "Model Development Kit"))
                    .filter(plugin -> plugin instanceof MDKPlugin)
                    .map(plugin -> (MDKPlugin) plugin)
                    .findAny()
                    .orElseThrow(IllegalStateException::new);
        }
        return INSTANCE;
    }

    public String getPluginName() {
        return this.getDescriptor().getName();
    }

    public String getPluginVersion() {
        return this.getDescriptor().getVersion();
    }

    public boolean isPluginRequired(Project var1) {
        return ProjectUtilities.findAttachedProjectByName(var1, "SysML Extensions.mdxml") != null;
    }

    public static void updateMainToolbarCategory() {
        if (MAIN_TOOLBAR_ACTIONS_MANAGER == null) {
            return;
        }
        ActionsCategory category = MAIN_TOOLBAR_ACTIONS_MANAGER.getCategory(MAIN_TOOLBAR_CATEGORY_NAME);
        if (category == null) {
            return;
        }
        List<NMAction> actions = new ArrayList<>(category.getActions());
        for (NMAction action : actions) {
            category.removeAction(action);
        }
        for (NMAction action : actions) {
            category.addAction(action);
        }
    }

    @Override
    public boolean close() {
        return true;
    }

    @Override
    public void init() {
        (new MDKPluginHelper()).init();
        loadExtensionJars();
        initJavaFX();
    }

    @Override
    public boolean isSupported() {
        return true;
    }


    private void loadExtensionJars() {
        File extensionDir = new File(getDescriptor().getPluginDirectory(), "extensions");
        if (!extensionDir.exists()) {
            extensionsClassloader = MDKPlugin.class.getClassLoader();
            return;
        }
        List<URL> extensions = new ArrayList<URL>();
        try {
            extensions.add(extensionDir.toURI().toURL());
        } catch (MalformedURLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        File[] files = extensionDir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            try {
                extensions.add(file.toURI().toURL());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        extensionsClassloader = new URLClassLoader(extensions.toArray(new URL[]{}),
                MDKPlugin.class.getClassLoader());
    }

    private void initJavaFX() {
        try {
            Class.forName("javafx.application.Platform");
        } catch (ClassNotFoundException e) {
            System.err.println("[WARNING] JavaFX libraries are unavailable.");
            return;
        }
        new Thread(() -> {
            try {
                Class<?> clazz = Class.forName("org.openmbee.mdk.Launcher");
                Method method = clazz.getMethod("main", String[].class);
                //has to be before invocation since it hangs
                MDKPlugin.JAVAFX_SUPPORTED = true;
                method.invoke(null, new Object[]{new String[]{}});
            } catch (Exception | Error e) {
                MDKPlugin.JAVAFX_SUPPORTED = false;
                System.err.println("[WARNING] Failed to initialize JavaFX application. JavaFX functionality is disabled.");
                e.printStackTrace();
            }
        }, "JavaFX Init").start();
    }

    public static boolean isJavaFXSupported() {
        return JAVAFX_SUPPORTED;
    }

}
