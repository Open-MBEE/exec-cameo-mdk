package gov.nasa.jpl.mbee.ems.sync.status;

import com.nomagic.actions.AMConfigurator;
import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import gov.nasa.jpl.mbee.DocGenPlugin;
import gov.nasa.jpl.mbee.actions.ems.sync.SyncStatusAction;

/**
 * Created by igomes on 8/16/16.
 */
public class SyncStatusConfigurator implements AMConfigurator {
    private static SyncStatusAction SYNC_STATUS_ACTION;

    @Override
    public void configure(ActionsManager actionsManager) {
        DocGenPlugin.MAIN_TOOLBAR_ACTIONS_MANAGER = actionsManager;
        ActionsCategory category = actionsManager.getCategory(DocGenPlugin.MAIN_TOOLBAR_CATEGORY_NAME);
        if (category == null) {
            category = new ActionsCategory(DocGenPlugin.MAIN_TOOLBAR_CATEGORY_NAME, DocGenPlugin.MAIN_TOOLBAR_CATEGORY_NAME);
            ActionsCategory lastActionsCategory = actionsManager.getLastActionsCategory();
            if (lastActionsCategory != null) {
                actionsManager.addCategory(lastActionsCategory, category, true);
            }
            else {
                actionsManager.addCategory(category);
            }
        }
        category.addAction(getSyncStatusAction());
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
