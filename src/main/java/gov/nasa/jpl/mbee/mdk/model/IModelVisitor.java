package gov.nasa.jpl.mbee.mdk.model;

public interface IModelVisitor {

    void visit(Query q);

    void visit(Document doc);

    void visit(Section sec);

}
