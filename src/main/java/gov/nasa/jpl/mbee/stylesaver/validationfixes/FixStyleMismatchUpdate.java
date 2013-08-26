package gov.nasa.jpl.mbee.stylesaver.validationfixes;

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;


import gov.nasa.jpl.mbee.stylesaver.ViewSaver;

import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.JOptionPane;

/**
 * Class for fixing a mismatch between the view style tag and the styling currently
 * on the active diagram. Updates the view style tag.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class FixStyleMismatchUpdate extends NMAction implements AnnotationAction {
	private static final long serialVersionUID = 1L;
	private DiagramPresentationElement diagToFix;

	/**
	 * Initializes this instance and adds a description to the fix.
	 * 
	 * @param diag the diagram to fix.
	 */
    public FixStyleMismatchUpdate(DiagramPresentationElement diag) {
        super("FIX_STYLE_MISMATCH_UPDATE", "Fix Style Mismatch: Save the current styling on this diagram", 0);

        this.diagToFix = diag;
    }

    /**
     * Executes the action.
     *
     * @param e event caused execution.
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        performSave();
    }

    /**
     * Executes the action on specified targets.
     *
     * @param annotations action targets.
     */
    @Override
	public void execute(Collection<Annotation> annotations) {
        if(annotations == null || annotations.isEmpty()) {
            return;
        }
        
        performSave();
    }

    /**
     * Checks if possible to execute action together on all specified annotations.
     * 
     * @param annotations target annotations.
     * @return true if the action can be executed.
     */
    @Override
	public boolean canExecute(Collection<Annotation> annotations) {
        return true;
    }
    
    /**
     * Performs the actual save on the diagram. 
     */
    private void performSave() {
    	SessionManager.getInstance().createSession("Saving...");
    	
        Project project = Application.getInstance().getProject();
        
        String JSONStr = ViewSaver.save(project, this.diagToFix, false);
        
		if(JSONStr != null) {
			SessionManager.getInstance().closeSession();
			JOptionPane.showMessageDialog(null, "Save complete.", "Info", JOptionPane.INFORMATION_MESSAGE);
		} else {
			SessionManager.getInstance().cancelSession();
			JOptionPane.showMessageDialog(null, "Save cancelled.", "Info", JOptionPane.INFORMATION_MESSAGE);
		}
    }
}