package gov.nasa.jpl.mbee.patternloader;

import com.nomagic.actions.AMConfigurator;
import com.nomagic.actions.ActionsCategory;
import com.nomagic.actions.ActionsManager;
import com.nomagic.magicdraw.actions.DiagramContextAMConfigurator;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;

/**
 * A class for configurating the Pattern Loader right-click menu option.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class PatternLoaderConfigurator implements DiagramContextAMConfigurator {
	/**
	 * Configures the element right-click menu option.
	 * 
	 * @param manager	the actions manager to add the category to.
	 * @param diagram	the diagram to configurate.
	 * @param selected	the selected elements.
	 * @param requestor	the element that was right-clicked
	 */
	public void configure(ActionsManager manager, DiagramPresentationElement diagram, PresentationElement[] selected, PresentationElement requestor) {
		// check to see if the category was already added to the manager
		if(manager.getActionFor("PatternLoader") == null) {
			if(requestor == null) {
				return;
			}
			
			if(!PatternLoaderUtils.isGoodRequestor(requestor)) {
				return;
			}
			
			ActionsCategory category = new ActionsCategory("Pattern Loader", "Pattern Loader");
	
			category.setNested(true);
			category.addAction(new PatternLoader("PatternLoader", "Load pattern...", 0, null, requestor));
			
			manager.addCategory(1, category);
		}
	}
	
	/**
	 * Gets the priority of the configurator.
	 * 
	 * @return the priority.
	 */
	public int getPriority() {
		return AMConfigurator.MEDIUM_PRIORITY;
	}
}