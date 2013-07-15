package gov.nasa.jpl.mgss.mbee.docgen.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

/**
 * hybrid of BOM and deployment for ehm
 * should really be a userscript
 * @author dlam
 *
 */
public class WorkPackageAssembly extends BillOfMaterials {

	protected Map<NamedElement, List<Class>> wpTopProduct;
	protected int wpaWpDepth;
	protected int wpaPDepth;
	protected EditableTable wpaTable;
	
	public int getWpaWpDepth() {
		return wpaWpDepth;
	}

	public void setWpaWpDepth(int wpaWpDepth) {
		this.wpaWpDepth = wpaWpDepth;
	}

	public int getWpaPDepth() {
		return wpaPDepth;
	}

	public void setWpaPDepth(int wpaPDepth) {
		this.wpaPDepth = wpaPDepth;
	}

	public WorkPackageAssembly(Class product, NamedElement workpackage, int precision, boolean suppliesAsso, boolean authorizesAsso, boolean includeInherited) {
		super(product, workpackage, precision, false, suppliesAsso, authorizesAsso, includeInherited, true, true);
		wpTopProduct = new HashMap<NamedElement, List<Class>>();
	}
	
	public void getWPATable() {
		getBOMTable();
		for(NamedElement wp: wpDeployment.keySet()) {
			List<Class> top = new ArrayList<Class>();
			getWPTopProduct(product, wp2p.get(wp), top, new HashSet<Class>(), true);
			wpTopProduct.put(wp, top);
		}
		List<List<Object>> wpatable = new ArrayList<List<Object>>();
		wpaWpDepth = wpDepth - 1;
		wpaPDepth = productDepth - 1;
		getWPATableModel(wpatable, workpackage, 1);

		List<Boolean> editableCol = new ArrayList<Boolean>();
		List<PropertyEnum> whatToShowCol = new ArrayList<PropertyEnum>();
		List<String> headers = new ArrayList<String>();
		for (int i = 1; i <= wpaWpDepth; i++) {
			editableCol.add(true);
			whatToShowCol.add(PropertyEnum.NAME);
			headers.add("Workpackage");
		}
		for (int i = 1; i <= wpaPDepth; i++) {
			editableCol.add(true);
			whatToShowCol.add(PropertyEnum.NAME);
			headers.add("Deployment");
		}
		editableCol.add(false);
		whatToShowCol.add(PropertyEnum.NAME);
		headers.add("Num of Units");
		editableCol.add(true);
		whatToShowCol.add(PropertyEnum.VALUE);
		headers.add("Mass per Unit (kg)");
		editableCol.add(true);
		whatToShowCol.add(PropertyEnum.VALUE);
		headers.add("Mass Contingency");
		editableCol.add(true);
		whatToShowCol.add(PropertyEnum.NAME);
		headers.add("Workpackage");
		
		wpaTable = new EditableTable("Workpackage Assembly Table of " + product.getName() + " for " + workpackage.getName(), wpatable, headers, null, null, precision);
		wpaTable.setWhatToShowCol(whatToShowCol);
		wpaTable.setEditableCol(editableCol);
		wpaTable.prepareTable();		
	}
	
	private void getWPATableModel(List<List<Object>> res, NamedElement workpackage, int curdepth) {
		List<NamedElement> wps = wpDeployment.get(workpackage);
		if (wps != null) {
			for(NamedElement wp: sortByName(wps)) {
				List<Object> wprow = new ArrayList<Object>();
				for (int i = 1; i < curdepth; i++)
					wprow.add("");
				wprow.add(wp);
				for (int i = curdepth + 1; i <= wpaWpDepth; i++)
					wprow.add("");
				for (int i = 1; i <= wpaPDepth; i++)
					wprow.add("");
				wprow.add(""); wprow.add(""); wprow.add(""); wprow.add(""); wprow.add("");
				res.add(wprow);
				for (Class top: wpTopProduct.get(wp)) {
					doWPATableForTop(res, top, 1, wp);
				}
				getWPATableModel(res, wp, curdepth+1);
			}
		}
	}
	
	private void doWPATableForTop(List<List<Object>> res, NamedElement cur, int curdepth, NamedElement curwp) {
		List<Object> row = new ArrayList<Object>();
		for (int i = 1; i <= wpaWpDepth; i++)
			row.add("");
		for (int i = 1; i < curdepth; i++)
			row.add("");
		row.add(cur);
		for (int i = curdepth+1; i <= wpaPDepth; i++)
			row.add("");
		row.add(totalUnits.get(cur).toString());
		row.add(mass.get(cur));
		row.add(massContingency.get(cur));

		NamedElement wp = p2wp.get(cur);
		if (wp == null)
			row.add(null);
		else if (wp != curwp)
			row.add(wp);
		else
			row.add("");
		res.add(row);
		List<NamedElement> children = deployment.get(cur);
		if (children != null) {
			for (NamedElement child: sortByName(children))
				doWPATableForTop(res, child, curdepth+1, curwp);
		}
	}
	
	private void getWPTopProduct(Class curProduct, List<Class> wpProducts, List<Class> tops, Set<Class> ignore, boolean first) {	
		List<NamedElement> children = deployment.get(curProduct);
		if (children != null) {
			for (NamedElement c: children) {
				Class next = (Class) c;
				
				if (wpProducts.contains(next)) {
					if (first || (!tops.contains(next) && !ignore.contains(next))) {
						tops.add(next);
						fillIgnore(ignore, next);
					}
					continue;
				}
				getWPTopProduct(next, wpProducts, tops, ignore, false);
			}
		} 
	}
	
	private void fillIgnore(Set<Class> ignore, Class c) {
		ignore.add(c);
		for (NamedElement nc: deployment.get(c))
			fillIgnore(ignore, (Class) nc);
	}

	public EditableTable getWPAEditableTable() {
		return wpaTable;
	}
}
