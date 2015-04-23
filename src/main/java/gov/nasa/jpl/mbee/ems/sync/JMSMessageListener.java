package gov.nasa.jpl.mbee.ems.sync;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.ImportUtility;
import gov.nasa.jpl.mbee.ems.validation.ViewValidator;
import gov.nasa.jpl.mbee.ems.validation.actions.ImportHierarchy;
import gov.nasa.jpl.mbee.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mbee.viewedit.ViewHierarchyVisitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class JMSMessageListener implements MessageListener {

    private Project project;
    private static Logger log = Logger.getLogger(JMSMessageListener.class);
    public JMSMessageListener(Project project) {
        this.project = project;
    }

    private Set<String> cannotAdd = new HashSet<String>();
    private Set<String> cannotChange = new HashSet<String>();
    private Set<String> cannotDelete = new HashSet<String>();
    
    public Set<String> getCannotAdd() {
        return cannotAdd;
    }

    public Set<String> getCannotChange() {
        return cannotChange;
    }

    public Set<String> getCannotDelete() {
        return cannotDelete;
    }

    @Override
    public void onMessage(Message msg) {
        try {
            // Take the incoming message and parse it into a
            // JSONObject.
            //
            TextMessage message = (TextMessage) msg;
            log.info("From JMS: " + message.getText());
            JSONObject ob = (JSONObject) JSONValue.parse(message.getText());
            if (ob.get("source") != null && ob.get("source").equals("magicdraw"))
                return;
            // Changed element are encapsulated in the "workspace2"
            // JSONObject.
            //
            JSONObject ws2 = (JSONObject) ob.get("workspace2");

            // Retrieve the changed elements: each type of change (updated,
            // added, moved, deleted)
            // will be returned as an JSONArray.
            //
            final JSONArray updated = (JSONArray) ws2.get("updatedElements");
            final JSONArray added = (JSONArray) ws2.get("addedElements");
            final JSONArray deleted = (JSONArray) ws2.get("deletedElements");
            final JSONArray moved = (JSONArray) ws2.get("movedElements");

            Runnable runnable = new Runnable() {
                private GUILog guilog = Application.getInstance().getGUILog();
                public void run() {
                    Map<String, ?> projectInstances = ProjectListenerMapping.getInstance().get(project);
                    AutoSyncCommitListener listener = (AutoSyncCommitListener) projectInstances
                            .get(AutoSyncProjectListener.LISTENER);

                    // Disable the listener so we do not react to the
                    // changes we are importing from MMS.
                    //
                    //if (listener != null)
                      //  listener.disable();

                    SessionManager sm = SessionManager.getInstance();
                    sm.createSession("mms sync change");
                    try {
                        List<Map<String, Object>> toChange = new ArrayList<Map<String, Object>>();
                        // Loop through each specified element.
                        //
                        List<JSONObject> sortedAdded = ImportUtility.getCreationOrder((List<JSONObject>)added);
                        if (sortedAdded != null) {
                            for (Object element : added) {
                                addElement((JSONObject) element, false);
                            }
                            for (Object element : added) { //do a second pass to update relations in case relations are part of new elements
                                addElement((JSONObject) element, true);
                            }
                        } else {
                            log.error("jms message added can't be executed - " + added.toJSONString());
                            for (JSONObject element: (List<JSONObject>)added) {
                                cannotAdd.add((String)((JSONObject)element).get("sysmlid"));
                            }
                        }
                        for (Object element : moved) {
                            moveElement((JSONObject) element);
                        }
                        for (Object element : updated) { 
                            Map<String, Object> results = makeChange((JSONObject) element);
                            if (results != null)
                                toChange.add(results);
                        }
                        for (Object element : deleted) {
                            deleteElement((JSONObject) element);
                        }
                        
                        if (listener != null)
                            listener.disable();

                        sm.closeSession();
                        for (Map<String, Object> r: toChange) {
                            ImportHierarchy.sendChanges(r);
                            //OutputQueue.getInstance().offer(r);
                        }
                        if (listener != null)
                            listener.enable();
                    }
                    catch (Exception e) {
                        sm.cancelSession();
                        log.error(e, e);
                        if (listener != null)
                            listener.enable();
                    }

                    // Once we've completed make all the
                    // changes, enable the listener, duplicated everywhere seems like some timing issue/bug isn't always reenabling it
                    //
                    if (listener != null)
                        listener.enable();
                }

                private Map<String, Object> makeChange(JSONObject ob) {
                    String sysmlid = (String) ob.get("sysmlid");
                    try {
                        Element changedElement = ExportUtility.getElementFromID(sysmlid);
                        if (changedElement == null) {
                            Utils.guilog("[ERROR - Autosync] element " + sysmlid + " not found for autosync change");
                            return null;
                        } else if (!changedElement.isEditable()) {
                            if (!TeamworkUtils.lockElement(project, changedElement, false)) {
                                Utils.guilog("[ERROR - Autosync] " + changedElement.getHumanName() + " is not editable!");
                                cannotChange.add(sysmlid);
                                return null;
                            }
                        }
                        ImportUtility.updateElement(changedElement, ob);
                        Utils.guilog("[Autosync] " + changedElement.getHumanName() + " updated");
                        if (ob.containsKey("specialization")) {
                            JSONArray view2view = (JSONArray)((JSONObject)ob.get("specialization")).get("view2view");
                            if (view2view != null) {
                                JSONObject web = ExportUtility.keyView2View(view2view);
                                DocumentGenerator dg = new DocumentGenerator(changedElement, null, null);
                                Document dge = dg.parseDocument(true, true, true);
                                ViewHierarchyVisitor vhv = new ViewHierarchyVisitor();
                                dge.accept(vhv);
                                JSONObject model = vhv.getView2View();
                                if (!ViewValidator.viewHierarchyMatch(changedElement, dge, vhv, (JSONObject)ob.get("specialization"))) {
                                    Map<String, Object> result = ImportHierarchy.importHierarchy(changedElement, model, web);
                                    if (result != null && (Boolean)result.get("success")) {
                                        Utils.guilog("[Autosync] Document hierarchy updated for " + changedElement.getHumanName());
                                        return result;
                                    } else {
                                        cannotChange.add(sysmlid);
                                    }
                                    //List<Request> requests = ImportHierarchy.sendChanges(result);
                                    //return requests;
                                }
                            }
                        }
                        return null;
                    } catch (Exception ex) {
                        log.error("", ex);
                        cannotChange.add(sysmlid);
                        return null;
                    }
                }

                private void addElement(JSONObject ob, boolean updateRelations) {
                    try {
                        Element e = ImportUtility.createElement(ob, updateRelations);
                        if (e == null && updateRelations) {
                            Utils.guilog("[ERROR -- Autosync] create element failed, owner not found");
                            cannotAdd.add((String)ob.get("sysmlid"));
                        }
                        else if (e != null && updateRelations)
                            Utils.guilog("[Autosync] " + e.getHumanName() + " created");
                    } catch (Exception ex) {
                        log.error("", ex);
                        cannotAdd.add((String)ob.get("sysmlid"));
                    }
                }

                private void deleteElement(JSONObject ob) {
                    String sysmlid = (String) ob.get("sysmlid");
                    Element changedElement = ExportUtility.getElementFromID(sysmlid);
                    if (changedElement == null) {
                        //Application.getInstance().getGUILog().log("[ERROR - Autosync] element " + sysmlid + " not found for autosync delete");
                        return;
                    }
                    if (!changedElement.isEditable())
                        TeamworkUtils.lockElement(project, changedElement, false);
                    try {
                        ModelElementsManager.getInstance().removeElement(changedElement);
                        Utils.guilog("[Autosync] " + changedElement.getHumanName() + " deleted");
                    } catch (ReadOnlyElementException e) {
                        Utils.guilog("[ERROR - Autosync] Sync: " + changedElement.getHumanName() + " cannot be deleted!");
                        log.error("", e);
                        cannotDelete.add((String)ob.get("sysmlid"));
                    }
                }

                private void moveElement(JSONObject ob) {
                    String sysmlid = (String) ob.get("sysmlid");
                    try {
                        Element changedElement = ExportUtility.getElementFromID(sysmlid);
                        if (changedElement == null) {
                            Utils.guilog("[ERROR - Autosync] element " + sysmlid + " not found for autosync move");
                            return;
                        }
                        ImportUtility.setOwner(changedElement, ob);
                        Utils.guilog("[Autosync] " + changedElement.getHumanName() + " moved");
                    } catch (Exception ex) {
                        log.error("", ex);
                        cannotChange.add(sysmlid);
                    }
                }
            };
            project.getRepository().invokeAfterTransaction(runnable);
            message.acknowledge();

        }
        catch (Exception e) {
            log.error("", e);
        }
    }
}
