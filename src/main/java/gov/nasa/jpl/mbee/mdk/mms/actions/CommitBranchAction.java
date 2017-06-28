package gov.nasa.jpl.mbee.mdk.mms.actions;

/**
 * Created by ablack on 3/16/17.
 */

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.ci.persistence.versioning.IVersionDescriptor;
import com.nomagic.esi.core.msg.info.impl.BranchInfoImpl;
import com.nomagic.esi.core.msg.info.impl.CommitInfoImpl;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.core.project.ProjectDescriptorsFactory;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.ui.ProgressStatusRunner;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.mms.sync.manual.ManualSyncRunner;
import gov.nasa.jpl.mbee.mdk.mms.validation.BranchValidator;
import gov.nasa.jpl.mbee.mdk.validation.IRuleViolationAction;
import gov.nasa.jpl.mbee.mdk.validation.RuleViolationAction;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class CommitBranchAction extends RuleViolationAction implements AnnotationAction, IRuleViolationAction, RunnableWithProgress {

    public static final String DEFAULT_ID = CommitBranchAction.class.getSimpleName();
    public static final String VALIDATE_MODEL_DEFAULT_ID = DEFAULT_ID + "_Validate_Model";

    private final Project project;
    private final EsiUtils.EsiBranchInfo branchInfo;
    private final String branchName;
    private final boolean validateModel;

    public CommitBranchAction(String branchName, Project project, EsiUtils.EsiBranchInfo branchInfo) {
        this(branchName, project, branchInfo, false);
    }

    public CommitBranchAction(String branchName, Project project, EsiUtils.EsiBranchInfo branchInfo, boolean validateModel) {
        super(validateModel ? VALIDATE_MODEL_DEFAULT_ID : DEFAULT_ID, "Commit Branch" + (validateModel ? " and Validate Model" : ""), null, null);
        this.branchName = branchName;
        this.project = project;
        this.branchInfo = branchInfo;
        this.validateModel = validateModel;
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
        ProgressStatusRunner.runWithProgressStatus(this, "Commit Branch", true, 0);
    }

    @Override
    public void run(ProgressStatus progressStatus) {
        BranchInfoImpl branchInfoImpl = toBranchInfoImpl(branchInfo);
        if (branchInfoImpl == null) {
            Application.getInstance().getGUILog().log("[ERROR] Current branch not found. Branch commit aborted.");
            return;
        }

        ProjectDescriptor projectDescriptor = ProjectDescriptorsFactory.createAnyRemoteProjectDescriptor(project);
        ProjectDescriptor branchDescriptor = EsiUtils.getDescriptorByBranchID(projectDescriptor, branchInfoImpl.getID());
        List<CommitInfoImpl> branchCommits = toCommitInfoImpls(EsiUtils.getVersions(branchDescriptor));

        long startRevisionCommitId = branchInfoImpl.getStartRevision();
        CommitInfoImpl startRevisionCommitInfo = branchCommits.stream().filter(version -> startRevisionCommitId == version.getID()).findAny().orElse(null);
        if (startRevisionCommitInfo == null) {
            Application.getInstance().getGUILog().log("[ERROR] Unable to find start revision commit info. Branch commit aborted.");
            return;
        }
        UUID parentBranchUuid = startRevisionCommitInfo.getBranchID();
        EsiUtils.EsiBranchInfo parentBranchInfo = EsiUtils.getBranches(projectDescriptor).stream().filter(branch -> branch.getID().equals(parentBranchUuid)).findAny().orElse(null);
        if (parentBranchInfo == null) {
            Application.getInstance().getGUILog().log("[ERROR] Unable to find parent branch info. Branch commit aborted.");
            return;
        }
        String parentBranchId = parentBranchUuid.toString();
        String parentBranchName = parentBranchInfo.getName();
        if (parentBranchInfo.getName().equals("trunk")) {
            parentBranchId = "master";
            parentBranchName = "master";
        }

        int parentCommitsBehind = 0;
        long latestRevisionCommitId = branchInfoImpl.getLatestRevision();
        CommitInfoImpl latestRevisionCommitInfo = branchCommits.stream().filter(version -> latestRevisionCommitId == version.getID()).findAny().orElse(null);
        if (latestRevisionCommitInfo == null) {
            Application.getInstance().getGUILog().log("[ERROR] Unable to find latest revision commit info of current branch. Branch commit aborted.");
            return;
        }
        CommitInfoImpl commit = latestRevisionCommitInfo;
        while (commit != null && commit.getID() != startRevisionCommitId) {
            parentCommitsBehind++;
            long directParent = commit.getDirectParent();
            commit = branchCommits.stream().filter(version -> directParent == version.getID()).findAny().orElse(null);
        }
        // This is needed since on branch init TWC automatically creates a commit. This presumably doesn't make any element changes, so it shouldn't be necessary to manually sync.
        parentCommitsBehind--;

        int parentCommitsAhead = 0;
        ProjectDescriptor parentBranchDescriptor = EsiUtils.getDescriptorByBranchID(projectDescriptor, parentBranchUuid);
        List<CommitInfoImpl> parentBranchCommits = toCommitInfoImpls(EsiUtils.getVersions(parentBranchDescriptor));
        BranchInfoImpl parentBranchInfoImpl = toBranchInfoImpl(parentBranchInfo);
        if (parentBranchInfoImpl == null) {
            Application.getInstance().getGUILog().log("[ERROR] Parent branch not found. Branch commit aborted.");
        }
        long parentLatestRevisionCommitId = parentBranchInfoImpl.getLatestRevision();
        CommitInfoImpl parentLatestRevisionCommitInfo = parentBranchCommits.stream().filter(version -> parentLatestRevisionCommitId == version.getID()).findAny().orElse(null);
        if (parentLatestRevisionCommitInfo == null) {
            Application.getInstance().getGUILog().log("[ERROR] Unable to find latest revision commit info of parent branch. Branch commit aborted.");
            return;
        }
        commit = parentLatestRevisionCommitInfo;
        while (commit != null && commit.getID() != startRevisionCommitId) {
            parentCommitsAhead++;
            long directParent = commit.getDirectParent();
            commit = parentBranchCommits.stream().filter(version -> directParent == version.getID()).findAny().orElse(null);
        }

        JsonNode parentBranchJsonNode = null;
        URIBuilder requestUri = MMSUtils.getServiceProjectsRefsUri(project);
        if (requestUri == null) {
            Application.getInstance().getGUILog().log("[ERROR] Unable to get MMS refs URL. Branch commit aborted.");
            return;
        }
        requestUri.setPath(requestUri.getPath() + "/" + parentBranchId);
        try {
            HttpRequestBase request = MMSUtils.buildRequest(MMSUtils.HttpRequestType.GET, requestUri);
            File responseFile = MMSUtils.sendMMSRequest(project, request);
            ObjectNode response;
            try (JsonParser jsonParser = JacksonUtils.getJsonFactory().createParser(responseFile)) {
                response = JacksonUtils.parseJsonObject(jsonParser);
            }
            JsonNode refsArray, value;
            if ((refsArray = response.get("refs")) != null && refsArray.isArray()) {
                for (JsonNode refJson : refsArray) {
                    if (refJson.isObject()) {
                        ObjectNode refObjectNode = (ObjectNode) refJson;
                        refObjectNode.remove(MDKConstants.PARENT_REF_ID_KEY);
                        String entryKey;
                        if ((value = refObjectNode.get(MDKConstants.ID_KEY)) != null && value.isTextual()) {
                            entryKey = refObjectNode.get(MDKConstants.ID_KEY).asText();
                            if (entryKey.equals(parentBranchId)) {
                                parentBranchJsonNode = refObjectNode;
                            }
                        }
                    }
                }
            }
        } catch (IOException | URISyntaxException | ServerException e) {
            e.printStackTrace();
            Application.getInstance().getGUILog().log("[ERROR] An error occurred while getting MMS branches. Branch commit aborted. Reason: " + e.getMessage());
            return;
        }
        if (parentBranchJsonNode == null) {
            Application.getInstance().getGUILog().log("[ERROR] Parent branch (" + parentBranchName + ") does not exist on MMS. Please commit that one first. Branch commit aborted.");
            return;
        }

        requestUri = MMSUtils.getServiceProjectsRefsUri(project);
        if (requestUri == null) {
            Application.getInstance().getGUILog().log("[ERROR] Unable to get MMS refs url. Branch commit aborted.");
            return;
        }

        Collection<ObjectNode> refsNodes = new LinkedList<>();
        ObjectNode branchNode = BranchValidator.generateRefObjectNode(project, branchInfo, parentBranchId);
        refsNodes.add(branchNode);

        if (parentCommitsBehind > 0 || parentCommitsAhead > 0) {
            Application.getInstance().getGUILog().log("[INFO] The parent branch (" + parentBranchName + ") is " +
                    (parentCommitsBehind > 0 ? NumberFormat.getInstance().format(parentCommitsBehind) + " commit" + (parentCommitsBehind != 1 ? "s" : "") + " behind " : "") +
                    (parentCommitsBehind > 0 && parentCommitsAhead > 0 ? "and " : "") +
                    (parentCommitsAhead > 0 ? NumberFormat.getInstance().format(parentCommitsAhead) + " commit" + (parentCommitsAhead != 1 ? "s" : "") + " ahead of " : "") +
                    "the branch being created (" + branchInfo.getName() + "). " +
                    "It is highly recommended that you manually sync the newly created branch to ensure parity.");
        }

        try {
            File sendFile = MMSUtils.createEntityFile(this.getClass(), ContentType.APPLICATION_JSON, refsNodes, MMSUtils.JsonBlobType.REF);
            HttpRequestBase request = MMSUtils.buildRequest(MMSUtils.HttpRequestType.POST, requestUri, sendFile, ContentType.APPLICATION_JSON);
            MMSUtils.sendMMSRequest(project, request);
        } catch (IOException | URISyntaxException | ServerException e) {
            Application.getInstance().getGUILog().log("[ERROR] An error occurred while posting branch. Branch commit aborted. Reason: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        Application.getInstance().getGUILog().log("[INFO] Branch creation for \"" + branchInfo.getName() + "\" on MMS initiated.");
        if (validateModel) {
            //RunnableWithProgress temp = new ManualSyncActionRunner<>(CommitClientElementAction.class, Collections.singletonList(project.getPrimaryModel()), project, -1);
            RunnableWithProgress temp = new ManualSyncRunner(Collections.singletonList(project.getPrimaryModel()), project, -1);
            ProgressStatusRunner.runWithProgressStatus(temp, "Model Initialization", true, 0);
        }
    }

    public BranchInfoImpl toBranchInfoImpl(EsiUtils.EsiBranchInfo esiBranchInfo) {
        try {
            Field field = Arrays.stream(esiBranchInfo.getClass().getDeclaredFields()).filter(f -> f.getType().isAssignableFrom(BranchInfoImpl.class)).findAny().orElse(null);
            if (field == null) {
                Application.getInstance().getGUILog().log("[ERROR] Branch field not found. Branch commit aborted.");
                return null;
            }
            field.setAccessible(true);
            Object o = field.get(esiBranchInfo);
            if (!(o instanceof BranchInfoImpl)) {
                Application.getInstance().getGUILog().log("[ERROR] Branch is of the wrong type. Branch commit aborted.");
                return null;
            }
            return (BranchInfoImpl) o;
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            Application.getInstance().getGUILog().log("[ERROR] Reflection on branch failed. Branch commit aborted.");
            return null;
        }
    }

    public List<CommitInfoImpl> toCommitInfoImpls(List<IVersionDescriptor> iVersionDescriptors) {
        return iVersionDescriptors.stream().map(version -> {
            try {
                Field field = Arrays.stream(version.getClass().getDeclaredFields()).filter(f -> f.getType().isAssignableFrom(CommitInfoImpl.class)).findAny().orElse(null);
                if (field == null) {
                    return null;
                }
                field.setAccessible(true);
                Object o = field.get(version);
                if (!(o instanceof CommitInfoImpl)) {
                    return null;
                }
                return (CommitInfoImpl) o;
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public void updateState() {
        String currentBranch = EsiUtils.getCurrentBranch(project.getPrimaryProject()).getName();
        if (currentBranch.equals("trunk")) {
            currentBranch = "master";
        }
        setEnabled(!validateModel || branchName.equals(currentBranch));
    }
}
