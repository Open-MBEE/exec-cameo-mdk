package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.table.Deployment;
import gov.nasa.jpl.mgss.mbee.docgen.table.EditableTable;
import gov.nasa.jpl.mgss.mbee.docgen.table.WorkpackageRollups;

import java.util.List;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;



public class DeploymentTable extends WorkpackageTable {
	
	public WorkpackageRollups getRollup(boolean fix) {
		Class c = null;
		for (Element t: this.targets) {
			if (!(t instanceof Class)) {
				continue;
			}
			c = (Class)t;
			break;
		}
		if (c == null)
			return null;
		Deployment d = new Deployment(c, this.floatingPrecision, sortByName, suppliesAsso, authorizesAsso, includeInherited);
		d.getDeploymentTable();
		return getRollup(d, c, fix, true, null);
	}
	
	public EditableTable getEditableTable() {
		Class c = null;
		for (Element t: this.targets) {
			if (!(t instanceof Class)) {
				continue;
			}
			c = (Class)t;
			break;
		}
		if (c == null)
			return null;
		Deployment d = new Deployment(c, this.floatingPrecision, sortByName, suppliesAsso, authorizesAsso, includeInherited);
		d.getDeploymentTable();
		return d.getDeploymentEditableTable();
	}
	
	public WorkpackageRollups getRollup(Deployment d, Class c, boolean fix, boolean gui, List<List<DocumentElement>> body) {
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

	@Override
	public void parse() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DocumentElement visit(boolean forViewEditor) {
		// TODO Auto-generated method stub
		return null;
	}
}
