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
package gov.nasa.jpl.mgss.mbee.docgen.docbook;

import java.util.List;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;

/**
 * If you find an occasion where you want to use this, let me know.
 * 
 * @author dlam
 * 
 */
public class DBBook extends DBHasContent {

    private String       subtitle;
    private String       acknowledgement;
    private String       legalnotice;
    private Diagram      coverimage;
    private boolean      index;
    private String       DocumentID;
    private String       DocumentVersion;
    private String       LogoAlignment;
    private String       LogoLocation;
    private String       AbbreviatedProjectName;
    private String       DocushareLink;
    private String       AbbreviatedTitle;
    private String       TitlePageLegalNotice;
    private String       FooterLegalNotice;
    private List<String> CollaboratorEmail;
    private Boolean      RemoveBlankPages;
    private List<String> Author;
    private List<String> Approver;
    private List<String> Concurrance;
    private List<String> RevisionHistory;
    private String       JPLProjectTitle;
    private boolean      UseDefaultStylesheet;
    private String       LogoSize;
    private String       InstLogo;
    private String       InstLogoSize;
    private String       InstTxt1;
    private String       InstTxt2;

    public DBBook() {
        subtitle = "";
    }

    public boolean getIndex() {
        return index;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getAcknowledgement() {
        return acknowledgement;
    }

    public String getLegalnotice() {
        return legalnotice;
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

    public String getLogoSize() {
        return LogoSize;
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

    public Boolean getRemoveBlankPages() {
        return RemoveBlankPages;
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

    public List<String> getRevisionHistory() {
        return RevisionHistory;
    }

    public String getJPLProjectTitle() {
        return JPLProjectTitle;
    }

    public boolean getUseDefaultStylesheet() {
        return UseDefaultStylesheet;
    }

    public Diagram getCoverimage() {
        return coverimage;
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

    public void setIndex(boolean index) {
        this.index = index;
    }

    public void setSubtitle(String s) {
        subtitle = s;
    }

    public void setAcknowledgement(String ack) {
        acknowledgement = ack;
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

    public void setDocumentID(String s) {
        DocumentID = s;
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

    public void setRevisionHistory(List<String> s) {
        RevisionHistory = s;
    }

    public void setRemoveBlankPages(Boolean s) {
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

    public void setJPLProjectTitle(String s) {
        JPLProjectTitle = s;
    }

    public void setUseDefaulStylesheet(boolean s) {
        UseDefaultStylesheet = s;
    }

    public void setCoverimage(Diagram d) {
        coverimage = d;
    }

    public void setLegalnotice(String l) {
        legalnotice = l;
    }

    @Override
    public void accept(IDBVisitor v) {
        v.visit(this);
    }
}
