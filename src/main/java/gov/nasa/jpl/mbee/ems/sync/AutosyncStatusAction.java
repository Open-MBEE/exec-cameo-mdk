package gov.nasa.jpl.mbee.ems.sync;

import gov.nasa.jpl.mbee.actions.systemsreasoner.SRAction;

import java.awt.event.ActionEvent;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;

public class AutosyncStatusAction extends SRAction {	
	private static final long serialVersionUID = 1L;
	
	private boolean current = false;
	public static final String NAME = "MMS Sync";
	
	public AutosyncStatusAction() {
		super(NAME + ": OFF");
	}
	
	public void update(final boolean on) {
		current = on;
		setName(AutosyncStatusAction.NAME + ": " + (current ? "ON" : "OFF"));
	}
	
	@Override
	public void actionPerformed(final ActionEvent event) {
		Project project = Application.getInstance().getProject();
		if (!current)
			AutoSyncProjectListener.initDurable(project);
		else
			AutoSyncProjectListener.close(project, true);
	}
}