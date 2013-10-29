package gov.nasa.jpl.mgss.mbee.docgen.generator;

import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.model.Container;
import gov.nasa.jpl.mgss.mbee.docgen.model.Document;
import gov.nasa.jpl.mgss.mbee.docgen.model.Section;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

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
	    Stereotype documentView = StereotypesHelper.getStereotype(Application.getInstance().getProject(), DocGen3Profile.documentViewStereotype, "Document Profile");
		if (StereotypesHelper.hasStereotypeOrDerived(start, documentView)) {
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
		Section viewSection = dg.parseView(view);
		
		parent.addElement(viewSection);
		if (!section && parent instanceof Section) //parent can be Document, in which case this view must be a section
			viewSection.setNoSection(true);
		
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
