package gov.nasa.jpl.mgss.mbee.docgen.model;

public interface IModelVisitor {

	public void visit(BulletedList bl);
	public void visit(BillOfMaterialsTable bom);
	public void visit(CombinedMatrix cm);
	public void visit(CustomTable cm);
	public void visit(DependencyMatrix dm);
	public void visit(DeploymentTable dt);
	public void visit(GenericTable gt);
	public void visit(PropertiesTableByAttributes pt);
	public void visit(Paragraph para);
	public void visit(UserScript us);
	public void visit(WorkpackageAssemblyTable wat);
	public void visit(Image image);
	public void visit(Document doc);
	public void visit(Section sec);
	public void visit(LibraryMapping libraryMapping);
	public void visit(MissionMapping missionMapping);
}
