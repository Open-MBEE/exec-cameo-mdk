package gov.nasa.jpl.mbee.actions;

import gov.nasa.jpl.mbee.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.generator.DocumentValidator;
import gov.nasa.jpl.mbee.generator.DocumentWriter;
import gov.nasa.jpl.mbee.generator.PostProcessor;
import gov.nasa.jpl.mbee.model.Document;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JFileChooser;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * generates docgen 3 document
 * 
 * @author dlam
 * 
 */
@SuppressWarnings("serial")
public class GenerateDocumentAction extends MDAction {

    private Element            doc;
    public static final String actionid = "GenerateDocument";

    public GenerateDocumentAction(Element e) {
        super(actionid, "Generate DocGen 3 Document", null, null);
        doc = e;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUILog gl = Application.getInstance().getGUILog();

        DocumentValidator dv = new DocumentValidator(doc);
        try {
            dv.validateDocument();
            if (dv.isFatal()) {
                dv.printErrors();
                return;
            }
            DocumentGenerator dg = new DocumentGenerator(doc, dv, null);
            Document dge = dg.parseDocument();
            boolean genNewImage = dge.getGenNewImage();
            (new PostProcessor()).process(dge);
            JFileChooser choose = new JFileChooser();
            choose.setDialogTitle("Save to output xml...");
            int retval = choose.showSaveDialog(null);
            if (retval == JFileChooser.APPROVE_OPTION) {
                if (choose.getSelectedFile() != null) {
                    File savefile = choose.getSelectedFile();
                    String userName = savefile.getName();
                    String filename = userName;
                    if (userName.length() < 4 || !userName.endsWith(".xml"))
                        filename = userName + ".xml";
                    File dir = savefile.getParentFile();
                    File realfile = new File(dir, filename);
                    ProgressStatusRunner.runWithProgressStatus(new DocumentWriter(dge, realfile, genNewImage,
                            dir), "Generating DocGen 3 Document...", true, 0);
                }
            }
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            gl.log(sw.toString()); // stack trace as a string
            ex.printStackTrace();
        }
        dv.printErrors();
    }
}
