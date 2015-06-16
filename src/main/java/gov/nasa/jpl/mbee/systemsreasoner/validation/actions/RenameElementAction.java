package gov.nasa.jpl.mbee.systemsreasoner.validation.actions;

import gov.nasa.jpl.mbee.systemsreasoner.validation.GenericRuleViolationAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

public class RenameElementAction extends GenericRuleViolationAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private NamedElement source, target;
	private String name;

	public RenameElementAction(final NamedElement source, final NamedElement target, final String name) {
		super(name, name, null, null);
		this.source = source;
		this.target = target;
		this.name = name;
	}

	@Override
	public void run() {
		target.setName(source.getName());
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getSessionName() {
		return "rename element";
	}
}
