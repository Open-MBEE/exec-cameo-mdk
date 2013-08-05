package gov.nasa.jpl.mbee.patternsaver;

import gov.nasa.jpl.mbee.stylesaver.ViewLoader;

import java.awt.event.ActionEvent;
import java.util.List;

import org.json.simple.JSONObject;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.task.ProgressStatus;

public class PatternLoader extends MDAction {
	private static final long serialVersionUID = 1L;

	public PatternLoader(String id, String value, int mnemonic, String group) {
		super(id, value, mnemonic, group);
	}

	/**
	 * 
	 * 
	 * @param e	the ActionEvent that fired this method.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
    	Project proj = Application.getInstance().getProject();
    	
    	// try to load the active diagram
    	DiagramPresentationElement diag;
    	try {
        	diag = proj.getActiveDiagram();
    	} catch (NullPointerException ex) {
			return;
    	}
    	
    	// get all of the diagram's elements
    	List<PresentationElement> elemList = diag.getPresentationElements();
    	
		// save the pattern
		PatternSaver ps  = new PatternSaver();
		ps.savePattern(proj, diag);
		
    	loadPattern(elemList, ps.getPattern());
	}
	
	private void loadPattern(List<PresentationElement> elemList, JSONObject pattern) {
		for(PresentationElement elem : elemList) {
			String elemStyle = (String) pattern.get(elem.getHumanType());
			
			if(elemStyle == null) {
				// there was no style string found for this element, load children and then continue
				setStyleChildren(elem, elemStyle);
				continue;
			}
			
			// load the style of the diagram element
			ViewLoader.setStyle(elem, elemStyle);
			
			// then load the style of its children recursively
			setStyleChildren(elem, elemStyle);
			
			elem.getDiagramSurface().repaint();
		}
	}
	
	

	
	
	/**
	 * Loads the style of elements on the diagram by gathering relevant style information from the JSON style string.
	 * 
	 * @param elemList	the list of elements to load styles into.
	 * @param style		the style string.
	 */
	public static void load(List<PresentationElement> elemList, String style) {
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
       	}
	}

	/**
	 * Loads the style of elements on the diagram by gathering relevant style information from the JSON style string.
	 * Monitors progress.
	 * 
	 * @param elemList			the list of elements to load styles into.
	 * @param style				the style string.
	 * @param progressStatus	the status of the program status bar.
	 */
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

	/**
	 * Recursively loads style information of owned elements.
	 * 
	 * @param parent	the parent element to recurse on.
	 * @param style		the central style string holding all style properties.
	 */
	private static void setStyleChildren(PresentationElement parent, String style) {
		List<PresentationElement> children = parent.getPresentationElements();
		
		// base case -- no children
		if(children.isEmpty()) {
			return;
		}
		
		// recursively load the style of the diagram element's children
		load(children, style);
	}
}