package gov.nasa.jpl.mbee.ems.migrate;

public enum MigrationKind {
    CRUSHINATOR21TO22("CRUSHINATOR21TO22", "EMS 2.1 to 2.2"),
    VIEW2VIEW("VIEW2VIEW", "Upgrade View2View"),
    CRUSHINATOR22TO23("22TO23", "EMS 2.2 to 2.3"),
    CRUSHINATOR23TO24("23TO24", "C-3 to C-4");

    public String title;
    public String actionid;

    MigrationKind(String actionid, String title) {
        this.actionid = actionid;
        this.title = title;
    }

}
