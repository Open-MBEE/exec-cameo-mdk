package gov.nasa.jpl.mgss.mbee.docgen.docbook;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;


public interface IDocumentElement {

	public void accept(IDBVisitor v);
	
	public void setFrom(Element e);
	
	public Element getFrom();
	
	public From getFromProperty();
	
	public void setFromProperty(From f);
}
