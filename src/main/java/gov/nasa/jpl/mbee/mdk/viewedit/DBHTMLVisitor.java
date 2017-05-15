package gov.nasa.jpl.mbee.mdk.viewedit;

import gov.nasa.jpl.mbee.mdk.docgen.DocGenUtils;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.*;

public class DBHTMLVisitor extends DBAbstractVisitor {

    private StringBuilder out;

    public DBHTMLVisitor() {
        out = new StringBuilder();
    }

    public String getOut() {
        return out.toString();
    }

    @Override
    public void visit(DBBook book) {
        visitChildren(book);
    }

    @Override
    public void visit(DBColSpec colspec) {
    }

    @Override
    public void visit(DBImage image) {

    }

    @Override
    public void visit(DBList list) {
        if (list.isOrdered()) {
            out.append("<ol>");
        }
        else {
            out.append("<ul>");
        }
        visitChildren(list);
        if (list.isOrdered()) {
            out.append("</ol>");
        }
        else {
            out.append("</ul>");
        }
    }

    @Override
    public void visit(DBListItem listitem) {
        out.append("<li>");
        visitChildren(listitem);
        out.append("</li>");
    }

    @Override
    public void visit(DBParagraph para) {
        out.append(DocGenUtils.addP(DocGenUtils.fixString(para.getText(), false)));
    }

    @Override
    public void visit(DBText text) {
        out.append(DocGenUtils.addP(DocGenUtils.fixString(text.getText(), false)));
    }

    @Override
    public void visit(DBSection section) {
        visitChildren(section);
    }

    @Override
    public void visit(DBSimpleList simplelist) {
        out.append("<ul>");
        for (Object o : simplelist.getContent()) {
            out.append("<li>");
            out.append(DocGenUtils.addP(DocGenUtils.fixString(o, false)));
            out.append("</li>");
        }
        out.append("</ul>");
    }

    @Override
    public void visit(DBTable table) {
    }

    @Override
    public void visit(DBTableEntry tableentry) {
    }

    @Override
    public void visit(DBTomSawyerDiagram tomSawyerDiagram) {

    }
}
