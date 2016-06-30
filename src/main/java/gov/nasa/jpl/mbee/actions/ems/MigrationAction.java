package gov.nasa.jpl.mbee.actions.ems;

import gov.nasa.jpl.mbee.ems.MigrationRunner;
import gov.nasa.jpl.mbee.ems.migrate.MigrationKind;

import java.awt.event.ActionEvent;

import com.nomagic.ui.ProgressStatusRunner;

public class MigrationAction extends MMSAction {
	
	private static final String actionid = "Migrate";
	private MigrationKind mk;
	
	public MigrationAction(MigrationKind mk) {
		super(actionid + mk.actionid, mk.title, null, null);
		this.mk = mk;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
        ProgressStatusRunner.runWithProgressStatus(new MigrationRunner(mk), "Migrating", true, 0);
	}

}
