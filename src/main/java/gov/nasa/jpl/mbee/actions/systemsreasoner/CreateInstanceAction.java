package gov.nasa.jpl.mbee.actions.systemsreasoner;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class CreateInstanceAction extends MDAction {
	
	public static final String actionid = "Create Instance";
	public Element element; 
	
	public CreateInstanceAction(Element element) {
        super(actionid, actionid, null, null);
        this.element = element;
	}

}
