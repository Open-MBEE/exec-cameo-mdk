package gov.nasa.jpl.mbee.actions.ems;

import gov.nasa.jpl.mbee.ems.sync.ManualSyncRunner;

import java.awt.event.ActionEvent;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.ui.ProgressStatusRunner;

public class UpdateFromJMSAndCommitWithDelete extends MDAction {
    private static final long serialVersionUID = 1L;
    public static final String actionid = "UpdateFromJMSAndCommitWithDelete";
    
    public UpdateFromJMSAndCommitWithDelete() {
        super(actionid, "Update and Commit With Deletes", null, null);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent ae) {
        ProgressStatusRunner.runWithProgressStatus(new ManualSyncRunner(true, true), "Delta Sync", true, 0);
    }
}