package gov.nasa.jpl.mgss.mbee.docgen.docbook;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * A paragraph. Unlike DBText, this WILL do processing on the given text to
 * escape characters and add the para tag if needed. Html also accepted.
 * 
 * @author dlam
 * 
 */
public class DBParagraph extends DocumentElement {

    private Object text;

    public DBParagraph() {
    }

    public DBParagraph(Object text) {
        this.text = text;
    }

    public DBParagraph(Object text, Element from, From fromProp) {
        this.text = text;
        this.from = from;
        this.fromProperty = fromProp;
    }

    public void setText(Object text) {
        this.text = text;
    }

    public Object getText() {
        return text;
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
        sb.insert(pos, ", " + getText());
        return sb.toString();
    }

}
