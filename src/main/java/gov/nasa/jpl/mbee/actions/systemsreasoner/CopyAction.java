package gov.nasa.jpl.mbee.actions.systemsreasoner;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.ImportUtility;
import gov.nasa.jpl.mbee.ems.ImportException;

import java.awt.event.ActionEvent;

import org.json.simple.JSONObject;

import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class CopyAction extends SRAction {
	
	/**
	 * Defunct. Was for testing purposes only.
	 */
	private static final long serialVersionUID = 1L;
	public static final String actionid = "Copy";
	
	public CopyAction(Element element) {
        super(actionid, element);
	}
	
	@Override
    public void actionPerformed(ActionEvent e) {
		final JSONObject json = ExportUtility.fillElement(element, new JSONObject());
		System.out.println(json.toJSONString());
		final Element owner = element.getOwner();
		SessionManager.getInstance().createSession("copying element");
		element.refDelete();
		Element copy = null;
		try {
		    copy = ImportUtility.createElement(json, false);
		    copy = ImportUtility.createElement(json, true);
		} catch (ImportException ex) {
		    
		}
		System.out.println(copy);
		owner.getOwnedElement().add(copy);
		SessionManager.getInstance().closeSession();
	}
	
	//public Element clone(final Element element) {
		
	//}

}
