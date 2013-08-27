package gov.nasa.jpl.mbee.stylesaver.validationfixes;

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.ui.BaseProgressMonitor;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;


import gov.nasa.jpl.mbee.stylesaver.RunnableLoaderWithProgress;
import gov.nasa.jpl.mbee.stylesaver.StyleSaverUtils;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.List;

import javax.swing.JOptionPane;

import org.json.simple.JSONObject;

/**
 * Class for fixing a mismatch between the view style tag and the styling currently
 * on the active diagram. Restores the active diagram with styling from the view style tag.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class FixStyleMismatchRestore extends NMAction implements AnnotationAction {
	private static final long serialVersionUID = 1L;
	private DiagramPresentationElement diagToFix;

	/**
	 * Initializes this instance and adds a description to the fix.
	 * 
	 * @param diag the diagram to fix.
	 */
    public FixStyleMismatchRestore(DiagramPresentationElement diag) {
        super("FIX_STYLE_MISMATCH_RESTORE", "Fix Style Mismatch: Load styling from previous save to diagram", 0);
        
        this.diagToFix = diag;
    }

    /**
     * Executes the action.
     *
     * @param e event caused execution.
     */
    @Override
	public void actionPerformed(ActionEvent e) {
        performLoad();
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
        
        performLoad();
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
     * Performs the actual load on the diagram.
     */
    private void performLoad() {
    	SessionManager.getInstance().createSession("Loading...");
    	
        Project project = Application.getInstance().getProject();
        
    	// ensure the diagram is locked for edit
    	if(!StyleSaverUtils.isDiagramLocked(project, diagToFix.getElement())) {
    		SessionManager.getInstance().cancelSession();
			JOptionPane.showMessageDialog(null, "This diagram is not locked for edit. Lock it before running this function.", "Error", JOptionPane.ERROR_MESSAGE);
    		return;
    	}
    	
    	// get the main style string from the view stereotype tag "style"
    	Object tag = StereotypesHelper.getStereotypePropertyFirst(this.diagToFix.getElement(), StyleSaverUtils.getWorkingStereotype(project), "style");
    	String styleStr = StereotypesHelper.getStereotypePropertyStringValue(tag);
    	
		JSONObject style = StyleSaverUtils.parse(styleStr); 
		
    	// get the elements on the diagram to load styles into
    	List<PresentationElement> list = this.diagToFix.getPresentationElements();
    	
    	// run the loader with a progress bar
		RunnableLoaderWithProgress runnable = new RunnableLoaderWithProgress(list, style);
		BaseProgressMonitor.executeWithProgress(runnable, "Load Progress", true);
		
		if(runnable.getSuccess()) {
			SessionManager.getInstance().closeSession();
			JOptionPane.showMessageDialog(null, "Load complete.", "Info", JOptionPane.INFORMATION_MESSAGE);
		} else {
			SessionManager.getInstance().cancelSession();
			JOptionPane.showMessageDialog(null, "Load cancelled.", "Info", JOptionPane.INFORMATION_MESSAGE);
		}
    }
}
