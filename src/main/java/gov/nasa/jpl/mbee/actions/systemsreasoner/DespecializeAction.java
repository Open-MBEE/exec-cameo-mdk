package gov.nasa.jpl.mbee.actions.systemsreasoner;

import com.nomagic.magicdraw.actions.ActionsGroups;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class DespecializeAction extends MDAction {
	
	public static final String actionid = "Despecialize";
	public Element element; 
	
	public DespecializeAction(Element element) {
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
