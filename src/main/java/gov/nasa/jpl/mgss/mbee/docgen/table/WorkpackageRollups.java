package gov.nasa.jpl.mgss.mbee.docgen.table;

import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.DocGenUtils;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DBText;
import gov.nasa.jpl.mgss.mbee.docgen.docbook.DocumentElement;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import javax.swing.JOptionPane;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
/**
 * calculations are done using all raw numbers
 * expected values are precisioned
 * raw values are not
 * comparisons are done using raw values
 * this should be refactored to be more like how the pel rollups are done in userscripts
 * in fact, any ehm code should not be in docgen core and should be done using userscripts
 * @author dlam
 *
 */
public class WorkpackageRollups {

	private Map<NamedElement, List<NamedElement>> deployment;
	private Map<Class, Map<Class, Integer>> realUnits;
	private Map<NamedElement, Property> mass;
	private Map<NamedElement, Property> mc;		// mass contingency
	private Map<NamedElement, Property> cbe;		// current best estimate
	private Map<NamedElement, BigDecimal> emass;	// estimated mass
	private Map<NamedElement, BigDecimal> emc;		// estimated mass contingency
	private Map<NamedElement, BigDecimal> ecbe;	// estimated current best estimate
	private Map<NamedElement, BigDecimal> rmass;	// rollup mass
	private Map<NamedElement, BigDecimal> rmc;		// rollup mass contingency
	private Map<NamedElement, BigDecimal> rcbe;	// rollup current best estimate
	private Map<NamedElement, Property> allo;      // Current Allocation
	private Map<NamedElement, Property> marg;
	private Map<NamedElement, BigDecimal> margin;
	
	private Map<NamedElement, List<Class>> wp2p;
	private Map<NamedElement, List<NamedElement>> deployment2;
	private Map<Class, Integer> totalUnits;
	
	private Set<NamedElement> done;
	private boolean fix;
	private GUILog log;
	private int precision;
	
	private boolean badTable;
	private boolean gui;
	private boolean showmassmargin;
	
	private MathContext mathcontext;
	
	public WorkpackageRollups(Map<NamedElement, List<NamedElement>> deployment, Map<NamedElement, List<Class>> wp2p, Map<NamedElement, List<NamedElement>> deployment2,
			Map<Class, Map<Class, Integer>> realUnits, Map<Class, Integer> totalUnits,
			Map<NamedElement, Property> mass, Map<NamedElement, Property> mc, Map<NamedElement, Property> cbe, Map<NamedElement, Property> allo,Map<NamedElement, Property> marg, boolean fix, int precision, boolean gui, boolean showmargin) {
		this.deployment = deployment;
		this.realUnits = realUnits;
		this.mass = mass;
		this.mc = mc;
		this.cbe = cbe;
		this.allo=allo;
		this.done = new HashSet<NamedElement>();
		this.emass = new HashMap<NamedElement, BigDecimal>();
		this.emc = new HashMap<NamedElement, BigDecimal>();
		this.ecbe = new HashMap<NamedElement, BigDecimal>();
		this.rmass = new HashMap<NamedElement, BigDecimal>();
		this.rmc = new HashMap<NamedElement, BigDecimal>();
		this.rcbe = new HashMap<NamedElement, BigDecimal>();
		this.margin = new HashMap<NamedElement, BigDecimal>();
		this.marg=marg;
		this.showmassmargin=showmargin;
		log = Application.getInstance().getGUILog();
		this.fix = fix;
		this.precision = precision;
		
		this.wp2p = wp2p;
		this.deployment2 = deployment2;
		this.totalUnits = totalUnits;
		badTable = false;
		this.gui = gui;
		
		this.mathcontext = new MathContext(10, RoundingMode.HALF_DOWN);
	}
	
