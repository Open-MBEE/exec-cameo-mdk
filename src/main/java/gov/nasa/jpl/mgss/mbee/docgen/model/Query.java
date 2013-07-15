package gov.nasa.jpl.mgss.mbee.docgen.model;

import java.util.List;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public abstract class Query extends DocGenElement {
	protected List<Element> targets;
	protected List<String> titles;	
	
	public void setTargets(List<Element> t) {
		targets = t;
	}
	
	public List<Element> getTargets() {
		return targets;
	}
	
	public void setTitles(List<String> t) {
		titles = t;
	}
	
	public List<String> getTitles() {
		return titles;
	}
	
	
}
