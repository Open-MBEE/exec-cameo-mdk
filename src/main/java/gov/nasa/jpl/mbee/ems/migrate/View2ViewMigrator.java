package gov.nasa.jpl.mbee.ems.migrate;

import com.nomagic.task.ProgressStatus;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.lib.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class View2ViewMigrator extends Migrator {

    public void migrate(ProgressStatus ps) {
        JSONArray exportElems = new JSONArray();
        for (Element elem : missing) {
            if (elem.getClass().getSimpleName().equals("ClassImpl")) {
                Stereotype view = Utils.getViewStereotype();
                Stereotype doc = Utils.getDocumentStereotype();
                if (view != null && StereotypesHelper.hasStereotypeOrDerived(elem, view)) {
                    if (doc != null && StereotypesHelper.hasStereotypeOrDerived(elem, doc)) {
                        JSONObject einfo = new JSONObject();
                        JSONObject spec = new JSONObject();
                        einfo.put("sysmlId", ExportUtility.getElementID(elem));
                        einfo.put("specialization", spec);
                        spec.put("view2view", null);
                        spec.put("type", "Product");
                        exportElems.add(einfo);
                    }
                }
            }
        }
        commit(exportElems);
    }
}
