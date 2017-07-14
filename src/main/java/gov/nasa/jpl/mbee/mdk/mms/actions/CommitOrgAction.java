package gov.nasa.jpl.mbee.mdk.mms.actions;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.validation.IRuleViolationAction;
import gov.nasa.jpl.mbee.mdk.validation.RuleViolationAction;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;

public class CommitOrgAction extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    public static final String DEFAULT_ID = CommitOrgAction.class.getSimpleName();
    private final Project project;

    public CommitOrgAction(Project project) {
        this(project, false);
    }

    public CommitOrgAction(Project project, boolean isDeveloperAction) {
        super(DEFAULT_ID, (isDeveloperAction ? "[DEVELOPER] " : "") + "Commit Org", null, null);
        this.project = project;
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
        // '{"elements": [{"sysmlId": "vetest", "name": "vetest"}]}' -X POST "http://localhost:8080/alfresco/service/orgs"

        // check for existing org
        URIBuilder requestUri = MMSUtils.getServiceOrgsUri(project);
        if (requestUri == null) {
            return null;
        }

        JFrame selectionDialog = new JFrame();
        String org = JOptionPane.showInputDialog(selectionDialog, "Input MMS org.");
        if (org == null) {
            Application.getInstance().getGUILog().log("[INFO] Org commit cancelled.");
            return null;
        }
        if (org.isEmpty()) {
            Application.getInstance().getGUILog().log("[ERROR] Unable to commit org without name. Org commit cancelled.");
            return null;
        }
        String orgId = org.toLowerCase().replaceAll("\\s+", "_").replaceAll("[^\\w]", "");
        if (orgId.isEmpty() || orgId.matches("^[_]*$")) {
            Application.getInstance().getGUILog().log("[ERROR] Org name \"" + org + "\" generates an invalid ID. Org commit cancelled.");
            return null;
        }

        File responseFile;
        try {
            responseFile = MMSUtils.sendMMSRequest(project, MMSUtils.buildRequest(MMSUtils.HttpRequestType.GET, requestUri));
            try (JsonParser responseParser = JacksonUtils.getJsonFactory().createParser(responseFile)) {
                ObjectNode response = JacksonUtils.parseJsonObject(responseParser);
                JsonNode arrayNode;
                if (response != null && (arrayNode = response.get("orgs")) != null && arrayNode.isArray()) {
                    for (JsonNode orgNode : arrayNode) {
                        JsonNode name, id;
                        if ((name = orgNode.get(MDKConstants.NAME_KEY)) != null && name.isTextual() && (id = orgNode.get(MDKConstants.ID_KEY)) != null && id.isTextual()) {
                            if (name.asText().equals(org)) {
                                Application.getInstance().getGUILog().log("[WARNING] An org with name \"" + org + "\" already exists. Org commit cancelled.");
                                return id.asText();
                            }
                            if (id.asText().equals(orgId)) {
                                Application.getInstance().getGUILog().log("[WARNING] An org with ID \"" + orgId + "\" already exists. Org commit cancelled.");
                                return id.asText();
                            }
                        }
                    }
                }
            }
        } catch (IOException | URISyntaxException | ServerException e) {
            Application.getInstance().getGUILog().log("[ERROR] An error occurred while getting MMS orgs. Aborting org creation. Reason: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        // build post data
        LinkedList<ObjectNode> orgs = new LinkedList<>();
        ObjectNode orgNode = JacksonUtils.getObjectMapper().createObjectNode();
        orgNode.put(MDKConstants.ID_KEY, orgId);
        orgNode.put(MDKConstants.NAME_KEY, org);
        orgs.add(orgNode);


        // do post request
        try {
            File sendData = MMSUtils.createEntityFile(this.getClass(), ContentType.APPLICATION_JSON, orgs, MMSUtils.JsonBlobType.ORG);
            MMSUtils.sendMMSRequest(project, MMSUtils.buildRequest(MMSUtils.HttpRequestType.POST, requestUri, sendData, ContentType.APPLICATION_JSON));
        } catch (IOException | ServerException | URISyntaxException e) {
            Application.getInstance().getGUILog().log("[ERROR] An error occurred while committing org. Org commit cancelled. Reason: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return orgId;
    }

}

