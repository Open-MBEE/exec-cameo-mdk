package gov.nasa.jpl.mbee.lib;

import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Classifier;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Relationship;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

public class ModelLib {
	private static boolean isIMCE = false;
	
	public static String CHARACTERIZES = "analysis:characterizes";
	
	public static boolean isWorkPackage(Element e){
		if (e == null)
			return false;
		
		if (!(e instanceof NamedElement))
			return false;
		
		
		return StereotypesHelper.hasStereotypeOrDerived(e, "Work Package") || StereotypesHelper.hasStereotypeOrDerived(e, "project:WorkPackage");
	}
	
	public static boolean isOriginalWorkPackage(Element e){
		if (e == null)
			return false;
		
		if (!(e instanceof Class))
			return false;
		
		
		return StereotypesHelper.hasStereotypeOrDerived(e, "Work Package");
	}
	
	public static boolean isSupplies(Element e){
		return StereotypesHelper.hasStereotypeOrDerived(e, "supplies") || StereotypesHelper.hasStereotypeOrDerived(e, "project:supplies");
	}
	
	public static boolean isIMCE(){
		return isIMCE;
	}
	
	public static boolean isProduct(Element p){
		return (StereotypesHelper.hasStereotypeOrDerived(p, "Product") || StereotypesHelper.hasStereotypeOrDerived(p, "mission:Component"));
	}
	
	public static boolean isPLProduct(Element p){
		return (StereotypesHelper.hasStereotypeOrDerived(p, "Power Load Product") || StereotypesHelper.hasStereotypeOrDerived(p, "Power Load Component") || StereotypesHelper.hasStereotypeOrDerived(p,  "europa:PowerLoadComponent"));
	}
	
	public static boolean isMassCharacterization(Element e){
		if (!(e instanceof Classifier))
			return false;

		return (StereotypesHelper.hasStereotypeOrDerived(e, "analysis:Characterization") && extendsFromMass((Classifier) e));
	
	}
		
	public static boolean isCharacterizes(Relationship r){
		return (StereotypesHelper.hasStereotypeOrDerived(r, "analysis:characterizes"));
	}
	
	public static boolean extendsFromMass(Classifier e){
		for (Classifier g : e.getGeneral()){
			if (g.getName().equals("Launch Mass"))
				return true;
				
			extendsFromMass(g);
		}
		
		return false;
	}
}
