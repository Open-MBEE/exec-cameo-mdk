package gov.nasa.jpl.mgss.mbee.docgen.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.Activity;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementImport;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageImport;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.Behavior;

import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.From;
import gov.nasa.jpl.mgss.mbee.docgen.model.Container;
import gov.nasa.jpl.mgss.mbee.docgen.model.Document;
import gov.nasa.jpl.mgss.mbee.docgen.model.Image;
import gov.nasa.jpl.mgss.mbee.docgen.model.Paragraph;
import gov.nasa.jpl.mgss.mbee.docgen.model.Section;

/**
 * This parses the view structure constructed using First, Next, NoSection dependencies
 * @author dlam
 *
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
	
	public void parse() {
		if (StereotypesHelper.hasStereotypeOrDerived(start, DocGen3Profile.documentViewStereotype)) {
			doc.setDgElement(start); //only set the DgElement if this is actually a document view, this affects processing down the line for various things (like docweb visitors)
			Element first = GeneratorUtils.findStereotypedRelationship(start, DocGen3Profile.firstStereotype);
			if (first != null)
				parseView(first, doc, true, false);				
		} else {//starting from regular view, not document
			parseView(start, doc, true, true);	
		}
	}
	
	/**
	 * 
	 * @param view current view
	 * @param parent parent view
	 * @param section should current view be a section
	 * @param singleView parse only one view
	 * @param recurse if singleView is true, but want all children view from top view
	 * @param top is current view the top view
	 */
	private void parseView(Element view, Container parent, boolean section, boolean top) {
		Element viewpoint = GeneratorUtils.findStereotypedRelationship(view, DocGen3Profile.conformStereotype);
		
		Section viewSection = new Section(); //Section is a misnomer, should be View
		viewSection.setTitle(((NamedElement)view).getName());
		viewSection.setDgElement(view);
		viewSection.setView(true);
		parent.addElement(viewSection);
		if (!section && parent instanceof Section) //parent can be Document, in which case this view must be a section
			viewSection.setNoSection(true);
		viewSection.setId(view.getID());
		if (StereotypesHelper.hasStereotype(view, DocGen3Profile.appendixViewStereotype))
			viewSection.isAppendix(true);
		
		if (viewpoint != null && viewpoint instanceof Class) { //view conforms to a viewpoint
			if (!(view instanceof Diagram)) { //if it's a diagram, people most likely put image query in viewpoint already. this is to prevent showing duplicate documentation
				String viewDoc = ModelHelper.getComment(view);
				if (viewDoc != null) {
					Paragraph para = new Paragraph(viewDoc);
					para.setDgElement(view);
					para.setFrom(From.DOCUMENTATION);
					viewSection.addElement(para);
				}
			}
			Collection<Behavior> viewpointBehavior = ((Class)viewpoint).getOwnedBehavior();
			Behavior b = null;
			if (viewpointBehavior.size() > 0) 
				b = viewpointBehavior.iterator().next();
			else {
				//viewpoint can inherit other viewpoints, if this viewpoint has no behavior, check inherited behaviors
				Class now = (Class)viewpoint;
				while(now != null) {
					if (!now.getSuperClass().isEmpty()) {
						now = now.getSuperClass().iterator().next();
						if (now.getOwnedBehavior().size() > 0) {
							b = now.getOwnedBehavior().iterator().next();
							break;
						}
					} else {
						now = null;
					}
				}
			}
			if (b != null) { //parse and execute viewpoint behavior, giving it the imported/queried elements
				List<Element> elementImports = Utils.collectDirectedRelatedElementsByRelationshipJavaClass(view, ElementImport.class, 1, 1);
				List<Element> packageImports = Utils.collectDirectedRelatedElementsByRelationshipJavaClass(view, PackageImport.class, 1, 1);
				List<Element> expose = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(view, DocGen3Profile.queriesStereotype, 1, false, 1);
				List<Element> queries = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(view, DocGen3Profile.oldQueriesStereotype, 1, false, 1);
				if (elementImports == null) elementImports = new ArrayList<Element>();
				if (packageImports != null) elementImports.addAll(packageImports);
				if (expose != null) elementImports.addAll(expose); //all three import/queries relationships are interpreted the same
				if (queries != null) elementImports.addAll(queries); //all three import/queries relationships are interpreted the same
				if (elementImports.isEmpty())
					elementImports.add(view); //if view does not import/query anything, give the view element itself to the viewpoint
				dg.getContext().pushTargets(elementImports); //this becomes the context of the activity going in
				if (b instanceof Activity) {
					dg.parseActivityOrStructuredNode(b, viewSection);
				}
				dg.getContext().popTargets();
			}
		} else { //view does not conform to a viewpoint, apply default behavior
			if (view instanceof Diagram) { //if a diagram, show diagram and documentation
				Image image = new Image();
				List<Element> images = new ArrayList<Element>();
				images.add(view);
				image.setTargets(images);
				String caption = (String)StereotypesHelper.getStereotypePropertyFirst(view, DocGen3Profile.dgviewStereotype, "caption");
				// Check for old stereotype name for backwards compatibility
				if (caption == null) caption = (String)StereotypesHelper.getStereotypePropertyFirst(view, DocGen3Profile.oldDgviewStereotype, "caption");
				List<String> captions = new ArrayList<String>();
				captions.add(caption);
				image.setCaptions(captions);
				image.setShowCaptions(true);
				viewSection.addElement(image);
			} else { //just show documentation
				String viewDoc = ModelHelper.getComment(view);
				if (viewDoc != null) {
					Paragraph para = new Paragraph(viewDoc);
					para.setDgElement(view);
					para.setFrom(From.DOCUMENTATION);
					viewSection.addElement(para);
				}
			}
		}
		
		if (!singleView) { //does everything from here including nexts
			Element content = GeneratorUtils.findStereotypedRelationship(view, DocGen3Profile.nosectionStereotype);
			if (content != null && section) //current view is a section, nosection children should go under it
				parseView(content,  viewSection, false, false);
			if (content != null && !section) //current view is not a section, further nosection children should be siblings
				parseView(content,  parent, false, false);
			Element first = GeneratorUtils.findStereotypedRelationship(view, DocGen3Profile.firstStereotype);
			if (first != null)
				parseView(first, viewSection, true, false);
			Element next = GeneratorUtils.findStereotypedRelationship(view, DocGen3Profile.nextStereotype);
			if (next != null) {
				parseView(next, parent, true, false);
			}
			
		} else if (recurse) {//single view, but recursive (gets everything underneath view including view, but not nexts from the top view
			Element content = GeneratorUtils.findStereotypedRelationship(view, DocGen3Profile.nosectionStereotype);
			if (content != null && section)
				parseView(content,  viewSection, false, false);
			if (content != null && !section)
				parseView(content,  parent, false, false);
			Element first = GeneratorUtils.findStereotypedRelationship(view, DocGen3Profile.firstStereotype);
			if (first != null)
				parseView(first, viewSection, true, false);
			if (!top) {
				Element next = GeneratorUtils.findStereotypedRelationship(view, DocGen3Profile.nextStereotype);
				if (next != null) {
					parseView(next, parent, true, false);
				}
			}
		}
	}
}
