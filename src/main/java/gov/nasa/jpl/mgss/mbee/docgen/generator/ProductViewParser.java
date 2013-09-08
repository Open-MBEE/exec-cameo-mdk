package gov.nasa.jpl.mgss.mbee.docgen.generator;

import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.From;
import gov.nasa.jpl.mgss.mbee.docgen.model.Container;
import gov.nasa.jpl.mgss.mbee.docgen.model.Document;
import gov.nasa.jpl.mgss.mbee.docgen.model.Image;
import gov.nasa.jpl.mgss.mbee.docgen.model.Paragraph;
import gov.nasa.jpl.mgss.mbee.docgen.model.Section;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.Activity;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementImport;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageImport;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.Behavior;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

/**
 * This parses a view structure and Product spec that uses associations for specifying children views and their order to form a view hierarchy
 * @author dlam
 *
 */
public class ProductViewParser {

	private Class start;
	private DocumentGenerator dg;
	private Document doc;
	private boolean recurse;
	private boolean singleView;
	private Set<Element> noSections;
	private Set<Element> excludeViews;
	private Stereotype productS;
	private boolean product;
	
	public ProductViewParser(DocumentGenerator dg, boolean singleView, boolean recurse, Document doc, Element start) {
		this.dg = dg;
		this.singleView = singleView;
		this.recurse = recurse;
		this.doc = doc;
		if (start instanceof Class)
			this.start = (Class)start;
		this.productS = dg.getProductStereotype();
		if (productS != null && StereotypesHelper.hasStereotypeOrDerived(start, productS)) {
			product = true;
			doc.setProduct(true);
			doc.setDgElement(start);
			List<Element> noSections = (List<Element>)StereotypesHelper.getStereotypePropertyValue(start, productS, "noSections");
			List<Element> excludeViews = (List<Element>)StereotypesHelper.getStereotypePropertyValue(start, productS, "excludeViews");
			this.noSections = new HashSet<Element>(noSections);
			this.excludeViews = new HashSet<Element>(excludeViews);
		} else {
			noSections = new HashSet<Element>();
			excludeViews = new HashSet<Element>();
		}
	}
	
	public void parse() {
		if (start == null)
			return;
		Container top = doc;
		if (!product) {
			Section chapter1 = dg.parseView(start);
			top = chapter1;
			doc.addElement(chapter1);
		}
		if (!singleView || recurse)
			handleViewChildren(start, top);
	}
	
	/**
	 * 
	 * @param view
	 * @param parent parent view the current view should go under
	 * @param section whether current view is a section 
	 */
	private void parseView(Class view, Container parent, boolean nosection) {
		Section viewSection = dg.parseView(view);
		viewSection.setNoSection(nosection);
		parent.addElement(viewSection);
		handleViewChildren(view, viewSection);
	}

	private void handleViewChildren(Class view, Container viewSection) {
		List<Class> childSections = new ArrayList<Class>();
		List<Class> childNoSections = new ArrayList<Class>();
		for (Property prop: view.getOwnedAttribute()) {
			Class type = (Class)prop.getType();
			if (type == null || !StereotypesHelper.hasStereotypeOrDerived(type, dg.getView()) || excludeViews.contains(prop) || excludeViews.contains(type))
				continue;
			if (noSections.contains(type) || noSections.contains(type)) {
				childNoSections.add(type);
			} else {
				childSections.add(type);
			}
		}
		for (Class nos: childNoSections) {
			parseView(nos, viewSection, true);
		}
		for (Class s: childSections) {
			parseView(s, viewSection, false);
		}
	}
}
