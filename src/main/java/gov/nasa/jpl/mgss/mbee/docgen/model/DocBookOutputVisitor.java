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
	public void visit(Query q) {
	    parent.peek().addElements(q.visit(forViewEditor, outputDir));
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
}
