package gov.nasa.jpl.mbee.mdk.docgen.docbook;

public abstract class DBAbstractVisitor implements IDBVisitor {

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
        visitChildren(list);
    }

    @Override
    public void visit(DBListItem listitem) {
        visitChildren(listitem);
    }

    @Override
    public void visit(DBParagraph para) {
    }

    @Override
    public void visit(DBText text) {
    }

    @Override
    public void visit(DBSection section) {
        visitChildren(section);
    }

    @Override
    public void visit(DBSimpleList simplelist) {
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

    @Override
    public void visit(DBPlot plot) {
    }


    protected void visitChildren(DBHasContent d) {
        for (DocumentElement de : d.getChildren()) {
            de.accept(this);
        }
    }

}
