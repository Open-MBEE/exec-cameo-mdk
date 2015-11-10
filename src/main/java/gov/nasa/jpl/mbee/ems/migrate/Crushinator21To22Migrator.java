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
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Constraint;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Operation;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Parameter;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.sync.OutputQueue;
import gov.nasa.jpl.mbee.ems.sync.Request;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportAssociation;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportMetatypes;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportOwnedAttribute;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportProperty;
import gov.nasa.jpl.mbee.lib.Utils;

/**
 * This class migrates a MagicDraw project from EMS 2.1 to EMS 2.2
 *
 * @author brower
 *
 */

public class Crushinator21To22Migrator extends Migrator {

	@SuppressWarnings("unchecked")
	public void migrate(ProgressStatus ps) {

		JSONArray exportElems = new JSONArray();
		for (Element elem : missing) {
			// assign the id so we can update the correct element
			JSONObject einfo = new JSONObject();
			einfo.put("sysmlid", ExportUtility.getElementID(elem));

			// export owner
			einfo = ExportUtility.fillOwner(elem, einfo);

			// export specialization type
			fillSpecializationType(elem, einfo, new JSONObject());

			// confirm all metatype is new
			einfo = ExportUtility.fillMetatype(elem, einfo);

			// owned attrib is definitely new
			einfo = ExportUtility.fillOwnedAttribute(elem, einfo);

			// switch the aggregation from Association spec to Property spec
			if (elem instanceof Property) {
			    JSONObject spec = ExportUtility.fillPropertySpecialization(elem, null, false, false);
				einfo.put("specialization", spec);
				//spec.put("type", "Property");
				//spec.put("aggregation", ((Property)elem).getAggregation().toString().toUpperCase());*/
			}

			// now we have all the element updates to add to export
			exportElems.add(einfo);
		}
		//just make the view instance package on mms:
		JSONObject vi = new JSONObject();
		String projectId = Application.getInstance().getProject().getPrimaryProject().getProjectID();
		vi.put("owner", projectId);
		vi.put("sysmlid", projectId.replace("PROJECT", "View_Instances"));
		vi.put("name", "View Instances");
		vi.put("documentation", "");
		JSONObject spec = new JSONObject();
		spec.put("type", "Package");
		vi.put("specialization", spec);
		exportElems.add(vi);

		commit(exportElems);

	}

	private JSONObject fillSpecializationType(Element e, JSONObject elementInfo, JSONObject specialization) {
		elementInfo.put("specialization", specialization);
		Stereotype commentS = Utils.getCommentStereotype();
		String type = "Untyped";
		if (e instanceof Package) {
			type = "Package";
		} else if (e instanceof Property || e instanceof Slot) {
			type = "Property";
		} else if (e instanceof DirectedRelationship) {
			if (e instanceof Dependency) {
				if (StereotypesHelper.hasStereotype(e, "characterizes"))
					type = "Characterizes";
				else if (StereotypesHelper.hasStereotypeOrDerived(e,
				         DocGen3Profile.queriesStereotype))
					type = "Expose";
				else
					type = "Dependency";
			} else if (e instanceof Generalization) {
				Stereotype conforms = Utils.getSysML14ConformsStereotype();
				if (conforms != null && StereotypesHelper.hasStereotypeOrDerived(e, conforms))
					type = "Conform";
				else
					type = "Generalization";
			} else {
				type = "DirectedRelationship";
			}
		} else if (e instanceof Connector) {
			type = "Connector";
		} else if (e instanceof Operation) {
			type = "Operation";
		} else if (e instanceof Constraint) {
			type = "Constraint";
		} else if (e instanceof InstanceSpecification) {
			type = "InstanceSpecification";
		} else if (e instanceof Parameter) {
			type = "Parameter";
		} else if (e instanceof Comment || StereotypesHelper.hasStereotypeOrDerived(e, commentS)) {
			type = "Comment";
		} else if (e instanceof Association) {
			type = "Association";
		} else if (e.getClass().getSimpleName().equals("ClassImpl")) {
			Stereotype viewpoint = Utils.getViewpointStereotype();
			Stereotype view = Utils.getViewStereotype();
			Stereotype doc = Utils.getProductStereotype();
			if (viewpoint != null && StereotypesHelper.hasStereotypeOrDerived(e, viewpoint))
				type = "Viewpoint";
			else if (view != null && StereotypesHelper.hasStereotypeOrDerived(e, view)) {
				if (StereotypesHelper.hasStereotypeOrDerived(e, doc))
					type = "Product";
				else
					type = "View";
			} else
				type = "Element";
		}
		specialization.put("type", type);
		return elementInfo;
	}
}
