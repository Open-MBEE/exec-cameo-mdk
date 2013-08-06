package gov.nasa.jpl.mbee.patternloader;

import gov.nasa.jpl.mbee.stylesaver.ViewLoader;

import java.awt.event.ActionEvent;
import java.util.List;

import org.json.simple.JSONObject;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;

/**
 * A class used to load patterns onto the active diagram from other project diagrams.
 * 
 * @author Benjamin Inada JPL/Caltech
 */
public class PatternLoader extends MDAction {
	private static final long serialVersionUID = 1L;

	/**
	 * Initializes the PatternLoader.
	 * 
	 * @param id 		the ID of the action.
	 * @param value 	the name of the action.
	 * @param mnemonic	the mnemonic key of the action.
	 * @param group		the name of the related commands group.
	 */
	public PatternLoader(String id, String value, int mnemonic, String group) {
		super(id, value, mnemonic, group);
	}

	/**
	 * Perform a pattern load on menu option mouse click.
	 * 
	 * @param e	the ActionEvent that fired this method.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
    	Project proj = Application.getInstance().getProject();
    	
    	// try to load the active diagram
    	DiagramPresentationElement activeDiag;
    	try {
        	activeDiag = proj.getActiveDiagram();
    	} catch (NullPointerException ex) {
    		Application.getInstance().getGUILog().log("There is no diagram open. The pattern saver is now exiting.");
			return;
    	}
    	
    	// get all of the diagram's elements
    	List<PresentationElement> elemList = activeDiag.getPresentationElements();
    	
		// save the pattern
		PatternSaver ps  = new PatternSaver();
		ps.savePattern(proj, activeDiag);
		
    	loadPattern(elemList, ps.getPattern());
	}
	
	/**
	 * Loads the style of elements on the diagram by gathering relevant style information from the JSONObject.
	 * 
	 * @param elemList	the list of elements to load styles into.
	 * @param pattern	the pattern to load.
	 */
	private static void loadPattern(List<PresentationElement> elemList, JSONObject pattern) {
		try {
			SessionManager.getInstance().checkSessionExistance();
		} catch(IllegalStateException ex) {
	        SessionManager.getInstance().createSession("Loading pattern...");
		}
		
		for(PresentationElement elem : elemList) {
			String elemStyle = (String) pattern.get(elem.getHumanType());
			
			if(elemStyle == null) {
				// there was no style pattern found for this element, load children and then continue
				setStyleChildren(elem, pattern);
				continue;
			}
			
			// load the style of the diagram element re-using the ViewLoader.setStyle() method
			ViewLoader.setStyle(elem, elemStyle);
			
			// then load the style of its children recursively
			setStyleChildren(elem, pattern);
			
			elem.getDiagramSurface().repaint();
		}
	}
	
	/**
	 * Recursively loads style information of owned elements.
	 * 
	 * @param parent	the parent element to recurse on.
	 * @param style		the central style string holding all style properties.
	 */
	private static void setStyleChildren(PresentationElement parent, JSONObject pattern) {
		List<PresentationElement> children = parent.getPresentationElements();
		
		// base case -- no children
		if(children.isEmpty()) {
			return;
		}
		
		// recursively load the style of the diagram element's children
		loadPattern(children, pattern);
	}

	
	/**
	 * Loads the style of elements on the diagram by gathering relevant style information from the JSON style string.
	 * Monitors progress.
	 * 
	 * @param elemList			the list of elements to load styles into.
	 * @param style				the style string.
	 * @param progressStatus	the status of the program status bar.
	 */
	/*
	public static void load(List<PresentationElement> elemList, String style, ProgressStatus progressStatus) {
    	for(PresentationElement elem : elemList) {
    		// parse the style string for the correct style of each element
    		String elemStyle = getStyleStringForElement(elem, style);
    		
    		if(elemStyle == null) {
    			// there was no style string found for this element, load children and then continue
				setStyleChildren(elem, style);
				continue;
    		}
    		
    		// load the style of the diagram element
    		setStyle(elem, elemStyle);
    		
    		// then load the style of its children recursively
    		setStyleChildren(elem, style);
    		
    		elem.getDiagramSurface().repaint();
    		
    		progressStatus.increase();
       	}
	}
	*/

	/**
	 * Recursively loads style information of owned elements.
	 * 
	 * @param parent	the parent element to recurse on.
	 * @param style		the central style string holding all style properties.
	 */
	/*
	private static void setStyleChildren(PresentationElement parent, String style) {
		List<PresentationElement> children = parent.getPresentationElements();
		
		// base case -- no children
		if(children.isEmpty()) {
			return;
		}
		
		// recursively load the style of the diagram element's children
		load(children, style);
	}
	*/
}