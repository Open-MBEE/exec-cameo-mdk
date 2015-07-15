package gov.nasa.jpl.mbee.ems;

import gov.nasa.jpl.mbee.ems.migrate.BenderToCrushinatorMigrator;
import gov.nasa.jpl.mbee.ems.migrate.View2ViewMigrator;

import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;

public class MigrationRunner implements RunnableWithProgress {

	@Override
	public void run(ProgressStatus arg0) {
		BenderToCrushinatorMigrator migrationVal = new BenderToCrushinatorMigrator();
		migrationVal.migrate(arg0);
//		View2ViewMigrator view2view = new View2ViewMigrator();
//		view2view.migrate(arg0);
	}

}
