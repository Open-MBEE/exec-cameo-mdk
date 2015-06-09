package gov.nasa.jpl.mbee.actions.systemsreasoner;

import com.nomagic.magicdraw.actions.ActionsGroups;
import com.nomagic.magicdraw.actions.ActionsStateUpdater;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class SRAction extends MDAction {
	
	private String actionid;
	public Element element;

	public SRAction(String actionid) {
        super(actionid, actionid, null, ActionsGroups.APPLICATION_RELATED);
		this.actionid = actionid;
	}
	
	public SRAction(String actionid, Element element) {
		this(actionid);
        this.element = element;
    }
	
	public void disable() {
		setEnabled(false);
	}
	
	public void disable(String error) {
		this.setName(actionid + " [" + error + "]");
		disable();
	}
	
	public void enable() {
		setEnabled(true);
	}
	
	@Override
	public void updateState() {
		setEnabled(true);
	}
	
}
