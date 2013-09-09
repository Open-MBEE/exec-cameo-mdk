package gov.nasa.jpl.mgss.mbee.docgen.table;

import gov.nasa.jpl.mbee.lib.ModelLib;
import gov.nasa.jpl.mbee.lib.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.AggregationKindEnum;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.TypedElement;

/**
 * ehm product deployment table
 * this should really be changed to be a userscript
 * @author dlam
 *
 */
public class Deployment {

	protected Class product;
	protected int precision;
	protected boolean includeInherited;
	protected boolean sortByName;
	protected boolean authorizesAsso;
	protected boolean suppliesAsso;

	protected Map<Class, Map<Class, Integer>> realUnits;
	protected int productDepth;
	protected Map<NamedElement, List<NamedElement>> deployment;
	protected Map<NamedElement, Property> mass;
	protected Map<NamedElement, Property> massContingency;
	protected Map<NamedElement, Property> cbeContingency;
	protected Map<NamedElement, Property> massAllocation;
	protected Map<NamedElement, Property> massMargin;
	protected Map<Class, NamedElement> p2wp;
	protected Map<Class, Integer> totalUnits;
	protected Map<NamedElement, NamedElement> wp2swp; //wp to specialized wp

	protected EditableTable deploymentTable;


	public Deployment(Class product, int precision, boolean sortByName, boolean suppliesAsso, boolean authorizesAsso, boolean includeInherited) {
		this.product = product;
		this.precision = precision;
		this.sortByName = sortByName;
		this.suppliesAsso = suppliesAsso;
		this.authorizesAsso = authorizesAsso;
		this.includeInherited = includeInherited;

		deployment = new HashMap<NamedElement, List<NamedElement>>();
		realUnits = new HashMap<Class, Map<Class, Integer>>();
		totalUnits = new HashMap<Class, Integer>();
		mass = new HashMap<NamedElement, Property>();
		massContingency = new HashMap<NamedElement, Property>();
		cbeContingency = new HashMap<NamedElement, Property>();
		massAllocation= new HashMap<NamedElement, Property>();
		massMargin = new HashMap<NamedElement, Property>();
		p2wp = new HashMap<Class, NamedElement>();
		wp2swp = new HashMap<NamedElement, NamedElement>();
	}

	public static int getDepthOfCompositionalMap(Map<NamedElement, List<NamedElement>> map, Class c) {
		return getDepthOfCompositionalMapRecursive(map, 1, c);
	}

	private static int getDepthOfCompositionalMapRecursive(Map<NamedElement, List<NamedElement>> map, int cur, NamedElement c) {
		int max = cur;
		for (NamedElement entry: map.get(c)) {
			int branch = getDepthOfCompositionalMapRecursive(map, cur+1, entry);
			if (branch > max)
				max = branch;
		}
		return max;
	}

	/**
	 * 
	 * @param product
	 * @param sortByName
	 * @return 
	 * 
	 */
	public void getDeploymentTable() {		
		productDepth = fillDeploymentTree(product, new HashSet<Class>(), 1);
		productDepth = getDepthOfCompositionalMap(deployment, product);
		for (NamedElement ne: deployment.get(product)){
			Class c = (Class) ne;
			fillTotalUnits(c, realUnits.get(product), 1);
		}
		fillMass(deployment.keySet());
		fillMassContingency(deployment.keySet());
		fillCBEContingency(deployment.keySet());
		fillWorkPackage(deployment.keySet());
		fillMassAllocation(deployment.keySet());
		fillMassMargin(deployment.keySet());
		if (includeInherited)
			fixInheritingWorkPackages();
		List<List<Object>> model = new ArrayList<List<Object>>();
		getDeploymentTableModel(product, realUnits.get(product), 1, model);

		List<String> headers = new ArrayList<String>();
		for (int i=1; i <= productDepth; i++)
			headers.add("Deployment");
		headers.add("Num of Units");
		headers.add("Mass per Unit (kg)");
		headers.add("Mass Contingency");
		headers.add("Mass CBE + Contingency (kg)");
		headers.add("Workpackage");
		deploymentTable = new EditableTable("Deployment Table of " + product.getName(), model, headers, null, null, precision);
		List<Boolean> editableCol = new ArrayList<Boolean>();
		for (int i=1; i <= productDepth; i++)
			editableCol.add(true);
		editableCol.add(false);
		editableCol.add(true);
		editableCol.add(true);
		editableCol.add(true);
		editableCol.add(true);
		List<PropertyEnum> whatToShowCol = new ArrayList<PropertyEnum>();
		for (int i=1; i <= productDepth; i++)
			whatToShowCol.add(PropertyEnum.NAME);
		whatToShowCol.add(PropertyEnum.NAME);
		whatToShowCol.add(PropertyEnum.VALUE);
		whatToShowCol.add(PropertyEnum.VALUE);
		whatToShowCol.add(PropertyEnum.VALUE);
		whatToShowCol.add(PropertyEnum.NAME);
		deploymentTable.setWhatToShowCol(whatToShowCol);
		deploymentTable.setEditableCol(editableCol);
		deploymentTable.prepareTable();
	}

