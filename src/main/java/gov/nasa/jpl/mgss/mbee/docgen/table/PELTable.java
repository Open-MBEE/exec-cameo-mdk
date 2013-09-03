package gov.nasa.jpl.mgss.mbee.docgen.table;

import gov.nasa.jpl.mbee.lib.ModelLib;
import gov.nasa.jpl.mbee.lib.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nomagic.magicdraw.core.GUILog;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.AggregationKindEnum;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.TypedElement;
import com.nomagic.uml2.ext.magicdraw.interactions.mdbasicinteractions.Interaction;

/**
 * <p>Helper class for EHM PELTable
 * given a top level power load product and workpackage, fills in the product hierarchy, workpackage hierarhcy, 
 * mappings from workpackage to products, workpackage to leaf products, product to power load characterizations, 
 * and product to workpackage
 * </p>
 * <p>
 * Currently the stereotypes it looks for are hardcoded:
 * <ul>
 * <li>The Product hierarchy will only look for and include "Power Load Product"</li>
 * <li>Workpackages have to be subclasses of "Work Package"</li>
 * <li>Workpackages hierarhcy follows the "authorizes" relationship</li>
 * <li>Workpackage to product follows the "supplies" relationship</li>
 * <li>Power Load Characterizations are classes. They're found by getting the types of product attributes that have "Power Load Characterization" stereotype</li>
 * </ul>
 * </p>
 * 
 * @author dlam
 *
 */
public class PELTable {

	protected Class product;
	protected NamedElement workpackage;
	protected boolean authorizesAsso;
	protected boolean suppliesAsso;
	
	protected int productDepth; 				//depth of product tree, root is depth 1
	protected int wpDepth; 						//depth of workpackage tree, root is depth 1
	protected Map<Class, List<Class>> deployment; //product hierarchy
	protected Map<NamedElement, List<NamedElement>> wpDeployment; //workpackage hierarchy
	protected Map<Class, NamedElement> p2wp;			//product to workpackage, each product should map to one workpackage
	protected Map<NamedElement, List<Class>> wp2p;  //workpackage to products
	protected Map<NamedElement, List<Class>> wp2lp; //workpackage to leaf power products (leaf means ones that don't have any power children)
	protected Map<Class, List<Class>> p2plc; //product to power load characterizations
	protected Map<NamedElement, List<Class>> wp2plc; //workpackage to power load characterizations
	protected Map<NamedElement, NamedElement> wp2pwp; //workpackage to parent workpackage
	protected Map<NamedElement, NamedElement> wp2swp;
	
	//for mode tables
	protected Map<Class, List<Property>> p2p; //product to parts with product as type
	protected Map<Property, Map<Property, Integer>> scopedUnits;
	protected Map<Class, Map<String, Interaction>> p2i; //product to interactions for each mode
	protected List<String> modes;
	protected List<Interaction> importedModes;
	
	protected Map<Class, List<Property>> propcache;
	
	protected Map<Property, Integer> totalUnits;

	protected boolean includeInherited;
	protected GUILog log;
	/** 
	 * @param product
	 * @param workpackage
	 * @param suppliesAsso if supplies is association
	 * @param authorizesAsso if authorizes is association
	 */
	public PELTable(Class product, NamedElement workpackage, List<Interaction> imodes, boolean suppliesAsso, boolean authorizesAsso, boolean includeInherited) {
		init(product, workpackage, imodes, suppliesAsso, authorizesAsso, includeInherited);
	}
	
	public PELTable(Class product, NamedElement workpackage, boolean suppliesAsso, boolean authorizesAsso, boolean includeInherited) {
		init(product, workpackage, new ArrayList<Interaction>(), suppliesAsso, authorizesAsso, includeInherited);

	}
	
