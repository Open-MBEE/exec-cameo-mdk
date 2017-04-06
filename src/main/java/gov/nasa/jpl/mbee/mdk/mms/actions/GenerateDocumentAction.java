package gov.nasa.jpl.mbee.mdk.mms.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.mdk.generator.DocumentValidator;
import gov.nasa.jpl.mbee.mdk.generator.DocumentWriter;
import gov.nasa.jpl.mbee.mdk.generator.PostProcessor;
import gov.nasa.jpl.mbee.mdk.model.Document;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * generates docgen 3 document
 *
 * @author dlam
 */
public class GenerateDocumentAction extends MDAction {

    private static final long serialVersionUID = 1L;
    private Element doc;
    public static final String DEFAULT_ID = "GenerateDocument";

    public GenerateDocumentAction(Element e) {
        super(DEFAULT_ID, "Generate DocGen 3 Document", null, null);
        doc = e;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUILog gl = Application.getInstance().getGUILog();
        try {
            File savefile = fileSelect();
            if (savefile != null) {
                generate(savefile);
            }
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            gl.log(sw.toString()); // stack trace as a string
            ex.printStackTrace();
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
        DocumentValidator dv = new DocumentValidator(doc);
        dv.validateDocument();
        if (dv.isFatal()) {
            dv.printErrors();
            return;
        }
        DocumentGenerator dg = new DocumentGenerator(doc, dv, null);
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
        ProgressStatusRunner.runWithProgressStatus(new DocumentWriter(dge, realfile, genNewImage,
                dir), "Generating DocGen 3 Document...", true, 0);
        dv.printErrors();
    }

}
