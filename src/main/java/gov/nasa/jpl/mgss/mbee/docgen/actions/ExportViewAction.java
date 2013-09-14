package gov.nasa.jpl.mgss.mbee.docgen.actions;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.generator.DocumentGenerator;
import gov.nasa.jpl.mgss.mbee.docgen.generator.DocumentValidator;
import gov.nasa.jpl.mgss.mbee.docgen.generator.PostProcessor;
import gov.nasa.jpl.mgss.mbee.docgen.model.Document;
import gov.nasa.jpl.mgss.mbee.docgen.viewedit.ViewEditUtils;
import gov.nasa.jpl.mgss.mbee.docgen.viewedit.ViewExporter;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.ui.ProgressStatusRunner;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * export view to view editor
 * recursive means export this view and all its children recursively
 * @author dlam
 *
 */
@SuppressWarnings("serial")
public class ExportViewAction extends MDAction {
	private Element doc;
	public static final String actionid = "ExportView";
	public ExportViewAction(Element e) {
		super(actionid, "Export View (Overwrite VE)", null, null);
		doc = e;
	}
	
	public void actionPerformed(ActionEvent e) {
		GUILog gl = Application.getInstance().getGUILog();
		GetMethod gm = null;
		try {
			String response = null;
			Boolean recurse = Utils.getUserYesNoAnswer("Export views recursively?");
			if (recurse == null)
				return;
			String url = ViewEditUtils.getUrl();
			if (url == null)
				return;
			gl.log("*** Starting export view ***");
			DocumentValidator dv = new DocumentValidator(doc);
			dv.validateDocument();
			dv.printErrors();
			if (dv.isFatal())
				return;
			DocumentGenerator dg = new DocumentGenerator(doc, dv, null);
			Document dge = dg.parseDocument(true, recurse);
			(new PostProcessor()).process(dge);
			ProgressStatusRunner.runWithProgressStatus(new ViewExporter(dge, doc, recurse, true, url), "Exporting View...", true, 0);
		} catch (Exception ex) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			gl.log(sw.toString()); // stack trace as a string
			ex.printStackTrace();
		} finally {
			if (gm != null)
				gm.releaseConnection();
		}
	}
}
