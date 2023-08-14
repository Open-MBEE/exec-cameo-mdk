package org.openmbee.mdk.docgen.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import org.openmbee.mdk.api.incubating.convert.Converters;
import org.openmbee.mdk.generator.DocumentGenerator;
import org.openmbee.mdk.docgen.ViewViewpointValidator;
import org.openmbee.mdk.generator.DocumentViewer;
import org.openmbee.mdk.generator.PostProcessor;
import org.openmbee.mdk.model.Document;
import org.openmbee.mdk.util.Utils;

import java.awt.event.ActionEvent;
import java.util.Collections;

/**
 * pops up a table showing views/sections/queries and targets given to queries
 *
 * @author dlam
 */
public class PreviewDocumentAction extends MDAction {
    private Element view;
    public static final String DEFAULT_ID = PreviewDocumentAction.class.getSimpleName();

    public PreviewDocumentAction(Element e) {
        super(DEFAULT_ID, "Preview Document", null, null);
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
