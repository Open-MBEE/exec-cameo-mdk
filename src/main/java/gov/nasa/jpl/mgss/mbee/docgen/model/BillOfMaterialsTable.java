package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.ModelLib;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.table.BillOfMaterials;
import gov.nasa.jpl.mgss.mbee.docgen.table.Deployment;
import gov.nasa.jpl.mgss.mbee.docgen.table.EditableTable;
import gov.nasa.jpl.mgss.mbee.docgen.table.WorkpackageRollups;

import java.util.List;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

public class BillOfMaterialsTable extends WorkpackageTable {
	public void findWorkPackage() {
		if (this.workpackage == null) {
			for (Element t: this.targets)
				if (ModelLib.isWorkPackage(t))
					this.workpackage = (NamedElement) t;
		}
	}
	
	public Class findFirstClass() {
		for (Element t: this.targets) {
			if (!(t instanceof Class) || ModelLib.isWorkPackage(t)) {
				continue;
			}
			return (Class)t;
		}
		return null;
	}
	
	public WorkpackageRollups getRollup(boolean fix) {
		findWorkPackage();
		if (!(ModelLib.isWorkPackage(this.workpackage)))
			return null;
		Class c = findFirstClass();
		if (c == null)
			return null;
		BillOfMaterials b = new BillOfMaterials(c, (NamedElement) this.workpackage, this.floatingPrecision, false, suppliesAsso, authorizesAsso, includeInherited, true, this.isShowMassMargin());
		b.getBOMTable();
		return getBOMRollup(b, c, fix, true, null);

	}
	
	public EditableTable getEditableTable() {
		findWorkPackage();
		if (!(ModelLib.isWorkPackage(this.workpackage)))
			return null;
		Class c = findFirstClass();
		if (c == null)
			return null;
		BillOfMaterials b = new BillOfMaterials(c, (NamedElement) this.workpackage, this.floatingPrecision, false, suppliesAsso, authorizesAsso, includeInherited, showProducts, showMassMargin);
		b.getBOMTable();
		return b.getBOMEditableTable();
	}
	
	public WorkpackageRollups getBOMRollup(BillOfMaterials b, Class c, boolean fix, boolean gui, List<List<DocumentElement>> body) {
		WorkpackageRollups roll = new WorkpackageRollups(b.getWpDeployment(), 
				b.getWp2p(), b.getDeployment(), 
				null, b.getTotalUnits(),
				b.getMass(), 
				b.getMassContingency(), 
				b.getCbeContingency(),
				b.getMassAllocation(),
				b.getMassMargin(),
				fix, floatingPrecision, gui, this.isShowMassMargin());
		roll.fillExpected((NamedElement)workpackage);			
		roll.validateOrFix((NamedElement)workpackage, body);
		return roll;
	}

	public WorkpackageRollups getDeploymentRollup(Deployment d, Class c, boolean fix, boolean gui, List<List<DocumentElement>> body) {
		WorkpackageRollups roll = new WorkpackageRollups(d.getDeployment(), null, null, 
				d.getRealUnits(), null,
				d.getMass(), 
				d.getMassContingency(), 
				d.getCbeContingency(),
				d.getMassAllocation(),
				d.getMassMargin(),
				fix, floatingPrecision, gui, this.isShowMassMargin());
		roll.fillExpected(c);
		roll.validateOrFix(c, body);
		return roll;
	}
	@Override
	public void accept(IModelVisitor v) {
		v.visit(this);
	}
}
