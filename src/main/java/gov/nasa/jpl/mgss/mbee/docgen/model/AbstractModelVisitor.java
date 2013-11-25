package gov.nasa.jpl.mgss.mbee.docgen.model;

public abstract class AbstractModelVisitor implements IModelVisitor {

    @Override
    public void visit(Query q) {

    }

    @Override
    public void visit(Document doc) {
        visitChildren(doc);

    }

    @Override
    public void visit(Section sec) {
        visitChildren(sec);
    }

    protected void visitChildren(Container c) {
        for (DocGenElement dge: c.getChildren())
            dge.accept(this);
    }

}
