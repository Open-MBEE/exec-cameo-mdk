package gov.nasa.jpl.mgss.mbee.docgen.model;


public class DependencyMatrix extends Table {

    @Override
    public void accept(IModelVisitor v) {
        v.visit(this);

    }

}
