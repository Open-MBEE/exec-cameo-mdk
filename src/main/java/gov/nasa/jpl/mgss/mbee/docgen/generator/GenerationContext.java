package gov.nasa.jpl.mgss.mbee.docgen.generator;

import java.util.List;
import java.util.Stack;

import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

/**
 * <p>A more OO way to pass around targets, current node, and whatever other variables might
 * be added to track generation in DocumentGenerator, which has been refactored to be more
 * modular.</p>
 * @author bcompane
 *
 */
public class GenerationContext {
	
	private Stack<List<Element>> targets;
	private ActivityNode current;
    private DocumentValidator validator;
	private GUILog log;
	
	public GenerationContext(Stack<List<Element>> t, ActivityNode a,
	                         DocumentValidator dv, GUILog l) {
		targets = t;
		current = a;
        validator = dv;
		log = l;
	}
	
    public void pushTargets(List<Element> t) {
		targets.push(t);
	}
	
	public List<Element> peekTargets() {
		return targets.peek();
	}
	
	public List<Element> popTargets() {
		return targets.pop();
	}
	
	public boolean targetsEmpty() {
		return targets.isEmpty();
	}
	
	public Stack<List<Element>> getTargets() {
		return targets;
	}

	public void setCurrentNode(ActivityNode a) {
		current = a;
	}
	
	public ActivityNode getCurrentNode() {
		return current;
	}
	
    public DocumentValidator getValidator() {
        return validator;
    }

	public void log(String msg) {
		log.log(msg);
	}
}
