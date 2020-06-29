package gov.nasa.jpl.mbee.mdk.mms.validation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.ci.persistence.IProject;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.esi.EsiUtils;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.http.ServerException;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;
import gov.nasa.jpl.mbee.mdk.mms.actions.CommitProjectAction;
import gov.nasa.jpl.mbee.mdk.validation.ValidationRule;
import gov.nasa.jpl.mbee.mdk.validation.ValidationRuleViolation;
import gov.nasa.jpl.mbee.mdk.validation.ValidationSuite;
import gov.nasa.jpl.mbee.mdk.validation.ViolationSeverity;
import org.apache.http.client.utils.URIBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * SysML 2 Pilot Implementation
 * Copyright (C) 2018  California Institute of Technology ("Caltech")
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
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

    public void validate() {
        URIBuilder requestUri = MMSUtils.getServiceProjectsUri(project);
        if (requestUri == null) {
            Application.getInstance().getGUILog().log("[ERROR] Unable to get MMS URL. Project validation aborted.");
            errors = true;
            return;
        }
        requestUri.setPath(requestUri.getPath() + "/" + Converters.getProjectToIdConverter().apply(project));
        File responseFile;
        ObjectNode response;
        try {
            responseFile = MMSUtils.sendMMSRequest(project, MMSUtils.buildRequest(MMSUtils.HttpRequestType.GET, requestUri));
            try (JsonParser jsonParser = JacksonUtils.getJsonFactory().createParser(responseFile)) {
                response = JacksonUtils.parseJsonObject(jsonParser);
            }
        } catch (IOException | ServerException | URISyntaxException | CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
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
                    return;
                }
            }
        }

        ValidationRuleViolation v;
        if (!project.isRemote() || EsiUtils.getCurrentBranch(project.getPrimaryProject()).getName().equals("trunk")) {
            v = new ValidationRuleViolation(project.getPrimaryModel(), "[PROJECT MISSING ON MMS] The project does not exist in the MMS.");
            v.addAction(new CommitProjectAction(project, true));
        }
        else {
            v = new ValidationRuleViolation(project.getPrimaryModel(), "[PROJECT MISSING ON MMS] The project does not exist in the MMS. You must initialize the project from the master branch first.");
        }
        projectExistenceValidationRule.addViolation(v);
    }

    public static ObjectNode generateProjectObjectNode(Project project) {
        return generateProjectObjectNode(project.getPrimaryProject());
    }

    public static ObjectNode generateProjectObjectNode(IProject iProject) {
        ObjectNode projectObjectNode = JacksonUtils.getObjectMapper().createObjectNode();
        projectObjectNode.put(MDKConstants.TYPE_KEY, "Project");
        projectObjectNode.put(MDKConstants.NAME_KEY, iProject.getName());
        projectObjectNode.put(MDKConstants.ID_KEY, Converters.getIProjectToIdConverter().apply(iProject));
        String resourceId = "";
        if (ProjectUtilities.getProject(iProject).isRemote()) {
            resourceId = ProjectUtilities.getResourceID(iProject.getLocationURI());
        }
        projectObjectNode.put(MDKConstants.TWC_ID_KEY, resourceId);
        String categoryId = "";
        if (ProjectUtilities.getProject(iProject).getPrimaryProject() == iProject && !resourceId.isEmpty()) {
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
