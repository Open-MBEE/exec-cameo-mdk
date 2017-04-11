package gov.nasa.jpl.mbee.mdk.mms.actions;

/**
 * Created by ablack on 3/16/17.
 */

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.ui.ProgressStatusRunner;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.mms.sync.manual.ManualSyncActionRunner;
import gov.nasa.jpl.mbee.mdk.mms.validation.BranchValidator;
import gov.nasa.jpl.mbee.mdk.validation.IRuleViolationAction;
import gov.nasa.jpl.mbee.mdk.validation.RuleViolationAction;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class CommitBranchAction extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    public static final String DEFAULT_ID = CommitBranchAction.class.getSimpleName();
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

        Collection<ObjectNode> refsNodes = new LinkedList<>();
        ObjectNode branchNode = BranchValidator.getRefObjectNode(project, branchInfo, updateBranch);
        refsNodes.add(branchNode);

        File responseFile;
        try {
            File sendFile = MMSUtils.createEntityFile(this.getClass(), ContentType.APPLICATION_JSON, refsNodes, MMSUtils.JsonBlobType.REF);
            HttpRequestBase request = MMSUtils.buildRequest(MMSUtils.HttpRequestType.POST, requestUri, sendFile, ContentType.APPLICATION_JSON);
            responseFile = MMSUtils.sendMMSRequest(project, request);
        } catch (IOException | URISyntaxException | ServerException e) {
            Application.getInstance().getGUILog().log("[ERROR] Exception occurred while posting branch. Reason: " + e.getMessage());
            e.printStackTrace();
            return;
        }
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
