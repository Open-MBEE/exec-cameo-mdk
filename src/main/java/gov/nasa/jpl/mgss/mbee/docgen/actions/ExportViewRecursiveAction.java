package gov.nasa.jpl.mgss.mbee.docgen.actions;

import gov.nasa.jpl.mgss.mbee.docgen.generator.DocumentValidator;
import gov.nasa.jpl.mgss.mbee.docgen.viewedit.ViewEditUtils;
import gov.nasa.jpl.mgss.mbee.docgen.viewedit.ViewExporter;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * export view to view editor recursive means export this view and all its
 * children recursively
 * 
 * @author dlam
 * 
 */
@SuppressWarnings("serial")
public class ExportViewRecursiveAction extends MDAction {
    private Element            doc;
    public static final String actionid = "ExportViewRecursive";

    public ExportViewRecursiveAction(Element e) {
        super(actionid, "Export Model (Overwrite)", null, null);
        doc = e;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GUILog gl = Application.getInstance().getGUILog();
        DocumentValidator dv = null;
        try {
            Boolean recurse = true; // Utils.getUserYesNoAnswer("Export views recursively?");
            String url = ViewEditUtils.getUrl();
            if (url == null)
                return;
            gl.log("*** Starting export model ***");
            dv = new DocumentValidator(doc);
            dv.validateDocument();
            if (dv.isFatal()) {
                dv.printErrors();
                return;
            }
            ProgressStatusRunner.runWithProgressStatus(new ViewExporter(null, doc, recurse, true, url, dv),
                    "Exporting Model...", true, 0);
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            gl.log(sw.toString()); // stack trace as a string
            ex.printStackTrace();
        }
        if (dv != null)
            dv.printErrors();
    }
}
