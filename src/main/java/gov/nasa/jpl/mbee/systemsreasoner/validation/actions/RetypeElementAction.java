package gov.nasa.jpl.mbee.systemsreasoner.validation.actions;

import gov.nasa.jpl.mbee.systemsreasoner.validation.GenericRuleViolationAction;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.TypedElement;

public class RetypeElementAction extends GenericRuleViolationAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private TypedElement source, target;
	private String name;

	public RetypeElementAction(final TypedElement source, final TypedElement target, final String name) {
		super(name);
		this.source = source;
		this.target = target;
		this.name = name;
	}

	@Override
	public void run() {
		if (!target.isEditable()) {
			Application.getInstance().getGUILog().log(target.getQualifiedName() + " is not editable. Skipping retype.");
			return;
		}
		target.setType(source.getType());
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getSessionName() {
		return "retype element";
	}
}
