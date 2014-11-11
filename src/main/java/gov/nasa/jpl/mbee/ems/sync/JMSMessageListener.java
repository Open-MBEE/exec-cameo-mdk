package gov.nasa.jpl.mbee.ems.sync;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.ImportUtility;
import gov.nasa.jpl.mbee.ems.validation.actions.ImportHierarchy;
import gov.nasa.jpl.mbee.generator.DocumentGenerator;
import gov.nasa.jpl.mbee.model.Document;
import gov.nasa.jpl.mbee.viewedit.ViewHierarchyVisitor;

import java.util.List;
import java.util.Map;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nomagic.magicdraw.core.Application;
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

    @Override
    public void onMessage(Message msg) {
        try {
            // Take the incoming message and parse it into a
            // JSONObject.
            //
            TextMessage message = (TextMessage) msg;
            JSONObject ob = (JSONObject) JSONValue.parse(message.getText());

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
                        // Loop through each specified element.
                        //
                        List<JSONObject> sortedAdded = ImportUtility.getCreationOrder((List<JSONObject>)added);
                        if (sortedAdded != null) {
                            for (Object element : added) {
                                addElement((JSONObject) element);
                            }
                        } else {
                            log.error("jms message added can't be executed - " + added.toJSONString());
                        }
                        for (Object element : updated) {
                            makeChange((JSONObject) element);
                        }
                        
                        for (Object element : deleted) {
                            deleteElement((JSONObject) element);
                        }
                        for (Object element : moved) {
                            moveElement((JSONObject) element);
                        }
                        if (listener != null)
                            listener.disable();

                        sm.closeSession();
                        if (listener != null)
                            listener.enable();
                    }
                    catch (Exception e) {
                        sm.cancelSession();
                        log.error(e);
                        if (listener != null)
                            listener.enable();
                    }

                    // Once we've completed make all the
                    // changes, enable the listener, duplicated everywhere seems like some timing issue/bug isn't always reenabling it
                    //
                    if (listener != null)
                        listener.enable();
                }

                private void makeChange(JSONObject ob) {
                    String sysmlid = (String) ob.get("sysmlid");
                    Element changedElement = ExportUtility.getElementFromID(sysmlid);
                    if (changedElement == null) {
                        Application.getInstance().getGUILog().log("element " + sysmlid + " not found from mms sync change");
                        return;
                    } else if (!changedElement.isEditable()) {
                        if (!TeamworkUtils.lockElement(project, changedElement, false)) {
                            Application.getInstance().getGUILog()
                                .log("[ERROR] Sync: " + changedElement.getID() + " is not editable!");
                            return;
                        }
                    }
                    if (ob.containsKey("specialization")) {
                        JSONArray view2view = (JSONArray)((JSONObject)ob.get("specialization")).get("view2view");
                        if (view2view != null) {
                            JSONObject web = ExportUtility.keyView2View(view2view);
                            DocumentGenerator dg = new DocumentGenerator(changedElement, null, null);
                            Document dge = dg.parseDocument(true, true);
                            ViewHierarchyVisitor vhv = new ViewHierarchyVisitor();
                            dge.accept(vhv);
                            JSONObject model = vhv.getView2View();
                            ImportHierarchy.importHierarchy(changedElement, model, web);
                        }
                    }
                    ImportUtility.updateElement(changedElement, ob);
                }

                private void addElement(JSONObject ob) {
                    ImportUtility.createElement(ob);
                }

                private void deleteElement(JSONObject ob) {
                    String sysmlid = (String) ob.get("sysmlid");
                    Element changedElement = ExportUtility.getElementFromID(sysmlid);
                    if (changedElement == null) {
                        Application.getInstance().getGUILog().log("element " + sysmlid + " not found from mms sync delete");
                        return;
                    }
                    if (!changedElement.isEditable())
                        TeamworkUtils.lockElement(project, changedElement, false);
                    try {
                        ModelElementsManager.getInstance().removeElement(changedElement);
                    } catch (ReadOnlyElementException e) {
                        Application.getInstance().getGUILog()
                        .log("[ERROR] Sync: " + changedElement.getID() + " cannot be deleted!");
                    }
                }

                private void moveElement(JSONObject ob) {
                    String sysmlid = (String) ob.get("sysmlid");
                    Element changedElement = ExportUtility.getElementFromID(sysmlid);
                    if (changedElement == null) {
                        Application.getInstance().getGUILog().log("element " + sysmlid + " not found from mms sync move");
                        return;
                    }
                    ImportUtility.setOwner(changedElement, ob);
                }
            };
            project.getRepository().invokeAfterTransaction(runnable);
            message.acknowledge();

        }
        catch (Exception e) {

        }
    }
}
