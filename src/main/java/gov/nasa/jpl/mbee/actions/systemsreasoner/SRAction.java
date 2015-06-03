package gov.nasa.jpl.mbee.actions.systemsreasoner;

import com.nomagic.magicdraw.actions.ActionsGroups;
import com.nomagic.magicdraw.actions.MDAction;

public class SRAction extends MDAction {
	
	private String actionid;
	private boolean actuallyUseful = true;

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
