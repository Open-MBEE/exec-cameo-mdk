package gov.nasa.jpl.mbee.actions.sync;

import gov.nasa.jpl.mbee.actions.systemsreasoner.SRAction;
import gov.nasa.jpl.mbee.ems.sync.delta.DeltaSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.lib.Utils;

import java.awt.event.ActionEvent;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;

public class AutoSyncStatusAction extends SRAction {
	private static final long serialVersionUID = 1L;
	
	private boolean current = false;
	public static final String NAME = "MMS Sync";
	
	public AutoSyncStatusAction() {
		super(NAME + ": OFF");
	}
	
	public void update(final boolean on) {
		current = on;
		setName(AutoSyncStatusAction.NAME + ": " + (current ? "ON" : "OFF"));
	}
	
	@Override
	public void actionPerformed(final ActionEvent event) {
		Project project = Application.getInstance().getProject();
		if (project == null) {
			Utils.guilog("[ERROR] Dynamic sync can only be started when a project is open.");
			return;
		}

		// TODO Re-implement me @Ivan
			
		/*if (!current)
			DeltaSyncProjectEventListenerAdapter.initDurable(project);
		else
			DeltaSyncProjectEventListenerAdapter.close(project, true);*/
	}
}