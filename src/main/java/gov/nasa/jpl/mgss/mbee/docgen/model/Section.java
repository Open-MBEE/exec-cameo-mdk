package gov.nasa.jpl.mgss.mbee.docgen.model;


/**
 * this should really be called View now
 * @author dlam
 *
 */
public class Section extends Container {
	private boolean isAppendix;
	private boolean isChapter;
	private String id;
	private boolean isView;
	private boolean isNoSection;
	
	public Section() {
		isAppendix = false;
		isChapter = false;
	}
	
	public void isAppendix(boolean a) {
		isAppendix = a;
	}
	
	public boolean isAppendix() {
		return isAppendix;
	}
	
	public void isChapter(boolean c) {
		isChapter = c;
	}
	
	public boolean isChapter() {
		return isChapter;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public void setView(boolean b) {
		this.isView = b;
	}
	
	public boolean isView() {
		return this.isView;
	}
	
	public void setNoSection(boolean b) {
		this.isNoSection = b;
	}
	
	public boolean isNoSection() {
		return this.isNoSection;
	}
	
	public String getId() {
		return this.id;
	}

	@Override
	public void accept(IModelVisitor v) {
		v.visit(this);
		
	}
}
