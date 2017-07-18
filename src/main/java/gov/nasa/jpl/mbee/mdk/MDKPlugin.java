package gov.nasa.jpl.mbee.mdk;

import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.options.EnvironmentOptions;
import com.nomagic.magicdraw.evaluation.EvaluationConfigurator;
import com.nomagic.magicdraw.plugins.Plugin;
import com.nomagic.magicdraw.plugins.PluginDescriptor;
import com.nomagic.magicdraw.plugins.PluginUtils;
import com.nomagic.magicdraw.properties.Property;
import com.nomagic.magicdraw.uml.DiagramTypeConstants;
import gov.nasa.jpl.mbee.mdk.mms.sync.queue.OutputQueueStatusConfigurator;
import gov.nasa.jpl.mbee.mdk.mms.sync.queue.OutputSyncRunner;
import gov.nasa.jpl.mbee.mdk.mms.sync.status.SyncStatusConfigurator;
import gov.nasa.jpl.mbee.mdk.ocl.OclQueryConfigurator;
import gov.nasa.jpl.mbee.mdk.options.MDKOptionsGroup;
import gov.nasa.jpl.mbee.mdk.systems_reasoner.SRConfigurator;
import gov.nasa.jpl.mbee.mdk.util.MDUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class MDKPlugin extends Plugin {
    public static final String MAIN_TOOLBAR_CATEGORY_NAME = "MDK";

    private static String VERSION;
    public static ClassLoader extensionsClassloader;
    public static ActionsManager MAIN_TOOLBAR_ACTIONS_MANAGER;

    public MDKPlugin() {
        super();
    }

    public static String getVersion() {
        if (VERSION == null) {
            VERSION = PluginUtils.getPlugins().stream().map(Plugin::getDescriptor).filter(descriptor -> descriptor.getName().equals("Model Development Kit")).map(PluginDescriptor::getVersion).findAny().orElse(null);
        }
        return VERSION;
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
        ActionsConfiguratorsManager acm = ActionsConfiguratorsManager.getInstance();
        System.setProperty("jsse.enableSNIExtension", "false");
        if (MDUtils.isDeveloperMode()) {
            System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
            System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
            System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.wire", "INFO");
        }
        // This somehow allows things to be loaded to evaluate opaque expressions or something.
        EvaluationConfigurator.getInstance().registerBinaryImplementers(this.getClass().getClassLoader());

        MDKConfigurator mdkConfigurator = new MDKConfigurator();
        acm.addContainmentBrowserContextConfigurator(mdkConfigurator);
        acm.addSearchBrowserContextConfigurator(mdkConfigurator);
        acm.addBaseDiagramContextConfigurator(DiagramTypeConstants.UML_ANY_DIAGRAM, mdkConfigurator);

        OclQueryConfigurator oclQueryConfigurator = new OclQueryConfigurator();
        acm.addMainMenuConfigurator(oclQueryConfigurator);
        acm.addSearchBrowserContextConfigurator(oclQueryConfigurator);
        acm.addContainmentBrowserContextConfigurator(oclQueryConfigurator);
        acm.addBaseDiagramContextConfigurator(DiagramTypeConstants.UML_ANY_DIAGRAM, oclQueryConfigurator);

        acm.addMainMenuConfigurator(new MMSConfigurator());
        EvaluationConfigurator.getInstance().registerBinaryImplementers(MDKPlugin.class.getClassLoader());

        SRConfigurator srConfigurator = new SRConfigurator();
        acm.addSearchBrowserContextConfigurator(srConfigurator);
        acm.addContainmentBrowserContextConfigurator(srConfigurator);
        acm.addBaseDiagramContextConfigurator(DiagramTypeConstants.UML_ANY_DIAGRAM, srConfigurator);

        acm.addMainToolbarConfigurator(new OutputQueueStatusConfigurator());
        acm.addMainToolbarConfigurator(new SyncStatusConfigurator());

        EvaluationConfigurator.getInstance().registerBinaryImplementers(MDKPlugin.class.getClassLoader());

        MMSSyncPlugin.getInstance().init();
        (new Thread(new OutputSyncRunner())).start();

        loadExtensionJars();
        configureEnvironmentOptions();
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

    private void configureEnvironmentOptions() {
        EnvironmentOptions mdkOptions = Application.getInstance().getEnvironmentOptions();
        mdkOptions.addGroup(new MDKOptionsGroup());
        EnvironmentOptions.EnvironmentChangeListener mdkEnvOptionsListener = list -> {
            Property advancedOptions = MDKOptionsGroup.getMDKOptions().getProperty(MDKOptionsGroup.SHOW_ADVANCED_OPTIONS_ID);
            for (Property p : list) {
                if (p.equals(advancedOptions) && MDKOptionsGroup.getMDKOptions().isMDKAdvancedOptions()) {
                    Application.getInstance().getGUILog().log("[INFO] You must restart MagicDraw to show advanced MDK options.");
                }
            }
        };
        mdkOptions.addEnvironmentChangeListener(mdkEnvOptionsListener);
    }

}
