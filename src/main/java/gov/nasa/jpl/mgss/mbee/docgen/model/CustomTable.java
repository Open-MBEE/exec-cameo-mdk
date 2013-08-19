package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.ocl.OclEvaluator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class CustomTable extends Table {

	private List<String> headers;
	private List<String> columns;
	protected boolean oclEvaluationVerbose = false;
	
	public CustomTable() {
	  setSortElementsByName( true );
	}
	
	public Object evaluateOcl( EObject o, String expression ) {
	  return OclEvaluator.evaluateQuery( o, expression, isOclEvaluationVerbose() );
	}
	
	public void setHeaders(List<String> d) {
		headers = d;
	}
	
	public List<String> getHeaders() {
		return headers;
	}

	public List<String> getColumns() {
		return this.columns;
	}

	public void setColumns(List<String> c) {
		this.columns = c;
	}
	
  /**
   * @return the verboseEvaluation
   */
  public boolean isOclEvaluationVerbose() {
    return oclEvaluationVerbose;
  }

  /**
   * @param verboseEvaluation the verboseEvaluation to set
   */
  public void setOclEvaluationVerbose( boolean oclEvaluationVerbose ) {
    this.oclEvaluationVerbose = oclEvaluationVerbose;
  }

  @Override
	public void accept(IModelVisitor v) {
		v.visit(this);
		
	}

  @Override
  public void initialize(ActivityNode an, List<Element> in) {
	  // Get "columns" slot -- should be a list of strings (e.g., OCL expressions)
	  List<String> columns = null;
	  Object columnsO = StereotypesHelper.getStereotypePropertyValue(an, DocGen3Profile.customTableStereotype, "columns");
	  if (columnsO != null && columnsO instanceof List)
		  columns = (List<String>)columnsO;

	  setHeaders((List<String>)GeneratorUtils.getListProperty(an, DocGen3Profile.headersChoosable, "headers", new ArrayList<String>()));
	  setCaptions((List<String>)GeneratorUtils.getListProperty(an, DocGen3Profile.hasCaptions, "captions", new ArrayList<String>()));
	  setShowCaptions((Boolean)GeneratorUtils.getObjectProperty(an, DocGen3Profile.hasCaptions, "showCaptions", true));
//	  setStereotypeProperties((List<Property>)GeneratorUtils.getListProperty(an, DocGen3Profile.stereotypePropertyChoosable, "stereotypeProperties", new ArrayList<Property>()));
	  setStyle((String)GeneratorUtils.getObjectProperty(an, DocGen3Profile.tableStereotype, "style", null));
	  setColumns(columns);
	  setColwidths((List<String>)GeneratorUtils.getListProperty(an, DocGen3Profile.tableStereotype, "colwidths", new ArrayList<String>()));
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
