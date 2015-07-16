package gov.nasa.jpl.mbee.actions.ems;

import gov.nasa.jpl.mbee.ems.migrate.MigrationKind;

import com.nomagic.actions.ActionsCategory;

public class MigrationCategory extends ActionsCategory {

	private static final String actionid = "Migration";
	
	public MigrationCategory() {
	    super(actionid, "Migration", null, null);
	    this.setNested(true);
	    this.addAction(new MigrationAction(MigrationKind.CRUSHINATOR21TO22));
	    this.addAction(new MigrationAction(MigrationKind.VIEW2VIEW));
	    // add more actions here
	}

}