	private void getDeploymentTableModel(Class curproduct, Map<Class, Integer> scopedUnits, int curdepth, List<List<Object>> res) {
		List<Object> row = new ArrayList<Object>();
		for (int i=1; i < curdepth; i++) {
			row.add("");
		}
		row.add(curproduct);
		for (int i=curdepth+1; i <= productDepth; i++) {
			row.add("");
		}
		Integer u = scopedUnits.get(curproduct);
		if (u == null)
			u = 1;
		if (curdepth == 1)
			row.add("");
		else	
			row.add(Integer.toString(u));
		row.add(mass.get(curproduct));
		row.add(massContingency.get(curproduct));
		row.add(cbeContingency.get(curproduct));
		row.add(p2wp.get(curproduct));
		res.add(row);

		List<NamedElement> children = deployment.get(curproduct);
		if(children != null) {
			if (sortByName)
				children = sortByName(children);
			else
				children = sortProductByWorkPackage(children, p2wp);
			for (NamedElement c: children) {
				getDeploymentTableModel((Class) c, realUnits.get(curproduct), curdepth+1, res);
			}
		}
	}

	protected void fillMass(Set<NamedElement> products) {
		for (NamedElement ne: products) {
			if (ne instanceof Class){
				Class c = (Class) ne;
				Property p = findMass(c, "Mass Current Best Estimate");

				if (p == null)
					p = findMassNewChar(c, "Mass Current Best Estimate");
				
				if (p != null)
					mass.put(c, p);
			} else {

			}
		}
	}

	protected void fillMassContingency(Set<NamedElement> products) {
		for (NamedElement ne: products) {
			if (ne instanceof Class){
				Class c = (Class) ne;
				Property p = findMass(c, "Mass Contingency");

				if (p == null)
					p = findMassNewChar(c, "Mass Contingency");
				
				if (p != null)
					massContingency.put(c, p);
			}
		}
	}

	protected void fillCBEContingency(Set<NamedElement> products) {
		for (NamedElement ne: products) {
			if (ne instanceof Class){
				Class c = (Class) ne;
				Property p = findMass(c, "Mass CBE_+_Contingency");
				
				if (p == null)
					p = findMassNewChar(c, "Mass CBE_+_Contingency");
				
				if (p != null)
					cbeContingency.put(c, p);
			}
		}
	}
	protected void fillMassAllocation(Set<NamedElement> products) {
		for (NamedElement ne: products){
			if (ne instanceof Class){
				Class c = (Class) ne;

				Property p = findMass(c, "Mass Allocation");
				
				if (p == null)
					p = findMassNewChar(c, "Mass Allocation");
				
				if (p != null)
					massAllocation.put(c, p);
			}
		}
	}
	protected void fillMassMargin(Set<NamedElement> products) {
		for (NamedElement ne: products){
			if (ne instanceof Class){

				Class c = (Class) ne;

				Property p = findMass(c, "Mass Margin");
				
				if (p == null)
					p = findMassNewChar(c, "Mass Margin");
				
				if (p != null)
					massMargin.put(c, p);
			}
		}
	}

	protected void fillWPMass(Set<NamedElement> wps) {
		for (NamedElement wp: wps) {
			Property p = null;
			if (ModelLib.isOriginalWorkPackage(wp)){
				p = findMass((Class) wp, "Mass Current Best Estimate");
			} else {
				p = findMassNewChar(wp, "Mass Current Best Estimate");

				if (p == null){
					List <Element> characterizes = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(wp, ModelLib.CHARACTERIZES, 2, true, 1);
					if (characterizes.size() > 1){
						throw new NullPointerException(); 
					}
					Element charact = characterizes.get(0);

					if (charact instanceof Class){
						p = findMass((Class) charact, "Mass Current Best Estimate");
					}
				}

			}
			if (p != null)
				mass.put(wp, p);
		}
	}

	protected void fillWPMassContingency(Set<NamedElement> wps) {
		for (NamedElement wp: wps) {
			Property p = null;
			if (ModelLib.isOriginalWorkPackage(wp)){
				p = findMass((Class) wp, "Mass Contingency");
			} else {
				p = findMassNewChar(wp, "Mass Contingency");

				if (p == null){

					List <Element> characterizes = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(wp, ModelLib.CHARACTERIZES, 2, true, 1);
					if (characterizes.size() > 1){
						throw new NullPointerException(); 
					}
					Element charact = characterizes.get(0);

					if (charact instanceof Class){
						p = findMass((Class) charact, "Mass Contingency");
					}
				}

			}
			if (p != null)
				massContingency.put(wp, p);
		}
	}

