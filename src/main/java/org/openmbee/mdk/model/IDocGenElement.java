package org.openmbee.mdk.model;

public interface IDocGenElement {
    void accept(IModelVisitor v);
}
