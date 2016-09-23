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
package gov.nasa.jpl.mbee.mdk.ems.validation.actions;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import gov.nasa.jpl.mbee.mdk.ems.ImportException;
import gov.nasa.jpl.mbee.mdk.ems.ImportUtility;
import gov.nasa.jpl.mbee.mdk.ems.ReferenceException;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import gov.nasa.jpl.mbee.mdk.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mbee.mdk.docgen.validation.RuleViolationAction;
import org.json.simple.JSONObject;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Map;

public class ImportInstanceSpec extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    private static final long serialVersionUID = 1L;
    private InstanceSpecification element;
    private JSONObject result;
    private JSONObject spec;

    public ImportInstanceSpec(InstanceSpecification e, JSONObject spec, JSONObject result) {
        super("ImportName", "Accept instance", null, null);
        this.element = e;
        this.result = result;
        this.spec = spec;
    }

    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @Override
    public void execute(Collection<Annotation> annos) {
        executeMany(annos, "Change instances");
    }

    @Override
    protected boolean doAction(Annotation anno) {
        if (anno != null) {
            Element e = (Element) anno.getTarget();
            if (!e.isEditable()) {
                Utils.guilog("[ERROR] " + ((NamedElement) e).getQualifiedName() + " isn't editable");
                return false;
            }
            JSONObject resultOb = ((Map<String, JSONObject>) result.get("elementsKeyed")).get(e.getID());
            try {
                ImportUtility.setInstanceSpecification((InstanceSpecification) e, (JSONObject) resultOb);
//                ImportUtility.setInstanceSpecification((InstanceSpecification) e, (JSONObject) resultOb.get("specialization"));
            } catch (ImportException ex) {
                if (ex instanceof ReferenceException) {
                    Utils.guilog("[ERROR] " + ((NamedElement) e).getQualifiedName() + " cannot be imported because it'll be missing classifiers.");
                }
                else {
                    Utils.guilog("[ERROR] " + ex.getMessage());
                }
                return false;
            }
        }
        else {
            try {
                ImportUtility.setInstanceSpecification(element, spec);
            } catch (ImportException ex) {
                if (ex instanceof ReferenceException) {
                    Utils.guilog("[ERROR] " + element.getQualifiedName() + " cannot be imported because it'll be missing classifiers.");
                }
                else {
                    Utils.guilog("[ERROR] " + ex.getMessage());
                }
                return false;
            }
        }
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!element.isEditable()) {
            Utils.guilog("[ERROR] " + element.getQualifiedName() + " is not editable!");
            return;
        }
        execute("Change instance");
    }
}
