package gov.nasa.jpl.mbee.mdk.ems.actions;

/**
 * Created by ablack on 3/16/17.
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import gov.nasa.jpl.mbee.mdk.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mbee.mdk.docgen.validation.RuleViolationAction;
import gov.nasa.jpl.mbee.mdk.ems.MMSUtils;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

public class CommitBranchAction extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    public static final String DEFAULT_ID = CommitProjectAction.class.getSimpleName();
    public static final String COMMIT_MODEL_DEFAULT_ID = DEFAULT_ID + "_Commit_Model";

    private final Project project;
    private final boolean shouldCommitModel;

    public CommitBranchAction(Project project) {
        this(project, false);
    }

    public CommitBranchAction(Project project, boolean shouldCommitModel) {
        super(shouldCommitModel ? COMMIT_MODEL_DEFAULT_ID : DEFAULT_ID, "Commit Project" + (shouldCommitModel ? " and Model" : ""), null, null);
        this.project = project;
        this.shouldCommitModel = shouldCommitModel;
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

        ObjectNode refsNode = MMSUtils.getRefObjectNode(project);
        HttpRequestBase request;
        ObjectNode response;

        try {
            request = MMSUtils.buildRequest(MMSUtils.HttpRequestType.POST, requestUri, refsNode);
            response = MMSUtils.sendMMSRequest(project, request);
        } catch (IOException | URISyntaxException | ServerException e) {
            Application.getInstance().getGUILog().log("[ERROR] Exception occurred while posting branch. Reason: " + e.getMessage());
            e.printStackTrace();
            return;
        }

    }
}
