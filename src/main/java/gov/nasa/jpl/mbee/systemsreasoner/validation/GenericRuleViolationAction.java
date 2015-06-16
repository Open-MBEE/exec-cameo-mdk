package gov.nasa.jpl.mbee.systemsreasoner.validation;

import java.util.Collection;

import javax.swing.KeyStroke;

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.openapi.uml.SessionManager;

import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

public abstract class GenericRuleViolationAction extends RuleViolationAction implements AnnotationAction, Runnable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GenericRuleViolationAction(String paramString1, String paramString2,
			KeyStroke paramKeyStroke, String paramString3) {
		super(paramString1, paramString2, paramKeyStroke, paramString3);
	}
	
	public abstract String getName();
	public abstract String getSessionName();
	
	public void actionPerformed(java.awt.event.ActionEvent e) {
		SessionManager.getInstance().createSession(getSessionName());
		run();
		SessionManager.getInstance().closeSession();
	}

	@Override
	public boolean canExecute(Collection<Annotation> arg0) {
		return true;
	}

	@Override
	public void execute(Collection<Annotation> annotations) {
		SessionManager.getInstance().createSession(getSessionName());
		for (final Annotation a : annotations) {
			for (final NMAction nma : a.getActions()) {
				if (nma.getID().equals(getName()) && nma instanceof Runnable) {
					((Runnable) nma).run();
					break;
				}
			}
		}
		SessionManager.getInstance().closeSession();
	}
	
	

}
