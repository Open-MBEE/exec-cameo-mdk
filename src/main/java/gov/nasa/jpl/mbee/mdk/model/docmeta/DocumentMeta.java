package gov.nasa.jpl.mbee.mdk.model.docmeta;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;

import java.util.List;

public class DocumentMeta {

    private List<Person> authors;
    private List<Person> concurrances;
    private List<Person> approvers;
    private String documentId;
    private String version;
    private String projectAcronym;
    private String link; //docushare?
    private String documentAcronym;
    private String titlePageLegalNotice;
    private String footerLegalNotice;
    private List<String> collaboratorEmails;
    private String projectTitle;
    private String instituteName; //JPL
    private String instituteName2; //Caltech
    private String logoLink;
    private String logoSize;
    private String logoAlignment;
    private String instituteLogoLink;
    private String instituteLogoSize;
    private List<Revision> history;

    //local generation/docbook specific
    private Diagram coverImage;
    private String acknowledgement;
    private boolean index;
    private String title;
    private String subtitle;
    private String header;
    private String footer;
    private String subheader;
    private String subfooter;
    private int tocSectionDepth = 20;
    private int chunkSectionDepth = 20;
    private boolean useDefaultStyleSheet = true;
    private boolean genNewImages = false;
    private boolean chunkFirstSections = false;

    public boolean isChunkFirstSections() {
        return chunkFirstSections;
    }

    public void setChunkFirstSections(boolean chunkFirstSections) {
        this.chunkFirstSections = chunkFirstSections;
    }

    public List<Person> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Person> authors) {
        this.authors = authors;
    }

    public List<Person> getConcurrances() {
        return concurrances;
    }

    public void setConcurrances(List<Person> concurrances) {
        this.concurrances = concurrances;
    }

    public List<Person> getApprovers() {
        return approvers;
    }

    public void setApprovers(List<Person> approvers) {
        this.approvers = approvers;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProjectAcronym() {
        return projectAcronym;
    }

    public void setProjectAcronym(String projectAcronym) {
        this.projectAcronym = projectAcronym;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDocumentAcronym() {
        return documentAcronym;
    }

    public void setDocumentAcronym(String documentAcronym) {
        this.documentAcronym = documentAcronym;
    }

    public String getTitlePageLegalNotice() {
        return titlePageLegalNotice;
    }

    public void setTitlePageLegalNotice(String titlePageLegalNotice) {
        this.titlePageLegalNotice = titlePageLegalNotice;
    }

    public String getFooterLegalNotice() {
        return footerLegalNotice;
    }

    public void setFooterLegalNotice(String footerLegalNotice) {
        this.footerLegalNotice = footerLegalNotice;
    }

    public List<String> getCollaboratorEmails() {
        return collaboratorEmails;
    }

    public void setCollaboratorEmails(List<String> collaboratorEmails) {
        this.collaboratorEmails = collaboratorEmails;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public String getInstituteName() {
        return instituteName;
    }

    public void setInstituteName(String instituteName) {
        this.instituteName = instituteName;
    }

    public String getInstituteName2() {
        return instituteName2;
    }

    public void setInstituteName2(String instituteName2) {
        this.instituteName2 = instituteName2;
    }

    public String getLogoLink() {
        return logoLink;
    }

    public void setLogoLink(String logoLink) {
        this.logoLink = logoLink;
    }

    public String getLogoSize() {
        return logoSize;
    }

    public void setLogoSize(String logoSize) {
        this.logoSize = logoSize;
    }

    public String getLogoAlignment() {
        return logoAlignment;
    }

    public void setLogoAlignment(String logoAlignment) {
        this.logoAlignment = logoAlignment;
    }

    public String getInstituteLogoLink() {
        return instituteLogoLink;
    }

    public void setInstituteLogoLink(String instituteLogoLink) {
        this.instituteLogoLink = instituteLogoLink;
    }

    public String getInstituteLogoSize() {
        return instituteLogoSize;
    }

    public void setInstituteLogoSize(String instituteLogoSize) {
        this.instituteLogoSize = instituteLogoSize;
    }

    public List<Revision> getHistory() {
        return history;
    }

    public void setHistory(List<Revision> history) {
        this.history = history;
    }

    public Diagram getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(Diagram coverImage) {
        this.coverImage = coverImage;
    }

    public String getAcknowledgement() {
        return acknowledgement;
    }

    public void setAcknowledgement(String acknowledgement) {
        this.acknowledgement = acknowledgement;
    }

    public boolean isIndex() {
        return index;
    }

    public void setIndex(boolean index) {
        this.index = index;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getFooter() {
        return footer;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }

    public int getTocSectionDepth() {
        return tocSectionDepth;
    }

    public void setTocSectionDepth(int tocSectionDepth) {
        this.tocSectionDepth = tocSectionDepth;
    }

    public int getChunkSectionDepth() {
        return chunkSectionDepth;
    }

    public void setChunkSectionDepth(int chunkSectionDepth) {
        this.chunkSectionDepth = chunkSectionDepth;
    }

    public boolean isUseDefaultStyleSheet() {
        return useDefaultStyleSheet;
    }

    public void setUseDefaultStyleSheet(boolean useDefaultStyleSheet) {
        this.useDefaultStyleSheet = useDefaultStyleSheet;
    }

    public boolean isGenNewImages() {
        return genNewImages;
    }

    public void setGenNewImages(boolean genNewImages) {
        this.genNewImages = genNewImages;
    }

}
