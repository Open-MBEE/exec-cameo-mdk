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
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportAggregation;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportMetatypes;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportOwnedAttribute;

public class MigrationValidator {
	
	private Project proj;
    private IPrimaryProject iproj;
	
	public MigrationValidator() {
		proj = Application.getInstance().getProject();
        iproj = proj.getPrimaryProject();
	}
	
	public void migrate(ProgressStatus ps) {
		
		// here's the code to get all the elements in the project (in focus)
		// have to make sure that elements are editable?
		
		Set<Element> missing = new HashSet<Element>();
		Map<String, JSONObject> elementsKeyed = new HashMap<String, JSONObject>();
		for (Element elem : proj.getModel().getOwnedElement()) {
			getAllMissing(elem, missing, elementsKeyed);
		}
		
		// we use the export utility fillmetatype function to build the elements
		// that we actually want to send over (for metatype stuff)
		
		JSONArray metaElems = new JSONArray();
		JSONArray attrElems = new JSONArray();
		JSONArray aggrElems = new JSONArray();
		
		// for all these elements, we need to fill (separately)
		
		for (Element elem: missing) {
			JSONObject meta = ExportUtility.fillMetatype(elem, null);
			// only add the useful outputs
			if (meta != null) {
				metaElems.add(meta);
			}
			JSONObject attr = ExportUtility.fillOwnedAttribute(elem, null);
			if (attr != null) {
				attrElems.add(attr);
			}
			if (elem instanceof Association) {
				JSONObject aggr = ExportUtility.fillAggregationSpecialization((Association)elem, null);
				if (aggr != null) {
					aggrElems.add(aggr);
				}
			}

		}
		
		// we put in null for ExportMetatypes because when we commit directly
		// we don't have any element in focus
		
		// commit metatypes
		
		ExportMetatypes exMeta = new ExportMetatypes(null);
		exMeta.commit(metaElems);

		// commit owned attributes
		
		ExportOwnedAttribute exAttr = new ExportOwnedAttribute(null);
		exAttr.commit(attrElems);
		
		// aggregation as part of property
		// for element that is of type property
		ExportAggregation exAggr = new ExportAggregation(null);
		exAttr.commit(aggrElems);
		
		// push without asking user
		// select the validation rules that we want (listed above) and only do those
		
		// if documents (products) clear out view2view property
		
	}
	
	private JSONObject switchAggregationProperty(Element element, JSONObject einfo) {
		JSONObject info = einfo;
		// put stuff in the new agg category
//		info.put("aggregation", einfo.get("property"));
		// gotta delete it based on the propertyType thing
		// remove the old property category
		return einfo;
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
