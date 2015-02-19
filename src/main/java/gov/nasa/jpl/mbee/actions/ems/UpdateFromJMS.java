package gov.nasa.jpl.mbee.actions.ems;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.ImportUtility;
import gov.nasa.jpl.mbee.ems.sync.AutoSyncCommitListener;
import gov.nasa.jpl.mbee.ems.sync.AutoSyncProjectListener;
import gov.nasa.jpl.mbee.ems.sync.OutputQueue;
import gov.nasa.jpl.mbee.ems.sync.Request;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class UpdateFromJMS extends MDAction {
    private static final long serialVersionUID = 1L;
    public static final String actionid = "UpdateFromJMS";
    
    private boolean commit;
    public UpdateFromJMS(boolean commit) {
        super(commit ? "CommitToMMS" : "UpdateFromJMS", commit ? "Commit" : "Update", null, null);
        this.commit = commit;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent ae) {
        Project project = Application.getInstance().getProject();
        Map<String, Set<String>> jms = AutoSyncProjectListener.getJMSChanges(Application.getInstance().getProject());
        AutoSyncCommitListener listener = AutoSyncProjectListener.getCommitListener(Application.getInstance().getProject());
        if (jms == null || listener == null)
            return; //some error here
        Map<String, Element> localAdded = listener.getAddedElements();
        Map<String, Element> localDeleted = listener.getDeletedElements();
        Map<String, Element> localChanged = listener.getChangedElements();
        
        Set<String> webChanged = jms.get("changed");
        Set<String> webAdded = jms.get("added");
        Set<String> webDeleted = jms.get("deleted");
        
        Set<String> toGet = new HashSet<String>(webChanged);
        toGet.addAll(webAdded);
        
        if (!toGet.isEmpty()) {
            JSONObject getJson = new JSONObject();
            JSONArray getElements = new JSONArray();
            getJson.put("elements", getElements);
            for (String e: toGet) {
                JSONObject el = new JSONObject();
                el.put("sysmlid", e);
                getElements.add(el);
            }
            String url = ExportUtility.getUrlWithWorkspace();
            url += "/elements";
            String response = ExportUtility.getWithBody(url, getJson.toJSONString());
            if (response == null)
                return; //bad
            Map<String, JSONObject> webElements = new HashMap<String, JSONObject>();
            JSONObject webObject = (JSONObject)JSONValue.parse(response);
            JSONArray webArray = (JSONArray)webObject.get("elements");
            for (Object o: webArray) {
                String webId = (String)((JSONObject)o).get("sysmlid");
                webElements.put(webId, (JSONObject)o);
            }
        
            SessionManager sm = SessionManager.getInstance();
            sm.createSession("mms delayed sync change");
            try {
                //take care of web added
                List<JSONObject> webAddedObjects = new ArrayList<JSONObject>();
                for (String webAdd: webAdded) {
                    webAddedObjects.add(webElements.get(webAdd));
                }
                List<JSONObject> webAddedSorted = ImportUtility.getCreationOrder(webAddedObjects);
                if (webAddedSorted != null) {
                    for (Object element : webAddedSorted) {
                        ImportUtility.createElement((JSONObject) element, false);
                    }
                    for (Object element : webAddedSorted) { 
                        ImportUtility.createElement((JSONObject) element, true);
                    }
                } else {
                    //cannot create all added elements
                }
            
                //take care of updated, find conflicts first
                Set<String> localChangedIds = new HashSet<String>(localChanged.keySet());
                localChangedIds.retainAll(webChanged);
                List<JSONObject> webConflictedObjects = new ArrayList<JSONObject>();
                if (!localChangedIds.isEmpty()) {
                    for (String conflictId: localChangedIds) {
                        webConflictedObjects.add(webElements.get(conflictId));
                    }
                }
                List<JSONObject> webChangedObjects = new ArrayList<JSONObject>();
                for (String webUpdate: webChanged) {
                    if (localChangedIds.contains(webUpdate))
                        continue;
                    webChangedObjects.add(webElements.get(webUpdate));
                }
                for (JSONObject webUpdated: webChangedObjects) {
                    Element e = ExportUtility.getElementFromID((String)webUpdated.get("sysmlid"));
                    ImportUtility.updateElement(e, webUpdated);
                }
                //conflicts???
            
                //take care of deleted
                for (String e: webDeleted) {
                    Element toBeDeleted = ExportUtility.getElementFromID(e);
                    if (toBeDeleted == null)
                        continue;
                    if (!toBeDeleted.isEditable())
                        TeamworkUtils.lockElement(project, toBeDeleted, false);
                    try {
                        ModelElementsManager.getInstance().removeElement(toBeDeleted);
                        //guilog.log("[Autosync] " + changedElement.getHumanName() + " deleted");
                    } catch (ReadOnlyElementException ex) {
                    //guilog.log("[ERROR - Autosync] Sync: " + changedElement.getHumanName() + " cannot be deleted!");
                    }
                }
                sm.closeSession();
            } catch (Exception e) {
                sm.cancelSession();
            }
        }
        
        if (commit) {
            JSONArray toSendElements = new JSONArray();
            for (Element e: localAdded.values()) {
                toSendElements.add(ExportUtility.fillElement(e, null));
            }
            for (Element e: localChanged.values()) {
                toSendElements.add(ExportUtility.fillElement(e, null));
            }
            JSONObject toSendUpdates = new JSONObject();
            toSendUpdates.put("elements", toSendElements);
            toSendUpdates.put("source", "magicdraw");
            OutputQueue.getInstance().offer(new Request(ExportUtility.getPostElementsUrl(), toSendUpdates.toJSONString(), "POST", true));
        
            JSONArray toDeleteElements = new JSONArray();
            for (String e: localDeleted.keySet()) {
                JSONObject toDelete = new JSONObject();
                toDelete.put("sysmlid", e);
                toDeleteElements.add(toDelete);
            }
            toSendUpdates.put("elements", toDeleteElements);
            OutputQueue.getInstance().offer(new Request(ExportUtility.getUrlWithWorkspace() + "/elements", toSendUpdates.toJSONString(), "DELETEALL", true));
        }
    }
}