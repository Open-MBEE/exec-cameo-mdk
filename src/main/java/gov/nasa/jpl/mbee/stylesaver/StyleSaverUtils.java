package gov.nasa.jpl.mbee.stylesaver;

import gov.nasa.jpl.mbee.lib.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.teamwork.application.TeamworkUtils;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;

/**
 * Utility functions for the Styler should be put here.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class StyleSaverUtils {
	private static List<DiagramPresentationElement>  dpels;
	
	/**
	 * Returns the correct View or derived stereotype necessary for saving styles in this project.
	 * 
	 * @param proj	the project to look up.
	 * @return		the working stereotype for this project or null if one does not exist.
	 */
	public static Stereotype getWorkingStereotype(Project proj) {
		Stereotype workingStereotype = null;
		String[] possStereotypes = { "view" };
		int index = 0;
		
		while((workingStereotype == null) && (index < possStereotypes.length)) {			
			workingStereotype = StereotypesHelper.getStereotype(proj, possStereotypes[index], "Document Profile");
			
			index++;
		}
		
		if(workingStereotype == null) {
			return null;
		}

		// get all the owned elements of the stereotype
		Collection<Element> ownedElems = workingStereotype.getOwnedElement();
		boolean stylePropertyFound = false;
		
		if(ownedElems == null) {
			/* 
			 * NOTE: may be a good idea to implement usage of a "style block" associated
			 * with this diagram if the style property is not found.
			 * 
			 * We will just return null and exit the program for now.
			 */
			
			return null;
		}
	
		// search for the style property
		for(Element elem : ownedElems) {
			if(elem.getHumanName().equals("Property style")) {
				stylePropertyFound = true;
			}
		}

		if(!stylePropertyFound) {
			// see block comment above
			return null;
		}
	
		return workingStereotype;
	}
	
	/**
	 * Checks if the diagram has the actual working stereotype or derived.
	 * Also checks if the working stereotype has a style tag associated with it.
	 * 
	 * @param diag				the diagram to check
	 * @param workingStereotype the stereotype to check
	 * @return 					true if the diagram is stereotyped property, false otherwise
	 */
	public static boolean isGoodStereotype(DiagramPresentationElement diag, Stereotype workingStereotype) {
		boolean hasStereotype = StereotypesHelper.hasStereotypeOrDerived(diag.getElement(), workingStereotype);
		boolean hasSlot = StereotypesHelper.getPropertyByName(workingStereotype, "style") != null;
		
		return hasStereotype && hasSlot;
	}
	
	/**
	 * Get the specific style string for an element from the main style string.
	 * 
	 * @param elem	the element the returned style string is for.
	 * @param style the main style string associated with the active diagram.
	 * @return 		the style string associated with the PresentationElement argument or null
	 *         		if the element has not yet had its style saved.
	 */
	public static String getStyleStringForElement(PresentationElement elem, JSONObject style) {
		// get the value associated with the element's ID
		String styleStr;
		try {
			styleStr = (String) style.get(elem.getID());
		} catch(NullPointerException e) {
			return null;
		}
		
		// element has not yet had its style saved
		if(styleStr == null) {
			return null;
		}
		
		return styleStr;
	}
	
	/**
	 * Checks if the diagram is a locked Teamwork project.
	 * 
	 * @param project	the project that contains the diagram.
	 * @param diagram	the diagram to check
	 * @return			true if the diagram is locked, false otherwise
	 */
	public static boolean isDiagramLocked(Project project, Element diagram) {
		// get all the locked elements in the project
		Collection<Element> lockedElems;
		try {
			lockedElems = TeamworkUtils.getLockedElement(project, null);
		} catch(NullPointerException e) {
			// looks like this is just a local project
			return true;
		}
		
		// try to find the diagram in the collection of locked project elements
		for(Element elem : lockedElems) {
			if(elem.getID().equals(diagram.getID())) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Parses the style string that should have been stored in the "style" tag.
	 * 
	 * @param style	the string to parse.
	 * @return		the parsed JSON Object.
	 */
	public static JSONObject parse(String style) {
		JSONParser parser = new JSONParser();
		Object obj = null;
		
		try {
			obj = parser.parse(style);
		} catch(ParseException e) {
			e.printStackTrace();
		}
		
		return (JSONObject) obj;
	}
	
	/**
	 * Utility for converting collection of Diagrams into DiagramPresentationElements.
	 * @param	elements	Collection of diagrams to convert
	 * @return				Collection of DiagramPresentationElements
	 */
	public static Collection<DiagramPresentationElement> findDiagramPresentationElements(Collection<? extends Element> elements) {
		List<DiagramPresentationElement> dpels = new ArrayList<DiagramPresentationElement>();
		for (Element e: elements) {
			if (e instanceof Diagram) {
				Diagram d = (Diagram) e;
				dpels.add(Application.getInstance().getProject().getDiagram(d));
			}
		}
		
		return (Collection<DiagramPresentationElement>) dpels;
	}
}