	public void fillExpected(NamedElement product) {
		List<NamedElement> children = deployment.get(product);
		if (children == null || children.isEmpty()) {
			if (wp2p != null) { //for workpackage rolllups
				rollupLeafWorkpackage(product);
				return;
			}
			calculateLeafCbe(product);
			return;
		}
		if (done.contains(product))
			return;
		Map<Class, Integer> units = null;
		if (realUnits != null)
			units = realUnits.get(product);
		BigDecimal m = new BigDecimal(0);
		BigDecimal mcbe = new BigDecimal(0);
		for (NamedElement child: children) { //rollup children for both
			fillExpected(child);
			BigDecimal multiplicity = null;
			if (units != null)
				multiplicity = new BigDecimal(units.get(child));
			if (multiplicity == null)
				multiplicity = new BigDecimal(1);
			BigDecimal cmass = rmass.get(child);
			if (cmass != null) {
				m = m.add(cmass.multiply(multiplicity));
			}
			BigDecimal ccbe = rcbe.get(child);
			if (ccbe != null) {
				mcbe = mcbe.add(ccbe.multiply(multiplicity));
			}
		}
		if (wp2p != null) { //if workpackage rollup, workpackage can also supplies leaf product nodes
			List<Class> products = wp2p.get(product);
			if (products != null) {
				for (Class p: products) {
					if (deployment2.get(p) == null || deployment2.get(p).isEmpty()) { //this is a leaf product node
						calculateLeafCbe(p);
						BigDecimal multiplicity = new BigDecimal(1);
						if (totalUnits.get(p) != null) {
							multiplicity = new BigDecimal(totalUnits.get(p));
						}
						BigDecimal cmass = rmass.get(p);
						if (cmass != null) {
							m = m.add(cmass.multiply(multiplicity));
						}
						BigDecimal ccbe = rcbe.get(p);
						if (ccbe != null) {
							mcbe = mcbe.add(ccbe.multiply(multiplicity));
						}
					}
				}
			}
		}
		rmass.put(product, m);
		rcbe.put(product, mcbe);
		BigDecimal mmc;
		try {
			mmc = mcbe.divide(m, mathcontext);
		} catch (ArithmeticException ae) {
			log.log("Calculating the contingency for " + product.getQualifiedName() + " results in NaN! Setting calculated to 1");
			if (gui) {
				JOptionPane.showMessageDialog(null, "Calculating the contingency for " + product.getQualifiedName() + " results in NaN! Setting calculated to 1", "Bad!", JOptionPane.ERROR_MESSAGE);
			}
			mmc = new BigDecimal(1);
		}
//		double mmc = mcbe / m;
//		m = Double.parseDouble(Utils.floatTruncate(m, precision));
//		mcbe = Double.parseDouble(Utils.floatTruncate(mcbe, precision));
		
//		try {
//			mmc = Double.parseDouble(Utils.floatTruncate(mmc, precision));
//		} catch (NumberFormatException e) {
//			log.log("Calculating the contingency for " + product.getQualifiedName() + " results in NaN! Setting expected and raw to 1");
//			if (gui) {
//				JOptionPane.showMessageDialog(null, "Calculating the contingency for " + product.getQualifiedName() + " results in NaN! Setting expected and raw to 1", "Bad!", JOptionPane.ERROR_MESSAGE);
//			}
//			mmc = 1;
//			rmc.put(product, mmc);
//		}
		rmc.put(product, mmc);
		emass.put(product, m);
		emc.put(product, mmc);
		ecbe.put(product, mcbe);
		done.add(product);
	}
	
	
	private void fix(NamedElement product, Property m, BigDecimal exm, BigDecimal raw, String type, List<List<DocumentElement>> dg) {
		if (m != null) {
			boolean bad = false;
			String thismasss = m.getDefault();
			//String thismassi = DocGenUtils.floatTruncate(thismasss, precision);
			BigDecimal thismassd = null;
			try {
				thismassd = new BigDecimal(thismasss); 
//				Double thismassd = Double.parseDouble(thismasss);
				if (thismassd.compareTo(raw) != 0)
					bad = true;
			} catch (NumberFormatException ex) {
				bad = true;
			}
			if (bad) {
				badTable = true;
				log.log("The " + type + " of " + product.getQualifiedName() + " does not match calculated! (model: " + thismassd + ", calculated: " + raw.toPlainString() + ")");
				if (dg != null) {
					List<DocumentElement> row = new ArrayList<DocumentElement>();
					row.add(new DBText(DocGenUtils.addInvisibleSpace(DocGenUtils.fixString(product.getQualifiedName()))));
					row.add(new DBText(type));
					row.add(new DBText(raw.toPlainString()));
					row.add(new DBText("<emphasis role=\"bold\">" + thismasss + "</emphasis>"));
					dg.add(row);
				}
				if (fix) {
					if (!m.isEditable()) {
						log.log("ERROR: " + m.getQualifiedName() + " is not editable!");
						if (gui) {
							JOptionPane.showMessageDialog(null, m.getQualifiedName() + " is not editable! Masses will not be right!", "Bad!", JOptionPane.ERROR_MESSAGE);
						}
					}
					else
						Utils.setPropertyValue(m, raw.toPlainString());
				}
			}
		} else {
			log.log("ERROR: "  + "The " + type + " of " + product.getQualifiedName() + " is not there!");
			badTable = true;
			if (dg != null) {
				List<DocumentElement> row = new ArrayList<DocumentElement>();
				row.add(new DBText(DocGenUtils.addInvisibleSpace(DocGenUtils.fixString(product.getQualifiedName()))));
				row.add(new DBText(type));
				row.add(new DBText(raw.toPlainString()));
				row.add(new DBText("<emphasis role=\"bold\">Missing</emphasis>"));
				dg.add(row);
			}
		}
	}
	
