/*******************************************************************************
 * Copyright (c) <2013>, California Institute of Technology ("Caltech").  
 * U.S. Government sponsorship acknowledged.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are 
 * permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice, this list of 
 *    conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice, this list 
 *    of conditions and the following disclaimer in the documentation and/or other materials 
 *    provided with the distribution.
 *  - Neither the name of Caltech nor its operating division, the Jet Propulsion Laboratory, 
 *    nor the names of its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS 
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY 
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER  
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package gov.nasa.jpl.mbee.mdk.patternloader;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;

import java.util.List;

/**
 * This class contains a run method for the pattern load operation. Updates
 * progress bar dynamically.
 *
 * @author Benjamin Inada, JPL/Caltech
 */
public class RunnablePatternLoaderWithProgress implements RunnableWithProgress {
    private Project proj;
    private DiagramPresentationElement patternDiagram;
    private List<PresentationElement> targetElements;
    private boolean success;

    /**
     * @param proj           the project that the diagrams reside in.
     * @param patternDiagram the pattern diagram to load styles from.
     * @param targetElements the list of elements on the target diagram to load styles
     *                       into.
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
     * @param progressStatus the status of the operation so far.
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
