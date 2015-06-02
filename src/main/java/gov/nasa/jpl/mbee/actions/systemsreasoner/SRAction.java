package gov.nasa.jpl.mbee.actions.systemsreasoner;

import com.nomagic.magicdraw.actions.ActionsGroups;
import com.nomagic.magicdraw.actions.MDAction;

public class SRAction extends MDAction {
	
	private boolean actuallyUseful = true;

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
