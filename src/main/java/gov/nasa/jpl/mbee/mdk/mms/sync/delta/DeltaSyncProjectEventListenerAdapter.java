package gov.nasa.jpl.mbee.mdk.mms.sync.delta;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectEventListenerAdapter;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.mms.sync.jms.JMSMessageListener;
import gov.nasa.jpl.mbee.mdk.mms.sync.jms.JMSSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.mdk.mms.sync.local.LocalSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.mdk.mms.sync.local.LocalSyncTransactionCommitListener;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.util.Changelog;
import java.util.function.BiFunction;
import java.util.function.Function;
import gov.nasa.jpl.mbee.mdk.options.MDKOptionsGroup;

import java.util.HashMap;
import java.util.Map;

/*
 * This class is responsible for taking action when a project is opened.
 * This class does the following when instantiated:
 *   1. Create a transaction manager
 *   2. Create a TransactionCommitListener object
 *   3. Add the listener to the transaction manager object 
 *   4. Create a MMS topic and connection to that topic
 *   5. Store that connection so we keep track of the connections to MMS.
 *   
 */
public class DeltaSyncProjectEventListenerAdapter extends ProjectEventListenerAdapter {
    private static final Map<SyncElement.Type, Function<Project, Changelog<String, ?>>> CHANGELOG_FUNCTIONS = new HashMap<>(2);

    static {
        CHANGELOG_FUNCTIONS.put(SyncElement.Type.LOCAL, project -> {
            Changelog<String, Void> combinedPersistedChangelog = new Changelog<>();
            for (SyncElement syncElement : SyncElements.getAllOfType(project, SyncElement.Type.LOCAL)) {
                combinedPersistedChangelog = combinedPersistedChangelog.and(SyncElements.buildChangelog(syncElement));
            }
            LocalSyncTransactionCommitListener localSyncTransactionCommitListener = LocalSyncProjectEventListenerAdapter.getProjectMapping(project).getLocalSyncTransactionCommitListener();
            if (localSyncTransactionCommitListener == null) {
                return combinedPersistedChangelog;
            }
            return combinedPersistedChangelog.and(localSyncTransactionCommitListener.getInMemoryLocalChangelog(), new BiFunction<String, Element, Void>() {
                @Override
                public Void apply(String key, Element element) {
                    return null;
                }
            });
        });
        CHANGELOG_FUNCTIONS.put(SyncElement.Type.MMS, project -> {
            Changelog<String, Void> combinedPersistedChangelog = new Changelog<>();
            for (SyncElement syncElement : SyncElements.getAllOfType(project, SyncElement.Type.MMS)) {
                combinedPersistedChangelog = combinedPersistedChangelog.and(SyncElements.buildChangelog(syncElement));
            }
            JMSMessageListener jmsMessageListener = JMSSyncProjectEventListenerAdapter.getProjectMapping(project).getJmsMessageListener();
            if (jmsMessageListener == null) {
                return combinedPersistedChangelog;
            }
            return combinedPersistedChangelog.and(jmsMessageListener.getInMemoryJMSChangelog(), (key, objectNode) -> null);
        });
    }

    @Override
    public void projectPreSaved(Project project, boolean savedInServer) {
        boolean save = MDKOptionsGroup.getMDKOptions().isPersistChangelog();
        if (!save) {
            return;
        }
        if (!StereotypesHelper.hasStereotype(project.getModel(), "ModelManagementSystem")) {
            return;
        }
        persistChanges(project);
    }

    private static void persistChanges(Project project) {

        LocalSyncTransactionCommitListener listener = LocalSyncProjectEventListenerAdapter.getProjectMapping(project).getLocalSyncTransactionCommitListener();
        if (listener != null) {
            listener.setDisabled(true);
        }

        if (!SessionManager.getInstance().isSessionCreated(project)) {
            SessionManager.getInstance().createSession(project, "Delta Sync Changelog Persistence #2");
        }
        for (Map.Entry<SyncElement.Type, Function<Project, Changelog<String, ?>>> entry : CHANGELOG_FUNCTIONS.entrySet()) {
            Changelog<String, ?> changelog = entry.getValue().apply(project);
            try {
                SyncElements.setByType(project, entry.getKey(), JacksonUtils.getObjectMapper().writeValueAsString(SyncElements.buildJson(changelog)));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        if (SessionManager.getInstance().isSessionCreated(project)) {
            SessionManager.getInstance().closeSession();
        }

        if (listener != null) {
            listener.setDisabled(false);
        }
    }
}
