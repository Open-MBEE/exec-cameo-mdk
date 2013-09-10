package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBHasContent;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.generator.Generatable;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public abstract class Query extends DocGenElement implements Generatable {
	protected List<Element> targets;
	protected List<String> titles;
  protected boolean sortElementsByName = false;	
	
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

  public boolean isSortElementsByName() {
    return sortElementsByName;
  }

  public void setSortElementsByName( boolean sortElementsByName ) {
    this.sortElementsByName = sortElementsByName;
  }
	
  public void initialize() {
	  
  }
  
  public void visit(boolean forViewEditor, DBHasContent parent, String outputDir) {
  }
	
  public void parse() {
	  
  }
}
