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
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

public class BillOfMaterialsTable extends WorkpackageTable {

	public WorkpackageRollups getRollup(boolean fix) {
		findWorkPackage();
		findCharacterizationName();

		if (!(ModelLib.isWorkPackage(this.workpackage)))
			return null;
		Class _class = findFirstClass();
		if (_class == null)
			return null;

		BillOfMaterials b = new BillOfMaterials(_class, (NamedElement) this.workpackage, this.floatingPrecision, false, suppliesAsso, authorizesAsso, includeInherited, true, this.isShowMassMargin());
		b.getBOMTable();
		return getBOMRollup(b, _class, fix, true, null);

	}

	public EditableTable getEditableTable() {
		findWorkPackage();
		findCharacterizationName();

		if (!(ModelLib.isWorkPackage(this.workpackage)))
			return null;
		Class _class = findFirstClass();
		if (_class == null)
			return null;

		BillOfMaterials b = new BillOfMaterials(_class, (NamedElement) this.workpackage, this.floatingPrecision, false, suppliesAsso, authorizesAsso, includeInherited, showProducts, showMassMargin);
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
	public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
	    List<DocumentElement> res = new ArrayList<DocumentElement>();
		findCharacterizationName();
		findWorkPackage();

		if (!(ModelLib.isWorkPackage(this.workpackage)))
			return res;

		Class _class = findFirstClass();
		if (_class == null)
			return res;

		int i = 0;
		
		BillOfMaterials bom = new BillOfMaterials((Class)_class, (NamedElement)getWorkpackage(), getFloatingPrecision(), false, isSuppliesAsso(), isAuthorizesAsso(), isIncludeInherited(), isShowProducts(), isShowMassMargin());
		bom.getBOMTable();

		List<List<DocumentElement>> bodyd = new ArrayList<List<DocumentElement>>();
		WorkpackageRollups rolld = getDeploymentRollup(bom, (Class)_class, false, false, bodyd);

		List<List<DocumentElement>> body = new ArrayList<List<DocumentElement>>();
		WorkpackageRollups roll = getBOMRollup(bom, (Class)_class, false, false, body);

		if (roll.isBad()) {
			res.add(new DBParagraph("<emphasis role=\"bold\">The Bill of Materials Mass Rollup did not pass validation!</emphasis>"));
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
			res.add(validation);	
		} 
		if (!roll.isBad() || forViewEditor) {
			EditableTable et = bom.getBOMEditableTable();
			int depth = bom.getWpDepth();
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
			res.add(dta);
		}
		if (rolld.isBad()) {
			res.add(new DBParagraph("<emphasis role=\"bold\">The deployment for " + ((NamedElement)_class).getName() + " did not pass mass rollup validation!</emphasis>"));
			DBTable validation = new DBTable();
			validation.setBody(bodyd);
			validation.setTitle("Mass Rollup Validation For " + ((NamedElement)_class).getName());
			List<List<DocumentElement>> header = new ArrayList<List<DocumentElement>>();
			List<DocumentElement> headerline = new ArrayList<DocumentElement>();
			headerline.add(new DBText("Element"));
			headerline.add(new DBText("Property"));
			headerline.add(new DBText("Calculated"));
			headerline.add(new DBText("Model"));
			header.add(headerline);
			validation.setHeaders(header);
			validation.setCols(4);
			res.add(validation);
		}
		i++;
		return res;
	}
}
