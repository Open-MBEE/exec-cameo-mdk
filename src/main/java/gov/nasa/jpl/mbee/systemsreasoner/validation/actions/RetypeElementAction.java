package gov.nasa.jpl.mbee.systemsreasoner.validation.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.TypedElement;

public class RetypeElementAction extends MDAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private TypedElement source, target;

	public RetypeElementAction(final TypedElement source, final TypedElement target, final String title) {
		super(title, title, null, null);
		this.source = source;
		this.target = target;
	}
	
	@Override
	public void actionPerformed(java.awt.event.ActionEvent e) {
		SessionManager.getInstance().createSession("retype element");
		target.setType(source.getType());
		SessionManager.getInstance().closeSession();
	}
}
