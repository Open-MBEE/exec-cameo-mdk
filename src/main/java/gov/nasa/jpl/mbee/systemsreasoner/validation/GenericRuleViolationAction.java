package gov.nasa.jpl.mbee.systemsreasoner.validation;

import java.util.Collection;
import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

public abstract class GenericRuleViolationAction extends RuleViolationAction implements AnnotationAction, Runnable {
	
	private String name;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GenericRuleViolationAction(final String name) {
		super(name, name, null, null);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String getSessionName() {
		return name;
	}
	
	public String getProgressDescription() {
		return name;
	}
	
	public void actionPerformed(java.awt.event.ActionEvent e) {
		onStart();
		run();
		onStop();
	}

	@Override
	public boolean canExecute(Collection<Annotation> annotations) {
		return true;
	}

	@Override
	public void execute(Collection<Annotation> annotations) {
		onStart();
		//System.out.println("annotation size: " + annotations.size());
		for (final Annotation a : annotations) {
			//System.out.println("Actions size: " + a.getActions().size());
			for (final NMAction nma : a.getActions()) {
				//System.out.println("nma id: " + nma.getID());
				if (nma.getID().equals(getName()) && nma instanceof Runnable) {
					//System.out.println("Name: " + getName());
					((Runnable) nma).run();
					break;
				}
			}
		}
		onStop();
	}
	
	public void onStart() {
		if (SessionManager.getInstance().isSessionCreated()) {
			SessionManager.getInstance().closeSession();
		}
		SessionManager.getInstance().createSession(getSessionName());
		//ProgressStatusRunner.runWithProgressStatus(progress, getProgressDescription(), false, 0);
	}
	
	public void onStop() {
		if (SessionManager.getInstance().isSessionCreated()) {
			SessionManager.getInstance().closeSession();
		}
	}
	
}
