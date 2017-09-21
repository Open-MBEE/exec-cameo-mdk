package gov.nasa.jpl.mbee.mdk.docgen.docbook;

/**
 * Created by mw107 on 7/7/2017.
 */
public class DBPlot extends DocumentElement {

    private String type;
    private String title;
    private String config;
    private DBTable table;

    public DBTable getTable() {
        return table;
    }

    public void setTable(DBTable table) {
        this.table = table;
    }


    @Override
    public void accept(IDBVisitor v) {
        v.visit(this);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
