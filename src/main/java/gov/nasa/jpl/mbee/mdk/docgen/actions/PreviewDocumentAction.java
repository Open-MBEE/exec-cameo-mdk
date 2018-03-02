package gov.nasa.jpl.mbee.mdk.docgen.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.mdk.docgen.ViewViewpointValidator;
import gov.nasa.jpl.mbee.mdk.generator.DocumentViewer;
import gov.nasa.jpl.mbee.mdk.generator.PostProcessor;
import gov.nasa.jpl.mbee.mdk.model.Document;
import gov.nasa.jpl.mbee.mdk.util.Utils;

import java.awt.event.ActionEvent;
import java.util.Collections;

/**
 * pops up a table showing views/sections/queries and targets given to queries
 *
 * @author dlam
 */
public class PreviewViewAction extends MDAction {
    private Element view;
    public static final String DEFAULT_ID = PreviewViewAction.class.getSimpleName();

    public PreviewViewAction(Element e) {
        super(DEFAULT_ID, "Preview View Locally", null, null);
        view = e;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        Project project = Application.getInstance().getProject();
        try {
            ViewViewpointValidator dv = new ViewViewpointValidator(Collections.singleton(view), Application.getInstance().getProject(), true);
            dv.run();
            if (dv.isFailed()) {
                Application.getInstance().getGUILog().log("[ERROR] View validation failed for " + Converters.getElementToHumanNameConverter().apply(view) + ". Aborting preview.");
                Utils.displayValidationWindow(project, dv.getValidationSuite(), dv.getValidationSuite().getName());
                return;
            }
            DocumentGenerator dg = new DocumentGenerator(view, dv, null);
            Document dge = dg.parseDocument();
            (new PostProcessor()).process(dge);
            DocumentViewer.view(dge);
        } catch (Exception e) {
            e.printStackTrace();
            Application.getInstance().getGUILog().log("[ERROR] An unexpected error occurred while validating views. View validation aborted. Reason: " + e.getMessage());
        }
    }
}
