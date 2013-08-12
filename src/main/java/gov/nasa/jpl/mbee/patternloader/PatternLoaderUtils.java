package gov.nasa.jpl.mbee.patternloader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

/**
 * A utility class for the Pattern Loader.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class PatternLoaderUtils {
	/**
	 * Checks that the requester and its relationships/targets are correctly formatted.
	 * 
	 * @param requester	the requester to check.
	 * @return			true if formatting is good, false otherwise.
	 */
	public static boolean isGoodRequester(PresentationElement requester) {
		if(requester.getElement() == null) {
			return false;
		}
		
		// check if it has a single conform relationship
		Collection<DirectedRelationship> relationships = requester.getElement().get_directedRelationshipOfSource();
		DirectedRelationship conform = (DirectedRelationship) getNextElement(relationships, "Conform", false);
		
		if(conform == null) {
			return false;
		}
		
		// check if the conform is linked to a viewpoint
		Collection<Element> targets = conform.getTarget();
		Element viewpoint = getNextElement(targets, "Viewpoint", false);
		
		if(viewpoint == null) {
			return false;
		}
		
		// check if the viewpoint has a dependency
		relationships = viewpoint.get_directedRelationshipOfSource();
		DirectedRelationship dependency = (DirectedRelationship) getNextElement(relationships, "Dependency", true);
		
		if(dependency == null) {
			return false;
		}
		
		// check if the dependency is linked to a package
		targets = dependency.getTarget();
		Element pkg = getNextElement(targets, "Package", true);
		
		if(pkg == null) {
			return false;
		}
		
		// check if the package has >0 pattern diagrams
		Collection<Element> patternDiagrams = pkg.getOwnedElement();
		
		if(patternDiagrams.size() == 0) {
			return false;
		}
		
		int diagramCtr = 0;
		for(Element elem : patternDiagrams) {
			if(elem instanceof Diagram) {
				diagramCtr++;
			}
		}
		
		if(diagramCtr == 0) {
			return false;
		}
		
		return true;
	}

	/**
	 * Gets the pattern diagrams stored in symbol's corresponding package holding pattern diagrams.
	 * 
	 * @param requester
	 * @return
	 */
	public static Collection<DiagramPresentationElement> getPatternDiagrams(PresentationElement requester) {
		Collection<DirectedRelationship> relationships = requester.getElement().get_directedRelationshipOfSource();
		DirectedRelationship conform = (DirectedRelationship) getNextElement(relationships, "Conform", false);
		
		Collection<Element> targets = conform.getTarget();
		Element viewpoint = getNextElement(targets, "Viewpoint", false);
		
		relationships = viewpoint.get_directedRelationshipOfSource();
		DirectedRelationship dependency = (DirectedRelationship) getNextElement(relationships, "Dependency", true);
		
		targets = dependency.getTarget();
		Package pkg = (Package) getNextElement(targets, "Package", true);
		
		Collection<Diagram> ownedElems = pkg.getOwnedDiagram();
		Collection<DiagramPresentationElement> patternDiagrams = new ArrayList<DiagramPresentationElement>(); 
		Iterator<Diagram> iter = ownedElems.iterator();
		
		while(iter.hasNext()) {
			Diagram currElem = iter.next();
			patternDiagrams.add(Application.getInstance().getProject().getDiagram(currElem));
		}
		
		return patternDiagrams;
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
	
	public static String removeUnnecessaryProperties(String pattern) {
		// parse the style string
		JSONParser parser = new JSONParser();
		Object parsedElemStyleStr = null;
		try {
			parsedElemStyleStr = parser.parse(pattern);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
		// remove properties we don't want to restore
		JSONObject parsedElemStyleObj = (JSONObject) parsedElemStyleStr;
		parsedElemStyleObj.remove("rect_x");
		parsedElemStyleObj.remove("rect_y");
		parsedElemStyleObj.remove("rect_height");
		parsedElemStyleObj.remove("rect_width");
		parsedElemStyleObj.remove("num_break_points");
		
		return parsedElemStyleObj.toJSONString();
	}
}