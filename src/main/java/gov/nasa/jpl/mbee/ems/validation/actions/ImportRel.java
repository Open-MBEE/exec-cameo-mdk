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

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import gov.nasa.jpl.mbee.ems.ImportUtility;
import gov.nasa.jpl.mbee.ems.ReferenceException;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;
import org.json.simple.JSONObject;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Map;

public class ImportRel extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    private static final long serialVersionUID = 1L;
    private Element element;
    private JSONObject result;

    public ImportRel(Element e, JSONObject result) {
        //JJS--MDEV-567 fix: changed 'Import' to 'Accept'
        //
        super("ImportRel", "Accept rel", null, null);
        this.element = e;
        this.result = result;
    }

    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @Override
    public void execute(Collection<Annotation> annos) {
        executeMany(annos, "Change Rels");
    }

    @Override
    protected boolean doAction(Annotation anno) {
        if (anno != null) {
            Element e = (Element) anno.getTarget();
            String name = e.get_representationText();
            if (e instanceof NamedElement) {
                name = ((NamedElement) e).getQualifiedName();
            }
            if (!e.isEditable()) {
                Utils.guilog("[ERROR] " + name + " isn't editable");
                return false;
            }
            JSONObject elementJson = ((Map<String, JSONObject>) result.get("elementsKeyed")).get(e.getID());
            try {
                ImportUtility.setRelationshipEnds((DirectedRelationship) e, elementJson);
            } catch (ReferenceException ex) {
                Utils.guilog("[ERROR] " + name + " cannot be imported because it would create a model inconsistency due to one or both ends missing.");
                return false;
            }
        }
        else {
            String name = element.get_representationText();
            if (element instanceof NamedElement) {
                name = ((NamedElement) element).getQualifiedName();
            }
            JSONObject elementJson = ((Map<String, JSONObject>) result.get("elementsKeyed")).get(element.getID());
            try {
                ImportUtility.setRelationshipEnds((DirectedRelationship) element, elementJson);
            } catch (ReferenceException ex) {
                Utils.guilog("[ERROR] " + name + " cannot be imported because it would create a model inconsistency due to one or both roles missing.");
                return false;
            }
        }
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!element.isEditable()) {
            Utils.guilog("[ERROR] " + element.getHumanName() + " is not editable!");
            return;
        }
        execute("Change Rel");
    }
}