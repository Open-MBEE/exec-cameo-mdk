package gov.nasa.jpl.mbee.ems.migrate;

import com.nomagic.task.ProgressStatus;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import gov.nasa.jpl.mbee.ems.ExportUtility;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * This class migrates a MagicDraw project from EMS 2.2 to EMS 2.3
 */

@Deprecated
//TODO purge? @donbot
public class Crushinator22To23Migrator extends Migrator {

    @SuppressWarnings("unchecked")
    public void migrate(ProgressStatus ps) {

        JSONArray exportElems = new JSONArray();
        for (Element elem : missing) {
            if (elem instanceof Property) { //property multiplicity and redefines
                JSONObject einfo = new JSONObject();
                einfo.put("sysmlId", ExportUtility.getElementID(elem));
                einfo = ExportUtility.fillOwner(elem, einfo);
                ExportUtility.fillPropertySpecialization(elem, einfo, false, false);
                exportElems.add(einfo);
            }

        }
        commit(exportElems);
    }
}
