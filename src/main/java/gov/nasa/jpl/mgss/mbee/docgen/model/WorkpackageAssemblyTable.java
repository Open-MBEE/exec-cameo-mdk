package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.ModelLib;
import gov.nasa.jpl.mgss.mbee.docgen.table.EditableTable;
import gov.nasa.jpl.mgss.mbee.docgen.table.WorkPackageAssembly;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;


public class WorkpackageAssemblyTable extends WorkpackageTable {

	public EditableTable getEditableTable() {
		if (this.workpackage == null) {
			for (Element t: this.targets) {
				if (ModelLib.isWorkPackage(t)) {
					this.workpackage = (NamedElement) t;
				}
			}
		}
		
		if (!(ModelLib.isWorkPackage(this.workpackage)))
			return null;
		Class c = null;
		for (Element t: this.targets) {
			if (!(t instanceof Class) || ModelLib.isWorkPackage(t)) {
				continue;
			}
			c = (Class)t;
			break;
		}
		if (c == null)
			return null;
		WorkPackageAssembly w = new WorkPackageAssembly(c, (NamedElement) this.workpackage, this.floatingPrecision, suppliesAsso, authorizesAsso, includeInherited);
		w.getWPATable();
		return w.getWPAEditableTable();
	}
	
	@Override
	public void accept(IModelVisitor v) {
		v.visit(this);
		
	}

}
