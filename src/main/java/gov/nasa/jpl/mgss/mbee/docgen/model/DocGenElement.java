package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;

import java.util.List;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public abstract class DocGenElement implements IDocGenElement {

	protected boolean ignore;
	protected boolean loop;
	protected String titleSuffix;
	protected String titlePrefix;
	protected boolean useContextNameAsTitle;
	/**
	 * this is usually the call behavior action element in a viewpoint method
	 */
	protected Element dgElement;
	
	public DocGenElement() {
		ignore = false;
		loop = false;
		titleSuffix = "";
		titlePrefix = "";
		useContextNameAsTitle = false;
	}
	
	public boolean getIgnore() {
		return ignore;
	}
	
	public void setIgnore(boolean i) {
		ignore = i;
	}
	
	public void setTitleSuffix(String s) {
		titleSuffix = s;
	}
	
	public void setTitlePrefix(String s) {
		titlePrefix = s;
	}
	
	public void setUseContextNameAsTitle(boolean b) {
		useContextNameAsTitle = b;
	}
	
	public void setDgElement(Element e) {
		dgElement = e;
	}
	
	public String getTitlePrefix() {
		return titlePrefix;
	}
	
	public String getTitleSuffix() {
		return titleSuffix;
	}
	
	public boolean getUseContextNameAsTitle() {
		return useContextNameAsTitle;
	}
	
	public Element getDgElement() {
		return dgElement;
	}
	
	public void setLoop(boolean loop) {
		this.loop = loop;
	}
	
	public boolean getLoop() {
		return this.loop;
	}
}
