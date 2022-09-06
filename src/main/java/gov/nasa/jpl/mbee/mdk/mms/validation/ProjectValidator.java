package gov.nasa.jpl.mbee.mdk.mms.validation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.ci.persistence.IProject;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.magicdraw.esi.EsiUtils.EsiBranchInfo;

import com.nomagic.magicdraw.teamwork2.ITeamworkService;
import com.nomagic.magicdraw.teamwork2.TeamworkService;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.mms.actions.CommitProjectAction;
import gov.nasa.jpl.mbee.mdk.mms.endpoints.*;
import gov.nasa.jpl.mbee.mdk.validation.ValidationRule;
import gov.nasa.jpl.mbee.mdk.validation.ValidationRuleViolation;
import gov.nasa.jpl.mbee.mdk.validation.ValidationSuite;
import gov.nasa.jpl.mbee.mdk.validation.ViolationSeverity;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.security.GeneralSecurityException;

/**
 * SysML 2 Pilot Implementation
 * Copyright (C) 2018  California Institute of Technology ("Caltech")
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * @license GPL-3.0 <http://spdx.org/licenses/GPL-3.0>
 */


public class ProjectValidator {

    private final Project project;
    private boolean errors;
    private ValidationSuite validationSuite = new ValidationSuite("structure");
    private ValidationRule projectExistenceValidationRule = new ValidationRule("Project Existence", "The project shall exist in the specified site.", ViolationSeverity.ERROR);

    public ProjectValidator(Project project) {
        this.project = project;
        validationSuite.addValidationRule(projectExistenceValidationRule);
    }

    public static String getTeamworkCloudServer() {
        String twcServer = null;

        Project project = Application.getInstance().getProject();
        if(project != null && project.isRemote()) {
            ITeamworkService teamworkService = TeamworkService.getInstance(project);
            if (teamworkService != null && teamworkService.getLastUsedLoginInfo() != null
                    && teamworkService.getLastUsedLoginInfo().server != null) {
                twcServer = teamworkService.getLastUsedLoginInfo().server;
                if (twcServer.indexOf(':') > -1) {
                    twcServer = twcServer.substring(0, twcServer.indexOf(':'));
                }
            }
        }
        return twcServer;
    }

    public void updateTWCServiceToProject(JsonNode projectJson) {
        JsonNode value = projectJson.get(MDKConstants.ORG_ID_KEY);
        String orgId = (value != null && value.isTextual()) ? value.asText() : null;
        if (orgId == null) {
            Application.getInstance().getGUILog()
                    .log("[ERROR] Could not find MMS Org associated with the project. TWC Service update cancelled.");
            return;
        }
        ObjectNode projectObjectNode = generateProjectObjectNode(project, orgId);
        Collection<ObjectNode> projects = new LinkedList<>();
        projects.add(projectObjectNode);
        File sendData;
        try {
            sendData = MMSUtils.createEntityFile(this.getClass(), ContentType.APPLICATION_JSON, projects,
                    MMSUtils.JsonBlobType.PROJECT);
            // generate project post request
            HttpRequestBase request = MMSUtils
                    .prepareEndpointBuilderBasicJsonPostRequest(MMSProjectsEndpoint.builder(), project, sendData)
                    .build();
            // do project post request
            MMSUtils.sendMMSRequest(project, request);
        } catch (IOException | ServerException | URISyntaxException | GeneralSecurityException e) {
            Application.getInstance().getGUILog()
                    .log("[ERROR] An error occurred while posting project to MMS. TWC Service update cancelled. Reason: "
                            + e.getMessage());
            e.printStackTrace();
        }
    }

    com.nomagic.magicdraw.core.project.options.CommonProjectOptions

    public void checkIfTWCServiceChanged(JsonNode projectJson) {
        JsonNode value;
        if ((value = projectJson.get(MDKConstants.TWC_SERVICE_ID)) != null && value.isTextual()
                && !value.asText().isEmpty()) {
            String twcServerUrl = getTeamworkCloudServer();
            if (twcServerUrl != null && !twcServerUrl.equalsIgnoreCase(value.asText())) {
                int response = JOptionPane.showConfirmDialog(Application.getInstance().getMainFrame(),
                        "Would you like to associate this project with current TWC Service?",
                        "Associated TWC Service Changed", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.OK_OPTION) {
                    updateTWCServiceToProject(projectJson);
                }
            }
        }
    }

