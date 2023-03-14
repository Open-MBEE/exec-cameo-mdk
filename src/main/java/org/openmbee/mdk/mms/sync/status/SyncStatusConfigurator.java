package org.openmbee.mdk.mms.sync.status;

import com.nomagic.actions.AMConfigurator;
import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import com.nomagic.actions.NMAction;
import org.openmbee.mdk.MDKPlugin;
import org.openmbee.mdk.mms.sync.status.actions.SyncStatusAction;

/**
 * Created by igomes on 8/16/16.
 */
public class SyncStatusConfigurator implements AMConfigurator {
    private static final String SYNC_STATUS_CATEGORY_NAME = "Sync Status";
    private static SyncStatusAction SYNC_STATUS_ACTION;

    @Override
    public void configure(ActionsManager actionsManager) {
        MDKPlugin.MAIN_TOOLBAR_ACTIONS_MANAGER = actionsManager;
        ActionsCategory category = actionsManager.getCategory(MDKPlugin.MAIN_TOOLBAR_CATEGORY_NAME);
        if (category == null) {
            category = new ActionsCategory(MDKPlugin.MAIN_TOOLBAR_CATEGORY_NAME, MDKPlugin.MAIN_TOOLBAR_CATEGORY_NAME);
            ActionsCategory lastActionsCategory = actionsManager.getLastActionsCategory();
            if (lastActionsCategory != null) {
                actionsManager.addCategory(lastActionsCategory, category, true);
            }
            else {
                actionsManager.addCategory(category);
            }
        }
        NMAction action = category.getAction(SYNC_STATUS_CATEGORY_NAME);
        ActionsCategory nestedCategory = null;
        if (action instanceof ActionsCategory) {
            nestedCategory = (ActionsCategory) action;
        }
        if (nestedCategory == null) {
            nestedCategory = new ActionsCategory(SYNC_STATUS_CATEGORY_NAME, SYNC_STATUS_CATEGORY_NAME);
            category.addAction(nestedCategory);
        }
        nestedCategory.addAction(getSyncStatusAction());
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
