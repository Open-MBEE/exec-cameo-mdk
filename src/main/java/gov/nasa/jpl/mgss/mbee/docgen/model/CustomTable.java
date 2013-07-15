package gov.nasa.jpl.mgss.mbee.docgen.model;

import java.util.List;

import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class CustomTable extends Table {

	//TODO decide tag options
	//"cMatSpec" means this variable/method was specific to CustomMatrix3
	
	private List<String> headers;
	private List<Stereotype> outgoing;
	private List<Stereotype> incoming;
	private boolean skipIfNoDoc;
	private List<String> columns;
	
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
	
	public List<String> getColumns() {
		return this.columns;
	}

	public void setColumns(List<String> c) {
		this.columns = c;
	}
	
	@Override
	public void accept(IModelVisitor v) {
		v.visit(this);
		
	}
}
