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

import gov.nasa.jpl.mbee.DocGenUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.nomagic.task.ProgressStatus;

/**
 * visitor that serializes to docbook xml
 * 
 * @author dlam
 * 
 */
public class DBSerializeVisitor extends DBAbstractVisitor {

    private File           dir;
    private boolean        genImage;
    private StringBuilder  out;
    private Set<String>    ids;
    private ProgressStatus ps;

    public DBSerializeVisitor(boolean genNewImages, File dir, ProgressStatus ps) {
        genImage = genNewImages;
        this.dir = dir;
        out = new StringBuilder();
        ids = new HashSet<String>();
        this.ps = ps;
    }

    public DBSerializeVisitor(boolean genNewImages, File dir, Set<String> ids, ProgressStatus ps) {
        genImage = genNewImages;
        this.dir = dir;
        out = new StringBuilder();
        this.ids = ids;
        this.ps = ps;
    }

    public String getOut() {
        return out.toString();
    }

    @Override
    public void visit(DBBook book) {
        out.append("<book xmlns=\"http://docbook.org/ns/docbook\" xmlns:xl=\"http://www.w3.org/1999/xlink\" version=\"5.0\">\n");
        String title = null;
        String delims = "[,]";
        String revdelims = "[|]";
        out.append("<info>");
        if (book.getUseDefaultStylesheet() == true) {
            if (book.getSubtitle() == null || book.getSubtitle().equals(""))
                title = DocGenUtils.fixString(book.getTitle());
            else
                title = DocGenUtils.fixString(book.getTitle() + ": " + book.getSubtitle());
            out.append("<title>" + title + "</title><subtitle>Generated On: " + new Date().toString()
                    + "</subtitle>");
        } else {

            if (book.getDocumentID() != null && !book.getDocumentID().equals("")) {
                out.append("\n<productnumber>" + book.getDocumentID() + "</productnumber>");
            }
            if (book.getDocumentVersion() != null && !book.getDocumentVersion().equals("")) {
                out.append("\n<releaseinfo>" + book.getDocumentVersion() + "</releaseinfo>");
            }
            if (book.getLogoLocation() != null && !book.getLogoLocation().equals("") && book.getLogoAlignment() != null && !book.getLogoAlignment().equals("")) {
                if (book.getLogoSize() != null && !book.getLogoSize().equals("")) {

                    if (book.getLogoAlignment().equals("center") || book.getLogoAlignment().equals("Center")) {
                        out.append("\n<mediaobject><imageobject><imagedata align= \"center\" fileref=\""
                                + book.getLogoLocation() + "\" depth=\"" + book.getLogoSize()
                                + "\"/></imageobject></mediaobject>");
                    } else if (book.getLogoAlignment().equals("left")
                            || book.getLogoAlignment().equals("Left")) {
                        out.append("\n<mediaobject><imageobject><imagedata align= \"left\" fileref=\""
                                + book.getLogoLocation() + "\" depth=\"" + book.getLogoSize()
                                + "\"/></imageobject></mediaobject>");
                    } else if (book.getLogoAlignment().equals("right")
                            || book.getLogoAlignment().equals("Right")) {
                        out.append("\n<mediaobject><imageobject><imagedata align= \"right\" fileref=\""
                                + book.getLogoLocation() + "\" depth=\"" + book.getLogoSize()
                                + "\"/></imageobject></mediaobject>");
                    }
                } else {
                    if (book.getLogoAlignment().equals("center") || book.getLogoAlignment().equals("Center")) {
                        out.append("\n<mediaobject><imageobject><imagedata align= \"center\" fileref=\""
                                + book.getLogoLocation() + "\"/></imageobject></mediaobject>");
                    } else if (book.getLogoAlignment().equals("left")
                            || book.getLogoAlignment().equals("Left")) {
                        out.append("\n<mediaobject><imageobject><imagedata align= \"left\" fileref=\""
                                + book.getLogoLocation() + "\"/></imageobject></mediaobject>");
                    } else if (book.getLogoAlignment().equals("right")
                            || book.getLogoAlignment().equals("Right")) {
                        out.append("\n<mediaobject><imageobject><imagedata align= \"right\" fileref=\""
                                + book.getLogoLocation() + "\"/></imageobject></mediaobject>");
                    }
                }
            }
            if (book.getAbbreviatedProjectName() != null && !book.getAbbreviatedProjectName().equals("")) {
                if (book.getDocushareLink() != null && !book.getDocushareLink().equals(""))
                    out.append("\n<publisher><publishername>" + book.getAbbreviatedProjectName()
                            + "</publishername><address>" + book.getDocushareLink()
                            + "</address></publisher>");
                else
                    out.append("\n<publisher><publishername>" + book.getAbbreviatedProjectName()
                            + "</publishername></publisher>");
            }
            out.append("\n<pubdate>" + new Date().toString() + "</pubdate>");
            if (book.getJPLProjectTitle() == null || book.getJPLProjectTitle().equals("")) {
                out.append("\n<title>" + DocGenUtils.fixString(book.getTitle()) + "</title>");
            } else {
                out.append("\n<title>" + book.getJPLProjectTitle() + "</title><subtitle>"
                    + DocGenUtils.fixString(book.getTitle()) + "</subtitle>");
            }
            if (book.getAbbreviatedTitle() != null && !book.getAbbreviatedTitle().equals("")) {
                out.append("\n<titleabbrev>" + book.getAbbreviatedTitle() + "</titleabbrev>");
            }
            out.append("\n<legalnotice><title>" + book.getTitlePageLegalNotice() + "</title><para>"
                    + book.getFooterLegalNotice() + "</para></legalnotice>");

            if (book.getInstTxt1() != null) {
                out.append("\n<collab><org>\n<orgname>" + book.getInstTxt1() + "</orgname>");

            } else {
                out.append("\n<collab><org>\n<orgname>Jet Propulsion Laboratory</orgname>");
            }

            if (book.getInstTxt2() != null) {
                out.append("\n<orgdiv>" + book.getInstTxt2() + "</orgdiv>");

            } else {
                out.append("\n<orgdiv>California Institute of Technology</orgdiv>");
            }

            if (book.getInstLogo() != null) {
                out.append("\n<uri>" + book.getInstLogo() + "</uri>");

            } else {
                out.append("\n<uri>http://sec274.jpl.nasa.gov/img/logos/jpl_logo(220x67).gif</uri>");
            }
            if (book.getInstLogoSize() != null) {
                out.append("\n<address><alt>" + book.getInstLogoSize() + "</alt></address>\n</org></collab>");

            } else {
                out.append("\n<address><alt>36px</alt></address>\n</org></collab>");
            }

            for (int index = 0; index < book.getAuthor().size(); index++) {
                if (book.getAuthor().get(index) != null && !book.getAuthor().get(index).equals("")) {
                    String[] tokens = book.getAuthor().get(index).split(delims);
                    out.append("\n<author><personname><firstname>" + tokens[0] + "</firstname><surname>"
                            + tokens[1] + "</surname></personname><affiliation>" + "<jobtitle>" + tokens[2]
                            + "</jobtitle><org><orgname>" + tokens[3] + "</orgname><orgdiv>" + tokens[4]
                            + "</orgdiv></org></affiliation></author>");
                }
            }
            for (int index = 0; index < book.getApprover().size(); index++) {
                if (book.getApprover().get(index) != null && !book.getApprover().get(index).equals("")) {
                    String[] tokens = book.getApprover().get(index).split(delims);
                    out.append("\n<editor><personname><firstname>" + tokens[0] + "</firstname><surname>"
                            + tokens[1] + "</surname></personname><affiliation>" + "<jobtitle>" + tokens[2]
                            + "</jobtitle><org><orgname>" + tokens[3] + "</orgname><orgdiv>" + tokens[4]
                            + "</orgdiv></org></affiliation></editor>");
                }
            }

            for (int index = 0; index < book.getConcurrance().size(); index++) {
                if (book.getConcurrance().get(index) != null && !book.getConcurrance().get(index).equals("")) {
                    String[] tokens = book.getConcurrance().get(index).split(delims);
                    out.append("\n<othercredit><personname><firstname>" + tokens[0] + "</firstname><surname>"
                            + tokens[1] + "</surname></personname><affiliation>" + "<jobtitle>" + tokens[2]
                            + "</jobtitle><org><orgname>" + tokens[3] + "</orgname><orgdiv>" + tokens[4]
                            + "</orgdiv></org></affiliation></othercredit>");
                }
            }
            for (int index = 0; index < book.getRevisionHistory().size(); index++) {
                if (book.getRevisionHistory().get(index) != null
                        && !book.getRevisionHistory().get(index).equals("")) {
                    String[] tokens = book.getRevisionHistory().get(index).split(revdelims);
                    out.append("\n<revhistory><revision><revnumber>" + tokens[0] + "</revnumber><date>"
                            + tokens[1] + "</date><author><personname><firstname>" + tokens[2]
                            + "</firstname><surname>" + tokens[3]
                            + "</surname></personname></author><revremark>" + tokens[4]
                            + "</revremark></revision></revhistory>");
                }
            }
            for (int index = 0; index < book.getCollaboratorEmail().size(); index++) {
                if (book.getCollaboratorEmail().get(index) != null
                        && !book.getCollaboratorEmail().get(index).equals("")) {
                    String[] tokens = book.getCollaboratorEmail().get(index).split(delims);
                    out.append("\n<address><email>" + tokens[0] + "</email></address>");
                }
            }
        }
        // out.append("<productnumber>" + book.getDocumentID() +
        // "</productnumber>");
        /*
         * if (book.getLegalnotice() != null &&
         * !book.getLegalnotice().equals("")) out.append("<legalnotice>" +
         * DocGenUtils.addDocbook(DocGenUtils.fixString(book.getLegalnotice()))
         * + "</legalnotice>");
         */
        // do authors
        if (book.getCoverimage() != null) {
            File imageDir = new File(dir, "images");
            imageDir.mkdirs();
            List<String> s = null;
            boolean ok = true;
            try {
                s = DocGenUtils.exportDiagram(book.getCoverimage(), imageDir, false);
            } catch (IOException e) {
                e.printStackTrace();
                ok = false;
            }
            if (ok) {
                out.append("<cover>");
                out.append("<mediaobject><imageobject role=\"fo\">\n");
                String filename = s.get(0);
                String scale = s.get(1);
                if (scale.equals("true"))
                    out.append("<imagedata fileref=\"" + filename
                            + "\" format=\"SVG\" scalefit=\"1\" width=\"100%\"/>\n");
                else
                    out.append("<imagedata fileref=\"" + filename + "\" format=\"SVG\"/>\n");
                out.append("</imageobject><imageobject role=\"html\"><imagedata fileref=\""
                        + filename.replaceAll(".svg", ".png") + "\"/></imageobject>\n");
                out.append("</mediaobject>\n");
                out.append("</cover>");
            }
        }
        out.append("</info>\n");
        if (book.getAcknowledgement() != null && !book.getAcknowledgement().equals(""))
            out.append("<acknowledgement>"
                    + DocGenUtils.addDocbook(DocGenUtils.fixString(book.getAcknowledgement()))
                    + "</acknowledgement>\n");
        for (DocumentElement e: book.getChildren())
            e.accept(this);
        if (book.getIndex())
            out.append("<index/>");
        out.append("</book>");
    }

