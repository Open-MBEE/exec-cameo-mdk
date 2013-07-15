package gov.nasa.jpl.mgss.mbee.docgen.actions;

import gov.nasa.jpl.mgss.mbee.docgen.generator.DocumentValidator;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * validates docgen 3 doc - checks for loops, duplicate dependencies, etc
 * @author dlam
 *
 */
@SuppressWarnings("serial")
public class ValidateDocument3Action extends MDAction {
	private Element doc;
	public static final String actionid = "ValidateDocument3";
	public ValidateDocument3Action(Element e) {
		super(actionid, "Validate DocGen 3 Document", null, null);
		doc = e;
	}
	
	public void actionPerformed(ActionEvent e) {
		GUILog gl = Application.getInstance().getGUILog();
		
		try {
			DocumentValidator dv = new DocumentValidator(doc);
			dv.validateDocument();
			dv.printErrors();
			
			
		} catch (Exception ex) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			gl.log(sw.toString()); // stack trace as a string
			ex.printStackTrace();
		}
	}
}
