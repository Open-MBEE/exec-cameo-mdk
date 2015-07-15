package gov.nasa.jpl.mbee.ems.migrate;

import gov.nasa.jpl.mbee.lib.Utils;

import com.nomagic.task.ProgressStatus;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class View2ViewMigrator extends Migrator {

	public void migrate(ProgressStatus ps) {
		for (Element elem: missing) {
			if (elem.getClass().getSimpleName().equals("ClassImpl")) {
				Stereotype view = Utils.getViewStereotype();
				Stereotype doc = Utils.getDocumentStereotype();
				if (view != null && StereotypesHelper.hasStereotypeOrDerived(elem, view)) {
					if (doc != null && StereotypesHelper.hasStereotypeOrDerived(elem, doc)) {
						// do product stuff
						System.out.println(elem.getHumanName());
					}
				}
			}
		}
	}

}
