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
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.validation.actions.CreateAlfrescoTask;
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
    
    public BranchAndModulesValidator() {
        suite.addValidationRule(alfrescoTask);
        suite.addValidationRule(teamworkBranch);
        suite.addValidationRule(unexportedModule);
    }
    
    public void validate() {
        Project proj = Application.getInstance().getProject();
        IPrimaryProject prj = proj.getPrimaryProject();
        Collection<IAttachedProject> modules = ProjectUtilities.getAllAttachedProjects(prj);
        String baseUrl = ExportUtility.getUrl();
        ExportUtility.updateMasterSites();
        Set<IMountPoint> mounts = ProjectUtilities.getAllMountPoints(prj);
        for (IAttachedProject module: modules) {
            if (ProjectUtilities.isFromTeamworkServer(module))
                continue;
            String site = ExportUtility.getSiteForProject(module);
            boolean siteExists = ExportUtility.siteExists(site);
            if (siteExists) {
                String response = ExportUtility.get(ExportUtility.getUrlForProject(module), false);
                if (response == null) {
                    Set<Element> packages = new HashSet<Element>();
                    for (IMountPoint mp: mounts) {
                        EObject mount = mp.getMountedPoint();
                        //ISharePoint sp = ProjectUtilities.getSharePoint(mp);
                        //mount = sp.getPoint();
                        if (mount instanceof Element && ProjectUtilities.isAttachedProjectRoot((Element)mount, module))
                            packages.add((Element)mount);
                    }
                    ValidationRuleViolation v = new ValidationRuleViolation(null, "The local module " + module.getName() + " isn't uploaded yet.");
                    unexportedModule.addViolation(v);
                    v.addAction(new ExportLocalModule(module, packages, site));
                }
            }
        }

        if (!ProjectUtilities.isFromTeamworkServer(prj))
            return;
        ExportUtility.updateWorkspaceIdMapping();
        Map<String, String> wsMapping = ExportUtility.wsIdMapping.get(prj.getProjectID());
        Map<String, String> wsIdMapping = new HashMap<String, String>();
        for (String key: wsMapping.keySet()) {
            wsIdMapping.put(wsMapping.get(key), key);
        }
        Set<String> seenTasks = new HashSet<String>();
        Map<String, ProjectDescriptor> branchDescriptors = new HashMap<String, ProjectDescriptor>();
        try {
            ProjectDescriptor currentProj = ProjectDescriptorsFactory.getDescriptorForProject(proj);
            ProjectDescriptor trunk = currentProj;
            String trunkBranch = ProjectDescriptorsFactory.getProjectBranchPath(trunk.getURI());
            while (trunkBranch != null) {
                BranchData parent = TeamworkUtils.getBranchedFrom(trunk);
                trunk = TeamworkUtils.getRemoteProjectDescriptor(parent.getBranchId());
                trunkBranch = ProjectDescriptorsFactory.getProjectBranchPath(trunk.getURI());
            }
            String currentBranch = ExportUtility.getTeamworkBranch(proj);
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
                    continue;
                }
                ValidationRuleViolation v = new ValidationRuleViolation(null, "The teamwork branch " + branchName + " doesn't have a corresponding task on the server.");
                teamworkBranch.addViolation(v);
                v.addAction(new CreateAlfrescoTask(branchName, wsMapping, wsIdMapping, branchDescriptors));
            }
            Set<String> allTasks = new HashSet<String>(wsMapping.keySet());
            allTasks.removeAll(seenTasks);
            //allTasks.remove("master");
            //allTasks.remove(currentBranch);
            for (String branchName: allTasks) {
                ValidationRuleViolation v = new ValidationRuleViolation(null, "The alfresco task " + branchName + " doesn't have a corresponding teamwork branch.");
                alfrescoTask.addViolation(v);
                v.addAction(new CreateTeamworkBranch(branchName, wsMapping.get(branchName), wsMapping, wsIdMapping, branchDescriptors));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //get tasks on server
        //get project branches
        //can only do project version inits on branches where the trunk project already exists
        
        
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
        Utils.displayValidationWindow(vss, "Module and Branch Differences");
    }
    
    public ValidationSuite getSuite() {
        return suite;
    }
}
