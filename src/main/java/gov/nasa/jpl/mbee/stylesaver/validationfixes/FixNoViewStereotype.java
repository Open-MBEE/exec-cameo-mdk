package gov.nasa.jpl.mbee.stylesaver.fixes;

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;


import gov.nasa.jpl.mbee.stylesaver.StyleSaverUtils;

import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.JOptionPane;

/**
 * Class for fixing a diagram not stereotyped view. Simply adds the stereotype
 * view to the diagram.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class FixNoViewStereotype extends NMAction implements AnnotationAction {
	private static final long serialVersionUID = 1L;
	private DiagramPresentationElement diagToFix;

	/**
	 * Initializes this instance and adds a description to the fix.
	 * 
	 * @param diag the diagram to fix.
	 */
    public FixNoViewStereotype(DiagramPresentationElement diag) {
        super("FIX_NO_VIEW_STEREOTYPE", "Fix View Not Set: Add a stereotype that supports saving/loading styles to this diagram", 0);
        
        diagToFix = diag;
    }

    /**
     * Executes the action.
     *
     * @param e event caused execution.
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        SessionManager sm = SessionManager.getInstance();
        
        sm.createSession("Applying stereotype to diagram");
        applyView();
        sm.closeSession();
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
        
        SessionManager sm = SessionManager.getInstance();
        
        sm.createSession("Applying stereotype to diagram");
        applyView();
        sm.closeSession();
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
     * Applies the view stereotype to the diagram.
     */
    private void applyView() {
        Project project = Application.getInstance().getProject();
        
        // add the view stereotype to the diagram
    	StereotypesHelper.addStereotype(diagToFix.getElement(), StyleSaverUtils.getWorkingStereotype(project));
		
		JOptionPane.showMessageDialog(null, "Stereotype added.", "Info", JOptionPane.INFORMATION_MESSAGE);
    }
}
