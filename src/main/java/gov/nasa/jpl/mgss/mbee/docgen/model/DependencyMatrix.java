package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;

import java.util.ArrayList;
import java.util.List;

public class DependencyMatrix extends Table {

	@Override
	public void accept(IModelVisitor v) {
		v.visit(this);
		
	}
}
