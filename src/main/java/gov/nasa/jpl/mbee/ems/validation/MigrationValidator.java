package gov.nasa.jpl.mbee.ems.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.ci.persistence.IPrimaryProject;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.task.ProgressStatus;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
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
		// this is the thing that does good work
		
		Set<Element> missing = new HashSet<Element>();
		Map<String, JSONObject> elementsKeyed = new HashMap<String, JSONObject>();
		for (Element elem : proj.getModel().getOwnedElement()) {
			System.out.println(elem.getHumanName());
			if (elem.isEditable()) {
				getAllMissing(elem, missing, elementsKeyed);
			}
		}
		
		JSONArray elems = new JSONArray();
		for (Element elem: missing) {
			System.out.println(elem.getHumanName());
			elems.add(ExportUtility.fillMetatype(elem, null));
		}
		
		ExportMetatypes exMeta = new ExportMetatypes(null);
		exMeta.commit(elems);
		
		
		// detect diffs: (Validate Model) but don't show it
		
		// metatype information
		
		// why detect diffs? just do it anyway
		
		// owned attribute
		
		// aggregation as part of property
		
		// push without asking user
		// select the validation rules that we want (listed above) and only do those
		
		
		
	}
	
    private void getAllMissing(Element current, Set<Element> missing, Map<String, JSONObject> elementsKeyed) {
        if (ProjectUtilities.isElementInAttachedProject(current))
            return;
        if (!ExportUtility.shouldAdd(current))
            return;
        if (!elementsKeyed.containsKey(current.getID()))
            if (!(current instanceof Model && ((Model)current).getName().equals("Data")))
                missing.add(current);
        for (Element e: current.getOwnedElement()) {
            getAllMissing(e, missing, elementsKeyed);            
        }
    }
}
