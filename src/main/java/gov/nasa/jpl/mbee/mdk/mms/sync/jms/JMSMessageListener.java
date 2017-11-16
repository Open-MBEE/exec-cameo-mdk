package gov.nasa.jpl.mbee.mdk.mms.sync.jms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.emf.EMFImporter;
import gov.nasa.jpl.mbee.mdk.json.ImportException;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.mms.actions.MMSLogoutAction;
import gov.nasa.jpl.mbee.mdk.mms.sync.delta.SyncElements;
import gov.nasa.jpl.mbee.mdk.mms.sync.status.SyncStatusConfigurator;
import gov.nasa.jpl.mbee.mdk.options.MDKOptionsGroup;
import gov.nasa.jpl.mbee.mdk.util.Changelog;
import gov.nasa.jpl.mbee.mdk.util.MDUtils;
import gov.nasa.jpl.mbee.mdk.util.TicketUtils;

import javax.jms.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class JMSMessageListener implements MessageListener, ExceptionListener {
    private static final Map<String, Changelog.ChangeType> CHANGE_MAPPING = new LinkedHashMap<>(4);

    static {
        CHANGE_MAPPING.put("addedElements", Changelog.ChangeType.CREATED);
        CHANGE_MAPPING.put("deletedElements", Changelog.ChangeType.DELETED);
        CHANGE_MAPPING.put("movedElements", Changelog.ChangeType.UPDATED);
        CHANGE_MAPPING.put("updatedElements", Changelog.ChangeType.UPDATED);
    }

    private final AtomicBoolean disabled = new AtomicBoolean(true);
    private final AtomicBoolean exceptionHandlerRunning = new AtomicBoolean();
    private int reconnectionAttempts = 0;

    private final Project project;
    private final Changelog<String, ObjectNode> inMemoryJMSChangelog = new Changelog<>();

    {
        if (MDUtils.isDeveloperMode()) {
            inMemoryJMSChangelog.setShouldLogChanges(true);
        }
    }

    public void setDisabled(boolean disabled) {
        synchronized (this.disabled) {
            this.disabled.set(disabled);
        }
    }

    public boolean isDisabled() {
        synchronized (this.disabled) {
            return (disabled.get() || !MDKOptionsGroup.getMDKOptions().isChangeListenerEnabled());
        }
    }

    private Message lastMessage;

    public boolean isExceptionHandlerRunning() {
        synchronized (this.exceptionHandlerRunning) {
            return exceptionHandlerRunning.get();
        }
    }

    JMSMessageListener(Project project) {
        this.project = project;
    }

    @Override
    public void onMessage(Message message) {
        if (isDisabled()) {
            return;
        }
        lastMessage = message;
        if (!(message instanceof TextMessage)) {
            return;
        }
        final String text;
        try {
            text = ((TextMessage) message).getText();
        } catch (JMSException e) {
            e.printStackTrace();
            return;
        }
        if (MDKOptionsGroup.getMDKOptions().isLogJson()) {
            System.out.println("MMS TextMessage for " + Converters.getIProjectToIdConverter().apply(project.getPrimaryProject()) + " -" + System.lineSeparator() + text);
        }
        JsonNode messageJsonNode;
        try {
            messageJsonNode = JacksonUtils.getObjectMapper().readTree(text);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (!messageJsonNode.isObject()) {
            return;
        }

        JsonNode refsJsonNode = messageJsonNode.get("refs"),
                syncedJsonNode = messageJsonNode.get("synced"),
                sourceJsonNode = messageJsonNode.get("source"),
                senderJsonNode = messageJsonNode.get("sender");

        if (refsJsonNode != null && refsJsonNode.isObject()) {
            if (sourceJsonNode != null && sourceJsonNode.isTextual() && sourceJsonNode.asText().startsWith("magicdraw")) {
                return;
            }
            for (Map.Entry<String, Changelog.ChangeType> entry : CHANGE_MAPPING.entrySet()) {
                JsonNode changeJsonNode = refsJsonNode.get(entry.getKey());
                if (changeJsonNode == null || !changeJsonNode.isArray()) {
                    continue;
                }
                for (JsonNode sysmlIdJsonNode : changeJsonNode) {
                    if (!sysmlIdJsonNode.isTextual() || sysmlIdJsonNode.asText().isEmpty()) {
                        continue;
                    }
                    String id = sysmlIdJsonNode.asText();
                    try {
                        ObjectNode elementJsonNode = JacksonUtils.getObjectMapper().createObjectNode();
                        elementJsonNode.put(MDKConstants.ID_KEY, id);
                        if (EMFImporter.PreProcessor.SYSML_ID_VALIDATION.getFunction().apply(elementJsonNode, project, false, project.getPrimaryModel()) == null) {
                            continue;
                        }
                    } catch (ImportException ignored) {
                        continue;
                    }
                    inMemoryJMSChangelog.addChange(id, null, entry.getValue());
                }
                SyncStatusConfigurator.getSyncStatusAction().update();
            }
        }
        else if (syncedJsonNode != null && syncedJsonNode.isObject()) {
            if (senderJsonNode != null && senderJsonNode.isTextual() && senderJsonNode.asText().equals(TicketUtils.getUsername(project))) {
                return;
            }
            Changelog<String, Void> syncedChangelog = SyncElements.buildChangelog((ObjectNode) syncedJsonNode);
            if (syncedChangelog.isEmpty()) {
                return;
            }
            Collection<String> ignoredIds;
            if (project.isRemote()) {
                ignoredIds = new HashSet<>();
                Collection<Element> locks = EsiUtils.getLockService(project).getLockedByMe();
                for (Element lock : locks) {
                    ignoredIds.add(lock.getLocalID());
                }
            }
            else {
                ignoredIds = Collections.emptyList();
            }
            for (Changelog.ChangeType changeType : Changelog.ChangeType.values()) {
                Map<String, ObjectNode> inMemoryJMSChanges = inMemoryJMSChangelog.get(changeType);
                Set<String> keys = syncedChangelog.get(changeType).keySet();
                keys.removeAll(ignoredIds);
                keys.forEach(inMemoryJMSChanges::remove);
            }
            int size = syncedChangelog.flattenedSize();
            if (MDUtils.isDeveloperMode()) {
                Application.getInstance().getGUILog().log("[INFO] " + project.getName() + " - Cleared up to " + size + " MMS element change" + (size != 1 ? "s" : "") + " as a result of another client syncing the model.");
            }
            SyncStatusConfigurator.getSyncStatusAction().update();
        }
    }

    public Changelog<String, ObjectNode> getInMemoryJMSChangelog() {
        return inMemoryJMSChangelog;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    @Override
    public void onException(JMSException exception) {
        if (exceptionHandlerRunning.get()) {
            return;
        }
        exceptionHandlerRunning.set(true);
        try {
            Application.getInstance().getGUILog().log("[WARNING] " + project.getName() + " - Lost connection with MMS. Please check your network configuration.");
            JMSSyncProjectEventListenerAdapter.getProjectMapping(project).getJmsMessageListener().setDisabled(true);
            while (shouldAttemptToReconnect()) {
                int delay = Math.min(600, (int) Math.pow(2, reconnectionAttempts++));
                Application.getInstance().getGUILog().log("[INFO] " + project.getName() + " - Attempting to reconnect to MMS in " + delay + " second" + (delay != 1 ? "s" : "") + ".");
                try {
                    Thread.sleep(delay * 1000);
                } catch (InterruptedException ignored) {
                }
                if (!exceptionHandlerRunning.get()) {
                    break;
                }
                if (shouldAttemptToReconnect()) {
                    JMSSyncProjectEventListenerAdapter.closeJMS(project);
                    JMSSyncProjectEventListenerAdapter.initializeJMS(project);
                }
            }
        } catch (RuntimeException e) {
            Application.getInstance().getGUILog().log("[WARNING] " + project.getName() + " - An unexpected error occurred. Reason: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (!JMSSyncProjectEventListenerAdapter.getProjectMapping(project).getJmsMessageListener().isDisabled()) {
                reconnectionAttempts = 0;
                Application.getInstance().getGUILog().log("[INFO] " + project.getName() + " - Successfully reconnected to MMS after dropped connection.");
            }
            else {
                Application.getInstance().getGUILog().log("[WARNING] " + project.getName() + " - Failed to reconnect to MMS after dropped connection. Please manually login to MMS, or close and re-open the project, to re-initiate.");
                MMSLogoutAction.logoutAction(project);
            }

            reconnectionAttempts = 0;
            exceptionHandlerRunning.set(false);
        }
    }

    private boolean shouldAttemptToReconnect() {
        return !project.isProjectClosed() && TicketUtils.isTicketSet(project)
                && JMSSyncProjectEventListenerAdapter.shouldEnableJMS(project)
                && JMSSyncProjectEventListenerAdapter.getProjectMapping(project).getJmsMessageListener().isDisabled();
    }
}
