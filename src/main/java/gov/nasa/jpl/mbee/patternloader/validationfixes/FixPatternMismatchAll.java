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
package gov.nasa.jpl.mbee.patternloader.validationfixes;

import gov.nasa.jpl.mbee.patternloader.PatternLoader;

import java.awt.event.ActionEvent;
import java.util.Collection;

import org.json.simple.JSONObject;

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.ui.EnvironmentLockManager;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;

/**
 * Class for fixing a mismatch between a diagram and its corresponding pattern.
 * All elements on the diagram are synced with the pattern in this fix.
 * 
 * @author Benjamin Inada, JPL/Caltech
 */
public class FixPatternMismatchAll extends NMAction implements AnnotationAction {
    private static final long          serialVersionUID = 1L;
    private DiagramPresentationElement diagToFix;

    /**
     * Initializes this instance and adds a description to the fix.
     * 
     * @param diag
     *            the diagram to fix.
     * @param pattern
     *            the pattern to load.
     */
    public FixPatternMismatchAll(DiagramPresentationElement diag, JSONObject pattern) {
        super("FIX_PATTERN_MISMATCH_ALL", "Fix Pattern Mismatch: Automatically load pattern onto diagram", 0);

        this.diagToFix = diag;
    }

    /**
     * Executes the action on specified targets.
     * 
     * @param annotations
     *            action targets.
     */
    @Override
    public void execute(Collection<Annotation> paramCollection) {
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
    public boolean canExecute(Collection<Annotation> paramCollection) {
        return false;
    }

    /**
     * Executes the action.
     * 
     * @param e
     *            event caused execution.
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
        boolean wasLocked = EnvironmentLockManager.isLocked();
        try {
            EnvironmentLockManager.setLocked(true);

            PatternLoader pl = new PatternLoader(null, null, 0, null, diagToFix);
            pl.prepAndRun(true);
        } finally {
            EnvironmentLockManager.setLocked(wasLocked);
        }
    }
}
