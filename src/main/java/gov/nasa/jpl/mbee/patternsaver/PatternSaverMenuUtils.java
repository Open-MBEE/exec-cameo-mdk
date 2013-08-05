package gov.nasa.jpl.mbee.patternsaver;

import java.awt.event.ActionEvent;

import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class PatternSaverMenuUtils {
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