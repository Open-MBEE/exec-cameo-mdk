package gov.nasa.jpl.mgss.mbee.docgen.model;

public abstract class AbstractModelVisitor implements IModelVisitor {

	@Override
	public void visit(BulletedList bl) {
	}

	@Override
	public void visit(BillOfMaterialsTable bom) {
	}

	@Override
	public void visit(CombinedMatrix cm) {
	}

	@Override
	public void visit(CustomTable cm) {
	}
	
	@Override
	public void visit(DependencyMatrix dm) {
	}

	@Override
	public void visit(DeploymentTable dt) {
	}

	@Override
	public void visit(GenericTable gt) {
	}

	@Override
	public void visit(PropertiesTableByAttributes pt) {
	}

	@Override
	public void visit(Paragraph para) {
	}

	@Override
	public void visit(UserScript us) {
	}

	@Override
	public void visit(WorkpackageAssemblyTable wat) {
	}

	@Override
	public void visit(Image image) {
	}

	@Override
	public void visit(Document doc) {
		visitChildren(doc);
		
	}

	@Override
	public void visit(Section sec) {
		visitChildren(sec);
	}
	
	@Override
	public void visit(LibraryMapping cm) {
		
	}
	
	@Override
	public void visit(MissionMapping mm) {
		
	}
	protected void visitChildren(Container c) {
		for (DocGenElement dge: c.getChildren())
			dge.accept(this);
	}

}
