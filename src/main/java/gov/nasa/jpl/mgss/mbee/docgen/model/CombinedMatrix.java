package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.DocGenUtils;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBColSpec;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBHasContent;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTable;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.From;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class CombinedMatrix extends Table {

	private List<String> headers;
	private List<Stereotype> outgoing;
	private List<Stereotype> incoming;
	private boolean skipIfNoDoc;
	private int nameColumn = 1;
	private int docColumn = 2;
	
	public CombinedMatrix() {
		setSortElementsByName( true );
	}
  
	public void setSkipIfNoDoc(boolean b) {
		skipIfNoDoc = b;
	}
	
	public void setHeaders(List<String> d) {
		headers = d;
	}
	
	public void setOutgoing(List<Stereotype> s) {
		outgoing = s;
	}
	
	public void setIncoming(List<Stereotype> s) {
		incoming = s;
	}

	public List<String> getHeaders() {
		return headers;
	}

	public List<Stereotype> getOutgoing() {
		return outgoing;
	}

	public List<Stereotype> getIncoming() {
		return incoming;
	}

	public boolean isSkipIfNoDoc() {
		return skipIfNoDoc;
	}

	public int getNameColumn() {
		return nameColumn;
	}

	public void setNameColumn(int nameColumn) {
		this.nameColumn = nameColumn;
	}

	public int getDocColumn() {
		return docColumn;
	}

	public void setDocColumn(int docColumn) {
		this.docColumn = docColumn;
	}

	@Override
	public void visit(boolean forViewEditor, DBHasContent parent, String outputDir) {
		if (getIgnore())
			return;
		DBTable dbTable = new DBTable();
		List<List<DocumentElement>> hs = new ArrayList<List<DocumentElement>>();
		if (!getHeaders().isEmpty()) {
			List<DocumentElement> first = new ArrayList<DocumentElement>();
			hs.add(first);
			for (String h: getHeaders())
				first.add(new DBText(h));
			dbTable.setCols(first.size());
		} else {
			List<DocumentElement> first = new ArrayList<DocumentElement>();
			hs.add(first);
			/*first.add(new DBText("Name"));
			if (isIncludeDoc())
				first.add(new DBText("Description"));*/
			for (Property p: getStereotypeProperties()) 
				first.add(new DBText(p.getName()));
			for (Stereotype s: getOutgoing())
				first.add(new DBText(s.getName()));
			for (Stereotype s: getIncoming())
				first.add(new DBText(s.getName()));
			if (getNameColumn() < getDocColumn()) {
				first.add(getNameColumn()-1, new DBText("Name"));
				if (isIncludeDoc())
					first.add(getDocColumn()-1, new DBText("Description"));
			}
			else {
				if (isIncludeDoc())
					first.add(getDocColumn()-1, new DBText("Description"));
				first.add(getNameColumn()-1, new DBText("Name"));
			}
			dbTable.setCols(first.size());
		}
		dbTable.setHeaders(hs);
		
		List<List<DocumentElement>> body = new ArrayList<List<DocumentElement>>();
		List< Element > targets = isSortElementsByName() ? Utils.sortByName( getTargets() ) : getTargets();
		for (Element e: targets) {
			if (isSkipIfNoDoc() && ModelHelper.getComment(e).trim().equals(""))
				continue;
			List<DocumentElement> row = new ArrayList<DocumentElement>();
			for (Property p: getStereotypeProperties()) 
				row.add(Common.getStereotypePropertyEntry(e, p));
			for (Stereotype s: getOutgoing()) {
				List<Element> blah = new ArrayList<Element>();
				blah.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotype(e, s, 1, true, 1));
                row.add(Common.getTableEntryFromObject(blah));
			}
			for (Stereotype s: getIncoming()) {
				List<Element> blah = new ArrayList<Element>();
				blah.addAll(Utils.collectDirectedRelatedElementsByRelationshipStereotype(e, s, 2, true, 1));
				row.add(Common.getTableEntryFromObject(blah));
			}
			DocumentElement name = null;
			DocumentElement doc = null;
			if (e instanceof NamedElement) {
				if (!forViewEditor)
					name = new DBText(DocGenUtils.addInvisibleSpace(DocGenUtils.fixString(((NamedElement)e).getName())));
				else
					name = new DBParagraph(((NamedElement)e).getName(), e, From.NAME);
			} else
				name = new DBParagraph(e.getHumanName());
			doc = new DBParagraph(ModelHelper.getComment(e), e, From.DOCUMENTATION);
			if (getNameColumn() < getDocColumn()) {
				row.add(getNameColumn()-1, name);
				if (isIncludeDoc())
					row.add(getDocColumn()-1, doc);
			}
			else {
				if (isIncludeDoc())
					row.add(getDocColumn()-1, doc);
				row.add(getNameColumn()-1, name);
			}
			body.add(row);
		}
		dbTable.setBody(body);
		if (colwidths != null && colwidths.isEmpty())
			colwidths.add(".4*");
		setTableThings(dbTable);
		parent.addElement(dbTable);
	}
	
	@Override
	public void accept(IModelVisitor v) {
		v.visit(this);
		
	}

	@Override
	public void initialize() {
		super.initialize();
		// TODO Auto-generated method stub
		Integer nameColumn = (Integer)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.combinedMatrixStereotype, "nameColumn", 1);
		Integer docColumn = (Integer)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.combinedMatrixStereotype, "docColumn", 2);
		nameColumn = nameColumn < 1 ? 1 : nameColumn;
		docColumn = docColumn < 1 ? 2 : docColumn;
		setHeaders((List<String>)GeneratorUtils.getListProperty(dgElement, DocGen3Profile.headersChoosable, "headers", new ArrayList<String>()));
		setOutgoing((List<Stereotype>)GeneratorUtils.getListProperty(dgElement, DocGen3Profile.stereotypedRelChoosable, "outgoingStereotypedRelationships", new ArrayList<Stereotype>()));
		setIncoming((List<Stereotype>)GeneratorUtils.getListProperty(dgElement, DocGen3Profile.stereotypedRelChoosable, "incomingStereotypedRelationships", new ArrayList<Stereotype>()));
		setSkipIfNoDoc((Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.docSkippable, "skipIfNoDoc", false));
		setNameColumn(nameColumn);
		setDocColumn(docColumn);
	}

}
