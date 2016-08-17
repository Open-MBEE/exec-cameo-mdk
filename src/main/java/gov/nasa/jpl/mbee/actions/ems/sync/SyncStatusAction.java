package gov.nasa.jpl.mbee.actions.ems.sync;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import gov.nasa.jpl.mbee.ems.sync.status.ui.SyncStatusFrame;
import gov.nasa.jpl.mbee.actions.systemsreasoner.SRAction;
import gov.nasa.jpl.mbee.ems.sync.delta.SyncElement;
import gov.nasa.jpl.mbee.ems.sync.delta.SyncElements;
import gov.nasa.jpl.mbee.ems.sync.jms.JMSMessageListener;
import gov.nasa.jpl.mbee.ems.sync.jms.JMSSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.ems.sync.local.LocalSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.ems.sync.local.LocalSyncTransactionCommitListener;
import gov.nasa.jpl.mbee.lib.Changelog;

import javax.annotation.CheckForNull;
import java.awt.event.ActionEvent;

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
        int inMemoryLocalCreatedCount = 0,
                inMemoryLocalUpdatedCount = 0,
                inMemoryLocalDeletedCount = 0,

                persistedLocalCreatedCount = 0,
                persistedLocalUpdatedCount = 0,
                persistedLocalDeletedCount = 0,

                totalLocalCreatedCount,
                totalLocalUpdatedCount,
                totalLocalDeletedCount,

                totalInMemoryLocalChangedCount = 0,
                totalPersistedLocalChangedCount = 0,
                totalLocalChangedCount = 0,

                inMemoryJmsCreatedCount = 0,
                inMemoryJmsUpdatedCount = 0,
                inMemoryJmsDeletedCount = 0,

                persistedJmsCreatedCount = 0,
                persistedJmsUpdatedCount = 0,
                persistedJmsDeletedCount = 0,

                totalJmsCreatedCount,
                totalJmsUpdatedCount,
                totalJmsDeletedCount,

                totalInMemoryJmsChangedCount = 0,
                totalPersistedJmsChangedCount = 0,
                totalJmsChangedCount = 0,

                totalChangedCount;

        Project project = Application.getInstance().getProject();
        if (project != null) {
            LocalSyncTransactionCommitListener localSyncTransactionCommitListener = LocalSyncProjectEventListenerAdapter.getProjectMapping(project).getLocalSyncTransactionCommitListener();
            if (localSyncTransactionCommitListener != null) {
                inMemoryLocalCreatedCount = localSyncTransactionCommitListener.getInMemoryLocalChangelog().get(Changelog.ChangeType.CREATED).size();
                inMemoryLocalUpdatedCount = localSyncTransactionCommitListener.getInMemoryLocalChangelog().get(Changelog.ChangeType.UPDATED).size();
                inMemoryLocalDeletedCount = localSyncTransactionCommitListener.getInMemoryLocalChangelog().get(Changelog.ChangeType.DELETED).size();
            }
            totalInMemoryLocalChangedCount = inMemoryLocalCreatedCount + inMemoryLocalUpdatedCount + inMemoryLocalDeletedCount;
            for (SyncElement syncElement : SyncElements.getAllOfType(project, SyncElement.Type.UPDATE)) {
                Changelog<String, Void> changelog = SyncElements.buildChangelog(syncElement);
                persistedLocalCreatedCount += changelog.get(Changelog.ChangeType.CREATED).size();
                persistedLocalUpdatedCount += changelog.get(Changelog.ChangeType.UPDATED).size();
                persistedLocalDeletedCount += changelog.get(Changelog.ChangeType.DELETED).size();
            }
            totalPersistedLocalChangedCount = persistedLocalCreatedCount + persistedLocalUpdatedCount + persistedLocalDeletedCount;
            totalLocalChangedCount = totalInMemoryLocalChangedCount + totalPersistedLocalChangedCount;

            JMSMessageListener jmsMessageListener = JMSSyncProjectEventListenerAdapter.getProjectMapping(project).getJmsMessageListener();
            if (jmsMessageListener != null) {
                inMemoryJmsCreatedCount = jmsMessageListener.getInMemoryJMSChangelog().get(Changelog.ChangeType.CREATED).size();
                inMemoryJmsUpdatedCount = jmsMessageListener.getInMemoryJMSChangelog().get(Changelog.ChangeType.UPDATED).size();
                inMemoryJmsDeletedCount = jmsMessageListener.getInMemoryJMSChangelog().get(Changelog.ChangeType.DELETED).size();
            }
            totalInMemoryJmsChangedCount = inMemoryJmsCreatedCount + inMemoryJmsUpdatedCount + inMemoryJmsDeletedCount;
            for (SyncElement syncElement : SyncElements.getAllOfType(project, SyncElement.Type.JMS)) {
                Changelog<String, Void> changelog = SyncElements.buildChangelog(syncElement);
                persistedJmsCreatedCount += changelog.get(Changelog.ChangeType.CREATED).size();
                persistedJmsUpdatedCount += changelog.get(Changelog.ChangeType.UPDATED).size();
                persistedJmsDeletedCount += changelog.get(Changelog.ChangeType.DELETED).size();
            }
            totalPersistedJmsChangedCount = persistedJmsCreatedCount + persistedJmsUpdatedCount + persistedJmsDeletedCount;
            totalJmsChangedCount = totalInMemoryJmsChangedCount + totalPersistedJmsChangedCount;
        }

        totalChangedCount = totalLocalChangedCount + totalJmsChangedCount;
        setName(NAME + ": " + totalChangedCount);

        if (getSyncStatusFrame().isVisible()) {
            totalLocalCreatedCount = inMemoryLocalCreatedCount + persistedLocalCreatedCount;
            totalLocalUpdatedCount = inMemoryLocalUpdatedCount + persistedLocalUpdatedCount;
            totalLocalDeletedCount = inMemoryLocalDeletedCount + persistedLocalDeletedCount;

            totalJmsCreatedCount = inMemoryJmsCreatedCount + persistedJmsCreatedCount;
            totalJmsUpdatedCount = inMemoryJmsUpdatedCount + persistedJmsUpdatedCount;
            totalJmsDeletedCount = inMemoryJmsDeletedCount + persistedJmsDeletedCount;

            getSyncStatusFrame().getInMemoryLocalCreatedLabel().setText(Integer.toString(inMemoryLocalCreatedCount));
            getSyncStatusFrame().getInMemoryLocalUpdatedLabel().setText(Integer.toString(inMemoryLocalUpdatedCount));
            getSyncStatusFrame().getInMemoryLocalDeletedLabel().setText(Integer.toString(inMemoryLocalDeletedCount));

            getSyncStatusFrame().getPersistedLocalCreatedLabel().setText(Integer.toString(persistedLocalCreatedCount));
            getSyncStatusFrame().getPersistedLocalUpdatedLabel().setText(Integer.toString(persistedLocalUpdatedCount));
            getSyncStatusFrame().getPersistedLocalDeletedLabel().setText(Integer.toString(persistedLocalDeletedCount));

            getSyncStatusFrame().getTotalLocalCreatedLabel().setText(Integer.toString(totalLocalCreatedCount));
            getSyncStatusFrame().getTotalLocalUpdatedLabel().setText(Integer.toString(totalLocalUpdatedCount));
            getSyncStatusFrame().getTotalLocalDeletedLabel().setText(Integer.toString(totalLocalDeletedCount));

            getSyncStatusFrame().getTotalInMemoryLocalChangedLabel().setText(Integer.toString(totalInMemoryLocalChangedCount));
            getSyncStatusFrame().getTotalPersistedLocalChangedLabel().setText(Integer.toString(totalPersistedLocalChangedCount));
            getSyncStatusFrame().getTotalLocalChangedLabel().setText(Integer.toString(totalLocalChangedCount));

            getSyncStatusFrame().getInMemoryJmsCreatedLabel().setText(Integer.toString(inMemoryJmsCreatedCount));
            getSyncStatusFrame().getInMemoryJmsUpdatedLabel().setText(Integer.toString(inMemoryJmsUpdatedCount));
            getSyncStatusFrame().getInMemoryJmsDeletedLabel().setText(Integer.toString(inMemoryJmsDeletedCount));

            getSyncStatusFrame().getPersistedJmsCreatedLabel().setText(Integer.toString(persistedJmsCreatedCount));
            getSyncStatusFrame().getPersistedJmsUpdatedLabel().setText(Integer.toString(persistedJmsUpdatedCount));
            getSyncStatusFrame().getPersistedJmsDeletedLabel().setText(Integer.toString(persistedJmsDeletedCount));

            getSyncStatusFrame().getTotalJmsCreatedLabel().setText(Integer.toString(totalJmsCreatedCount));
            getSyncStatusFrame().getTotalJmsUpdatedLabel().setText(Integer.toString(totalJmsUpdatedCount));
            getSyncStatusFrame().getTotalJmsDeletedLabel().setText(Integer.toString(totalJmsDeletedCount));

            getSyncStatusFrame().getTotalInMemoryJmsChangedLabel().setText(Integer.toString(totalInMemoryJmsChangedCount));
            getSyncStatusFrame().getTotalPersistedJmsChangedLabel().setText(Integer.toString(totalPersistedJmsChangedCount));
            getSyncStatusFrame().getTotalJmsChangedLabel().setText(Integer.toString(totalJmsChangedCount));
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
        setEnabled(Application.getInstance().getProject() != null);
    }
}
