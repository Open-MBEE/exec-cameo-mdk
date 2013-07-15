package gov.nasa.jpl.mgss.mbee.docgen.model;


public interface IDocGenElement {
	public void accept(IModelVisitor v);
}
