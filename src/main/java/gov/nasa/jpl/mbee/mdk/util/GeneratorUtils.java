package gov.nasa.jpl.mbee.mdk.util;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.sysml.util.SysMLProfile;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.jmi.helpers.TagsHelper;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.activities.mdbasicactivities.InitialNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.Behavior;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import gov.nasa.jpl.mbee.mdk.SysMLExtensions;
import gov.nasa.jpl.mbee.mdk.model.Document;
import gov.nasa.jpl.mbee.mdk.model.docmeta.DocumentMeta;
import gov.nasa.jpl.mbee.mdk.model.docmeta.Person;
import gov.nasa.jpl.mbee.mdk.model.docmeta.Revision;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class GeneratorUtils {
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

    public static Object getStereotypePropertyFirst(Element element, Property property, Object defaultValue) {
        Collection<?> values = getStereotypePropertyValue(element, property, Collections.emptyList());
        return !values.isEmpty() ? values.iterator().next() : defaultValue;
    }

    public static List<?> getStereotypePropertyValue(Element element, Property property, List<?> defaultValue) {
        TaggedValue value = TagsHelper.getTaggedValue(element, property);
        List<?> values = value == null ? Collections.emptyList() : value.getValue();
        Behavior behavior;
        if (values.isEmpty() && element instanceof CallBehaviorAction && (behavior = ((CallBehaviorAction) element).getBehavior()) != null) {
            value = TagsHelper.getTaggedValue(behavior, property);
            if (value != null) {
                values = value.getValue();
            }
        }
        if (values.isEmpty()) {
            values = defaultValue;
        }
        return values;
    }

    public static boolean hasStereotype(Element e, Stereotype stereotype) {
        return hasStereotype(e, stereotype, false);
    }

    public static boolean hasStereotype(Element e, Stereotype stereotype, boolean derived) {
        Behavior a = null;
        if (e instanceof CallBehaviorAction) {
            a = ((CallBehaviorAction) e).getBehavior();
        }
        if (!derived) {
            if (StereotypesHelper.hasStereotype(e, stereotype)
                    || (a != null && StereotypesHelper.hasStereotype(a, stereotype))) {
                return true;
            }
        } else {
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
        SysMLExtensions profile = SysMLExtensions.getInstance(start);
        SysMLExtensions.ProductStereotype product = profile.product();
        SysMLExtensions.DocumentMetaStereotype m = profile.documentMeta();
        // documentMeta Backwards Compatibility
        String title = m.getTitle(start);
        String subtitle = m.getSubtitle(start);
        List<String> header = m.getHeader(start);
        List<String> footer = m.getFooter(start);
        String legalNotice = m.getLegalNotice(start);
        String acknowledgements = m.getAcknowledgements(start);
        Boolean chunkFirstSectionsO = m.isChunkFirstSections(start);
        Diagram coverImage = (Diagram) m.getCoverImage(start);
        boolean chunkFirstSections = !(chunkFirstSectionsO instanceof Boolean && !(Boolean) chunkFirstSectionsO);
        Boolean indexO = m.isIndex(start);
        boolean index = (indexO instanceof Boolean && (Boolean) indexO);
        Integer tocSectionDepthO = m.getTocSectionDepth(start);
        Integer tocSectionDepth = 20;
        if (tocSectionDepthO != null && tocSectionDepthO instanceof Integer && (Integer) tocSectionDepthO > 0) {
            tocSectionDepth = (Integer) tocSectionDepthO;
        }
        Integer chunkSectionDepthO = m.getChunkSectionDepth(start);
        Integer chunkSectionDepth = 20;
        if (chunkSectionDepthO != null && chunkSectionDepthO instanceof Integer
                && (Integer) chunkSectionDepthO > 0) {
            chunkSectionDepth = (Integer) chunkSectionDepthO;
        }

        // Document View Settings
        String LogoAlignment = product.getLogoAlignment(start);
        String LogoLocation = product.getLogoLocation(start);
        String AbbreviatedProjectName = product.getProjectAcronym(start);

        String AbbreiviatedTitle = product.getDocumentAcronym(start);
        String TitlePageLegalNotice = product.getTitlePageLegalNotice(start);
        String FooterLegalNotice = product.getFooterLegalNotice(start);
        Boolean RemoveBlankPages = product.isRemoveBlankPages(start);

        List<String> CollaboratorEmail = product.getCollaboratorEmail(start);
        List<String> RevisionHistory = product.getRevisionHistory(start);
        String JPLProjectTitle = product.getFormalProjectTitle(start);

        String LogoSize = product.getLogoSize(start);
        Boolean UseDefaultStylesheetO = product.isSupressMetadata(start);
        boolean UseDefaultStylesheet = !(UseDefaultStylesheetO instanceof Boolean
                && !(Boolean) UseDefaultStylesheetO);

        Boolean genO = product.isGenNewImages(start);
        boolean gen = (genO instanceof Boolean && genO);

        if (title == null || title.isEmpty()) {
            title = ((NamedElement) start).getName();
        }

        // Institutional Logo setup
        String instLogo = product.getInstLogo(start);
        String instLogoSize = product.getInstLogoSize(start);
        String instTxt1 = product.getInsttxt1(start);
        String instTxt2 = product.getInsttxt2(start);

        // Collect author information
        List<String> Author = product.getAuthor(start);
        // Collect approver information
        List<String> Approver = product.getApprover(start);
        // Collect concurrence information
        List<String> Concurrence = product.getConcurrence(start);

        doc.setChunkFirstSections(chunkFirstSections);
        doc.setChunkSectionDepth(chunkSectionDepth);
        doc.setFooter(footer.size() == 0 ? null : footer.get(0));
        doc.setHeader(header.size() == 0 ? null : header.get(0));
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
        meta.setFooter(footer.size() == 0 ? null : footer.get(0));
        meta.setHeader(header.size() == 0 ? null : header.get(0));
        meta.setTitlePageLegalNotice(legalNotice);
        meta.setIndex(index);
        meta.setSubtitle(subtitle);
        meta.setTitle(title);
        meta.setTocSectionDepth(tocSectionDepth);
        meta.setLogoAlignment(LogoAlignment);
        meta.setLogoLink(LogoLocation);
        meta.setProjectAcronym(AbbreviatedProjectName);
        meta.setDocumentAcronym(AbbreiviatedTitle);
        meta.setTitlePageLegalNotice(TitlePageLegalNotice);
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

    public static Behavior getViewpointMethod(Classifier viewpoint, Project project) {
        List<?> methods = SysMLProfile.getInstanceByProject(project).viewpoint().getMethod(viewpoint);
        if (methods == null || methods.isEmpty()) {
            return null;
        }
        if (!(methods.get(0) instanceof Behavior)) {
            return null;
        }
        return (Behavior) methods.get(0);
    }
}
