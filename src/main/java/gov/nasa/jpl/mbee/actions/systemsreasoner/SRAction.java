package gov.nasa.jpl.mbee.actions.systemsreasoner;

import com.nomagic.magicdraw.actions.ActionsGroups;
import com.nomagic.magicdraw.actions.MDAction;

public class SRAction extends MDAction {
	
<<<<<<< HEAD
=======
	// actuallyUseful is just a class var that keeps track of whether the action is
	// enabled or disable in an 'actually useful' way
>>>>>>> 9733057... Systems Reasoner now supports multiple selected elements
	private boolean actuallyUseful = true;
	private String actionid;

	public SRAction(String actionid) {
        super(actionid, actionid, null, ActionsGroups.APPLICATION_RELATED);
    }
	
	public void disable() {
		this.setEnabled(actuallyUseful = false);
	}
	
	public void enable() {
		this.setEnabled(actuallyUseful = true);
	}
	
	@Override
	public void updateState() {
		this.setEnabled(actuallyUseful);
	}
}
