package gov.nasa.jpl.mbee.mdk.mms.actions;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.ui.ProgressStatusRunner;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.mms.sync.manual.ManualSyncActionRunner;
import gov.nasa.jpl.mbee.mdk.mms.validation.ProjectValidator;
import gov.nasa.jpl.mbee.mdk.util.Pair;
import gov.nasa.jpl.mbee.mdk.validation.IRuleViolationAction;
import gov.nasa.jpl.mbee.mdk.validation.RuleViolationAction;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class CommitProjectAction extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    public static final String DEFAULT_ID = CommitProjectAction.class.getSimpleName();
    public static final String COMMIT_MODEL_DEFAULT_ID = DEFAULT_ID + "_Commit_Model";
    public static final String NEW_ORG_VALUE = "*new*";

    private final Project project;
    private final boolean shouldCommitModel;

    public CommitProjectAction(Project project, boolean shouldCommitModel) {
        this(project, shouldCommitModel, false);
    }

    public CommitProjectAction(Project project, boolean shouldCommitModel, boolean isDeveloperAction) {
        super(shouldCommitModel ? COMMIT_MODEL_DEFAULT_ID : DEFAULT_ID, (isDeveloperAction ? "[DEVELOPER] " : "") + "Commit Project" + (shouldCommitModel ? " and Model" : ""), null, null);
        this.project = project;
        this.shouldCommitModel = shouldCommitModel;
    }

    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return false;
    }

    @Override
    public void execute(Collection<Annotation> annos) {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        commitAction();
    }

    public String commitAction() {
        // '{"elements": [{"sysmlId": "123456", "name": "vetest", "type": "Project"}]}' -X POST "http://localhost:8080/alfresco/service/orgs/vetest/projects"
        String orgId;

        // get orgs uri to check orgs / post project
        URIBuilder requestUri = MMSUtils.getServiceOrgsUri(project);
        if (requestUri == null) {
            Application.getInstance().getGUILog().log("[ERROR] Unable to get MMS orgs url. Project commit cancelled.");
            return null;
        }

        // check for existing org, use that if it exists instead of prompting to select one
        try {
            orgId = MMSUtils.getMmsOrg(project);
            // a null result here just means the project isn't on mms
        } catch (IOException | URISyntaxException | ServerException e1) {
            Application.getInstance().getGUILog().log("[ERROR] An error occurred while checking for project org on MMS. Project commit cancelled. Reason: " + e1.getMessage());
            e1.printStackTrace();
            return null;
        }

        // check for org options if one isn't specified in the project already
        ObjectNode response;
        if (orgId == null || orgId.isEmpty()) {
            try {
                File responseFile = MMSUtils.sendMMSRequest(project, MMSUtils.buildRequest(MMSUtils.HttpRequestType.GET, requestUri));
                try (JsonParser jsonParser = JacksonUtils.getJsonFactory().createParser(responseFile)) {
                    response = JacksonUtils.parseJsonObject(jsonParser);
                }
            } catch (IOException | URISyntaxException | ServerException e1) {
                Application.getInstance().getGUILog().log("[ERROR] An error occurred while getting MMS orgs. Project commit cancelled. Reason: " + e1.getMessage());
                e1.printStackTrace();
                return null;
            }
            List<Pair<String, String>> mmsOrgs = new ArrayList<>();
            if (response != null) {
                JsonNode arrayNode;
                if ((arrayNode = response.get("orgs")) != null && arrayNode.isArray()) {
                    for (JsonNode orgNode : arrayNode) {
                        JsonNode name, id;
                        if ((name = orgNode.get(MDKConstants.NAME_KEY)) != null && name.isTextual() && !name.asText().isEmpty()
                                && (id = orgNode.get(MDKConstants.ID_KEY)) != null && id.isTextual() && !id.asText().isEmpty()) {
                            mmsOrgs.add(new Pair<String, String>(id.asText(), name.asText()) {
                                @Override
                                public String toString() {
                                    return this.getValue() + (mmsOrgs.stream().filter(pair -> pair.getValue().equals(this.getValue())).count() > 1 ? " (" + this.getKey() + ")" : "");
                                }
                            });
                        }
                    }
                }
            }
            mmsOrgs.sort(Comparator.comparing(Pair::toString));
            mmsOrgs.add(new Pair<String, String>(NEW_ORG_VALUE, "New...") {
                @Override
                public String toString() {
                    return this.getValue();
                }
            });
            JFrame selectionDialog = new JFrame();
            Pair<String, String> selection = (Pair<String, String>) JOptionPane.showInputDialog(selectionDialog, "Which MMS org should this project be created under?",
                    "Choose MMS Org", JOptionPane.QUESTION_MESSAGE, null, mmsOrgs.toArray(), mmsOrgs.get(0));
            if (selection != null) {
                orgId = selection.getKey();
                // trigger commit org action if user selects create new org
                if (orgId.equals(NEW_ORG_VALUE)) {
                    orgId = new CommitOrgAction(project).commitAction();
                }
            }
        }

        if (orgId == null || orgId.isEmpty()) {
            Application.getInstance().getGUILog().log("[ERROR] Unable to commit project without an org. Project commit cancelled.");
            return null;
        }

        // update request with project post path
        requestUri.setPath(requestUri.getPath() + "/" + orgId + "/projects");
        Collection<ObjectNode> projects = new LinkedList<>();
        projects.add(ProjectValidator.generateProjectObjectNode(project));

        // do project post request
        try {
            File sendData = MMSUtils.createEntityFile(this.getClass(), ContentType.APPLICATION_JSON, projects, MMSUtils.JsonBlobType.PROJECT);
            File responseFile = MMSUtils.sendMMSRequest(project, MMSUtils.buildRequest(MMSUtils.HttpRequestType.POST, requestUri, sendData, ContentType.APPLICATION_JSON));
            try (JsonParser jsonParser = JacksonUtils.getJsonFactory().createParser(responseFile)) {
                response = JacksonUtils.parseJsonObject(jsonParser);
            }
            // we don't need to process this response, just make sure the request comes back without exception
            if (response != null) {
                // crude method of waiting for project post to propagate
                Thread.sleep(5000);
            }
        } catch (IOException | URISyntaxException | ServerException | InterruptedException e1) {
            Application.getInstance().getGUILog().log("[ERROR] An error occurred while posting project to MMS. Project commit cancelled. Reason: " + e1.getMessage());
            e1.printStackTrace();
            return null;
        }
        // update master ref
        // TODO @donbot enable this ref post after master is updatable
//        new CommitBranchAction("master", project, EsiUtils.getCurrentBranch(project.getPrimaryProject()), false).commitAction();
        // do model post
        if (shouldCommitModel) {
            RunnableWithProgress temp = new ManualSyncActionRunner<>(CommitClientElementAction.class, Collections.singletonList(project.getPrimaryModel()), project, -1);
            ProgressStatusRunner.runWithProgressStatus(temp, "Model Initialization", true, 0);
        }
        return Converters.getIProjectToIdConverter().apply(project.getPrimaryProject());
    }
}

