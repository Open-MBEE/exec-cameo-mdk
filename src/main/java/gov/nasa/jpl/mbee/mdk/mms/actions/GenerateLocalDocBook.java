package gov.nasa.jpl.mbee.mdk.mms.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.mdk.docgen.ViewViewpointValidator;
import gov.nasa.jpl.mbee.mdk.generator.DocumentWriter;
import gov.nasa.jpl.mbee.mdk.generator.PostProcessor;
import gov.nasa.jpl.mbee.mdk.model.Document;
import gov.nasa.jpl.mbee.mdk.util.Utils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Collections;

public class GenerateLocalDocBook extends MDAction {

    private static final long serialVersionUID = 1L;
    private Element view;
    public static final String DEFAULT_ID = GenerateLocalDocBook.class.getSimpleName();

    public GenerateLocalDocBook(Element view) {
        super(DEFAULT_ID, "DocBook", null, null);
        this.view = view;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            File savefile = fileSelect();
            if (savefile != null) {
                generate(savefile);
            }
        } catch (Exception ex) {
            Utils.printException(ex);
        }
    }

    private File fileSelect() {
        JFileChooser choose = new JFileChooser();
        choose.setDialogTitle("Save to output xml...");
        int retval = choose.showSaveDialog(null);
        if (retval == JFileChooser.APPROVE_OPTION) {
            return choose.getSelectedFile();
        }
        return null;
    }

    public void generate(File savefile) {
        Project project = Application.getInstance().getProject();
        ViewViewpointValidator dv = new ViewViewpointValidator(Collections.singleton(view), project, true);
        dv.run();
        if (dv.isFailed()) {
            Application.getInstance().getGUILog().log("[ERROR] View validation failed for " + Converters.getElementToHumanNameConverter().apply(view) + ". Aborting generation.");
            Utils.displayValidationWindow(project, dv.getValidationSuite(), dv.getValidationSuite().getName());
            return;
        }
        DocumentGenerator dg = new DocumentGenerator(view, dv, null);
        Document dge = dg.parseDocument();
        boolean genNewImage = dge.getGenNewImage();
        (new PostProcessor()).process(dge);
        String userName = savefile.getName();
        String filename = userName;
        if (userName.length() < 4 || !userName.endsWith(".xml")) {
            filename = userName + ".xml";
        }
        File dir = savefile.getParentFile();
        File realfile = new File(dir, filename);
        ProgressStatusRunner.runWithProgressStatus(new DocumentWriter(dge, realfile, genNewImage, dir), "Generating DocGen Docbook Document...", true, 0);
    }

}
