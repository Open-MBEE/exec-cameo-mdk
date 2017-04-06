package gov.nasa.jpl.mbee.mdk.docgen.docbook;

/**
 * docbook column spec in a table<br/>
 * assign a name to a column number (this is needed if you have table entries
 * that span columns)<br/>
 * if just given a column number to constructor the col name would be the string
 * of the integer a list of colspecs can be assigned to a DBTable<br/>
 *
 * @author dlam
 */
public class DBColSpec extends DocumentElement {
    private int colnum;
    private String colname;
    private String colwidth;

    public DBColSpec(int num, String name) {
        colnum = num;
        colname = name;
    }

    public DBColSpec(int num) {
        colnum = num;
        colname = Integer.toString(num);
    }

    public DBColSpec() {

    }

    public DBColSpec(int num, String name, String colwidth) {
        colnum = num;
        colname = name;
        this.colwidth = colwidth;
    }

    public void setColnum(int num) {
        colnum = num;
    }

    public void setColname(String name) {
        colname = name;
    }

    public void setColwidth(String colwidth) {
        this.colwidth = colwidth;
    }

    public int getColnum() {
        return colnum;
    }

    public String getColname() {
        return colname;
    }

    public String getColwidth() {
        return colwidth;
    }

    @Override
    public void accept(IDBVisitor v) {
        v.visit(this);
    }

}
