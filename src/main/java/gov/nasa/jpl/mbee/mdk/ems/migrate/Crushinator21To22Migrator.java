package gov.nasa.jpl.mbee.mdk.ems.migrate;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.task.ProgressStatus;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.DocGen3Profile;
import gov.nasa.jpl.mbee.mdk.ems.ExportUtility;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * This class migrates a MagicDraw project from EMS 2.1 to EMS 2.2
 *
 * @author brower
 */

@Deprecated
//TODO purge? @donbot
public class Crushinator21To22Migrator extends Migrator {

    @SuppressWarnings("unchecked")
    public void migrate(ProgressStatus ps) {

        JSONArray exportElems = new JSONArray();
        for (Element elem : missing) {
            // assign the id so we can update the correct element
            JSONObject einfo = new JSONObject();
            einfo.put("sysmlId", ExportUtility.getElementID(elem));

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
                ExportUtility.fillPropertySpecialization(elem, einfo, false, false);
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
        vi.put("sysmlId", projectId.replace("PROJECT", "View_Instances"));
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
        }
        else if (e instanceof Property || e instanceof Slot) {
            type = "Property";
        }
        else if (e instanceof DirectedRelationship) {
            if (e instanceof Dependency) {
                if (StereotypesHelper.hasStereotype(e, "characterizes")) {
                    type = "Characterizes";
                }
                else if (StereotypesHelper.hasStereotypeOrDerived(e,
                        DocGen3Profile.queriesStereotype)) {
                    type = "Expose";
                }
                else {
                    type = "Dependency";
                }
            }
            else if (e instanceof Generalization) {
                Stereotype conforms = Utils.getSysML14ConformsStereotype();
                if (conforms != null && StereotypesHelper.hasStereotypeOrDerived(e, conforms)) {
                    type = "Conform";
                }
                else {
                    type = "Generalization";
                }
            }
            else {
                type = "DirectedRelationship";
            }
        }
        else if (e instanceof Connector) {
            type = "Connector";
        }
        else if (e instanceof Operation) {
            type = "Operation";
        }
        else if (e instanceof Constraint) {
            type = "Constraint";
        }
        else if (e instanceof InstanceSpecification) {
            type = "InstanceSpecification";
        }
        else if (e instanceof Parameter) {
            type = "Parameter";
        }
        else if (e instanceof Comment || StereotypesHelper.hasStereotypeOrDerived(e, commentS)) {
            type = "Comment";
        }
        else if (e instanceof Association) {
            type = "Association";
        }
        else if (e.getClass().getSimpleName().equals("ClassImpl")) {
            Stereotype viewpoint = Utils.getViewpointStereotype();
            Stereotype view = Utils.getViewStereotype();
            Stereotype doc = Utils.getProductStereotype();
            if (viewpoint != null && StereotypesHelper.hasStereotypeOrDerived(e, viewpoint)) {
                type = "Viewpoint";
            }
            else if (view != null && StereotypesHelper.hasStereotypeOrDerived(e, view)) {
                if (StereotypesHelper.hasStereotypeOrDerived(e, doc)) {
                    type = "Product";
                }
                else {
                    type = "View";
                }
            }
            else {
                type = "Element";
            }
        }
        specialization.put("type", type);
        return elementInfo;
    }
}
