package gov.nasa.jpl.mbee.ems;

import gov.nasa.jpl.mbee.ems.validation.MigrationValidator;

import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;

public class MigrationRunner implements RunnableWithProgress {

	@Override
	public void run(ProgressStatus arg0) {
		MigrationValidator migrationVal = new MigrationValidator();
		migrationVal.migrate(arg0);
	}

}