    @Override
    public void visit(DBColSpec colspec) {
        out.append("<colspec ");
        out.append("colname=\"" + colspec.getColname() + "\" ");
        out.append("colnum=\"" + colspec.getColnum() + "\"");
        if (colspec.getColwidth() != null && !colspec.getColwidth().equals(""))
            out.append(" colwidth=\"" + colspec.getColwidth() + "\"/>\n");
        else
            out.append("/>\n");
    }

    @Override
    public void visit(DBImage image) {
        if (ps != null && ps.isCancel())
            return;

        List<String> s = null;
        File imageDir = new File(dir, "images");
        imageDir.mkdirs();
        try {
            s = DocGenUtils.exportDiagram(image.getImage(), imageDir, genImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (image.isDoNotShow())
            return;
        String id = "";
        if (image.getId() != null && !ids.contains(image.getId())) {
            id = " xml:id=\"" + image.getId() + "\"";
            ids.add(image.getId());
        }
        out.append("<figure" + id + " pgwide=\"1\">");
        out.append("<title>" + DocGenUtils.fixString(image.getTitle()) + "</title>\n");
        out.append("<mediaobject><imageobject role=\"fo\">\n");
        String filename = s.get(0);
        String scale = s.get(1);
        if (scale.equals("true"))
            out.append("<imagedata fileref=\"" + filename
                    + "\" format=\"SVG\" scalefit=\"1\" width=\"100%\"/>\n");
        else
            out.append("<imagedata fileref=\"" + filename + "\" format=\"SVG\"/>\n");
        out.append("</imageobject><imageobject role=\"html\"><imagedata fileref=\""
                + filename.replaceAll(".svg", ".png") + "\"/></imageobject>\n");
        if (image.getCaption() != null && !image.getCaption().equals(""))
            out.append("<caption>" + DocGenUtils.addDocbook(DocGenUtils.fixString(image.getCaption()))
                    + "</caption>\n");
        out.append("</mediaobject></figure>\n");

    }

    @Override
    public void visit(DBList list) {
        if (list.getChildren().isEmpty())
            return;
        if (list.isOrdered())
            out.append("<orderedlist spacing=\"compact\">\n");
        else
            out.append("<itemizedlist spacing=\"compact\">\n");
        for (DocumentElement e: list.getChildren()) {
            if (!(e instanceof DBListItem)) {
                out.append("<listitem>\n");
                e.accept(this);
                out.append("</listitem>\n");
            } else {
                e.accept(this);
            }
        }
        if (list.isOrdered())
            out.append("</orderedlist>\n");
        else
            out.append("</itemizedlist>\n");

    }

    @Override
    public void visit(DBListItem listitem) {
        out.append("<listitem>\n");
        for (DocumentElement de: listitem.getChildren()) {
            de.accept(this);
        }
        out.append("</listitem>\n");

    }

    @Override
    public void visit(DBParagraph para) {
        if (para.getText() == null)
            out.append("<para></para>\n");
        else {
            if (para.getText() instanceof Collection) {
                for (Object p: (Collection)para.getText())
                    out.append(DocGenUtils.addDocbook(DocGenUtils.fixString(p)));
            } else
                out.append(DocGenUtils.addDocbook(DocGenUtils.fixString(para.getText())) + "\n");
        }

    }

    @Override
    public void visit(DBText text) {
        if (text.getText() != null)
            out.append(DocGenUtils.fixString(text.getText()));
    }

    @Override
    public void visit(DBSection section) {
        DBSerializeVisitor inside = new DBSerializeVisitor(genImage, dir, ids, ps);
        for (DocumentElement de: section.getChildren()) {
            de.accept(inside);
        }
        String inString = inside.getOut();
        if (inString.equals("")) {
            if (section.isSkipIfEmpty())
                return;
            inString = "<para>" + section.getStringIfEmpty() + "</para>\n";
        }
        if (!section.isNoSection()) {
            String id = "";
            if (section.getId() != null && !ids.contains(section.getId())) {
                id = " xml:id=\"" + section.getId() + "\"";
                ids.add(section.getId());
            }
            if (section.isAppendix()) {
                out.append("<appendix" + id + ">\n");
            } else if (section.isChapter()) {
                out.append("<chapter" + id + ">\n");
            } else {
                out.append("<section" + id + ">\n");
            }
            out.append("<info><title>" + DocGenUtils.fixString(section.getTitle()) + "</title></info>\n");
            out.append(inString);
            if (section.isAppendix())
                out.append("</appendix>\n");
            else if (section.isChapter())
                out.append("</chapter>\n");
            else
                out.append("</section>\n");
        } else {
            out.append(inString);
        }
    }

    @Override
    public void visit(DBSimpleList simplelist) {
        if (simplelist.getContent().isEmpty())
            return;
        out.append("<simplelist>\n");
        for (Object s: simplelist.getContent()) {
            out.append("<member>" + DocGenUtils.fixString(s) + "</member>\n");
        }
        out.append("</simplelist>\n");
    }

    @Override
    public void visit(DBTable table) {
        if (table.isTranspose())
            table.transpose();
        int cols = table.getCols();
        if (table.getBody() == null || table.getBody().isEmpty())
            return;
        if (cols == 0) {
            for (List<DocumentElement> row: table.getBody()) {
                if (row.size() > cols)
                    cols = row.size();
            }
        }
        String id = "";
        if (table.getId() != null && !ids.contains(table.getId())) {
            id = " xml:id=\"" + table.getId() + "\"";
            ids.add(table.getId());
        }
        String style = "";
        if (table.getStyle() != null && !table.getStyle().equals(""))
            style = " tabstyle=\"" + table.getStyle() + "\"";
        out.append("<table frame=\"all\" pgwide=\"1\" role=\"longtable\"" + id + style + ">\n");
        // out.append("<informaltable frame=\"all\" pgwide=\"1\" role=\"longtable\""
        // + id + style + ">\n");
        out.append("<title>" + DocGenUtils.fixString(table.getTitle()) + "</title>\n"); // don't
                                                                                        // have
                                                                                        // this
                                                                                        // for
                                                                                        // informaltable
        out.append("<tgroup cols=\"" + cols + "\" align=\"left\" colsep=\"1\" rowsep=\"1\">\n");
        if (table.getColspecs() != null)
            for (DBColSpec colspec: table.getColspecs())
                colspec.accept(this);
        if (table.getHeaders() != null) {
            out.append("<thead>\n");
            getTableRows(table.getHeaders());
            out.append("</thead>\n");
        }
        out.append("<tbody>\n");
        getTableRows(table.getBody());
        out.append("</tbody>\n");
        out.append("</tgroup>\n");
        if (table.getCaption() != null && !table.getCaption().equals(""))
            out.append("<caption>" + DocGenUtils.addDocbook(DocGenUtils.fixString(table.getCaption()))
                    + "</caption>\n");
        out.append("</table>\n");

    }

    private void getTableRows(List<List<DocumentElement>> grid) {
        for (List<DocumentElement> row: grid) {
            out.append("<row>");
            for (DocumentElement cell: row) {
                if (cell instanceof DBTableEntry)
                    cell.accept(this);
                else if (cell == null)
                    continue;
                else {
                    out.append("<entry>");
                    cell.accept(this);
                    out.append("</entry>");
                }
            }
            out.append("</row>\n");
        }
    }

    @Override
    public void visit(DBTableEntry tableentry) {
        out.append("<entry");
        if (tableentry.getMorerows() > 0)
            out.append(" morerows=\"" + tableentry.getMorerows() + "\"");
        if (tableentry.getNamest() != null && !tableentry.getNamest().equals(""))
            out.append(" namest=\"" + tableentry.getNamest() + "\"");
        if (tableentry.getNameend() != null && !tableentry.getNameend().equals(""))
            out.append(" nameend=\"" + tableentry.getNameend() + "\"");
        out.append(">");
        for (DocumentElement de: tableentry.getChildren())
            de.accept(this);
        out.append("</entry>");

    }
}
