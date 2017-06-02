package gov.nasa.jpl.mbee.mdk.generator;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.AggregationKindEnum;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.util.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * compliment of the InstanceViewpointAction this checks a view hierarchy model
 * against the viewpoints it conforms to
 *
 * @author dlam
 */
public class ViewStructureValidator {

    private Stereotype sysmlViewpoint;
    private Stereotype sysmlConforms;
    private Stereotype sysmlView;
    private List<Element> missing = new ArrayList<>();

    public void validate(Element curView) {
        Project project = Project.getProject(curView);
        sysmlViewpoint = Utils.getViewpointStereotype(project);
        sysmlConforms = Utils.getSysML14ConformsStereotype(project);
        sysmlView = Utils.getViewStereotype(project);

        List<Element> childrenViews = getChildrenViews(curView);
        Element curViewpoint = getConforms(curView);
        if (curViewpoint == null) {
            return;
        }
        List<Element> childrenViewpoints = getChildrenViewpoints(curViewpoint);
        Set<Element> childrenConforms = getChildrenConforms(childrenViews);
        for (Element vp : childrenViewpoints) {
            if (!childrenConforms.contains(vp)) {
                missing.add(vp);
            }
        }
        for (Element cv : childrenViews) {
            validate(cv);
        }
    }

    public void printErrors() {
        for (Element e : missing) {
            Application.getInstance().getGUILog().log("Viewpoint " + ((NamedElement) e).getQualifiedName() + " is missing from view structure.");
        }
    }

    private List<Element> getChildrenViewpoints(Element v) {
        return Utils.filterElementsByStereotype(
                Utils.collectAssociatedElements(v, 1, AggregationKindEnum.COMPOSITE), sysmlViewpoint, true,
                true);
    }

    private List<Element> getChildrenViews(Element e) {
        return Utils.filterElementsByStereotype(
                Utils.collectAssociatedElements(e, 1, AggregationKindEnum.COMPOSITE), sysmlView, true, true);
    }

    private Element getConforms(Element v) {
        List<Element> conforms = Utils.collectDirectedRelatedElementsByRelationshipStereotype(v,
                sysmlConforms, 1, false, 1);
        if (conforms.isEmpty()) {
            return null;
        }
        return conforms.get(0);
    }

    private Set<Element> getChildrenConforms(List<Element> views) {
        Set<Element> res = new HashSet<>();
        for (Element e : views) {
            res.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotype(e,
                    sysmlConforms, 1, false, 1));
        }
        return res;
    }
}
