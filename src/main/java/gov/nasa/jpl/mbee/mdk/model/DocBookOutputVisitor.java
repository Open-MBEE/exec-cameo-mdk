package gov.nasa.jpl.mbee.mdk.model;

import gov.nasa.jpl.mbee.mdk.docgen.docbook.*;

import java.util.List;
import java.util.Stack;

public class DocBookOutputVisitor extends AbstractModelVisitor {

    private boolean forViewEditor;
    private Stack<DBHasContent> parent;
    private String outputDir;

    public DocBookOutputVisitor(boolean forViewEditor) {
        this.forViewEditor = forViewEditor;
        this.parent = new Stack<DBHasContent>();
    }

    public DocBookOutputVisitor(boolean forViewEditor, String outputDir) {
        this.forViewEditor = forViewEditor;
        this.parent = new Stack<DBHasContent>();
        this.outputDir = outputDir;
    }

    public Stack<DBHasContent> getParent() {
        return parent;
    }

    public DBBook getBook() {
        if (!parent.isEmpty() && parent.get(0) instanceof DBBook) {
            return (DBBook) parent.get(0);
        }
        return null;
    }

    @Override
    public void visit(Query q) {
        List<DocumentElement> results = q.visit(forViewEditor, outputDir);
        for (DocumentElement result : results) {
            result.setDgElement(q);
        }
        parent.peek().addElements(results);
    }

    @Override
    public void visit(Document doc) {
        DBBook book = new DBBook();
        book.setDgElement(doc);
        book.setTitle(doc.getTitle());
        if (doc.getTitle() == null || doc.getTitle().isEmpty()) {
            book.setTitle("Default Title");
        }
        book.setFrom(doc.getDgElement());
        book.setRemoveBlankPages(doc.getRemoveBlankPages());
        book.setUseDefaultStylesheet(doc.getUseDefaultStylesheet());
        book.setMetadata(doc.getMetadata());
        parent.push(book);
        visitChildren(doc);
    }

    @Override
    public void visit(Section section) {
        if (section.getIgnore()) {
            return;
        }
        DBSection sec = new DBSection();
        sec.setDgElement(section);
        sec.setFrom(section.getDgElement());
        sec.isAppendix(section.isAppendix());
        sec.isChapter(section.isChapter());
        sec.setView(section.isView());
        String title = "";
        if (section.getTitle() != null && !section.getTitle().isEmpty()) {
            title = section.getTitle();
        }
        if (section.getTitlePrefix() != null) {
            title = section.getTitlePrefix() + title;
        }
        if (section.getTitleSuffix() != null) {
            title = title + section.getTitleSuffix();
        }
        sec.setTitle(title);
        sec.setStringIfEmpty(section.getStringIfEmpty());
        sec.setSkipIfEmpty(section.getSkipIfEmpty());
        if (section.getId() != null) {
            sec.setId(section.getId());
        }

        parent.push(sec);
        visitChildren(section);
        parent.pop();

        if (sec.getChildren().isEmpty() && !forViewEditor) {
            if (section.getSkipIfEmpty()) {
                return;
            }
            if (section.getStringIfEmpty() != null) {
                sec.addElement(new DBParagraph(section.getStringIfEmpty()));
            }
            else {
                sec.addElement(new DBParagraph(""));
            }
        }
        parent.peek().addElement(sec);
    }
}