    public void validate() {
        ObjectNode response;


        try {
            HttpRequestBase projectsRequest = MMSUtils.prepareEndpointBuilderBasicGet(MMSProjectsEndpoint.builder(), project).build();
            File responseFile = MMSUtils.sendMMSRequest(project, projectsRequest);
            try (JsonParser jsonParser = JacksonUtils.getJsonFactory().createParser(responseFile)) {
                response = JacksonUtils.parseJsonObject(jsonParser);
            }
        } catch (IOException | ServerException | URISyntaxException | GeneralSecurityException e) {
            errors = true;
            e.printStackTrace();
            Application.getInstance().getGUILog().log("[ERROR] An error occurred while getting MMS projects. Project validation aborted. Reason: " + e.getMessage());
            return;
        }
        JsonNode projectsJson;
        if ((projectsJson = response.get("projects")) != null && projectsJson.isArray()) {
            JsonNode value;
            for (JsonNode projectJson : projectsJson) {
                if ((value = projectJson.get(MDKConstants.ID_KEY)) != null && value.isTextual()
                        && value.asText().equals(Converters.getIProjectToIdConverter().apply(project.getPrimaryProject()))) {
                    checkIfTWCServiceChanged(projectJson);
                    return;
                }
            }
        }

        ValidationRuleViolation v;
        EsiBranchInfo currentBranch = EsiUtils.getCurrentBranch(project.getPrimaryProject());
        if (!project.isRemote() || (currentBranch != null && currentBranch.getName().equals("trunk"))) {
            v = new ValidationRuleViolation(project.getPrimaryModel(), "[PROJECT MISSING ON MMS] The project does not exist in the MMS.");
            v.addAction(new CommitProjectAction(project, true));
        }
        else {
            v = new ValidationRuleViolation(project.getPrimaryModel(), "[PROJECT MISSING ON MMS] The project does not exist in the MMS. You must initialize the project from the master branch first.");
        }
        projectExistenceValidationRule.addViolation(v);
    }


    public static ObjectNode generateProjectObjectNode(Project project, String orgId) {
        return generateProjectObjectNode(project.getPrimaryProject(), orgId);
    }

    public static ObjectNode generateProjectObjectNode(IProject iProject, String orgId) {
        ObjectNode projectObjectNode = JacksonUtils.getObjectMapper().createObjectNode();
        projectObjectNode.put(MDKConstants.ID_KEY, Converters.getIProjectToIdConverter().apply(iProject));
        projectObjectNode.put(MDKConstants.NAME_KEY, iProject.getName());
        projectObjectNode.put(MDKConstants.ORG_ID_KEY, orgId);
        projectObjectNode.put(MDKConstants.PROJECT_TYPE_KEY, MDKConstants.PROJECT_TYPE_VALUE);
        String resourceId = "";
        String twcServerUrl = "";
        Project project = ProjectUtilities.getProject(iProject);
        if (ProjectUtilities.isStandardSystemProfile(iProject)) {
            try {
                projectObjectNode.set("cameo", JacksonUtils.parseJsonString("{ \"custom\": { \"cameo\": { \"_standardProfile\": true }}}"));
            }
            catch (IOException e) {
                Application.getInstance().getGUILog().log("Unknown JSON error");
            }
        }
        if (project != null && project.isRemote()) {
            resourceId = ProjectUtilities.getResourceID(iProject.getLocationURI());
            twcServerUrl = getTeamworkCloudServer();
        }
        if(twcServerUrl != null && !twcServerUrl.isEmpty()) {
            projectObjectNode.put(MDKConstants.TWC_SERVICE_ID, twcServerUrl);
        }
        projectObjectNode.put(MDKConstants.TWC_ID_KEY, resourceId);
        String categoryId = "";
        if (project != null && project.getPrimaryProject() == iProject && !resourceId.isEmpty()) {
            categoryId = EsiUtils.getCategoryID(resourceId);
        }
        projectObjectNode.put(MDKConstants.CATEGORY_ID_KEY, categoryId);
        return projectObjectNode;
    }

    public boolean hasErrors() {
        return this.errors;
    }

    public ValidationSuite getValidationSuite() {
        return validationSuite;
    }

}
