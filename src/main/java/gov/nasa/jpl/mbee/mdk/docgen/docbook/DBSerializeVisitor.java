package gov.nasa.jpl.mbee.mdk.docgen.docbook;

import com.nomagic.task.ProgressStatus;
import gov.nasa.jpl.mbee.mdk.docgen.DocGenUtils;
import gov.nasa.jpl.mbee.mdk.model.docmeta.DocumentMeta;
import gov.nasa.jpl.mbee.mdk.model.docmeta.Person;
import gov.nasa.jpl.mbee.mdk.model.docmeta.Revision;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * visitor that serializes to docbook xml
 *
 * @author dlam
 */
public class DBSerializeVisitor extends DBAbstractVisitor {

    private File dir;
    private boolean genImage;
    private StringBuilder out;
    private Set<String> ids;
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
        DocumentMeta meta = book.getMetadata();
        out.append("<book xmlns=\"http://docbook.org/ns/docbook\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"5.0\">\n");
        String title = null;
        out.append("<info>");
        if (book.getUseDefaultStylesheet() == true) {
            if (book.getSubtitle() == null || book.getSubtitle().isEmpty()) {
                title = DocGenUtils.fixString(book.getTitle());
            }
            else {
                title = DocGenUtils.fixString(book.getTitle() + ": " + book.getSubtitle());
            }
            out.append("<title>" + title + "</title><subtitle>Generated On: " + new Date().toString()
                    + "</subtitle>");
        }
        else {

            if (meta.getDocumentId() != null && !meta.getDocumentId().isEmpty()) {
                out.append("\n<productnumber>" + meta.getDocumentId() + "</productnumber>");
            }
            if (meta.getVersion() != null && !meta.getVersion().isEmpty()) {
                out.append("\n<releaseinfo>" + meta.getVersion() + "</releaseinfo>");
            }
            if (meta.getLogoLink() != null && !meta.getLogoLink().isEmpty() && meta.getLogoAlignment() != null && !meta.getLogoAlignment().isEmpty()) {
                String depth = "";
                if (meta.getLogoSize() != null && !meta.getLogoSize().isEmpty()) {
                    depth = "depth=\"" + meta.getLogoSize() + "\"";
                }
                String align = "";
                if (meta.getLogoAlignment().equals("center") || meta.getLogoAlignment().equals("Center")) {
                    align = "center";
                }
                else if (meta.getLogoAlignment().equals("left") || meta.getLogoAlignment().equals("Left")) {
                    align = "left";
                }
                else if (meta.getLogoAlignment().equals("right") || meta.getLogoAlignment().equals("Right")) {
                    align = "right";
                }
                out.append("\n<mediaobject><imageobject><imagedata align=\"" + align + "\" fileref=\""
                        + meta.getLogoLink() + "\" " + depth + "/></imageobject></mediaobject>");

            }
            if (meta.getProjectAcronym() != null && !meta.getProjectAcronym().isEmpty()) {
                if (meta.getLink() != null && !meta.getLink().isEmpty()) {
                    out.append("\n<publisher><publishername>" + meta.getProjectAcronym()
                            + "</publishername><address>" + meta.getLink()
                            + "</address></publisher>");
                }
                else {
                    out.append("\n<publisher><publishername>" + meta.getProjectAcronym()
                            + "</publishername></publisher>");
                }
            }
            out.append("\n<pubdate>" + new Date().toString() + "</pubdate>");
            if (meta.getProjectTitle() == null || meta.getProjectTitle().isEmpty()) {
                out.append("\n<title>" + DocGenUtils.fixString(book.getTitle()) + "</title>");
            }
            else {
                out.append("\n<title>" + meta.getProjectTitle() + "</title><subtitle>"
                        + DocGenUtils.fixString(book.getTitle()) + "</subtitle>");
            }
            if (meta.getDocumentAcronym() != null && !meta.getDocumentAcronym().isEmpty()) {
                out.append("\n<titleabbrev>" + meta.getDocumentAcronym() + "</titleabbrev>");
            }
            out.append("\n<legalnotice><title>" + meta.getTitlePageLegalNotice() + "</title><para>"
                    + meta.getFooterLegalNotice() + "</para></legalnotice>");

            if (meta.getInstituteName() != null) {
                out.append("\n<collab><org>\n<orgname>" + meta.getInstituteName() + "</orgname>");

            }
            else {
                out.append("\n<collab><org>\n<orgname>Jet Propulsion Laboratory</orgname>");
            }

            if (meta.getInstituteName2() != null) {
                out.append("\n<orgdiv>" + meta.getInstituteName2() + "</orgdiv>");

            }
            else {
                out.append("\n<orgdiv>California Institute of Technology</orgdiv>");
            }

            if (meta.getInstituteLogoLink() != null) {
                out.append("\n<uri>" + meta.getInstituteLogoLink() + "</uri>");

            }
            else {
                out.append("\n<uri>http://sec274.jpl.nasa.gov/img/logos/jpl_logo(220x67).gif</uri>");
            }
            if (meta.getInstituteLogoSize() != null) {
                out.append("\n<address><alt>" + meta.getInstituteLogoSize() + "</alt></address>\n</org></collab>");

            }
            else {
                out.append("\n<address><alt>36px</alt></address>\n</org></collab>");
            }

            for (Person p : meta.getAuthors()) {
                out.append("\n<author><personname><firstname>" + p.getFirstname() + "</firstname><surname>"
                        + p.getLastname() + "</surname></personname><affiliation>" + "<jobtitle>" + p.getTitle()
                        + "</jobtitle><org><orgname>" + p.getOrgname() + "</orgname><orgdiv>" + p.getOrgdiv()
                        + "</orgdiv></org></affiliation></author>");

            }
            for (Person p : meta.getApprovers()) {
                out.append("\n<editor><personname><firstname>" + p.getFirstname() + "</firstname><surname>"
                        + p.getLastname() + "</surname></personname><affiliation>" + "<jobtitle>" + p.getTitle()
                        + "</jobtitle><org><orgname>" + p.getOrgname() + "</orgname><orgdiv>" + p.getOrgdiv()
                        + "</orgdiv></org></affiliation></editor>");

            }
            for (Person p : meta.getConcurrances()) {
                out.append("\n<othercredit><personname><firstname>" + p.getFirstname() + "</firstname><surname>"
                        + p.getLastname() + "</surname></personname><affiliation>" + "<jobtitle>" + p.getTitle()
                        + "</jobtitle><org><orgname>" + p.getOrgname() + "</orgname><orgdiv>" + p.getOrgdiv()
                        + "</orgdiv></org></affiliation></othercredit>");

            }
            for (Revision rev : meta.getHistory()) {
                out.append("\n<revhistory><revision><revnumber>" + rev.getRevNumber() + "</revnumber><date>"
                        + rev.getDate() + "</date><author><personname><firstname>" + rev.getFirstName()
                        + "</firstname><surname>" + rev.getLastName()
                        + "</surname></personname></author><revremark>" + rev.getRemark()
                        + "</revremark></revision></revhistory>");

            }
            for (String email : meta.getCollaboratorEmails()) {
                out.append("\n<address><email>" + email + "</email></address>");
            }
        }
        // out.append("<productnumber>" + book.getDocumentID() +
        // "</productnumber>");
        /*
         * if (book.getLegalnotice() != null &&
         * !book.getLegalnotice().isEmpty()) out.append("<legalnotice>" +
         * DocGenUtils.addDocbook(DocGenUtils.fixString(book.getLegalnotice()))
         * + "</legalnotice>");
         */
        // do authors
        if (meta.getCoverImage() != null) {
            File imageDir = new File(dir, "images");
            imageDir.mkdirs();
            List<String> s = null;
            boolean ok = true;
            try {
                s = DocGenUtils.exportDiagram(meta.getCoverImage(), imageDir, false);
            } catch (IOException e) {
                e.printStackTrace();
                ok = false;
            }
            if (ok) {
                out.append("<cover>");
                out.append("<mediaobject><imageobject role=\"fo\">\n");
                String filename = s.get(0);
                String scale = s.get(1);
                if (scale.equals("true")) {
                    out.append("<imagedata fileref=\"" + filename
                            + "\" format=\"SVG\" scalefit=\"1\" width=\"100%\"/>\n");
                }
                else {
                    out.append("<imagedata fileref=\"" + filename + "\" format=\"SVG\"/>\n");
                }
                out.append("</imageobject><imageobject role=\"html\"><imagedata fileref=\""
                        + filename.replaceAll(".svg", ".png") + "\"/></imageobject>\n");
                out.append("</mediaobject>\n");
                out.append("</cover>");
            }
        }
        out.append("</info>\n");
        if (meta.getAcknowledgement() != null && !meta.getAcknowledgement().isEmpty()) {
            out.append("<acknowledgement>"
                    + DocGenUtils.addDocbook(DocGenUtils.fixString(meta.getAcknowledgement()))
                    + "</acknowledgement>\n");
        }
        for (DocumentElement e : book.getChildren()) {
            if (e instanceof DBSection && ((DBSection) e).isView()) {
                e.accept(this);
            }
        }
        if (meta.isIndex()) {
            out.append("<index/>");
        }
        out.append("</book>");
    }

    @Override
    public void visit(DBColSpec colspec) {
        out.append("<colspec ");
        out.append("colname=\"" + colspec.getColname() + "\" ");
        out.append("colnum=\"" + colspec.getColnum() + "\"");
        if (colspec.getColwidth() != null && !colspec.getColwidth().isEmpty()) {
            out.append(" colwidth=\"" + colspec.getColwidth() + "\"/>\n");
        }
        else {
            out.append("/>\n");
        }
    }

    @Override
    public void visit(DBImage image) {
        if (ps != null && ps.isCancel()) {
            return;
        }

        List<String> s = null;
        File imageDir = new File(dir, "images");
        imageDir.mkdirs();
        try {
            s = DocGenUtils.exportDiagram(image.getImage(), imageDir, genImage);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (image.isDoNotShow()) {
            return;
        }
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
        if (scale.equals("true")) {
            out.append("<imagedata fileref=\"" + filename
                    + "\" format=\"SVG\" scalefit=\"1\" width=\"100%\"/>\n");
        }
        else {
            out.append("<imagedata fileref=\"" + filename + "\" format=\"SVG\"/>\n");
        }
        out.append("</imageobject><imageobject role=\"html\"><imagedata fileref=\""
                + filename.replaceAll(".svg", ".png") + "\"/></imageobject>\n");
        if (image.getCaption() != null && !image.getCaption().isEmpty()) {
            out.append("<caption>" + DocGenUtils.addDocbook(DocGenUtils.fixString(image.getCaption()))
                    + "</caption>\n");
        }
        out.append("</mediaobject></figure>\n");

    }

    @Override
    public void visit(DBList list) {
        if (list.getChildren().isEmpty()) {
            return;
        }
        if (list.isOrdered()) {
            out.append("<orderedlist spacing=\"compact\">\n");
        }
        else {
            out.append("<itemizedlist spacing=\"compact\">\n");
        }
        for (DocumentElement e : list.getChildren()) {
            if (!(e instanceof DBListItem)) {
                out.append("<listitem>\n");
                e.accept(this);
                out.append("</listitem>\n");
            }
            else {
                e.accept(this);
            }
        }
        if (list.isOrdered()) {
            out.append("</orderedlist>\n");
        }
        else {
            out.append("</itemizedlist>\n");
        }

    }

    @Override
    public void visit(DBListItem listitem) {
        out.append("<listitem>\n");
        for (DocumentElement de : listitem.getChildren()) {
            de.accept(this);
        }
        out.append("</listitem>\n");

    }

    @Override
    public void visit(DBParagraph para) {
        if (para.getText() == null) {
            out.append("<para></para>\n");
        }
        else {
            if (para.getText() instanceof Collection) {
                for (Object p : (Collection) para.getText()) {
                    out.append(DocGenUtils.addDocbook(DocGenUtils.fixString(p)));
                }
            }
            else {
                out.append(DocGenUtils.addDocbook(DocGenUtils.fixString(para.getText())) + "\n");
            }
        }

    }

    @Override
    public void visit(DBText text) {
        if (text.getText() != null) {
            out.append(DocGenUtils.fixString(text.getText()));
        }
    }

    @Override
    public void visit(DBSection section) {
        DBSerializeVisitor inside = new DBSerializeVisitor(genImage, dir, ids, ps);
        for (DocumentElement de : section.getChildren()) {
            de.accept(inside);
        }
        String inString = inside.getOut();
        if (inString.isEmpty()) {
            if (section.isSkipIfEmpty()) {
                return;
            }
            inString = "<para>" + section.getStringIfEmpty() + "</para>\n";
        }
        String id = "";
        if (section.getId() != null && !ids.contains(section.getId())) {
            id = " xml:id=\"" + section.getId() + "\"";
            ids.add(section.getId());
        }
        if (section.isAppendix()) {
            out.append("<appendix" + id +">\n");
        }
        else if (section.isChapter()) {
            out.append("<chapter" + id + ">\n");
        }
        else {
            out.append("<section" + id + ">\n");
        }
        out.append("<info><title>" + DocGenUtils.fixString(section.getTitle()) + "</title></info>\n");
        out.append(inString);
        if (section.isAppendix()) {
            out.append("</appendix>\n");
        }
        else if (section.isChapter()) {
            out.append("</chapter>\n");
        }
        else {
            out.append("</section>\n");
        }
    }

    @Override
    public void visit(DBSimpleList simplelist) {
        if (simplelist.getContent().isEmpty()) {
            return;
        }
        out.append("<simplelist>\n");
        for (Object s : simplelist.getContent()) {
            out.append("<member>" + DocGenUtils.fixString(s) + "</member>\n");
        }
        out.append("</simplelist>\n");
    }

    @Override
    public void visit(DBTable table) {
        if (table.isTranspose()) {
            table.transpose();
        }
        int cols = table.getCols();
        if (table.getBody() == null || table.getBody().isEmpty()) {
            return;
        }
        if (cols == 0) {
            for (List<DocumentElement> row : table.getBody()) {
                if (row.size() > cols) {
                    cols = row.size();
                }
            }
        }
        String id = "";
        if (table.getId() != null && !ids.contains(table.getId())) {
            id = " xml:id=\"" + table.getId() + "\"";
            ids.add(table.getId());
        }
        String style = "";
        if (table.getStyle() != null && !table.getStyle().isEmpty()) {
            style = " tabstyle=\"" + table.getStyle() + "\"";
        }
        out.append("<table frame=\"all\" pgwide=\"1\" role=\"longtable\"" + id + style + ">\n");
        // out.append("<informaltable frame=\"all\" pgwide=\"1\" role=\"longtable\""
        // + id + style + ">\n");
        out.append("<title>" + DocGenUtils.fixString(table.getTitle()) + "</title>\n"); // don't
        // have
        // this
        // for
        // informaltable
        out.append("<tgroup cols=\"" + cols + "\" align=\"left\" colsep=\"1\" rowsep=\"1\">\n");
        if (table.getColspecs() != null) {
            for (DBColSpec colspec : table.getColspecs()) {
                colspec.accept(this);
            }
        }
        if (table.getHeaders() != null) {
            out.append("<thead>\n");
            getTableRows(table.getHeaders());
            out.append("</thead>\n");
        }
        out.append("<tbody>\n");
        getTableRows(table.getBody());
        out.append("</tbody>\n");
        out.append("</tgroup>\n");
        if (table.getCaption() != null && !table.getCaption().isEmpty()) {
            out.append("<caption>" + DocGenUtils.addDocbook(DocGenUtils.fixString(table.getCaption()))
                    + "</caption>\n");
        }
        out.append("</table>\n");

    }

    private void getTableRows(List<List<DocumentElement>> grid) {
        for (List<DocumentElement> row : grid) {
            out.append("<row>");
            for (DocumentElement cell : row) {
                if (cell instanceof DBTableEntry) {
                    cell.accept(this);
                }
                else if (cell == null) {
                    continue;
                }
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
        if (tableentry.getMorerows() > 0) {
            out.append(" morerows=\"" + tableentry.getMorerows() + "\"");
        }
        if (tableentry.getNamest() != null && !tableentry.getNamest().isEmpty()) {
            out.append(" namest=\"" + tableentry.getNamest() + "\"");
        }
        if (tableentry.getNameend() != null && !tableentry.getNameend().isEmpty()) {
            out.append(" nameend=\"" + tableentry.getNameend() + "\"");
        }
        out.append(">");
        for (DocumentElement de : tableentry.getChildren()) {
            de.accept(this);
        }
        out.append("</entry>");

    }
}
