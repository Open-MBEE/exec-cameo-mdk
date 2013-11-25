package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBBook;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBHasContent;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBSection;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTable;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;

import java.util.Stack;

public class DocBookOutputVisitor extends AbstractModelVisitor {

    private boolean             forViewEditor;
    private Stack<DBHasContent> parent;
    private String              outputDir;

    public DocBookOutputVisitor(boolean forViewEditor) {
        this.forViewEditor = forViewEditor;
        this.parent = new Stack<DBHasContent>();
    }

    public DocBookOutputVisitor(boolean forViewEditor, String outputDir) {
        this.forViewEditor = forViewEditor;
        this.parent = new Stack<DBHasContent>();
        this.outputDir = outputDir;
    }

    public DBBook getBook() {
        if (!parent.isEmpty() && parent.get(0) instanceof DBBook)
            return (DBBook)parent.get(0);
        return null;
    }

    @Override
    public void visit(Query q) {
        parent.peek().addElements(q.visit(forViewEditor, outputDir));
    }

    @Override
    public void visit(Document doc) {
        DBBook book = new DBBook();
        book.setTitle(doc.getTitle());
        if (doc.getTitle() == null || doc.getTitle().equals(""))
            book.setTitle("Default Title");
        book.setFrom(doc.getDgElement());
        book.setSubtitle(doc.getSubtitle());
        book.setLegalnotice(doc.getLegalnotice());
        book.setAcknowledgement(doc.getAcknowledgement());
        book.setCoverimage(doc.getCoverimage());
        book.setDocumentID(doc.getDocumentID());
        book.setDocumentVersion(doc.getDocumentVersion());
        book.setLogoAlignment(doc.getLogoAlignment());
        book.setLogoLocation(doc.getLogoLocation());
        book.setAbbreviatedProjectName(doc.getAbbreviatedProjectName());
        book.setDocushareLink(doc.getDocushareLink());
        book.setAbbreviatedTitle(doc.getAbbreviatedTitle());
        book.setTitlePageLegalNotice(doc.getTitlePageLegalNotice());
        book.setFooterLegalNotice(doc.getFooterLegalNotice());
        book.setCollaboratorEmail(doc.getCollaboratorEmail());
        book.setRemoveBlankPages(doc.getRemoveBlankPages());
        book.setAuthor(doc.getAuthor());
        book.setApprover(doc.getApprover());
        book.setConcurrance(doc.getConcurrance());
        book.setJPLProjectTitle(doc.getJPLProjectTitle());
        book.setRevisionHistory(doc.getRevisionHistory());
        book.setUseDefaulStylesheet(doc.getUseDefaultStylesheet());
        book.setLogoSize(doc.getLogoSize());

        parent.push(book);
        visitChildren(doc);
    }

    @Override
    public void visit(Section section) {
        if (section.getIgnore())
            return;
        DBSection sec = new DBSection();
        sec.setFrom(section.getDgElement());
        sec.isAppendix(section.isAppendix());
        sec.isChapter(section.isChapter());
        sec.setView(section.isView());
        sec.isNoSection(section.isNoSection());
        String title = "";
        if (section.getTitle() != null && !section.getTitle().equals(""))
            title = section.getTitle();
        if (section.getTitlePrefix() != null)
            title = section.getTitlePrefix() + title;
        if (section.getTitleSuffix() != null)
            title = title + section.getTitleSuffix();
        sec.setTitle(title);
        sec.setStringIfEmpty(section.getStringIfEmpty());
        sec.setSkipIfEmpty(section.getSkipIfEmpty());
        if (section.getId() != null)
            sec.setId(section.getId());

        parent.push(sec);
        visitChildren(section);
        parent.pop();

        if (section.isNoSection()) {
            for (DocumentElement de: sec.getChildren()) {
                if (de instanceof DBTable)
                    de.setId(section.getId());
            }
        }
        if (sec.getChildren().isEmpty()) {
            if (section.getSkipIfEmpty())
                return;
            if (section.getStringIfEmpty() != null)
                sec.addElement(new DBParagraph(section.getStringIfEmpty()));
            else
                sec.addElement(new DBParagraph(""));
        }
        parent.peek().addElement(sec);
    }
}
