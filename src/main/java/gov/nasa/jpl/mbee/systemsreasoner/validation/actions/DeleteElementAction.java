package gov.nasa.jpl.mbee.systemsreasoner.validation.actions;

import gov.nasa.jpl.mbee.systemsreasoner.validation.GenericRuleViolationAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class DeleteElementAction extends GenericRuleViolationAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Element element;
	private String name;

	public DeleteElementAction(final Element element, final String name) {
		super(name, name, null, null);
		this.element = element;
		this.name = name;
	}
	
	@Override
	public void run() {
		element.refDelete();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getSessionName() {
		return "delete element";
	}
}
