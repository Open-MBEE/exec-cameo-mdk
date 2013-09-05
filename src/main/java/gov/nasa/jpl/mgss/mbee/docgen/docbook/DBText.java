package gov.nasa.jpl.mgss.mbee.docgen.docbook;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;


/**
 * This is a bit like DBParagraph, but will not force it into a paragraph. <br/>
 * You can put in plain text, html, or docbook in a DBText. <br/>
 * If you want your document to look right on the view editor, then don't put any docbook tags in here. You can use html to achieve styles like bold, italics, underline etc, and DocGen will process them according to which destination it's generating for. <br/>
 * You can still put in verbatim docbook, but there's no guarantee they will show up in the view editor. If all you need is docbook output or have your document on DocWeb, then that's ok. 
 * 
 * @author dlam
 *
 */
public class DBText extends DocumentElement{
	
	private Object text;
	
	public DBText() {	}
	
	public DBText(Object s) {
		text = s;
	}
	
	public DBText(Object s, Element e, From f) {
		this.text = s;
		this.from = e;
		this.fromProperty = f;
	}
	
	public void setText(Object t) {
		text = t;
	}
	
	public Object getText() {
		return text;
	}
	
	@Override
	public void accept(IDBVisitor v) {
		v.visit(this);
	}
	
	   @Override
	    public String toString() {
	        StringBuffer sb = new StringBuffer();
	        sb.append( super.toString() );
	        int pos = sb.lastIndexOf(")");
	        sb.insert(pos, ", " + getText() );
	        return sb.toString();
	    }

}
