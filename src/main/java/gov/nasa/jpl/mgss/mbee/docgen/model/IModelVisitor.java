package gov.nasa.jpl.mgss.mbee.docgen.model;

public interface IModelVisitor {

	public void visit(Query q);
	public void visit(Document doc);
	public void visit(Section sec);
	
}
