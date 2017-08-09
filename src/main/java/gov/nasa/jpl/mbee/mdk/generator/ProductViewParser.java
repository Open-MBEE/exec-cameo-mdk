package gov.nasa.jpl.mbee.mdk.generator;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.model.Container;
import gov.nasa.jpl.mbee.mdk.model.DocGenElement;
import gov.nasa.jpl.mbee.mdk.model.Document;
import gov.nasa.jpl.mbee.mdk.model.Section;
import gov.nasa.jpl.mbee.mdk.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This parses a view structure and Product spec that uses associations for
 * specifying children views and their order to form a view hierarchy
 *
 * @author dlam
 */
public class ProductViewParser {

    private Class start;
    private DocumentGenerator dg;
    private Document doc;
    private boolean recurse;
    private boolean singleView;
    private Stereotype productS;
    private boolean product;
    private List<Class> visitedViews;


    @SuppressWarnings("unchecked")
    public ProductViewParser(DocumentGenerator dg, boolean singleView, boolean recurse, Document doc,
                             Element start) {
        this.visitedViews = new ArrayList<Class>();
        this.dg = dg;
        this.singleView = singleView;
        this.recurse = recurse;
        this.doc = doc;
        if (start instanceof Class) {
            this.start = (Class) start;
        }
        this.productS = dg.getProductStereotype();
        if (productS != null && StereotypesHelper.hasStereotypeOrDerived(start, productS)) {
            product = true;
            doc.setProduct(true);
            doc.setDgElement(start);
        }
    }

    public void parse() {
        if (start == null) {
            return;
        }
        Container top = doc;
        if (!product) {
            Section chapter1 = dg.parseView(start);
            top = chapter1;
            doc.addElement(chapter1);
        }
        else {
            Section s = dg.parseView(start);
            for (DocGenElement e : s.getChildren()) {
                top.addElement(e);
            }
        }
        if (!singleView || recurse) {
            handleViewChildren(start, top);
        }
    }

    /**
     * @param view
     * @param parent    parent view the current view should go under
     */
    private void parseView(Class view, Container parent, boolean recurse) {
        if (visitedViews.contains(view)) {
            Application.getInstance().getGUILog().log("[WARNING] View " + view.getName() + " has already been visited. Skipping view.");
        }
        else {
            visitedViews.add(view);
            Section viewSection = dg.parseView(view);
            parent.addElement(viewSection);
            if (recurse) {
                handleViewChildren(view, viewSection);
            }
            visitedViews.remove(view);
        }
    }

    private void handleViewChildren(Class view, Container viewSection) {
        List<Pair<Class, AggregationKind>> childSections = new ArrayList<>();
        for (Property prop : view.getOwnedAttribute()) {
            if (!(prop.getType() instanceof Class)) {
                continue;
            }
            Class type = (Class) prop.getType();
            if (type == null || !StereotypesHelper.hasStereotypeOrDerived(type, dg.getView())) {
                continue;
            }
            childSections.add(new Pair<>(type, prop.getAggregation()));
        }
        for (Pair<Class, AggregationKind> pair : childSections) {
            parseView(pair.getKey(), viewSection, !AggregationKindEnum.NONE.equals(pair.getValue()));
        }
    }
}
