package gov.nasa.jpl.mbee.ems.sync;

import com.nomagic.actions.AMConfigurator;
import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;

public class AutosyncStatusConfigurator implements AMConfigurator {
	
	private static final AutosyncStatusAction autosyncStatusAction = new AutosyncStatusAction();
	
	public static final String SYNC_STATUS = "Autosync Status";

	public static AutosyncStatusAction getAutosyncStatusAction() {
		return autosyncStatusAction;
	}
	
	@Override
	public int getPriority() {
		return AMConfigurator.MEDIUM_PRIORITY;
	}

	@Override
	public void configure(ActionsManager mngr) {
		final ActionsCategory category = new ActionsCategory(SYNC_STATUS, SYNC_STATUS);
		category.addAction(autosyncStatusAction);
		mngr.addCategory(category);
	}
}

