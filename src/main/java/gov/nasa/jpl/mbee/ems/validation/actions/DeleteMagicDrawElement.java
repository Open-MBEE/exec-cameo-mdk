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
package gov.nasa.jpl.mbee.ems.validation.actions;

import gov.nasa.jpl.mbee.ems.sync.AutoSyncCommitListener;
import gov.nasa.jpl.mbee.ems.sync.ProjectListenerMapping;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

public class DeleteMagicDrawElement extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    private static final long serialVersionUID = 1L;
    private Element element;

    public DeleteMagicDrawElement(Element e) {
        super("DeleteElement", "Delete MagicDraw element", null, null);
        this.element = e;
    }

    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @Override
    public void execute(Collection<Annotation> annos) {
        Project project = Application.getInstance().getProject();
        Map<String, ?> projectInstances = ProjectListenerMapping.getInstance().get(project);
        AutoSyncCommitListener listener = (AutoSyncCommitListener)projectInstances.get("AutoSyncCommitListener");
        if (listener != null)
            listener.disable();
        boolean noneditable = false;
        Collection<Annotation> toremove = new HashSet<Annotation>();
        if (!SessionManager.getInstance().isSessionCreated()) {
            SessionManager.getInstance().createSession("Delete MagicDraw Elements");
            for (Annotation anno : annos) {
                Element e = (Element) anno.getTarget();
                try {
                    ModelElementsManager.getInstance().removeElement(e);
                    toremove.add(anno);
                } catch (Exception ex) {
                    Utils.printException(ex);
                    noneditable = true;
                }
            }
            SessionManager.getInstance().closeSession();
            saySuccess();
            this.removeViolationsAndUpdateWindow(toremove);
        } else {
            for (Annotation anno : annos) {
                Element e = (Element) anno.getTarget();
                try {
                    ModelElementsManager.getInstance().removeElement(e);
                    toremove.add(anno);
                } catch (Exception ex) {
                    Utils.printException(ex);
                    noneditable = true;
                }
            }
        }
        if (noneditable) {
            Application.getInstance().getGUILog().log("[ERROR] There were some elements that're not editable and not deleted");
        } else
            saySuccess();
        this.removeViolationsAndUpdateWindow(toremove);
        if (listener != null)
            listener.enable();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Project project = Application.getInstance().getProject();
        Map<String, ?> projectInstances = ProjectListenerMapping.getInstance().get(project);
        AutoSyncCommitListener listener = (AutoSyncCommitListener)projectInstances.get("AutoSyncCommitListener");
        if (listener != null)
            listener.disable();
        if (!SessionManager.getInstance().isSessionCreated()) {
            SessionManager.getInstance().createSession("Delete MagicDraw Element");
            try {
                ModelElementsManager.getInstance().removeElement(element);
                SessionManager.getInstance().closeSession();
                saySuccess();
                this.removeViolationAndUpdateWindow();
            } catch (Exception ex) {
                Utils.printException(ex);
                SessionManager.getInstance().cancelSession();
            }
        } else {
            try {
                ModelElementsManager.getInstance().removeElement(element);
                saySuccess();
                this.removeViolationAndUpdateWindow();
            } catch (Exception ex) {
                Utils.printException(ex);
            }
        }
        if (listener != null)
            listener.enable();
    }
}
