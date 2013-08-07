package gov.nasa.jpl.mbee.patternloader;

import java.awt.event.ActionEvent;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * Utility class for adding menu actions for the Pattern Loader.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class PatternLoaderMenuUtils {
	/**
	 * Adds the "Load pattern..." menu action.
	 * 
	 * @param event		the firing event.
	 * @param element	the applicable element.
	 */
	public static void addLoad(ActionEvent event, Element element) {
		PatternLoader s = new PatternLoader(null, "Load pattern...", 0, null);
		s.actionPerformed(event);
	}
}