package gov.nasa.jpl.mgss.mbee.docgen.model;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBHasContent;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBParagraph;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBTable;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;
import gov.nasa.jpl.mgss.mbee.docgen.table.Deployment;
import gov.nasa.jpl.mgss.mbee.docgen.table.EditableTable;
import gov.nasa.jpl.mgss.mbee.docgen.table.WorkpackageRollups;

import java.util.ArrayList;
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
	public void visit(boolean forViewEditor, DBHasContent parent, String outputDir) {
		int i = 0;
	    List< Element > targets =
	        isSortElementsByName() ? Utils.sortByName( getTargets() )
	                                 : getTargets();
	    for (Element t: targets) {
				if (!(t instanceof Class)) {
					continue;
				}
				Deployment d = new Deployment((Class)t, getFloatingPrecision(), isSortByName(), isSuppliesAsso(), isAuthorizesAsso(), isIncludeInherited());
				d.getDeploymentTable();
				List<List<DocumentElement>> body = new ArrayList<List<DocumentElement>>();
				WorkpackageRollups roll = getRollup(d, (Class)t, false, false, body);
				if (roll.isBad()) {
					parent.addElement(new DBParagraph("<emphasis role=\"bold\">The deployment for " + ((Class)t).getName() + " did not pass mass rollup validation!</emphasis>"));
					DBTable validation = new DBTable();
					validation.setBody(body);
					validation.setTitle("Mass Rollup Validation for Deployment Table - " + ((Class)t).getName());
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
				
					EditableTable et = d.getDeploymentEditableTable();
					int depth = d.getProductDepth();
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
					dta.setStyle(getStyle());
				}
				i++;
			}
	}
	
	@Override
	public void accept(IModelVisitor v) {
		v.visit(this);
		
	}

}
