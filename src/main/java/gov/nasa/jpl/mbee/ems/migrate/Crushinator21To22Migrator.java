package gov.nasa.jpl.mbee.ems.migrate;

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
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.sync.OutputQueue;
import gov.nasa.jpl.mbee.ems.sync.Request;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportAssociation;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportMetatypes;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportOwnedAttribute;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportPropertyType;
import gov.nasa.jpl.mbee.lib.Utils;

/**
 * This class migrates a MagicDraw project from EMS 2.1 to EMS 2.2
 * 
 * @author brower
 *
 */

public class Crushinator21To22Migrator extends Migrator {
	
	private Project proj;
	
	@SuppressWarnings("unchecked")
	public void migrate(ProgressStatus ps) {
				
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
			if (elem instanceof Property) {
				JSONObject spec = new JSONObject();
				einfo.put("specialization", spec);
				spec.put("aggregation", ((Property)elem).getAggregation().toString().toUpperCase());
			}
			
			// now we have all the element updates to add to export
			exportElems.add(einfo);
		}
				
		commit(exportElems);

	}
}
