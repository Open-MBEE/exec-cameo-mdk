package gov.nasa.jpl.mbee.systemsreasoner.validation.actions;

import gov.nasa.jpl.mbee.systemsreasoner.validation.GenericRuleViolationAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.RedefinableElement;

public class SetRedefinitionAction extends GenericRuleViolationAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private RedefinableElement redefiningElement, redefinedElement;
	private String name;

	public SetRedefinitionAction(final RedefinableElement redefiningElement, final RedefinableElement redefinedElement, String name) {
		super(name, name, null, null);
		this.redefiningElement = redefiningElement;
		this.redefinedElement = redefinedElement;
		this.name = name;
	}

	@Override
	public void run() {
		redefiningElement.getRedefinedElement().add(redefinedElement);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getSessionName() {
		return "set redefinition";
	}
	
}
