package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class DependencyMatrix extends Table {

	@Override
	public void accept(IModelVisitor v) {
		v.visit(this);
		
	}

	@Override
	public void initialize(ActivityNode an, List<Element> in) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void parse() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DocumentElement visit(boolean forViewEditor) {
		// TODO Auto-generated method stub
		return null;
	}
}
