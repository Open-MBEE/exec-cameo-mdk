package gov.nasa.jpl.mgss.mbee.docgen.table;

import gov.nasa.jpl.mbee.lib.ModelLib;
import gov.nasa.jpl.mbee.lib.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;

/**
 * the bom class that actually does all the heavy lifting
 * it's extending deployment because everything deployment tables analysis bom needs it also
 * this should be a userscript also
 * @author dlam
 *
 */
public class BillOfMaterials extends Deployment {

	protected NamedElement workpackage;
	protected Map<NamedElement, List<NamedElement>> wpDeployment;
	protected int wpDepth;
	protected Map<NamedElement, List<Class>> wp2p;
	protected Map<NamedElement, NamedElement> wp2pwp;
	private boolean showProducts;
	private boolean showMassMargin;
	protected EditableTable bomTable;
	protected Boolean authorizes;

	public BillOfMaterials(Class product, NamedElement workpackage, int precision, boolean sortByName, boolean suppliesAsso, boolean authorizesAsso, boolean includeInherited, boolean showProducts, boolean showMassMargin) {
		super(product, precision, sortByName, suppliesAsso, authorizesAsso, includeInherited);
		this.workpackage = workpackage;
		this.showProducts = showProducts;
		this.showMassMargin=showMassMargin;
		wpDeployment = new HashMap<NamedElement, List<NamedElement>>();
		wp2p = new HashMap<NamedElement, List<Class>>();
		wp2pwp = new HashMap<NamedElement, NamedElement>();
	}

	/**
	 * 
	 * @param product
	 * @param workpackage
	 * @param sortByName
	 * @return 
	 */
	public void getBOMTable() {
		getDeploymentTable();
		Set<NamedElement> products = new HashSet<NamedElement>(deployment.keySet());
		//products.remove(product);
		wpDepth = getAuthorizedWorkPackages(workpackage, new HashSet<NamedElement>(), 1);
		getWorkPackageProductMapping(products, wpDeployment.keySet());
		removeEmptyWorkpackagesFromTree();
		List<List<Object>> table = new ArrayList<List<Object>>();
		fillWPMass(wpDeployment.keySet());
		fillWPMassContingency(wpDeployment.keySet());
		fillWPCBEContingency(wpDeployment.keySet());
		getBOMTableModel(workpackage, 1, table);

		List<String> headers = new ArrayList<String>();
		for (int i=1; i <= wpDepth; i++)
			headers.add("Workpackage");
		if (showProducts) {
			headers.add("Deployments");
			headers.add("Num of Units");
		}
		headers.add("Mass per Unit (kg)");
		headers.add("Mass Contingency");
		headers.add("Mass CBE + Contingency (kg)");
		if(showMassMargin){
			headers.add("Allocation");
			headers.add("JPL Margin");
		}
		bomTable = new EditableTable("Bill of Materials Table of " + product.getName() + " for " + workpackage.getName(), table, headers, null, null, precision);
		List<Boolean> editableCol = new ArrayList<Boolean>();
		for (int i=1; i <= wpDepth; i++)
			editableCol.add(true);
		if (showProducts) {
			editableCol.add(true);
			editableCol.add(false);
		}
		editableCol.add(true);
		editableCol.add(true);
		editableCol.add(true);
		if(showMassMargin){
			editableCol.add(true);
			editableCol.add(true);
		}

		List<PropertyEnum> whatToShowCol = new ArrayList<PropertyEnum>();
		for (int i=1; i <= wpDepth; i++)
			whatToShowCol.add(PropertyEnum.NAME);
		if (showProducts) {
			whatToShowCol.add(PropertyEnum.NAME);
			whatToShowCol.add(PropertyEnum.NAME);
		}
		whatToShowCol.add(PropertyEnum.VALUE);
		whatToShowCol.add(PropertyEnum.VALUE);
		whatToShowCol.add(PropertyEnum.VALUE);
		if(showMassMargin){
			whatToShowCol.add(PropertyEnum.VALUE);
			whatToShowCol.add(PropertyEnum.VALUE);
		}
		bomTable.setWhatToShowCol(whatToShowCol);
		bomTable.setEditableCol(editableCol);
		bomTable.prepareTable();
	}

