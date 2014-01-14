/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 * 
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.mbee.model;

import java.util.List;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;

public class Document extends Container {

    private String       acknowledgement;
    private String       legalnotice;
    private String       subtitle;
    private Diagram      coverimage;
    private boolean      index;
    private String       header;
    private String       footer;
    private String       subheader;
    private String       subfooter;
    private String       DocumentID;
    private String       DocumentVersion;
    private String       LogoAlignment;
    private String       LogoLocation;
    private String       AbbreviatedProjectName;
    private String       DocushareLink;
    private String       AbbreviatedTitle;
    private String       TitlePageLegalNotice;
    private String       FooterLegalNotice;
    private String       RemoveBlankPages;
    private List<String> Author;
    private List<String> Approver;
    private List<String> Concurrance;
    private List<String> CollaboratorEmail;
    private List<String> RevisionHistory;
    private boolean      UseDefaultStylesheet;
    private String       JPLProjectTitle;
    private String       LogoSize;
    private String       InstLogo;
    private String       InstLogoSize;
    private String       InstTxt1;
    private String       InstTxt2;

    private boolean      chunkFirstSections;
    private int          chunkSectionDepth;
    private int          tocSectionDepth;

    private boolean      genNewImage;

    private boolean      product;

    public boolean isProduct() {
        return product;
    }

    public void setProduct(boolean product) {
        this.product = product;
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

    public String getSubheader() {
        return subheader;
    }

    public void setSubheader(String subheader) {
        this.subheader = subheader;
    }

    public String getSubfooter() {
        return subfooter;
    }

    public void setSubfooter(String subfooter) {
        this.subfooter = subfooter;
    }

    public boolean getChunkFirstSections() {
        return chunkFirstSections;
    }

    public void setChunkFirstSections(boolean chunkFirstSections) {
        this.chunkFirstSections = chunkFirstSections;
    }

    public int getChunkSectionDepth() {
        return chunkSectionDepth;
    }

    public void setChunkSectionDepth(int chunkSectionDepth) {
        this.chunkSectionDepth = chunkSectionDepth;
    }

    public int getTocSectionDepth() {
        return tocSectionDepth;
    }

    public void setTocSectionDepth(int tocSectionDepth) {
        this.tocSectionDepth = tocSectionDepth;
    }

    public String getAcknowledgement() {
        return acknowledgement;
    }

    public String getLegalnotice() {
        return legalnotice;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getDocumentID() {
        return DocumentID;
    }

    public String getDocumentVersion() {
        return DocumentVersion;
    }

    public String getLogoAlignment() {
        return LogoAlignment;
    }

    public String getLogoLocation() {
        return LogoLocation;
    }

    public String getAbbreviatedProjectName() {
        return AbbreviatedProjectName;
    }

    public String getDocushareLink() {
        return DocushareLink;
    }

    public String getAbbreviatedTitle() {
        return AbbreviatedTitle;
    }

    public String getTitlePageLegalNotice() {
        return TitlePageLegalNotice;
    }

    public String getFooterLegalNotice() {
        return FooterLegalNotice;
    }

    public List<String> getCollaboratorEmail() {
        return CollaboratorEmail;
    }

    public List<String> getRevisionHistory() {
        return RevisionHistory;
    }

    public boolean getUseDefaultStylesheet() {
        return UseDefaultStylesheet;
    }

    public Boolean getRemoveBlankPages() {
        if (RemoveBlankPages == "1")
            return true;
        else
            return false;
    }

    public List<String> getAuthor() {
        return Author;
    }

    public List<String> getApprover() {
        return Approver;
    }

    public List<String> getConcurrance() {
        return Concurrance;
    }

    public String getJPLProjectTitle() {
        return JPLProjectTitle;
    }

    public String getLogoSize() {
        return LogoSize;
    }

    public String getInstLogo() {
        return InstLogo;
    }

    public String getInstLogoSize() {
        return InstLogoSize;
    }

    public String getInstTxt1() {
        return InstTxt1;
    }

    public String getInstTxt2() {
        return InstTxt2;
    }

    public Diagram getCoverimage() {
        return coverimage;
    }

    public boolean isIndex() {
        return index;
    }

    public void setAcknowledgement(String a) {
        acknowledgement = a;
    }

    public void setLegalnotice(String a) {
        legalnotice = a;
    }

    public void setDocumentID(String z) {
        DocumentID = z;
    }

    public void setDocumentVersion(String s) {
        DocumentVersion = s;
    }

    public void setLogoAlignment(String s) {
        LogoAlignment = s;
    }

    public void setLogoLocation(String s) {
        LogoLocation = s;
    }

    public void setAbbreviatedProjectName(String s) {
        AbbreviatedProjectName = s;
    }

    public void setDocushareLink(String s) {
        DocushareLink = s;
    }

    public void setAbbreviatedTitle(String s) {
        AbbreviatedTitle = s;
    }

    public void setTitlePageLegalNotice(String s) {
        TitlePageLegalNotice = s;
    }

    public void setFooterLegalNotice(String s) {
        FooterLegalNotice = s;
    }

    public void setCollaboratorEmail(List<String> s) {
        CollaboratorEmail = s;
    }

    public void setRemoveBlankPages(String s) {
        RemoveBlankPages = s;
    }

    public void setAuthor(List<String> s) {
        Author = s;
    }

    public void setApprover(List<String> s) {
        Approver = s;
    }

    public void setConcurrance(List<String> s) {
        Concurrance = s;
    }

    public void setRevisionHistory(List<String> s) {
        RevisionHistory = s;
    }

    public void setJPLProjectTitle(String s) {
        JPLProjectTitle = s;
    }

    public void setUseDefaultStylesheet(boolean s) {
        UseDefaultStylesheet = s;
    }

    public void setLogoSize(String s) {
        LogoSize = s;
    }

    public void setInstLogo(String s) {
        InstLogo = s;
    }

    public void setInstLogoSize(String s) {
        InstLogoSize = s;
    }

    public void setInstTxt1(String s) {
        InstTxt1 = s;
    }

    public void setInstTxt2(String s) {
        InstTxt2 = s;
    }

    public void setSubtitle(String s) {
        subtitle = s;
    }

    public void setCoverimage(Diagram d) {
        coverimage = d;
    }

    public void setIndex(boolean d) {
        index = d;
    }

    public Document() {
        chunkFirstSections = false;
        chunkSectionDepth = 20;
        tocSectionDepth = 20;
    }

    public boolean getGenNewImage() {
        return genNewImage;
    }

    public void setGenNewImage(boolean n) {
        genNewImage = n;
    }

    @Override
    public void accept(IModelVisitor v) {
        v.visit(this);

    }
}
