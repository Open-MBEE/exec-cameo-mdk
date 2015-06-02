package gov.nasa.jpl.mbee.actions.systemsreasoner;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class DespecializeAction extends MDAction {
	
	public static final String actionid = "Despecialize";
	public Element element; 
	
	public DespecializeAction(Element element) {
        super(actionid, actionid, null, null);
        this.element = element;
	}
	
}
