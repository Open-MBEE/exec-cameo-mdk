package gov.nasa.jpl.mgss.mbee.docgen.actions;

import gov.nasa.jpl.mgss.mbee.docgen.generator.DocumentGenerator;
import gov.nasa.jpl.mgss.mbee.docgen.generator.DocumentValidator;
import gov.nasa.jpl.mgss.mbee.docgen.generator.DocumentViewer;
import gov.nasa.jpl.mgss.mbee.docgen.generator.PostProcessor;
import gov.nasa.jpl.mgss.mbee.docgen.model.Document;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * pops up a table showing views/sections/queries and targets given to queries
 * @author dlam
 *
 */
@SuppressWarnings("serial")
public class ViewDocument3Action extends MDAction {
	private Element doc;
	public static final String actionid = "ViewDocument3";
	public ViewDocument3Action(Element e) {
		super(actionid, "Preview DocGen 3 Document", null, null);
		doc = e;
	}
	
	public void actionPerformed(ActionEvent e) {
		GUILog gl = Application.getInstance().getGUILog();
		
		try {
			DocumentValidator dv = new DocumentValidator(doc);
			dv.validateDocument();
			dv.printErrors();
			if (dv.isFatal())
				return;
			DocumentGenerator dg = new DocumentGenerator(doc, null);
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
	}
}