	private void init(Class product, NamedElement workpackage, List<Interaction> imodes, boolean suppliesAsso, boolean authorizesAsso, boolean includeInherited) {
		this.product = product;
		this.suppliesAsso = suppliesAsso;
		this.authorizesAsso = authorizesAsso;
		this.workpackage = workpackage;
		
		wpDeployment = new HashMap<NamedElement, List<NamedElement>>();
		deployment = new HashMap<Class, List<Class>>();
		
		p2wp = new HashMap<Class, NamedElement>();
		wp2p = new HashMap<NamedElement, List<Class>>();
		wp2lp = new HashMap<NamedElement, List<Class>>();
		p2plc = new HashMap<Class, List<Class>>();
		wp2plc = new HashMap<NamedElement, List<Class>>();
		wp2swp = new HashMap<NamedElement, NamedElement>();
		wp2pwp = new HashMap<NamedElement, NamedElement>();
		p2p = new HashMap<Class, List<Property>>();
		p2i = new HashMap<Class, Map<String, Interaction>>();
		modes = new ArrayList<String>();
		totalUnits = new HashMap<Property, Integer>();
		scopedUnits = new HashMap<Property, Map<Property, Integer>>();
		importedModes = imodes;
		this.includeInherited = includeInherited;
		propcache = new HashMap<Class, List<Property>>();
		log = Application.getInstance().getGUILog();
	}
	
	/**
	 * calculate and fill out bunch of stuff, call this before any get*** calls
	 */
	public void fillGraphs() {		
		productDepth = fillDeploymentTree(product, null, new HashSet<Class>(), 1);
		for (Class c: deployment.get(product)) {
			for (Property p: p2p.get(c)) {
				fillTotalUnits(p, null, 1);
			}
		}
		fillWorkPackage(deployment.keySet());
		//if (includeInherited)
		//	fixInheritingWorkPackages();
		wpDepth = getAuthorizedWorkPackages(workpackage, new HashSet<NamedElement>(), 1);
		getWorkPackageProductMapping(deployment.keySet(), wpDeployment.keySet());
		removeEmptyWorkpackagesFromTree();
		fillWp2lp();
		fillP2plc();
		fillWp2plc();
		fillP2i();
		if (!importedModes.isEmpty()) {
			List<String> realmodes = new ArrayList<String>();
			for (Interaction ia: importedModes) {
				if (modes.contains(ia.getName()))
					realmodes.add(ia.getName());
			}
			modes = realmodes;
		}
	}
	
	  
	private int fillDeploymentTree(Class cur, Property p, Set<Class> done, int curdepth) {
		int maxdepth = curdepth;
		if (!deployment.containsKey(cur)) {
			deployment.put(cur, new ArrayList<Class>());
			done.add(cur);
		}
		if (!p2p.containsKey(cur))
			p2p.put((Class)cur, new ArrayList<Property>());
		
		List<Class> children = deployment.get(cur);
		List<Property> iterate = new ArrayList<Property>(cur.getOwnedAttribute());
    	if (includeInherited)
    		iterate.addAll(PropertiesTable.getInheritedProperties(cur));
    	Map<Property, Integer> props = null;
    	if (p != null) {
    		if (!scopedUnits.containsKey(p))
    			 scopedUnits.put(p, new HashMap<Property, Integer>());
    		props = scopedUnits.get(p);
    	} else {
    		props = this.totalUnits;
    	}
    	List<Property> iterate2 = new ArrayList<Property>();
		for (Property prop: iterate) {
			if (Utils.getMultiplicity(prop)  == 0)
				continue;
			Type ptype = prop.getType();
			//this is looking for products instead of power load products so running this on inherited workpackages won't screw up if the redefined work packages doesn't supply any powered products
			if (ptype != null && ModelLib.isProduct(ptype) && prop.getAggregation() == AggregationKindEnum.COMPOSITE) {
				iterate2.add(prop);
				//log.log("mul of " + prop.getName() + " is " + Utils.getMultiplicity(prop));
				props.put(prop, Utils.getMultiplicity(prop));
				if (!children.contains(ptype))
					children.add((Class)ptype);
				if (!p2p.containsKey(ptype))
					p2p.put((Class)ptype, new ArrayList<Property>());
				List<Property> parts = p2p.get(ptype);
				if (!parts.contains(prop))
					parts.add(prop);
				if (!done.contains(ptype)) {
					int tempdepth = fillDeploymentTree((Class)ptype, prop, done, curdepth + 1);
					if (tempdepth > maxdepth)
						maxdepth = tempdepth;
				} else {
					Map<Property, Integer> cprops = null;
					if (!scopedUnits.containsKey(prop))
						scopedUnits.put(prop, new HashMap<Property, Integer>());
					cprops = scopedUnits.get(prop);
					for (Property cp: propcache.get(ptype)) {
						cprops.put(cp, Utils.getMultiplicity(cp));
						//log.log("mul of " + cp.getName() + " is " + Utils.getMultiplicity(cp));
					}
				}
			}
		}
		propcache.put(cur, iterate2);
		return maxdepth;
	}
	
