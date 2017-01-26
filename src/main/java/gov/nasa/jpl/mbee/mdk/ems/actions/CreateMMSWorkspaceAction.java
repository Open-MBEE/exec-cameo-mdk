package gov.nasa.jpl.mbee.mdk.ems.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import gov.nasa.jpl.mbee.mdk.MDKPlugin;
import gov.nasa.jpl.mbee.mdk.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mbee.mdk.docgen.validation.RuleViolationAction;
import gov.nasa.jpl.mbee.mdk.ems.MMSUtils;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
import gov.nasa.jpl.mbee.mdk.ems.jms.JMSUtils;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.lib.MDUtils;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import org.apache.http.client.utils.URIBuilder;
import org.joda.time.DateTime;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;

public class CreateMMSWorkspaceAction extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {
    private final String branchName;
    private final Map<String, String> wsMapping;
    private final Map<String, String> wsIdMapping;
    private final Map<String, ProjectDescriptor> branchDescriptors;
    private final Project project;

    public CreateMMSWorkspaceAction(String branchName, Map<String, String> wsMapping, Map<String, String> wsIdMapping, Map<String, ProjectDescriptor> branchDescriptors, Project project) {
        super(CreateMMSWorkspaceAction.class.getSimpleName(), "Create MMS Task", null, null);
        this.branchName = branchName;
        this.wsMapping = wsMapping;
        this.wsIdMapping = wsIdMapping;
        this.branchDescriptors = branchDescriptors;
        this.project = project;
    }

    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(Collection<Annotation> annos) {

    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent e) {
        String[] branches = branchName.split("/");
        String parentBranch = "master";
        for (int i = 1; i < branches.length - 1; i++) {
            parentBranch += "/" + branches[i];
        }
        String parentId = wsMapping.get(parentBranch);
        if (parentId == null) {
            Utils.guilog("The parent branch doesn't have a corresponding alfresco task yet, cannot create this task");
            return;
        }

        URIBuilder uriBuilder = MMSUtils.getServiceUri(project);
        if (uriBuilder == null) {
            return;
        }
        uriBuilder.setPath(uriBuilder.getPath() + "/workspaces");

        ObjectNode objectNode = JacksonUtils.getObjectMapper().createObjectNode()
                .put("source", "magicdraw").put("mdkVersion", MDKPlugin.VERSION)
                .putArray("workspaces").addObject()
                .put("name", branches[branches.length - 1]).put("parent", parentId).put("branched", new DateTime().toString())
                .put("description", "Created from MagicDraw.");

        ObjectNode responseObjectNode;
        try {
            responseObjectNode = MMSUtils.sendMMSRequest(MMSUtils.buildRequest(MMSUtils.HttpRequestType.POST, uriBuilder, objectNode));
        } catch (IOException | ServerException | URISyntaxException e1) {
            e1.printStackTrace();
            return;
        }

        JsonNode jsonNode;
        if (responseObjectNode == null || (jsonNode = responseObjectNode.get("workspaces")) == null || !jsonNode.isArray() || jsonNode.size() == 0) {
            return;
        }
        JsonNode workspaceJsonNode = jsonNode.get(0);
        JsonNode idJsonNode = workspaceJsonNode.get("id");
        JsonNode qualifiedNameJsonNode = workspaceJsonNode.get("qualifiedName");
        if (idJsonNode == null || !idJsonNode.isTextual() || qualifiedNameJsonNode == null || !qualifiedNameJsonNode.isTextual()) {
            return;
        }

        String id = idJsonNode.asText(), name = qualifiedNameJsonNode.asText();
        wsMapping.put(name, id);
        wsIdMapping.put(id, name);
        ProjectDescriptor newBranch = branchDescriptors.get(name);
        // TODO Test this version stuff @donbot
        int version = MDUtils.getLatestEsiVersion(newBranch);
        try {
            CreateMMSWorkspaceAction.initializeWorkspace(project, id);
        } catch (IOException | URISyntaxException | ServerException e1) {
            e1.printStackTrace();
            Application.getInstance().getGUILog().log("[ERROR] Unexpected exception occurred while attempting to initialize branch on MMS. Aborting.");
            return;
        }
        if (version == 0) {
            JMSUtils.initializeDurableQueue(project, id);
        }
    }

    public static ObjectNode initializeWorkspace(Project project, String branchId) throws IOException, URISyntaxException, ServerException {
        //TODO @donbot confirm
//        String site = MMSUtils.getSiteName(project);
//        URIBuilder uriBuilder = MMSUtils.getServiceWorkspacesUri(project);
//        uriBuilder.setPath(uriBuilder.getPath() + "/workspaces/" + branchId + "/sites/" + site + "/projects?createSite=true");
        URIBuilder uriBuilder = MMSUtils.getServiceProjectsRefsUri(project);

        ObjectNode objectNode = JacksonUtils.getObjectMapper().createObjectNode();
        objectNode.putArray("elements").add(MMSUtils.getProjectObjectNode(project));
        objectNode.put("source", "magicdraw").put("mdkVersion", MDKPlugin.VERSION);
        return MMSUtils.sendMMSRequest(MMSUtils.buildRequest(MMSUtils.HttpRequestType.POST, uriBuilder, objectNode));
    }
}
