package gov.nasa.jpl.mgss.mbee.docgen.docbook;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;


/**
 * A paragraph. Unlike DBText, this WILL do processing on the given text to escape characters and add the para tag if needed. Html also accepted.
 * @author dlam
 *
 */
public class DBParagraph extends DocumentElement {

	private String text;

	
	public DBParagraph() {}
	
	public DBParagraph(String text) {
		this.text = text;
	}
	
	public DBParagraph(String text, Element from, From fromProp) {
		this.text = text;
		this.from = from;
		this.fromProperty = fromProp;
	}
	
	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
	
 	@Override
	public void accept(IDBVisitor v) {
		v.visit(this);
	}
}
