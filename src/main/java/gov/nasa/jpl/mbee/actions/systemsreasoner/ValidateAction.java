package gov.nasa.jpl.mbee.actions.systemsreasoner;

import java.util.ArrayList;

import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ValidateAction extends SRAction {
	
	public static final String actionid = "Validate";
	public ArrayList<Element> elements;
	
	public ValidateAction(Element element) {
        super(actionid, element);
	}
	
	public ValidateAction(ArrayList<Element> elements) {
		super(actionid);
		this.elements = elements;
	}
}
