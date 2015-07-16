package gov.nasa.jpl.mbee.ems;

import gov.nasa.jpl.mbee.ems.migrate.Crushinator21To22Migrator;
import gov.nasa.jpl.mbee.ems.migrate.MigrationKind;
import gov.nasa.jpl.mbee.ems.migrate.View2ViewMigrator;

import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;

public class MigrationRunner implements RunnableWithProgress {
	
	private MigrationKind mk;
	
	public MigrationRunner(MigrationKind mk) {
		this.mk = mk;
	}

	@Override
	public void run(ProgressStatus arg0) {
		switch (mk) {
			case CRUSHINATOR21TO22:
				Crushinator21To22Migrator migrationVal = new Crushinator21To22Migrator();
				migrationVal.migrate(arg0);
				// this will keep rolling into the next migrator
			case VIEW2VIEW:
				View2ViewMigrator view2view = new View2ViewMigrator();
				view2view.migrate(arg0);
				break;
			default:
				break;
		}
	}

}
