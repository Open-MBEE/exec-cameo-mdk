package gov.nasa.jpl.mbee.mdk.ems.validation;

import com.nomagic.ci.persistence.IPrimaryProject;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.core.project.ProjectDescriptorsFactory;
import com.nomagic.magicdraw.teamwork.application.BranchData;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.task.ProgressStatus;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRule;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ViolationSeverity;
import gov.nasa.jpl.mbee.mdk.ems.actions.CreateMMSWorkspaceAction;
import gov.nasa.jpl.mbee.mdk.ems.actions.CreateTeamworkBranchAction;
import gov.nasa.jpl.mbee.mdk.lib.MDUtils;
import gov.nasa.jpl.mbee.mdk.lib.Utils;

import java.rmi.RemoteException;
import java.util.*;

public class BranchValidator {

    private ValidationSuite suite = new ValidationSuite("structure");
    private ValidationRule alfrescoTask = new ValidationRule("Task on alfresco", "Task on alfresco not in teamwork", ViolationSeverity.WARNING);
    private ValidationRule teamworkBranch = new ValidationRule("Teamwork branch", "Branch on teamwork not on alfresco", ViolationSeverity.WARNING);
    private ValidationRule versionMatch = new ValidationRule("Version", "Version", ViolationSeverity.INFO);
    private ValidationSuite siteSuite = null;

    public BranchValidator() {
        suite.addValidationRule(alfrescoTask);
        suite.addValidationRule(teamworkBranch);
        suite.addValidationRule(versionMatch);
    }

    public void validate(ProgressStatus ps) {
        Project project = Application.getInstance().getProject();
        IPrimaryProject primaryProject = project.getPrimaryProject();

        if (!ProjectUtilities.isFromTeamworkServer(primaryProject)) {
            return;
        }
        if (TeamworkUtils.getLoggedUserName() == null) {
            Utils.guilog("You need to log in to teamwork first to do branches validation.");
            return;
        }
        // TODO Fix me @donbot
        Map<String, String> wsMapping = null /*ExportUtility.wsIdMapping.get(primaryProject.getProjectID())*/;
        Map<String, String> wsIdMapping = new HashMap<>();
        for (String key : wsMapping.keySet()) {
            wsIdMapping.put(wsMapping.get(key), key);
        }
        Set<String> seenTasks = new HashSet<>();
        Map<String, ProjectDescriptor> branchDescriptors = new HashMap<>();
        String currentBranch = MDUtils.getTeamworkBranch(project);
        try {
            ProjectDescriptor currentProj = ProjectDescriptorsFactory.getDescriptorForProject(project);
            ProjectDescriptor trunk = currentProj;
            String trunkBranch = ProjectDescriptorsFactory.getProjectBranchPath(trunk.getURI());
            while (trunkBranch != null) {
                BranchData parent = TeamworkUtils.getBranchedFrom(trunk);
                trunk = TeamworkUtils.getRemoteProjectDescriptor(parent.getBranchedFromId());
                trunkBranch = ProjectDescriptorsFactory.getProjectBranchPath(trunk.getURI());
            }

            if (currentBranch == null) {
                currentBranch = "master";
            }
            else {
                currentBranch = "master/" + currentBranch;
            }
            branchDescriptors.put(currentBranch, currentProj);
            fillBranchData(trunk, branchDescriptors);
            //Set<BranchData> branches = TeamworkUtils.getBranches(currentProj);
            for (String branchName : branchDescriptors.keySet()) {
                if (ps.isCancel()) {
                    return;
                }
                /*if (wsMapping.containsKey(branchName)) {
                    seenTasks.add(branchName);
                    Integer webVersion = ExportUtility.getAlfrescoProjectVersion(primaryProject.getProjectID(), wsMapping.get(branchName));
                    int localVersion = TeamworkUtils.getLastVersion(branchDescriptors.get(branchName));
                    if (webVersion == null || webVersion != localVersion) {
                        ValidationRuleViolation v = new ValidationRuleViolation(null, "[VERSION] The versions of branch " + branchName + " for this project doesn't match.");
                        versionMatch.addViolation(v);
                    }
                    //check project version
                    continue;
                }*/
                ValidationRuleViolation v = new ValidationRuleViolation(null, "[BRANCH] The Teamwork branch " + branchName + " does not have a corresponding MMS task.");
                teamworkBranch.addViolation(v);
                v.addAction(new CreateMMSWorkspaceAction(branchName, wsMapping, wsIdMapping, branchDescriptors, project));
            }
            Set<String> allTasks = new HashSet<>(wsMapping.keySet());
            allTasks.removeAll(seenTasks);
            //allTasks.remove("master");
            //allTasks.remove(currentBranch);
            for (String branchName : allTasks) {
                if (branchName.equals("master")) {
                    continue;
                }
                ValidationRuleViolation v = new ValidationRuleViolation(null, "[TASK] The MMS task " + branchName + " does not have a corresponding Teamwork branch.");
                alfrescoTask.addViolation(v);
                v.addAction(new CreateTeamworkBranchAction(branchName, wsMapping.get(branchName), wsMapping, wsIdMapping, branchDescriptors, project));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void fillBranchData(ProjectDescriptor currentProj, Map<String, ProjectDescriptor> branchDescriptors) throws RemoteException {
        Set<BranchData> branches = TeamworkUtils.getBranches(currentProj);
        for (BranchData bd : branches) {
            ProjectDescriptor rpd = TeamworkUtils.getRemoteProjectDescriptor(bd.getBranchId());
            String branchName = "master/" + ProjectDescriptorsFactory.getProjectBranchPath(rpd.getURI());
            branchDescriptors.put(branchName, rpd);
            fillBranchData(rpd, branchDescriptors);
        }
    }

    public void showWindow() {
        List<ValidationSuite> vss = new ArrayList<>();
        vss.add(suite);
        if (siteSuite != null) {
            vss.add(siteSuite);
        }
        Utils.displayValidationWindow(vss, "Branch Differences");
    }

    public ValidationSuite getSuite() {
        return suite;
    }
}
