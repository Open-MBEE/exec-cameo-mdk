package gov.nasa.jpl.mbee.patternloader;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.List;

import org.json.simple.JSONObject;

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;

/**
 * Class for fixing a mismatch between a diagram and its corresponding pattern.
 * All elements on the diagram are synced with the pattern in this fix.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class FixPatternMismatchAll extends NMAction implements AnnotationAction {
	private static final long serialVersionUID = 1L;
	private DiagramPresentationElement diagToFix;
	private JSONObject pattern;

	/**
	 * Initializes this instance and adds a description to the fix.
	 * 
	 * @param diag		the diagram to fix.
	 * @param pattern	the pattern to load.
	 */
	public FixPatternMismatchAll(DiagramPresentationElement diag, JSONObject pattern) {
		super("FIX_PATTERN_MISMATCH_ALL", "Fix Pattern Mismatch: Load pattern onto diagram", 0);
		
		this.diagToFix = diag;
		this.pattern = pattern;
	}
	
    /**
     * Executes the action on specified targets.
     *
     * @param annotations action targets.
     */
	@Override
	public void execute(Collection<Annotation> paramCollection) {
	}
	
    /**
     * Checks if possible to execute action together on all specified annotations.
     * 
     * @param annotations target annotations.
     * @return true if the action can be executed.
     */
	@Override
	public boolean canExecute(Collection<Annotation> paramCollection) {
		return false;
	}
	
    /**
     * Executes the action.
     *
     * @param e event caused execution.
     */
	@Override
	public void actionPerformed(ActionEvent paramActionEvent) {
		SessionManager.getInstance().createSession("Fixing mismatch");
		syncAll();
		SessionManager.getInstance().closeSession();
	}
	
	/**
	 * Performs a sync on all elements on the diagram.
	 */
	private void syncAll() {
		List<PresentationElement> elemsToFix = diagToFix.getPresentationElements();
		PatternLoader.loadPattern(elemsToFix, pattern, null);
	}
}