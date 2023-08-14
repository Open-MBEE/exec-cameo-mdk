package org.openmbee.mdk.docgen.docbook;

/**
 * Use this if a cell in your table needs to have multiple things in it or it
 * spans.<br/>
 * The morerows, namest, and nameend attributes correspond to the same things in
 * actual docbook entry tag.<br/>
 * morerows is how many more rows the cell will span downward (if it doesn't,
 * don't set it, 1 means it spans 2 rows)<br/>
 * namest and nameend refer to the names of columns the cell start and end,
 * inclusive, you need to set colspecs in the containing table for this to work.
 *
 * @author dlam
 */
public class DBTableEntry extends DBHasContent {

    private int morerows;
    private String namest;
    private String nameend;

    public void setMorerows(int i) {
        morerows = i;
    }

    public void setNamest(String s) {
        namest = s;
    }

    public void setNameend(String s) {
        nameend = s;
    }

    public int getMorerows() {
        return morerows;
    }

    public String getNamest() {
        return namest;
    }

    public String getNameend() {
        return nameend;
    }

    @Override
    public void accept(IDBVisitor v) {
        v.visit(this);
    }
}
