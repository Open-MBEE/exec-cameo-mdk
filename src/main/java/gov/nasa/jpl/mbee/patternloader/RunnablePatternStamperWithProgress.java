package gov.nasa.jpl.mbee.patternloader;

import java.util.List;

import com.nomagic.magicdraw.copypaste.CopyPasting;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;

/**
 * This class contains a run method for the stamp pattern operation.
 * Updates progress bar dynamically.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class RunnablePatternStamperWithProgress implements RunnableWithProgress {
	private DiagramPresentationElement targetDiagram;
	private List<PresentationElement> patternElements;
	private boolean success;
	
	/**
	 * @param targetDiagram		the target diagram to stamp onto.
	 * @param patternElements	the elements to clone and stamp.
	 */
	public RunnablePatternStamperWithProgress(DiagramPresentationElement targetDiagram, List<PresentationElement> patternElements) {
		this.targetDiagram = targetDiagram;
		this.patternElements = patternElements;
	}
	
	/**
	 * Runs the pattern stamp operation.
	 * 
	 * @param progressStatus the status of the operation so far.
	 */
	@Override
	public void run(ProgressStatus progressStatus) {
		try {
			progressStatus.init("Stamping pattern...", 0, 100);
			CopyPasting.copyPasteElements(patternElements, targetDiagram.getObjectParent(), targetDiagram, true, false);
			targetDiagram.getDiagramSurface().repaint();
		} catch(Exception e) {
			success = false;
		}
		
		success = true;
	}

	/**
	 * Gets the value of the success property.
	 * 
	 * @return the value of the success property.
	 */
	public boolean getSuccess() {
		return success;
	}
}
