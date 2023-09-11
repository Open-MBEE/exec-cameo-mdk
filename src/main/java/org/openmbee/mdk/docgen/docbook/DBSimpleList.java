package org.openmbee.mdk.docgen.docbook;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple list. items in the list (any object) will be converted to text as
 * appropriate. Do not put other docbook elements in here.<br/>
 * Ex text conversions: NamedElement to their names, value specs to string
 * representations of their values, etc.
 *
 * @author dlam
 */
public class DBSimpleList extends DocumentElement {

    private List<Object> content;

    public DBSimpleList() {
        content = new ArrayList<Object>();
    }

    public void setContent(List<Object> content) {
        this.content = content;
    }

    public void add(Object o) {
        content.add(o);
    }

    public List<Object> getContent() {
        return content;
    }

    @Override
    public void accept(IDBVisitor v) {
        v.visit(this);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        int pos = sb.lastIndexOf(")");
        sb.insert(pos, ", " + getContent());
        return sb.toString();
    }

}
