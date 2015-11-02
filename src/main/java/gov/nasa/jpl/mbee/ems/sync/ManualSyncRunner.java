package gov.nasa.jpl.mbee.ems.sync;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.ImportException;
import gov.nasa.jpl.mbee.ems.ImportUtility;
import gov.nasa.jpl.mbee.ems.ServerException;
import gov.nasa.jpl.mbee.ems.validation.ModelValidator;
import gov.nasa.jpl.mbee.ems.validation.ViewValidator;
import gov.nasa.jpl.mbee.ems.validation.actions.DetailDiff;
import gov.nasa.jpl.mbee.ems.validation.actions.ImportHierarchy;
import gov.nasa.jpl.mbee.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mbee.viewedit.ViewHierarchyVisitor;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;

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
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

public class ManualSyncRunner implements RunnableWithProgress {
    private boolean delete = false;
    private boolean commit;
    
    private GUILog gl = Application.getInstance().getGUILog();
    private Logger log = Logger.getLogger(ManualSyncRunner.class);
    
    private boolean isFromTeamwork = false;
    private boolean loggedIn = true;
    private boolean failure = false;
    private boolean skipUpdate = false;
    
    private ValidationSuite suite = new ValidationSuite("Updated Elements/Failed Updates");
    private ValidationRule updated = new ValidationRule("updated", "updated", ViolationSeverity.INFO);
    private ValidationRule cannotUpdate = new ValidationRule("cannotUpdate", "cannotUpdate", ViolationSeverity.ERROR);
    private ValidationRule cannotRemove = new ValidationRule("cannotDelete", "cannotDelete", ViolationSeverity.WARNING);
    private ValidationRule cannotCreate = new ValidationRule("cannotCreate", "cannotCreate", ViolationSeverity.ERROR);
    private Set<String> cannotChange;
    
    public ManualSyncRunner(boolean commit, boolean skipUpdate, boolean delete) {
        this.commit = commit;
        this.skipUpdate = skipUpdate;
        this.delete = delete;
    }
    
    public ManualSyncRunner(boolean commit, boolean delete) {
        this.commit = commit;
        this.delete = delete;
    }
    
