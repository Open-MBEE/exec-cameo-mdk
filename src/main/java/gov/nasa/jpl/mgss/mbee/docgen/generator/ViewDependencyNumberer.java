package gov.nasa.jpl.mgss.mbee.docgen.generator;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;

import java.util.List;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

/**
 * numbers view dependencies (first, next) to the section number their target
 * has
 * 
 * @author dlam
 * 
 */
public class ViewDependencyNumberer {
    /**
     * Clear out any existing dependency numbering names on the first, next,
     * nosection relations. Recursively goes through each view to remove
     * relation names.
     * 
     * @param view
     *            View to clear strings for
     */
    public static void clearAll(Element view) {
        Dependency first = findStereotypedRelationship(view, DocGen3Profile.firstStereotype);
        if (first != null) {
            if (first.isEditable())
                first.setName("");
            clearAll(ModelHelper.getSupplierElement(first));
        }
        Dependency next = findStereotypedRelationship(view, DocGen3Profile.nextStereotype);
        if (next != null) {
            if (next.isEditable())
                next.setName("");
            clearAll(ModelHelper.getSupplierElement(next));
        }
        Dependency nosection = findStereotypedRelationship(view, DocGen3Profile.nosectionStereotype);
        if (nosection != null) {
            if (nosection.isEditable())
                nosection.setName("");
            clearAll(ModelHelper.getSupplierElement(nosection));
        }
    }

    /**
     * Applies the numbering to the relationships so the outline numbering can
     * be seen. Recursively goes through each view to add dependency numbers.
     * 
     * @param view
     *            View to add dependency numbering strings for
     * @param prefix
     *            List of the tree hierarchy that includes the numbering
     *            prefixes
     */
    public static void start(Element view, List<Integer> prefix) {
        Dependency first = findStereotypedRelationship(view, DocGen3Profile.firstStereotype);
        if (first != null) {
            prefix.add(1);
            String s = Utils.join(prefix, ".");
            if (first.isEditable())
                first.setName(s);
            start(ModelHelper.getSupplierElement(first), prefix);
            prefix.remove(prefix.size() - 1);
        }
        Dependency next = findStereotypedRelationship(view, DocGen3Profile.nextStereotype);
        if (next != null) {
            if (!prefix.isEmpty()) {
                prefix.set(prefix.size() - 1, prefix.get(prefix.size() - 1) + 1);
                String s = Utils.join(prefix, ".");
                if (next.isEditable())
                    next.setName(s);
                start(ModelHelper.getSupplierElement(next), prefix);
            }
        }
    }

    public static Dependency findStereotypedRelationship(Element e, String s) {
        Stereotype stereotype = StereotypesHelper.getStereotype(Application.getInstance().getProject(), s);
        for (DirectedRelationship dr: e.get_directedRelationshipOfSource()) {
            if (dr instanceof Dependency && StereotypesHelper.hasStereotype(dr, stereotype)) // REVIEW
                                                                                             // --
                                                                                             // hasStereotypeOrDerived()?
                return (Dependency)dr;
        }
        return null;
    }

}
