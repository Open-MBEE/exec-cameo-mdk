package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.ModelLib;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mbee.tree.Node;
import gov.nasa.jpl.mgss.mbee.docgen.DgvalidationDBSwitch;
import gov.nasa.jpl.mgss.mbee.docgen.DocGenUtils;
import gov.nasa.jpl.mgss.mbee.docgen.dgvalidation.Suite;
import gov.nasa.jpl.mgss.mbee.docgen.dgview.ViewElement;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBBook;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBColSpec;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBHasContent;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBImage;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBList;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBListItem;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBSection;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTable;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTableEntry;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.From;
import gov.nasa.jpl.mgss.mbee.docgen.generator.DiagramTableTool;
import gov.nasa.jpl.mgss.mbee.docgen.model.ui.LibraryComponent;
import gov.nasa.jpl.mgss.mbee.docgen.model.ui.MissionComponent;
import gov.nasa.jpl.mgss.mbee.docgen.table.BillOfMaterials;
import gov.nasa.jpl.mgss.mbee.docgen.table.Deployment;
import gov.nasa.jpl.mgss.mbee.docgen.table.EditableTable;
import gov.nasa.jpl.mgss.mbee.docgen.table.WorkPackageAssembly;
import gov.nasa.jpl.mgss.mbee.docgen.table.WorkpackageRollups;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.ApplicationEnvironment;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class DocBookOutputVisitor extends AbstractModelVisitor {

	private boolean forViewEditor;
	private Stack<DBHasContent> parent;
	private String outputDir;
	
	public DocBookOutputVisitor(boolean forViewEditor) {
		this.forViewEditor = forViewEditor;
		this.parent = new Stack<DBHasContent>();
	}
	
	public DocBookOutputVisitor(boolean forViewEditor, String outputDir) {
		this.forViewEditor = forViewEditor;
		this.parent = new Stack<DBHasContent>();
		this.outputDir = outputDir;
	}
	
	public DBBook getBook() {
		if (!parent.isEmpty() && parent.get(0) instanceof DBBook)
			return (DBBook)parent.get(0);
		return null;
	}
	
	@Override
	public void visit(BulletedList bl) {
		bl.visit(forViewEditor, parent.peek(), outputDir);
	}

	@Override
	public void visit(BillOfMaterialsTable bom) {
		bom.visit(forViewEditor, parent.peek(), outputDir);
	}

	@Override
	public void visit(CombinedMatrix cm) {
		cm.visit(forViewEditor, parent.peek(), outputDir);
	}

	@Override
	public void visit(CustomTable customTable) {
	  Debug.outln("entering visit(CustomTable): " + customTable);
    if (customTable==null) {
      Debug.errln( "Can't create DocBook table from null CustomTable!" );
      return;
    }
    if (customTable.getIgnore()) {
      return;
    }
    if (Utils2.isNullOrEmpty(customTable.getColumns())) {
      Debug.errln( "No columns specified for CustomTable! "
                   + customTable.getColumns() );
      return;
    }
    if (Utils2.isNullOrEmpty(customTable.getTargets())) {
      Debug.errln( "No targets specified for CustomTable! "
                   + customTable.getTargets() );
      return;
    }
    Debug.outln( "visiting custom table " + customTable );
		DBTable dbTable = new DBTable();
		
		// get column headings
		List<List<DocumentElement>> hs = new ArrayList<List<DocumentElement>>();
		if (!customTable.getHeaders().isEmpty()) {
			List<DocumentElement> first = new ArrayList<DocumentElement>();
			hs.add(first);
			for (String h: customTable.getHeaders())
				first.add(new DBText(h));
			dbTable.setCols(first.size());
		} else {
			List<DocumentElement> first = new ArrayList<DocumentElement>();
			hs.add(first);
			
	    if (Utils2.isNullOrEmpty(customTable.getColumns())) {
        Debug.errln( "No columns specified for CustomTable! "
                     + customTable.getColumns() );
	    } else {
	      for (String oclExpr: customTable.getColumns()) {
	        first.add(new DBText(oclExpr) );
	      }
	    }
//			for (Property p: cm.getStereotypeProperties()) 
//				first.add(new DBText(p.getName()));
			dbTable.setCols(first.size());
		}
		dbTable.setHeaders(hs);
		
		// get title
		String title = "";
		if (customTable.getTitles() != null && customTable.getTitles().size() > 0)
			title = customTable.getTitles().get(0);
		title = customTable.getTitlePrefix() + title + customTable.getTitleSuffix();
		dbTable.setTitle(title);
		
        // get caption
        if (customTable.getCaptions() != null
                && customTable.getCaptions().size() > 0
                && customTable.isShowCaptions()) {
            dbTable.setCaption(customTable.getCaptions().get(0));
        }

        // construct the main body of the table
        List<List<DocumentElement>> body = new ArrayList<List<DocumentElement>>();
        List<Element> targets = customTable.isSortElementsByName() ? Utils
                .sortByName(customTable.getTargets()) : customTable
                .getTargets();
        // construct row for each target
        for (Element e : targets) {
            List<DocumentElement> row = new ArrayList<DocumentElement>();
            // construct cell for each column
            for (String oclExpr : customTable.getColumns()) {
                Object result = customTable.evaluateOcl(e, oclExpr);
                row.add(Common.getTableEntryFromObject(result));
            }
            body.add(row);
        }
        dbTable.setBody(body);

        // set column widths
        List<DBColSpec> cslist = new ArrayList<DBColSpec>();
        if (customTable.getColwidths() != null
                && !customTable.getColwidths().isEmpty()) {
            int i = 1;
            for (String s : customTable.getColwidths()) {
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
        dbTable.setStyle(customTable.getStyle());

        parent.peek().addElement(dbTable);
        Debug.outln("got custom DBTable " + dbTable);
	}
	
	@Override
	public void visit(DependencyMatrix dm) {
	}

	@Override
	public void visit(DeploymentTable dt) {
		dt.visit(forViewEditor, parent.peek(), outputDir);
	}

	@Override
	public void visit(GenericTable gt) {
		gt.visit(forViewEditor, parent.peek(), outputDir);
	}

	@Override
	public void visit(PropertiesTableByAttributes pt) {
		pt.visit(forViewEditor, parent.peek(), outputDir);
	}

	@Override
	public void visit(Paragraph para) {
		para.visit(forViewEditor, parent.peek(), outputDir);
	}

	public void visit(TableStructure ts) {
		ts.visit(forViewEditor, parent.peek(), outputDir);
	}
	
	@Override
	public void visit(UserScript us) {
		us.visit(forViewEditor, parent.peek(), outputDir);
	}

	@Override
	public void visit(WorkpackageAssemblyTable wat) {
		wat.visit(forViewEditor, parent.peek(), outputDir);
	}

	@Override
	public void visit(Image image) {
		image.visit(forViewEditor, parent.peek(), outputDir);
	}

	@Override
	public void visit(Document doc) {
		DBBook book = new DBBook();
		book.setTitle(doc.getTitle());
		if (doc.getTitle() == null || doc.getTitle().equals(""))
			book.setTitle("Default Title");
		book.setFrom(doc.getDgElement());
		book.setSubtitle(doc.getSubtitle());
		book.setLegalnotice(doc.getLegalnotice());
		book.setAcknowledgement(doc.getAcknowledgement());
		book.setCoverimage(doc.getCoverimage());
		book.setDocumentID(doc.getDocumentID());
		book.setDocumentVersion(doc.getDocumentVersion());
		book.setLogoAlignment(doc.getLogoAlignment());
		book.setLogoLocation(doc.getLogoLocation());
		book.setAbbreviatedProjectName(doc.getAbbreviatedProjectName());
		book.setDocushareLink(doc.getDocushareLink());
		book.setAbbreviatedTitle(doc.getAbbreviatedTitle());
		book.setTitlePageLegalNotice(doc.getTitlePageLegalNotice());
		book.setFooterLegalNotice(doc.getFooterLegalNotice());
		book.setCollaboratorEmail(doc.getCollaboratorEmail());
		book.setRemoveBlankPages(doc.getRemoveBlankPages());
		book.setAuthor(doc.getAuthor());
		book.setApprover(doc.getApprover());
		book.setConcurrance(doc.getConcurrance());
		book.setJPLProjectTitle(doc.getJPLProjectTitle());
		book.setRevisionHistory(doc.getRevisionHistory());
		book.setUseDefaulStylesheet(doc.getUseDefaultStylesheet());
		book.setLogoSize(doc.getLogoSize());
		
		parent.push(book);
		visitChildren(doc);
	}

	@Override
	public void visit(Section section) {
		if (section.getIgnore())
			return;
		DBSection sec = new DBSection();
		sec.setFrom(section.getDgElement());
		sec.isAppendix(section.isAppendix());
		sec.isChapter(section.isChapter());
		sec.setView(section.isView());
		sec.isNoSection(section.isNoSection());
		String title = "";
		if (section.getTitle() != null && !section.getTitle().equals(""))
			title = section.getTitle();
		if (section.getTitlePrefix() != null)
			title = section.getTitlePrefix() + title;
		if (section.getTitleSuffix() != null)
			title = title + section.getTitleSuffix();
		sec.setTitle(title);
		sec.setStringIfEmpty(section.getStringIfEmpty());
		sec.setSkipIfEmpty(section.getSkipIfEmpty());
		if (section.getId() != null)
			sec.setId(section.getId());
	
		parent.push(sec);
		visitChildren(section);
		parent.pop();
		
		if (section.isNoSection()) {
			for (DocumentElement de: sec.getChildren()) {
				if (de instanceof DBTable)
					de.setId(section.getId());
			}
		}
		if (sec.getChildren().isEmpty()) {
			if (section.getSkipIfEmpty())
				return;
			if (section.getStringIfEmpty() != null)
				sec.addElement(new DBParagraph(section.getStringIfEmpty()));
			else
				sec.addElement(new DBParagraph(""));
		}
		parent.peek().addElement(sec);
	}

	@Override
	public void visit(MissionMapping cm) {
		cm.visit(forViewEditor, parent.peek(), outputDir);
	}
	
	@Override
	public void visit(LibraryMapping cm) {
		cm.visit(forViewEditor, parent.peek(), outputDir);
		
	}
	
}
