package gov.nasa.jpl.mbee.ems.sync.jms;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.ImportException;
import gov.nasa.jpl.mbee.ems.ImportUtility;
import gov.nasa.jpl.mbee.ems.sync.common.CommonSyncProjectEventListenerAdapter;
import gov.nasa.jpl.mbee.ems.sync.common.CommonSyncTransactionCommitListener;
import gov.nasa.jpl.mbee.ems.validation.ViewValidator;
import gov.nasa.jpl.mbee.ems.validation.actions.ImportHierarchy;
import gov.nasa.jpl.mbee.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.lib.Changelog;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mbee.options.MDKOptionsGroup;
import gov.nasa.jpl.mbee.viewedit.ViewHierarchyVisitor;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Deprecated
public class LegacyJMSMessageListener implements MessageListener {
    private Project project;
    private static Logger log = Logger.getLogger(LegacyJMSMessageListener.class);
    private Changelog<String, Void> jmsChangelog = new Changelog<>(),
            failedChangelog = new Changelog<>();

    public LegacyJMSMessageListener(Project project) {
        this.project = project;
    }

    @Override
    public void onMessage(Message msg) {
        boolean print = MDKOptionsGroup.getMDKOptions().isLogJson();
        try {
            // Take the incoming message and parse it into a JSONObject.
            TextMessage message = (TextMessage) msg;
            if (print) {
                log.info("From JMS: " + message.getText());
            }
            JSONObject ob = (JSONObject) JSONValue.parse(message.getText());
            if (ob.get("source") != null && ob.get("source").equals("magicdraw")) {
                return;
            }
            // Changed element are encapsulated in the "workspace2" JSONObject.
            Object o = ob.get("workspace2");
            if (!(o instanceof JSONObject)) {
                return;
            }
            JSONObject workspace2 = (JSONObject) o;
            // Retrieve the changed elements: each type of change (updated, added, moved, deleted) will be returned as an JSONArray.
            final JSONArray updated = ((o = workspace2.get("updatedElements")) instanceof JSONArray) ? (JSONArray) o : new JSONArray(),
                    added = ((o = workspace2.get("addedElements")) instanceof JSONArray) ? (JSONArray) o : new JSONArray(),
                    deleted = ((o = workspace2.get("deletedElements")) instanceof JSONArray) ? (JSONArray) o : new JSONArray(),
                    moved = ((o = workspace2.get("movedElements")) instanceof JSONArray) ? (JSONArray) o : new JSONArray();

            Runnable runnable = new Runnable() {
                public void run() {
                    CommonSyncTransactionCommitListener listener = CommonSyncProjectEventListenerAdapter.getProjectMapping(project).getCommonSyncTransactionCommitListener();

                    SessionManager sm = SessionManager.getInstance();
                    sm.createSession("MMS Sync Change");
                    try {
                        List<Map<String, Object>> toChange = new ArrayList<>();
                        List<JSONObject> addedJsons = new ArrayList<>(added.size());
                        for (Object o : added) {
                            if (o instanceof JSONObject) {
                                addedJsons.add((JSONObject) o);
                            }
                        }
                        ImportUtility.CreationOrder creationOrder = ImportUtility.getCreationOrder(addedJsons);
                        List<JSONObject> sortedAdded = creationOrder.getOrder();
                        Set<JSONObject> fail = creationOrder.getFailed();
                        if (sortedAdded != null) {
                            for (Object element : added) {
                                addElement((JSONObject) element, false);
                            }
                            // do a second pass to update relations in case relations are part of new elements
                            for (Object element : added) {
                                addElement((JSONObject) element, true);
                            }
                        }
                        if (!fail.isEmpty()) {
                            Map<String, Void> failedToCreate = failedChangelog.get(Changelog.ChangeType.CREATED);
                            for (JSONObject element : fail) {
                                Object o = element.get("sysmlid");
                                if (o instanceof String) {
                                    failedToCreate.put((String) o, null);
                                }
                            }
                        }

                        for (Object element : moved) {
                            if (element instanceof JSONObject) {
                                moveElement((JSONObject) element);
                            }
                        }
                        for (Object element : updated) {
                            if (element instanceof JSONObject) {
                                Map<String, Object> results = makeChange((JSONObject) element);
                                if (results != null) {
                                    toChange.add(results);
                                }
                            }
                        }
                        for (Object element : deleted) {
                            if (element instanceof JSONObject) {
                                deleteElement((JSONObject) element);
                            }
                        }

                        if (listener != null) {
                            listener.setDisabled(true);
                        }

                        sm.closeSession();
                        for (Map<String, Object> r : toChange) {
                            ImportHierarchy.sendChanges(r);
                            //OutputQueue.getInstance().offer(r);
                        }
                    } catch (Exception e) {
                        log.error(e, e);
                        if (SessionManager.getInstance().isSessionCreated()) {
                            sm.cancelSession();
                        }
                    } finally {
                        if (listener != null) {
                            listener.setDisabled(false);
                        }
                    }
                }

                private Map<String, Object> makeChange(JSONObject ob) {
                    Object o = ob.get("sysmlid");
                    if (!(o instanceof String)) {
                        return null;
                    }
                    String sysmlid = (String) o;
                    try {
                        Element changedElement = ExportUtility.getElementFromID(sysmlid);
                        if (changedElement == null) {
                            Utils.guilog("[ERROR - JMS] Element " + sysmlid + " was not found for JMS change.");
                            return null;
                        }
                        else if (!changedElement.isEditable()) {
                            // TODO Remove printing and implement caching change to apply on lock
                            Utils.guilog("[ERROR - JMS] Element " + changedElement.getHumanName() + " is not editable!");
                            failedChangelog.addChange(sysmlid, null, Changelog.ChangeType.UPDATED);
                            return null;
                        }
                        ImportUtility.updateElement(changedElement, ob);
                        Utils.guilog("[JMS] Element " + changedElement.getHumanName() + " updated.");
                        if ((o = ob.get("specialization")) instanceof JSONObject && (o = ((JSONObject) o).get("view2view")) instanceof JSONArray) {
                            JSONArray view2view = (JSONArray) o;
                            JSONObject web = ExportUtility.keyView2View(view2view);
                            DocumentGenerator dg = new DocumentGenerator(changedElement, null, null);
                            Document dge = dg.parseDocument(true, true, true);
                            ViewHierarchyVisitor vhv = new ViewHierarchyVisitor();
                            dge.accept(vhv);
                            JSONObject model = vhv.getView2View();
                            if (!ViewValidator.viewHierarchyMatch(changedElement, dge, vhv, (JSONObject) ob.get("specialization"))) {
                                Map<String, Object> result = ImportHierarchy.importHierarchy(changedElement, model, web);
                                if (result != null && (Boolean) result.get("success")) {
                                    Utils.guilog("[JMS] Document hierarchy updated for element " + changedElement.getHumanName());
                                    return result;
                                }
                                else {
                                    failedChangelog.addChange(sysmlid, null, Changelog.ChangeType.UPDATED);
                                }
                            }
                        }
                        return null;
                    } catch (Exception ex) {
                        log.error("", ex);
                        if (ex instanceof ImportException) {
                            Utils.guilog("[ERROR -- JMS] " + ex.getMessage());
                        }
                        failedChangelog.addChange(sysmlid, null, Changelog.ChangeType.UPDATED);
                        return null;
                    }
                }

                private void addElement(JSONObject ob, boolean updateRelations) {
                    try {
                        Element e = ImportUtility.createElement(ob, updateRelations);
                        if (e == null && updateRelations) {
                            Object o = ob.get("sysmlid");
                            if (o instanceof String) {
                                String sysmlid = (String) o;
                                Utils.guilog("[ERROR -- JMS] Creating element " + sysmlid + " failed."); // most likely owner not found
                                failedChangelog.addChange(sysmlid, null, Changelog.ChangeType.CREATED);
                            }
                        }
                        else if (e != null && updateRelations) {
                            Utils.guilog("[JMS] Element " + e.getHumanName() + " created.");
                        }
                    } catch (Exception ex) {
                        log.error("", ex);
                        if (ex instanceof ImportException) {
                            Utils.guilog("[ERROR -- JMS] " + ex.getMessage());
                        }
                        Object o = ob.get("sysmlid");
                        if (o instanceof String) {
                            failedChangelog.addChange((String) o, null, Changelog.ChangeType.CREATED);
                        }
                    }
                }

                private void deleteElement(JSONObject ob) {
                    Object o = ob.get("sysmlid");
                    if (!(o instanceof String)) {
                        return;
                    }
                    String sysmlid = (String) o;
                    Element changedElement = ExportUtility.getElementFromID(sysmlid);
                    if (changedElement == null) {
                        return;
                    }
                    if (!changedElement.isEditable()) {
                        Utils.guilog("[JMS] Element " + changedElement.getHumanName() + " is not editable.");
                        failedChangelog.addChange(sysmlid, null, Changelog.ChangeType.DELETED);
                        return;
                    }
                    try {
                        ModelElementsManager.getInstance().removeElement(changedElement);
                        Utils.guilog("[JMS] " + changedElement.getHumanName() + " deleted");
                    } catch (ReadOnlyElementException e) {
                        Utils.guilog("[ERROR - JMS] Sync: " + changedElement.getHumanName() + " is read-only.");
                        log.error("", e);
                        failedChangelog.addChange(sysmlid, null, Changelog.ChangeType.DELETED);
                    }
                }

                private void moveElement(JSONObject ob) {
                    Object o = ob.get("sysmlid");
                    if (!(o instanceof String)) {
                        return;
                    }
                    String sysmlid = (String) o;
                    try {
                        Element changedElement = ExportUtility.getElementFromID(sysmlid);
                        if (changedElement == null) {
                            Utils.guilog("[ERROR - JMS] Element " + sysmlid + " not found when attempting to move.");
                            return;
                        }
                        ImportUtility.setOwner(changedElement, ob);
                        Utils.guilog("[JMS] Element " + changedElement.getHumanName() + " moved.");
                    } catch (Exception ex) {
                        log.error("", ex);
                        failedChangelog.addChange(sysmlid, null, Changelog.ChangeType.UPDATED);
                    }
                }
            };
            project.getRepository().invokeAfterTransaction(runnable);
            message.acknowledge();
        } catch (Exception e) {
            log.error("", e);
        }
    }
}
