package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.ModelLib;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBHasContent;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTable;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.table.EditableTable;
import gov.nasa.jpl.mgss.mbee.docgen.table.WorkPackageAssembly;
import gov.nasa.jpl.mgss.mbee.docgen.table.WorkpackageRollups;

import java.util.ArrayList;
import java.util.List;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;


public class WorkpackageAssemblyTable extends WorkpackageTable {

	public EditableTable getEditableTable() {
		findWorkPackage();
		findCharacterizationName();
		Class _class = findFirstClass();

		if (!(ModelLib.isWorkPackage(this.workpackage)))
			return null;

		if (_class == null)
			return null;

		WorkPackageAssembly w = new WorkPackageAssembly(_class, (NamedElement) this.workpackage, this.floatingPrecision, suppliesAsso, authorizesAsso, includeInherited);
		w.getWPATable();
		return w.getWPAEditableTable();
	}

	@Override
	public void visit(boolean forViewEditor, DBHasContent parent, String outputDir) {
		findCharacterizationName();
		findWorkPackage();
		Class _class = findFirstClass();

		if (!(ModelLib.isWorkPackage(this.workpackage)))
			return;

		int i = 0;


		WorkPackageAssembly w = new WorkPackageAssembly((Class) _class, (NamedElement) getWorkpackage(), getFloatingPrecision(), isSuppliesAsso(), isAuthorizesAsso(), isIncludeInherited());
		w.getWPATable();

		WorkpackageRollups rolld = new WorkpackageRollups(w.getDeployment(), null, null, 
				w.getRealUnits(), null,
				w.getMass(), 
				w.getMassContingency(), 
				w.getCbeContingency(),
				w.getMassAllocation(),
				w.getMassMargin(),
				false, getFloatingPrecision(), false, isShowMassMargin());
		rolld.fillExpected((NamedElement) _class);

		List<List<DocumentElement>> bodyd = new ArrayList<List<DocumentElement>>();
		rolld.validateOrFix((NamedElement) _class, bodyd);

		WorkpackageRollups roll = new WorkpackageRollups(w.getWpDeployment(), 
				w.getWp2p(), w.getDeployment(), 
				null, w.getTotalUnits(),
				w.getMass(), 
				w.getMassContingency(), 
				w.getCbeContingency(),
				w.getMassAllocation(),
				w.getMassMargin(),
				false, getFloatingPrecision(), false, isShowMassMargin());
		roll.fillExpected((NamedElement)getWorkpackage());

		List<List<DocumentElement>> body = new ArrayList<List<DocumentElement>>();
		roll.validateOrFix((NamedElement)getWorkpackage(), body);

		if (rolld.isBad() || roll.isBad()) {
			if (rolld.isBad()) {
				parent.addElement(new DBParagraph("<emphasis role=\"bold\">" + "The deployment for " + ((Class) _class).getName() + " did not pass mass rollup validation!</emphasis>"));
				DBTable validation = new DBTable();
				validation.setBody(bodyd);
				validation.setTitle("Mass Rollup Validation For " + ((NamedElement) _class).getName());
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

			if (roll.isBad()) {
				parent.addElement(new DBParagraph("<emphasis role=\"bold\">The workpackage mass rollup did not pass mass rollup validation!</emphasis>"));
				DBTable validation = new DBTable();
				validation.setBody(body);
				validation.setTitle("Mass Rollup Validation for " + ((NamedElement) getWorkpackage()).getName());
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
		} 

		if ((!rolld.isBad() && !roll.isBad()) || forViewEditor) {
			EditableTable et = w.getWPAEditableTable();
			int wpdepth = w.getWpaWpDepth();
			int pdepth = w.getWpaPDepth();
			DBTable dta = null;
			if (!forViewEditor)
				dta = Utils.getDBTableFromEditableTable(et, true, wpdepth, pdepth);
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
			dta.setStyle(getStyle());
		}
		i++;
	}


	@Override
	public void accept(IModelVisitor v) {
		v.visit(this);

	}



}
