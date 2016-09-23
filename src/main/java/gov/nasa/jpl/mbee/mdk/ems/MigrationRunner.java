package gov.nasa.jpl.mbee.mdk.ems;

import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import gov.nasa.jpl.mbee.mdk.ems.migrate.*;

public class MigrationRunner implements RunnableWithProgress {

    private MigrationKind mk;

    public MigrationRunner(MigrationKind mk) {
        this.mk = mk;
    }

    @Override
    public void run(ProgressStatus arg0) {
        switch (mk) {
            case CRUSHINATOR21TO22:
                new Crushinator21To22Migrator().migrate(arg0);
                break;
            case CRUSHINATOR22TO23:
                new Crushinator22To23Migrator().migrate(arg0);
                break;
            case CRUSHINATOR23TO24:
                new Crushinator23To24Migrator().migrate(arg0);
                break;
            case VIEW2VIEW:
                new View2ViewMigrator().migrate(arg0);
                break;
            default:
                break;
        }
    }

}
