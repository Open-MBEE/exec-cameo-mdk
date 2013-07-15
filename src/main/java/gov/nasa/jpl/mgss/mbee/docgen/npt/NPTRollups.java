package gov.nasa.jpl.mgss.mbee.docgen.npt;

import gov.nasa.jpl.mbee.lib.Utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;

/**
 * this does get called form the docgen 3 version of properties table, but I don't think anyone actually uses it
 * the rollup property definition needs more requirements
 * @author dlam
 *
 */
public class NPTRollups {

	private Map<Class, Map<Class, Integer>> deployment;
	private Map<Class, Double> emass;
	private Map<Class, Double> rmass;
	private List<String> path;
	
	private Set<Class> done;
	private boolean fix;
	private GUILog log;
	private int precision;
	
	public NPTRollups(Map<Class, Map<Class, Integer>> deployment, 
			List<String> path, boolean fix, int precision) {
		this.deployment = deployment;
		this.done = new HashSet<Class>();
		this.emass = new HashMap<Class, Double>();
		this.rmass = new HashMap<Class, Double>();
		log = Application.getInstance().getGUILog();
		this.fix = fix;
		this.precision = precision;
		this.path = path;
	}
	
	public void fillExpected(Class product) {
		Map<Class, Integer> children = deployment.get(product);
		if (children == null || children.isEmpty()) {
			Property leafmass = getPropertyElement(product, path);
			double leafmassf = 0;
			double thismass = 0;
			if (leafmass != null) {
				String leafmassd = leafmass.getDefault();
				if (leafmassd != null && !leafmassd.equals("")) {
					try {
						leafmassf = Double.parseDouble(leafmassd);	
						thismass = Double.parseDouble(Utils.floatTruncate(leafmassf, precision));
					} catch (NumberFormatException e) {
						log.log("ERROR: "  + leafmass.getQualifiedName() + " isn't a valid number, rollup might be incorrect");
					}
				} else {
					log.log("ERROR: "  + "The mass of leaf node "  + product.getQualifiedName()  + " isn't there, rollup might be incorrect");
				}
			} else {
				log.log("ERROR: "  + "The mass of leaf node "  + product.getQualifiedName()  + " isn't there, rollup might be incorrect");
			}
			rmass.put(product, leafmassf);
			emass.put(product, thismass);
			return;
		}
		if (done.contains(product))
			return;
		double m = 0;
		for (Class child: children.keySet()) {
			fillExpected(child);
			Integer multiplicity = children.get(child);
			Double cmass = emass.get(child);
			if (cmass != null) {
				m += cmass * multiplicity;
			}
		}
		rmass.put(product, m);
		m = Double.parseDouble(Utils.floatTruncate(m, precision));
		emass.put(product, m);
		done.add(product);
	}
	
	
	private void fix(Class product, Property m, Double exm, Double raw, String type) {
		if (m != null) {
			boolean bad = false;
			String thismasss = m.getDefault();
			String thismassi = Utils.floatTruncate(thismasss, precision);
			try {
				Double thismassd = Double.parseDouble(thismassi);
				if (thismassd.compareTo(exm) != 0)
					bad = true;
			} catch (NumberFormatException ex) {
				bad = true;
			}
			if (bad) {
				log.log("The " + type + " of " + product.getQualifiedName() + " does not match expected! (raw values shown) (" + thismasss + " instead of " + Double.toString(raw) + ")");
				if (fix) {
					if (!m.isEditable())
						log.log("ERROR: " + m.getQualifiedName() + " is not editable!");
					else
						Utils.setPropertyValue(m, Double.toString(raw));
				}
			}
		} else {
			log.log("ERROR: "  + "The " + type + " of " + product.getQualifiedName() + " is not there!");
		}
	}
	
	public void validateOrFix(Class product) {
		Map<Class, Integer> children = deployment.get(product);
		for (Class child: children.keySet())
			validateOrFix(child);
		Property thismass = getPropertyElement(product, path);
		Double exmass = emass.get(product);
		double rawm = rmass.get(product);
		fix(product, thismass, exmass, rawm, "mass");
	}
	
	public Property getPropertyElement(Class e, List<String> propSpec) {
    	if (propSpec.size() < 2) {
    		for (Property p: e.getOwnedAttribute()) {
    			if (p.getName().equals(propSpec.get(0)))
    				return p;
    		}
    		return null;
    	}
    	for (Property p: e.getOwnedAttribute()) {
    		if (p.getName().equals(propSpec.get(0)) && p.getType() != null && p.getType() instanceof Class)
    			return getPropertyElement((Class)p.getType(), propSpec.subList(1, propSpec.size()));
    	}
    	return null;
    }
}
