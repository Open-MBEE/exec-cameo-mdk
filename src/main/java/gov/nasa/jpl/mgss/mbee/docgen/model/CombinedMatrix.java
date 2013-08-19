package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
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
	public void accept(IModelVisitor v) {
		v.visit(this);
		
	}

	@Override
	public void initialize(ActivityNode an, List<Element> in) {
		// TODO Auto-generated method stub
		Integer nameColumn = (Integer)GeneratorUtils.getObjectProperty(an, DocGen3Profile.combinedMatrixStereotype, "nameColumn", 1);
		Integer docColumn = (Integer)GeneratorUtils.getObjectProperty(an, DocGen3Profile.combinedMatrixStereotype, "docColumn", 2);
		nameColumn = nameColumn < 1 ? 1 : nameColumn;
		docColumn = docColumn < 1 ? 2 : docColumn;
		setHeaders((List<String>)GeneratorUtils.getListProperty(an, DocGen3Profile.headersChoosable, "headers", new ArrayList<String>()));
		setCaptions((List<String>)GeneratorUtils.getListProperty(an, DocGen3Profile.hasCaptions, "captions", new ArrayList<String>()));
		setShowCaptions((Boolean)GeneratorUtils.getObjectProperty(an, DocGen3Profile.hasCaptions, "showCaptions", true));
		setStereotypeProperties((List<Property>)GeneratorUtils.getListProperty(an, DocGen3Profile.stereotypePropertyChoosable, "stereotypeProperties", new ArrayList<Property>()));
		setOutgoing((List<Stereotype>)GeneratorUtils.getListProperty(an, DocGen3Profile.stereotypedRelChoosable, "outgoingStereotypedRelationships", new ArrayList<Stereotype>()));
		setIncoming((List<Stereotype>)GeneratorUtils.getListProperty(an, DocGen3Profile.stereotypedRelChoosable, "incomingStereotypedRelationships", new ArrayList<Stereotype>()));
		setIncludeDoc((Boolean)GeneratorUtils.getObjectProperty(an, DocGen3Profile.documentationChoosable, "includeDoc", false));
		setSkipIfNoDoc((Boolean)GeneratorUtils.getObjectProperty(an, DocGen3Profile.docSkippable, "skipIfNoDoc", false));
		setStyle((String)GeneratorUtils.getObjectProperty(an, DocGen3Profile.tableStereotype, "style", null));
		setNameColumn(nameColumn);
		setDocColumn(docColumn);
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
