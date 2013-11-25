package gov.nasa.jpl.mgss.mbee.docgen.generator;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.AggregationKindEnum;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

/**
 * compliment of the InstanceViewpointAction this checks a view hierarchy model
 * against the viewpoints it conforms to
 * 
 * @author dlam
 * 
 */
public class ViewStructureValidator {

    private Stereotype    sysmlViewpoint;
    private GUILog        gl;
    private List<Element> missing;

    public ViewStructureValidator(Element view) {
        gl = Application.getInstance().getGUILog();
        sysmlViewpoint = Utils.getViewpointStereotype();
        missing = new ArrayList<Element>();
    }

    public void validate(Element curView) {
        List<Element> childrenViews = getChildrenViews(curView);
        Element curViewpoint = getConforms(curView);
        if (curViewpoint == null)
            return;
        List<Element> childrenViewpoints = getChildrenViewpoints(curViewpoint);
        Set<Element> childrenConforms = getChildrenConforms(childrenViews);
        for (Element vp: childrenViewpoints) {
            if (!childrenConforms.contains(vp)) {
                missing.add(vp);
            }
        }
        for (Element cv: childrenViews) {
            validate(cv);
        }
    }

    public void printErrors() {
        for (Element e: missing) {
            gl.log("Viewpoint " + ((NamedElement)e).getQualifiedName() + " is missing from view structure.");
        }
    }

    private List<Element> getChildrenViewpoints(Element v) {
        return Utils.filterElementsByStereotype(
                Utils.collectAssociatedElements(v, 1, AggregationKindEnum.COMPOSITE), sysmlViewpoint, true,
                true);
    }

    private List<Element> getChildrenViews(Element e) {
        List<Element> first = getFirst(e);
        List<Element> nexts = new ArrayList<Element>();
        for (Element el: first) {
            nexts.addAll(getNexts(el));
        }
        first.addAll(nexts);
        return first;
    }

    public List<Element> getNexts(Element e) {
        return Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(e,
                DocGen3Profile.nextStereotype, 1, false, 0);
    }

    public List<Element> getFirst(Element e) {
        return Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(e,
                DocGen3Profile.firstStereotype, 1, false, 1);
    }

    private Element getConforms(Element v) {
        List<Element> conforms = Utils.collectDirectedRelatedElementsByRelationshipStereotype(v,
                Utils.getConformsStereotype(), 1, false, 1);
        if (conforms.isEmpty())
            return null;
        return conforms.get(0);
    }

    private Set<Element> getChildrenConforms(List<Element> views) {
        Set<Element> res = new HashSet<Element>();
        for (Element e: views) {
            res.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotype(e,
                    Utils.getConformsStereotype(), 1, false, 1));
        }
        return res;
    }
}
