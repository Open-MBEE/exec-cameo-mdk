package gov.nasa.jpl.mbee.mdk.model;


public class DependencyMatrix extends Table {

    @Override
    public void accept(IModelVisitor v) {
        v.visit(this);

    }

}
