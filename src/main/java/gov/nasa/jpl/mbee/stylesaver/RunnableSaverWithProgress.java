package gov.nasa.jpl.mbee.stylesaver;

import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import org.json.simple.JSONValue;

import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

/**
 * Nested class contains a run method for the save operation.
 * Updates progress bar dynamically.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class RunnableSaverWithProgress implements RunnableWithProgress {
	private Map mainStore;
	private List<PresentationElement> elemList;
	private String styleString;
	private Element diagram;
	private Stereotype workingStereotype;
	private boolean success;
	
	/**
	 * @param mainStore2 the main style store for this diagram.
	 * @param elemList	the elements to save.
	 */
	public RunnableSaverWithProgress(Map mainStore2, List<PresentationElement> elemList, Element diagram, Stereotype workingStereotype) {
		this.mainStore = mainStore2;
		this.elemList = elemList;
		this.diagram = diagram;
		this.workingStereotype = workingStereotype;
	}
	
	/**
	 * Runs the save operation.
	 * 
	 * @param progressStatus the status of the operation so far.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void run(ProgressStatus progressStatus) {
		progressStatus.init("Saving styles...", 0, elemList.size() + 1);
		
		for(PresentationElement elem : elemList) {
			if(progressStatus.isCancel()) {
				success = false;
				return;
			}
			
       		// save the element's style properties
			try {
    			String styleStr = ViewSaver.getStyle(elem);
    			
    			// if there is no style to save, continue to next element
    			if(styleStr == null) {
    				progressStatus.increase();
    				continue;
    			}
    			
    			mainStore.put(elem.getID(), styleStr);
			} catch(ClassCastException e) {
				e.printStackTrace();
			} catch(MissingResourceException e) {
				e.printStackTrace();
			}
			
       		// recursively save child elements' style properties (e.g. ports)
			ViewSaver.getStyleChildren(elem, mainStore, progressStatus);
       		
       		progressStatus.increase();
       	}
		
		// convert to JSON - this takes a while
		styleString = JSONValue.toJSONString(mainStore);
		
		if(progressStatus.isCancel()) {
			success = false;
			return;
		}
		
		// set the style string into the view "style" tag
		StereotypesHelper.setStereotypePropertyValue(diagram, workingStereotype, "style", styleString);
		
		progressStatus.increase();
		success = true;
	}
	
	/**
	 * Gets the value of the success property.
	 * 
	 * @return the value of the success property.
	 */
	public boolean getSuccess() {
		return success;
	}
	
	/**
	 * Gets the value of the styleString property.
	 * 
	 * @return the value of the styleString property.
	 */
	public String getStyleString() {
		return styleString;
	}
}		