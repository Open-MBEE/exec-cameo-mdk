package gov.nasa.jpl.mbee.mdk.ems.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.esi.EsiUtils;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRule;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ViolationSeverity;
import gov.nasa.jpl.mbee.mdk.ems.MMSUtils;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
import gov.nasa.jpl.mbee.mdk.ems.actions.CommitProjectAction;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by ablack on 3/20/17.
 */


public class ProjectValidator {

    private final Project project;

    private ValidationSuite validationSuite = new ValidationSuite("structure");
    private ValidationRule projectExistenceValidationRule = new ValidationRule("Project Existence", "The project shall exist in the specified site.", ViolationSeverity.ERROR);

    public ProjectValidator(Project project) {
        this.project = project;
        validationSuite.addValidationRule(projectExistenceValidationRule);
    }

    public void validate() {
        Project project = Application.getInstance().getProject();

        URIBuilder requestUri = MMSUtils.getServiceProjectsUri(project);
        if (requestUri == null) {
            Application.getInstance().getGUILog().log("[ERROR] Unable to get MMS URL. Project validation cancelled.");
            return;
        }
        requestUri.setPath(requestUri.getPath() + "/" + Converters.getProjectToIdConverter().apply(project));
        ObjectNode response;
        try {
            response = JacksonUtils.parseJsonObject(MMSUtils.sendMMSRequest(project, MMSUtils.buildRequest(MMSUtils.HttpRequestType.GET, requestUri)));
        } catch (IOException | ServerException | URISyntaxException e) {
            e.printStackTrace();
            Application.getInstance().getGUILog().log("[ERROR] Exception occurred while getting MMS projects. Project validation cancelled. Reason: " + e.getMessage());
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

    public ValidationSuite getValidationSuite() {
        return validationSuite;
    }

}
