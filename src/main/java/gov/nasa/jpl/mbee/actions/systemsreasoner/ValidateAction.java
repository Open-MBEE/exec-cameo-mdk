package gov.nasa.jpl.mbee.actions.systemsreasoner;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ValidateAction extends MDAction {
	
	public static final String actionid = "Despecialize";
	public Element element; 
	
	public ValidateAction(Element element) {
        super(actionid, actionid, null, null);
        this.element = element;
	}

}
