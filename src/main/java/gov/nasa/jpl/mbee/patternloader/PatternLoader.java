package gov.nasa.jpl.mbee.patternloader;

import gov.nasa.jpl.mbee.stylesaver.ViewLoader;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import org.json.simple.JSONObject;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.copypaste.CopyPasting;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.ui.BaseProgressMonitor;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * A class used to load patterns onto the active diagram from other project diagrams.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class PatternLoader extends MDAction {
	private static final long serialVersionUID = 1L;
	private PresentationElement requester;

	/**
	 * Initializes the Pattern Loader.
	 * 
	 * @param id 		the ID of the action.
	 * @param value 	the name of the action.
	 * @param mnemonic	the mnemonic key of the action.
	 * @param group		the name of the related commands group.
	 */
	public PatternLoader(String id, String value, int mnemonic, String group, PresentationElement requester) {
		super(id, value, mnemonic, group);
		
		this.requester = requester;
	}

	/**
	 * Perform a pattern load on menu option mouse click.
	 * 
	 * @param e	the ActionEvent that fired this method.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
        SessionManager.getInstance().createSession("Loading Pattern...");
		
		// allow user to pick load or stamp operation
		try {
			if(requester == null) {
				this.requester = Application.getInstance().getProject().getActiveDiagram();
			}
			delegateOperation();
		} catch(RuntimeException ex) {
			ex.printStackTrace();
			SessionManager.getInstance().cancelSession();
			return;
		}
		
		SessionManager.getInstance().closeSession();
	}
	
	/**
	 * Allows the user to pick the load or stamp operation.
	 * 
	 * @throws RuntimeException if a problem with loading or stamping occurs.
	 */
	private void delegateOperation() throws RuntimeException {
		Object[] options = { "Load pattern", "Stamp pattern" };

		int opt = JOptionPane.showOptionDialog(null,
                                               "Would you like to load or stamp a pattern onto the diagram?\n",
			                                   null,
			                                   JOptionPane.YES_NO_OPTION,
			                                   JOptionPane.QUESTION_MESSAGE,
			                                   null,
			                                   options,
			                                   options[0]);
	
		// if user presses no or closes the dialog, exit the program. else, add view stereotype to diagram 
		if((opt == JOptionPane.CANCEL_OPTION) || (opt == JOptionPane.CLOSED_OPTION)) {
			throw new RuntimeException();
		}
		
		if((opt == JOptionPane.YES_OPTION)) {
			try {
				runLoadPattern();
			} catch(RuntimeException e) {
				e.printStackTrace();
				throw new RuntimeException();
			}
		} else if((opt == JOptionPane.NO_OPTION)) {
			try {
				runStampPattern();
			} catch(RuntimeException e) {
				e.printStackTrace();
				throw new RuntimeException();
			}
		}
	}
	
	/**
	 * Runs the Pattern Loader with a progress bar.
	 * 
	 * @throws RuntimeException if a problem with loading is encountered.
	 */
	private void runLoadPattern() throws RuntimeException {
    	final Project proj = Application.getInstance().getProject();
        
    	// get the presentation elements of the requester - there should only be one (the diagram)
    	Element requesterElem = requester.getElement();
    	Diagram requesterDiagramElem = (Diagram) proj.getElementByID(requesterElem.getID());
       	
		final DiagramPresentationElement patternDiagram = getPatternDiagram();
    	DiagramPresentationElement targetDiagram = proj.getDiagram(requesterDiagramElem);
		
		if((patternDiagram == null) || (targetDiagram == null)) {
    		throw new RuntimeException();
		}
		
    	final List<PresentationElement> elemList = targetDiagram.getPresentationElements();
    		
		RunnableWithProgress runnable = null;
		try {
    		runnable = new RunnableWithProgress() {
    			public void run(ProgressStatus progressStatus) {
    				progressStatus.init("Loading pattern...", 0, elemList.size());
    				
					// save the pattern in the pattern diagram
					PatternSaver ps  = new PatternSaver();
					ps.savePattern(proj, patternDiagram);
					
					// load the pattern in the active diagram
			    	loadPattern(elemList, ps.getPattern(), progressStatus);
    			}
    		};
		} catch(NoSuchMethodError ex) {
    		Application.getInstance().getGUILog().log("There was a problem starting the Pattern Loader. The Pattern Loader is now exiting.");
    		throw new RuntimeException();
		}
		
		BaseProgressMonitor.executeWithProgress(runnable, "Load Progress", false);
		
		JOptionPane.showMessageDialog(null, "Pattern load complete.", "Info", JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * Runs the Pattern Stamper.
	 * 
	 * @throws RuntimeException if a problem with loading is encountered.
	 */
	private void runStampPattern() throws RuntimeException {
    	final Project proj = Application.getInstance().getProject();
    	
    	// get the presentation elements of the requester - there should only be one (the diagram)
    	Element requesterElem = requester.getElement();
    	final Diagram requesterDiagramElem = (Diagram) proj.getElementByID(requesterElem.getID());
    	
		final DiagramPresentationElement patternDiagram = getPatternDiagram();
		final DiagramPresentationElement targetDiagram = proj.getDiagram(requesterDiagramElem);
		
		if((patternDiagram == null) || (targetDiagram == null)) {
    		throw new RuntimeException();
		}
		
		// need to ensure that both diagrams are loaded before operating on them
		patternDiagram.ensureLoaded();
		targetDiagram.ensureLoaded();
		
		final List<PresentationElement> patternElements = patternDiagram.getPresentationElements();
		
		RunnableWithProgress runnable = null;
		try {
    		runnable = new RunnableWithProgress() {
    			public void run(ProgressStatus progressStatus) {
    				progressStatus.init("Stamping pattern...", 0, 100);
    				CopyPasting.copyPasteElements(patternElements, targetDiagram.getObjectParent(), targetDiagram, true, false);
    			}
    		};
		} catch(NoSuchMethodError ex) {
    		Application.getInstance().getGUILog().log("There was a problem starting the Pattern Stamper. The Pattern Stamper is now exiting.");
    		throw new RuntimeException();
		}
		
		BaseProgressMonitor.executeWithProgress(runnable, "Load Progress", false);
		
		JOptionPane.showMessageDialog(null, "Pattern stamp complete.", "Info", JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * Provides an input dialog for the user to pick a diagram to load a pattern from.
	 * 
	 * @return the user-selected pattern diagram.
	 */
	private DiagramPresentationElement getPatternDiagram() {
		// get all of the diagrams in the package for the user to pick from
		Collection<DiagramPresentationElement> diagCollection = PatternLoaderUtils.getPatternDiagrams(requester);
		Iterator<DiagramPresentationElement> diagIter = diagCollection.iterator();
		
		int numNames = diagCollection.size();
		String[] diagramNames = new String[numNames];
		
		for(int i = 0; diagIter.hasNext(); i++) {
			DiagramPresentationElement currDiag = diagIter.next();
			diagramNames[i] = currDiag.getName() + "  [" + currDiag.getHumanType() + "]";
		}
		
		// sort the diagram names for better UI
		Arrays.sort(diagramNames, String.CASE_INSENSITIVE_ORDER);
		String[] sortedNames = Arrays.copyOfRange(diagramNames, 0, diagramNames.length);
		
		String userInput;
		try {
			userInput = (String) JOptionPane.showInputDialog(null,
										                     "Choose a diagram to get a pattern from:",
										                     null,
										                     JOptionPane.DEFAULT_OPTION,
										                     null,
										                     sortedNames,
										                     sortedNames[0]);
		} catch(HeadlessException e) {
			Application.getInstance().getGUILog().log("The Pattern Loader must be run in a graphical interface");
			return null;
		}
		
		// user cancelled input - return safely
		if(userInput == null) {
			return null;
		}
		
		// find and return the pattern diagram
		diagIter = diagCollection.iterator();
		while(diagIter.hasNext()) {
			DiagramPresentationElement currDiag = diagIter.next();
			
			String currDiagDescriptor = currDiag.getName() + "  [" + currDiag.getHumanType() + "]";
			
			if(currDiagDescriptor.equals(userInput)) {
				return currDiag;
			}
		}
		
		Application.getInstance().getGUILog().log("There was a problem starting the Pattern Loader. The Pattern Loader is now exiting.");
		return null;
	}
	
	/**
	 * Loads the style of elements on the diagram by gathering relevant style information from the JSONObject.
	 * 
	 * @param elemList	the list of elements to load styles into.
	 * @param pattern	the pattern to load.
	 */
	public static void loadPattern(List<PresentationElement> elemList, JSONObject pattern, ProgressStatus progressStatus) {
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
			
			if(progressStatus != null) {
				progressStatus.increase();
			}
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
		loadPattern(children, pattern, null);
	}
}