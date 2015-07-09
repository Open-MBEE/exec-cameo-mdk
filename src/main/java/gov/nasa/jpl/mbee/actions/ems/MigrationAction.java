package gov.nasa.jpl.mbee.actions.ems;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.MigrationRunner;
import gov.nasa.jpl.mbee.ems.ValidateViewRunner;

import java.awt.event.ActionEvent;

import com.nomagic.actions.NMAction;
import com.nomagic.ui.ProgressStatusRunner;

public class MigrationAction extends NMAction {
	
	private static final String actionid = "Migrate";
	
	public MigrationAction() {
		super(actionid, "EMS 2.1 to 2.2", null, null);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
        if (!ExportUtility.checkBaseline()) {    
            return;
        }
        ProgressStatusRunner.runWithProgressStatus(new MigrationRunner(), "Migrating", true, 0);
	}

}
