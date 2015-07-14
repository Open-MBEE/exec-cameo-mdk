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

import gov.nasa.jpl.mbee.ems.ExportUtility;
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
		JSONArray assoElems = new JSONArray();
		JSONArray propElems = new JSONArray();
		
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
				Association asso = (Association) elem;
				// fix the association specialization
				JSONObject assoSpec = ExportUtility.fillAssociationSpecialization(asso, null);
				if (assoSpec != null) {
					// update only the association specialization
					JSONObject assoElem = ExportUtility.fillId(asso, null);
					assoElem.put("specialization", assoSpec);
					assoElems.add(assoElem);
				}
				// get the source and target properties and fill the aggregation types there
				List<Property> props = asso.getMemberEnd();
				for (Property prop: props) {
					JSONObject propElem = ExportUtility.fillId(prop, null);
//					propElem.put("aggregation", prop.getAggregation());
					JSONObject propSpec = ExportUtility.fillPropertySpecialization(prop, null, true, true);
					propElem.put("specialization", propSpec);
					propElems.add(propElem);
				}
			}

		}
		
		// we put in null for these exports because when we commit directly
		// we don't have any element in focus
		
		// commit metatypes
		
		ExportMetatypes exMeta = new ExportMetatypes(null);
		exMeta.commit(metaElems);

		// commit owned attributes
		
		ExportOwnedAttribute exAttr = new ExportOwnedAttribute(null);
		exAttr.commit(attrElems);
		
		// commit updated association
		
		ExportAssociation exAsso = new ExportAssociation(null);
		exAsso.commit(assoElems);
		
		// commit updated properties
		
		ExportPropertyType exProp = new ExportPropertyType(null);
		exProp.commit(propElems);
				
		// if documents (products) clear out view2view property
		
		
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
