package org.openmbee.mdk.docgen.docbook;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import org.openmbee.mdk.docgen.docbook.stereotypes.EditableChoosable;

/**
 * A paragraph. Unlike DBText, this WILL do processing on the given text to
 * escape characters and add the para tag if needed. Html also accepted.
 *
 * @author dlam
 */
public class DBParagraph extends DocumentElement implements EditableChoosable {

    private Object text;
    private Boolean editable;

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

    @Override
    public Boolean isEditable() {
        return editable;
    }

    @Override
    public void setEditable(Boolean editable) {
        this.editable = editable;
    }
}
