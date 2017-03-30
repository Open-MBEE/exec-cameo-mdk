package gov.nasa.jpl.mbee.mdk.ems.actions;

/**
 * Created by ablack on 3/16/17.
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.ui.ProgressStatusRunner;
import gov.nasa.jpl.mbee.mdk.MDKPlugin;
import gov.nasa.jpl.mbee.mdk.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mbee.mdk.docgen.validation.RuleViolationAction;
import gov.nasa.jpl.mbee.mdk.ems.MMSUtils;
import gov.nasa.jpl.mbee.mdk.ems.ManualSyncActionRunner;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
import gov.nasa.jpl.mbee.mdk.ems.validation.BranchValidator;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;

public class CommitBranchAction extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    public static final String DEFAULT_ID = CreateMMSWorkspaceAction.class.getSimpleName();
    public static final String COMMIT_MODEL_DEFAULT_ID = DEFAULT_ID + "_Commit_Model";

    private final Project project;
    private final EsiUtils.EsiBranchInfo branchInfo;
    private final String branchName;
    private final boolean shouldCommitModel;
    private final boolean updateBranch;

    public CommitBranchAction(String branchName, Project project, EsiUtils.EsiBranchInfo branchInfo) {
        this(branchName, project, branchInfo, false, false);
    }

    public CommitBranchAction(String branchName, Project project, EsiUtils.EsiBranchInfo branchInfo, boolean shouldCommitModel, boolean updateBranch) {
        super(shouldCommitModel ? COMMIT_MODEL_DEFAULT_ID : DEFAULT_ID, "Commit Branch" + (shouldCommitModel ? " and Sync Model" : ""), null, null);
        this.branchName = branchName;
        this.project = project;
        this.branchInfo = branchInfo;
        this.shouldCommitModel = shouldCommitModel;
        this.updateBranch = false;
    }

    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return false;
    }

    @Override
    public void execute(Collection<Annotation> annos) {
        // do nothing
    }

    @Override
    public void actionPerformed(ActionEvent e) {
            commitAction();
        }

    public void commitAction() {
        URIBuilder requestUri = MMSUtils.getServiceProjectsRefsUri(project);
        if (requestUri == null) {
            Application.getInstance().getGUILog().log("[ERROR] Unable to get MMS refs url. Project commit cancelled.");
            return;
        }


        ObjectNode requestData = JacksonUtils.getObjectMapper().createObjectNode();
        ArrayNode elementsArrayNode = requestData.putArray("refs");
        requestData.put("source", "magicdraw");
        requestData.put("mdkVersion", MDKPlugin.VERSION);
        ObjectNode branchNode = BranchValidator.getRefObjectNode(project, branchInfo, updateBranch);
        elementsArrayNode.add(branchNode);

        ObjectNode response;
        try {
            HttpRequestBase request = MMSUtils.buildRequest(MMSUtils.HttpRequestType.POST, requestUri, requestData);
            response = MMSUtils.sendMMSRequest(project, request);
        } catch (IOException | URISyntaxException | ServerException e) {
            Application.getInstance().getGUILog().log("[ERROR] Exception occurred while posting branch. Reason: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        // do any response processing

        if (shouldCommitModel) {
            RunnableWithProgress temp = new ManualSyncActionRunner<>(CommitClientElementAction.class, Collections.singletonList(project.getPrimaryModel()), project, -1);
            ProgressStatusRunner.runWithProgressStatus(temp, "Model Initialization", true, 0);
        }
    }

    @Override
    public void updateState() {
        String currentBranch = EsiUtils.getCurrentBranch(project.getPrimaryProject()).getName();
        if (currentBranch.equals("trunk")) {
            currentBranch = "master";
        }
        setEnabled(!shouldCommitModel || branchName.equals(currentBranch));
    }
}
