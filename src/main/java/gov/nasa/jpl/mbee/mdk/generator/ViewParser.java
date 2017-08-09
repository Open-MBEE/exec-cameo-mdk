package gov.nasa.jpl.mbee.mdk.generator;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenProfile;
import gov.nasa.jpl.mbee.mdk.model.Container;
import gov.nasa.jpl.mbee.mdk.model.Document;
import gov.nasa.jpl.mbee.mdk.model.Section;
import gov.nasa.jpl.mbee.mdk.util.GeneratorUtils;

/**
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
        Stereotype documentView = StereotypesHelper.getStereotype(Application.getInstance().getProject(), DocGenProfile.documentViewStereotype);
        if (StereotypesHelper.hasStereotypeOrDerived(start, documentView)) {
            doc.setDgElement(start);
        }
        else {// starting from regular view, not document
            return parseView(start, doc);
        }
        return null;
    }

    /**
     * @param view       current view
     * @param parent     parent view
     */
    private Section parseView(Element view, Container parent) {
        Section viewSection = dg.parseView(view);
        parent.addElement(viewSection);
        return viewSection;
    }
}
