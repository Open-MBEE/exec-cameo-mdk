package gov.nasa.jpl.mbee.ems.validation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.ci.persistence.IPrimaryProject;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.task.ProgressStatus;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.sync.OutputQueue;
import gov.nasa.jpl.mbee.ems.sync.Request;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportAssociation;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportMetatypes;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportOwnedAttribute;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportPropertyType;

public class MigrationValidator {
	
	private Project proj;
    private IPrimaryProject iproj;
	
	public MigrationValidator() {
		proj = Application.getInstance().getProject();
        iproj = proj.getPrimaryProject();
	}
	
	@SuppressWarnings("unchecked")
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
		
		JSONArray exportElems = new JSONArray();
				
		for (Element elem: missing) {
			// assign the id so we can update the correct element
			JSONObject einfo = new JSONObject();
			einfo.put("sysmlid", ExportUtility.getElementID(elem));
			
			// confirm all metatype is new
			einfo = ExportUtility.fillMetatype(elem, einfo);
			
			// owned attrib is definitely new
			einfo = ExportUtility.fillOwnedAttribute(elem, einfo);
			
			// switch the aggregation from Association spec to Property spec
			if (elem instanceof Association) {
				JSONObject spec = new JSONObject();
				einfo.put("specialization", spec);
				// these will be created every time you do a migrate unfortunately
				spec.put("sourceAggregation", "null");
				spec.put("targetAggregation", "null");
			} else if (elem instanceof Property) {
				JSONObject spec = new JSONObject();
				einfo.put("specialization", spec);
				spec.put("aggregation", ((Property)elem).getAggregation());
			}
			exportElems.add(einfo);
		}
		
		commit(exportElems);
				
		// if documents (products) clear out view2view property
		
		
	}
	
	private void commit(JSONArray elements) {
		JSONObject send = new JSONObject();
		send.put("elements", elements);
		send.put("source", "magicdraw");
		
		String url = ExportUtility.getPostElementsUrl();
		if (url == null) {
			return;
		}
		
		Application.getInstance().getGUILog().log("[INFO] Request is added to queue.");
		OutputQueue.getInstance().offer(new Request(url, send.toJSONString(), elements.size()));
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
