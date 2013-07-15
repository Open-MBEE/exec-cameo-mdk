package gov.nasa.jpl.mgss.mbee.docgen.model;

import java.util.List;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

public abstract class Table extends Query {

	protected boolean includeDoc;
	protected List<Property> stereotypeProperties;
	protected List<String> captions;
	protected boolean showCaptions;
	protected String style;
	protected List<String> colwidths;
	
	public void setIncludeDoc(boolean d) {
		includeDoc = d;
	}
	
	public boolean isIncludeDoc() {
		return includeDoc;
	}
	
	public void setStereotypeProperties(List<Property> p) {
		stereotypeProperties = p;
	}
	
	public void setCaptions(List<String> c) {
		captions = c;
	}
	
	public void setShowCaptions(boolean b) {
		showCaptions = b;
	}
	
	public Boolean isShowCaptions() {
		return showCaptions;
	}
	
	public List<String> getCaptions() {
		return captions;
	}
	
	public List<Property> getStereotypeProperties() {
		return stereotypeProperties;
	}
	
	public void setStyle(String s) {
		style = s;
	}
	
	public String getStyle() {
		return style;
	}
	
	public void setColwidths(List<String> colwidths) {
		this.colwidths = colwidths;
	}
	
	public List<String> getColwidths() {
		return colwidths;
	}
}
