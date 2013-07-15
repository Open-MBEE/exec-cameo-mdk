package gov.nasa.jpl.mgss.mbee.docgen.docbook;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * 
 * @author dlam
 *
 */
public abstract class DocumentElement implements IDocumentElement {
	protected String id;
	protected String title;
	protected Element from; //this is for view editor syncing, indicates what md element is the source of this document element's content, if applicable
	protected From fromProperty; //this is for view editor purpose (where the document fragment comes from - element's name/documentation, etc)
	
	public DocumentElement() {title = "";}
	
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
	
	public Element getFrom() {
		return from;
	}
	
	public void setFrom(Element e) {
		this.from = e;
	}
	
	public void setFromProperty(From f) {
		this.fromProperty = f;
	}
	
	public From getFromProperty() {
		return fromProperty;
	}
	
}