	private void getBOMTableModel(NamedElement workpackage, int curdepth, List<List<Object>> table) {
		List<Object> row = new ArrayList<Object>();
		for (int i=1; i < curdepth; i++)
			row.add("");
		row.add(workpackage);
		for (int i=curdepth+1; i <= wpDepth; i++)
			row.add("");
		if (showProducts) {
			row.add("");
			row.add("");
		}
		row.add(mass.get(workpackage));
		row.add(massContingency.get(workpackage));
		row.add(cbeContingency.get(workpackage));
		if (showMassMargin){
			row.add("");
			row.add("");
			//row.add(massAllocation.get(workpackage));
			//row.add(massMargin.get(workpackage));
		}

		table.add(row);

		if (showProducts) {

			List<Class> supplies = wp2p.get(workpackage);
			for (Class product: sortClassByName(supplies)) {
				if (deployment.containsKey(product) && !deployment.get(product).isEmpty()) //skip non-leaf nodes
					continue;
				List<Object> row2 = new ArrayList<Object>();
				for (int i = 1; i <= wpDepth; i++)
					row2.add("");
				row2.add(product);
				Integer unit = totalUnits.get(product);
				if (unit != null)
					row2.add(Integer.toString(unit));
				else
					row2.add("");
				row2.add(mass.get(product));
				row2.add(massContingency.get(product));
				row2.add(cbeContingency.get(product));
				if(showMassMargin){
					row2.add(massAllocation.get(product));
					row2.add(massMargin.get(product));
				}
				table.add(row2);
			}
		}
		List<NamedElement> children = wpDeployment.get(workpackage);
		if(children != null) {
			for (NamedElement c: sortByName(children)) {
				getBOMTableModel(c, curdepth+1, table);
			}
		}
	}

	private int getAuthorizedWorkPackages(NamedElement cur, Set<NamedElement> done, int curdepth) {		int maxdepth = curdepth;
		if (!wpDeployment.containsKey(cur)) {
			wpDeployment.put(cur, new ArrayList<NamedElement>());
			done.add(cur);
		}
		List<NamedElement> children = wpDeployment.get(cur);
		if (!authorizesAsso) {
			if (authorizes == null || authorizes) {
			for (Element target: Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(cur, "authorizes", 1, true, 1)) {
				if (ModelLib.isWorkPackage(target)) {
					authorizes = true;
					if (!children.contains(target)) {
						children.add((NamedElement)target);
						wp2pwp.put((NamedElement)target, cur);
					}
					if (!done.contains((NamedElement)target)) {
						int tempdepth = getAuthorizedWorkPackages((NamedElement)target, done, curdepth + 1);
						if (tempdepth > maxdepth)
							maxdepth = tempdepth;
					}
				}
			}
			} 
			
			if (authorizes == null || !authorizes) {
			for (Element target : cur.getOwnedElement()){
				if (ModelLib.isWorkPackage(target)) {
					authorizes = false;
					if (!children.contains(target)) {
						children.add((NamedElement)target);
						wp2pwp.put((NamedElement)target, cur);
					}
					if (!done.contains((NamedElement)target)) {
						int tempdepth = getAuthorizedWorkPackages((NamedElement)target, done, curdepth + 1);
						if (tempdepth > maxdepth)
							maxdepth = tempdepth;
					}
				}
			}
			}
			if (includeInherited) {
				for (Element parent: Utils.collectDirectedRelatedElementsByRelationshipJavaClass(cur, Generalization.class, 1, 1)) {
					if (StereotypesHelper.hasStereotypeOrDerived(parent, "Work Package")) {
						for (Element target: Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(parent, "authorizes", 1, true, 1)) {
							if (StereotypesHelper.hasStereotypeOrDerived(target, "Work Package")) {
								boolean look = true;
								for (Element specialized: Utils.collectDirectedRelatedElementsByRelationshipJavaClass(target, Generalization.class, 2, 1)) {
									if (children.contains(specialized)) {
										look = false;
									}
								}
								if (!look)
									continue;
								if (!children.contains(target)) {
									children.add((NamedElement)target);
									wp2pwp.put((NamedElement)target, cur);
								}
								if (!done.contains((Class)target)) {
									int tempdepth = getAuthorizedWorkPackages((NamedElement)target, done, curdepth + 1);
									if (tempdepth > maxdepth)
										maxdepth = tempdepth;
								}
							}
						}
					}
				}
			}
		} else {
			for (Element p: cur.getOwnedElement()){
				if (p instanceof Property){

					Type t = ((Property) p).getType();
					if (t != null && StereotypesHelper.hasStereotypeOrDerived(t, "Work Package") && t instanceof Class) {
						if (!children.contains((Class)t))
							children.add((Class)t);
						if (!done.contains((Class)t)) {
							int tempdepth = getAuthorizedWorkPackages((Class)t, done, curdepth + 1);
							if (tempdepth > maxdepth)
								maxdepth = tempdepth;
						}
					}
				}

			}
		}
		return maxdepth;
	}

