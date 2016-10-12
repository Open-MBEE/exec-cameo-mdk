package gov.nasa.jpl.mbee.mdk.ems.validation;

import com.nomagic.ci.persistence.IAttachedProject;
import com.nomagic.ci.persistence.IPrimaryProject;
import com.nomagic.ci.persistence.mounting.IMountPoint;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.task.ProgressStatus;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.ems.ExportUtility;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
import gov.nasa.jpl.mbee.mdk.ems.actions.CreateModuleSite;
import gov.nasa.jpl.mbee.mdk.ems.actions.ExportLocalModule;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRule;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ViolationSeverity;
import org.eclipse.emf.ecore.EObject;

import java.util.*;

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
        Project proj = Application.getInstance().getProject();
        IPrimaryProject prj = proj.getPrimaryProject();
        Collection<IAttachedProject> modules = ProjectUtilities.getAllAttachedProjects(prj);
        String baseUrl = ExportUtility.getUrl(proj);
        String projectSite = ExportUtility.getSite();
        ExportUtility.updateMasterSites();
        Set<IMountPoint> mounts = ProjectUtilities.getAllMountPoints(prj);
        if (projectSite != null && !ExportUtility.siteExists(projectSite, false)) {
            projectSiteExist.addViolation(new ValidationRuleViolation(null, "[PSITE] The site for this project doesn't exist."));
        }
        for (IAttachedProject module : modules) {
            if (ps.isCancel()) {
                return;
            }
            if (ProjectUtilities.isFromTeamworkServer(module)) {
                continue;
            }
            String siteHuman = ExportUtility.getHumanSiteForProject(module);

            boolean siteExists = ExportUtility.siteExists(siteHuman, true);
            if (siteExists) {
                String response = null;
                try {
                    response = ExportUtility.get(ExportUtility.getUrlForProject(module), false);
                } catch (ServerException ex) {

                }
                if (response == null) {
                    Set<Element> packages = new HashSet<>();
                    for (IMountPoint mp : mounts) {
                        EObject mount = mp.getMountedPoint();
                        if (mount instanceof Element && ProjectUtilities.isAttachedProjectRoot((Element) mount, module)) {
                            packages.add((Element) mount);
                        }
                    }
                    ValidationRuleViolation v = new ValidationRuleViolation(null, "[LOCAL] The local module " + module.getName() + " isn't uploaded yet.");
                    unexportedModule.addViolation(v);
                    String site = ExportUtility.getSiteForProject(module);
                    v.addAction(new ExportLocalModule(module, packages, site));
                }
            }
            else {
                ValidationRuleViolation v = new ValidationRuleViolation(null, "[SITE] The site for local module " + module.getName() + " doesn't exist. (" + siteHuman + ")");
                siteExist.addViolation(v);
                String[] urls = baseUrl.split("/alfresco");
                v.addAction(new CreateModuleSite(siteHuman, urls[0]));
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
