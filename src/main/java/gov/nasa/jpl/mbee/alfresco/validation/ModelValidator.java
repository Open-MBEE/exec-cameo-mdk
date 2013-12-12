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
package gov.nasa.jpl.mbee.alfresco.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Expression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralBoolean;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralInteger;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralReal;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralUnlimitedNatural;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;

import gov.nasa.jpl.mbee.alfresco.validation.actions.FixModelOwner;
import gov.nasa.jpl.mbee.alfresco.validation.actions.ImportDoc;
import gov.nasa.jpl.mbee.alfresco.validation.actions.ImportName;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;

public class ModelValidator {

    private ValidationSuite suite = new ValidationSuite("Model Sync");
    private ValidationRule nameDiff = new ValidationRule("Mismatched Name", "name is different", ViolationSeverity.ERROR);
    private ValidationRule docDiff = new ValidationRule("Mismatched Doc", "documentation is different", ViolationSeverity.ERROR);
    private ValidationRule valueDiff = new ValidationRule("Mismatched Value", "value is different", ViolationSeverity.ERROR);
    private ValidationRule ownership = new ValidationRule("Moved", "Wrong containment", ViolationSeverity.ERROR);
    private Project prj;
    private Element start;
    private JSONObject result;
    
    public ModelValidator(Element start, JSONObject result) {
        this.start = start;
        this.result = result;
        suite.addValidationRule(nameDiff);
        suite.addValidationRule(docDiff);
        suite.addValidationRule(valueDiff);
        suite.addValidationRule(ownership);
        prj = Application.getInstance().getProject();
    }
    
    public void validate() {
        JSONObject elements = (JSONObject)result.get("elements");
        if (elements == null)
            return;
        for (String elementId: (Set<String>)elements.keySet()) {
            JSONObject elementInfo = (JSONObject)elements.get(elementId);
            Element e = (Element)prj.getElementByID(elementId);
            if (e == null)
                continue;
       
            boolean valuediff = false;
            String elementDoc = ModelHelper.getComment(e);
            String elementName = null;
            if (e instanceof NamedElement) {
                elementName = ((NamedElement)e).getName();
            }
            if (elementName != null && !elementName.equals(elementInfo.get("name"))) {
                ValidationRuleViolation v = new ValidationRuleViolation(e, "[NAME] model: " + elementName + ", web: " + elementInfo.get("name"));
                v.addAction(new ImportName((NamedElement)e, (String)elementInfo.get("name")));
                nameDiff.addViolation(v);
            }
            if (elementDoc != null && !elementDoc.equals(elementInfo.get("documentation"))) {
                ValidationRuleViolation v = new ValidationRuleViolation(e, "[DOC] model: " + elementDoc + ", web: " + elementInfo.get("documentation"));
                v.addAction(new ImportDoc(e, (String)elementInfo.get("documentation")));
                docDiff.addViolation(v);
            }
            if (e instanceof Property) {
                if (valueDiff((Property)e, elementInfo))
                    valuediff = true;
            }
            if (e instanceof Slot) {
                if (valueDiff((Slot)e, elementInfo))
                    valuediff = true;
            }
            String ownerID = e.getOwner().getID();
            if (!ownerID.equals(elementInfo.get("owner"))) {
                Element owner = (Element)prj.getElementByID((String)elementInfo.get("owner"));
                if (owner == null) {
                    continue;//??
                }
                ValidationRuleViolation v = new ValidationRuleViolation(e, "[OWNER] model: " + e.getOwner().getHumanName() + ", web: " + owner.getHumanName());
                v.addAction(new FixModelOwner(e, owner));
                ownership.addViolation(v);
            }
            
        }
    }
    
    private boolean valueDiff(Property e, JSONObject info) {
        ValueSpecification vs = e.getDefaultValue();
        String valueType = (String)info.get("valueType");
        if (vs == null && valueType == null)
            return false;
        if (valueType == null || vs == null)
            return true;
        JSONArray value = null;
        boolean diff = false;
        if (vs instanceof LiteralString && valueType.equals("LiteralString")) {
            value = (JSONArray)info.get("string");
            if (value.size() < 1)
                return true;
        } else if (vs instanceof LiteralBoolean && valueType.equals("LiteralBoolean")) {
            value = (JSONArray)info.get("boolean");
            if (value.size() < 1)
                return true;
            if ((Boolean)value.get(0) == ((LiteralBoolean)vs).isValue())
                return false;
        } else if (vs instanceof LiteralInteger && valueType.equals("LiteralInteger")) {
            value = (JSONArray)info.get("integer");
            if (value.size() < 1)
                return true;
            if (((LiteralInteger)vs).getValue() == (Integer)value.get(0))
                return false;
        } else if (vs instanceof LiteralUnlimitedNatural && valueType.equals("LiteralInteger")) {
            value = (JSONArray)info.get("integer");
            if (value.size() < 1)
                return true;
            if (((LiteralUnlimitedNatural)vs).getValue() == (Integer)value.get(0))
                return false;
        } else if (vs instanceof LiteralReal && valueType.equals("LiteralReal")) {
            value = (JSONArray)info.get("double");
            if (value.size() < 1)
                return true;
            if (((LiteralReal)vs).getValue() == (Double)value.get(0)) 
                return false;
        } else if (vs instanceof ElementValue && valueType.equals("ElementValue")) {
            value = (JSONArray)((JSONObject)((JSONObject)result.get("relationships")).get("elementValues")).get(e.getID());
            if (value == null || value.size() < 1)
                return true;
            if (((ElementValue)vs).getElement() != null && ((ElementValue)vs).getElement().getID().equals(value.get(0)))
                return false;
        } else if (vs instanceof Expression && valueType.equals("Expression")) {
            //???
            value = (JSONArray)info.get("expression");
            if (value.size() < 1)
                return true;
        } else { //type of value in model and alfresco don't match or unknown type
            return true;
        }   
        return true;
    }
    
    private boolean valueDiff(Slot e, JSONObject info) {
        List<ValueSpecification> vss = e.getValue();
        return false;
    }
    
    public void showWindow() {
        List<ValidationSuite> vss = new ArrayList<ValidationSuite>();
        vss.add(suite);
        Utils.displayValidationWindow(vss, "Model Web Difference Validation");
    }
   
    
    
    
}
