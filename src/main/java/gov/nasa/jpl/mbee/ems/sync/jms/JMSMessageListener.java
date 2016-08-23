package gov.nasa.jpl.mbee.ems.sync.jms;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import gov.nasa.jpl.mbee.MMSSyncPlugin;
import gov.nasa.jpl.mbee.api.docgen.presentation_elements.PresentationElementEnum;
import gov.nasa.jpl.mbee.api.docgen.presentation_elements.properties.PresentationElementPropertyEnum;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.sync.delta.SyncElements;
import gov.nasa.jpl.mbee.ems.sync.status.SyncStatusConfigurator;
import gov.nasa.jpl.mbee.ems.validation.ModelValidator;
import gov.nasa.jpl.mbee.lib.Changelog;
import gov.nasa.jpl.mbee.lib.MDUtils;
import gov.nasa.jpl.mbee.options.MDKOptionsGroup;
import gov.nasa.jpl.mbee.viewedit.ViewEditUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.jms.*;
import java.util.*;

public class JMSMessageListener implements MessageListener, ExceptionListener {
    private static final Map<String, Changelog.ChangeType> CHANGE_MAPPING = new LinkedHashMap<>(4);

    private volatile boolean isExceptionHandlerRunning;
    private int reconnectionAttempts = 0;

    static {
        CHANGE_MAPPING.put("addedElements", Changelog.ChangeType.CREATED);
        CHANGE_MAPPING.put("deletedElements", Changelog.ChangeType.DELETED);
        CHANGE_MAPPING.put("updatedElements", Changelog.ChangeType.UPDATED);
        CHANGE_MAPPING.put("movedElements", Changelog.ChangeType.UPDATED);
    }

    private final Project project;
    private final Changelog<String, JSONObject> inMemoryJMSChangelog = new Changelog<>();

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
        Object o = JSONValue.parse(text);
        if (!(o instanceof JSONObject)) {
            return;
        }
        JSONObject jsonObject = (JSONObject) o;
        // Changed elements are encapsulated in the "workspace2" JSONObject.
        if ((o = jsonObject.get("workspace2")) instanceof JSONObject) {
            JSONObject workspace2 = (JSONObject) o;

            if (((o = jsonObject.get("source")) instanceof String) && ((String) o).startsWith("magicdraw")) {
                return;
            }

            // Retrieve the changed elements: each type of change (updated, added, moved, deleted) will be returned as an JSONArray.
            for (Map.Entry<String, Changelog.ChangeType> entry : CHANGE_MAPPING.entrySet()) {
                if (!((o = workspace2.get(entry.getKey())) instanceof JSONArray)) {
                    continue;
                }
                for (Object arrayObject : (JSONArray) o) {
                    if (!(arrayObject instanceof JSONObject)) {
                        continue;
                    }
                    JSONObject elementJson = (JSONObject) arrayObject;
                    if (!((o = elementJson.get("sysmlid")) instanceof String)) {
                        continue;
                    }
                    String sysmlid = (String) o;
                    if (sysmlid.startsWith("PROJECT")) {
                        continue;
                    }
                    if (sysmlid.startsWith(ModelValidator.HIDDEN_ID_PREFIX)) {
                        continue;
                    }
                    if ((o = elementJson.get("specialization")) instanceof JSONObject) {
                        JSONObject specialization = (JSONObject) o;
                        Object o2;
                        if ((o2 = specialization.get("classifier")) instanceof JSONArray) {
                            boolean isPresentationElement = false;
                            for (Object c : (JSONArray) o2) {
                                if (c instanceof String) {
                                    for (PresentationElementEnum presentationElementEnum : PresentationElementEnum.values()) {
                                        if (c.equals(presentationElementEnum.get().getID())) {
                                            isPresentationElement = true;
                                            break;
                                        }
                                    }
                                }
                                if (isPresentationElement) {
                                    break;
                                }
                            }
                            if (isPresentationElement) {
                                continue;

                            }
                        }
                        if ((o2 = specialization.get("propertyType")) instanceof JSONArray) {
                            boolean isPresentationElementProperty = false;
                            for (Object c : (JSONArray) o2) {
                                if (c instanceof String) {
                                    for (PresentationElementPropertyEnum presentationElementPropertyEnum : PresentationElementPropertyEnum.values()) {
                                        if (c.equals(presentationElementPropertyEnum.get().getID())) {
                                            isPresentationElementProperty = true;
                                            break;
                                        }
                                    }
                                }
                                if (isPresentationElementProperty) {
                                    break;
                                }
                            }
                            if (isPresentationElementProperty) {
                                continue;
                            }
                        }
                    }
                    inMemoryJMSChangelog.addChange(sysmlid, elementJson, entry.getValue());
                }
            }
            SyncStatusConfigurator.getSyncStatusAction().update();
        }
        else if ((o = jsonObject.get("synced")) instanceof JSONObject) {
            JSONObject synced = (JSONObject) o;

            if (!((o = jsonObject.get("source")) instanceof String) || !o.equals("magicdraw")) {
                return;
            }

            String username = ViewEditUtils.getUsername();
            if (username != null && username.equals(jsonObject.get("sender"))) {
                return;
            }

            Changelog<String, Void> syncedChangelog = SyncElements.buildChangelog(synced);
            if (syncedChangelog.isEmpty()) {
                return;
            }

            for (Changelog.ChangeType changeType : Changelog.ChangeType.values()) {
                Map<String, JSONObject> inMemoryJMSChanges = inMemoryJMSChangelog.get(changeType);
                for (String key : syncedChangelog.get(changeType).keySet()) {
                    inMemoryJMSChanges.remove(key);
                }
            }
            int size = syncedChangelog.flattenedSize();
            if (MDUtils.isDeveloperMode()) {
                Application.getInstance().getGUILog().log("[INFO] Cleared " + size + " MMS element change" + (size != 1 ? "s" : "") + " as a result of another client syncing the model.");
            }
            SyncStatusConfigurator.getSyncStatusAction().update();
        }
    }

    public Changelog<String, JSONObject> getInMemoryJMSChangelog() {
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
