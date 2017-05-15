package gov.nasa.jpl.mbee.mdk.generator;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenProfile;
import gov.nasa.jpl.mbee.mdk.util.GeneratorUtils;
import gov.nasa.jpl.mbee.mdk.model.Container;
import gov.nasa.jpl.mbee.mdk.model.Document;
import gov.nasa.jpl.mbee.mdk.model.Section;

/**
 * This parses the view structure constructed using First, Next, NoSection
 * dependencies
 *
 * @author dlam
 */
public class ViewParser {

    private DocumentGenerator dg;
    private boolean singleView;
    private boolean recurse;
    private Document doc;
    private Element start;

    public ViewParser(DocumentGenerator dg, boolean singleView, boolean recurse, Document doc, Element start) {
        this.dg = dg;
        this.singleView = singleView;
        this.recurse = recurse;
        this.doc = doc;
        this.start = start;
    }

    public Section parse() {
        Stereotype documentView = StereotypesHelper.getStereotype(Application.getInstance().getProject(),
                DocGenProfile.documentViewStereotype, "Document Profile");
        if (StereotypesHelper.hasStereotypeOrDerived(start, documentView)) {
            doc.setDgElement(start); // only set the DgElement if this is
            // actually a document view, this affects
            // processing down the line for various
            // things (like docweb visitors)
            Element first = GeneratorUtils.findStereotypedRelationship(start, DocGenProfile.firstStereotype);
            if (first != null) {
                return parseView(first, doc, true, false);
            }
        }
        else {// starting from regular view, not document
            return parseView(start, doc, true, true);
        }
        return null;
    }

    /**
     * @param view       current view
     * @param parent     parent view
     * @param section    should current view be a section
     * @param singleView parse only one view
     * @param recurse    if singleView is true, but want all children view from top
     *                   view
     * @param top        is current view the top view
     */
    private Section parseView(Element view, Container parent, boolean section, boolean top) {
        Section viewSection = dg.parseView(view);

        parent.addElement(viewSection);
        if (!section && parent instanceof Section) // parent can be Document, in
        // which case this view must
        // be a section
        {
            viewSection.setNoSection(true);
        }

        if (!singleView) { // does everything from here including nexts
            Element content = GeneratorUtils.findStereotypedRelationship(view,
                    DocGenProfile.nosectionStereotype);
            if (content != null && section) // current view is a section,
            // nosection children should go
            // under it
            {
                parseView(content, viewSection, false, false);
            }
            if (content != null && !section) // current view is not a section,
            // further nosection children
            // should be siblings
            {
                parseView(content, parent, false, false);
            }
            Element first = GeneratorUtils.findStereotypedRelationship(view, DocGenProfile.firstStereotype);
            if (first != null) {
                parseView(first, viewSection, true, false);
            }
            Element next = GeneratorUtils.findStereotypedRelationship(view, DocGenProfile.nextStereotype);
            if (next != null) {
                parseView(next, parent, true, false);
            }

        }
        else if (recurse) {// single view, but recursive (gets everything
            // underneath view including view, but not nexts
            // from the top view
            Element content = GeneratorUtils.findStereotypedRelationship(view,
                    DocGenProfile.nosectionStereotype);
            if (content != null && section) {
                parseView(content, viewSection, false, false);
            }
            if (content != null && !section) {
                parseView(content, parent, false, false);
            }
            Element first = GeneratorUtils.findStereotypedRelationship(view, DocGenProfile.firstStereotype);
            if (first != null) {
                parseView(first, viewSection, true, false);
            }
            if (!top) {
                Element next = GeneratorUtils
                        .findStereotypedRelationship(view, DocGenProfile.nextStereotype);
                if (next != null) {
                    parseView(next, parent, true, false);
                }
            }
        }
        return viewSection;
    }
}
