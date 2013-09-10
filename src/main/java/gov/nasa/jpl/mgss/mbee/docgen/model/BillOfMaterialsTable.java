package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.ModelLib;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBHasContent;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTable;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.table.BillOfMaterials;
import gov.nasa.jpl.mgss.mbee.docgen.table.Deployment;
import gov.nasa.jpl.mgss.mbee.docgen.table.EditableTable;
import gov.nasa.jpl.mgss.mbee.docgen.table.WorkpackageRollups;

import java.util.ArrayList;
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
	public void visit(boolean forViewEditor, DBHasContent parent, String outputDir) {
		findWorkPackage();
		if (!(getWorkpackage() instanceof NamedElement))
			return;
		int i = 0;
    List< Element > targets =
        isSortElementsByName() ? Utils.sortByName( getTargets() )
                                   : getTargets();
		for (Element t: targets) {
			if (!(t instanceof Class) || ModelLib.isWorkPackage(t)) {
				continue;
			}
			BillOfMaterials b = new BillOfMaterials((Class)t, (NamedElement)getWorkpackage(), getFloatingPrecision(), false, isSuppliesAsso(), isAuthorizesAsso(), isIncludeInherited(), isShowProducts(), isShowMassMargin());
			b.getBOMTable();
			
			List<List<DocumentElement>> bodyd = new ArrayList<List<DocumentElement>>();
			WorkpackageRollups rolld = getDeploymentRollup(b, (Class)t, false, false, bodyd);
		
			List<List<DocumentElement>> body = new ArrayList<List<DocumentElement>>();
			WorkpackageRollups roll = getBOMRollup(b, (Class)t, false, false, body);
			
			if (roll.isBad()) {
				parent.addElement(new DBParagraph("<emphasis role=\"bold\">The Bill of Materials Mass Rollup did not pass validation!</emphasis>"));
				DBTable validation = new DBTable();
				validation.setBody(body);
				validation.setTitle("Mass Rollup Validation for Bill of Materials Table - " + ((NamedElement)getWorkpackage()).getName());
				List<List<DocumentElement>> header = new ArrayList<List<DocumentElement>>();
				List<DocumentElement> headerline = new ArrayList<DocumentElement>();
				headerline.add(new DBText("Element"));
				headerline.add(new DBText("Property"));
				headerline.add(new DBText("Calculated"));
				headerline.add(new DBText("Model"));
				header.add(headerline);
				validation.setHeaders(header);
				validation.setCols(4);
				parent.addElement(validation);	
			} 
			if (!roll.isBad() || forViewEditor) {
				EditableTable et = b.getBOMEditableTable();
				int depth = b.getWpDepth();
				DBTable dta = null;
				if (!forViewEditor)
					dta = Utils.getDBTableFromEditableTable(et, true, depth);
				else
					dta = Utils.getDBTableFromEditableTable(et, true);
				String title = et.getTitle();
				if (getTitles() != null && getTitles().size() > i)
					title = getTitles().get(i);
				title = getTitlePrefix() + title + getTitleSuffix();
				dta.setTitle(title);
				if (getCaptions() != null && getCaptions().size() > i && isShowCaptions())
					dta.setCaption(getCaptions().get(i));
				parent.addElement(dta);
			}
			if (rolld.isBad()) {
				parent.addElement(new DBParagraph("<emphasis role=\"bold\">The deployment for " + ((NamedElement)t).getName() + " did not pass mass rollup validation!</emphasis>"));
				DBTable validation = new DBTable();
				validation.setBody(bodyd);
				validation.setTitle("Mass Rollup Validation For " + ((NamedElement)t).getName());
				List<List<DocumentElement>> header = new ArrayList<List<DocumentElement>>();
				List<DocumentElement> headerline = new ArrayList<DocumentElement>();
				headerline.add(new DBText("Element"));
				headerline.add(new DBText("Property"));
				headerline.add(new DBText("Calculated"));
				headerline.add(new DBText("Model"));
				header.add(headerline);
				validation.setHeaders(header);
				validation.setCols(4);
				parent.addElement(validation);
			}
			i++;
		}
	}
	
	@Override
	public void accept(IModelVisitor v) {
		v.visit(this);
	}

}
