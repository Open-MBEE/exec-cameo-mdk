package gov.nasa.jpl.mbee.mdk.generator;

import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.magicdraw.activities.mdfundamentalactivities.ActivityNode;
import gov.nasa.jpl.mbee.mdk.docgen.ViewViewpointValidator;

import java.util.List;
import java.util.Stack;

/**
 * <p>
 * A more OO way to pass around targets, current node, and whatever other
 * variables might be added to track generation in DocumentGenerator, which has
 * been refactored to be more modular.
 * </p>
 *
 * @author bcompane
 */
public class GenerationContext {

    private Stack<List<Object>> targets;
    private ActivityNode current;
    private ViewViewpointValidator validator;
    private GUILog log;

    public GenerationContext(Stack<List<Object>> t, ActivityNode a, ViewViewpointValidator dv, GUILog l) {
        targets = t;
        current = a;
        validator = dv;
        log = l;
    }

    public GenerationContext(Stack<List<Object>> t, ActivityNode a, GUILog l) {
        targets = t;
        current = a;
        validator = null;
        log = l;
    }

    public void pushTargets(List<Object> t) {
        targets.push(t);
    }

    public List<Object> peekTargets() {
        return targets.peek();
    }

    public List<Object> popTargets() {
        return targets.pop();
    }

    public boolean targetsEmpty() {
        return targets.isEmpty();
    }

    public Stack<List<Object>> getTargets() {
        return targets;
    }

    public void setCurrentNode(ActivityNode a) {
        current = a;
    }

    public ActivityNode getCurrentNode() {
        return current;
    }

    public ViewViewpointValidator getValidator() {
        return validator;
    }

    public void setValidator(ViewViewpointValidator validator) {
        this.validator = validator;
    }

    public void log(String msg) {
        log.log(msg);
    }
}
