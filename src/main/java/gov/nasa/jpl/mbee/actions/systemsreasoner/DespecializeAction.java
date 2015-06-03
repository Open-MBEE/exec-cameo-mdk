package gov.nasa.jpl.mbee.actions.systemsreasoner;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

public class DespecializeAction extends SRAction {
	
	public static final String actionid = "Despecialize";
	public Class clazz;
	
	public DespecializeAction(Class clazz) {
        super(actionid, clazz);
        this.clazz = clazz;
	}
	
}