    public Set<String> getCannotChange() {
        return cannotChange;
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
    
    private boolean tryToLock(Project project, Element e) {
        return Utils.tryToLock(project, e, isFromTeamwork);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void run(ProgressStatus ps) {
        if (!skipUpdate)
            Utils.guilog("[INFO] Getting changes from MMS...");
    	suite.addValidationRule(updated);
    	suite.addValidationRule(cannotUpdate);
    	suite.addValidationRule(cannotRemove);
    	suite.addValidationRule(cannotCreate);
    	
        Project project = Application.getInstance().getProject();
        if (ProjectUtilities.isFromTeamworkServer(project.getPrimaryProject())) {
            isFromTeamwork = true;
            if (TeamworkUtils.getLoggedUserName() == null) {
                loggedIn = false;
                failure = true;
                Utils.guilog("[ERROR] You need to be logged in to teamwork first.");
                return;
            }
        }
            
        AutoSyncCommitListener listener = AutoSyncProjectListener.getCommitListener(Application.getInstance().getProject());
        if (listener == null) {
            Utils.guilog("[ERROR] Unexpected error happened, cannot get commit listener.");
            failure = true;
            return; //some error here
        }
        listener.disable();
        Map<String, Set<String>> jms = AutoSyncProjectListener.getJMSChanges(Application.getInstance().getProject());
        listener.enable();
        if (jms == null) {
            failure = true;
            return;
        }
        Map<String, Element> localAdded = listener.getAddedElements();
        Map<String, Element> localDeleted = listener.getDeletedElements();
        Map<String, Element> localChanged = listener.getChangedElements();
        
        //account for possible teamwork updates
        JSONObject previousUpdates = AutoSyncProjectListener.getUpdatesOrFailed(Application.getInstance().getProject(), "update");
        if (previousUpdates != null) {
            for (String added: (List<String>)previousUpdates.get("added")) {
                if (localAdded.containsKey(added) || localChanged.containsKey(added))
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
                if (ExportUtility.getElementFromID(deleted) != null) {
                    if (localDeleted.containsKey(deleted))
                        localDeleted.remove(deleted);
                    continue; //not deleted?
                }
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
        cannotChange = new HashSet<String>();
        Set<String> cannotDelete = new HashSet<String>();
        
        if (!toGet.isEmpty()) {
            //TeamworkUtils.lockElement(project, project.getModel(), true);
            //tryToLock(project);
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
            Utils.guilog("[INFO] Getting " + getElements.size() + " elements from MMS.");
            String response = null;
            try {
                response = ExportUtility.getWithBody(url, getJson.toJSONString());
            } catch (ServerException ex) {
                Utils.guilog("[ERROR] Get elements failed.");
            }
            if (response == null)
                return; //should repopulate error block?
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
            
            Utils.guilog("[INFO] Applying changes...");
            SessionManager sm = SessionManager.getInstance();
            sm.createSession("mms delayed sync change");
            try {
                Map<String, List<JSONObject>> toCreate = ImportUtility.getCreationOrder(webAddedObjects);
                
                List<JSONObject> webAddedSorted = toCreate.get("create");
                List<JSONObject> fails = toCreate.get("fail");
                List<Map<String, Object>> toChange = new ArrayList<Map<String, Object>>();
                //take care of web added
                if (webAddedSorted != null) {
                    ImportUtility.outputError = false;
                    for (Object element : webAddedSorted) {
                        try {
                            ImportUtility.createElement((JSONObject) element, false);
                        } catch (ImportException ex) {
                            
                        }
                    }
                    ImportUtility.outputError = true;
                    for (Object element : webAddedSorted) { 
                        try {
                            Element newe = ImportUtility.createElement((JSONObject) element, true);
                            //Utils.guilog("[SYNC ADD] " + newe.getHumanName() + " created.");
                            updated.addViolation(new ValidationRuleViolation(newe, "[CREATED]"));
                        } catch (Exception ex) {
                            log.error("", ex);
                            cannotAdd.add((String)((JSONObject)element).get("sysmlid"));
                            ValidationRuleViolation vrv = new ValidationRuleViolation(null, "[CREATE FAILED] " + ex.getMessage());
                            vrv.addAction(new DetailDiff(new JSONObject(), (JSONObject)element));
                            cannotCreate.addViolation(vrv);
                        }
                    }
                } 
                for (JSONObject element: fails) {
                    cannotAdd.add((String)element.get("sysmlid"));
                    ValidationRuleViolation vrv = new ValidationRuleViolation(null, "[CREATE FAILED] Owner or chain of owners not found");
                    vrv.addAction(new DetailDiff(new JSONObject(), element));
                    cannotCreate.addViolation(vrv);
                }
                
            
                //take care of updated
                for (JSONObject webUpdated: webChangedObjects) {
                    Element e = ExportUtility.getElementFromID((String)webUpdated.get("sysmlid"));
                    if (e == null) {
                        //TODO bad? maybe it was supposed to have been added?
                        continue;
                    }
                    JSONObject spec = (JSONObject)webUpdated.get("specialization");
                    if (spec != null && spec.get("contents") != null) {
                        Constraint c = Utils.getViewConstraint(e);
                        if (c != null) {
                            if (!tryToLock(project, c)) {
                                cannotChange.add(ExportUtility.getElementID(e)); //this is right since contents is embedded in view
                                //Utils.guilog("[ERROR - SYNC UPDATE] " + e.getHumanName() + " not editable.");
                                cannotUpdate.addViolation(new ValidationRuleViolation(c, "[UPDATE FAILED] - not editable"));
                                continue;
                            }
                        }
                    }
                    if (!tryToLock(project, e)) {
                        cannotChange.add(ExportUtility.getElementID(e));
                        //Utils.guilog("[ERROR - SYNC UPDATE] " + e.getHumanName() + " not editable.");
                        cannotUpdate.addViolation(new ValidationRuleViolation(e, "[UPDATE FAILED] - not editable"));
                        continue;
                    }
                    try {
                        ImportUtility.updateElement(e, webUpdated);
                        ImportUtility.setOwner(e, webUpdated);
                        updated.addViolation(new ValidationRuleViolation(e, "[UPDATED]"));
                        //Utils.guilog("[SYNC UPDATE] " + e.getHumanName() + " updated.");
                        /*if (webUpdated.containsKey("specialization")) { //do auto doc hierarchy? very risky
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
                                        Utils.guilog("[SYNC] Document hierarchy updated for " + e.getHumanName());
                                        toChange.add(result);
                                    } else
                                        cannotChange.add(ExportUtility.getElementID(e));
                                }
                            }
                        }*/
                    } catch (Exception ex) {
                        //Utils.guilog("[ERROR - SYNC UPDATE] " + e.getHumanName() + " failed to update from MMS: " + ex.getMessage());
                        ValidationRuleViolation vrv = new ValidationRuleViolation(e, "[UPDATE FAILED] " + ex.getMessage());
                        cannotUpdate.addViolation(vrv);
                        log.error("", ex);
                        cannotChange.add(ExportUtility.getElementID(e));
                    }
                }
                
                //take care of deleted
                for (String e: webDeleted) {
                    Element toBeDeleted = ExportUtility.getElementFromID(e);
                    if (toBeDeleted == null)
                        continue;
                    if (!tryToLock(project, toBeDeleted)) {
                        cannotDelete.add(e);
                        cannotRemove.addViolation(new ValidationRuleViolation(toBeDeleted, "[DELETE FAILED] - not editable"));
                        //Utils.guilog("[ERROR - SYNC DELETE] " + toBeDeleted.getHumanName() + " not editable.");
                        continue;
                    }
                    try {
                        ModelElementsManager.getInstance().removeElement(toBeDeleted);
                        Utils.guilog("[SYNC DELETE] " + toBeDeleted.getHumanName() + " deleted.");
                    } catch (Exception ex) {
                        log.error("", ex);
                        cannotDelete.add(e);
                    }
                }
                listener.disable();
                sm.closeSession();
                listener.enable();
                if (!skipUpdate)
                    Utils.guilog("[INFO] Finished applying changes.");
                for (Map<String, Object> r: toChange) {
                    ImportHierarchy.sendChanges(r); //what about if doc is involved in conflict?
                }
            } catch (Exception ex) {
                //something really bad happened, save all changes for next time;
                log.error("", ex);
                sm.cancelSession();
                Utils.printException(ex);
                cannotAdd.clear();
                cannotChange.clear();
                cannotDelete.clear();
                updated.getViolations().clear();
                cannotUpdate.getViolations().clear();
                cannotRemove.getViolations().clear();
                cannotCreate.getViolations().clear();
                for (String e: webDeleted) {
                    cannotDelete.add(e);
                }
                for (JSONObject element: webAddedObjects) {
                    cannotAdd.add((String)((JSONObject)element).get("sysmlid"));
                }
                for (JSONObject element: webChangedObjects) {
                    cannotChange.add((String)element.get("sysmlid"));
                }
                Utils.guilog("[ERROR] Unexpected exception happened, all changes will be reattempted at next update.");
                
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
                        AutoSyncProjectListener.setUpdatesOrFailed(project, failed, "error", true);
                        sm.closeSession();
                    } catch (Exception ex) {
                        log.error("", ex);
                        sm.cancelSession();
                    }
                    listener.enable();
                    Utils.guilog("[INFO] There were changes that couldn't be applied. These will be attempted on the next update.");
                    //if (!cannotAdd.isEmpty() || !cannotChange.isEmpty()) {
                      //  failure = true;
                    //}
                } else {
                    listener.disable();
                    sm.createSession("failed changes");
                    try {
                        AutoSyncProjectListener.setUpdatesOrFailed(project, null, "error", true);
                        sm.closeSession();
                    } catch (Exception ex) {
                        log.error("", ex);
                        sm.cancelSession();
                    }
                    listener.enable();
                }
                
                //show window of what got changed
                List<ValidationSuite> vss = new ArrayList<ValidationSuite>();
                vss.add(suite);
                if (suite.hasErrors())
                    Utils.displayValidationWindow(vss, "Delta Sync Log");
                
                
              //conflicts
                JSONObject mvResult = new JSONObject();
                mvResult.put("elements", webConflictedObjects);
                ModelValidator mv = new ModelValidator(null, mvResult, false, localConflictedElements, false);
                try {
                    mv.validate(false, null);
                } catch (ServerException ex) {
                    
                }
                Set<Element> conflictedElements = mv.getDifferentElements();
                if (!conflictedElements.isEmpty()) {
                    JSONObject conflictedToSave = new JSONObject();
                    JSONArray conflictedElementIds = new JSONArray();
                    for (Element ce: conflictedElements)
                        conflictedElementIds.add(ExportUtility.getElementID(ce));
                    conflictedToSave.put("elements", conflictedElementIds);
                    Utils.guilog("[INFO] There are potential conflicts between changes from MMS and locally changed elements, please resolve first and rerun update/commit.");
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
                    failure = true;
                    mv.showWindow();
                    return;
                } 
        } else {
            if (!skipUpdate)
                Utils.guilog("[INFO] MMS has no updates.");
            //AutoSyncProjectListener.setLooseEnds(project, null);
            //AutoSyncProjectListener.setFailed(project, null);
        }
        
        //send local changes
        if (commit) {
            Utils.guilog("[INFO] Committing local changes to MMS...");
            JSONArray toSendElements = new JSONArray();
            Set<String> alreadyAdded = new HashSet<String>();
            for (Element e: localAdded.values()) {
                if (e == null)
                    continue;
                String id = ExportUtility.getElementID(e);
                if (id == null)
                    continue;
                if (alreadyAdded.contains(id))
                    continue;
                alreadyAdded.add(id);
                toSendElements.add(ExportUtility.fillElement(e, null));
            }
            for (Element e: localChanged.values()) {
                if (e == null)
                    continue;
                String id = ExportUtility.getElementID(e);
                if (id == null)
                    continue;
                if (alreadyAdded.contains(id))
                    continue;
                alreadyAdded.add(id);
                toSendElements.add(ExportUtility.fillElement(e, null));
            }
            JSONObject toSendUpdates = new JSONObject();
            toSendUpdates.put("elements", toSendElements);
            toSendUpdates.put("source", "magicdraw");
            if (toSendElements.size() > 100) {
                
            }
            //do foreground?
            if (!toSendElements.isEmpty()) {
            	Utils.guilog("[INFO] Change requests are added to queue.");
                OutputQueue.getInstance().offer(new Request(ExportUtility.getPostElementsUrl(), toSendUpdates.toJSONString(), "POST", true, toSendElements.size(), "Sync Changes"));
            }
            localAdded.clear();
            localChanged.clear();
            
            JSONArray toDeleteElements = new JSONArray();
            if (delete) {
                for (String e: localDeleted.keySet()) {
                    if (ExportUtility.getElementFromID(e) != null) //somehow the model has it, don't delete on server
                        continue;
                    JSONObject toDelete = new JSONObject();
                    toDelete.put("sysmlid", e);
                    toDeleteElements.add(toDelete);
                }
                toSendUpdates.put("elements", toDeleteElements);
                if (!toDeleteElements.isEmpty()) {
                	Utils.guilog("[INFO] Delete requests are added to queue.");
                    OutputQueue.getInstance().offer(new Request(ExportUtility.getUrlWithWorkspace() + "/elements", toSendUpdates.toJSONString(), "DELETEALL", true, toDeleteElements.size(), "Sync Deletes"));
                }
                localDeleted.clear();
            }
            if (toDeleteElements.isEmpty() && toSendElements.isEmpty())
                Utils.guilog("[INFO] No changes to commit.");
            if (!toDeleteElements.isEmpty() || !toSendElements.isEmpty() || !toGet.isEmpty())
                Utils.guilog("[INFO] Don't forget to save or commit to teamwork and unlock!");
            
            JSONObject toSave = null;
            if (!delete && !localDeleted.isEmpty()) {
                toSave = new JSONObject();
                JSONArray toSaveDelete = new JSONArray();
                toSaveDelete.addAll(localDeleted.keySet());
                toSave.put("deleted", toSaveDelete);
                toSave.put("changed", new JSONArray());
                toSave.put("added", new JSONArray());
                localDeleted.clear();
            }
            listener.disable();
            SessionManager sm = SessionManager.getInstance();
            sm.createSession("updates sent");
            try {
                AutoSyncProjectListener.setUpdatesOrFailed(project, toSave, "update", true);
                sm.closeSession();
            } catch (Exception ex) {
                log.error("", ex);
                sm.cancelSession();
            }
            listener.enable();
        }
        if (!toGet.isEmpty() && !commit)
            Utils.guilog("[INFO] Don't forget to save or commit to teamwork and unlock!");
    }
    
    public boolean getFailure() {
        return failure;
    }
}
