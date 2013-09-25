package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.generator.Generatable;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.magicdraw.actions.MDAction;
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
	
  @Override
  public void initialize() {
	  
  }
  
  @Override
  public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
      return new ArrayList<DocumentElement>();
  }
	
  @Override
  public void parse() {
	  
  }
  
  @Override
  public List<MDAction> getActions() {
      return new ArrayList<MDAction>();
  }
  
  @Override
  public void accept(IModelVisitor visitor) {
      visitor.visit(this);
  }
}
