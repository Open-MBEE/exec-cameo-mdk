package gov.nasa.jpl.mgss.mbee.docgen.docbook;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * 
 * @author dlam
 * 
 */
public abstract class DocumentElement implements IDocumentElement {
    protected String  id;
    protected String  title;
    protected Element from;        // this is for view editor syncing,
                                    // indicates what md element is the source
                                    // of this document element's content, if
                                    // applicable
    protected From    fromProperty; // this is for view editor purpose (where
                                    // the document fragment comes from -
                                    // element's name/documentation, etc)

    public DocumentElement() {
        title = "";
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    @Override
    public Element getFrom() {
        return from;
    }

    @Override
    public void setFrom(Element e) {
        this.from = e;
    }

    @Override
    public void setFromProperty(From f) {
        this.fromProperty = f;
    }

    @Override
    public From getFromProperty() {
        return fromProperty;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        sb.append("id=" + id + ", ");
        sb.append("title=" + title + ", ");
        sb.append("from=" + (from == null ? "null" : from.getHumanName()) + ", ");
        sb.append("fromProperty=" + fromProperty);
        sb.append(")");
        return sb.toString();
    }

}
