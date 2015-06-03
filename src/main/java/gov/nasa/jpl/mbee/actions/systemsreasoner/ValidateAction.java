package gov.nasa.jpl.mbee.actions.systemsreasoner;

import java.util.ArrayList;

import com.nomagic.magicdraw.ui.browser.Node;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ValidateAction extends SRAction {
	
	public static final String actionid = "Validate";
	public Element element; 
	public ArrayList<Element> elements;
	
	public ValidateAction(Element element) {
        super(actionid);
        this.element = element;
	}
	
	public ValidateAction(Node[] nodes) {
		super(actionid);
		for (Node n: nodes) {
			if (n instanceof Element) {
				this.elements.add((Element)n);
			}
		}
	}
	
}
