package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.generator.Generatable;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

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

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		Boolean doNotShow = (Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.imageStereotype, "doNotShow", false);
		setCaptions((List<String>)GeneratorUtils.getListProperty(dgElement, DocGen3Profile.hasCaptions, "captions", new ArrayList<String>()));
		setShowCaptions((Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.hasCaptions, "showCaptions", true));
		setDoNotShow(doNotShow);
	}

	
}
