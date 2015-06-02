package gov.nasa.jpl.mbee.actions.systemsreasoner;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class CopyAction extends SRAction {
	
	public static final String actionid = "Copy";
	public Element element; 
	
	public CopyAction(Element element) {
        super(actionid);
        this.element = element;
	}

}
