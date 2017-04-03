package gov.nasa.jpl.mbee.mdk.docgen.docbook;

import java.util.ArrayList;
import java.util.List;

public abstract class DBHasContent extends DocumentElement {
    protected List<DocumentElement> children;

    public DBHasContent() {
        children = new ArrayList<DocumentElement>();
    }

    public List<DocumentElement> getChildren() {
        return children;
    }

    public void addElement(DocumentElement e) {
        children.add(e);
    }

    public void addElements(List<DocumentElement> es) {
        children.addAll(es);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        int pos = sb.lastIndexOf(")");
        sb.insert(pos, ", children=" + getChildren());
        return sb.toString();
    }

}
