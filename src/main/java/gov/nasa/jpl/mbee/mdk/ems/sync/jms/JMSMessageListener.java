package gov.nasa.jpl.mbee.mdk.ems.sync.jms;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import gov.nasa.jpl.mbee.mdk.MMSSyncPlugin;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.emf.EMFImporter;
import gov.nasa.jpl.mbee.mdk.ems.ExportUtility;
import gov.nasa.jpl.mbee.mdk.ems.ImportException;
import gov.nasa.jpl.mbee.mdk.ems.sync.delta.SyncElements;
import gov.nasa.jpl.mbee.mdk.ems.sync.status.SyncStatusConfigurator;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.lib.Changelog;
import gov.nasa.jpl.mbee.mdk.lib.MDUtils;
import gov.nasa.jpl.mbee.mdk.options.MDKOptionsGroup;
import gov.nasa.jpl.mbee.mdk.viewedit.ViewEditUtils;

import javax.jms.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class JMSMessageListener implements MessageListener, ExceptionListener {
    private static final Map<String, Changelog.ChangeType> CHANGE_MAPPING = new LinkedHashMap<>(4);

    static {
        CHANGE_MAPPING.put("addedElements", Changelog.ChangeType.CREATED);
        CHANGE_MAPPING.put("deletedElements", Changelog.ChangeType.DELETED);
        CHANGE_MAPPING.put("updatedElements", Changelog.ChangeType.UPDATED);
        CHANGE_MAPPING.put("movedElements", Changelog.ChangeType.UPDATED);
    }


    private volatile boolean isExceptionHandlerRunning;
    private int reconnectionAttempts = 0;

    private final Project project;
    private final Changelog<String, ObjectNode> inMemoryJMSChangelog = new Changelog<>();

    {
        if (MDUtils.isDeveloperMode()) {
            inMemoryJMSChangelog.setShouldLogChanges(true);
        }
    }

    private Message lastMessage;

    JMSMessageListener(Project project) {
        this.project = project;
    }

    @Override
    public void onMessage(Message message) {
        if (!MDKOptionsGroup.getMDKOptions().isChangeListenerEnabled()) {
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
            System.out.println("JMS TextMessage for " + ExportUtility.getProjectId(project) + " -" + System.lineSeparator() + text);
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
        // Changed elements are encapsulated in the "workspace2" JSONObject.
        JsonNode workspaceJsonNode = messageJsonNode.get("workspace2");
        JsonNode syncedJsonNode;
        if (workspaceJsonNode != null && workspaceJsonNode.isObject()) {
            JsonNode sourceJsonNode = workspaceJsonNode.get("source");
            if (sourceJsonNode != null && sourceJsonNode.isTextual() && sourceJsonNode.asText().equalsIgnoreCase("magicdraw")) {
                return;
            }
            for (Map.Entry<String, Changelog.ChangeType> entry : CHANGE_MAPPING.entrySet()) {
                JsonNode changeJsonNode = workspaceJsonNode.get(entry.getKey());
                if (changeJsonNode == null || !changeJsonNode.isArray()) {
                    continue;
                }
                for (JsonNode elementJsonNode : changeJsonNode) {
                    if (elementJsonNode == null || !elementJsonNode.isObject()) {
                        continue;
                    }
                    JsonNode sysmlIdJsonNode = elementJsonNode.get(MDKConstants.SYSML_ID_KEY);
                    if (sysmlIdJsonNode == null || !sysmlIdJsonNode.isTextual()) {
                        continue;
                    }
                    try {
                        if (EMFImporter.PreProcessor.SYSML_ID_VALIDATION.getFunction().apply((ObjectNode) elementJsonNode, project, false, project.getModel()) == null) {
                            continue;
                        }
                    } catch (ImportException | ReadOnlyElementException ignored) {
                        continue;
                    }
                    inMemoryJMSChangelog.addChange(sysmlIdJsonNode.asText(), (ObjectNode) elementJsonNode, entry.getValue());
                }
            }
            SyncStatusConfigurator.getSyncStatusAction().update();
        }
        else if ((syncedJsonNode = messageJsonNode.get("synced")) != null && syncedJsonNode.isObject()) {
            JsonNode sourceJsonNode = messageJsonNode.get("source");
            if (sourceJsonNode == null || !sourceJsonNode.isTextual() || !sourceJsonNode.asText().equals("magicdraw")) {
                return;
            }

            JsonNode senderJsonNode = messageJsonNode.get("sender");
            if (senderJsonNode != null && senderJsonNode.isTextual() && senderJsonNode.asText().equals(ViewEditUtils.getUsername())) {
                return;
            }

            Changelog<String, Void> syncedChangelog = SyncElements.buildChangelog((ObjectNode) syncedJsonNode);
            if (syncedChangelog.isEmpty()) {
                return;
            }

            for (Changelog.ChangeType changeType : Changelog.ChangeType.values()) {
                Map<String, ObjectNode> inMemoryJMSChanges = inMemoryJMSChangelog.get(changeType);
                syncedChangelog.get(changeType).keySet().forEach(inMemoryJMSChanges::remove);
            }
            int size = syncedChangelog.flattenedSize();
            if (MDUtils.isDeveloperMode()) {
                Application.getInstance().getGUILog().log("[INFO] Cleared " + size + " MMS element change" + (size != 1 ? "s" : "") + " as a result of another client syncing the model.");
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
        if (isExceptionHandlerRunning) {
            return;
        }
        isExceptionHandlerRunning = true;
        Application.getInstance().getGUILog().log("[WARNING] Lost connection with MMS. Please check your network configuration.");
        JMSSyncProjectEventListenerAdapter.getProjectMapping(project).setDisabled(true);
        while (shouldAttemptToReconnect()) {
            int delay = Math.min(600, (int) Math.pow(2, reconnectionAttempts++));
            Application.getInstance().getGUILog().log("[INFO] Attempting to reconnect to MMS in " + delay + " second" + (delay != 1 ? "s" : "") + ".");
            try {
                Thread.sleep(delay * 1000);
            } catch (InterruptedException ignored) {
            }
            if (shouldAttemptToReconnect()) {
                MMSSyncPlugin.getInstance().getJmsSyncProjectEventListenerAdapter().projectOpened(project);
            }
        }
        if (!JMSSyncProjectEventListenerAdapter.getProjectMapping(project).isDisabled()) {
            reconnectionAttempts = 0;
            Application.getInstance().getGUILog().log("[INFO] Successfully reconnected to MMS after dropped connection.");
        }
        else {
            Application.getInstance().getGUILog().log("[WARNING] Failed to reconnect to MMS after dropped connection. Please close and re-open the project to re-initiate.");
        }
        isExceptionHandlerRunning = false;
    }

    private boolean shouldAttemptToReconnect() {
        return JMSSyncProjectEventListenerAdapter.getProjectMapping(project).isDisabled() && StereotypesHelper.hasStereotype(project.getModel(), "ModelManagementSystem") && !project.isProjectClosed() && MDKOptionsGroup.getMDKOptions().isChangeListenerEnabled();
    }
}
