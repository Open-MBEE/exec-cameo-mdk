package gov.nasa.jpl.mbee.ems.validation.actions;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;

public class CreateTeamworkBranch extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    private static final long serialVersionUID = 1L;
    private String branchName;
    private String taskId;
    private Map<String, String> wsMapping;
    private Map<String, String> wsIdMapping;
    private Map<String, ProjectDescriptor> branchDescriptors;
    
    public CreateTeamworkBranch(String branchName, String taskId, Map<String, String> wsMapping, Map<String, String> wsIdMapping, Map<String, ProjectDescriptor> branchDescriptors) {
        super("CreateTeamworkBranch", "Create Teamwork Branch", null, null);
        this.branchName = branchName;
        this.taskId = taskId;
        this.wsMapping = wsMapping;
        this.wsIdMapping = wsIdMapping;
        this.branchDescriptors = branchDescriptors;
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
            Utils.guilog("The parent teamwork branch doesn't exist, create the parent branch first.");
            return;
        }
        ProjectDescriptor child = createBranch(branches[branches.length-1], parentBranchPd);
        if (child == null) {
            Utils.guilog("Creat branch failed");
            return;
        }
        branchDescriptors.put(branchName, child);
        Utils.guilog("Created Branch");
        //initialize jms queue
        
        Utils.guilog("Initializing Branch Sync");
        ExportUtility.initializeBranchVersion(taskId);
        ExportUtility.initializeDurableQueue(taskId);
        //Utils.guilog("Initialized");
    }
    
    private ProjectDescriptor createBranch(String name, ProjectDescriptor parentBranch) {
        //need to take into account time and version?
        try {
            Map<String, String> result = TeamworkUtils.branchProject(parentBranch, new HashSet<String>(), name, "Branched due to validation violation with alfresco task");
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

