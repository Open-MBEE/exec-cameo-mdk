package gov.nasa.jpl.mbee.patternloader;

import java.awt.event.ActionEvent;
import java.util.Collection;

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;

/**
 * Class for fixing a mismatch between a diagram and its corresponding pattern.
 * The user selects element types to sync with the pattern in this fix.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class FixPatternMismatchSelect extends NMAction implements AnnotationAction {
	private static final long serialVersionUID = 1L;

	public FixPatternMismatchSelect(String paramString1, String paramString2, int paramInt) {
		super(paramString1, paramString2, paramInt);
	}

	@Override
	public void execute(Collection<Annotation> paramCollection) {
	}

	@Override
	public boolean canExecute(Collection<Annotation> paramCollection) {
		return false;
	}

	@Override
	public void actionPerformed(ActionEvent paramActionEvent) {
	}
}