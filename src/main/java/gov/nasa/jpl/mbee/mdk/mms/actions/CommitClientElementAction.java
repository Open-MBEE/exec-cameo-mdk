package gov.nasa.jpl.mbee.mdk.mms.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.mms.sync.queue.OutputQueue;
import gov.nasa.jpl.mbee.mdk.mms.sync.queue.Request;
import gov.nasa.jpl.mbee.mdk.validation.IRuleViolationAction;
import gov.nasa.jpl.mbee.mdk.validation.RuleViolationAction;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;

import javax.annotation.CheckForNull;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by igomes on 9/27/16.
 */
// TODO Abstract this and update to a common class @donbot
public class CommitClientElementAction extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {
    private static final String DEFAULT_ID = "Commit Element to MMS";
    private static final int COMMIT_ELEMENT_COUNT_THRESHOLD = 10;

    private final String elementID;
    private final Element element;
    private final ObjectNode elementObjectNode;
    private final Project project;

    public CommitClientElementAction(String elementID, Element element, ObjectNode elementObjectNode, Project project) {
        super(DEFAULT_ID, DEFAULT_ID, null, null);
        this.elementID = elementID;
        this.element = element;
        this.elementObjectNode = elementObjectNode;
        this.project = project;
    }

    @Override
    public void execute(Collection<Annotation> annotations) {
        List<ObjectNode> elementsToUpdate = new LinkedList<>();
        List<String> elementsToDelete = new LinkedList<>();

        for (Annotation annotation : annotations) {
            for (NMAction action : annotation.getActions()) {
                if (action instanceof CommitClientElementAction) {
                    ObjectNode elementObjectNode = ((CommitClientElementAction) action).getElementObjectNode();
                    if (elementObjectNode != null) {
                        elementsToUpdate.add(elementObjectNode);
                    }
                    else if (elementID.startsWith(MDKConstants.HOLDING_BIN_ID_PREFIX)) {
                        Application.getInstance().getGUILog().log("[INFO] Skipping deletion of Holding Bin from MMS.");
                    }
                    else {
                        elementsToDelete.add(((CommitClientElementAction) action).getElementID());
                    }
                    break;
                }
            }
        }
        try {
            CommitClientElementAction.request(elementsToUpdate, elementsToDelete, project);
        } catch (JsonProcessingException e) {
            Application.getInstance().getGUILog().log("[ERROR]: Exception occurred committing element to server. Commit aborted.");
            e.printStackTrace();
        }
    }

    @Override
    public boolean canExecute(Collection<Annotation> annotations) {
        return true;
    }

    public String getElementID() {
        return elementID;
    }

    public Element getElement() {
        return element;
    }

    public ObjectNode getElementObjectNode() {
        return elementObjectNode;
    }

    @Override
    public void actionPerformed(@CheckForNull ActionEvent actionEvent) {
        List<ObjectNode> elementsToUpdate = new ArrayList<>(1);
        List<String> elementsToDelete = new ArrayList<>(1);
        if (elementObjectNode != null) {
            elementsToUpdate.add(elementObjectNode);
        }
        else {
            elementsToDelete.add(elementID);
        }
        try {
            CommitClientElementAction.request(elementsToUpdate, elementsToDelete, project);
        } catch (JsonProcessingException e) {
            Application.getInstance().getGUILog().log("[ERROR]: Exception occurred committing element to server. Commit aborted.");
            e.printStackTrace();
        }
    }

