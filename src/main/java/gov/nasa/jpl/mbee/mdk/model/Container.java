package gov.nasa.jpl.mbee.mdk.model;

import gov.nasa.jpl.mbee.mdk.lib.MoreToString;

import java.util.ArrayList;
import java.util.List;

public abstract class Container extends DocGenElement {
    protected String title;
    protected String stringIfEmpty;
    protected boolean skipIfEmpty;
    protected List<DocGenElement> children;

    public Container() {
        children = new ArrayList<DocGenElement>();
        title = null;
        stringIfEmpty = "";
        skipIfEmpty = false;
    }

    public void addElement(DocGenElement e) {
        children.add(e);
    }

    public List<DocGenElement> getChildren() {
        return children;
    }

    public void setSkipIfEmpty(boolean a) {
        skipIfEmpty = a;
    }

    public void setTitle(String t) {
        title = t;
    }

    public void setStringIfEmpty(String t) {
        stringIfEmpty = t;
    }

    public String getTitle() {
        return title;
    }

    public String getStringIfEmpty() {
        return stringIfEmpty;
    }

    public boolean getSkipIfEmpty() {
        return skipIfEmpty;
    }

    public boolean isEmpty() {
        return children.isEmpty();
    }

    @Override
    protected String toStringStart() {
        return super.toStringStart() + "title=" + title;
    }

    @Override
    protected String toStringEnd() {
        return ",children=" + MoreToString.Helper.toString(children) + super.toStringEnd();
    }
}
