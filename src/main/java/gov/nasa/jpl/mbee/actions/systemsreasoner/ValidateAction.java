package gov.nasa.jpl.mbee.actions.systemsreasoner;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ValidateAction extends SRAction {
	
	public static final String actionid = "Validate";
	public Element element; 
	
	public ValidateAction(Element element) {
        super(actionid);
        this.element = element;
	}
	
}
