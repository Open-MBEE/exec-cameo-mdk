package gov.nasa.jpl.mbee.systemsreasoner.validation;

import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;

public abstract class GenericValidationSuite extends ValidationSuite implements Runnable {

	public GenericValidationSuite(String name) {
		super(name);
	}

}
