package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mgss.mbee.docgen.docbook.From;

import java.util.List;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

public class Paragraph extends Query {

	private String text;
	private List<Property> stereotypeProperties;
	private From fromProperty;
	
	public Paragraph(String t) {
		text = t;
	}
	
	public Paragraph() {}
	
	public void setText(String t) {
		text = t;
	}
	
	public String getText() {
		return text;
	}
	
	public void setStereotypeProperties(List<Property> p) {
		stereotypeProperties = p;
	}
	
	public List<Property> getStereotypeProperties() {
		return stereotypeProperties;
	}
	
	public void setFrom(From f) {
		fromProperty = f;
	}
	
	public From getFrom() {
		return fromProperty;
	}
	
	@Override
	public void accept(IModelVisitor v) {
		v.visit(this);
		
	}
}
