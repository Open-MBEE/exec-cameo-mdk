package gov.nasa.jpl.mbee.mdk.docgen.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.mdk.generator.DocumentValidator;
import gov.nasa.jpl.mbee.mdk.generator.DocumentViewer;
import gov.nasa.jpl.mbee.mdk.generator.PostProcessor;
import gov.nasa.jpl.mbee.mdk.model.Document;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * pops up a table showing views/sections/queries and targets given to queries
 *
 * @author dlam
 */
public class ViewDocument3Action extends MDAction {

    private static final long serialVersionUID = 1L;
    private Element doc;
    public static final String DEFAULT_ID = "ViewDocument3";

    public ViewDocument3Action(Element e) {
        super(DEFAULT_ID, "Preview DocGen 3 Document", null, null);
        doc = e;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUILog gl = Application.getInstance().getGUILog();

        DocumentValidator dv = null;
        try {
            dv = new DocumentValidator(doc);
            dv.validateDocument();
            if (dv.isFatal()) {
                dv.printErrors();
                return;
            }
            DocumentGenerator dg = new DocumentGenerator(doc, dv, null);
            Document dge = dg.parseDocument();
            (new PostProcessor()).process(dge);
            DocumentViewer.view(dge);
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            gl.log(sw.toString()); // stack trace as a string
            ex.printStackTrace();
        }
        if (dv != null) {
            dv.printErrors();
        }
    }
}
