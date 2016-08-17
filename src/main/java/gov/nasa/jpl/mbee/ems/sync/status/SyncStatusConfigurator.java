package gov.nasa.jpl.mbee.ems.sync.status;

import com.nomagic.actions.AMConfigurator;
import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import gov.nasa.jpl.mbee.actions.ems.sync.SyncStatusAction;

/**
 * Created by igomes on 8/16/16.
 */
public class SyncStatusConfigurator implements AMConfigurator {
    private static final String SYNC_STATUS = "Sync Status";
    private static SyncStatusAction SYNC_STATUS_ACTION;

    @Override
    public void configure(ActionsManager actionsManager) {
        final ActionsCategory category = new ActionsCategory(SYNC_STATUS, SYNC_STATUS);
        category.addAction(getSyncStatusAction());
        actionsManager.addCategory(category);
    }

    @Override
    public int getPriority() {
        return AMConfigurator.MEDIUM_PRIORITY;
    }

    public static SyncStatusAction getSyncStatusAction() {
        if (SYNC_STATUS_ACTION == null) {
            SYNC_STATUS_ACTION = new SyncStatusAction();
        }
        return SYNC_STATUS_ACTION;
    }
}
