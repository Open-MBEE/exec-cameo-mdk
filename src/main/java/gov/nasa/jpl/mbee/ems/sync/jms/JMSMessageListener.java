package gov.nasa.jpl.mbee.ems.sync.jms;

import com.nomagic.magicdraw.core.Project;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.lib.Changelog;
import gov.nasa.jpl.mbee.options.MDKOptionsGroup;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by igomes on 7/13/16.
 */
public class JMSMessageListener implements MessageListener {
    private static final Map<String, Changelog.ChangeType> CHANGE_MAPPING = new LinkedHashMap<>(4);

    static {
        CHANGE_MAPPING.put("addedElements", Changelog.ChangeType.CREATED);
        CHANGE_MAPPING.put("deletedElements", Changelog.ChangeType.DELETED);
        CHANGE_MAPPING.put("updatedElements", Changelog.ChangeType.UPDATED);
        CHANGE_MAPPING.put("movedElements", Changelog.ChangeType.UPDATED);
    }

    private final Project project;
    private final Changelog<String, JSONObject> inMemoryJMSChangelog = new Changelog<>();

    public JMSMessageListener(Project project) {
        this.project = project;
    }

    @Override
    public void onMessage(Message message) {
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
        if (!((o = jsonObject.get("source")) instanceof String) || o.equals("magicdraw")) {
            return;
        }
        // Changed element are encapsulated in the "workspace2" JSONObject.
        o = jsonObject.get("workspace2");
        if (!(o instanceof JSONObject)) {
            return;
        }
        JSONObject workspace2 = (JSONObject) o;
        // Retrieve the changed elements: each type of change (updated, added, moved, deleted) will be returned as an JSONArray.
        for (Map.Entry<String, Changelog.ChangeType> entry : CHANGE_MAPPING.entrySet()) {
            if ((o = workspace2.get(entry.getKey())) instanceof JSONArray) {
                for (Object arrayObject : (JSONArray) o) {
                    if (arrayObject instanceof JSONObject) {
                        JSONObject elementJson = (JSONObject) arrayObject;
                        if ((o = elementJson.get("sysmlid")) instanceof String) {
                            inMemoryJMSChangelog.addChange((String) o, elementJson, entry.getValue());
                        }
                    }
                }
            }
        }
        for (Changelog.ChangeType changeType : Changelog.ChangeType.values()) {
            System.out.println(changeType.name() + ": " + inMemoryJMSChangelog.get(changeType).size());
        }
    }

    public Changelog<String, JSONObject> getInMemoryJMSChangelog() {
        return inMemoryJMSChangelog;
    }
}
