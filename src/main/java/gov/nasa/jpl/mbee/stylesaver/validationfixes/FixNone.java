package gov.nasa.jpl.mbee.stylesaver.validationfixes;

import java.awt.event.ActionEvent;
import java.util.Collection;

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;

/**
 * Class for providing a separator action in the validation menu that does
 * nothing. Not the prettiest, but it works.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class FixNone extends NMAction implements AnnotationAction {
    private static final long serialVersionUID = 1L;

    /**
     * Initializes this instance and adds a description to the fix.
     * 
     * @param diag
     *            the diagram to fix.
     */
    public FixNone(DiagramPresentationElement diag) {
        super("FIX_NONE", "", 0);
    }

    /**
     * Executes the action.
     * 
     * @param e
     *            event caused execution.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // do nothing, this is just used as a separator in menu
    }

    /**
     * Executes the action on specified targets.
     * 
     * @param annotations
     *            action targets.
     */
    @Override
    public void execute(Collection<Annotation> annotations) {
        // do nothing
    }

    /**
     * Checks if possible to execute action together on all specified
     * annotations.
     * 
     * @param annotations
     *            target annotations.
     * @return true if the action can be executed.
     */
    @Override
    public boolean canExecute(Collection<Annotation> annotations) {
        return true;
    }
}
