package gov.nasa.jpl.mbee.actions.ems;

import com.nomagic.actions.ActionsCategory;

public class MigrationCategory extends ActionsCategory {

	private static final String actionid = "Migration";
	
	public MigrationCategory() {
	    super(actionid, "Migration", null, null);
	    this.setNested(true);
	    this.addAction(new MigrationAction());
	    // add more actions here
	}

}