	public void validateOrFix(NamedElement product, List<List<DocumentElement>> dg) {
		List<NamedElement> children = deployment.get(product);
		for (NamedElement child: children)
			validateOrFix(child, dg);
		if (wp2p != null) {
			List<Class> products = wp2p.get(product);
			if (products != null) {
				for (Class p: products) {
					if (deployment2.get(p) == null || deployment2.get(p).isEmpty()) { //this is a leaf product node
						fix(p, mass.get(p), emass.get(p), rmass.get(p), "mass", dg);
						fix(p, mc.get(p), emc.get(p), rmc.get(p), "mass contingency", dg);
						fix(p, cbe.get(p), ecbe.get(p), rcbe.get(p), "cbe + contingency", dg);
						if(showmassmargin){
						if (margin.get(p)!=null)
						fix(p, marg.get(p), ecbe.get(p), margin.get(p), "Mass Margin", dg); 
						}
					}
				}
			}
		}
		fix(product, mass.get(product), emass.get(product), rmass.get(product), "mass", dg);
		fix(product, mc.get(product), emc.get(product), rmc.get(product), "mass contingency", dg);
		fix(product, cbe.get(product), ecbe.get(product), rcbe.get(product), "cbe + contingency", dg);
	}
	
	private void rollupLeafWorkpackage(NamedElement wp) {
		BigDecimal wpmass = new BigDecimal(0);
		BigDecimal wpcbe = new BigDecimal(0);
		List<Class> products = wp2p.get(wp);
		if (products != null) {
			for (Class p: products) {
				if (deployment2.get(p) == null || deployment2.get(p).isEmpty()) { //this is a leaf product node
					calculateLeafCbe(p);
					BigDecimal multiplicity = new BigDecimal(1);
					if (totalUnits.get(p) != null) {
						multiplicity = new BigDecimal(totalUnits.get(p));
					}
					BigDecimal cmass = rmass.get(p);
					if (cmass != null) {
						wpmass = wpmass.add(cmass.multiply(multiplicity));
					}
					BigDecimal ccbe = rcbe.get(p);
					if (ccbe != null) {
						wpcbe = wpcbe.add(ccbe.multiply(multiplicity));
					}
				}
			}
		}
		rmass.put(wp, wpmass);
		rcbe.put(wp, wpcbe);
		BigDecimal wpmc;
		try {
			wpmc = wpcbe.divide(wpmass, mathcontext);
		} catch (ArithmeticException e) {
			log.log("Calculating the contingency for " + wp.getQualifiedName() + " results in NaN! Setting expected and raw to 1");
			if (gui) {
				JOptionPane.showMessageDialog(null, "Calculating the contingency for " + wp.getQualifiedName() + " results in NaN! Setting expected and raw to 1", "Bad!", JOptionPane.ERROR_MESSAGE);
			}
			wpmc = new BigDecimal(1);
		}
//		double wpmc = wpcbe / wpmass;
//		wpmass = Double.parseDouble(Utils.floatTruncate(wpmass, precision));
//		wpcbe = Double.parseDouble(Utils.floatTruncate(wpcbe, precision));
		
//		try {
//			wpmc = Double.parseDouble(Utils.floatTruncate(wpmc, precision));
//		} catch (NumberFormatException ex) {
//			log.log("Calculating the contingency for " + wp.getQualifiedName() + " results in NaN! Setting expected and raw to 1");
//			if (gui) {
//				JOptionPane.showMessageDialog(null, "Calculating the contingency for " + wp.getQualifiedName() + " results in NaN! Setting expected and raw to 1", "Bad!", JOptionPane.ERROR_MESSAGE);
//			}
//			wpmc = 1;
//			rmc.put(wp, wpmc);
//		}
		rmc.put(wp, wpmc);
		emass.put(wp, wpmass);
		emc.put(wp, wpmc);
		ecbe.put(wp, wpcbe);
	}
	private void calculateLeafMargin(NamedElement product){
		Property leafallo = allo.get(product);
		Property leafmass = mass.get(product);
		Property leafmc = mc.get(product);
		Property leafcbe = cbe.get(product);
		BigDecimal thismass = new BigDecimal(0);
		BigDecimal thismc = new BigDecimal(0);
		BigDecimal calcbe = new BigDecimal(0);
		BigDecimal leafmassf = new BigDecimal(0);
		BigDecimal leafmcf = new BigDecimal(0);
		BigDecimal thisallo = new BigDecimal(0);
		BigDecimal leafallof = new BigDecimal(0);
		boolean nan = false;
		boolean doupdatetable=true;
		if (leafmass != null) {
			String leafmassd = leafmass.getDefault();
			if (leafmassd != null && !leafmassd.equals("")) {
				try {
					leafmassf = new BigDecimal(leafmassd); //Double.parseDouble(leafmassd);	
					thismass = leafmassf; //Double.parseDouble(Utils.floatTruncate(leafmassf, precision));
				} catch (NumberFormatException e) {
					nan=true;
					doupdatetable=false;
					log.log("ERROR: "  + leafmass.getQualifiedName() + " isn't a valid number, cannot calculate Mass Margin, rollup might be incorrect");
					if (gui) {
						//JOptionPane.showMessageDialog(null, leafmass.getQualifiedName() + " isn't a valid number, cannot calculate Mass Margin, rollup might be incorrect", "Bad!", JOptionPane.ERROR_MESSAGE);
					}
				}
			} else {
				log.log("ERROR: "  + "The mass of  "  + product.getQualifiedName()  + " isn't there, cannot calculate Mass Margin, rollup might be incorrect");
				if (gui) {
					//JOptionPane.showMessageDialog(null, "The mass of  "  + product.getQualifiedName()  + " isn't there, cannot calculate  Mass Margin, rollup might be incorrect", "Bad!", JOptionPane.ERROR_MESSAGE);
				}
			}
		} else {
			log.log("ERROR: "  + "The mass of "  + product.getQualifiedName()  + " isn't there, cannot calculate  Mass Margin, rollup might be incorrect");
			if (gui) {
				//JOptionPane.showMessageDialog(null, "The mass of "  + product.getQualifiedName()  + " isn't there, cannot calculate Mass Margin, rollup might be incorrect", "Bad!", JOptionPane.ERROR_MESSAGE);
			}
		}
		if (leafallo != null) {
			String leafallod = leafallo.getDefault();
			if (leafallod != null && !leafallod.equals("")) {
				try {
					leafallof = new BigDecimal(leafallod); //Double.parseDouble(leafmcd);		
					thisallo = leafallof; //Double.parseDouble(Utils.floatTruncate(leafmcf, precision));
				} catch (NumberFormatException e) {
					nan=true;
					doupdatetable=false;
					log.log("ERROR: "  + leafallo.getQualifiedName() + " isn't a valid number, cannot calculate Mass Margin, rollup might be incorrect");
					if (gui) {
						//JOptionPane.showMessageDialog(null, leafallo.getQualifiedName() + " isn't a valid number, cannot calculate Mass Margin, rollup might be incorrect", "Bad!", JOptionPane.ERROR_MESSAGE);
					}
				}
			} else {
				log.log("ERROR: "  + "The "  + product.getQualifiedName()  + " isn't there, cannot calculate Mass Margin, rollup might be incorrect");
				if (gui) {
					//JOptionPane.showMessageDialog(null, "The "  + product.getQualifiedName()  + " isn't there, cannot calculate Mass Margin, rollup might be incorrect", "Bad!", JOptionPane.ERROR_MESSAGE);
				}
			}
		} else {
			log.log("ERROR: "  + "The"  + product.getQualifiedName()  + " isn't there, cannot calculate Mass Margin, rollup might be incorrect");
			if (gui) {
				//JOptionPane.showMessageDialog(null, "The"  + product.getQualifiedName()  + " isn't there, cannot calculate MassMargin, rollup might be incorrect", "Bad!", JOptionPane.ERROR_MESSAGE);
			}
		}
		if(nan==false){
		calcbe = leafallof.subtract(leafmassf);
		calcbe = calcbe.divide(leafallof,10, RoundingMode.HALF_UP);
		}	
		if(doupdatetable)
			margin.put(product, calcbe);
		else
			margin.put(product, null);

	}
	
