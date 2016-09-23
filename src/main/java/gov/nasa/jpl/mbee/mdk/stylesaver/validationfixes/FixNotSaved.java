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
package gov.nasa.jpl.mbee.mdk.stylesaver.validationfixes;

import com.nomagic.actions.NMAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.magicdraw.ui.EnvironmentLockManager;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import gov.nasa.jpl.mbee.mdk.stylesaver.StyleSaverUtils;
import gov.nasa.jpl.mbee.mdk.stylesaver.ViewSaver;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collection;

/**
 * Class for fixing a mismatch between the view style tag and the styling
 * currently on the active diagram. Updates the view style tag.
 *
 * @author Benjamin Inada, JPL/Caltech
 */
public class FixNotSaved extends NMAction implements AnnotationAction {
    private static final long serialVersionUID = 1L;
    private DiagramPresentationElement diagToFix;

    /**
     * Initializes this instance and adds a description to the fix.
     *
     * @param diag the diagram to fix.
     */
    public FixNotSaved(DiagramPresentationElement diag) {
        super("FIX_NOT_SAVED", "Fix Not Saved: Save current styling on diagram", 0);

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
        if (annotations == null || annotations.isEmpty()) {
            return;
        }

        performSave();
    }

    /**
     * Checks if possible to execute action together on all specified
     * annotations.
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
        boolean wasLocked = EnvironmentLockManager.isLocked();
        try {
            EnvironmentLockManager.setLocked(true);

            SessionManager.getInstance().createSession("Saving...");

            Project project = Application.getInstance().getProject();

            // ensure the diagram is locked for edit
            if (!StyleSaverUtils.isDiagramLocked(project, diagToFix.getElement())) {
                SessionManager.getInstance().cancelSession();
                JOptionPane.showMessageDialog(null,
                        "This diagram is not locked for edit. Lock it before running this function.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String JSONStr = ViewSaver.save(project, diagToFix, false);

            if (JSONStr != null) {
                SessionManager.getInstance().closeSession();
                JOptionPane
                        .showMessageDialog(null, "Save complete.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
            else {
                SessionManager.getInstance().cancelSession();
                JOptionPane.showMessageDialog(null, "Save cancelled.", "Info",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } finally {
            EnvironmentLockManager.setLocked(wasLocked);
        }
    }
}
