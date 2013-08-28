package gov.nasa.jpl.mgss.mbee.docgen.generator;

import gov.nasa.jpl.graphs.DirectedEdgeVector;
import gov.nasa.jpl.graphs.DirectedGraphHashSet;
import gov.nasa.jpl.graphs.algorithms.TopologicalSort;
import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.ScriptRunner;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.DocGenUtils;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBBook;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBSerializeVisitor;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.From;
import gov.nasa.jpl.mgss.mbee.docgen.model.BillOfMaterialsTable;
import gov.nasa.jpl.mgss.mbee.docgen.model.BulletedList;
import gov.nasa.jpl.mgss.mbee.docgen.model.CombinedMatrix;
import gov.nasa.jpl.mgss.mbee.docgen.model.Container;
import gov.nasa.jpl.mgss.mbee.docgen.model.CustomTable;
import gov.nasa.jpl.mgss.mbee.docgen.model.DependencyMatrix;
import gov.nasa.jpl.mgss.mbee.docgen.model.DeploymentTable;
import gov.nasa.jpl.mgss.mbee.docgen.model.DocBookOutputVisitor;
import gov.nasa.jpl.mgss.mbee.docgen.model.DocGenElement;
import gov.nasa.jpl.mgss.mbee.docgen.model.Document;
import gov.nasa.jpl.mgss.mbee.docgen.model.GenericTable;
import gov.nasa.jpl.mgss.mbee.docgen.model.HierarchicalPropertiesTable;
import gov.nasa.jpl.mgss.mbee.docgen.model.Image;
import gov.nasa.jpl.mgss.mbee.docgen.model.LibraryMapping;
import gov.nasa.jpl.mgss.mbee.docgen.model.MissionMapping;
import gov.nasa.jpl.mgss.mbee.docgen.model.Paragraph;
import gov.nasa.jpl.mgss.mbee.docgen.model.PropertiesTableByAttributes;
import gov.nasa.jpl.mgss.mbee.docgen.model.Query;
import gov.nasa.jpl.mgss.mbee.docgen.model.Section;
import gov.nasa.jpl.mgss.mbee.docgen.model.TableStructure;
import gov.nasa.jpl.mgss.mbee.docgen.model.UserScript;
import gov.nasa.jpl.mgss.mbee.docgen.model.WorkpackageAssemblyTable;
import gov.nasa.jpl.mgss.mbee.docgen.model.WorkpackageTable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;

import javax.script.ScriptException;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallOperationAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.ActivityEdge;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.InitialNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.Activity;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.DecisionNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.ForkNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.JoinNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdintermediateactivities.MergeNode;
import com.nomagic.uml2.ext.magicdraw.activities.mdstructuredactivities.StructuredActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.AggregationKind;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.AggregationKindEnum;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementImport;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.EnumerationLiteral;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.PackageImport;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.TypedElement;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.Behavior;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;


/**
 * <p>Given the document head or a view, generates the document element model classes and structure in gov.nasa.jpl.mgss.mbee.docgen.model.</p>
 * <p>Should explore use of java reflection to set stereotype property values, but right now the profile names and java object/field names MAY
 * not always match exactly, need some thorough scrubbing to be able to use reflection.</p>
 * <p>call behavior actions are parsed by their stereotype, and then their typed behavior stereotype if any - 
 * because the tags on the action can override tags on the behavior, there's a lot of seemingly duplicate code where it's just checking for tag values.</p>
 * @author dlam
 *
 */
public class DocumentGenerator {

	private Stack<List<Element>> targets;
	private Element start;
	private Document doc;
	private ActivityNode current;
	private GUILog log;
	private Stereotype sysmlview;
	
