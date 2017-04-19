package gov.nasa.jpl.mbee.mdk.mms.sync.status.actions;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import gov.nasa.jpl.mbee.mdk.MDKPlugin;
import gov.nasa.jpl.mbee.mdk.systems_reasoner.actions.SRAction;
import gov.nasa.jpl.mbee.mdk.mms.sync.delta.SyncElement;
import gov.nasa.jpl.mbee.mdk.mms.sync.delta.SyncElements;
import gov.nasa.jpl.mbee.mdk.mms.sync.jms.JMSMessageListener;
import gov.nasa.jpl.mbee.mdk.mms.sync.jms.JMSSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.mdk.mms.sync.local.LocalSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.mdk.mms.sync.local.LocalSyncTransactionCommitListener;
import gov.nasa.jpl.mbee.mdk.mms.sync.status.ui.SyncStatusFrame;
import gov.nasa.jpl.mbee.mdk.util.Changelog;

import javax.annotation.CheckForNull;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;

/**
 * Created by igomes on 8/16/16.
 */
public class SyncStatusAction extends SRAction {
    public static final String NAME = "Unsynced";

    private SyncStatusFrame syncStatusFrame;

    public SyncStatusAction() {
        super(NAME);
        update();
    }

    public void update() {
        final int[] inMemoryLocalCreatedCount = new int[]{0},
                inMemoryLocalUpdatedCount = new int[]{0},
                inMemoryLocalDeletedCount = new int[]{0},

                persistedLocalCreatedCount = new int[]{0},
                persistedLocalUpdatedCount = new int[]{0},
                persistedLocalDeletedCount = new int[]{0},

                totalLocalCreatedCount = new int[]{0},
                totalLocalUpdatedCount = new int[]{0},
                totalLocalDeletedCount = new int[]{0},

                totalInMemoryLocalChangedCount = new int[]{0},
                totalPersistedLocalChangedCount = new int[]{0},
                totalLocalChangedCount = new int[]{0},

                inMemoryJmsCreatedCount = new int[]{0},
                inMemoryJmsUpdatedCount = new int[]{0},
                inMemoryJmsDeletedCount = new int[]{0},

                persistedJmsCreatedCount = new int[]{0},
                persistedJmsUpdatedCount = new int[]{0},
                persistedJmsDeletedCount = new int[]{0},

                totalJmsCreatedCount = new int[]{0},
                totalJmsUpdatedCount = new int[]{0},
                totalJmsDeletedCount = new int[]{0},

                totalInMemoryJmsChangedCount = new int[]{0},
                totalPersistedJmsChangedCount = new int[]{0},
                totalJmsChangedCount = new int[]{0},

                totalChangedCount = new int[]{0};
        final NumberFormat numberFormat = NumberFormat.getIntegerInstance();
        Project project = Application.getInstance().getProject();
        if (project != null) {
            LocalSyncTransactionCommitListener localSyncTransactionCommitListener = LocalSyncProjectEventListenerAdapter.getProjectMapping(project).getLocalSyncTransactionCommitListener();
            if (localSyncTransactionCommitListener != null) {
                inMemoryLocalCreatedCount[0] = localSyncTransactionCommitListener.getInMemoryLocalChangelog().get(Changelog.ChangeType.CREATED).size();
                inMemoryLocalUpdatedCount[0] = localSyncTransactionCommitListener.getInMemoryLocalChangelog().get(Changelog.ChangeType.UPDATED).size();
                inMemoryLocalDeletedCount[0] = localSyncTransactionCommitListener.getInMemoryLocalChangelog().get(Changelog.ChangeType.DELETED).size();
            }
            totalInMemoryLocalChangedCount[0] = inMemoryLocalCreatedCount[0] + inMemoryLocalUpdatedCount[0] + inMemoryLocalDeletedCount[0];
            for (SyncElement syncElement : SyncElements.getAllOfType(project, SyncElement.Type.LOCAL)) {
                Changelog<String, Void> changelog = SyncElements.buildChangelog(syncElement);
                persistedLocalCreatedCount[0] += changelog.get(Changelog.ChangeType.CREATED).size();
                persistedLocalUpdatedCount[0] += changelog.get(Changelog.ChangeType.UPDATED).size();
                persistedLocalDeletedCount[0] += changelog.get(Changelog.ChangeType.DELETED).size();
            }
            totalPersistedLocalChangedCount[0] = persistedLocalCreatedCount[0] + persistedLocalUpdatedCount[0] + persistedLocalDeletedCount[0];
            totalLocalChangedCount[0] = totalInMemoryLocalChangedCount[0] + totalPersistedLocalChangedCount[0];

            JMSMessageListener jmsMessageListener = JMSSyncProjectEventListenerAdapter.getProjectMapping(project).getJmsMessageListener();
            if (jmsMessageListener != null) {
                inMemoryJmsCreatedCount[0] = jmsMessageListener.getInMemoryJMSChangelog().get(Changelog.ChangeType.CREATED).size();
                inMemoryJmsUpdatedCount[0] = jmsMessageListener.getInMemoryJMSChangelog().get(Changelog.ChangeType.UPDATED).size();
                inMemoryJmsDeletedCount[0] = jmsMessageListener.getInMemoryJMSChangelog().get(Changelog.ChangeType.DELETED).size();
            }
            totalInMemoryJmsChangedCount[0] = inMemoryJmsCreatedCount[0] + inMemoryJmsUpdatedCount[0] + inMemoryJmsDeletedCount[0];
            for (SyncElement syncElement : SyncElements.getAllOfType(project, SyncElement.Type.MMS)) {
                Changelog<String, Void> changelog = SyncElements.buildChangelog(syncElement);
                persistedJmsCreatedCount[0] += changelog.get(Changelog.ChangeType.CREATED).size();
                persistedJmsUpdatedCount[0] += changelog.get(Changelog.ChangeType.UPDATED).size();
                persistedJmsDeletedCount[0] += changelog.get(Changelog.ChangeType.DELETED).size();
            }
            totalPersistedJmsChangedCount[0] = persistedJmsCreatedCount[0] + persistedJmsUpdatedCount[0] + persistedJmsDeletedCount[0];
            totalJmsChangedCount[0] = totalInMemoryJmsChangedCount[0] + totalPersistedJmsChangedCount[0];
        }

        totalChangedCount[0] = totalLocalChangedCount[0] + totalJmsChangedCount[0];
        SwingUtilities.invokeLater(() -> {
            setName(NAME + ": " + numberFormat.format(totalChangedCount[0]));
            MDKPlugin.updateMainToolbarCategory();
        });

        if (getSyncStatusFrame().isVisible()) {
            totalLocalCreatedCount[0] = inMemoryLocalCreatedCount[0] + persistedLocalCreatedCount[0];
            totalLocalUpdatedCount[0] = inMemoryLocalUpdatedCount[0] + persistedLocalUpdatedCount[0];
            totalLocalDeletedCount[0] = inMemoryLocalDeletedCount[0] + persistedLocalDeletedCount[0];

            totalJmsCreatedCount[0] = inMemoryJmsCreatedCount[0] + persistedJmsCreatedCount[0];
            totalJmsUpdatedCount[0] = inMemoryJmsUpdatedCount[0] + persistedJmsUpdatedCount[0];
            totalJmsDeletedCount[0] = inMemoryJmsDeletedCount[0] + persistedJmsDeletedCount[0];

            SwingUtilities.invokeLater(() -> {
                getSyncStatusFrame().getInMemoryLocalCreatedLabel().setText(numberFormat.format(inMemoryLocalCreatedCount[0]));
                getSyncStatusFrame().getInMemoryLocalUpdatedLabel().setText(numberFormat.format(inMemoryLocalUpdatedCount[0]));
                getSyncStatusFrame().getInMemoryLocalDeletedLabel().setText(numberFormat.format(inMemoryLocalDeletedCount[0]));

                getSyncStatusFrame().getPersistedLocalCreatedLabel().setText(numberFormat.format(persistedLocalCreatedCount[0]));
                getSyncStatusFrame().getPersistedLocalUpdatedLabel().setText(numberFormat.format(persistedLocalUpdatedCount[0]));
                getSyncStatusFrame().getPersistedLocalDeletedLabel().setText(numberFormat.format(persistedLocalDeletedCount[0]));

                getSyncStatusFrame().getTotalLocalCreatedLabel().setText(numberFormat.format(totalLocalCreatedCount[0]));
                getSyncStatusFrame().getTotalLocalUpdatedLabel().setText(numberFormat.format(totalLocalUpdatedCount[0]));
                getSyncStatusFrame().getTotalLocalDeletedLabel().setText(numberFormat.format(totalLocalDeletedCount[0]));

                getSyncStatusFrame().getTotalInMemoryLocalChangedLabel().setText(numberFormat.format(totalInMemoryLocalChangedCount[0]));
                getSyncStatusFrame().getTotalPersistedLocalChangedLabel().setText(numberFormat.format(totalPersistedLocalChangedCount[0]));
                getSyncStatusFrame().getTotalLocalChangedLabel().setText(numberFormat.format(totalLocalChangedCount[0]));

                getSyncStatusFrame().getInMemoryJmsCreatedLabel().setText(numberFormat.format(inMemoryJmsCreatedCount[0]));
                getSyncStatusFrame().getInMemoryJmsUpdatedLabel().setText(numberFormat.format(inMemoryJmsUpdatedCount[0]));
                getSyncStatusFrame().getInMemoryJmsDeletedLabel().setText(numberFormat.format(inMemoryJmsDeletedCount[0]));

                getSyncStatusFrame().getPersistedJmsCreatedLabel().setText(numberFormat.format(persistedJmsCreatedCount[0]));
                getSyncStatusFrame().getPersistedJmsUpdatedLabel().setText(numberFormat.format(persistedJmsUpdatedCount[0]));
                getSyncStatusFrame().getPersistedJmsDeletedLabel().setText(numberFormat.format(persistedJmsDeletedCount[0]));

                getSyncStatusFrame().getTotalJmsCreatedLabel().setText(numberFormat.format(totalJmsCreatedCount[0]));
                getSyncStatusFrame().getTotalJmsUpdatedLabel().setText(numberFormat.format(totalJmsUpdatedCount[0]));
                getSyncStatusFrame().getTotalJmsDeletedLabel().setText(numberFormat.format(totalJmsDeletedCount[0]));

                getSyncStatusFrame().getTotalInMemoryJmsChangedLabel().setText(numberFormat.format(totalInMemoryJmsChangedCount[0]));
                getSyncStatusFrame().getTotalPersistedJmsChangedLabel().setText(numberFormat.format(totalPersistedJmsChangedCount[0]));
                getSyncStatusFrame().getTotalJmsChangedLabel().setText(numberFormat.format(totalJmsChangedCount[0]));
            });
        }
    }

    public SyncStatusFrame getSyncStatusFrame() {
        if (syncStatusFrame == null) {
            syncStatusFrame = new SyncStatusFrame();
        }
        return syncStatusFrame;
    }

    @Override
    public void actionPerformed(@CheckForNull ActionEvent actionEvent) {
        getSyncStatusFrame().setVisible(!getSyncStatusFrame().isVisible());
        if (getSyncStatusFrame().isVisible()) {
            update();
        }
    }

    @Override
    public void updateState() {
        super.updateState();
        Project project = Application.getInstance().getProject();
        setEnabled(project != null && project.isRemote());
    }
}
