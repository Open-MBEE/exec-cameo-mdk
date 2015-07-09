package gov.nasa.jpl.mbee.ems.validation;

import java.util.Collection;
import java.util.Set;

import org.json.simple.JSONArray;

import com.nomagic.ci.persistence.IPrimaryProject;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.task.ProgressStatus;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportMetatypes;

public class MigrationValidator {
	
	private Project proj;
    private IPrimaryProject iproj;
	
	public MigrationValidator() {
		proj = Application.getInstance().getProject();
        iproj = proj.getPrimaryProject();
	}
	
	public void migrate(ProgressStatus ps) {
		// probably need to wrap this in a Session
		// perhaps we wont even have to use these validation suites at all
		// gotta actually get the real elements from the project first
//		Collection<BaseElement> elements = (Collection<BaseElement>) proj.getElementsByIDs(proj.getAllIDS());
//		for (BaseElement elem: elements) {
//			if (elem instanceof Element) {
//				ExportMetatypes exMeta = new ExportMetatypes((Element) elem);
//				exMeta.execute();
//			}
//		}
		
		Collection<BaseElement> elements = (Collection<BaseElement>) proj.getElementsByIDs(proj.getAllIDS());
		JSONArray elems = new JSONArray();
		for (BaseElement elem: elements) {
			if (elem instanceof Element) {
				elems.add((Element) elem);
			}
		}
		
		ExportMetatypes exMeta = new ExportMetatypes(null);
		exMeta.commit(elems);
		
		// need to compile into a blob or something
		
		// probably need to be passed in the project
		
		// detect diffs: (Validate Model) but don't show it
		
		// metatype information
		
		// why detect diffs? just do it anyway
		
		// owned attribute
		
		// aggregation as part of property
		
		// push without asking user
		// select the validation rules that we want (listed above) and only do those
		
	}
}
