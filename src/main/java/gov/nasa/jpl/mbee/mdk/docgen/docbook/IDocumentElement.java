package gov.nasa.jpl.mbee.mdk.docgen.docbook;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public interface IDocumentElement {

    void accept(IDBVisitor v);

    void setFrom(Element e);

    Element getFrom();

    From getFromProperty();

    void setFromProperty(From f);
}
