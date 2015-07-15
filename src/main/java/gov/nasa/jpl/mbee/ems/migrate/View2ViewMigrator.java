package gov.nasa.jpl.mbee.ems.migrate;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import gov.nasa.jpl.mbee.lib.Utils;

import com.nomagic.task.ProgressStatus;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class View2ViewMigrator extends Migrator {

	public void migrate(ProgressStatus ps) {
		JSONArray exportElems = new JSONArray();
		for (Element elem: missing) {
			if (elem.getClass().getSimpleName().equals("ClassImpl")) {
				Stereotype view = Utils.getViewStereotype();
				Stereotype doc = Utils.getDocumentStereotype();
				if (view != null && StereotypesHelper.hasStereotypeOrDerived(elem, view)) {
					if (doc != null && StereotypesHelper.hasStereotypeOrDerived(elem, doc)) {
						JSONObject einfo = new JSONObject();
						JSONObject spec = new JSONObject();
						einfo.put("specialization", spec);
						spec.put("view2view", new JSONArray());
						exportElems.add(einfo);
					}
				}
			}
		}
		commit(exportElems);
	}

}
