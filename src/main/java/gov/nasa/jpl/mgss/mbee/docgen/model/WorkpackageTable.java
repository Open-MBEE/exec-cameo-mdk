package gov.nasa.jpl.mgss.mbee.docgen.model;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.jpl.mbee.lib.GeneratorUtils;
import gov.nasa.jpl.mbee.lib.ModelLib;
import gov.nasa.jpl.mgss.mbee.docgen.DocGen3Profile;
import gov.nasa.jpl.mgss.mbee.docgen.actions.EditWorkpackageTableAction;
import gov.nasa.jpl.mgss.mbee.docgen.actions.RollupWorkpackageTableAction;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
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
	
//
	
	public void findWorkPackage() {
		if (this.workpackage == null) {
			for (Element t: this.targets){
				if (ModelLib.isWorkPackage(t)) {
					this.workpackage = (NamedElement) t;
				}
			}
		}
	}
	
	public void findCharacterizationName(){
		for (Element t : this.targets){
			if (ModelLib.isCharacterization(t)){
				ModelLib.MASS_CHARACTERIZATION = ((NamedElement) t).getName();
				return;
			}
		}
		// Default
		ModelLib.MASS_CHARACTERIZATION = "Launch Mass";
	}
	
	protected String massCharacterizationName;
	public void setMassCharacterizationName(String name){
		this.massCharacterizationName = name;
	}
	
	public String getMassCharacterizationName(String name){
		return massCharacterizationName;
	}
	
	public Class findFirstClass() {
		for (Element target: this.targets) {
			if (!(target instanceof Class) || ModelLib.isWorkPackage(target) || ModelLib.isCharacterization(target)) {
				continue;
			}
			return (Class)target;
		}
		return null;
	}
	
	@Override
	public void initialize() {
		super.initialize();
		Element workpackage = (Element)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.workpackageTablesStereotype, "workpackage", null);
		Boolean doRollup = (Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.workpackageTablesStereotype, "doRollup", false);
		Boolean suppliesAsso = (Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.workpackageTablesStereotype, "suppliesAsso", false);
		Boolean authorizesAsso = (Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.workpackageTablesStereotype, "authorizesAsso", false);
		Boolean sortByName = (Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.workpackageTablesStereotype, "sortDeploymentByName", false);
		Boolean showProducts = (Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.workpackageTablesStereotype, "showProducts", true);
		Boolean showMassMargin = (Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.workpackageTablesStereotype, "showMassMargin", false);
		
		setFloatingPrecision((Integer)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.precisionChoosable, "floatingPrecision", -1));
		setWorkpackage(workpackage);
		setDoRollup(doRollup);
		setIncludeInherited((Boolean)GeneratorUtils.getObjectProperty(dgElement, DocGen3Profile.inheritedChoosable, "includeInherited", false));
		setSuppliesAsso(suppliesAsso);
		setAuthorizesAsso(authorizesAsso);
		setSortByName(sortByName);
		setShowProducts(showProducts);
		setShowMassMargin(showMassMargin);
	}
	
	@Override
	public List<MDAction> getActions() {
	    List<MDAction> res = new ArrayList<MDAction>();
	    res.add(new EditWorkpackageTableAction(this));
	    if (this instanceof DeploymentTable || this instanceof BillOfMaterialsTable) {
	        res.add(new RollupWorkpackageTableAction(this));
	    }
	    return res;
	}
}
