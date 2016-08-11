package gov.nasa.jpl.mbee.actions.systemsreasoner;

import java.awt.event.ActionEvent;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import gov.nasa.jpl.mbee.ems.EMFImporter;

public class CopyAction extends SRAction {

	/**
	 * Defunct. Was for testing purposes only.
	 */
	private static final long serialVersionUID = 1L;
	public static final String actionid = "Import JSON";

	public CopyAction(Element element) {
		super(actionid, element);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		EMFImporter imp = new EMFImporter(element);
		imp.createElementsFromJSON();
		// final JSONObject json = ExportUtility.fillElement(element, new JSONObject());
		// System.out.println(json.toJSONString());
		// final Element owner = element.getOwner();
		// SessionManager.getInstance().createSession("importing element");
		// element.refDelete();
		// Element copy = null;
		// try {
		// copy = ImportUtility.createElement(json, false);
		// copy = ImportUtility.createElement(json, true);
		// } catch (ImportException ex) {
		//
		// }
		// System.out.println(copy);
		// owner.getOwnedElement().add(copy);
		// SessionManager.getInstance().closeSession();
	}

	// public Element clone(final Element element) {

	// }

}
