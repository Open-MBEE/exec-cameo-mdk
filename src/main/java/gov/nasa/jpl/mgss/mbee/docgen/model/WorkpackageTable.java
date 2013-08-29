package gov.nasa.jpl.mgss.mbee.docgen.model;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;

import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
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
	
	@Override
	public void initialize() {
		Element workpackage = (Element)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.workpackageTablesStereotype, "workpackage", null);
		Boolean doRollup = (Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.workpackageTablesStereotype, "doRollup", false);
		Boolean suppliesAsso = (Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.workpackageTablesStereotype, "suppliesAsso", false);
		Boolean authorizesAsso = (Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.workpackageTablesStereotype, "authorizesAsso", false);
		Boolean sortByName = (Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.workpackageTablesStereotype, "sortDeploymentByName", false);
		Boolean showProducts = (Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.workpackageTablesStereotype, "showProducts", true);
		Boolean showMassMargin = (Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.workpackageTablesStereotype, "showMassMargin", false);
		
		setCaptions((List<String>)GeneratorUtils.getListProperty(dgElement, DocGen3Profile.hasCaptions, "captions", new ArrayList<String>()));
		setShowCaptions((Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.hasCaptions, "showCaptions", true));
		setFloatingPrecision((Integer)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.precisionChoosable, "floatingPrecision", -1));
		setWorkpackage(workpackage);
		setDoRollup(doRollup);
		setIncludeInherited((Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.inheritedChoosable, "includeInherited", false));
		setSuppliesAsso(suppliesAsso);
		setAuthorizesAsso(authorizesAsso);
		setSortByName(sortByName);
		setShowProducts(showProducts);
		setStyle((String)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.tableStereotype, "style", null));
		setShowMassMargin(showMassMargin);
	}
}
