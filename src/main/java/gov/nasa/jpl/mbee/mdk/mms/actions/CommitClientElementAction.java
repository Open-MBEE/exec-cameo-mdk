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
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.mms.endpoints.MMSElementsEndpoint;
import gov.nasa.jpl.mbee.mdk.mms.endpoints.MMSEndpointBuilderConstants;
import gov.nasa.jpl.mbee.mdk.util.MDUtils;
import gov.nasa.jpl.mbee.mdk.util.TaskRunner;
import gov.nasa.jpl.mbee.mdk.validation.IRuleViolationAction;
import gov.nasa.jpl.mbee.mdk.validation.RuleViolationAction;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;

import javax.annotation.CheckForNull;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.text.NumberFormat;
import java.util.*;

/**
 * Created by igomes on 9/27/16.
 */
// TODO Abstract this and update to a common class @donbot
public class CommitClientElementAction extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {
    private static final String DEFAULT_ID = "Commit Element to MMS";

    private final String elementID;
    private final Element element;
    private final ObjectNode elementObjectNode;
    private final ObjectNode serverObjectNode;
    private final Project project;

    public CommitClientElementAction(String elementID, Element element, ObjectNode elementObjectNode, ObjectNode serverObjectNode, Project project) {
        super(DEFAULT_ID, DEFAULT_ID, null, null);
        this.elementID = elementID;
        this.element = element;
        this.elementObjectNode = elementObjectNode;
        this.serverObjectNode = serverObjectNode;
        this.project = project;
    }

    @Override
    public void execute(Collection<Annotation> annotations) {
        List<ObjectNode> elementsToUpdate = new LinkedList<>();
        List<String> elementsToDelete = new LinkedList<>();

        for (Annotation annotation : annotations) {
            for (NMAction action : annotation.getActions()) {
                if (action instanceof CommitClientElementAction) {
                    ObjectNode serverObjectNode = ((CommitClientElementAction) action).getServerObjectNode();
                    ObjectNode elementObjectNode = ((CommitClientElementAction) action).getElementObjectNode();
                    if (elementObjectNode != null) {
                        if (serverObjectNode != null && serverObjectNode.get(MDKConstants.CONTENTS_KEY) != null && !serverObjectNode.get(MDKConstants.CONTENTS_KEY).isEmpty())
                            elementObjectNode.set(MDKConstants.CONTENTS_KEY, serverObjectNode.get(MDKConstants.CONTENTS_KEY));
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

    public ObjectNode getServerObjectNode() {
        return serverObjectNode;
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
        String projectId = Converters.getIProjectToIdConverter().apply(project.getPrimaryProject());
        String refId = MDUtils.getBranchId(project);
        if (elementsToUpdate != null && !elementsToUpdate.isEmpty()) {
            Application.getInstance().getGUILog().log("[INFO] Queuing request to create/update " + NumberFormat.getInstance().format(elementsToUpdate.size()) + " element" + (elementsToUpdate.size() != 1 ? "s" : "") + " on MMS.");
            try {
                File file = MMSUtils.createEntityFile(CommitClientElementAction.class, ContentType.APPLICATION_JSON, elementsToUpdate, MMSUtils.JsonBlobType.ELEMENT_JSON);
                HashMap<String, String> uriBuilderParams = new HashMap<>();
                uriBuilderParams.put("overwrite", "true");
                HttpRequestBase elementsUpdateCreateRequest = MMSUtils.prepareEndpointBuilderBasicJsonPostRequest(MMSElementsEndpoint.builder(), project, file)
                        .addParam(MMSEndpointBuilderConstants.URI_PROJECT_SUFFIX, projectId)
                        .addParam(MMSEndpointBuilderConstants.URI_REF_SUFFIX, refId)
                        .addParam(MMSEndpointBuilderConstants.URI_BUILDER_PARAMETERS, uriBuilderParams)
                        .build();
                TaskRunner.runWithProgressStatus(progressStatus -> {
                    try {
                        MMSUtils.sendMMSRequest(project, elementsUpdateCreateRequest, progressStatus);
                    } catch (IOException | ServerException | URISyntaxException | GeneralSecurityException e) {
                        // TODO Implement error handling that was previously not possible due to OutputQueue implementation
                        e.printStackTrace();
                    }
                }, "Element Create/Update x" + NumberFormat.getInstance().format(elementsToUpdate.size()), true, TaskRunner.ThreadExecutionStrategy.SINGLE);
            } catch (IOException | URISyntaxException e) {
                Application.getInstance().getGUILog().log("[ERROR] Unexpected failure processing request. Reason: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }
        if (elementsToDelete != null && !elementsToDelete.isEmpty()) {
            Application.getInstance().getGUILog().log("[INFO] Queuing request to delete " + NumberFormat.getInstance().format(elementsToDelete.size()) + " element" + (elementsToDelete.size() != 1 ? "s" : "") + " on MMS.");
            try {
                File file = MMSUtils.createEntityFile(CommitClientElementAction.class, ContentType.APPLICATION_JSON, elementsToDelete, MMSUtils.JsonBlobType.ELEMENT_ID);
                HttpRequestBase elementsDeleteRequest = MMSUtils.prepareEndpointBuilderBasicJsonDeleteRequest(MMSElementsEndpoint.builder(), project, file)
                        .addParam(MMSEndpointBuilderConstants.URI_PROJECT_SUFFIX, projectId)
                        .addParam(MMSEndpointBuilderConstants.URI_REF_SUFFIX, refId).build();
                TaskRunner.runWithProgressStatus(progressStatus -> {
                    try {
                        MMSUtils.sendMMSRequest(project, elementsDeleteRequest, progressStatus);
                    } catch (IOException | ServerException | URISyntaxException | GeneralSecurityException e) {
                        // TODO Implement error handling that was previously not possible due to OutputQueue implementation
                        e.printStackTrace();
                    }
                }, "Element Delete x" + NumberFormat.getInstance().format(elementsToDelete.size()), true, TaskRunner.ThreadExecutionStrategy.SINGLE);
            } catch (IOException | URISyntaxException e) {
                Application.getInstance().getGUILog().log("[ERROR] Unexpected failure processing request. Reason: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }
    }
}
