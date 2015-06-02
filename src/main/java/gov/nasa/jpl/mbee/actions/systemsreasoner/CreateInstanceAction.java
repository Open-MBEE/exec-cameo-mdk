package gov.nasa.jpl.mbee.actions.systemsreasoner;

import com.nomagic.magicdraw.actions.ActionsGroups;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class CreateInstanceAction extends MDAction {
	
	public static final String actionid = "Create Instance";
	public Element element; 
	
	public CreateInstanceAction(Element element) {
        super(actionid, actionid, null, ActionsGroups.APPLICATION_RELATED);
        this.element = element;
	}
	
	@Override
	/**
	 * This override gives the SRConfigurator enable/disable control over each individual action
	 * Otherwise, this action would not be able to be enabled or disabled once set
	 */
	public void updateState() {
		if (this.isEnabled())
			this.setEnabled(false);
		else
			this.setEnabled(true);
	}

}
