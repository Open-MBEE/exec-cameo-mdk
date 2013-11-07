package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBColSpec;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBHasContent;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTable;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.generator.CollectFilterParser;
import gov.nasa.jpl.mgss.mbee.docgen.generator.DocumentValidator;
import gov.nasa.jpl.mgss.mbee.docgen.generator.GenerationContext;
import gov.nasa.jpl.ocl.OclEvaluator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.ocl.ParserException;

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
	
	public Object evaluateOcl( EObject o, String expression ) throws ParserException {
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
  public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
      List<DocumentElement> res = new ArrayList<DocumentElement>();
      Debug.outln("entering visit(CustomTable): " + this);
      if (this==null) {
        Debug.errln( "Can't create DocBook table from null CustomTable!" );
        return res;
      }
      if (this.getIgnore()) {
        return res;
      }
      if (Utils2.isNullOrEmpty(this.getColumns())) {
        Debug.errln( "No columns specified for CustomTable! "
                     + this.getColumns() );
        return res;
      }
      if (Utils2.isNullOrEmpty(this.getTargets())) {
        Debug.errln( "No targets specified for CustomTable! "
                     + this.getTargets() );
        return res;
      }
      Debug.outln( "visiting custom table " + this );
          DBTable dbTable = new DBTable();
          
          // get column headings
          List<List<DocumentElement>> hs = new ArrayList<List<DocumentElement>>();
          if (!this.getHeaders().isEmpty()) {
              List<DocumentElement> first = new ArrayList<DocumentElement>();
              hs.add(first);
              for (String h: this.getHeaders())
                  first.add(new DBText(h));
              dbTable.setCols(first.size());
          } else {
              List<DocumentElement> first = new ArrayList<DocumentElement>();
              hs.add(first);
              
          if (Utils2.isNullOrEmpty(this.getColumns())) {
          Debug.errln( "No columns specified for CustomTable! "
                       + this.getColumns() );
          } else {
            for (String oclExpr: this.getColumns()) {
              first.add(new DBText(oclExpr) );
            }
          }
//            for (Property p: cm.getStereotypeProperties()) 
//                first.add(new DBText(p.getName()));
              dbTable.setCols(first.size());
          }
          dbTable.setHeaders(hs);
          
          // get title
          String title = "";
          if (this.getTitles() != null && this.getTitles().size() > 0)
              title = this.getTitles().get(0);
          title = this.getTitlePrefix() + title + this.getTitleSuffix();
          dbTable.setTitle(title);
          
          // get caption
          if (this.getCaptions() != null
                  && this.getCaptions().size() > 0
                  && this.isShowCaptions()) {
              dbTable.setCaption(this.getCaptions().get(0));
          }

          // construct the main body of the table
          List<List<DocumentElement>> body = new ArrayList<List<DocumentElement>>();
          List<Element> targets = this.isSortElementsByName() ? Utils
                  .sortByName(this.getTargets()) : this
                  .getTargets();
          // construct row for each target
          for (Element e : targets) {
              List<DocumentElement> row = new ArrayList<DocumentElement>();
              // construct cell for each column
              for (String oclExpr : this.getColumns()) {
                  Object result = null;
                  DocumentValidator dv = CollectFilterParser.getValidator();
                  DocumentValidator.evaluate( oclExpr, e, dv, true );
                try {
                    result = this.evaluateOcl(e, oclExpr);
                } catch ( ParserException e1 ) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                  row.add(Common.getTableEntryFromObject(result));
              }
              body.add(row);
          }
          dbTable.setBody(body);

          // set column widths
          List<DBColSpec> cslist = new ArrayList<DBColSpec>();
          if (this.getColwidths() != null
                  && !this.getColwidths().isEmpty()) {
              int i = 1;
              for (String s : this.getColwidths()) {
                  DBColSpec cs = new DBColSpec(i);
                  cs.setColwidth(s);
                  cslist.add(cs);
                  i++;
              }
          } else {
              DBColSpec cs = new DBColSpec(1);
              cs.setColwidth(".4*");
              cslist.add(cs);
          }
          dbTable.setColspecs(cslist);

          // set style
          dbTable.setStyle(this.getStyle());

          res.add(dbTable);
          Debug.outln("got custom DBTable " + dbTable);
          return res;
  }
  
  @Override
  public void initialize() {
	  super.initialize();
	  setHeaders((List<String>)GeneratorUtils.getListProperty(dgElement, DocGen3Profile.headersChoosable, "headers", new ArrayList<String>()));
	  setColumns((List<String>)GeneratorUtils.getListProperty(dgElement, DocGen3Profile.customTableStereotype, "columns", new ArrayList<String>()));
  }


}