	private void getWorkPackageProductMapping(Set<NamedElement> products, Set<NamedElement> wps) {
		/*if (!suppliesAsso) {
			for (Class wp: wps) {
				List<Class> wpProducts = new ArrayList<Class>();
				for (DirectedRelationship dr: wp.get_directedRelationshipOfSource()) {
					Element target = ModelHelper.getSupplierElement(dr);
					if (products.contains(target) && StereotypesHelper.hasStereotypeOrDerived(dr, "supplies"))
						if (!wpProducts.contains(target))
							wpProducts.add((Class)target);
				}
				wp2p.put(wp, wpProducts);
			}
		} else {
			for (Class wp: wps) {
				List<Class> wpProducts = new ArrayList<Class>();
				for (Property p: wp.getOwnedAttribute()){
					Type t = p.getType();
					if (t != null && StereotypesHelper.hasStereotypeOrDerived(t, "Product") && t instanceof Class) {
						wpProducts.add((Class)t);
					}
				}
				wp2p.put(wp, wpProducts);
			}
		}*/
		for (NamedElement product: products) {
			NamedElement wp = p2wp.get(product);
			if (wp == null)
				continue;
			/*	//kludge for allowing multiple supplies relationship to one product from different workpackage trees
			if (!wps.contains(wp)) {
				for (Element pwp: Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(product, "supplies", 2, false, 1)) {
					if (wps.contains(pwp)) {
						wp = (Class)pwp;
						p2wp.put(product, wp);
						break;
					}
				}
			}*/
			if (includeInherited) {
				List<Element> special = Utils.collectDirectedRelatedElementsByRelationshipJavaClass(wp, Generalization.class, 2, 1);
				for (Element e: special) {
					if (wps.contains(e)) {
						wp = (Class)e;
						p2wp.put((Class) product, wp);
						break;
					}
				}
			}
			List<Class> wpProducts = null;
			if (wp2p.containsKey(wp))
				wpProducts = wp2p.get(wp);
			else {
				wpProducts = new ArrayList<Class>();
				wp2p.put(wp, wpProducts);
			}
			wpProducts.add((Class) product);
		}
		for (NamedElement wp: wps) {
			if (!wp2p.containsKey(wp))
				wp2p.put(wp, new ArrayList<Class>());
		}
	}

	private void removeEmptyWorkpackagesFromTree() {
		for (NamedElement w: new HashSet<NamedElement>(wpDeployment.keySet())) {
			if (wpDeployment.get(w).isEmpty() && wp2p.get(w).isEmpty()) {
				NamedElement pwp = wp2pwp.get(w);
				if (pwp != null && wpDeployment.containsKey(pwp)) {
					wpDeployment.get(pwp).remove(w);
				}
				wpDeployment.remove(w);
				wp2p.remove(w);
			}
		}
	}

	public Map<NamedElement, List<NamedElement>> getWpDeployment() {
		return wpDeployment;
	}

	public int getWpDepth() {
		return wpDepth;
	}

	public Map<NamedElement, List<Class>> getWp2p() {
		return wp2p;
	}

	public EditableTable getBOMEditableTable() {
		return bomTable;
	}
}