	private void fillTotalUnits(Property p, Map<Property, Integer> realUnits, int multiplier) {
		int total = 0;
		if (realUnits != null) {
			Integer scoped = realUnits.get(p);
			total = scoped*multiplier;
			if (!totalUnits.containsKey(p))
				totalUnits.put(p, total);
			else
				totalUnits.put(p, total + totalUnits.get(p));
		} else {
			total = totalUnits.get(p);
		}
		for (Property c: scopedUnits.get(p).keySet())
			fillTotalUnits(c, scopedUnits.get(p), total);
	}
	
	private void fillWorkPackage(Set<Class> products) {
		if (!suppliesAsso) {
			for (Class c: products) {	
				for (DirectedRelationship dr: c.get_directedRelationshipOfTarget()) {
					Element e = ModelHelper.getClientElement(dr);
					if (ModelLib.isWorkPackage(e)&& ModelLib.isSupplies(dr))
						p2wp.put(c, (NamedElement)e);
				}
			}
		} else {
			for (Class c: products) {
				for (TypedElement a: c.get_typedElementOfType()) {
					if (a instanceof Property && a.getOwner() instanceof Class && ModelLib.isWorkPackage(a.getOwner()))
						p2wp.put(c, (NamedElement)a.getOwner());
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
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
	
	private int getAuthorizedWorkPackages(NamedElement cur, Set<NamedElement> done, int curdepth) {
		int maxdepth = curdepth;
		if (!wpDeployment.containsKey(cur)) {
			wpDeployment.put(cur, new ArrayList<NamedElement>());
			done.add(cur);
		}
		List<NamedElement> children = wpDeployment.get(cur);
		if (!authorizesAsso) {
			for (Element target: Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(cur, "authorizes", 1, true, 1)) {
				if (ModelLib.isWorkPackage(target)) {
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
			for (Element target : cur.getOwnedElement()){
				if (ModelLib.isWorkPackage(target)) {
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
			
			if (includeInherited) {
			for (Element parent: Utils.collectDirectedRelatedElementsByRelationshipJavaClass(cur, Generalization.class, 1, 1)) {
				if (StereotypesHelper.hasStereotypeOrDerived(parent, "Work Package")) {
					for (Element target: Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(parent, "authorizes", 1, true, 1)) {
						if (StereotypesHelper.hasStereotypeOrDerived(target, "Work Package")) {
							boolean look = true;
							for (Element specialized: Utils.collectDirectedRelatedElementsByRelationshipJavaClass(target, Generalization.class, 2, 1)) {
								if (children.contains(specialized))
									look = false;
							}
							if (!look)
								continue;
							if (!children.contains(target)) {
								children.add((Class)target);
								wp2pwp.put((Class)target, cur);
							}
							if (!done.contains((Class)target)) {
								int tempdepth = getAuthorizedWorkPackages((Class)target, done, curdepth + 1);
								if (tempdepth > maxdepth)
									maxdepth = tempdepth;
							}
						}
					}
				}
			}
			}
		} else if (ModelLib.isOriginalWorkPackage(cur)){
			for (Property p: ((Class) cur).getOwnedAttribute()){
				Type t = p.getType();
				if (t != null && ModelLib.isOriginalWorkPackage(t)) {
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
		return maxdepth;
	}
	
	private void getWorkPackageProductMapping(Set<Class> products, Set<NamedElement> wps) {
		/*if (!suppliesAsso) {
			for (Class wp: wps) {
				List<Class> wpProducts = new ArrayList<Class>();
				for (DirectedRelationship dr: wp.get_directedRelationshipOfSource()) {
					Element target = ModelHelper.getSupplierElement(dr);
					if (products.contains(target) && StereotypesHelper.hasStereotypeOrDerived(dr, "supplies"))
						wpProducts.add((Class)target);
				}
				wp2p.put(wp, wpProducts);
			}
		} else {
			for (Class wp: wps) {
				List<Class> wpProducts = new ArrayList<Class>();
				for (Property p: wp.getOwnedAttribute()){
					Type t = p.getType();
					if (t != null && StereotypesHelper.hasStereotypeOrDerived(t, "Power Load Product") && t instanceof Class) {
						wpProducts.add((Class)t);
					}
				}
				wp2p.put(wp, wpProducts);
			}
		}*/
		for (Class product: products) {
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
						wp = (NamedElement)e;
						p2wp.put(product, wp);
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
			wpProducts.add(product);
			//Application.getInstance().getGUILog().log("adding " + product.getName() + " to supplied by " + wp.getName());
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
				//Application.getInstance().getGUILog().log("removing " + w.getName());
			}
		}
	}
	
	private boolean isLeafPoweredHardwareProduct(Class p) {
		if (!ModelLib.isPLProduct(p))
			return false;
		List<Class> children = deployment.get(p);
		if (children == null || children.isEmpty()) {
			return true;
		}
		for (Class c: children) {
			if (ModelLib.isPLProduct(c))
				return false;
		}
		return true;
	}
	
	private void fillWp2lp() {
		for (NamedElement wp: wp2p.keySet()) {
			List<Class> ps = new ArrayList<Class>();
			for (Class p: wp2p.get(wp)) {
				if (isLeafPoweredHardwareProduct(p))
					ps.add(p);
			}
			wp2lp.put(wp, ps);
		}
	}
	
	private void fillP2plc() {
		for (Class p: deployment.keySet()) {
			List<Class> cs = new ArrayList<Class>();
			for (Property prop: p.getOwnedAttribute()) {
				Type t = prop.getType();
				if (t != null && StereotypesHelper.hasStereotypeOrDerived(t, "Power Load Characterization")) {
					for (Element e: Utils.collectDirectedRelatedElementsByRelationshipJavaClass(t, Generalization.class, 2, 1)) {
						if (e instanceof Class && StereotypesHelper.hasStereotypeOrDerived(e, "Power Load Characterization")) {
							cs.add((Class)e);
						}
					}
				}
			}
			p2plc.put(p, cs);
		}
	}
	
	private void fillWp2plc() {
		for (NamedElement wp: wpDeployment.keySet()) {
			List<Class> cs = new ArrayList<Class>();

			if (ModelLib.isOriginalWorkPackage(wp)){
				for (Property prop: ((Class)wp).getOwnedAttribute()) {
					Type t = prop.getType();
					if (t != null && StereotypesHelper.hasStereotypeOrDerived(t, "Power Load Characterization")) {
						for (Element e: Utils.collectDirectedRelatedElementsByRelationshipJavaClass(t, Generalization.class, 2, 1)) {
							if (e instanceof Class && StereotypesHelper.hasStereotypeOrDerived(e, "Power Load Characterization")) {
								cs.add((Class)e);
							}
						}
					}
				}
			} else {
				List <Element> characterizes = Utils.collectDirectedRelatedElementsByRelationshipStereotypeString(wp, ModelLib.CHARACTERIZES, 2, true, 1);
				if (characterizes.size() > 1){
					throw new NullPointerException(); 
				}
				Element charact = characterizes.get(0);
				if (charact instanceof Class){
					for (Property prop: ((Class)charact).getOwnedAttribute()) {
						Type t = prop.getType();
						if (t != null && StereotypesHelper.hasStereotypeOrDerived(t, "Power Load Characterization")) {
							for (Element e: Utils.collectDirectedRelatedElementsByRelationshipJavaClass(t, Generalization.class, 2, 1)) {
								if (e instanceof Class && StereotypesHelper.hasStereotypeOrDerived(e, "Power Load Characterization")) {
									cs.add((Class)e);
								}
							}
						}
					}				
				}
		}
			
			wp2plc.put(wp, cs);
		}
	}
	
	private void fillP2i() {
		for (Class p: deployment.keySet()) {
			Map<String, Interaction> is = new HashMap<String, Interaction>();
			p2i.put(p, is);
			Set<Classifier> redefined = new HashSet<Classifier>();
			for (Element e: p.getOwnedElement()) {
				if (e instanceof Interaction && StereotypesHelper.hasStereotype(e, "Mode Scenario")) { // REVIEW -- hasStereotypeOrDerived()?
					String mode = ((Interaction)e).getName();
					is.put(mode, (Interaction)e);
					if (!modes.contains(mode))
						modes.add(mode);
					redefined.addAll(((Interaction)e).getRedefinedClassifier());
				}
			}
			if (includeInherited) {
			for (Element parent: Utils.collectDirectedRelatedElementsByRelationshipJavaClass(p, Generalization.class, 1, 1)) {
				for (Element e: parent.getOwnedElement()) {
					if (e instanceof Interaction && StereotypesHelper.hasStereotype(e, "Mode Scenario") && !redefined.contains(e)) { // REVIEW -- hasStereotypeOrDerived()?
						String mode = ((Interaction)e).getName();
						is.put(mode, (Interaction)e);
						if (!modes.contains(mode))
							modes.add(mode);
					}
				}
			}
			}
			/*for (Property prop: p.getOwnedAttribute()) {
				Type t = prop.getType();
				if (t != null && t instanceof StateMachine && StereotypesHelper.hasStereotypeOrDerived(t, "Power Load Behavior Characterization")) {
					for (Region r: ((StateMachine)t).getRegion()) {
						for (Vertex v: r.getSubvertex()) {
							if (v instanceof State && StereotypesHelper.hasStereotypeOrDerived(((State)v).getSubmachine(), "Power Load Characterization")) {
								String i = ((State)v).getSubmachine().getName();
								for (Element e2: p.getOwnedElement()) {
									if (e2 instanceof Interaction && ((Interaction)e2).getName().equals(i)) {
										is.put(i, (Interaction)e2);
										if (!modes.contains(i))
											modes.add(i);
										break;
									}
								}
							}
						}
					}
					break;
				}
			}*/
		}
	}
	
	/**
	 * product hierarchy depth
	 * @return
	 */
	public int getProductDepth() {
		return productDepth;
	}

	/**
	 * deployment hierarchy
	 * @return
	 */
	public Map<Class, List<Class>> getDeployment() {
		return deployment;
	}

	/**
	 * product to workpackage
	 * @return
	 */
	public Map<Class, NamedElement> getP2wp() {
		return p2wp;
	}
	
	/**
	 * workpackage hierarchy
	 * @return
	 */
	public Map<NamedElement, List<NamedElement>> getWpDeployment() {
		return wpDeployment;
	}

	/**
	 * workpackage hierarchy depth
	 * @return
	 */
	public int getWpDepth() {
		return wpDepth;
	}

	/**
	 * workpackage to list of products mapping
	 * @return
	 */
	public Map<NamedElement, List<Class>> getWp2p() {
		return wp2p;
	}
	
	/**
	 * workpackage to list of leaf products mapping
	 * @return
	 */
	public Map<NamedElement, List<Class>> getWp2LeafP() {
		return wp2lp;
	}
	
	/**
	 * product to list of power load characterizations
	 * @return
	 */
	public Map<Class, List<Class>> getP2plc() {
		return p2plc;
	}
	
	public Map<NamedElement, List<Class>> getWp2plc() {
		return wp2plc;
	}
	
	public Map<Class, Map<String, Interaction>> getP2i() {
		return p2i;
	}
	
	public List<String> getModes() {
		return modes;
	}
	
	public Map<Class, List<Property>> getP2p() {
		return p2p;
	}

	public Map<Property, Integer> getTotalUnits() {
		return totalUnits;
	}
	
}
