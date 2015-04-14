package gov.nasa.jpl.mbee.ems.validation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import com.nomagic.ci.persistence.IPrimaryProject;
import com.nomagic.ci.persistence.IProject;
import com.nomagic.ci.persistence.IAttachedProject;
import com.nomagic.ci.persistence.mounting.IMountPoint;
import com.nomagic.ci.persistence.sharing.ISharePoint;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.core.project.ProjectDescriptorsFactory;
import com.nomagic.magicdraw.teamwork.application.BranchData;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.task.ProgressStatus;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.validation.actions.CreateAlfrescoTask;
import gov.nasa.jpl.mbee.ems.validation.actions.CreateModuleSite;
import gov.nasa.jpl.mbee.ems.validation.actions.CreateTeamworkBranch;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportLocalModule;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;

public class BranchAndModulesValidator {

    private ValidationSuite suite = new ValidationSuite("structure");
    private ValidationRule alfrescoTask = new ValidationRule("Task on alfresco", "Task on alfresco not in teamwork", ViolationSeverity.WARNING);
    private ValidationRule teamworkBranch = new ValidationRule("Teamwork branch", "Branch on teamwork not on alfresco", ViolationSeverity.WARNING);
    private ValidationRule unexportedModule = new ValidationRule("Unexported module", "Unexported module", ViolationSeverity.ERROR);
    private ValidationRule siteExist = new ValidationRule("Site Existence", "Site Existence", ViolationSeverity.ERROR);
    private ValidationRule versionMatch = new ValidationRule("Version", "Version", ViolationSeverity.INFO);
    private ValidationRule projectSiteExist = new ValidationRule("Site", "Project Site", ViolationSeverity.ERROR);
    private ValidationSuite siteSuite = null;
    
    public BranchAndModulesValidator() {
        suite.addValidationRule(alfrescoTask);
        suite.addValidationRule(teamworkBranch);
        suite.addValidationRule(unexportedModule);
        suite.addValidationRule(siteExist);
        suite.addValidationRule(versionMatch);
        suite.addValidationRule(projectSiteExist);
    }
    
