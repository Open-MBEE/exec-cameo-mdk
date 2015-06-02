package gov.nasa.jpl.mbee.actions.systemsreasoner;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class CopyAction extends MDAction {
	
	public static final String actionid = "Copy";
	public Element element; 
	
	public CopyAction(Element element) {
        super(actionid, actionid, null, null);
        this.element = element;
	}

}