	private void calculateLeafCbe(NamedElement product) {
		if(showmassmargin){
			calculateLeafMargin(product);
			}
		Property leafmass = mass.get(product);
		Property leafmc = mc.get(product);
		Property leafcbe = cbe.get(product);
		BigDecimal thismass = new BigDecimal(0);
		BigDecimal thismc = new BigDecimal(0);
		BigDecimal calcbe = new BigDecimal(0);
		BigDecimal leafmassf = new BigDecimal(0);
		BigDecimal leafmcf = new BigDecimal(0);
		
		if (leafmass != null) {
			String leafmassd = leafmass.getDefault();
			if (leafmassd != null && !leafmassd.equals("")) {
				try {
					leafmassf = new BigDecimal(leafmassd); //Double.parseDouble(leafmassd);	
					thismass = leafmassf; //Double.parseDouble(Utils.floatTruncate(leafmassf, precision));
				} catch (NumberFormatException e) {
					log.log("ERROR: "  + leafmass.getQualifiedName() + " isn't a valid number, cannot calculate leaf node cbe, rollup might be incorrect");
					if (gui) {
						JOptionPane.showMessageDialog(null, leafmass.getQualifiedName() + " isn't a valid number, cannot calculate leaf node cbe, rollup might be incorrect", "Bad!", JOptionPane.ERROR_MESSAGE);
					}
				}
			} else {
				log.log("ERROR: "  + "The mass of leaf node "  + product.getQualifiedName()  + " isn't there, cannot calculate leaf node cbe, rollup might be incorrect");
				if (gui) {
					JOptionPane.showMessageDialog(null, "The mass of leaf node "  + product.getQualifiedName()  + " isn't there, cannot calculate leaf node cbe, rollup might be incorrect", "Bad!", JOptionPane.ERROR_MESSAGE);
				}
			}
		} else {
			log.log("ERROR: "  + "The mass of leaf node "  + product.getQualifiedName()  + " isn't there, cannot calculate leaf node cbe, rollup might be incorrect");
			if (gui) {
				JOptionPane.showMessageDialog(null, "The mass of leaf node "  + product.getQualifiedName()  + " isn't there, cannot calculate leaf node cbe, rollup might be incorrect", "Bad!", JOptionPane.ERROR_MESSAGE);
			}
		}
		if (leafmc != null) {
			String leafmcd = leafmc.getDefault();
			if (leafmcd != null && !leafmcd.equals("")) {
				try {
					leafmcf = new BigDecimal(leafmcd); //Double.parseDouble(leafmcd);		
					thismc = leafmcf; //Double.parseDouble(Utils.floatTruncate(leafmcf, precision));
				} catch (NumberFormatException e) {
					log.log("ERROR: "  + leafmc.getQualifiedName() + " isn't a valid number, cannot calculate leaf node cbe, rollup might be incorrect");
					if (gui) {
						JOptionPane.showMessageDialog(null, leafmc.getQualifiedName() + " isn't a valid number, cannot calculate leaf node cbe, rollup might be incorrect", "Bad!", JOptionPane.ERROR_MESSAGE);
					}
				}
			} else {
				log.log("ERROR: "  + "The mass contingency of leaf node "  + product.getQualifiedName()  + " isn't there, cannot calculate leaf node cbe, rollup might be incorrect");
				if (gui) {
					JOptionPane.showMessageDialog(null, "The mass contingnecy of leaf node "  + product.getQualifiedName()  + " isn't there, cannot calculate leaf node cbe, rollup might be incorrect", "Bad!", JOptionPane.ERROR_MESSAGE);
				}
			}
		} else {
			log.log("ERROR: "  + "The mass contingnecy of leaf node "  + product.getQualifiedName()  + " isn't there, cannot calculate leaf node cbe, rollup might be incorrect");
			if (gui) {
				JOptionPane.showMessageDialog(null, "The mass contingnecy of leaf node "  + product.getQualifiedName()  + " isn't there, cannot calculate leaf node cbe, rollup might be incorrect", "Bad!", JOptionPane.ERROR_MESSAGE);
			}
		}
		//calcbe = thismass * thismc;
//		calcbe = leafmassf * leafmcf; //raw
		calcbe = leafmassf.multiply(leafmcf);
		rmass.put(product, leafmassf);
		rmc.put(product, leafmcf);
		rcbe.put(product, calcbe);
		
//		calcbe = Double.parseDouble(Utils.floatTruncate(calcbe, precision));
		emass.put(product, thismass);
		emc.put(product, thismc);
		ecbe.put(product, calcbe);
	}
	
	public boolean isBad() {
		return badTable;
	}
}
