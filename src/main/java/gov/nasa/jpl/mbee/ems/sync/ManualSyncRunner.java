package gov.nasa.jpl.mbee.ems.sync;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.ImportUtility;
import gov.nasa.jpl.mbee.ems.validation.ModelValidator;
import gov.nasa.jpl.mbee.ems.validation.ViewValidator;
import gov.nasa.jpl.mbee.ems.validation.actions.ImportHierarchy;
import gov.nasa.jpl.mbee.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mbee.viewedit.ViewHierarchyVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class ManualSyncRunner implements RunnableWithProgress {

    private boolean commit;
    
    private GUILog gl = Application.getInstance().getGUILog();
    private Logger log = Logger.getLogger(ManualSyncRunner.class);
    
    public ManualSyncRunner(boolean commit) {
        this.commit = commit;
    }
    
    private void tryToLock(Project project) {
        if (!ProjectUtilities.isFromTeamworkServer(project.getPrimaryProject())) 
            return;
        for (Element e: project.getModel().getOwnedElement()) {
            if (ProjectUtilities.isElementInAttachedProject(e))
                continue;
            TeamworkUtils.lockElement(project, e, true);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void run(ProgressStatus ps) {
        Application.getInstance().getGUILog().log("[INFO] Getting changes from MMS...");
        Project project = Application.getInstance().getProject();
        if (ProjectUtilities.isFromTeamworkServer(project.getPrimaryProject())) {
            if (TeamworkUtils.getLoggedUserName() == null) {
                gl.log("[ERROR] You need to be logged in to teamwork first.");
                return;
            }
        }
            
        AutoSyncCommitListener listener = AutoSyncProjectListener.getCommitListener(Application.getInstance().getProject());
        if (listener == null) {
            gl.log("[ERROR] Unexpected error happened.");
            return; //some error here
        }
        listener.disable();
        Map<String, Set<String>> jms = AutoSyncProjectListener.getJMSChanges(Application.getInstance().getProject());
        listener.enable();
        if (jms == null)
            return;
        Map<String, Element> localAdded = listener.getAddedElements();
        Map<String, Element> localDeleted = listener.getDeletedElements();
        Map<String, Element> localChanged = listener.getChangedElements();
        
        //account for possible teamwork updates
        JSONObject previousUpdates = AutoSyncProjectListener.getUpdatesOrFailed(Application.getInstance().getProject(), "update");
        if (previousUpdates != null) {
            for (String added: (List<String>)previousUpdates.get("added")) {
                if (localAdded.containsKey(added))
                    continue;
                Element e = ExportUtility.getElementFromID(added);
                if (e != null)
                    localAdded.put(added, e);
            }
            for (String updated: (List<String>)previousUpdates.get("changed")) {
                if (!localChanged.containsKey(updated)) {
                    Element e = ExportUtility.getElementFromID(updated);
                    if (e != null) 
                        localChanged.put(updated, e);
                }
                localAdded.remove(updated);
            }
            for (String deleted: (List<String>)previousUpdates.get("deleted")) {
                if (!localDeleted.containsKey(deleted)) {
                    localDeleted.put(deleted, null);
                }
                localAdded.remove(deleted);
                localChanged.remove(deleted);
            }
        }
        
        Set<String> webChanged = jms.get("changed");
        Set<String> webAdded = jms.get("added");
        Set<String> webDeleted = jms.get("deleted");
        
        Set<String> toGet = new HashSet<String>(webChanged);
        toGet.addAll(webAdded);
        
        Set<String> cannotAdd = new HashSet<String>();
        Set<String> cannotChange = new HashSet<String>();
        Set<String> cannotDelete = new HashSet<String>();
        
        if (!toGet.isEmpty()) {
            //TeamworkUtils.lockElement(project, project.getModel(), true);
            tryToLock(project);
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
            gl.log("[INFO] Getting " + getElements.size() + " elements from MMS.");
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
            //if (webElements.size() != toGet.size())
            //    return; //??
                
            //calculate order to create web added elements
            List<JSONObject> webAddedObjects = new ArrayList<JSONObject>();
            for (String webAdd: webAdded) {
                if (webElements.containsKey(webAdd))
                    webAddedObjects.add(webElements.get(webAdd));
            }
            List<JSONObject> webAddedSorted = ImportUtility.getCreationOrder(webAddedObjects);
            
            //calculate potential conflicted set and clean web updated set
            Set<String> localChangedIds = new HashSet<String>(localChanged.keySet());
            localChangedIds.retainAll(webChanged);
            JSONArray webConflictedObjects = new JSONArray();
            Set<Element> localConflictedElements = new HashSet<Element>();
            if (!localChangedIds.isEmpty()) {
                for (String conflictId: localChangedIds) {
                    if (webElements.containsKey(conflictId)) {
                        webConflictedObjects.add(webElements.get(conflictId));
                        localConflictedElements.add(localChanged.get(conflictId));
                    }
                }
            }
            //find web changed that are not conflicted
            List<JSONObject> webChangedObjects = new ArrayList<JSONObject>();
            for (String webUpdate: webChanged) {
                if (localChangedIds.contains(webUpdate))
                    continue;
                if (webElements.containsKey(webUpdate))
                    webChangedObjects.add(webElements.get(webUpdate));
            }
            
            gl.log("[INFO] Applying changes...");
            SessionManager sm = SessionManager.getInstance();
            sm.createSession("mms delayed sync change");
            try {
                List<Map<String, Object>> toChange = new ArrayList<Map<String, Object>>();
                //take care of web added
                if (webAddedSorted != null) {
                    for (Object element : webAddedSorted) {
                        try {
                            ImportUtility.createElement((JSONObject) element, false);
                        } catch (Exception ex) {
                            
                        }
                    }
                    for (Object element : webAddedSorted) { 
                        try {
                            Element newe = ImportUtility.createElement((JSONObject) element, true);
                            gl.log("[SYNC] " + newe.getHumanName() + " created.");
                        } catch (Exception ex) {
                            log.error("", ex);
                            cannotAdd.add((String)((JSONObject)element).get("sysmlid"));
                        }
                    }
                } else {
                    for (Object element: webAddedObjects) {
                        cannotAdd.add((String)((JSONObject)element).get("sysmlid"));
                    }
                }
            
                //take care of updated
                for (JSONObject webUpdated: webChangedObjects) {
                    Element e = ExportUtility.getElementFromID((String)webUpdated.get("sysmlid"));
                    if (e == null) {
                        //bad? maybe it was supposed to have been added?
                        continue;
                    }
                    if (!e.isEditable()) {
                        cannotChange.add(ExportUtility.getElementID(e));
                        gl.log("[ERROR - SYNC] " + e.getHumanName() + " not editable.");
                        continue;
                    }
                    try {
                        ImportUtility.updateElement(e, webUpdated);
                        ImportUtility.setOwner(e, webUpdated);
                        gl.log("[SYNC] " + e.getHumanName() + " updated.");
                        if (webUpdated.containsKey("specialization")) {
                            JSONArray view2view = (JSONArray)((JSONObject)webUpdated.get("specialization")).get("view2view");
                            if (view2view != null) {
                                JSONObject web = ExportUtility.keyView2View(view2view);
                                DocumentGenerator dg = new DocumentGenerator(e, null, null);
                                Document dge = dg.parseDocument(true, true, true);
                                ViewHierarchyVisitor vhv = new ViewHierarchyVisitor();
                                dge.accept(vhv);
                                JSONObject model = vhv.getView2View();
                                if (!ViewValidator.viewHierarchyMatch(e, dge, vhv, (JSONObject)webUpdated.get("specialization"))) {
                                    Map<String, Object> result = ImportHierarchy.importHierarchy(e, model, web);
                                    if (result != null && (Boolean)result.get("success")) {
                                        gl.log("[SYNC] Document hierarchy updated for " + e.getHumanName());
                                        toChange.add(result);
                                    } else
                                        cannotChange.add(ExportUtility.getElementID(e));
                                }
                            }
                        }
                    } catch (Exception ex) {
                        gl.log("[ERROR - SYNC] " + e.getHumanName() + " failed to update from MMS: " + ex.getMessage());
                        log.error("", ex);
                        cannotChange.add(ExportUtility.getElementID(e));
                    }
                }
                
                //take care of deleted
                for (String e: webDeleted) {
                    Element toBeDeleted = ExportUtility.getElementFromID(e);
                    if (toBeDeleted == null)
                        continue;
                    try {
                        ModelElementsManager.getInstance().removeElement(toBeDeleted);
                        gl.log("[SYNC] " + toBeDeleted.getHumanName() + " deleted.");
                    } catch (Exception ex) {
                        log.error("", ex);
                        cannotDelete.add(e);
                    }
                }
                listener.disable();
                sm.closeSession();
                listener.enable();
                gl.log("[INFO] Finished applying changes.");
                for (Map<String, Object> r: toChange) {
                    ImportHierarchy.sendChanges(r); //what about if doc is involved in conflict?
                }
                
                if (!cannotAdd.isEmpty() || !cannotChange.isEmpty() || !cannotDelete.isEmpty()) {
                    JSONObject failed = new JSONObject();
                    JSONArray failedAdd = new JSONArray();
                    failedAdd.addAll(cannotAdd);
                    JSONArray failedChange = new JSONArray();
                    failedChange.addAll(cannotChange);
                    JSONArray failedDelete = new JSONArray();
                    failedDelete.addAll(cannotDelete);
                    failed.put("changed", failedChange);
                    failed.put("added", failedAdd);
                    failed.put("deleted", failedDelete);
                    listener.disable();
                    sm.createSession("failed changes");
                    try {
                        AutoSyncProjectListener.setUpdatesOrFailed(project, failed, "error");
                        sm.closeSession();
                    } catch (Exception ex) {
                        log.error("", ex);
                        sm.cancelSession();
                    }
                    listener.enable();
                    gl.log("[INFO] There were changes that couldn't be applied. These will be attempted on the next update.");
                }
                
              //conflicts
                JSONObject mvResult = new JSONObject();
                mvResult.put("elements", webConflictedObjects);
                ModelValidator mv = new ModelValidator(null, mvResult, false, localConflictedElements, false);
                mv.validate(false, null);
                Set<Element> conflictedElements = mv.getDifferentElements();
                if (!conflictedElements.isEmpty()) {
                    JSONObject conflictedToSave = new JSONObject();
                    JSONArray conflictedElementIds = new JSONArray();
                    for (Element ce: conflictedElements)
                        conflictedElementIds.add(ExportUtility.getElementID(ce));
                    conflictedToSave.put("elements", conflictedElementIds);
                    gl.log("[INFO] There are potential conflicts between changes from MMS and locally changed elements, please resolve first and rerun update/commit.");
                    listener.disable();
                    sm.createSession("failed changes");
                    try {
                        AutoSyncProjectListener.setConflicts(project, conflictedToSave);
                        sm.closeSession();
                    } catch (Exception ex) {
                        log.error("", ex);
                        sm.cancelSession();
                    }
                    listener.enable();
                    mv.showWindow();
                    return;
                } 
            } catch (Exception e) {
                log.error("", e);
                sm.cancelSession();
            }
        } else {
            gl.log("[INFO] MMS has no updates.");
            //AutoSyncProjectListener.setLooseEnds(project, null);
            //AutoSyncProjectListener.setFailed(project, null);
        }
        
        //send local changes
        if (commit) {
            gl.log("[INFO] Committing local changes to MMS...");
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
            if (toSendElements.size() > 100) {
                
            }
            //do foreground?
            if (!toSendElements.isEmpty()) {
                Application.getInstance().getGUILog().log("[INFO] Change requests are added to queue.");
                OutputQueue.getInstance().offer(new Request(ExportUtility.getPostElementsUrl(), toSendUpdates.toJSONString(), "POST", true, toSendElements.size()));
            }
            localAdded.clear();
            localChanged.clear();
            
            JSONArray toDeleteElements = new JSONArray();
            for (String e: localDeleted.keySet()) {
                if (ExportUtility.getElementFromID(e) != null) //somehow the model has it, don't delete on server
                    continue;
                JSONObject toDelete = new JSONObject();
                toDelete.put("sysmlid", e);
                toDeleteElements.add(toDelete);
            }
            toSendUpdates.put("elements", toDeleteElements);
            if (!toDeleteElements.isEmpty()) {
                Application.getInstance().getGUILog().log("[INFO] Delete requests are added to queue.");
                OutputQueue.getInstance().offer(new Request(ExportUtility.getUrlWithWorkspace() + "/elements", toSendUpdates.toJSONString(), "DELETEALL", true, toDeleteElements.size()));
            }
            localDeleted.clear();
            if (toDeleteElements.isEmpty() && toSendElements.isEmpty())
                gl.log("[INFO] No changes to commit.");
            if (!toDeleteElements.isEmpty() || !toSendElements.isEmpty() || !toGet.isEmpty())
                gl.log("[INFO] Don't forget to save or commit to teamwork and unlock!");
            
            listener.disable();
            SessionManager sm = SessionManager.getInstance();
            sm.createSession("updates sent");
            try {
                AutoSyncProjectListener.setUpdatesOrFailed(project, null, "update");
                sm.closeSession();
            } catch (Exception ex) {
                log.error("", ex);
                sm.cancelSession();
            }
            listener.enable();
        }
        if (!toGet.isEmpty() && !commit)
            gl.log("[INFO] Don't forget to save or commit to teamwork and unlock!");
    }
}
