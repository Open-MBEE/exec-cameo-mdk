package gov.nasa.jpl.mgss.mbee.docgen.model;

import java.util.List;

public class Image extends Query {

	protected List<String> captions;
	protected boolean showCaptions;
	protected boolean doNotShow;
	
	public void setCaptions(List<String> c) {
		captions = c;
	}
	
	public void setShowCaptions(boolean b) {
		showCaptions = b;
	}
	
	public void setDoNotShow(boolean b) {
		doNotShow = b;
	}

	public Boolean getDoNotShow() {
		return doNotShow;
	}
	
	public Boolean getShowCaptions() {
		return showCaptions;
	}
	
	public List<String> getCaptions() {
		return captions;
	}
	@Override
	public void accept(IModelVisitor v) {
		v.visit(this);
		
	}
}
