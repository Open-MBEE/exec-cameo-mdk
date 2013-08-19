package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.From;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
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

	@Override
	public void initialize(ActivityNode an, List<Element> in) {
		setDgElement(an);
		String body = (String)GeneratorUtils.getObjectProperty(an, DocGen3Profile.paragraphStereotype, "body", "");
		setText(body);
		setStereotypeProperties((List<Property>)GeneratorUtils.getListProperty(an, DocGen3Profile.stereotypePropertyChoosable, "stereotypeProperties", new ArrayList<Property>()));
	}

	@Override
	public void parse() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DocumentElement visit(boolean forViewEditor) {
		// TODO Auto-generated method stub
		return null;
	}
}
