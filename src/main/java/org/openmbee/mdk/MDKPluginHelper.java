package org.openmbee.mdk;

import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;
import com.nomagic.magicdraw.commandline.CommandLineActionManager;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.options.EnvironmentOptions;
import com.nomagic.magicdraw.evaluation.EvaluationConfigurator;
import com.nomagic.magicdraw.hyperlinks.HyperlinksHandlersRegistry;
import com.nomagic.magicdraw.uml.DiagramDescriptor;
import com.nomagic.magicdraw.uml.DiagramTypeConstants;
import org.openmbee.mdk.cli.AutomatedCommitter;
import org.openmbee.mdk.cli.AutomatedViewGenerator;
import org.openmbee.mdk.hyperlinks.TranclusionHandler;
import org.openmbee.mdk.hyperlinks.TransclusionEditorPanel;
import org.openmbee.mdk.mms.sync.status.SyncStatusConfigurator;
import org.openmbee.mdk.options.ConfigureProjectOptions;
import org.openmbee.mdk.options.MDKEnvironmentOptionsGroup;
import org.openmbee.mdk.util.MDUtils;

public class MDKPluginHelper {


    public  MDKPluginHelper() {

    }

    public void init() {
        (new ConfigureProjectOptions()).configure();
        configureEnvironmentOptions();

        ActionsConfiguratorsManager acm = ActionsConfiguratorsManager.getInstance();
        if (MDUtils.isDeveloperMode()) {
            System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
            System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
            System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.wire", "INFO");
        }
        // This somehow allows things to be loaded to evaluate opaque expressions or something.
        EvaluationConfigurator.getInstance().registerBinaryImplementers(this.getClass().getClassLoader());

        // Add Transclusion Handler
        HyperlinksHandlersRegistry.addHandler(new TranclusionHandler());


        CommandLineActionManager.getInstance().addAction(new AutomatedViewGenerator());
        CommandLineActionManager.getInstance().addAction(new AutomatedCommitter());

        MDKConfigurator mdkConfigurator = new MDKConfigurator();
        acm.addMainMenuConfigurator(mdkConfigurator);
        acm.addContainmentBrowserContextConfigurator(mdkConfigurator);
        acm.addSearchBrowserContextConfigurator(mdkConfigurator);
        acm.addBaseDiagramContextConfigurator(DiagramTypeConstants.UML_ANY_DIAGRAM, mdkConfigurator);

        acm.addMainMenuConfigurator(new MMSConfigurator());
        EvaluationConfigurator.getInstance().registerBinaryImplementers(MDKPlugin.class.getClassLoader());

        acm.addMainToolbarConfigurator(new SyncStatusConfigurator());

        DiagramDescriptor viewDiagramDescriptor = Application.getInstance().getDiagramDescriptor(ViewDiagramConfigurator.DIAGRAM_NAME);
        if (viewDiagramDescriptor != null) {
            ActionsConfiguratorsManager actionsConfiguratorsManager = ActionsConfiguratorsManager.getInstance();
            ViewDiagramConfigurator viewDiagramConfigurator = new ViewDiagramConfigurator();
            actionsConfiguratorsManager.addDiagramToolbarConfigurator(ViewDiagramConfigurator.DIAGRAM_NAME, viewDiagramConfigurator);
            actionsConfiguratorsManager.addTargetElementAMConfigurator(ViewDiagramConfigurator.DIAGRAM_NAME, viewDiagramConfigurator);
        }

        EvaluationConfigurator.getInstance().registerBinaryImplementers(MDKPlugin.class.getClassLoader());

        MMSSyncPlugin.getInstance().init();
    }

    private void configureEnvironmentOptions() {
        EnvironmentOptions mdkOptions = Application.getInstance().getEnvironmentOptions();
        mdkOptions.addGroup(MDKEnvironmentOptionsGroup.getInstance());
    }




}
