package gov.nasa.jpl.mgss.mbee.docgen.model;

import java.util.List;

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
}