    private static void request(List<ObjectNode> elementsToUpdate, List<String> elementsToDelete, Project project) throws JsonProcessingException {
        if (elementsToUpdate != null && !elementsToUpdate.isEmpty()) {
            Application.getInstance().getGUILog().log("[INFO] Queueing request to create/update " + elementsToUpdate.size() + " element" + (elementsToUpdate.size() != 1 ? "s" : "") + " on MMS.");
            int requestCapacity = elementsToUpdate.size() > COMMIT_ELEMENT_COUNT_THRESHOLD ? (elementsToUpdate.size() / COMMIT_ELEMENT_COUNT_THRESHOLD + (elementsToUpdate.size() % COMMIT_ELEMENT_COUNT_THRESHOLD != 0 ? 1 : 0)) * 2 : 1;
            List<Request> requests = new ArrayList<>(requestCapacity);
            List<ObjectNode> elementsToPost = new ArrayList<>(Math.min(elementsToUpdate.size(), COMMIT_ELEMENT_COUNT_THRESHOLD));
            for (int i = 0; i < elementsToUpdate.size(); i++) {
                elementsToPost.add(elementsToUpdate.get(i));
                // send requests in chunks if above threshold
                if (elementsToPost.size() == COMMIT_ELEMENT_COUNT_THRESHOLD || i + 1 == elementsToUpdate.size()) {
                    try {
                        File file = MMSUtils.createEntityFile(CommitClientElementAction.class, ContentType.APPLICATION_JSON, elementsToPost, MMSUtils.JsonBlobType.ELEMENT_JSON);
                        if (elementsToUpdate.size() > COMMIT_ELEMENT_COUNT_THRESHOLD) {
                            int requestIndex = (i + 1) / COMMIT_ELEMENT_COUNT_THRESHOLD + ((i + 1) % COMMIT_ELEMENT_COUNT_THRESHOLD != 0 ? 1 : 0) - 1;
                            URIBuilder requestUri = MMSUtils.getServiceProjectsRefsElementsUri(project);
                            if (requestUri == null) {
                                throw new IOException();
                            }
                            requestUri.addParameter("nodes", Boolean.toString(true));
                            //requestUri.addParameter("edges", Boolean.toString(false));
                            requests.add(requestIndex, new Request(project, MMSUtils.HttpRequestType.POST, requestUri, file, ContentType.APPLICATION_JSON, true, elementsToPost.size(),
                                    "Sync Changes - Nodes - " + NumberFormat.getInstance().format(requestIndex + 1) + " / " + (requestCapacity / 2)));
                            requestUri = MMSUtils.getServiceProjectsRefsElementsUri(project);
                            if (requestUri == null) {
                                throw new IOException();
                            }
                            //requestUri.addParameter("nodes", Boolean.toString(false));
                            requestUri.addParameter("edges", Boolean.toString(true));
                            requests.add(new Request(project, MMSUtils.HttpRequestType.POST, requestUri, file, ContentType.APPLICATION_JSON, true, elementsToPost.size(),
                                    "Sync Changes - Edges - " + NumberFormat.getInstance().format(requestIndex + 1) + " / " + (requestCapacity / 2)));
                        }
                        else {
                            URIBuilder requestUri = MMSUtils.getServiceProjectsRefsElementsUri(project);
                            requests.add(new Request(project, MMSUtils.HttpRequestType.POST, requestUri, file, ContentType.APPLICATION_JSON, true, elementsToPost.size(), "Sync Changes"));
                        }
                    } catch (IOException | URISyntaxException e) {
                        Application.getInstance().getGUILog().log("[ERROR] Unexpected failure processing request. Reason: " + e.getMessage());
                        e.printStackTrace();
                        return;
                    }
                    if (i + 1 != elementsToUpdate.size()) {
                        elementsToPost = new ArrayList<>(Math.min(elementsToUpdate.size() - (i + 1), COMMIT_ELEMENT_COUNT_THRESHOLD));
                    }
                }
            }
            requests.forEach(request -> OutputQueue.getInstance().offer(request));
        }
        if (elementsToDelete != null && !elementsToDelete.isEmpty()) {
            Application.getInstance().getGUILog().log("[INFO] Queueing request to delete " + elementsToDelete.size() + " element" + (elementsToDelete.size() != 1 ? "s" : "") + " on MMS.");
            try {
                File file = MMSUtils.createEntityFile(CommitClientElementAction.class, ContentType.APPLICATION_JSON, elementsToDelete, MMSUtils.JsonBlobType.ELEMENT_ID);
                URIBuilder requestUri = MMSUtils.getServiceProjectsRefsElementsUri(project);
                OutputQueue.getInstance().offer((new Request(project, MMSUtils.HttpRequestType.DELETE, requestUri, file, ContentType.APPLICATION_JSON, true, elementsToDelete.size(), "Sync Changes")));
            } catch (IOException | URISyntaxException e) {
                Application.getInstance().getGUILog().log("[ERROR] Unexpected failure processing request. Reason: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }
    }
}
