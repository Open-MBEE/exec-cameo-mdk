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
package gov.nasa.jpl.mbee.actions;

import gov.nasa.jpl.mbee.dgview.Paragraph;
import gov.nasa.jpl.mbee.dgview.ViewElement;
import gov.nasa.jpl.mbee.model.UserScript;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.m2m.qvt.oml.ModelExtent;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.GUILog;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;

@SuppressWarnings("serial")
public class RunUserScriptAction extends MDAction {
    private UserScript         scripti;
    public static final String actionid = "RunUserScript";

    public RunUserScriptAction(UserScript e) {
        super(null, "Run User Script", null, null);
        scripti = e;
        String name = e.getStereotypeName();
        if (name != null)
            this.setName("Run " + name);
    }

    public RunUserScriptAction(UserScript e, boolean useid) {
        super(actionid, "Run User Script", null, null);
        scripti = e;
        String name = e.getStereotypeName();
        if (name != null)
            this.setName("Run " + name);
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        GUILog log = Application.getInstance().getGUILog();
        Map<?, ?> o = scripti.getScriptOutput(null);
        if (o != null) {
            log.log("output from script: " + o.toString());
            /*
             * for (Object key: o.keySet()) { try { log.log("key: " +
             * key.toString() + " value: " + o.get(key).toString()); } catch
             * (Exception e) {
             * 
             * }
             * 
             * }
             */
            if (o.containsKey("docgenOutput")) {
                Object result = o.get("docgenOutput");
                if (result instanceof List) {
                    for (Object res: (List<?>)result) {
                        if (res instanceof NamedElement) {
                            log.log(((NamedElement)res).getName());
                        } else if (res instanceof ViewElement) {
                            log.log(res.toString());
                        }
                    }
                } else if (result instanceof ModelExtent) {
                    for (EObject object: ((ModelExtent)result).getContents()) {
                        if (object instanceof Paragraph) {
                            log.log(((Paragraph)object).getText());
                        }
                    }
                }
            }

        } else
            log.log("script has no output!");

    }
}
