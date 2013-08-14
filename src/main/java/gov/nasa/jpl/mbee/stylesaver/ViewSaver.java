package gov.nasa.jpl.mbee.stylesaver;

import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.paths.PathElement;
import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.properties.Property;
import com.nomagic.magicdraw.properties.PropertyManager;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.ui.BaseProgressMonitor;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;

import javax.swing.JOptionPane;

import org.json.simple.*;

/**
 * A class that saves style information of elements on a diagram.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class ViewSaver extends MDAction {
	private static final long serialVersionUID = 1L;
	private static boolean suppressOutput;
	
	/**
	 * Initializes the ViewSaver.
	 * 
	 * @param id 		the ID of the action.
	 * @param value 	the name of the action.
	 * @param mnemonic	the mnemonic key of the action.
	 * @param group		the name of the related commands group.
	 * @param s			if true, user-interactivity is suppressed.
	 */
	public ViewSaver(String id, String value, int mnemonic, String group, boolean s) {
		super(id, value, null, null);
		
		suppressOutput = s;
	}
	
	/**
	 * Opens up the active diagram and performs the save operation.
	 * 
	 * @param e	the ActionEvent that fired this method.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		SessionManager.getInstance().createSession("Saving styles...");
		
    	Project proj = Application.getInstance().getProject();
    	
    	// try to load the active diagram
    	DiagramPresentationElement diagram;
    	try {
        	diagram = proj.getActiveDiagram();
        	diagram.ensureLoaded();
    	} catch (NullPointerException ex) {
    		if(!suppressOutput) {
	    		JOptionPane.showMessageDialog(null, "Exiting - there was an error loading the diagram.", "Error", JOptionPane.ERROR_MESSAGE);
    		}
			return;
    	}
    	
    	// ensure the diagram is locked for edit
    	if(!StylerUtils.isDiagramLocked(proj, diagram.getElement())) {
			JOptionPane.showMessageDialog(null, "This diagram is not locked for edit. Lock it before running this function.", "Error", JOptionPane.ERROR_MESSAGE);
    		return;
    	}
    	
    	// perform the save operation
		String JSONStr = save(proj, diagram, false);
		if(JSONStr == null) {
			SessionManager.getInstance().cancelSession();
			return;
		}
		
		SessionManager.getInstance().closeSession();
	}
	
	/**
	 * Returns relevant style information in a JSON string.
	 * 
	 * @param proj		the diagram's project.
	 * @param diagram	the diagram to save.
	 * @param suppress	set to true to suppress swing dialog boxes.
	 * @return 			null on error or the JSON style string for this diagram.
	 */
	public static String save(Project proj, DiagramPresentationElement diagram, boolean suppress) {
		suppressOutput = suppress;
				
    	// ensure diagram is open
    	if(diagram == null) {
    		if(!suppressOutput) {
    			JOptionPane.showMessageDialog(null, "Please open a diagram first.", "Error", JOptionPane.ERROR_MESSAGE);
    		}
    		
    		return null;
    	} else {
    		diagram.ensureLoaded();
    	}

    	// get the proper stereotype for diagrams of this project
		Stereotype workingStereotype = StylerUtils.getWorkingStereotype(proj);
		if(workingStereotype == null) {
			if(!suppressOutput) {
				JOptionPane.showMessageDialog(null, "This diagram's stereotype is invalid for this function.", "Error", JOptionPane.ERROR_MESSAGE);
    		}

			return null;
		}

    	// ensure that this diagram is stereotyped view
    	if(!StylerUtils.isGoodStereotype(diagram, workingStereotype)) {
    		int opt;
    		if(!suppressOutput) {
    			Object[] options = { "Yes", "Cancel save" };

	    		opt = JOptionPane.showOptionDialog(null, "This diagram is not stereotyped " + workingStereotype.getName() + ". It must be stereotyped " + workingStereotype.getName() + "\n" +
	    		                                         "or derived to continue with the save.\n\n" +
	    				                                 "Would you like to add the " + workingStereotype.getName() + " stereotype to this diagram?",
	    				                                 "Error", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
    		
	    		// if user presses no or closes the dialog, exit the program. else, add view stereotype to diagram 
	    		if((opt == JOptionPane.NO_OPTION) || (opt == JOptionPane.CLOSED_OPTION)) {
	    			return null;
	    		} else {
	    			StereotypesHelper.addStereotype(diagram.getElement(), workingStereotype);
	    		}
    		}
    	}

    	// get all the elements in the diagram and store them into a list
    	List<PresentationElement> elemList;
    	try {
    		elemList = diagram.getPresentationElements();
    	} catch(NullPointerException e) {
    		if(!suppressOutput) {
    			JOptionPane.showMessageDialog(null, "Save cancelled. There are no elements in the diagram.", "Info", JOptionPane.INFORMATION_MESSAGE);
    		}
    		
			return null;
    	}
    	
    	// get a JSON style string from each element and store them into a main store for this diagram
    	JSONObject mainStore = new JSONObject();
    	
    	// display a progress bar if output is not being suppressed, otherwise just execute the save
    	if(!suppressOutput) {
    		RunnableSaverWithProgress runnable;
			try {
		    	ViewSaver vs = new ViewSaver(null, null, 0, null, true);
	    		runnable = vs.new RunnableSaverWithProgress(mainStore, elemList);
			} catch(NoSuchMethodError ex) {
				ex.printStackTrace();
				return null;
			}
			
			BaseProgressMonitor.executeWithProgress(runnable, "Save Progress", true);
			
			// set the style string into the view "style" tag
			String finalStyleString = mainStore.toJSONString();
			StereotypesHelper.setStereotypePropertyValue(diagram.getElement(), workingStereotype, "style", finalStyleString);
			
			if(runnable.getSuccess()) {
				JOptionPane.showMessageDialog(null, "Save complete.", "Info", JOptionPane.INFORMATION_MESSAGE);
			}
			
			return finalStyleString;
    	} else {
    		return executeSave(elemList, mainStore);
    	}
	}
	
	/**
	 * Inner class contains a run method for the save operation.
	 * Updates progress bar dynamically.
	 */
	class RunnableSaverWithProgress implements RunnableWithProgress {
		private JSONObject mainStore;
		private List<PresentationElement> elemList;
		private boolean success;
		
		/**
		 * @param mainStore the main style store for this diagram.
		 * @param elemList	the elements to save.
		 */
		public RunnableSaverWithProgress(JSONObject mainStore, List<PresentationElement> elemList) {
			this.mainStore = mainStore;
			this.elemList = elemList;
		}
		
		/**
		 * Runs the save operation.
		 * @param progressStatus the status of the operation so far.
		 */
		@SuppressWarnings("unchecked")
		@Override
		public void run(ProgressStatus progressStatus) {
			for(PresentationElement elem : elemList) {
				if(progressStatus.isCancel()) {
					success = false;
					return;
				}
				
	       		// save the element's style properties
				try {
	    			String styleStr = getStyle(elem);
	    			
	    			// if there is no style to save, continue to next element
	    			if(styleStr == null) {
	    				continue;
	    			}
	    			
	    			mainStore.put(elem.getID(), styleStr);
				} catch(ClassCastException e) {
					e.printStackTrace();
				} catch(MissingResourceException e) {
					e.printStackTrace();
				}
				
	       		// recursively save child elements' style properties (e.g. ports)
	       		getStyleChildren(elem, mainStore, progressStatus);
	       		
	       		progressStatus.increase();
	       	}
			
			success = true;
		}
		
		/**
		 * Gets the value of the success property.
		 * @return the value of the success property.
		 */
		public boolean getSuccess() {
			return success;
		}
	}
	
	/**
	 * Generates the style string for the presentation elements on the diagram.
	 * 
	 * @param elemList	the list of presentation elements on the diagram.
	 * @param mainStore the JSONObject that will be converted into a JSON string.
	 * @return			the style string.
	 */
	@SuppressWarnings("unchecked")
	private static String executeSave(List<PresentationElement> elemList, JSONObject mainStore) {
       	for(PresentationElement elem : elemList) {
       		// save the element's style properties
			try {
    			String styleStr = getStyle(elem);
    			
    			// if there is no style to save, continue to next element
    			if(styleStr == null) {
    				continue;
    			}
    			
    			mainStore.put(elem.getID(), styleStr);
			} catch(ClassCastException e) {
				e.printStackTrace();
			} catch(MissingResourceException e) {
				e.printStackTrace();
			}
			
       		// recursively save child elements' style properties (e.g. ports)
       		getStyleChildren(elem, mainStore, null);
		}
       	
       	return mainStore.toJSONString();
	}
	
	/**
	 * Get a JSON string representing the style properties of the parameterized
	 * presentation element.
	 * 
	 * @param elem	the element to get style properties from.
	 * @return		the element's respective JSON style string.
	 */
	@SuppressWarnings("unchecked") // for JSONObject put() method
	private static String getStyle(PresentationElement elem) {
    	PropertyManager propMan;
    	JSONObject entry = new JSONObject();

		// get a property manager to retrieve style properties
		propMan = elem.getPropertyManager();
		List<Property> propList = propMan.getProperties();
		
		// no properties to save
		if(propList.isEmpty()) {
			return null;
		}
		
		// iterate over each property in the list and store key/value pairs into "entry"
		Iterator<Property> iter = propList.iterator();
		
		while(iter.hasNext()) {
			Property prop = iter.next();
			entry.put(prop.getID(), prop.toString());
		}
		
		// put element bounds into "entry" as well
		Rectangle rect = elem.getBounds();
		entry.put("rect_x", rect.getX());
		entry.put("rect_y", rect.getY());
		entry.put("rect_height", rect.getHeight());
		entry.put("rect_width", rect.getWidth());
		
		// save break points if the element is a PathElement
		if(elem instanceof PathElement) {
			List<Point> breakPoints = ((PathElement) elem).getAllBreakPoints();
			
			// add in the break points
			for(int i = 0; i < breakPoints.size(); i++) {
				entry.put("break_point_" + i, breakPoints.get(i).toString());
			}
			
			// add in the number of break points
			entry.put("num_break_points", breakPoints.size());
			
			Point supplierPt = ((PathElement) elem).getSupplierPoint();
			Point clientPt = ((PathElement) elem).getClientPoint();
			
			// add in the supplier and client points
			entry.put("supplier_point", supplierPt.toString());
			entry.put("client_point", clientPt.toString());
			
			int lineWidth = ((PathElement) elem).getLineWidth();
			
			entry.put("path_line_width", Integer.toString(lineWidth));
		}
		
		// convert the main entry store to a JSON string
		String JSONStr = entry.toJSONString();

		return JSONStr;
    }
	
	/**
	 * Recursively saves style information of owned elements into the JSONObject
	 * pointed to by mainStore.
	 * 
	 * @param elem		the element to get style properties from.
	 * @param mainStore	the JSONObject to store style information into.
	 */
	@SuppressWarnings("unchecked") // for JSONObject put() method
	private static void getStyleChildren(PresentationElement parent, JSONObject mainStore, ProgressStatus progressStatus) {
		// get the parent element's children
		List<PresentationElement> children = parent.getPresentationElements();
		
		// base case -- no children
		if(children.isEmpty()) {
			return;
		}

		// check to see if the user cancelled
		if(progressStatus != null) {
			if(progressStatus.isCancel()) {
				return;
			}
		}
		
		Iterator<PresentationElement> iter = children.iterator();
		
		// iterate over each owned element storing style properties
		while(iter.hasNext()) {
			PresentationElement currElem = iter.next();
			
			// recursively call on the current element
			getStyleChildren(currElem, mainStore, progressStatus);
			
			try {
    			String styleStr = getStyle(currElem);
    			
    			// store style only if there is a string to store
    			if(styleStr != null) {
	    			mainStore.put(currElem.getID(), styleStr);
    			}
			} catch(ClassCastException e) {
				e.printStackTrace();
			} catch(MissingResourceException e) {
				e.printStackTrace();
			}
		}
	}
}