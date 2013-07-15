package gov.nasa.jpl.mgss.mbee.docgen.actions;

import java.awt.event.ActionEvent;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ImportViewDryAction extends MDAction {

	private Element doc;
	public static final String actionid = "ImportViewDry";
	
	public ImportViewDryAction(Element e) {
		super(actionid, "Validate Sync", null, null);
		doc = e;
	}
	
	public void actionPerformed(ActionEvent e) {
		ImportViewAction.doImportView(doc, false, null, null);
	}
}
