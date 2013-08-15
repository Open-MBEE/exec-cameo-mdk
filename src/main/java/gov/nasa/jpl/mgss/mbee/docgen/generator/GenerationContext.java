package gov.nasa.jpl.mgss.mbee.docgen.generator;

import java.util.List;
import java.util.Stack;

import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class GenerationContext {
	
	private Stack<List<Element>> targets;
	private ActivityNode current;
	private GUILog log;
	
	public GenerationContext(Stack<List<Element>> t, ActivityNode a, GUILog l) {
		targets = t;
		current = a;
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
	
	public void log(String msg) {
		log.log(msg);
	}
}