    public void validate(ProgressStatus ps) {
        Project proj = Application.getInstance().getProject();
        IPrimaryProject prj = proj.getPrimaryProject();
        Collection<IAttachedProject> modules = ProjectUtilities.getAllAttachedProjects(prj);
        String baseUrl = ExportUtility.getUrl();
        String projectSite = ExportUtility.getSite();
        ExportUtility.updateMasterSites();
        Set<IMountPoint> mounts = ProjectUtilities.getAllMountPoints(prj);
        if (projectSite != null && !ExportUtility.siteExists(projectSite, false)) {
            projectSiteExist.addViolation(new ValidationRuleViolation(null, "[PSITE] The site for this project doesn't exist."));
        }
        for (IAttachedProject module: modules) {
            if (ProjectUtilities.isFromTeamworkServer(module))
                continue;
            String siteHuman = ExportUtility.getHumanSiteForProject(module);
            
            boolean siteExists = ExportUtility.siteExists(siteHuman, true);
            if (siteExists) {
                String response = ExportUtility.get(ExportUtility.getUrlForProject(module), false);
                if (response == null) {
                    Set<Element> packages = new HashSet<Element>();
                    for (IMountPoint mp: mounts) {
                        EObject mount = mp.getMountedPoint();
                        if (mount instanceof Element && ProjectUtilities.isAttachedProjectRoot((Element)mount, module))
                            packages.add((Element)mount);
                    }
                    ValidationRuleViolation v = new ValidationRuleViolation(null, "[LOCAL] The local module " + module.getName() + " isn't uploaded yet.");
                    unexportedModule.addViolation(v);
                    String site = ExportUtility.getSiteForProject(module);
                    v.addAction(new ExportLocalModule(module, packages, site));
                }
            } else {
                ValidationRuleViolation v = new ValidationRuleViolation(null, "[SITE] The site for local module " + module.getName() + " doesn't exist. (" + siteHuman + ")");
                siteExist.addViolation(v);
                String[] urls = baseUrl.split("/alfresco");
                v.addAction(new CreateModuleSite(siteHuman, urls[0]));
            }
        }

        if (!ProjectUtilities.isFromTeamworkServer(prj))
            return;
        if (TeamworkUtils.getLoggedUserName() == null) {
            Application.getInstance().getGUILog().log("You need to log in to teamwork first to do branches validation.");
            return;
        }
        ExportUtility.updateWorkspaceIdMapping();
        Map<String, String> wsMapping = ExportUtility.wsIdMapping.get(prj.getProjectID());
        Map<String, String> wsIdMapping = new HashMap<String, String>();
        for (String key: wsMapping.keySet()) {
            wsIdMapping.put(wsMapping.get(key), key);
        }
        Set<String> seenTasks = new HashSet<String>();
        Map<String, ProjectDescriptor> branchDescriptors = new HashMap<String, ProjectDescriptor>();
        String currentBranch = ExportUtility.getTeamworkBranch(proj);
        try {
            ProjectDescriptor currentProj = ProjectDescriptorsFactory.getDescriptorForProject(proj);
            ProjectDescriptor trunk = currentProj;
            String trunkBranch = ProjectDescriptorsFactory.getProjectBranchPath(trunk.getURI());
            while (trunkBranch != null) {
                BranchData parent = TeamworkUtils.getBranchedFrom(trunk);
                trunk = TeamworkUtils.getRemoteProjectDescriptor(parent.getBranchedFromId());
                trunkBranch = ProjectDescriptorsFactory.getProjectBranchPath(trunk.getURI());
            }
            
            if (currentBranch == null)
                currentBranch = "master";
            else
                currentBranch = "master/" + currentBranch;
            branchDescriptors.put(currentBranch, currentProj);
            fillBranchData(trunk, branchDescriptors);
            //Set<BranchData> branches = TeamworkUtils.getBranches(currentProj);
            for (String branchName: branchDescriptors.keySet()) {
                if (wsMapping.containsKey(branchName)) {
                    seenTasks.add(branchName);
                    Integer webVersion = ExportUtility.getAlfrescoProjectVersion(prj.getProjectID(), wsMapping.get(branchName));
                    int localVersion = TeamworkUtils.getLastVersion(branchDescriptors.get(branchName));
                    if (webVersion == null || webVersion != localVersion) {
                        ValidationRuleViolation v = new ValidationRuleViolation(null, "[VERSION] The versions of branch " + branchName + " for this project doesn't match.");
                        versionMatch.addViolation(v);
                    }
                    //check project version
                    continue;
                }
                ValidationRuleViolation v = new ValidationRuleViolation(null, "[BRANCH] The teamwork branch " + branchName + " doesn't have a corresponding task on the server.");
                teamworkBranch.addViolation(v);
                v.addAction(new CreateAlfrescoTask(branchName, wsMapping, wsIdMapping, branchDescriptors));
            }
            Set<String> allTasks = new HashSet<String>(wsMapping.keySet());
            allTasks.removeAll(seenTasks);
            //allTasks.remove("master");
            //allTasks.remove(currentBranch);
            for (String branchName: allTasks) {
                if (branchName.equals("master"))
                    continue;
                ValidationRuleViolation v = new ValidationRuleViolation(null, "[TASK] The alfresco task " + branchName + " doesn't have a corresponding teamwork branch.");
                alfrescoTask.addViolation(v);
                v.addAction(new CreateTeamworkBranch(branchName, wsMapping.get(branchName), wsMapping, wsIdMapping, branchDescriptors));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //get tasks on server
        //get project branches
        //can only do project version inits on branches where the trunk project already exists
        
        if (currentBranch.equals("master")) {
            Utils.guilog("[INFO] Validating Site Characterizations");
            SiteCharValidator siteVal = new SiteCharValidator();
            siteVal.validate(ps);
            siteSuite = siteVal.getSuite();
        }
    }
    
    private void fillBranchData(ProjectDescriptor currentProj, Map<String, ProjectDescriptor> branchDescriptors) throws RemoteException {
        Set<BranchData> branches = TeamworkUtils.getBranches(currentProj);
        for (BranchData bd: branches) {
            ProjectDescriptor rpd = TeamworkUtils.getRemoteProjectDescriptor(bd.getBranchId());
            String branchName = "master/" + ProjectDescriptorsFactory.getProjectBranchPath(rpd.getURI());
            branchDescriptors.put(branchName, rpd);
            fillBranchData(rpd, branchDescriptors);
        }
    }
    
    public void showWindow() {
        List<ValidationSuite> vss = new ArrayList<ValidationSuite>();
        vss.add(suite);
        if (siteSuite != null)
            vss.add(siteSuite);
        Utils.displayValidationWindow(vss, "Module and Branch Differences");
    }
    
    public ValidationSuite getSuite() {
        return suite;
    }
}
