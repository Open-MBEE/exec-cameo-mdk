package gov.nasa.jpl.mbee.ems.sync;

import com.nomagic.actions.AMConfigurator;
import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import gov.nasa.jpl.mbee.actions.sync.AutoSyncStatusAction;

public class AutoSyncStatusConfigurator implements AMConfigurator {
	
	private static AutoSyncStatusAction statusAction;
	
	public static final String SYNC_STATUS = "Autosync Status";

	public static AutoSyncStatusAction getStatusAction() {
		if (statusAction == null) {
			statusAction = new AutoSyncStatusAction();
		}
		return statusAction;
	}
	
	@Override
	public int getPriority() {
		return AMConfigurator.MEDIUM_PRIORITY;
	}

	@Override
	public void configure(ActionsManager mngr) {
		final ActionsCategory category = new ActionsCategory(SYNC_STATUS, SYNC_STATUS);
		category.addAction(statusAction);
		mngr.addCategory(category);
	}
}

