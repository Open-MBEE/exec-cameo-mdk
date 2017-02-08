package gov.nasa.jpl.mbee.mdk.ems.actions;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import gov.nasa.jpl.mbee.mdk.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mbee.mdk.docgen.validation.RuleViolationAction;
import gov.nasa.jpl.mbee.mdk.ems.ServerException;
import gov.nasa.jpl.mbee.mdk.ems.jms.JMSUtils;
import gov.nasa.jpl.mbee.mdk.lib.Utils;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

// TODO Update me @donbot
@Deprecated
public class CreateTeamworkBranchAction extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {
    private final String branchName;
    private final String workspace;
    private final Map<String, String> wsMapping;
    private final Map<String, String> wsIdMapping;
    private final Map<String, ProjectDescriptor> branchDescriptors;
    private final Project project;

    public CreateTeamworkBranchAction(String branchName, String workspace, Map<String, String> wsMapping, Map<String, String> wsIdMapping, Map<String, ProjectDescriptor> branchDescriptors, Project project) {
        super(CreateTeamworkBranchAction.class.getSimpleName(), "Create Teamwork Branch", null, null);
        this.branchName = branchName;
        this.workspace = workspace;
        this.wsMapping = wsMapping;
        this.wsIdMapping = wsIdMapping;
        this.branchDescriptors = branchDescriptors;
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
        String[] branches = branchName.split("/");
        String parentBranch = "master";
        for (int i = 1; i < branches.length - 1; i++) {
            parentBranch += "/" + branches[i];
        }
        ProjectDescriptor parentBranchPd = branchDescriptors.get(parentBranch);
        if (parentBranchPd == null) {
            Utils.guilog("[ERROR] The parent branch does not exist for branch \"" + branchName +"\". Create the parent branch first. Aborting.");
            return;
        }
        ProjectDescriptor child = createBranch(branches[branches.length - 1], parentBranchPd);
        if (child == null) {
            Utils.guilog("[ERROR] Create branch failed.");
            return;
        }
        branchDescriptors.put(branchName, child);
        Utils.guilog("[INFO] Created branch.");
        //initialize jms queue

        Utils.guilog("Initializing Branch Sync");
        try {
            CreateMMSWorkspaceAction.initializeWorkspace(project, workspace);
        } catch (IOException | URISyntaxException | ServerException e1) {
            e1.printStackTrace();
            Application.getInstance().getGUILog().log("[ERROR] Unexpected exception occurred while attempting to initialize task on MMS. Aborting.");
            return;
        }
        JMSUtils.initializeDurableQueue(project, workspace);
        //Utils.guilog("Initialized");
    }

    private ProjectDescriptor createBranch(String name, ProjectDescriptor parentBranch) {
        //need to take into account time and version?
        try {
            Map<String, String> result = TeamworkUtils.branchProject(parentBranch, new HashSet<>(), name, "Branched due to validation violation with MMS task.");
            Collection<String> branched = result.values();
            if (branched.size() > 0) {
                String branchId = branched.iterator().next();
                return TeamworkUtils.getRemoteProjectDescriptor(branchId);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
}

