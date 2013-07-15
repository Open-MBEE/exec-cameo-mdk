package gov.nasa.jpl.mgss.mbee.docgen.model;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

public abstract class WorkpackageTable extends Table {
	protected Element workpackage;
	protected int floatingPrecision;
	protected boolean doRollup;
	protected boolean includeInherited;
	protected boolean authorizesAsso;
	protected boolean suppliesAsso;
	protected boolean sortByName;
	protected boolean showProducts;
	protected boolean showMassMargin;
	
	public boolean isAuthorizesAsso() {
		return authorizesAsso;
	}
	public void setAuthorizesAsso(boolean authorizesAsso) {
		this.authorizesAsso = authorizesAsso;
	}
	public boolean isSuppliesAsso() {
		return suppliesAsso;
	}
	public void setSuppliesAsso(boolean suppliesAsso) {
		this.suppliesAsso = suppliesAsso;
	}
	public boolean isDoRollup() {
		return doRollup;
	}
	public boolean isIncludeInherited() {
		return includeInherited;
	}
	public void setIncludeInherited(boolean includeInherited) {
		this.includeInherited = includeInherited;
	}
	public Element getWorkpackage() {
		return workpackage;
	}
	public void setWorkpackage(Element workpackage) {
		this.workpackage = workpackage;
	}
	public int getFloatingPrecision() {
		return floatingPrecision;
	}
	public void setFloatingPrecision(int floatingPrecision) {
		this.floatingPrecision = floatingPrecision;
	}
	public void setDoRollup(boolean doRollup) {
		this.doRollup = doRollup;
	}
	
	public void setSortByName(boolean sortByName) {
		this.sortByName = sortByName;
	}
	public boolean isShowProducts() {
		return showProducts;
	}
	public void setShowMassMargin(boolean showMassMargin){
		this.showMassMargin=showMassMargin;
	}
	public boolean isShowMassMargin(){
		return showMassMargin;
	}
	public void setShowProducts(boolean showProducts) {
		this.showProducts = showProducts;
	}
	public boolean isSortByName() {
		return sortByName;
	}
}
