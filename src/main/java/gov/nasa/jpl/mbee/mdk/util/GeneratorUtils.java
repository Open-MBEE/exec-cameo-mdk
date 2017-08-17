package gov.nasa.jpl.mbee.mdk.util;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml2.util.UML2ModelUtil;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.InitialNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.Behavior;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Profile;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenProfile;
import gov.nasa.jpl.mbee.mdk.model.Document;
import gov.nasa.jpl.mbee.mdk.model.docmeta.DocumentMeta;
import gov.nasa.jpl.mbee.mdk.model.docmeta.Person;
import gov.nasa.jpl.mbee.mdk.model.docmeta.Revision;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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

    public static Element findStereotypedRelationship(Element e, Stereotype s) {
        List<Stereotype> ss = new ArrayList<Stereotype>();
        ss.add(s);
        List<Element> es = Utils.collectDirectedRelatedElementsByRelationshipStereotypes(e, ss, 1, true, 1);
        if (es.size() > 0) {
            return es.get(0);
        }
        return null;
    }

    public static InitialNode findInitialNode(Element a) {
        if (a == null) {
            return null;
        }
        for (Element e : a.getOwnedElement()) {
            if (e instanceof InitialNode) {
                return (InitialNode) e;
            }
        }
        return null;
    }

    public static Object getStereotypePropertyFirst(Element element, String stereotypeName, String propertyName, String profileName, Object defaultValue) {
        Collection<?> values = getStereotypePropertyValue(element, stereotypeName, propertyName, profileName, Collections.emptyList());
        return !values.isEmpty() ? values.iterator().next() : defaultValue;
    }

    public static List<?> getStereotypePropertyValue(Element element, String stereotypeName, String propertyName, String profileName, List<?> defaultValue) {
        Project project = Project.getProject(element);
        Profile profile = StereotypesHelper.getProfile(project, profileName);
        Stereotype stereotype = StereotypesHelper.getStereotype(project, stereotypeName, profile);
        List<?> value = StereotypesHelper.getStereotypePropertyValue(element, stereotype, propertyName);
        Behavior behavior;
        if (value.isEmpty() && element instanceof CallBehaviorAction && (behavior = ((CallBehaviorAction) element).getBehavior()) != null) {
            value = StereotypesHelper.getStereotypePropertyValue(behavior, stereotype, propertyName);
        }
        if (value.isEmpty()) {
            value = defaultValue;
        }
        return value;
    }

    public static boolean hasStereotypeByString(Element e, String stereotype) {
        return hasStereotypeByString(e, stereotype, false);
    }

    public static boolean hasStereotypeByString(Element e, String stereotype, boolean derived) {
        Behavior a = null;
        if (e instanceof CallBehaviorAction) {
            a = ((CallBehaviorAction) e).getBehavior();
        }
        if (!derived) {
            if (StereotypesHelper.hasStereotype(e, stereotype)
                    || (a != null && StereotypesHelper.hasStereotype(a, stereotype))) {
                return true;
            }
        }
        else {
            if (StereotypesHelper.hasStereotypeOrDerived(e, stereotype)
                    || (a != null && StereotypesHelper.hasStereotypeOrDerived(a, stereotype))) {
                return true;
            }
        }
        return false;
    }

    public static void docMetadata(Document doc, Element start) {
        DocumentMeta meta = new DocumentMeta();
        doc.setMetadata(meta);

        Stereotype documentView = StereotypesHelper.getStereotype(Application.getInstance().getProject(),
                DocGenProfile.documentViewStereotype);
        // documentMeta Backwards Compatibility
        String title = (String) StereotypesHelper.getStereotypePropertyFirst(start,
                DocGenProfile.documentMetaStereotype, "title");
        String subtitle = (String) StereotypesHelper.getStereotypePropertyFirst(start,
                DocGenProfile.documentMetaStereotype, "subtitle");
        String header = (String) StereotypesHelper.getStereotypePropertyFirst(start,
                DocGenProfile.documentMetaStereotype, "header");
        String footer = (String) StereotypesHelper.getStereotypePropertyFirst(start,
                DocGenProfile.documentMetaStereotype, "footer");
        String subheader = (String) StereotypesHelper.getStereotypePropertyFirst(start,
                DocGenProfile.documentMetaStereotype, "subheader");
        String subfooter = (String) StereotypesHelper.getStereotypePropertyFirst(start,
                DocGenProfile.documentMetaStereotype, "subfooter");
        String legalNotice = (String) StereotypesHelper.getStereotypePropertyFirst(start,
                DocGenProfile.documentMetaStereotype, "legalNotice");
        String acknowledgements = (String) StereotypesHelper.getStereotypePropertyFirst(start,
                DocGenProfile.documentMetaStereotype, "acknowledgement");
        Object chunkFirstSectionsO = StereotypesHelper.getStereotypePropertyFirst(start,
                DocGenProfile.documentMetaStereotype, "chunkFirstSections");
        Diagram coverImage = (Diagram) StereotypesHelper.getStereotypePropertyFirst(start,
                DocGenProfile.documentMetaStereotype, "coverImage");
        boolean chunkFirstSections = !(chunkFirstSectionsO instanceof Boolean && !(Boolean) chunkFirstSectionsO || chunkFirstSectionsO instanceof String
                && chunkFirstSectionsO.equals("false"));
        Object indexO = StereotypesHelper.getStereotypePropertyFirst(start,
                DocGenProfile.documentMetaStereotype, "index");
        boolean index = (indexO instanceof Boolean && (Boolean) indexO || indexO instanceof String
                && indexO.equals("true"));
        Object tocSectionDepthO = StereotypesHelper.getStereotypePropertyFirst(start,
                DocGenProfile.documentMetaStereotype, "tocSectionDepth");
        Integer tocSectionDepth = 20;
        if (tocSectionDepthO != null && tocSectionDepthO instanceof Integer && (Integer) tocSectionDepthO > 0) {
            tocSectionDepth = (Integer) tocSectionDepthO;
        }
        if (tocSectionDepthO != null && tocSectionDepthO instanceof String) {
            tocSectionDepth = Integer.parseInt((String) tocSectionDepthO);
        }
        Object chunkSectionDepthO = StereotypesHelper.getStereotypePropertyFirst(start,
                DocGenProfile.documentMetaStereotype, "chunkSectionDepth");
        Integer chunkSectionDepth = 20;
        if (chunkSectionDepthO != null && chunkSectionDepthO instanceof Integer
                && (Integer) chunkSectionDepthO > 0) {
            chunkSectionDepth = (Integer) chunkSectionDepthO;
        }
        if (chunkSectionDepthO != null && chunkSectionDepthO instanceof String) {
            chunkSectionDepth = Integer.parseInt((String) chunkSectionDepthO);
        }

        // Document View Settings
        String DocumentID = (String) StereotypesHelper.getStereotypePropertyFirst(start, documentView,
                "Document ID");
        String DocumentVersion = (String) StereotypesHelper.getStereotypePropertyFirst(start, documentView,
                "Version");
        String LogoAlignment = (String) StereotypesHelper.getStereotypePropertyFirst(start, documentView,
                "Logo Alignment");
        String LogoLocation = (String) StereotypesHelper.getStereotypePropertyFirst(start, documentView,
                "Logo Location");
        String AbbreviatedProjectName = (String) StereotypesHelper.getStereotypePropertyFirst(start,
                documentView, "Project Acronym");
        String DocushareLink = (String) StereotypesHelper.getStereotypePropertyFirst(start, documentView,
                "Docushare Link");
        String AbbreiviatedTitle = (String) StereotypesHelper.getStereotypePropertyFirst(start, documentView,
                "Document Acronym");
        String TitlePageLegalNotice = (String) StereotypesHelper.getStereotypePropertyFirst(start,
                documentView, "Title Page Legal Notice");
        String FooterLegalNotice = (String) StereotypesHelper.getStereotypePropertyFirst(start, documentView,
                "Footer Legal Notice");
        String RemoveBlankPages = (String) StereotypesHelper.getStereotypePropertyFirst(start, documentView,
                "Remove Blank Pages");

        List<String> CollaboratorEmail = StereotypesHelper.getStereotypePropertyValueAsString(start,
                documentView, "Collaborator Email");
        List<String> RevisionHistory = StereotypesHelper.getStereotypePropertyValueAsString(start,
                documentView, "Revision History");
        String JPLProjectTitle = (String) StereotypesHelper.getStereotypePropertyFirst(start, documentView,
                "Formal Project Title");

        String LogoSize = (String) StereotypesHelper.getStereotypePropertyFirst(start, documentView,
                "Logo Size");
        Object UseDefaultStylesheetO = StereotypesHelper.getStereotypePropertyFirst(start, documentView,
                "SupressMetadata");
        boolean UseDefaultStylesheet = !(UseDefaultStylesheetO instanceof Boolean
                && !(Boolean) UseDefaultStylesheetO || UseDefaultStylesheetO instanceof String
                && UseDefaultStylesheetO.equals("false"));

        Object genO = StereotypesHelper.getStereotypePropertyFirst(start,
                DocGenProfile.documentMetaStereotype, "genNewImages");
        boolean gen = (genO instanceof Boolean && (Boolean) genO || genO instanceof String
                && genO.equals("true"));

        if (title == null || title.isEmpty()) {
            title = ((NamedElement) start).getName();
        }

        if (FooterLegalNotice == null || FooterLegalNotice.isEmpty()) {
            Property propertyByName = StereotypesHelper
                    .getPropertyByName(documentView, "Footer Legal Notice");
            if (propertyByName != null) {
                FooterLegalNotice = UML2ModelUtil.getDefault(propertyByName);
            }
        }
        if (TitlePageLegalNotice == null || TitlePageLegalNotice.isEmpty()) {
            Property propertyByName = StereotypesHelper.getPropertyByName(documentView,
                    "Title Page Legal Notice");
            if (propertyByName != null) {
                TitlePageLegalNotice = UML2ModelUtil.getDefault(propertyByName);
            }

        }

        // Institutional Logo setup
        String instLogo = (String) StereotypesHelper.getStereotypePropertyFirst(start, documentView,
                "InstLogo");
        String instLogoSize = (String) StereotypesHelper.getStereotypePropertyFirst(start, documentView,
                "InstLogoSize");
        String instTxt1 = (String) StereotypesHelper.getStereotypePropertyFirst(start, documentView,
                "Insttxt1");
        String instTxt2 = (String) StereotypesHelper.getStereotypePropertyFirst(start, documentView,
                "Insttxt2");

        // Collect author information
        List<String> Author = StereotypesHelper.getStereotypePropertyValueAsString(start, documentView,
                "Author");
        // Collect approver information
        List<String> Approver = StereotypesHelper.getStereotypePropertyValueAsString(start, documentView,
                "Approver");
        // Collect concurrence information
        List<String> Concurrence = StereotypesHelper.getStereotypePropertyValueAsString(start, documentView,
                "Concurrence");

        doc.setChunkFirstSections(chunkFirstSections);
        doc.setChunkSectionDepth(chunkSectionDepth);
        doc.setFooter(footer);
        doc.setHeader(header);
        doc.setSubfooter(subfooter);
        doc.setSubheader(subheader);
        doc.setTitle(title);
        doc.setTocSectionDepth(tocSectionDepth);

        doc.setRemoveBlankPages(RemoveBlankPages);

        doc.setUseDefaultStylesheet(UseDefaultStylesheet);

        meta.setAuthors(getPersons(Author));
        meta.setApprovers(getPersons(Approver));
        meta.setConcurrances(getPersons(Concurrence));
        meta.setHistory(getRevisions(RevisionHistory));

        meta.setCollaboratorEmails(CollaboratorEmail);
        meta.setGenNewImages(gen);
        meta.setAcknowledgement(acknowledgements);
        meta.setChunkSectionDepth(chunkSectionDepth);
        meta.setChunkFirstSections(chunkFirstSections);
        meta.setCoverImage(coverImage);
        meta.setFooter(footer);
        meta.setHeader(header);
        meta.setTitlePageLegalNotice(legalNotice);
        meta.setIndex(index);
        meta.setSubfooter(subfooter);
        meta.setSubheader(subheader);
        meta.setSubtitle(subtitle);
        meta.setTitle(title);
        meta.setTocSectionDepth(tocSectionDepth);
        meta.setDocumentId(DocumentID);
        meta.setVersion(DocumentVersion);
        meta.setLogoAlignment(LogoAlignment);
        meta.setLogoLink(LogoLocation);
        meta.setProjectAcronym(AbbreviatedProjectName);
        meta.setDocumentAcronym(AbbreiviatedTitle);
        meta.setTitlePageLegalNotice(TitlePageLegalNotice);
        meta.setLink(DocushareLink);
        meta.setFooterLegalNotice(FooterLegalNotice);
        meta.setProjectTitle(JPLProjectTitle);
        meta.setUseDefaultStyleSheet(UseDefaultStylesheet);
        meta.setLogoSize(LogoSize);
        meta.setInstituteLogoLink(instLogo);
        meta.setInstituteLogoSize(instLogoSize);
        meta.setInstituteName(instTxt1);
        meta.setInstituteName2(instTxt2);

    }

    public static List<Person> getPersons(List<String> s) {
        List<Person> ps = new ArrayList<Person>();
        for (String author : s) {
            if (author == null || author.isEmpty()) {
                continue;
            }
            String[] tokens = author.split("[,]");
            if (tokens.length < 5) {
                continue;
            }
            Person p = new Person();
            p.setFirstname(tokens[0]);
            p.setLastname(tokens[1]);
            p.setTitle(tokens[2]);
            p.setOrgname(tokens[3]);
            p.setOrgdiv(tokens[4]);
            ps.add(p);
        }
        return ps;
    }

    public static List<Revision> getRevisions(List<String> s) {
        List<Revision> rs = new ArrayList<Revision>();
        for (String rev : s) {
            if (rev == null || rev.isEmpty()) {
                continue;
            }
            String[] tokens = rev.split("[|]");
            if (tokens.length < 5) {
                continue;
            }
            Revision p = new Revision();
            p.setRevNumber(tokens[0]);
            p.setDate(tokens[1]);
            p.setFirstName(tokens[2]);
            p.setLastName(tokens[3]);
            p.setRemark(tokens[4]);
            rs.add(p);
        }
        return rs;
    }

}
