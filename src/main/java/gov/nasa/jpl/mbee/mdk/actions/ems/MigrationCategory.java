package gov.nasa.jpl.mbee.mdk.actions.ems;

import com.nomagic.actions.ActionsCategory;
import gov.nasa.jpl.mbee.mdk.ems.migrate.MigrationKind;

public class MigrationCategory extends ActionsCategory {

    private static final String actionid = "Migration";

    public MigrationCategory() {
        super(actionid, "Migration", null, null);
        this.setNested(true);
        this.addAction(new MigrationAction(MigrationKind.CRUSHINATOR21TO22));
//	    this.addAction(new MigrationAction(MigrationKind.VIEW2VIEW));
        this.addAction(new MigrationAction(MigrationKind.CRUSHINATOR22TO23));
        this.addAction(new MigrationAction(MigrationKind.CRUSHINATOR23TO24));
        this.addAction(new CreateHoldingBinAction());
        //this.addAction(new FixViewDocumentation());
        // add more actions here
    }

}
