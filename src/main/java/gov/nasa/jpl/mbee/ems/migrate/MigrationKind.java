package gov.nasa.jpl.mbee.ems.migrate;

public enum MigrationKind {
	BENDERTOCRUSHINATOR ("EMS 2.1 to 2.2"),
	VIEW2VIEW ("Upgrade View2View");
	
	public final String title;
	
	MigrationKind(String title) {
		this.title = title;
	}

}
