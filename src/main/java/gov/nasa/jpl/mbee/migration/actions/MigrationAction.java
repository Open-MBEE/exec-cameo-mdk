package gov.nasa.jpl.mbee.migration.actions;

import java.awt.event.ActionEvent;

import com.nomagic.actions.NMAction;

public class MigrationAction extends NMAction{
	
	private static final String actionid = "Migrate";
	
	public MigrationAction() {
		super(actionid, "EMS 2.1 to 2.2", null, null);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println("done");
	}

}
