package gov.nasa.jpl.mbee.actions;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.ui.browser.actions.DefaultBrowserAction;
import com.nomagic.magicdraw.uml.ConvertElementInfo;
import com.nomagic.magicdraw.uml.Refactoring;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.components.mdbasiccomponents.Component;

public class ClassToComponentRefactorWithIDAction extends DefaultBrowserAction{

	
	Element element;
	
	private static final long serialVersionUID = 1L;

	public ClassToComponentRefactorWithIDAction(Element e) {
		super("", "Convert Class To Component", null, null);
		this.element = e;
	}
	
	public void actionPerformed(ActionEvent e){		
		
		String elementID = element.getID();
		
		SessionManager sessionManager = SessionManager.getInstance();
		sessionManager.createSession("Convert Class To Component");
		// Converts the element to an interface.
		ConvertElementInfo info = new ConvertElementInfo(Class.class);
		// Preserves the old element ID for the new element.
		info.setPreserveElementID(false);
		try {
			Element conversionTarget = Refactoring.Converting.convert(element, info);
			Application.getInstance().getProject().getCounter().setCanResetIDForObject(true);
//			element.setID(elementID);
			conversionTarget.setID(elementID);
//			String newElementID = conversionTarget.getID();
		} catch (ReadOnlyElementException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		sessionManager.closeSession();
	}

}
