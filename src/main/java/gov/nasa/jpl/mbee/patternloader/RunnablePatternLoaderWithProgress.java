package gov.nasa.jpl.mbee.patternloader;

import java.util.List;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;

/**
 * This class contains a run method for the pattern load operation. Updates
 * progress bar dynamically.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class RunnablePatternLoaderWithProgress implements RunnableWithProgress {
    private Project                    proj;
    private DiagramPresentationElement patternDiagram;
    private List<PresentationElement>  targetElements;
    private boolean                    success;

    /**
     * @param proj
     *            the project that the diagrams reside in.
     * @param patternDiagram
     *            the pattern diagram to load styles from.
     * @param targetElements
     *            the list of elements on the target diagram to load styles
     *            into.
     */
    public RunnablePatternLoaderWithProgress(Project proj, DiagramPresentationElement patternDiagram,
            List<PresentationElement> targetElements) {
        this.proj = proj;
        this.patternDiagram = patternDiagram;
        this.targetElements = targetElements;
    }

    /**
     * Runs the pattern load operation
     * 
     * @param progressStatus
     *            the status of the operation so far.
     */
    @Override
    public void run(ProgressStatus progressStatus) {
        try {
            progressStatus.init("Loading pattern...", 0, targetElements.size());

            // save the pattern in the pattern diagram
            PatternSaver ps = new PatternSaver();
            ps.savePattern(proj, patternDiagram);

            // load the pattern in the active diagram
            PatternLoader.loadPattern(targetElements, ps.getPattern(), progressStatus);
        } catch (Exception e) {
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
