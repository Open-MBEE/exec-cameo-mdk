package gov.nasa.jpl.mbee.actions.ems;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils.AvailableAttribute;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import com.nomagic.actions.NMAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class PurgeHistory extends NMAction {
	
	private static final String actionid = "Purge History";
	private Element element;
	
	public PurgeHistory(Element e, String name) {
        super(actionid, name, null, null);	
        this.element = e;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object docu = Utils.getElementAttribute(element, AvailableAttribute.Documentation);
		System.out.println(docu.toString());
	}

}
