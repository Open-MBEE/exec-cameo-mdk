package gov.nasa.jpl.mbee.mdk.docgen.docbook;

public interface IDBVisitor {

    void visit(DBBook book);

    void visit(DBColSpec colspec);

    void visit(DBImage image);

    void visit(DBList list);

    void visit(DBListItem listitem);

    void visit(DBParagraph para);

    void visit(DBText text);

    void visit(DBSection section);

    void visit(DBSimpleList simplelist);

    void visit(DBTable table);

    void visit(DBTableEntry tableentry);

    void visit(DBTomSawyerDiagram tomSawyerDiagram);

    void visit(DBPlot plot);
}
