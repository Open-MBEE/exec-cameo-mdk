package gov.nasa.jpl.mbee.mdk.mms.sync.status.actions;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.ui.ProgressStatusRunner;
import gov.nasa.jpl.mbee.mdk.MDKPlugin;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.mms.sync.delta.SyncElement;
import gov.nasa.jpl.mbee.mdk.mms.sync.delta.SyncElements;
import gov.nasa.jpl.mbee.mdk.mms.sync.local.LocalDeltaProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.mdk.mms.sync.local.LocalDeltaTransactionCommitListener;
import gov.nasa.jpl.mbee.mdk.mms.sync.mms.MMSDeltaProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.mdk.mms.sync.status.ui.SyncStatusFrame;
import gov.nasa.jpl.mbee.mdk.util.Changelog;

import javax.annotation.CheckForNull;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.NumberFormat;

/**
 * Created by igomes on 8/16/16.
 */
public class SyncStatusAction extends MDAction {
    public static final String NAME = "Unsynced";

    private SyncStatusFrame syncStatusFrame;


    public SyncStatusAction() {
        super(NAME, NAME, null, null);
        update();
    }

    public void update() {
        final Changelog<String, Void> localPersistedChangelog = new Changelog<>(),
                mmsPersistedChangelog = new Changelog<>();

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

                inMemoryMmsCreatedCount = new int[]{0},
                inMemoryMmsUpdatedCount = new int[]{0},
                inMemoryMmsDeletedCount = new int[]{0},

                persistedMmsCreatedCount = new int[]{0},
                persistedMmsUpdatedCount = new int[]{0},
                persistedMmsDeletedCount = new int[]{0},

                totalMmsCreatedCount = new int[]{0},
                totalMmsUpdatedCount = new int[]{0},
                totalMmsDeletedCount = new int[]{0},

                totalInMemoryMmsChangedCount = new int[]{0},
                totalPersistedMmsChangedCount = new int[]{0},
                totalMmsChangedCount = new int[]{0},

                totalChangedCount = new int[]{0};
        final NumberFormat numberFormat = NumberFormat.getIntegerInstance();
        Project project = Application.getInstance().getProject();
        if (project != null && !project.isClosing() && !project.isProjectClosed()) {
            LocalDeltaTransactionCommitListener localDeltaTransactionCommitListener = LocalDeltaProjectEventListenerAdapter.getProjectMapping(project).getLocalDeltaTransactionCommitListener();
            if (localDeltaTransactionCommitListener != null) {
                inMemoryLocalCreatedCount[0] = localDeltaTransactionCommitListener.getInMemoryLocalChangelog().get(Changelog.ChangeType.CREATED).size();
                inMemoryLocalUpdatedCount[0] = localDeltaTransactionCommitListener.getInMemoryLocalChangelog().get(Changelog.ChangeType.UPDATED).size();
                inMemoryLocalDeletedCount[0] = localDeltaTransactionCommitListener.getInMemoryLocalChangelog().get(Changelog.ChangeType.DELETED).size();
            }
            totalInMemoryLocalChangedCount[0] = inMemoryLocalCreatedCount[0] + inMemoryLocalUpdatedCount[0] + inMemoryLocalDeletedCount[0];

            for (SyncElement localSyncElement : SyncElements.getAllByType(project, SyncElement.Type.LOCAL)) {
                SyncElements.buildChangelog(localPersistedChangelog, localSyncElement);
            }
            persistedLocalCreatedCount[0] += localPersistedChangelog.get(Changelog.ChangeType.CREATED).size();
            persistedLocalUpdatedCount[0] += localPersistedChangelog.get(Changelog.ChangeType.UPDATED).size();
            persistedLocalDeletedCount[0] += localPersistedChangelog.get(Changelog.ChangeType.DELETED).size();

            totalPersistedLocalChangedCount[0] = persistedLocalCreatedCount[0] + persistedLocalUpdatedCount[0] + persistedLocalDeletedCount[0];
            totalLocalChangedCount[0] = totalInMemoryLocalChangedCount[0] + totalPersistedLocalChangedCount[0];

            inMemoryMmsCreatedCount[0] = MMSDeltaProjectEventListenerAdapter.getProjectMapping(project).getInMemoryChangelog().get(Changelog.ChangeType.CREATED).size();
            inMemoryMmsUpdatedCount[0] = MMSDeltaProjectEventListenerAdapter.getProjectMapping(project).getInMemoryChangelog().get(Changelog.ChangeType.UPDATED).size();
            inMemoryMmsDeletedCount[0] = MMSDeltaProjectEventListenerAdapter.getProjectMapping(project).getInMemoryChangelog().get(Changelog.ChangeType.DELETED).size();
            totalInMemoryMmsChangedCount[0] = inMemoryMmsCreatedCount[0] + inMemoryMmsUpdatedCount[0] + inMemoryMmsDeletedCount[0];

            for (SyncElement mmsSyncElement : SyncElements.getAllByType(project, SyncElement.Type.MMS)) {
                SyncElements.buildChangelog(mmsPersistedChangelog, mmsSyncElement);
            }
            persistedMmsCreatedCount[0] += mmsPersistedChangelog.get(Changelog.ChangeType.CREATED).size();
            persistedMmsUpdatedCount[0] += mmsPersistedChangelog.get(Changelog.ChangeType.UPDATED).size();
            persistedMmsDeletedCount[0] += mmsPersistedChangelog.get(Changelog.ChangeType.DELETED).size();

            totalPersistedMmsChangedCount[0] = persistedMmsCreatedCount[0] + persistedMmsUpdatedCount[0] + persistedMmsDeletedCount[0];
            totalMmsChangedCount[0] = totalInMemoryMmsChangedCount[0] + totalPersistedMmsChangedCount[0];
        }

        totalChangedCount[0] = totalLocalChangedCount[0] + totalMmsChangedCount[0];
        SwingUtilities.invokeLater(() -> {
            setName(NAME + ": " + numberFormat.format(totalChangedCount[0]));
            MDKPlugin.updateMainToolbarCategory();
        });

        if (getSyncStatusFrame().isVisible()) {
            totalLocalCreatedCount[0] = inMemoryLocalCreatedCount[0] + persistedLocalCreatedCount[0];
            totalLocalUpdatedCount[0] = inMemoryLocalUpdatedCount[0] + persistedLocalUpdatedCount[0];
            totalLocalDeletedCount[0] = inMemoryLocalDeletedCount[0] + persistedLocalDeletedCount[0];

            totalMmsCreatedCount[0] = inMemoryMmsCreatedCount[0] + persistedMmsCreatedCount[0];
            totalMmsUpdatedCount[0] = inMemoryMmsUpdatedCount[0] + persistedMmsUpdatedCount[0];
            totalMmsDeletedCount[0] = inMemoryMmsDeletedCount[0] + persistedMmsDeletedCount[0];

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

                getSyncStatusFrame().getInMemoryMmsCreatedLabel().setText(numberFormat.format(inMemoryMmsCreatedCount[0]));
                getSyncStatusFrame().getInMemoryMmsUpdatedLabel().setText(numberFormat.format(inMemoryMmsUpdatedCount[0]));
                getSyncStatusFrame().getInMemoryMmsDeletedLabel().setText(numberFormat.format(inMemoryMmsDeletedCount[0]));

                getSyncStatusFrame().getPersistedMmsCreatedLabel().setText(numberFormat.format(persistedMmsCreatedCount[0]));
                getSyncStatusFrame().getPersistedMmsUpdatedLabel().setText(numberFormat.format(persistedMmsUpdatedCount[0]));
                getSyncStatusFrame().getPersistedMmsDeletedLabel().setText(numberFormat.format(persistedMmsDeletedCount[0]));

                getSyncStatusFrame().getTotalMmsCreatedLabel().setText(numberFormat.format(totalMmsCreatedCount[0]));
                getSyncStatusFrame().getTotalMmsUpdatedLabel().setText(numberFormat.format(totalMmsUpdatedCount[0]));
                getSyncStatusFrame().getTotalMmsDeletedLabel().setText(numberFormat.format(totalMmsDeletedCount[0]));

                getSyncStatusFrame().getTotalInMemoryMmsChangedLabel().setText(numberFormat.format(totalInMemoryMmsChangedCount[0]));
                getSyncStatusFrame().getTotalPersistedMmsChangedLabel().setText(numberFormat.format(totalPersistedMmsChangedCount[0]));
                getSyncStatusFrame().getTotalMmsChangedLabel().setText(numberFormat.format(totalMmsChangedCount[0]));
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
        if (!getSyncStatusFrame().isVisible()) {
            getSyncStatusFrame().setVisible(true);
        }
        else {
            getSyncStatusFrame().toFront();
        }
        Project project = Application.getInstance().getProject();
        if (project != null) {
            ProgressStatusRunner.runWithProgressStatus(progressStatus -> {
                progressStatus.setIndeterminate(true);
                try {
                    progressStatus.setDescription("Fetching MMS changes");
                    MMSDeltaProjectEventListenerAdapter.getProjectMapping(project).update();
                    progressStatus.setDescription("Updating table");
                    update();
                } catch (URISyntaxException | IOException | ServerException e) {
                    e.printStackTrace();
                }
            }, "Sync Status Update", false, 0);
        }
        getSyncStatusFrame().toFront();
    }

    @Override
    public void updateState() {
        super.updateState();
        Project project = Application.getInstance().getProject();
        setEnabled(project != null && project.isRemote());
    }
}
