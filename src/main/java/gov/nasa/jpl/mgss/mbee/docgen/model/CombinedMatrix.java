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
