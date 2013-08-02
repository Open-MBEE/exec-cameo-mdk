package gov.nasa.jpl.mbee.stylesaver;

import java.awt.event.ActionEvent;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * Utility class for adding menu actions for the Saver/Loader.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class DiagramUtils {
	static boolean justForViewDiagrams = true;

	/**
	 * Adds the save menu action.
	 * 
	 * @param event		the firing event.
	 * @param element	the applicable element.
	 */
	public static void addSave(ActionEvent event, Element element) {
		if(justForViewDiagrams) {
			ViewSaver vs = new ViewSaver(String.valueOf(event.getID()), "Save styling on diagram", 0, null, false);
			vs.actionPerformed(event);
		} else {
			Saver s = new Saver(null, "Save styling on diagram", 0, null);
			s.actionPerformed(event);
		}
	}
	
	/**
	 * Adds the load menu action.
	 * 
	 * @param event		the firing event.
	 * @param element	the applicable element.
	 */
	public static void addLoad(ActionEvent event, Element element) {
		// add the save and load options to the menu
		if(justForViewDiagrams) {
			ViewLoader vl = new ViewLoader(String.valueOf(event.getID()), "Load saved styling onto diagram", 0, null);
			vl.actionPerformed(event);
		} else {
			Loader l = new Loader(null, "Load saved styling onto diagram", 0, null);
			l.actionPerformed(event);
		}
	}
}