	protected void fillWPCBEContingency(Set<NamedElement> wps) {
		for (NamedElement wp: wps) {
			Property p = null;
			if (ModelLib.isOriginalWorkPackage(wp)){
				p = findMass((Class) wp, "Mass CBE_+_Contingency");
			} else {
				p = findMassNewChar(wp, "Mass CBE_+_Contingency");

				if (p == null){

					List <Element> characterizes = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(wp, ModelLib.CHARACTERIZES, 2, true, 1);
					if (characterizes.size() > 1){
						throw new NullPointerException(); 
					}
					Element charact = characterizes.get(0);

					if (charact instanceof Class){
						p = findMass((Class) charact, "Mass CBE_+_Contingency");
					}
				}


			}
			if (p != null)
				cbeContingency.put(wp, p);
		}

	}
	protected void fillWPMassAllocation(Set<NamedElement> wps) {
		for (NamedElement wp: wps) {
			Property p = null;
			if (ModelLib.isOriginalWorkPackage(wp)){
				p = findMass((Class) wp, "Mass Allocation");
			} else {
				p = findMassNewChar(wp, "Mass Allocation");

				if (p == null){
					List <Element> characterizes = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(wp, ModelLib.CHARACTERIZES, 2, true, 1);
					if (characterizes.size() > 1){
						throw new NullPointerException(); 
					}
					Element charact = characterizes.get(0);
					if (charact instanceof Class){
						p = findMass((Class) charact, "Mass Allocation");
					}
				}
			}
			if (p != null)
				massAllocation.put(wp, p);
		}

	}
	protected void fillWPMassMargin(Set<NamedElement> wps) {
		for (NamedElement wp: wps) {
			Property p = null;
			if (ModelLib.isOriginalWorkPackage(wp)){
				p = findMass((Class) wp, "Mass Margin");
			} else {
				p = findMassNewChar(wp, "Mass Margin");

				if (p == null){

					List <Element> characterizes = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(wp, ModelLib.CHARACTERIZES, 2, true, 1);
					if (characterizes.size() > 1){
						throw new NullPointerException(); 
					}
					Element charact = characterizes.get(0);
					if (charact instanceof Dependency){
						Element client = ((Dependency) charact).getClient().iterator().next();
						if (client instanceof Class){
							p = findMass((Class) client, "Mass Margin");
						}
					}
				}

			}
			if (p != null)
				massMargin.put(wp, p);
		}
	}


	public static Property findMassNewChar(NamedElement c, String prop){
		for (DirectedRelationship dr : c.get_directedRelationshipOfTarget()){
			for (Element element : dr.getSource()){
				if (ModelLib.isMassCharacterization(element)){
					for (Property p3: ((Class) element).getOwnedAttribute()) {
						if (p3.getName().equals(prop))
							return p3;
					}
				}
			}
		}

		return null;
	}

	public static Property findMass(Class c, String prop) {
		for (Property p: c.getOwnedAttribute()) {
			Type ptype = p.getType();
			if (ptype != null && StereotypesHelper.hasStereotypeOrDerived(ptype, "Mass Durative Event")) {
				for (Property p2: ((Class)ptype).getOwnedAttribute()) {
					Type ptype2 = p2.getType();
					if (ptype2 != null && StereotypesHelper.hasStereotypeOrDerived(ptype2, "Mass State Prototype")) {
						for (Property p3: ((Class)ptype2).getOwnedAttribute()) {
							if (p3.getName().equals(prop))
								return p3;
						}
					}
				}
			}

		}
		return null;	
	}

	private void fillWorkPackage(Set<NamedElement> products) {
		if (!suppliesAsso) {
			for (NamedElement c: products) {	
				for (DirectedRelationship dr: c.get_directedRelationshipOfTarget()) {
					Element e = ModelHelper.getClientElement(dr);
					if (ModelLib.isWorkPackage(e) && ModelLib.isSupplies(dr))
						p2wp.put((Class) c, (NamedElement)e);
				}
			}
		} else {
			for (NamedElement c: products) {
				for (TypedElement a: ((Class) c).get_typedElementOfType()) {
					if (a instanceof Property && a.getOwner() instanceof Class && ModelLib.isWorkPackage(a.getOwner()))
						p2wp.put((Class)c, (NamedElement)a.getOwner());
				}
			}
		}
	}

