package gov.nasa.jpl.mgss.mbee.docgen.sync;

import javax.swing.JOptionPane;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

/**
 * Action wrapper that handles MagicDraw sessions, leaving
 * the action itself to be implemented in a subclass.
 * Rather than assuming that a session either exists or
 * needs to be created, this handles both cases from a
 * "defensive programming" posture.
 */
public abstract class ChangeTheModel implements Runnable {

	public abstract String getDescription();

	protected abstract void makeChange();

	/**
	 * Override to do something after makeChange() session.
	 * If the change uses a new session, cleanUp() is called
	 * after the session is committed.
	 */
	protected void cleanUp() { }

	@Override
	public void run() {
		boolean cancel = false;
		boolean close = false;
		try {
			if (!SessionManager.getInstance().isSessionCreated()) {
				SessionManager.getInstance().createSession(getDescription());
				close = true;
				cancel = true;
			}
			makeChange();
			cancel = false;
		} finally {
			if (cancel) {
				SessionManager.getInstance().cancelSession();
			} else if (close) {
				SessionManager.getInstance().closeSession();
			}
			cleanUp();
		}
	}

	protected String getUsername() {
		String username;
		String teamworkUsername = TeamworkUtils.getLoggedUserName();
		if (teamworkUsername != null) {
			username = teamworkUsername;
		} else {
			username = System.getProperty("user.name", "");
		}
		return username;
	}

	protected Stereotype getStereotype(String name) {
		Project project = Application.getInstance().getProject();
		return StereotypesHelper.getStereotype(project, name);
	}

	protected void fail(String reason) {
		JOptionPane.showMessageDialog(null, "Failed: " + reason);
	}
}