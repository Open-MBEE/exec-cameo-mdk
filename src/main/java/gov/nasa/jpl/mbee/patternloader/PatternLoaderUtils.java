package gov.nasa.jpl.mbee.patternloader;

import java.util.Collection;

import gov.nasa.jpl.mbee.stylesaver.StylerUtils;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

/**
 * A utility class for the Pattern Loader.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class PatternLoaderUtils {
	/**
	 * Checks that the requestor and its relationships/targets are correctly formatted.
	 * 
	 * @param requestor	the requestor the check
	 * @return			true if formatting is good, false otherwise
	 */
	public static boolean isGoodRequestor(PresentationElement requestor) {
		// check the stereotype
		Stereotype workingStereotype = StylerUtils.getWorkingStereotype(Application.getInstance().getProject());
		if(!StereotypesHelper.hasStereotypeOrDerived(requestor.getElement(), workingStereotype)) {
			Application.getInstance().getGUILog().log("1");
			return false;
		}
		
		// check if it has a single conform relationship
		Collection<DirectedRelationship> relationships = requestor.getElement().get_directedRelationshipOfSource();
		DirectedRelationship conform = (DirectedRelationship) getNextElement(relationships, "Conform", false);
		
		if(conform == null) {
			Application.getInstance().getGUILog().log("2");
			return false;
		}
		
		// check if the conform is linked to a viewpoint
		Collection<Element> targets = conform.getTarget();
		Element viewpoint = getNextElement(targets, "Viewpoint", false);
		
		if(viewpoint == null) {
			Application.getInstance().getGUILog().log("3");
			return false;
		}
		
		// check if the viewpoint has a dependency
		relationships = viewpoint.get_directedRelationshipOfSource();
		DirectedRelationship dependency = (DirectedRelationship) getNextElement(relationships, "Dependency", true);
		
		if(dependency == null) {
			Application.getInstance().getGUILog().log("4");
			return false;
		}
		
		// check if the dependency is linked to a package
		targets = dependency.getTarget();
		Element pkg = getNextElement(targets, "Package", true);
		
		if(pkg == null) {
			Application.getInstance().getGUILog().log("5");
			return false;
		}
		
		// check if the package has >0 pattern diagrams
		Collection<Element> patternDiagrams = pkg.getOwnedElement();
		
		if(patternDiagrams.size() == 0) {
			Application.getInstance().getGUILog().log("6");
			return false;
		}
		
		int diagramCtr = 0;
		for(Element elem : patternDiagrams) {
			if(elem instanceof Diagram) {
				diagramCtr++;
			}
		}
		
		if(diagramCtr == 0) {
			Application.getInstance().getGUILog().log("7");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Gets the next element in sequence for the Pattern Loader.
	 * 
	 * @param candidates	the collection of candidate next elements.
	 * @param comparisonStr	the identifying string to check against.
	 * @param checkType		set to true to get the next element by checking by type,
	 * 						set to false to get the next element by checking by stereotype
	 * @return				the next element in sequence if found, null otherwise
	 */
	private static Element getNextElement(Collection<? extends Element> candidates, String comparisonStr, boolean checkType) {
		Element nextElement = null;
		boolean found = false;
		
		// get next element by checking type
		if(checkType) {
			for(Element c : candidates) {
				if(c.getHumanType().equals(comparisonStr)) {
					if(found == true) {
						return null;
					}
					
					nextElement = c;
					found = true;
				}
			}
		}
		
		// get next element by checking stereotype
		if(!checkType) {
			for(Element c : candidates) {
				StereotypesHelper.getStereotype(Application.getInstance().getProject(), comparisonStr);
				if(StereotypesHelper.hasStereotypeOrDerived(c, comparisonStr)) {
					if(found == true) {
						return null;
					}
					
					nextElement = c;
					found = true;
				}
			}
		}
		
		return nextElement;
	}
}