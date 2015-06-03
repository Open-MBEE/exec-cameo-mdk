package gov.nasa.jpl.mbee.actions.systemsreasoner;

import com.nomagic.magicdraw.actions.ActionsGroups;
import com.nomagic.magicdraw.actions.MDAction;

public class SRAction extends MDAction {
	
	// actuallyUseful is just a class var that keeps track of whether the action is
	// enabled or disable in an 'actually useful' way
	private boolean actuallyUseful = true;
	private String actionid;

	public SRAction(String actionid) {
        super(actionid, actionid, null, ActionsGroups.APPLICATION_RELATED);
        this.actionid = actionid;
    }
	
	public void disable() {
		this.setEnabled(actuallyUseful = false);
	}
	
	public void disable(String error) {
		this.setName(actionid + " [" + error + "]");
		this.disable();
	}
	
	public void enable() {
		this.setEnabled(actuallyUseful = true);
	}
	
	@Override
	public void updateState() {
		this.setEnabled(actuallyUseful);
	}
}