	private void fixInheritingWorkPackages() {
		Collection<NamedElement> wps = p2wp.values();
		for (NamedElement wp: wps) {
			for (Element e: Utils.collectDirectedRelatedElementsByRelationshipJavaClass(wp, Generalization.class, 2, 1)) {
				if (wps.contains(e)) {
					wp2swp.put(wp, (Class)e);
				}
			}
		}

		for (Class p: p2wp.keySet()) {
			NamedElement wp = p2wp.get(p);
			if (wp2swp.containsKey(wp)) {
				p2wp.put(p, wp2swp.get(wp));
			}
		}
	}

	private int fillDeploymentTree(Class cur, Set<Class> done, int curdepth) {
		int maxdepth = curdepth;
		if (!deployment.containsKey(cur)) {
			deployment.put(cur, new ArrayList<NamedElement>());
			done.add(cur);
		}
		if (!realUnits.containsKey(cur)) {
			realUnits.put(cur, new HashMap<Class, Integer>());
		}
		Map<Class, Integer> scopedunit = realUnits.get(cur);
		List<NamedElement> children = deployment.get(cur);
		List<Property> iterate = new ArrayList<Property>(cur.getOwnedAttribute());
		if (includeInherited)
			iterate.addAll(PropertiesTable.getInheritedProperties(cur));
		for (Property p: iterate) {
			if (Utils.getMultiplicity(p) == 0)
				continue;
			Type ptype = p.getType();
			if (ptype != null && ModelLib.isProduct(ptype) && p.getAggregation() == AggregationKindEnum.COMPOSITE) {
				int mul = PropertiesTable.getMultiplicity(p);
				if (!scopedunit.containsKey(ptype))
					scopedunit.put((Class)ptype, mul);
				else
					scopedunit.put((Class)ptype, scopedunit.get(ptype) + mul);
				if (!children.contains(ptype))
					children.add((Class)ptype);
				if (!done.contains(ptype)) {
					int tempdepth = fillDeploymentTree((Class)ptype, done, curdepth + 1);
					if (tempdepth > maxdepth)
						maxdepth = tempdepth;
				}
			}
		}
		return maxdepth;
	}

	public static List<NamedElement> sortProductByWorkPackage(List<NamedElement> products, final Map<Class, NamedElement> product2package) {
		List<NamedElement> res = new ArrayList<NamedElement>(products);
		Collections.sort(res, new Comparator<NamedElement>() {
			public int compare(NamedElement arg0, NamedElement arg1) {
				NamedElement arg0wp = product2package.get((Class) arg0);
				NamedElement arg1wp = product2package.get((Class) arg1);
				if (arg0wp == null || arg1wp == null)
					return arg0.getName().compareTo(arg1.getName());
				return arg0wp.getName().compareTo(arg1wp.getName());
			}
		});
		return res;
	}

	public static List<NamedElement> sortByName(List<NamedElement> products) {
		List<NamedElement> res = new ArrayList<NamedElement>(products);
		Collections.sort(res, new Comparator<NamedElement>() {
			public int compare(NamedElement arg0, NamedElement arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}
		});
		return res;
	}

	public static List<Class> sortClassByName(List<Class> products) {
		List<Class> res = new ArrayList<Class>(products);
		Collections.sort(res, new Comparator<Class>() {
			public int compare(Class arg0, Class arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}
		});
		return res;
	}

	private void fillTotalUnits(Class cur, Map<Class, Integer> scopedUnits, int multiplier) {
		Integer scoped = scopedUnits.get(cur);
		int total = scoped*multiplier;
		if (!totalUnits.containsKey(cur))
			totalUnits.put(cur, total);
		else
			totalUnits.put(cur, total + totalUnits.get(cur));
		for (NamedElement ne: deployment.get(cur)){
			Class c = (Class) ne;
			fillTotalUnits(c, realUnits.get(cur), total);
		}

	}

	public int getPrecision() {
		return precision;
	}

	public Map<Class, Map<Class, Integer>> getRealUnits() {
		return realUnits;
	}

	public int getProductDepth() {
		return productDepth;
	}

	public Map<NamedElement, List<NamedElement>> getDeployment() {
		return deployment;
	}

	public Map<NamedElement, Property> getMass() {
		return mass;
	}

	public Map<NamedElement, Property> getMassContingency() {
		return massContingency;
	}

	public Map<NamedElement, Property> getCbeContingency() {
		return cbeContingency;
	}

	public Map<NamedElement, Property> getMassAllocation(){
		return massAllocation;
	}
	public Map<NamedElement, Property> getMassMargin(){
		return massMargin;
	}
	public Map<Class, NamedElement> getP2wp() {
		return p2wp;
	}

	public Map<Class, Integer> getTotalUnits() {
		return totalUnits;
	}

	public EditableTable getDeploymentEditableTable() {
		return deploymentTable;
	}
}
