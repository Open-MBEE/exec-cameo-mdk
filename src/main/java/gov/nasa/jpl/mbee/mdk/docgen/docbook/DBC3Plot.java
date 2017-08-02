package gov.nasa.jpl.mbee.mdk.docgen.docbook;

import gov.nasa.jpl.mbee.mdk.docgen.docbook.DocumentElement;

/**
 * Created by mw107 on 7/7/2017.
 */
public class DBC3Plot extends DocumentElement
{

    private String options;
    private String functions;
    private DBTable table;

    public DBTable getTable()
    {
        return table;
    }

    public void setTable(DBTable table)
    {
        this.table = table;
    }


    @Override
    public void accept(IDBVisitor v) {
        v.visit(this);
    }

    public String getOptions()
    {
        return options;
    }

    public void setOptions(String options)
    {
        this.options = options;
    }

    public String getFunctions()
    {
        return functions;
    }

    public void setFunctions(String functions)
    {
        this.functions = functions;
    }
}
