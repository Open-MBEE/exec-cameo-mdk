package gov.nasa.jpl.mbee.mdk.model;

public interface IDocGenElement {
    void accept(IModelVisitor v);
}
