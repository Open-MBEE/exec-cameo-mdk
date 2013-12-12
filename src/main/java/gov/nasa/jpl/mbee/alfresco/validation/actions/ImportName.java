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
package gov.nasa.jpl.mbee.alfresco.validation.actions;

import gov.nasa.jpl.mbee.alfresco.validation.ResultHolder;

import java.awt.event.ActionEvent;
import java.util.Collection;

import org.json.simple.JSONObject;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

public class ImportName extends MDAction implements AnnotationAction {

    private static final long serialVersionUID = 1L;
    private NamedElement element;
    private String name;
    
    public ImportName(NamedElement e, String name) {
        super("ImportName", "Import name", null, null);
        this.element = e;
        this.name = name;
    }
    
    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @Override
    public void execute(Collection<Annotation> annos) {
        JSONObject result = ResultHolder.lastResults;
        SessionManager.getInstance().createSession("Change Names");
        try {
            for (Annotation anno: annos) {
                Element e = (Element)anno.getTarget();
                if (!e.isEditable()) {
                    continue;
                }
                String resultName = (String)((JSONObject)((JSONObject)result.get("elements")).get(e.getID())).get("name");
                if (resultName == null)
                    continue;
                ((NamedElement)e).setName(resultName);
            }
            SessionManager.getInstance().closeSession();
        } catch (Exception ex) {
            SessionManager.getInstance().cancelSession();
        }
        
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SessionManager.getInstance().createSession("Change Name");
        try {
            if (element.isEditable())
                element.setName(name);
            else
                Application.getInstance().getGUILog().log("[ERROR] " + element.getQualifiedName() + " is not editable!");
            SessionManager.getInstance().closeSession();
        } catch (Exception ex) {
            SessionManager.getInstance().cancelSession();
        }
    }
}
