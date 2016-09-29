package gov.nasa.jpl.mbee.mdk.ems.actions;

import com.nomagic.actions.NMAction;
import com.nomagic.documentmodeling.mbee.DocGenPlugin;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.MDKPlugin;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mbee.mdk.docgen.validation.RuleViolationAction;
import gov.nasa.jpl.mbee.mdk.ems.ExportUtility;
import gov.nasa.jpl.mbee.mdk.ems.sync.queue.OutputQueue;
import gov.nasa.jpl.mbee.mdk.ems.sync.queue.Request;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.annotation.CheckForNull;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by igomes on 9/27/16.
 */
// TODO Abstract this and update to a common class @donbot
public class CommitClientElementAction extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {
    private static final String NAME = "Commit Element to MMS";

    private final String id;
    private final Element element;
    private final JSONObject elementJson;
    private final Project project;

    public CommitClientElementAction(String id, Element element, JSONObject elementJson, Project project) {
        super(NAME, NAME, null, null);
        this.id = id;
        this.element = element;
        this.elementJson = elementJson;
        this.project = project;
    }

    @Override
    public void execute(Collection<Annotation> annotations) {
        List<JSONObject> elementsToUpdate = new ArrayList<>(annotations.size());
        List<String> elementsToDelete = new ArrayList<>(annotations.size());

        for (Annotation annotation : annotations) {
            for (NMAction action : annotation.getActions()) {
                if (action instanceof CommitClientElementAction) {
                    JSONObject jsonObject = ((CommitClientElementAction) action).getElementJson();
                    if (jsonObject != null) {
                        elementsToUpdate.add(jsonObject);
                    }
                    else {
                        elementsToDelete.add(id);
                    }
                    break;
                }
            }
        }
        CommitClientElementAction.request(elementsToUpdate, elementsToDelete, project);
    }

    @Override
    public boolean canExecute(Collection<Annotation> annotations) {
        return true;
    }

    public Element getElement() {
        return element;
    }

    public JSONObject getElementJson() {
        return elementJson;
    }

    @Override
    public void actionPerformed(@CheckForNull ActionEvent actionEvent) {
        List<JSONObject> elementsToUpdate = new ArrayList<>(1);
        List<String> elementsToDelete = new ArrayList<>(1);
        if (elementJson != null) {
            elementsToUpdate.add(elementJson);
        }
        else {
            elementsToDelete.add(id);
        }
        CommitClientElementAction.request(elementsToUpdate, elementsToDelete, project);
    }

    private static void request(List<JSONObject> elementsToUpdate, List<String> elementsToDelete, Project project) {
        if (elementsToUpdate != null && !elementsToUpdate.isEmpty()) {
            Application.getInstance().getGUILog().log("[INFO] Queueing request to create/update " + elementsToUpdate.size() + " element" + (elementsToUpdate.size() != 1 ? "s" : "") + " on MMS.");
            JSONObject request = new JSONObject();
            JSONArray elements = new JSONArray();
            elements.addAll(elementsToUpdate);
            request.put("elements", elements);
            request.put("source", "magicdraw");
            request.put("mmsVersion", MDKPlugin.VERSION);
            OutputQueue.getInstance().offer((new Request(ExportUtility.getPostElementsUrl(), request.toJSONString(), "POST", true, elements.size(), "Sync Changes")));
        }
        if (elementsToDelete != null && !elementsToDelete.isEmpty()) {
            Application.getInstance().getGUILog().log("[INFO] Queueing request to delete " + elementsToDelete.size() + " element" + (elementsToDelete.size() != 1 ? "s" : "") + " on MMS.");
            JSONObject request = new JSONObject();
            JSONArray elements = new JSONArray();
            for (String id : elementsToDelete) {
                JSONObject element = new JSONObject();
                element.put(MDKConstants.SYSML_ID_KEY, id);
                elements.add(element);
            }
            request.put("elements", elements);
            request.put("source", "magicdraw");
            request.put("mmsVersion", MDKPlugin.VERSION);
            OutputQueue.getInstance().offer(new Request(ExportUtility.getUrlWithWorkspace() + "/elements", request.toJSONString(), "DELETEALL", true, elements.size(), "Sync Deletes"));
        }
    }
}
