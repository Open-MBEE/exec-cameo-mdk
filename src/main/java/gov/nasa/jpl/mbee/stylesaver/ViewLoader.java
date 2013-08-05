package gov.nasa.jpl.mbee.stylesaver;

import java.awt.event.ActionEvent;

import javax.swing.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.properties.Property;
import com.nomagic.magicdraw.properties.PropertyManager;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.magicdraw.uml.symbols.paths.PathElement;
import com.nomagic.magicdraw.uml.symbols.shapes.ShapeElement;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.ui.BaseProgressMonitor;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

/**
 * A class that loads style information into elements on the active diagram
 * from the "style" tag of the diagram's stereotype. The diagram must be stereotyped
 * View or derived.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class ViewLoader extends MDAction {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Initializes the Loader.
	 * 
	 * @param id 		the ID of the action.
	 * @param value 	the name of the action.
	 * @param mnemonic	the mnemonic key of the action.
	 * @param group		the name of the related commands group.
	 */
	public ViewLoader(String id, String value, int mnemonic, String group) {
		super(id, value, null, null);
	}
	
	/**
	 * Opens up the active diagram and performs the load operation.
	 * 
	 * @param e	the ActionEvent that fired this method.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			SessionManager.getInstance().checkSessionExistance();
		} catch(IllegalStateException ex) {
	        SessionManager.getInstance().createSession("Load");
		}

		Project proj = Application.getInstance().getProject();	// get the project

    	// try to load the active diagram
    	DiagramPresentationElement diagram;
    	try {
        	diagram = proj.getActiveDiagram();
        	diagram.ensureLoaded();
    	} catch (NullPointerException ex) {
    		JOptionPane.showMessageDialog(null, "Exiting - there was an error loading the diagram.", "Error", JOptionPane.ERROR_MESSAGE);
            SessionManager.getInstance().cancelSession();
			return;
    	}
    	
    	if(!StylerUtils.isDiagramLocked(proj, diagram.getElement())) {
			JOptionPane.showMessageDialog(null, "This diagram is not locked for edit. Lock it before running this function.", "Error", JOptionPane.ERROR_MESSAGE);
			SessionManager.getInstance().cancelSession();
    		return;
    	}
    	
		Stereotype workingStereotype = StylerUtils.getWorkingStereotype(proj);
		if(workingStereotype == null) {
			return;
		}
    	
    	// ensure that this diagram is stereotyped with the working stereotype
    	if(!StylerUtils.isGoodStereotype(diagram, workingStereotype)) {
			Object[] options = { "Yes", "Cancel load" };

    		int opt = JOptionPane.showOptionDialog(null, "This diagram is not stereotyped " + workingStereotype.getName() + ". It must be stereotyped " + workingStereotype.getName() + "\n" +
    		                                         	 "or derived to continue with the load.\n\n" +
    				                                     "Would you like to add the " + workingStereotype.getName() + " stereotype to this diagram?",
    				                                     "Error", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		
    		// if user presses no or closes the dialog, exit the program. else, add view stereotype to diagram 
    		if((opt == JOptionPane.NO_OPTION) || (opt == JOptionPane.CLOSED_OPTION)) {
    			SessionManager.getInstance().cancelSession();
    			return;
    		} else {
    			StereotypesHelper.addStereotype(diagram.getElement(), workingStereotype);
    			JOptionPane.showMessageDialog(null, "Stereotype added.", "Info", JOptionPane.INFORMATION_MESSAGE);
    		}
    	}

    	// get all the elements in the active diagram and store them into a list
    	final List<PresentationElement> list;
    	try {
    		list = diagram.getPresentationElements();
    	} catch(NullPointerException ex) {
			JOptionPane.showMessageDialog(null, "Load cancelled. There are no elements in the diagram.", "Info", JOptionPane.INFORMATION_MESSAGE);
            SessionManager.getInstance().cancelSession();
			return;
    	}
       	
    	// get the main style string from the view stereotype tag "style"
    	Object styleObj = StereotypesHelper.getStereotypePropertyFirst(diagram.getElement(), workingStereotype, "style");
    	final String style = StereotypesHelper.getStereotypePropertyStringValue(styleObj);
    	
    	if((style != null) && (!style.equals(""))) {
    		RunnableWithProgress runnable = null;
    		try {
	    		runnable = new RunnableWithProgress() {
	    			public void run(ProgressStatus progressStatus) {
	    				progressStatus.init("Loading styles...", 0, list.size());
	    				load(list, style, progressStatus);
	    			}
	    		};
    		} catch(NoSuchMethodError ex) {
    			ex.printStackTrace();
    			return;
    		}
    		
    		BaseProgressMonitor.executeWithProgress(runnable, "Load Progress", false);
    	}
    	
		JOptionPane.showMessageDialog(null, "Load complete.", "Info", JOptionPane.INFORMATION_MESSAGE);
        SessionManager.getInstance().closeSession();
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
	
	/**
	 * Get the specific style string for an element from the main style string.
	 * 
	 * @param elem	the element the returned style string is for.
	 * @param style the main style string associated with the active diagram.
	 * @return 		the style string associated with the PresentationElement argument or null
	 *         		if the element has not yet had its style saved.
	 */
	private static String getStyleStringForElement(PresentationElement elem, String style) {
		JSONParser parser = new JSONParser();

		// parse the style string
		Object obj = null;
		try {
			obj = parser.parse(style);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		JSONObject jsonObj = (JSONObject) obj;
		
		// get the value associated with the element's ID
		String styleStr;
		try {
			styleStr = (String) jsonObj.get(elem.getID());
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
	 * Sets style properties in the parameterized PresentationElement according to the
	 * PresentationElement's style property.
	 * 
	 * @param elem	the element to set style properties in.
	 * @param style	the JSON style string for the element.
	 */
	public static void setStyle(PresentationElement elem, String style) {
		PropertyManager propMan = elem.getPropertyManager();
		JSONParser parser = new JSONParser();

		// parse the style string
		Object obj = null;
		try {
			obj = parser.parse(style);
		} catch(ParseException e) {
			e.printStackTrace();
		}
		
		JSONObject jsonObj = (JSONObject) obj;

		List<Property> propList = new ArrayList<Property>(propMan.getProperties());
		
		// no properties to load
		if(propList.isEmpty()) {
			return;
		}
		
		ListIterator<Property> iter = propList.listIterator();
		
		// iterate over all the current properties
		while(iter.hasNext()) {
			Property currProp = iter.next();
			
			String currPropID = currProp.getID();

			// generate the new property
			String newPropValue = (String) jsonObj.get(currPropID);
			
			// select the correct set method for the property's type
			if(currProp.getValue() instanceof java.awt.Color) {
				ViewLoaderHelper.setColor(currProp, newPropValue, elem);
			} else if(currProp.getValue() instanceof java.awt.Font) {
				ViewLoaderHelper.setFont(currProp, newPropValue, elem);
			} else if(currProp.getValue() instanceof java.lang.Boolean) {
				ViewLoaderHelper.setBoolean(currProp, newPropValue, elem);
			} else if(currProp instanceof com.nomagic.magicdraw.properties.ChoiceProperty) {
				ViewLoaderHelper.setChoice(currProp, newPropValue, elem);
			}
		}
		
		// deal with break points if the element is a PathElement
		if(elem instanceof PathElement) {
			ViewLoaderHelper.setBreakPoints((PathElement) elem, jsonObj);
			ViewLoaderHelper.setLineWidth((PathElement) elem, jsonObj);
		}
		
		// deal with bounds if the element is a ShapeElement
		if(elem instanceof ShapeElement) {
			ViewLoaderHelper.setBounds((ShapeElement) elem, jsonObj);
		}
	}
}