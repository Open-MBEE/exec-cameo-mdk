package gov.nasa.jpl.mgss.mbee.docgen.generator;

import gov.nasa.jpl.mgss.mbee.docgen.model.Container;
import gov.nasa.jpl.mgss.mbee.docgen.model.Document;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

/**
 * This parses a view structure and Product spec that uses associations for specifying children views and their order to form a view hierarchy
 * @author dlam
 *
 */
public class ProductViewParser {

	private Element start;
	private DocumentGenerator dg;
	private Document doc;
	private boolean recurse;
	private boolean singleView;
	private Set<Element> noSections;
	private Set<Element> excludeViews;
	private Stereotype product;
	
	public ProductViewParser(DocumentGenerator dg, boolean singleView, boolean recurse, Document doc, Element start) {
		this.dg = dg;
		this.singleView = singleView;
		this.recurse = recurse;
		this.doc = doc;
		this.start = start;
		this.product = dg.getProductStereotype();
		if (product != null && StereotypesHelper.hasStereotypeOrDerived(start, product)) {
			doc.setDgElement(start);
			List<Element> noSections = StereotypesHelper.getStereotypePropertyValue(start, product, "noSections");
			List<Element> excludeViews = StereotypesHelper.getStereotypePropertyValue(start, product, "excludeViews");
			this.noSections = new HashSet<Element>(noSections);
			this.excludeViews = new HashSet<Element>(excludeViews);
		} else {
			noSections = new HashSet<Element>();
			excludeViews = new HashSet<Element>();
		}
	}
	
	public void parse() {
		
	}
	
	private void parse(Element view, Container parent) {
		
	}
	
}
