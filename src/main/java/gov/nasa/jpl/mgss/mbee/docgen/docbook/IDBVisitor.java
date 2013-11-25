package gov.nasa.jpl.mgss.mbee.docgen.docbook;

public interface IDBVisitor {

    public void visit(DBBook book);

    public void visit(DBColSpec colspec);

    public void visit(DBImage image);

    public void visit(DBList list);

    public void visit(DBListItem listitem);

    public void visit(DBParagraph para);

    public void visit(DBText text);

    public void visit(DBSection section);

    public void visit(DBSimpleList simplelist);

    public void visit(DBTable table);

    public void visit(DBTableEntry tableentry);
}
