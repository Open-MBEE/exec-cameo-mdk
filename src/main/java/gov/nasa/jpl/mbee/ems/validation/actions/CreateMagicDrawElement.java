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

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.ImportUtility;
import gov.nasa.jpl.mbee.ems.sync.AutoSyncCommitListener;
import gov.nasa.jpl.mbee.ems.sync.ProjectListenerMapping;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class CreateMagicDrawElement extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    private static final long serialVersionUID = 1L;
    private JSONObject ob;
    private Map<String, JSONObject> elementsKeyed;

    public CreateMagicDrawElement(JSONObject ob, Map<String, JSONObject> elementsKeyed) {
        super("CreateMagicDrawElement", "Create MagicDraw element", null, null);
        this.ob = ob;
        this.elementsKeyed = elementsKeyed;
    }

    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(Collection<Annotation> annos) {
        Project project = Application.getInstance().getProject();
        Map<String, ?> projectInstances = ProjectListenerMapping.getInstance().get(project);
        AutoSyncCommitListener listener = (AutoSyncCommitListener)projectInstances.get("AutoSyncCommitListener");
        if (listener != null)
            listener.disable();
        SessionManager.getInstance().createSession("create elements");
        Collection<Annotation> toremove = new HashSet<Annotation>();
        try {
            List<JSONObject> tocreate = new ArrayList<JSONObject>();
            for (Annotation anno: annos) {
                String message = anno.getText();
                String[] mes = message.split("'");
                String eid = null;
                if (mes.length > 2)
                    eid = mes[1];
                if (eid != null) {
                    JSONObject newe = elementsKeyed.get(eid);
                    if (newe != null) {
                        tocreate.add(newe);
                        /*Element newElement = ImportUtility.createElement(newe);
                        if (newElement != null)
                            toremove.add(anno);
                        else {
                            Application.getInstance().getGUILog().log("[ERROR] Cannot create element " + eid + " (id already exists or owner not found)");
                        }*/
                    }
                }
            }
            tocreate = ImportUtility.getCreationOrder(tocreate);
            if (tocreate == null) {
                Application.getInstance().getGUILog().log("[ERROR] Cannot create elements (id already exists or owner(s) not found)");
            } else {
                for (JSONObject newe: tocreate) {
                    Element newElement = ImportUtility.createElement(newe);
                    if (newElement == null) {
                        Application.getInstance().getGUILog().log("[ERROR] Cannot create element " + newe.get("sysmlid") + " (owner not found)");
                    }
                }
                saySuccess();
                this.removeViolationsAndUpdateWindow(toremove);
            }
            SessionManager.getInstance().closeSession();
            
        } catch (Exception ex) {
            SessionManager.getInstance().cancelSession();
            Utils.printException(ex);
        } finally {
            if (listener != null)
                listener.enable();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent e) {		
        Project project = Application.getInstance().getProject();
        Map<String, ?> projectInstances = ProjectListenerMapping.getInstance().get(project);
        AutoSyncCommitListener listener = (AutoSyncCommitListener)projectInstances.get("AutoSyncCommitListener");
        if (listener != null)
            listener.disable();
        SessionManager.getInstance().createSession("Create Element");
        try {
            Element magicDrawElement = ImportUtility.createElement(ob); 
            if (magicDrawElement == null) {
                SessionManager.getInstance().cancelSession();
                Application.getInstance().getGUILog().log("[ERROR] Element not created (id already exists or owner not found)");
                return;
            }
            SessionManager.getInstance().closeSession();
            this.removeViolationAndUpdateWindow();
            saySuccess();
        } catch (Exception ex) {
            SessionManager.getInstance().cancelSession();
            Utils.printException(ex);
        } finally {
            if (listener != null)
                listener.enable();
        }
	}
}
