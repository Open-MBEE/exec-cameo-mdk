package gov.nasa.jpl.mbee.actions.systemsreasoner;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class DespecializeAction extends SRAction {
	
	public static final String actionid = "Despecialize";
	public Element element; 
	
	public DespecializeAction(Element element) {
        super(actionid);
        this.element = element;
	}
	
}
