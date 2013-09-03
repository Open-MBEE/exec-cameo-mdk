package gov.nasa.jpl.mbee.lib;

import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.model.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.InitialNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Association;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.TypedElement;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.Behavior;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class GeneratorUtils {
	
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
	
	public static InitialNode findInitialNode(Element a) {
		for (Element e: a.getOwnedElement())
			if (e instanceof InitialNode)
				return (InitialNode)e;
		return null;
	}
	
	public static Object getObjectProperty(Element e, String stereotype, String property, Object defaultt) {
		Object value = StereotypesHelper.getStereotypePropertyFirst(e, stereotype, property);
		if (value == null && e instanceof CallBehaviorAction && ((CallBehaviorAction)e).getBehavior() != null) {
			value = StereotypesHelper.getStereotypePropertyFirst(((CallBehaviorAction)e).getBehavior(), stereotype, property);
		}
		if (value == null)
			value = defaultt;
		return value;
	}
	
	public static List<? extends Object> getListProperty(Element e, String stereotype, String property, List<? extends Object> defaultt) {
		List<? extends Object> value = StereotypesHelper.getStereotypePropertyValue(e, stereotype, property);
		if ((value == null || value.isEmpty()) && e instanceof CallBehaviorAction && ((CallBehaviorAction)e).getBehavior() != null) {
			value = StereotypesHelper.getStereotypePropertyValue(((CallBehaviorAction)e).getBehavior(), stereotype, property);
		}
		if (value == null || value.isEmpty())
			value = defaultt;
		return value;
	}
	
	public static boolean hasStereotypeByString(Element e, String stereotype) {
		return hasStereotypeByString(e, stereotype, false);
	}
	
	public static boolean hasStereotypeByString(Element e, String stereotype, boolean derived) {
		Behavior a = null;
		if (e instanceof CallBehaviorAction)
			a = ((CallBehaviorAction)e).getBehavior();
		if (!derived) {
			if (StereotypesHelper.hasStereotype(e, stereotype) || (a != null && StereotypesHelper.hasStereotype(a, stereotype)))
				return true;
		} else {
			if (StereotypesHelper.hasStereotypeOrDerived(e, stereotype) || (a != null && StereotypesHelper.hasStereotypeOrDerived(a, stereotype)))
				return true;
		}
		return false;
	}
	
	public static void docMetadata(Document doc, Element start) {
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
	
}
