package gov.nasa.jpl.mbee.actions.ems;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import gov.nasa.jpl.mbee.ems.sync.realtime.RealTimeSyncProjectEventListenerAdapter;

import java.awt.event.ActionEvent;

public class StartAutoSyncAction extends MMSAction {
	private static final long serialVersionUID = 1L;
	public static final String actionid = "StartAutoSync";

	public StartAutoSyncAction() {
		super(actionid, "Start Dynamic Sync", null, null);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Project project = Application.getInstance().getProject();
		if (project == null) {
			return;
		}
		RealTimeSyncProjectEventListenerAdapter.getProjectMapping(project).setDisabled(false);
	}

}
