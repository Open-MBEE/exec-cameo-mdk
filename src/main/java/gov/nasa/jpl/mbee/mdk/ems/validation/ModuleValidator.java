package gov.nasa.jpl.mbee.mdk.ems.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.ci.persistence.IAttachedProject;
import com.nomagic.ci.persistence.IPrimaryProject;
import com.nomagic.ci.persistence.mounting.IMountPoint;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.task.ProgressStatus;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRule;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ViolationSeverity;
import gov.nasa.jpl.mbee.mdk.ems.MMSUtils;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
import gov.nasa.jpl.mbee.mdk.ems.actions.CreateModuleSite;
import gov.nasa.jpl.mbee.mdk.ems.actions.ExportLocalModule;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import org.apache.http.client.utils.URIBuilder;
import org.eclipse.emf.ecore.EObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.StreamSupport;

public class ModuleValidator {

    private ValidationSuite suite = new ValidationSuite("structure");
    private ValidationRule unexportedModule = new ValidationRule("Unexported module", "Unexported module", ViolationSeverity.ERROR);
    private ValidationRule siteExist = new ValidationRule("Site Existence", "Site Existence", ViolationSeverity.ERROR);
    private ValidationRule projectSiteExist = new ValidationRule("Site", "Project Site", ViolationSeverity.ERROR);
    private ValidationSuite siteSuite = null;

    public ModuleValidator() {
        suite.addValidationRule(unexportedModule);
        suite.addValidationRule(siteExist);
        suite.addValidationRule(projectSiteExist);
    }

    public void validate(ProgressStatus ps) {
        Project project = Application.getInstance().getProject();
        IPrimaryProject primaryProject = project.getPrimaryProject();
        Collection<IAttachedProject> modules = ProjectUtilities.getAllAttachedProjects(primaryProject);
        String baseUrl = MMSUtils.getServerUrl(project);
        String projectSite = MMSUtils.getSiteName(project);

        URIBuilder uriBuilder = MMSUtils.getServiceWorkspacesSitesUri(project);
        if (uriBuilder == null) {
            return;
        }
        JsonNode responseJsonNode;
        try {
            responseJsonNode = MMSUtils.sendCancellableMMSRequest(MMSUtils.buildRequest(MMSUtils.HttpRequestType.GET, uriBuilder), ps);
        } catch (IOException | ServerException | URISyntaxException e) {
            e.printStackTrace();
            Application.getInstance().getGUILog().log("[ERROR] Unexpected server error occurred. Aborting module validation.");
            return;
        }

        JsonNode sitesJsonNode;
        if (responseJsonNode == null || (sitesJsonNode = responseJsonNode.get("sites")) == null || !sitesJsonNode.isArray()) {
            Application.getInstance().getGUILog().log("[ERROR] Malformed sites query response. Aborting module validation.");
            return;
        }

        Set<IMountPoint> mounts = ProjectUtilities.getAllMountPoints(primaryProject);
        if (projectSite == null) {
            projectSiteExist.addViolation(new ValidationRuleViolation(null, "[PROJECT] No site defined for this project."));
        }
        else if (StreamSupport.stream(sitesJsonNode.spliterator(), false).anyMatch(jsonNode -> {
            JsonNode nameJsonNode;
            return jsonNode.isObject() && (nameJsonNode = jsonNode.get("name")) != null && nameJsonNode.isTextual() && nameJsonNode.asText().equals(projectSite);
        })) {
            projectSiteExist.addViolation(new ValidationRuleViolation(null, "[PROJECT] The site (" + projectSite + ") for this project does not exist."));
        }
        for (IAttachedProject module : modules) {
            if (ps.isCancel()) {
                return;
            }
            if (ProjectUtilities.isFromTeamworkServer(module)) {
                continue;
            }
            String moduleSite = MMSUtils.getDefaultSiteName(module);
            if (StreamSupport.stream(sitesJsonNode.spliterator(), false).anyMatch(jsonNode -> {
                JsonNode nameJsonNode;
                return jsonNode.isObject() && (nameJsonNode = jsonNode.get("name")) != null && nameJsonNode.isTextual() && nameJsonNode.asText().equals(moduleSite);
            })) {
                URIBuilder projectUriBuilder = MMSUtils.getServiceWorkspacesSitesUri(project);
                if (projectUriBuilder == null) {
                    continue;
                }
                projectUriBuilder.setPath(projectUriBuilder.getPath() + "/projects/" + module.getProjectID());
                ObjectNode responseObjectNode;
                try {
                    responseObjectNode = MMSUtils.sendMMSRequest(MMSUtils.buildRequest(MMSUtils.HttpRequestType.GET, projectUriBuilder));
                } catch (IOException | ServerException | URISyntaxException e) {
                    e.printStackTrace();
                    Application.getInstance().getGUILog().log("[ERROR] Unexpected server error occurred. Aborting module validation.");
                    return;
                }
                JsonNode elementsJsonNode;
                if (responseObjectNode == null || (elementsJsonNode = responseObjectNode.get("elements")) == null || !elementsJsonNode.isArray() || elementsJsonNode.size() == 0) {
                    Set<Element> packages = new HashSet<>();
                    for (IMountPoint mp : mounts) {
                        EObject mount = mp.getMountedPoint();
                        if (mount instanceof Element && ProjectUtilities.isAttachedProjectRoot((Element) mount, module)) {
                            packages.add((Element) mount);
                        }
                    }
                    ValidationRuleViolation v = new ValidationRuleViolation(null, "[MOUNT] The local mount " + module.getName() + " has not been uploaded yet.");
                    unexportedModule.addViolation(v);
                    String site = MMSUtils.getDefaultSiteName(module);
                    v.addAction(new ExportLocalModule(module, packages, site, project));
                }
            }
            else {
                ValidationRuleViolation v = new ValidationRuleViolation(null, "[MOUNT] The site - " + moduleSite + " - for local mount " + module.getName() + " does not exist.");
                siteExist.addViolation(v);
                String[] urls = baseUrl.split("/alfresco");
                v.addAction(new CreateModuleSite(moduleSite, urls[0]));
            }
        }
    }

    public void showWindow() {
        List<ValidationSuite> vss = new ArrayList<>();
        vss.add(suite);
        if (siteSuite != null) {
            vss.add(siteSuite);
        }
        Utils.displayValidationWindow(vss, "Module Differences");
    }

    public ValidationSuite getSuite() {
        return suite;
    }
}