	/**
	 * this is just some static method added as an experiment in triggering docgen from simulation toolkit
	 * Louise was trying this sometime ago, not sure if it's used by anyone
	 * @param e
	 * @param file
	 * @return
	 */
	public static boolean generateDocument(Element e, String file) {
		DocumentValidator dv = new DocumentValidator(e);
		dv.validateDocument();
		dv.printErrors();
		if (dv.isFatal())
			return false;
		DocumentGenerator dg = new DocumentGenerator(e, null);
		Document dge = dg.parseDocument();
		boolean genNewImage = dge.getGenNewImage();
		(new PostProcessor()).process(dge);
		File savefile = null;
		if (file == null) {
			String homedir = System.getProperty("user.home") + File.separator + "DocGenOutput";
			File dir = new File(homedir);
			dir.mkdirs();
			savefile = new File(homedir + File.separator + "out.xml");
		} else
			savefile = new File(file);
		File dir = savefile.getParentFile();
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(savefile));
			//List<DocumentElement> books = dge.getDocumentElement();
			DocBookOutputVisitor visitor = new DocBookOutputVisitor(false);
			dge.accept(visitor);
			DBBook book = visitor.getBook();
			if (book != null) {
				DBSerializeVisitor v = new DBSerializeVisitor(genNewImage, dir, null);
				book.accept(v);
				writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
				writer.write(v.getOut());
			}
			writer.flush();
			writer.close();
					
		} catch (IOException ex) {
			ex.printStackTrace();	
			return false;
					
		}
		return true;
	}
		
	
	public DocumentGenerator(Element e, PrintWriter wlog) {
		start = e;
		sysmlview = StereotypesHelper.getStereotype(Project.getProject(e), DocGen3Profile.viewStereotype, DocGen3Profile.sysmlProfile);
		StereotypesHelper.getStereotype(Project.getProject(e), DocGen3Profile.viewpointStereotype, DocGen3Profile.sysmlProfile);
		targets = new Stack<List<Element>>();
		doc = new Document();
		log = Application.getInstance().getGUILog();
	}

	public Document parseDocument() {
		return this.parseDocument(false, true);
	}
	
	/**
	 * singleView: whether to only parse the passed in view
	 * recurse: only if singleView is true, whether to process all children views
	 * these options are to accommodate normal docgen to docbook xml and view editor export options
	 */
	public Document parseDocument(boolean singleView, boolean recurse) {
		if (StereotypesHelper.hasStereotypeOrDerived(start, sysmlview)) {
			if (StereotypesHelper.hasStereotypeOrDerived(start, DocGen3Profile.documentViewStereotype)) {
				doc.setDgElement(start); //only set the DgElement if this is actually a document view, this affects processing down the line for various things (like docweb visitors)
				Element first = findStereotypedRelationship(start, DocGen3Profile.firstStereotype);
				if (first != null)
					parseView(first, doc, true, singleView, recurse, false);				
			} else {//starting from regular view, not document
				parseView(start, doc, true, singleView, recurse, true);	
			}
		} else if (StereotypesHelper.hasStereotypeOrDerived(start, DocGen3Profile.documentStereotype) && start instanceof Activity)
			parseActivityOrStructuredNode(start, doc);
		else {
		
		}
		docMetadata();
		for (DocGenElement e: doc.getChildren()) {
			if (e instanceof Section)
				((Section)e).isChapter(true);
		}
		return doc;
	}
	
	private void docMetadata() {
		// documentMeta Backwards Compatibility 
		String title = (String)StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentMetaStereotype, "title");
		String subtitle = (String)StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentMetaStereotype, "subtitle");
		String header = (String)StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentMetaStereotype, "header");
		String footer = (String)StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentMetaStereotype, "footer");
		String subheader = (String)StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentMetaStereotype, "subheader");
		String subfooter = (String)StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentMetaStereotype, "subfooter");
		String legalNotice = (String)StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentMetaStereotype, "legalNotice");
		String acknowledgements = (String)StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentMetaStereotype, "acknowledgement");
		Object chunkFirstSectionsO = StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentMetaStereotype, "chunkFirstSections");
		Diagram coverImage = (Diagram)StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentMetaStereotype, "coverImage");
		boolean chunkFirstSections = (chunkFirstSectionsO instanceof Boolean && !(Boolean)chunkFirstSectionsO || chunkFirstSectionsO instanceof String && chunkFirstSectionsO.equals("false")) ? false : true;
		Object indexO = StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentMetaStereotype, "index");
		boolean index = (indexO instanceof Boolean && (Boolean)indexO || indexO instanceof String && indexO.equals("true")) ? true : false;
		Object tocSectionDepthO = StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentMetaStereotype, "tocSectionDepth");
    	Integer tocSectionDepth = 20;
		if (tocSectionDepthO != null && tocSectionDepthO instanceof Integer && (Integer)tocSectionDepthO > 0)
    		tocSectionDepth = (Integer)tocSectionDepthO;
    	if (tocSectionDepthO != null && tocSectionDepthO instanceof String)
    		tocSectionDepth = Integer.parseInt((String)tocSectionDepthO);
    	Object chunkSectionDepthO = StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentMetaStereotype, "chunkSectionDepth");
    	Integer chunkSectionDepth = 20;
		if (chunkSectionDepthO != null && chunkSectionDepthO instanceof Integer && (Integer)chunkSectionDepthO > 0)
    		chunkSectionDepth = (Integer)chunkSectionDepthO;
    	if (chunkSectionDepthO != null && chunkSectionDepthO instanceof String)
    		chunkSectionDepth = Integer.parseInt((String)chunkSectionDepthO);
    	
    	//Document View Settings
		String DocumentID = (String)StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentViewStereotype, "Document ID");
		String DocumentVersion = (String)StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentViewStereotype, "Version");
		String LogoAlignment = (String)StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentViewStereotype, "Logo Alignment");
		String LogoLocation = (String)StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentViewStereotype, "Logo Location");
		String AbbreviatedProjectName= (String)StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentViewStereotype, "Project Acronym");
		String DocushareLink= (String)StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentViewStereotype, "Docushare Link");
		String AbbreiviatedTitle = (String)StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentViewStereotype, "Document Acronym");
		String TitlePageLegalNotice = (String)StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentViewStereotype, "Title Page Legal Notice");
		String FooterLegalNotice = (String)StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentViewStereotype, "Footer Legal Notice");
		String RemoveBlankPages = (String)StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentViewStereotype, "Remove Blank Pages");
		
		List <String> CollaboratorEmail = StereotypesHelper.getStereotypePropertyValueAsString(start, DocGen3Profile.documentViewStereotype, "Collaborator Email");
		List <String> RevisionHistory = StereotypesHelper.getStereotypePropertyValueAsString(start, DocGen3Profile.documentViewStereotype, "Revision History");
		String JPLProjectTitle =(String)StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentViewStereotype, "Formal Project Title");
		
		String LogoSize = (String)StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentViewStereotype, "Logo Size");
		Object UseDefaultStylesheetO =StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentViewStereotype, "UseDefaultStylesheet");
		boolean UseDefaultStylesheet=(UseDefaultStylesheetO instanceof Boolean && !(Boolean)UseDefaultStylesheetO || UseDefaultStylesheetO instanceof String && UseDefaultStylesheetO.equals("false")) ? false : true;
		
    	
    	Object genO = StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentMetaStereotype, "genNewImages");
		boolean gen = (genO instanceof Boolean && (Boolean)genO || genO instanceof String && genO.equals("true")) ? true : false;
		
    	if (title == null || title.equals(""))
    		title = ((NamedElement)start).getName();
    	Stereotype stereotype = StereotypesHelper.getStereotype(Application.getInstance().getProject(), DocGen3Profile.documentViewStereotype);
      if (FooterLegalNotice == null || FooterLegalNotice.equals("")){
    		Property propertyByName = StereotypesHelper.getPropertyByName(stereotype, "Footer Legal Notice");
        if ( propertyByName != null ) {
          FooterLegalNotice= propertyByName.getDefault();
        }    	
    	}
    	if (TitlePageLegalNotice == null || TitlePageLegalNotice.equals("")){
    		Property propertyByName = StereotypesHelper.getPropertyByName(stereotype, "Title Page Legal Notice");
    		if ( propertyByName != null ) {
    		  TitlePageLegalNotice = propertyByName.getDefault();
    		}
    	
    	}
    	
    	//Institutional Logo setup
    	String instLogo = (String)StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentViewStereotype, "InstLogo");
		String instLogoSize = (String)StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentViewStereotype, "InstLogoSize");
		String instTxt1= (String)StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentViewStereotype, "Insttxt1");
		String instTxt2 = (String)StereotypesHelper.getStereotypePropertyFirst(start, DocGen3Profile.documentViewStereotype, "Insttxt2");
    	
    	//Collect author information
    	List <String> Author= StereotypesHelper.getStereotypePropertyValueAsString(start, DocGen3Profile.documentViewStereotype, "Author");
		
    	List<String> authorCollect = new ArrayList<String>();
    	List <Element> roles = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(start, DocGen3Profile.accountableForStereotype, 2, false, 1);
    	String s = "1,2,3,4,5";
		for (Element r: roles) {
			
			String t = ((NamedElement)r).getName();
			s = "1,2," + t + "4,5";
			Collection<Element> rAttrs = ((NamedElement) r).getOwnedElement();
			for (Element rA: rAttrs) {
				String f = ((NamedElement)rA).getName();
				if (f.isEmpty()) {}
				else {
				s = f + ",2," + t + "4,5";
				Type rT = ((TypedElement) rA).getType();
				//if StereotypesHelper.hasSereotype(rT, DocGen3Profile.projectStaffStereotype) {
				String o = (String)StereotypesHelper.getStereotypePropertyFirst(rT, DocGen3Profile.projectStaffStereotype, "Organization"); 
				String d = (String)StereotypesHelper.getStereotypePropertyFirst(rT, DocGen3Profile.projectStaffStereotype, "Division"); 
				s = f + ",," + t + "," + o + "," + d;
				authorCollect.add(s);
				}
			}
		}
		if (Author.isEmpty()) {
		Author  = authorCollect;
		}
    	
    	//Collect approver information
		List <String> Approver = StereotypesHelper.getStereotypePropertyValueAsString(start, DocGen3Profile.documentViewStereotype, "Approver");
		List<String> approverCollect = new ArrayList<String>();
		
		List<Element> aprvrs = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(start, DocGen3Profile.approvesStereotype, 1, false, 1);
		for (Element a: aprvrs) {
			List<Property> aM =  ((Association) a).getMemberEnd();
			String f = "";
			String o = "";
			String t = "";
			String d = "";
			for (Property aR: aM) {
				Element aT = ((TypedElement) aR).getType();
				
				if (StereotypesHelper.hasStereotype(aT, DocGen3Profile.projectStaffStereotype)) {
					f = ((NamedElement)aR).getName();
					o = (String)StereotypesHelper.getStereotypePropertyFirst(aT, DocGen3Profile.projectStaffStereotype, "Organization"); 
					d = (String)StereotypesHelper.getStereotypePropertyFirst(aT, DocGen3Profile.projectStaffStereotype, "Division"); 
				}
				else if (StereotypesHelper.hasStereotype(aT, DocGen3Profile.roleStereotype)) {
					t = ((NamedElement)aT).getName();
				}
			}
			String z = f + ",," + t + "," + o + "," + d;
			if( z != ",,,,") {
			approverCollect.add(z);
			}	
		}
		if (Approver.isEmpty()) {
		Approver = approverCollect;
		}
		
		//Collect concurrence information
		List <String> Concurrence = StereotypesHelper.getStereotypePropertyValueAsString(start, DocGen3Profile.documentViewStereotype, "Concurrence");
		List<String> concurCollect = new ArrayList<String>();
		
		List<Element> cncr = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(start, DocGen3Profile.concursStereotype, 1, false, 1);
		for (Element c: cncr) {
			List<Property> cM =  ((Association) c).getMemberEnd();
			String f = "";
			String o = "";
			String t = "";
			String d = "";
			for (Property cR: cM) {
				Element cT = ((TypedElement) cR).getType();
				
				if (StereotypesHelper.hasStereotype(cT, DocGen3Profile.projectStaffStereotype)) {
					f = ((NamedElement)cR).getName();
					o = (String)StereotypesHelper.getStereotypePropertyFirst(cT, DocGen3Profile.projectStaffStereotype, "Organization"); 
					d = (String)StereotypesHelper.getStereotypePropertyFirst(cT, DocGen3Profile.projectStaffStereotype, "Division"); 
				}
				else if (StereotypesHelper.hasStereotype(cT, DocGen3Profile.roleStereotype)) {
					t = ((NamedElement)cT).getName();
				}
			}
			String z = f + ",," + t + "," + o + "," + d;
			if( z != ",,,,") {
			concurCollect.add(z);
			}	
		}
		if (Concurrence.isEmpty()) {
		Concurrence = concurCollect;
		}
		
    	
    	doc.setGenNewImage(gen);
		doc.setAcknowledgement(acknowledgements);
		doc.setChunkFirstSections(chunkFirstSections);
		doc.setChunkSectionDepth(chunkSectionDepth);
		doc.setCoverimage(coverImage);
		doc.setFooter(footer);
		doc.setHeader(header);
		doc.setIndex(index);
		doc.setLegalnotice(legalNotice);
		doc.setSubfooter(subfooter);
		doc.setSubheader(subheader);
		doc.setSubtitle(subtitle);
		doc.setTitle(title);
		doc.setTocSectionDepth(tocSectionDepth);
		doc.setDocumentID(DocumentID);
		doc.setDocumentVersion(DocumentVersion);
		doc.setLogoAlignment(LogoAlignment);
		doc.setLogoLocation(LogoLocation);
		doc.setAbbreviatedProjectName(AbbreviatedProjectName);
		doc.setAbbreviatedTitle(AbbreiviatedTitle);
		doc.setDocushareLink(DocushareLink);
		doc.setTitlePageLegalNotice(TitlePageLegalNotice);
		doc.setFooterLegalNotice(FooterLegalNotice);
		doc.setCollaboratorEmail(CollaboratorEmail);
		doc.setRemoveBlankPages(RemoveBlankPages);
		doc.setAuthor(Author);
		doc.setApprover(Approver);
		doc.setConcurrance(Concurrence);
		doc.setJPLProjectTitle(JPLProjectTitle);
		doc.setRevisionHistory(RevisionHistory);
		doc.setUseDefaultStylesheet(UseDefaultStylesheet);
		doc.setLogoSize(LogoSize);
		doc.setInstLogo(instLogo);
		doc.setInstLogoSize(instLogoSize);
		doc.setInstTxt1(instTxt1);
		doc.setInstTxt2(instTxt2);
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
	private void parseView(Element view, Container parent, boolean section, boolean singleView, boolean recurse, boolean top) {
		Element viewpoint = findStereotypedRelationship(view, DocGen3Profile.conformStereotype);
		
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
				targets.push(elementImports); //this becomes the context of the activity going in
				if (b instanceof Activity) {
					parseActivityOrStructuredNode(b, viewSection);
				}
				targets.pop();
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
			Element content = findStereotypedRelationship(view, DocGen3Profile.nosectionStereotype);
			if (content != null && section) //current view is a section, nosection children should go under it
				parseView(content,  viewSection, false, singleView, recurse, false);
			if (content != null && !section) //current view is not a section, further nosection children should be siblings
				parseView(content,  parent, false, singleView, recurse, false);
			Element first = findStereotypedRelationship(view, DocGen3Profile.firstStereotype);
			if (first != null)
				parseView(first, viewSection, true, singleView, recurse, false);
			Element next = findStereotypedRelationship(view, DocGen3Profile.nextStereotype);
			if (next != null) {
				parseView(next, parent, true, singleView, recurse, false);
			}
			
		} else if (recurse) {//single view, but recursive (gets everything underneath view including view, but not nexts from the top view
			Element content = findStereotypedRelationship(view, DocGen3Profile.nosectionStereotype);
			if (content != null && section)
				parseView(content,  viewSection, false, singleView, recurse, false);
			if (content != null && !section)
				parseView(content,  parent, false, singleView, recurse, false);
			Element first = findStereotypedRelationship(view, DocGen3Profile.firstStereotype);
			if (first != null)
				parseView(first, viewSection, true, singleView, recurse, false);
			if (!top) {
				Element next = findStereotypedRelationship(view, DocGen3Profile.nextStereotype);
				if (next != null) {
					parseView(next, parent, true, singleView, recurse, false);
				}
			}
		}
	}
	
	/**
	 * parses activity/structured node - these usually indicate a new context of target elements
	 * @param a
	 * @param parent
	 */
	@SuppressWarnings("unchecked")
	private void parseActivityOrStructuredNode(Element a, Container parent) {
		InitialNode in = findInitialNode(a);
		if (in == null)
			return;
		Collection<ActivityEdge> outs = in.getOutgoing();
		int pushed = 0;
		ActivityNode next2 = in;
		while (outs != null && outs.size() == 1) {
			ActivityNode next = outs.iterator().next().getTarget();
			next2 = null;
			if (next instanceof CallBehaviorAction) {
				Behavior b = ((CallBehaviorAction)next).getBehavior();
				if (StereotypesHelper.hasStereotypeOrDerived(next, DocGen3Profile.sectionStereotype) || b != null && StereotypesHelper.hasStereotypeOrDerived(b, DocGen3Profile.sectionStereotype)) {
					parseSection((CallBehaviorAction)next, parent);
					next2 = next;
				} else if (StereotypesHelper.hasStereotypeOrDerived(next, DocGen3Profile.templateStereotype) || b != null && StereotypesHelper.hasStereotypeOrDerived(b, DocGen3Profile.templateStereotype)) {
					parseQuery((CallBehaviorAction)next, parent);
					next2 = next;
				} else if (StereotypesHelper.hasStereotypeOrDerived(next, DocGen3Profile.collectFilterStereotype) || b != null && StereotypesHelper.hasStereotypeOrDerived(b, DocGen3Profile.collectFilterStereotype)) {
					List<Element> results = startCollectAndFilterSequence(next, null);
					this.targets.push(results);
					pushed++;
					next2 = current;
				}
			} else if (next instanceof StructuredActivityNode && (StereotypesHelper.hasStereotype(next, DocGen3Profile.tableStructureStereotype))) {
				// Get all the variables or whatever
				parseTableStructure((StructuredActivityNode)next, parent);
			} else if (next instanceof StructuredActivityNode) {
				Boolean loop = (Boolean)getObjectProperty(next, DocGen3Profile.templateStereotype, "loop", false);
				Boolean ignore = (Boolean)getObjectProperty(next, DocGen3Profile.templateStereotype, "ignore", false);
				Boolean createSections = (Boolean)getObjectProperty(next, DocGen3Profile.structuredQueryStereotype, "createSections", false);
				Boolean useContextNameAsTitle = (Boolean)getObjectProperty(next, DocGen3Profile.templateStereotype, "useSectionNameAsTitle", false);
				String titlePrefix = (String)StereotypesHelper.getStereotypePropertyFirst(a, DocGen3Profile.templateStereotype, "titlePrefix");
				String titleSuffix = (String)StereotypesHelper.getStereotypePropertyFirst(a, DocGen3Profile.templateStereotype, "titleSuffix");
				List<String> titles = (List<String>)StereotypesHelper.getStereotypePropertyValue(next, DocGen3Profile.templateStereotype, "titles");
				if (titles == null)
					titles = new ArrayList<String>();
				List<Element> targets = (List<Element>)StereotypesHelper.getStereotypePropertyValue(next, DocGen3Profile.templateStereotype, "targets");
				if (targets == null || targets.isEmpty()) {
					targets = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(next, DocGen3Profile.queriesStereotype, 1, false, 1);
					targets.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(next, DocGen3Profile.oldQueriesStereotype, 1, false, 1));
				}
				if (targets.isEmpty() && !this.targets.isEmpty()) {
					targets = this.targets.peek();
				}
				if (!ignore) {
					if (loop) {
						int count = 0;
						for (Element e: targets) {
							List<Element> target = new ArrayList<Element>();
							target.add(e);
							this.targets.push(target);
							Container con = parent;
							if (createSections) {
								Section sec = new Section();
								if (titles != null && titles.size() > count)
									sec.setTitle(titles.get(count));
								else if (e instanceof NamedElement)
									sec.setTitle(((NamedElement)e).getName());
								sec.setTitlePrefix(titlePrefix);
								sec.setTitleSuffix(titleSuffix);
								sec.setDgElement(next);
								parent.addElement(sec);
								con = sec;
							}
							parseActivityOrStructuredNode(next, con);
							this.targets.pop();
							count++;
						}
					} else {
						this.targets.push(targets);
						Container con = parent;
						if (createSections) {
							Section sec = new Section();
							if (titles != null && titles.size() > 0)
								sec.setTitle(titles.get(0));
							else if (!next.getName().equals(""))
								sec.setTitle(next.getName());
							sec.setUseContextNameAsTitle(useContextNameAsTitle);
							sec.setDgElement(next);
							sec.setTitlePrefix(titlePrefix);
							sec.setTitleSuffix(titleSuffix);
							parent.addElement(sec);
							con = sec;
						}
						parseActivityOrStructuredNode(next, con);
						this.targets.pop();
					}
				}
				next2 = next;
			} else if (next instanceof ForkNode && StereotypesHelper.hasStereotype(next, DocGen3Profile.parallel)) {
				List<Element> results = startCollectAndFilterSequence(next, null);
				this.targets.push(results);
				pushed++;
				next2 = current;
			}
			if (next2 == null) {
				next2 = next;
			}
			outs = next2.getOutgoing();
		} 
		while(pushed > 0) {
			this.targets.pop();
			pushed--;
		}
	}
	
	private void parseSection(CallBehaviorAction cba, Container parent) {
		String titlePrefix = (String)getObjectProperty(cba, DocGen3Profile.sectionStereotype, "titlePrefix", "");
		String titleSuffix = (String)getObjectProperty(cba, DocGen3Profile.sectionStereotype, "titleSuffix", "");
		Boolean useContextNameAsTitle = (Boolean)getObjectProperty(cba, DocGen3Profile.sectionStereotype, "useSectionNameAsTitle", false);
		String stringIfEmpty = (String)getObjectProperty(cba, DocGen3Profile.sectionStereotype, "stringIfEmpty", "");
		Boolean skipIfEmpty = (Boolean)getObjectProperty(cba, DocGen3Profile.sectionStereotype, "skipIfEmpty", false);
		Boolean ignore = (Boolean)getObjectProperty(cba, DocGen3Profile.sectionStereotype, "ignore", false);
		Boolean loop = (Boolean)getObjectProperty(cba, DocGen3Profile.sectionStereotype, "loop", false);
		Boolean isAppendix = false;
		if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.appendixStereotype) || (cba.getBehavior() != null && StereotypesHelper.hasStereotype(cba.getBehavior(), DocGen3Profile.appendixStereotype)))
			isAppendix = true;
		String title = (String)getObjectProperty(cba, DocGen3Profile.sectionStereotype, "title", "");
		if (title == null || title.equals("")) {
			title = cba.getName();
			if (title.equals("") && cba.getBehavior() != null)
				title = cba.getBehavior().getName();
		}
		if (loop) {
			if (!targets.isEmpty()) {
				for (Element e: targets.peek()) {
					List<Element> target = new ArrayList<Element>();
					target.add(e);
					targets.push(target);
					Section sec = new Section();
					sec.isAppendix(isAppendix);
					sec.setTitlePrefix(titlePrefix);
					sec.setTitleSuffix(titleSuffix);
					if (e instanceof NamedElement)
						sec.setTitle(((NamedElement)e).getName());
					else
						sec.setTitle(title);
					sec.setStringIfEmpty(stringIfEmpty);
					sec.setSkipIfEmpty(skipIfEmpty);
					sec.setIgnore(ignore);
					sec.setUseContextNameAsTitle(useContextNameAsTitle);
					parent.addElement(sec);
					parseActivityOrStructuredNode(cba.getBehavior(), sec);
					targets.pop();
				}
			}
		} else {
			Section sec = new Section();
			sec.isAppendix(isAppendix);
			sec.setTitlePrefix(titlePrefix);
			sec.setTitleSuffix(titleSuffix);
			sec.setTitle(title);
			sec.setStringIfEmpty(stringIfEmpty);
			sec.setSkipIfEmpty(skipIfEmpty);
			sec.setIgnore(ignore);
			sec.setUseContextNameAsTitle(useContextNameAsTitle);
			parent.addElement(sec);
			parseActivityOrStructuredNode(cba.getBehavior(), sec);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void parseQuery(CallBehaviorAction cba, Container parent) {
		String titlePrefix = (String)getObjectProperty(cba, DocGen3Profile.templateStereotype, "titlePrefix", "");
		String titleSuffix = (String)getObjectProperty(cba, DocGen3Profile.templateStereotype, "titleSuffix", "");
		Boolean useContextNameAsTitle = (Boolean)getObjectProperty(cba, DocGen3Profile.templateStereotype, "useSectionNameAsTitle", false);
		Boolean ignore = (Boolean)getObjectProperty(cba, DocGen3Profile.templateStereotype, "ignore", false);
		Boolean loop = (Boolean)getObjectProperty(cba, DocGen3Profile.templateStereotype, "loop", false);
		List<String> titles = (List<String>)getListProperty(cba, DocGen3Profile.templateStereotype, "titles", new ArrayList<String>());
		boolean structured = false;
		if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.structuredQueryStereotype) || (cba.getBehavior() != null && StereotypesHelper.hasStereotype(cba.getBehavior(), DocGen3Profile.structuredQueryStereotype)))
			structured = true;
		List<Element> targets = (List<Element>)StereotypesHelper.getStereotypePropertyValue(cba, DocGen3Profile.templateStereotype, "targets");
		if (targets == null || targets.isEmpty()) {
			targets = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(cba, DocGen3Profile.queriesStereotype, 1, false, 1);
			targets.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(cba, DocGen3Profile.oldQueriesStereotype, 1, false, 1));
		}
		if ((targets == null || targets.isEmpty()) && cba.getBehavior() != null) {
			targets = (List<Element>)StereotypesHelper.getStereotypePropertyValue(cba.getBehavior(), DocGen3Profile.templateStereotype, "targets");
			if (targets == null || targets.isEmpty()) {
				targets = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(cba.getBehavior(), DocGen3Profile.queriesStereotype, 1, false, 1);
				targets.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(cba.getBehavior(), DocGen3Profile.oldQueriesStereotype, 1, false, 1));
			}
		}
		if (targets.isEmpty() && !this.targets.isEmpty()) {
			targets = this.targets.peek();
		}		
		if (structured && !ignore) {
			Boolean createSections = (Boolean)getObjectProperty(cba, DocGen3Profile.structuredQueryStereotype, "createSections", false);
			if (loop) {
				int count = 0;
				for (Element e: targets) {
					List<Element> target = new ArrayList<Element>();
					target.add(e);
					this.targets.push(target);
					Container con = parent;
					if (createSections) {
						Section sec = new Section();
						if (titles != null && titles.size() > count)
							sec.setTitle(titles.get(count));
						else if (e instanceof NamedElement)
							sec.setTitle(((NamedElement)e).getName());
						sec.setTitlePrefix(titlePrefix);
						sec.setTitleSuffix(titleSuffix);
						sec.setDgElement(cba);
						parent.addElement(sec);
						con = sec;
					}
					parseActivityOrStructuredNode(cba.getBehavior(), con);
					this.targets.pop();
				}
			} else {
				this.targets.push(targets);
				Container con = parent;
				if (createSections) {
					Section sec = new Section();
					if (titles.size() > 0)
						sec.setTitle(titles.get(0));
					else if (!cba.getName().equals(""))
						sec.setTitle(cba.getName());
					else if (!cba.getBehavior().getName().equals(""))
						sec.setTitle(cba.getBehavior().getName());
					sec.setUseContextNameAsTitle(useContextNameAsTitle);
					sec.setDgElement(cba);
					sec.setTitlePrefix(titlePrefix);
					sec.setTitleSuffix(titleSuffix);
					parent.addElement(sec);
					con = sec;
				}
				parseActivityOrStructuredNode(cba.getBehavior(), con);
				this.targets.pop();
			}
		} else {
			Query dge = parseTemplate(cba);
			if (dge != null) {
				dge.setDgElement(cba);
				dge.setTargets(targets);
				dge.setTitles(titles);
				dge.setTitlePrefix(titlePrefix);
				dge.setTitleSuffix(titleSuffix);
				dge.setUseContextNameAsTitle(useContextNameAsTitle);
				dge.setIgnore(ignore);
				dge.setLoop(loop);
				parent.addElement(dge);
			}
		}
		
		
	}
	
	/**
	 * parses query actions into classes in gov.nasa.jpl.mgss.mbee.docgen.model - creates class representation of the queries
	 * There's gotta be a way to make this less ugly
	 * @param cba
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Query parseTemplate(CallBehaviorAction cba) {
		Element a = cba.getBehavior();
		List<Property> stereotypeProperties = (List<Property>)getListProperty(cba, DocGen3Profile.stereotypePropertyChoosable, "stereotypeProperties", new ArrayList<Property>());
		List<String> captions = (List<String>)getListProperty(cba, DocGen3Profile.hasCaptions, "captions", new ArrayList<String>());
		Boolean showDoc = (Boolean)getObjectProperty(cba, DocGen3Profile.documentationChoosable, "includeDoc", false);
		List<Stereotype> outgoing = (List<Stereotype>)getListProperty(cba, DocGen3Profile.stereotypedRelChoosable, "outgoingStereotypedRelationships", new ArrayList<Stereotype>());
		List<Stereotype> incoming = (List<Stereotype>)getListProperty(cba, DocGen3Profile.stereotypedRelChoosable, "incomingStereotypedRelationships", new ArrayList<Stereotype>());
		List<String> headers = (List<String>)getListProperty(cba, DocGen3Profile.headersChoosable, "headers", new ArrayList<String>());
		Boolean skipIfNoDoc = (Boolean)getObjectProperty(cba, DocGen3Profile.docSkippable, "skipIfNoDoc", false);
		Integer floatingPrecision = (Integer)getObjectProperty(cba, DocGen3Profile.precisionChoosable, "floatingPrecision", -1);
		Boolean showCaptions = (Boolean)getObjectProperty(cba, DocGen3Profile.hasCaptions, "showCaptions", true);
		Boolean includeInherited = (Boolean)getObjectProperty(cba, DocGen3Profile.inheritedChoosable, "includeInherited", false);
		String style = (String)getObjectProperty(cba, DocGen3Profile.tableStereotype, "style", null);
		List<String> colwidths = (List<String>)getListProperty(cba, DocGen3Profile.tableStereotype, "colwidths", new ArrayList<String>());
		
		Query dge = null;
		if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.imageStereotype) || (a != null && StereotypesHelper.hasStereotype(a, DocGen3Profile.imageStereotype))) {
			dge = new Image();
			Boolean doNotShow = (Boolean)getObjectProperty(cba, DocGen3Profile.imageStereotype, "doNotShow", false);
			((Image)dge).setCaptions(captions);
			((Image)dge).setShowCaptions(showCaptions);
			((Image)dge).setDoNotShow(doNotShow);
		} else if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.paragraphStereotype) || (a != null && StereotypesHelper.hasStereotype(a, DocGen3Profile.paragraphStereotype))) {
			dge = new Paragraph();
			dge.setDgElement(cba);
			String body = (String)getObjectProperty(cba, DocGen3Profile.paragraphStereotype, "body", "");
			((Paragraph)dge).setText(body);
			((Paragraph)dge).setStereotypeProperties(stereotypeProperties);
		} else if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.bulletedListStereotype) || (a != null && StereotypesHelper.hasStereotype(a, DocGen3Profile.bulletedListStereotype))) {
			dge = new BulletedList();
			Boolean showTargets = (Boolean)getObjectProperty(cba, DocGen3Profile.bulletedListStereotype, "showTargets", false);
			Boolean showSPN = (Boolean)getObjectProperty(cba, DocGen3Profile.bulletedListStereotype, "showStereotypePropertyNames", false);
			Boolean ordered = (Boolean)getObjectProperty(cba, DocGen3Profile.bulletedListStereotype, "orderedList", false);
			((BulletedList)dge).setShowTargets(showTargets);
			((BulletedList)dge).setShowStereotypePropertyNames(showSPN);
			((BulletedList)dge).setOrderedList(ordered);
			((BulletedList)dge).setIncludeDoc(showDoc);
			((BulletedList)dge).setStereotypeProperties(stereotypeProperties);
		} else if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.dependencyMatrixStereotype) || (a != null && StereotypesHelper.hasStereotype(a, DocGen3Profile.dependencyMatrixStereotype))) {
			dge = new DependencyMatrix();
		} else if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.genericTableStereotype) || (a != null && StereotypesHelper.hasStereotype(a, DocGen3Profile.genericTableStereotype))) {
			dge = new GenericTable();
			((GenericTable)dge).setCaptions(captions);
			((GenericTable)dge).setShowCaptions(showCaptions);
			((GenericTable)dge).setHeaders(headers);
			((GenericTable)dge).setSkipIfNoDoc(skipIfNoDoc);
			((GenericTable)dge).setStyle(style);
		} else if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.combinedMatrixStereotype) || (a != null && StereotypesHelper.hasStereotype(a, DocGen3Profile.combinedMatrixStereotype))) {
			dge = new CombinedMatrix();
			Integer nameColumn = (Integer)getObjectProperty(cba, DocGen3Profile.combinedMatrixStereotype, "nameColumn", 1);
			Integer docColumn = (Integer)getObjectProperty(cba, DocGen3Profile.combinedMatrixStereotype, "docColumn", 2);
			nameColumn = nameColumn < 1 ? 1 : nameColumn;
			docColumn = docColumn < 1 ? 2 : docColumn;
			((CombinedMatrix)dge).setHeaders(headers);
			((CombinedMatrix)dge).setCaptions(captions);
			((CombinedMatrix)dge).setShowCaptions(showCaptions);
			((CombinedMatrix)dge).setStereotypeProperties(stereotypeProperties);
			((CombinedMatrix)dge).setOutgoing(outgoing);
			((CombinedMatrix)dge).setIncoming(incoming);
			((CombinedMatrix)dge).setIncludeDoc(showDoc);
			((CombinedMatrix)dge).setSkipIfNoDoc(skipIfNoDoc);
			((CombinedMatrix)dge).setStyle(style);
			((CombinedMatrix)dge).setNameColumn(nameColumn);
			((CombinedMatrix)dge).setDocColumn(docColumn);
			((CombinedMatrix)dge).setColwidths(colwidths);
		} else if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.customTableStereotype) || (a != null && StereotypesHelper.hasStereotype(a, DocGen3Profile.customTableStereotype))) { //TODO columns added, name/docCol removed
			dge = new CustomTable();
			// Get "columns" slot -- should be a list of strings (e.g., OCL expressions)
			List<String> columns = null;
			Object columnsO = StereotypesHelper.getStereotypePropertyValue(cba, DocGen3Profile.customTableStereotype, "columns");
			if (columnsO != null && columnsO instanceof List)
	    		columns = (List<String>)columnsO;
	    	
			((CustomTable)dge).setHeaders(headers);
			((CustomTable)dge).setCaptions(captions);
			((CustomTable)dge).setShowCaptions(showCaptions);
//			((CustomTable)dge).setStereotypeProperties(stereotypeProperties);
			((CustomTable)dge).setStyle(style);
			((CustomTable)dge).setColumns(columns);
			((CustomTable)dge).setColwidths(colwidths);
		} else if (StereotypesHelper.hasStereotypeOrDerived(cba, DocGen3Profile.userScriptStereotype) || (a != null && StereotypesHelper.hasStereotypeOrDerived(a, DocGen3Profile.userScriptStereotype))) {
			dge = new UserScript();
		} else if (StereotypesHelper.hasStereotypeOrDerived(cba, DocGen3Profile.hierarchicalPropertiesTableStereotype) || (a != null && StereotypesHelper.hasStereotypeOrDerived(a, DocGen3Profile.hierarchicalPropertiesTableStereotype))) {
			Integer maxDepth = (Integer)getObjectProperty(cba, DocGen3Profile.hierarchicalPropertiesTableStereotype, "maxDepth", 0);
			List<String> topIncludeTypeName = DocGenUtils.getElementNames((Collection<NamedElement>)getListProperty(cba, DocGen3Profile.hierarchicalPropertiesTableStereotype, "topIncludeTypeName", new ArrayList<Property>()));
			List<String> topExcludeTypeName = DocGenUtils.getElementNames((Collection<NamedElement>)getListProperty(cba, DocGen3Profile.hierarchicalPropertiesTableStereotype, "topExcludeTypeName", new ArrayList<Property>()));
			List<Stereotype> topIncludeStereotype = (List<Stereotype>)getListProperty(cba, DocGen3Profile.hierarchicalPropertiesTableStereotype, "topIncludeStereotype", new ArrayList<Stereotype>());
			List<Stereotype> topExcludeStereotype = (List<Stereotype>)getListProperty(cba, DocGen3Profile.hierarchicalPropertiesTableStereotype, "topExcludeStereotype", new ArrayList<Stereotype>());
			List<String> topIncludeName = DocGenUtils.getElementNames((Collection<NamedElement>)getListProperty(cba, DocGen3Profile.hierarchicalPropertiesTableStereotype, "topIncludeName", new ArrayList<Property>()));
			List<String> topExcludeName = DocGenUtils.getElementNames((Collection<NamedElement>)getListProperty(cba, DocGen3Profile.hierarchicalPropertiesTableStereotype, "topExcludeName", new ArrayList<Property>()));
			Integer topAssociationType = (Integer)getObjectProperty(cba, DocGen3Profile.hierarchicalPropertiesTableStereotype, "topAssociationType", 0);
			List<String> topOrder = DocGenUtils.getElementNames((Collection<NamedElement>)getListProperty(cba, DocGen3Profile.hierarchicalPropertiesTableStereotype, "topOrder", new ArrayList<Property>()));
	    	if (!topIncludeName.isEmpty() && topOrder.isEmpty())
	    		topOrder = topIncludeName;
			if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.propertiesTableByAttributesStereotype) || (a != null && StereotypesHelper.hasStereotype(a, DocGen3Profile.propertiesTableByAttributesStereotype))) {
				List<Stereotype> splitStereotype = (List<Stereotype>)getListProperty(cba, DocGen3Profile.propertiesTableByAttributesStereotype, "splitStereotype", new ArrayList<Stereotype>());
				List<Stereotype> systemIncludeStereotype = (List<Stereotype>)getListProperty(cba, DocGen3Profile.propertiesTableByAttributesStereotype, "systemIncludeStereotype", new ArrayList<Stereotype>());
				List<Stereotype> systemExcludeStereotype = (List<Stereotype>)getListProperty(cba, DocGen3Profile.propertiesTableByAttributesStereotype, "systemExcludeStereotype", new ArrayList<Stereotype>());
				List<String> systemIncludeTypeName = DocGenUtils.getElementNames((Collection<NamedElement>)getListProperty(cba, DocGen3Profile.propertiesTableByAttributesStereotype, "systemIncludeTypeName", new ArrayList<Property>()));
				List<String> systemExcludeTypeName = DocGenUtils.getElementNames((Collection<NamedElement>)getListProperty(cba, DocGen3Profile.propertiesTableByAttributesStereotype, "systemExcludeTypeName", new ArrayList<Property>()));
				List<String> systemIncludeName = DocGenUtils.getElementNames((Collection<NamedElement>)getListProperty(cba, DocGen3Profile.propertiesTableByAttributesStereotype, "systemIncludeName", new ArrayList<Property>()));
				List<String> systemExcludeName = DocGenUtils.getElementNames((Collection<NamedElement>)getListProperty(cba, DocGen3Profile.propertiesTableByAttributesStereotype, "systemExcludeName", new ArrayList<Property>()));
				Integer systemAssociationType = (Integer)getObjectProperty(cba, DocGen3Profile.propertiesTableByAttributesStereotype, "systemAssociationType", 0);
				Boolean consolidateTypes = (Boolean)getObjectProperty(cba, DocGen3Profile.propertiesTableByAttributesStereotype, "consolidateTypes", false);
				Boolean showMultiplicity = (Boolean)getObjectProperty(cba, DocGen3Profile.propertiesTableByAttributesStereotype, "showMultiplicity", false);
				Boolean doRollup = (Boolean)getObjectProperty(cba, DocGen3Profile.propertiesTableByAttributesStereotype, "doRollup", false);
				List<String> rollupProperty = DocGenUtils.getElementNames((Collection<NamedElement>)getListProperty(cba, DocGen3Profile.propertiesTableByAttributesStereotype, "rollupProperty", new ArrayList<Property>()));
				dge = new PropertiesTableByAttributes();
				((PropertiesTableByAttributes)dge).setSplitStereotype(splitStereotype);
				((PropertiesTableByAttributes)dge).setSystemIncludeStereotype(systemIncludeStereotype);
				((PropertiesTableByAttributes)dge).setSystemExcludeStereotype(systemExcludeStereotype);
				((PropertiesTableByAttributes)dge).setSystemIncludeName(systemIncludeName);
				((PropertiesTableByAttributes)dge).setSystemExcludeName(systemExcludeName);
				((PropertiesTableByAttributes)dge).setSystemIncludeTypeName(systemIncludeTypeName);
				((PropertiesTableByAttributes)dge).setSystemExcludeTypeName(systemExcludeTypeName);
				((PropertiesTableByAttributes)dge).setSystemAssociationType(systemAssociationType);
				((PropertiesTableByAttributes)dge).setConsolidateTypes(consolidateTypes);
				((PropertiesTableByAttributes)dge).setShowMultiplicity(showMultiplicity);
				((PropertiesTableByAttributes)dge).setDoRollup(doRollup);
				((PropertiesTableByAttributes)dge).setRollupProperty(rollupProperty);
			}
			((HierarchicalPropertiesTable)dge).setFloatingPrecision(floatingPrecision);
			((HierarchicalPropertiesTable)dge).setMaxDepth(maxDepth);
			((HierarchicalPropertiesTable)dge).setTopIncludeTypeName(topIncludeTypeName);
			((HierarchicalPropertiesTable)dge).setTopExcludeTypeName(topExcludeTypeName);
			((HierarchicalPropertiesTable)dge).setTopIncludeStereotype(topIncludeStereotype);
			((HierarchicalPropertiesTable)dge).setTopExcludeStereotype(topExcludeStereotype);
			((HierarchicalPropertiesTable)dge).setTopIncludeName(topIncludeName);
			((HierarchicalPropertiesTable)dge).setTopExcludeName(topExcludeName);
			((HierarchicalPropertiesTable)dge).setTopAssociationType(topAssociationType);
			((HierarchicalPropertiesTable)dge).setTopOrder(topOrder);
			((HierarchicalPropertiesTable)dge).setShowCaptions(showCaptions);
			((HierarchicalPropertiesTable)dge).setCaptions(captions);
			((HierarchicalPropertiesTable)dge).setStereotypeProperties(stereotypeProperties);
			((HierarchicalPropertiesTable)dge).setIncludeDoc(showDoc);
			((HierarchicalPropertiesTable)dge).setIncludeInherited(includeInherited);
			((HierarchicalPropertiesTable)dge).setStyle(style);
		} else if (StereotypesHelper.hasStereotypeOrDerived(cba, DocGen3Profile.workpackageTablesStereotype) || (a != null && StereotypesHelper.hasStereotypeOrDerived(a, DocGen3Profile.workpackageTablesStereotype))) {
			Element workpackage = (Element)getObjectProperty(cba, DocGen3Profile.workpackageTablesStereotype, "workpackage", null);
			Boolean doRollup = (Boolean)getObjectProperty(cba, DocGen3Profile.workpackageTablesStereotype, "doRollup", false);
			Boolean suppliesAsso = (Boolean)getObjectProperty(cba, DocGen3Profile.workpackageTablesStereotype, "suppliesAsso", false);
			Boolean authorizesAsso = (Boolean)getObjectProperty(cba, DocGen3Profile.workpackageTablesStereotype, "authorizesAsso", false);
			Boolean sortByName = (Boolean)getObjectProperty(cba, DocGen3Profile.workpackageTablesStereotype, "sortDeploymentByName", false);
			Boolean showProducts = (Boolean)getObjectProperty(cba, DocGen3Profile.workpackageTablesStereotype, "showProducts", true);
			Boolean showMassMargin = (Boolean)getObjectProperty(cba, DocGen3Profile.workpackageTablesStereotype, "showMassMargin", false);
			if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.billOfMaterialsStereotype) || (a != null && StereotypesHelper.hasStereotype(a, DocGen3Profile.billOfMaterialsStereotype))) {
				dge = new BillOfMaterialsTable();
			} else if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.deploymentStereotype) || (a != null && StereotypesHelper.hasStereotype(a, DocGen3Profile.deploymentStereotype))) {
				dge = new DeploymentTable();
			} else if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.workpakcageAssemblyStereotype) || (a != null && StereotypesHelper.hasStereotype(a, DocGen3Profile.workpakcageAssemblyStereotype))) {
				dge = new WorkpackageAssemblyTable();
			}
			((WorkpackageTable)dge).setCaptions(captions);
			((WorkpackageTable)dge).setShowCaptions(showCaptions);
			((WorkpackageTable)dge).setFloatingPrecision(floatingPrecision);
			((WorkpackageTable)dge).setWorkpackage(workpackage);
			((WorkpackageTable)dge).setDoRollup(doRollup);
			((WorkpackageTable)dge).setIncludeInherited(includeInherited);
			((WorkpackageTable)dge).setSuppliesAsso(suppliesAsso);
			((WorkpackageTable)dge).setAuthorizesAsso(authorizesAsso);
			((WorkpackageTable)dge).setSortByName(sortByName);
			((WorkpackageTable)dge).setShowProducts(showProducts);
			((WorkpackageTable)dge).setStyle(style);
			((WorkpackageTable)dge).setShowMassMargin(showMassMargin);
		} else if (StereotypesHelper.hasStereotypeOrDerived(cba, DocGen3Profile.missionMappingStereotype) || (a != null && StereotypesHelper.hasStereotypeOrDerived(a, DocGen3Profile.missionMappingStereotype))) {
			dge = new MissionMapping();
		} else if (StereotypesHelper.hasStereotypeOrDerived(cba, DocGen3Profile.libraryChooserStereotype) || (a != null && StereotypesHelper.hasStereotypeOrDerived(a, DocGen3Profile.libraryChooserStereotype))) {
			dge = new LibraryMapping();
		}
		return dge;
	}
	
	/**
	 * traverses elements contained in a TableStructure (here on: tStruct) and accordingly extracts relevant properties/elements/etc
	 * @param cba a StructuredActivityNode w/tStruct stereotype 
	 * @param parent wherever we're adding the table
	 */
	GUILog parseTS = Application.getInstance().getGUILog(); //debug! TODO: remove
	private boolean debug = true; // debug! TODO: remove
	
	private void parseTableStructure(StructuredActivityNode san, Container parent) {

		TableStructure ts = new TableStructure();
		List<Element> rows = targets.peek().isEmpty()?new ArrayList<Element>():targets.peek();
		List<String> hs = new ArrayList();
		boolean hasBehavior = false;

		if (rows == null) return;
		ActivityNode curNode = findInitialNode(san);
		ActivityNode bNode = null;
		if (curNode == null) return;
		Collection<ActivityEdge> outs = curNode.getOutgoing();
		while (outs != null && outs.size() == 1) {
			curNode = outs.iterator().next().getTarget();
			// Find out if have a behavior
			if (curNode instanceof CallBehaviorAction && ((CallBehaviorAction)curNode).getBehavior() != null) {
				bNode = findInitialNode(((CallBehaviorAction)curNode).getBehavior());
				hasBehavior = true;
				//				parseTS.log("INIT BEHAVIOR NODE?: " + bNode.getName()); //debug! TODO: remove
			}
			boolean hasTablePropColStereoType = StereotypesHelper.hasStereotype(curNode, DocGen3Profile.tablePropertyColumnStereotype );
			boolean hasTableExprColStereoType = StereotypesHelper.hasStereotype(curNode, DocGen3Profile.tableExpressionColumnStereotype);
			boolean hasTableAttrColStereoType = StereotypesHelper.hasStereotype(curNode, DocGen3Profile.tableAttributeColumnStereotype);
			boolean hasTableSumRowStereoType = StereotypesHelper.hasStereotype(curNode, DocGen3Profile.tableSumRowStereotype);
			if (hasTablePropColStereoType||hasTableExprColStereoType) {
				// TablePropertyColumn tags
				Object dProp = null;
				if (hasTablePropColStereoType) {
					dProp = getObjectProperty(curNode, DocGen3Profile.tablePropertyColumnStereotype, "desiredProperty", null);
				} else if (hasTableExprColStereoType) {
					dProp = getObjectProperty(curNode, DocGen3Profile.tableExpressionColumnStereotype, "expression", null);
				}
				// Headings
				hs.add(parseTableHeadings(curNode));
				// Parse stuff
				if ( hasTableExprColStereoType ) {
					ts.parseExpressionColumn(curNode, dProp, rows);
				} else {
					if ( dProp instanceof Property ) {
						if (hasBehavior) ts.parseColumn((Property)dProp, handleTableBehavior(bNode, rows), TableStructure.propertyColumn);
						else ts.parseColumn((Property)dProp, rows, TableStructure.propertyColumn);
					} else if (dProp == null) {
						if (hasBehavior) ts.parseColumn((Property)dProp, handleTableBehavior(bNode, rows), TableStructure.propertyColumn);
						else {
							Debug.error( false, "Expected Property but got null" );
						}
					} else {
						Debug.error( false, "Expected Property but got: "
								+ dProp
								+ ( dProp == null ? "" : " of type "
										+ dProp.getClass()
										.getName() ) );

					}
				}
			} else if (hasTableAttrColStereoType) {
				// TableAttributeColumn tags
				Object dAttr = getObjectProperty(curNode, DocGen3Profile.tableAttributeColumnStereotype, "desiredAttribute", null);
				// Headings
				hs.add(parseTableHeadings(curNode));
				// Parse stuff
				if (hasBehavior) ts.parseColumn((Object)dAttr, handleTableBehavior(bNode, rows), TableStructure.attributeColumn);
				else ts.parseColumn(dAttr, rows, TableStructure.attributeColumn);
			} else if (hasTableSumRowStereoType) {
				ts.addSumRow();
			}
			if (hasBehavior)
				hasBehavior = false;
			outs = curNode.getOutgoing();
		}

		ts.setHeaders(hs);
		parent.addElement(ts);
	}
	
	private String parseTableHeadings(ActivityNode curNode) {
		String cName = ((NamedElement)curNode).getName();
		// Heading choice branch
		if (cName != null && !cName.equals(""))
			return cName;
		else 
			return new String();
	}
	
	private List<Object> handleTableBehavior(ActivityNode bNode, List<Element> rowsIn) {
		List<Object> rowsOut = new ArrayList<Object>();
		
		// Find the first collect/filter activitynode in the behavior
		// IMPORTANT INVARIANT: assumes the activity whose initial node is passed as bNode ONLY
		//   contains collect or filter action first!
		if (bNode != null) {
			if (bNode.getOutgoing() != null) {
				bNode = bNode.getOutgoing().iterator().next().getTarget();
			}
		} 
	
		// Loop through row elements, passing each one as a target to the startCollectandFilter thing
		for (Element r: rowsIn) {
			List<Element> rAsTargets = new ArrayList<Element>();
			rAsTargets.add(r);
//			targets.push(rAsTargets);
			rowsOut.add(startCollectAndFilterSequence(bNode, rAsTargets));
		}
		
		return rowsOut; // TODO: for some reason, this is always an empty list. Why?
	}
		
	/**
	 * an activity that should only has collect/filter actions in it
	 * @param a
	 * @param in
	 * @return
	 */
	private List<Element> collectAndFilterGroup(Activity a, List<Element> in) {
		InitialNode initial = findInitialNode(a);
		Collection<ActivityEdge> outs = initial.getOutgoing();
		List<Element> res = in;
		if (outs != null && outs.size() == 1) {
			ActivityNode n = outs.iterator().next().getTarget();
			if (StereotypesHelper.hasStereotypeOrDerived(n, DocGen3Profile.collectFilterStereotype) || 
					n instanceof CallBehaviorAction && ((CallBehaviorAction)n).getBehavior() != null && 
					StereotypesHelper.hasStereotypeOrDerived(((CallBehaviorAction)n).getBehavior(), DocGen3Profile.collectFilterStereotype)) {
				res = startCollectAndFilterSequence(n, in);
			}
		}
		return res;
	}
	
	/**
	 * gets a graph of the collect/filter actions that starts from a, evaluates and executes actions topologically and return result
	 * @param a
	 * @param in
	 * @return
	 */
	private List<Element> startCollectAndFilterSequence(ActivityNode a, List<Element> in) {
		DirectedGraphHashSet<CollectFilterNode, DirectedEdgeVector<CollectFilterNode>> graph = new DirectedGraphHashSet<CollectFilterNode, DirectedEdgeVector<CollectFilterNode>>();
		getCollectFilterGraph(a, new HashSet<ActivityNode>(), graph, new HashMap<ActivityNode, CollectFilterNode>());
		SortedSet<CollectFilterNode> reverse = (new TopologicalSort()).topological_sort(graph);
		List<CollectFilterNode> toposort = new ArrayList<CollectFilterNode>(reverse);
		Collections.reverse(toposort);

		List<Element> res = null;
		for (CollectFilterNode node: toposort) {
			Set<CollectFilterNode> incomings = new HashSet<CollectFilterNode>();
			for (DirectedEdgeVector<CollectFilterNode> edge: graph.findEdgesWithTargetVertex(node)) {
				incomings.add(edge.getSourceVertex());
			}
			Set<List<Element>> ins = new HashSet<List<Element>>();
			if (incomings.isEmpty()) {
				if (in == null) {
					if (!targets.isEmpty())
						ins.add(targets.peek());
				} else {
					ins.add(in);
				}
			} else {
				for (CollectFilterNode i: incomings) {
					ins.add(i.getResult());
				}
			}
			if (node.getNode() instanceof CallBehaviorAction) {
				if (ins.size() == 1) {
					res = collectAndFilter((CallBehaviorAction)node.getNode(), ins.iterator().next());
					node.setResult(res);
				} else {
					//???
					res = new ArrayList<Element>();
					node.setResult(res);
				}
			} else if (node.getNode() instanceof ForkNode) {
				if (ins.size() == 1) {
					res = ins.iterator().next();
					node.setResult(res);
				} else {
					res = new ArrayList<Element>();
					node.setResult(res);
					//???
				}
			} else if (node.getNode() instanceof MergeNode) {
				res = Utils.unionOfCollections(ins);
				node.setResult(res);
			} else if (node.getNode() instanceof JoinNode) {
				res = Utils.intersectionOfCollections(ins);
				node.setResult(res);
			} else if (node.getNode() instanceof DecisionNode) {
				res = Utils.xorOfCollections(ins);
				node.setResult(res);
			}
			current = node.getNode();
		}
		return res;
	}
	
	private void getCollectFilterGraph(ActivityNode cur, Set<ActivityNode> done, DirectedGraphHashSet<CollectFilterNode, DirectedEdgeVector<CollectFilterNode>> graph, Map<ActivityNode, CollectFilterNode> mapping) {
		if (done.contains(cur))
			return;
		done.add(cur);
		CollectFilterNode source = mapping.get(cur);
		if (source == null) {
			source = new CollectFilterNode(cur);
			mapping.put(cur, source);
		}
		graph.addVertex(source);
		for (ActivityEdge e: cur.getOutgoing()) {
			ActivityNode n = e.getTarget();
			if (StereotypesHelper.hasStereotypeOrDerived(n, DocGen3Profile.collectFilterStereotype) || 
					n instanceof CallBehaviorAction && ((CallBehaviorAction)n).getBehavior() != null && 
					StereotypesHelper.hasStereotypeOrDerived(((CallBehaviorAction)n).getBehavior(), DocGen3Profile.collectFilterStereotype)) {
				CollectFilterNode target = mapping.get(n);
				if (target == null) {
					target = new CollectFilterNode(n);
					mapping.put(n, target);
				}
				graph.addEdge(source, target);
				getCollectFilterGraph(n, done, graph, mapping);
			}
		}
	}
	
	/**
	 * given in as input, execute collect/filter action and return result
	 * @param cba
	 * @param in
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private List<Element> collectAndFilter(CallBehaviorAction cba, List<Element> in) {
		Element a = cba.getBehavior();
		Integer depth = (Integer)getObjectProperty(cba, DocGen3Profile.depthChoosable, "depth", 0);
		int direction = ((Boolean)getObjectProperty(cba, DocGen3Profile.directionChoosable, "directionOut", true)) ? 1 : 2;
		List<Stereotype> stereotypes = (List<Stereotype>)getListProperty(cba, DocGen3Profile.stereotypeChoosable, "stereotypes", new ArrayList<Stereotype>());
		List<Class> metaclasses = (List<Class>)getListProperty(cba, DocGen3Profile.metaclassChoosable, "metaclasses", new ArrayList<Class>());
		Boolean derived = (Boolean)getObjectProperty(cba, DocGen3Profile.derivedChoosable, "considerDerived", true);
		List<String> names = (List<String>)getListProperty(cba, DocGen3Profile.nameChoosable, "names", new ArrayList<String>());
		List<String> diagramTypes = Utils.getElementNames((List<NamedElement>)getListProperty(cba, DocGen3Profile.diagramTypeChoosable, "diagramTypes", new ArrayList<NamedElement>()));
		Boolean include = (Boolean)getObjectProperty(cba, DocGen3Profile.includeChoosable, "include", true);
		List<Property> stereotypeProperties = (List<Property>)getListProperty(cba, DocGen3Profile.stereotypePropertyChoosable, "stereotypeProperties", new ArrayList<Property>());
		Boolean inherited = (Boolean)getObjectProperty(cba, DocGen3Profile.inheritedChoosable, "includeInherited", false);
		EnumerationLiteral asso = (EnumerationLiteral)getObjectProperty(cba, DocGen3Profile.associationChoosable, "associationType", null);
		AggregationKind associationType = null;
		if (asso != null) {
			if (asso.getName().equals("composite"))
				associationType = AggregationKindEnum.COMPOSITE;
			else if (asso.getName().equals("none"))
				associationType = AggregationKindEnum.NONE;
			else
				associationType = AggregationKindEnum.SHARED;
		}
		if (associationType == null)
			associationType = AggregationKindEnum.COMPOSITE;
		List<Element> res = new ArrayList<Element>();
		
		if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.collectDiagram) || (a != null && StereotypesHelper.hasStereotype(a, DocGen3Profile.collectDiagram))) {
			for (Element e: in)
				if (e instanceof Diagram)
					res.addAll(Utils.getElementsOnDiagram((Diagram)e));
		} else if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.collectAssociationStereotype) || (a != null && StereotypesHelper.hasStereotype(a, DocGen3Profile.collectAssociationStereotype))) {
			for (Element e: in)
				res.addAll(Utils.collectAssociatedElements(e, depth, associationType));
		} else if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.collectOwnedElementStereotype) || (a != null && StereotypesHelper.hasStereotype(a, DocGen3Profile.collectOwnedElementStereotype))) {
			for (Element e: in)
				res.addAll(Utils.collectOwnedElements(e, depth));
		} else if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.collectOwnerStereotype) || (a != null && StereotypesHelper.hasStereotype(a, DocGen3Profile.collectOwnerStereotype))) {
			for (Element e: in)
				res.addAll(Utils.collectOwners(e, depth));
		} else if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.collectRelMetaclassStereotype) || (a!= null && StereotypesHelper.hasStereotype(a, DocGen3Profile.collectRelMetaclassStereotype))) {
			for (Element e: in)
				res.addAll(Utils.collectDirectedRelatedElementsByRelationshipMetaclasses(e, metaclasses, direction, depth));
		} else if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.collectRelStereotypeStereotype) || (a != null && StereotypesHelper.hasStereotype(a, DocGen3Profile.collectRelStereotypeStereotype))) {
			for (Element e: in)
				res.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotypes(e, stereotypes, direction, derived, depth));
		} else if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.collectStereotypePropStereotype) || (a != null && StereotypesHelper.hasStereotype(a, DocGen3Profile.collectStereotypePropStereotype))) {
			List<Object> blah = new ArrayList<Object>();
			for (Element e: in)
				for (Property p: stereotypeProperties)
					blah.addAll(Utils.getStereotypePropertyValues(e, p));
			for (Object b:blah)
				if (b instanceof Element)
					res.add((Element)b);
		} else if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.collectTypeStereotype) || (a != null && StereotypesHelper.hasStereotype(a, DocGen3Profile.collectTypeStereotype))) {
			for (Element e: in) {
				if (e instanceof TypedElement) {
					if (((TypedElement)e).getType() != null) {
						res.add(((TypedElement)e).getType());
					}
				} else if (e instanceof CallBehaviorAction && ((CallBehaviorAction)e).getBehavior() != null) {
					res.add(((CallBehaviorAction)e).getBehavior());
				} else if (e instanceof CallOperationAction && ((CallOperationAction)e).getOperation() != null) {
					res.add(((CallOperationAction)e).getOperation());
				}
			}
		} else if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.collectClassifierAttributes) || (a != null && StereotypesHelper.hasStereotype(a, DocGen3Profile.collectClassifierAttributes))) {
				for (Element e: in)
					res.addAll(Utils.getAttributes(e, inherited));
		} else if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.filterDiagramTypeStereotype) || (a != null && StereotypesHelper.hasStereotype(a, DocGen3Profile.filterDiagramTypeStereotype))) {
			res.addAll(Utils.filterDiagramsByDiagramTypes(in, diagramTypes, include));
		} else if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.filterMetaclassStereotype) || (a != null && StereotypesHelper.hasStereotype(a, DocGen3Profile.filterMetaclassStereotype))) {
			res.addAll(Utils.filterElementsByMetaclasses(in, metaclasses, include));
		} else if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.filterNameStereotype) || (a != null && StereotypesHelper.hasStereotype(a, DocGen3Profile.filterNameStereotype))) {
			res.addAll(Utils.filterElementsByNameRegex(in, names, include));
		} else if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.filterStereotypeStereotype) || (a != null && StereotypesHelper.hasStereotype(a, DocGen3Profile.filterStereotypeStereotype))) {
			res.addAll(Utils.filterElementsByStereotypes(in, stereotypes, include, derived));
		} else if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.collectionStereotype) || (a != null && StereotypesHelper.hasStereotype(a, DocGen3Profile.collectionStereotype))) {
			res.addAll(collectAndFilterGroup((Activity)cba.getBehavior(), in));
		} else if (StereotypesHelper.hasStereotype(cba, DocGen3Profile.removeDuplicates) || (a != null && StereotypesHelper.hasStereotype(a, DocGen3Profile.removeDuplicates))) {
			res.addAll(Utils.removeDuplicates(in));
		} else if (StereotypesHelper.hasStereotypeOrDerived(cba, DocGen3Profile.userScriptCFStereotype) || (a != null && StereotypesHelper.hasStereotypeOrDerived(a, DocGen3Profile.userScriptCFStereotype))) {
			res.addAll(getUserScriptCF(in, cba));
		} else if (StereotypesHelper.hasStereotypeOrDerived(cba, DocGen3Profile.sortByName) || (a != null && StereotypesHelper.hasStereotypeOrDerived(a, DocGen3Profile.sortByName))) {
			res.addAll(Utils.sortByName(in));
		} 
		Object derp = res;
		return res;
	}
	
	/**
	 * collect/filter action can be a userscript - input and output are both collection of elements
	 * @param in
	 * @param cba
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private List<Element> getUserScriptCF(List<Element> in, CallBehaviorAction cba) {
		List<Element> res = new ArrayList<Element>();
		try {
			Map<String, Object> inputs = new HashMap<String, Object>();
			inputs.put("DocGenTargets", in);
			Element e = cba;
			if (!StereotypesHelper.hasStereotypeOrDerived(cba, DocGen3Profile.userScriptCFStereotype))
				if (cba.getBehavior() != null && StereotypesHelper.hasStereotypeOrDerived(cba.getBehavior(), DocGen3Profile.userScriptCFStereotype))
					e = ((CallBehaviorAction)e).getBehavior();
			Object o = ScriptRunner.runScriptFromStereotype(e, StereotypesHelper.checkForDerivedStereotype(e, DocGen3Profile.userScriptCFStereotype), inputs);
			if (o != null && o instanceof Map && ((Map)o).containsKey("DocGenOutput")) {
				Object l = ((Map)o).get("DocGenOutput");
				if (l instanceof List) {
					for (Object oo: (List)l) {
						if (oo instanceof Element)
							res.add((Element)oo);
					}
				}
			}
		} catch (ScriptException ex) {
			ex.printStackTrace();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			log.log(sw.toString()); // stack trace as a string
		}
		return res;
	}
	
	public static Element findStereotypedRelationship(Element e, String s) {
		Stereotype stereotype = StereotypesHelper.getStereotype(Application.getInstance().getProject(), s);
		List<Stereotype> ss = new ArrayList<Stereotype>();
		ss.add(stereotype);
		List<Element> es = Utils.collectDirectedRelatedElementsByRelationshipStereotypes(e, ss, 1, true, 1);
		if (es.size() > 0) {
			return es.get(0);
		}
		return null;
	}
	
	private InitialNode findInitialNode(Element a) {
		for (Element e: a.getOwnedElement())
			if (e instanceof InitialNode)
				return (InitialNode)e;
		return null;
	}
	
	private Object getObjectProperty(Element e, String stereotype, String property, Object defaultt) {
		Object value = StereotypesHelper.getStereotypePropertyFirst(e, stereotype, property);
		if (value == null && e instanceof CallBehaviorAction && ((CallBehaviorAction)e).getBehavior() != null) {
			value = StereotypesHelper.getStereotypePropertyFirst(((CallBehaviorAction)e).getBehavior(), stereotype, property);
		}
		if (value == null)
			value = defaultt;
		return value;
	}
	
	private List<? extends Object> getListProperty(Element e, String stereotype, String property, List<? extends Object> defaultt) {
		List<? extends Object> value = StereotypesHelper.getStereotypePropertyValue(e, stereotype, property);
		if ((value == null || value.isEmpty()) && e instanceof CallBehaviorAction && ((CallBehaviorAction)e).getBehavior() != null) {
			value = StereotypesHelper.getStereotypePropertyValue(((CallBehaviorAction)e).getBehavior(), stereotype, property);
		}
		if (value == null || value.isEmpty())
			value = defaultt;
		return value;
	}
}
