package gov.nasa.jpl.mbee.actions.ems;

import gov.nasa.jpl.mbee.ems.sync.ManualSyncRunner;

import java.awt.event.ActionEvent;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.ui.ProgressStatusRunner;

public class UpdateFromJMS extends MDAction {
    private static final long serialVersionUID = 1L;
    public static final String actionid = "UpdateFromJMS";
    
    private boolean commit;
    public UpdateFromJMS(boolean commit) {
        super(commit ? "CommitToMMS" : "UpdateFromJMS", commit ? "Update and Commit" : "Update", null, null);
        this.commit = commit;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent ae) {
        ProgressStatusRunner.runWithProgressStatus(new ManualSyncRunner(commit), "Delta Sync", true, 0);
    }
}