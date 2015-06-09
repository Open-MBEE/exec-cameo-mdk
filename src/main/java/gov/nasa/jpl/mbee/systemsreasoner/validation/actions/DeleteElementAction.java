package gov.nasa.jpl.mbee.systemsreasoner.validation.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class DeleteElementAction extends MDAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Element element;

	public DeleteElementAction(final Element element, final String title) {
		super(title, title, null, null);
		this.element = element;
	}
	
	@Override
	public void actionPerformed(java.awt.event.ActionEvent e) {
		SessionManager.getInstance().createSession("delete element");
		element.refDelete();
		SessionManager.getInstance().closeSession();
	}
}
