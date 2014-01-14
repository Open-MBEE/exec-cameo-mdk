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
package gov.nasa.jpl.mbee.ems.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.RepresentationTextCreator;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
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
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Extension;

import gov.nasa.jpl.mbee.ems.validation.actions.ExportDoc;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportElement;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportName;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportOwner;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportRel;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportValue;
import gov.nasa.jpl.mbee.ems.validation.actions.FixModelOwner;
import gov.nasa.jpl.mbee.ems.validation.actions.ImportDoc;
import gov.nasa.jpl.mbee.ems.validation.actions.ImportName;
import gov.nasa.jpl.mbee.ems.validation.actions.ImportRel;
import gov.nasa.jpl.mbee.ems.validation.actions.ImportValue;
import gov.nasa.jpl.mbee.lib.Debug;
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
    private ValidationRule exist = new ValidationRule("Exist", "Doesn't Exist or Moved", ViolationSeverity.WARNING);
    private ValidationRule relDiff = new ValidationRule("Relationship", "Relationship source or target", ViolationSeverity.ERROR);
    private Project prj;
    private Element start;
    private JSONObject result;
    private boolean checkExist;
    
    public ModelValidator(Element start, JSONObject result, boolean checkExist) {
        this.start = start;
        this.result = result;
        suite.addValidationRule(nameDiff);
        suite.addValidationRule(docDiff);
        suite.addValidationRule(valueDiff);
        suite.addValidationRule(ownership);
        suite.addValidationRule(exist);
        suite.addValidationRule(relDiff);
        this.checkExist = checkExist;
        prj = Application.getInstance().getProject();
    }
    
    public void validate() {
        JSONArray elements = (JSONArray)result.get("elements");
        if (elements == null)
            return;
        JSONObject elementsKeyed = new JSONObject();
        for (JSONObject elementInfo: (List<JSONObject>)elements) {
            //JSONObject elementInfo = (JSONObject)elements.get(elementId);
            String elementId = (String)elementInfo.get("id");
            Debug.outln("validating " + elementInfo + ", id = " + elementId);
            if (elementsKeyed.containsKey(elementId)) {
                Debug.outln("elementKeyed (" + elementsKeyed + ") contains " + elementId);
                continue;
            }
            elementsKeyed.put(elementId, elementInfo);
            Element e = (Element)prj.getElementByID(elementId);
            Debug.outln("element = " + e);
            if (e == null) {
                continue;
            }
            Debug.outln( "element.getClass() = "
                       	 + e.getClass().getSimpleName() );
            String elementDoc = ModelHelper.getComment(e);
            String elementDocClean = Utils.stripHtmlWrapper(elementDoc);
            String elementName = null;
            if (e instanceof NamedElement) {
                elementName = ((NamedElement)e).getName();
            }
            if (elementName != null && !elementName.equals(elementInfo.get("name"))) {
                ValidationRuleViolation v = new ValidationRuleViolation(e, "[NAME] model: " + elementName + ", web: " + elementInfo.get("name"));
                v.addAction(new ImportName((NamedElement)e, (String)elementInfo.get("name")));
                v.addAction(new ExportName((NamedElement)e));
                nameDiff.addViolation(v);
            }
            if (elementDoc != null && !elementDocClean.equals(elementInfo.get("documentation"))) {
                ValidationRuleViolation v = new ValidationRuleViolation(e, "[DOC] model: " + elementDocClean + ", web: " + elementInfo.get("documentation"));
                v.addAction(new ImportDoc(e, (String)elementInfo.get("documentation")));
                v.addAction(new ExportDoc(e));
                docDiff.addViolation(v);
            }
            if (e instanceof Property) {
                ValidationRuleViolation v = valueDiff((Property)e, elementInfo);
                if (v != null)
                    valueDiff.addViolation(v);
            }
            if (e instanceof Slot) {
                ValidationRuleViolation v = valueDiff((Slot)e, elementInfo);
                if (v != null)
                    valueDiff.addViolation(v);
            }
            if (e instanceof DirectedRelationship) {
                String websourceId = (String)elementInfo.get("source");
                Element websource = null;
                String webtargetId = (String)elementInfo.get("target");
                Element webtarget = null;
                Element localsource = ModelHelper.getClientElement(e);
                Element localtarget = ModelHelper.getSupplierElement(e);
                if (websourceId != null)
                    websource = (Element)prj.getElementByID(websourceId);
                if (webtargetId != null)
                    webtarget = (Element)prj.getElementByID(webtargetId);
                if (websource != localsource || webtarget != localtarget) {
                    String msg = "[REL] ";
                    if (websource != localsource)
                        msg += "model source: " + localsource.getHumanName() + ", web source: " + websource.getHumanName() + " ";
                    if (webtarget != localtarget)
                        msg += "model target: " + localtarget.getHumanName() + ", web target: " + webtarget.getHumanName();
                    ValidationRuleViolation v = new ValidationRuleViolation(e, msg);
                    v.addAction(new ImportRel(e));
                    v.addAction(new ExportRel(e));
                    relDiff.addViolation(v);
                }
            }
            String ownerID = e.getOwner().getID();
            String webOwnerID = (String)elementInfo.get("owner");
            if (webOwnerID == null || webOwnerID.startsWith("PROJECT"))
                webOwnerID = Application.getInstance().getProject().getModel().getID();
            if (!ownerID.equals(webOwnerID)) {
                Element owner = (Element)prj.getElementByID(webOwnerID);
                if (owner == null) {
                    continue;//??
                }
                ValidationRuleViolation v = new ValidationRuleViolation(e, "[OWNER] model: " + e.getOwner().getHumanName() + ", web: " + owner.getHumanName());
                v.addAction(new FixModelOwner(e, owner));
                v.addAction(new ExportOwner(e));
                ownership.addViolation(v);
            }
            
        }
        if (checkExist) {
            for (Element e: Utils.collectOwnedElements(start, 0)) {
                if (e instanceof Comment || e instanceof Extension || e instanceof ValueSpecification)
                    continue;
                if (elementsKeyed.containsKey(e.getID()))
                    continue;
                ValidationRuleViolation v = new ValidationRuleViolation(e, "[EXIST] This doesn't exist on alfresco or it may be moved");
                v.addAction(new ExportElement(e));
                exist.addViolation(v);
            }
        }
        result.put("elementsKeyed", elementsKeyed);
    }
    
    private ValidationRuleViolation valueDiff(Property e, JSONObject info) {
        ValueSpecification vs = e.getDefaultValue();
        String valueTypes = (String)info.get("valueType");
        JSONArray value = (JSONArray)info.get("value");
        if (vs == null && (valueTypes == null || value == null || value.isEmpty()))
            return null;
        if (vs != null && (valueTypes == null || value == null || value.isEmpty())) {
            ValidationRuleViolation v = new ValidationRuleViolation(e, "[VALUE] model: not null, web: null");
            v.addAction(new ImportValue(e, null, null));
            v.addAction(new ExportValue(e));
            return v;
        }
        if (vs == null && value != null && value.size() > 0 && valueTypes != null) {
            ValidationRuleViolation v = new ValidationRuleViolation(e, "[VALUE] model: null, web: " + value.toString());
            v.addAction(new ImportValue(e, value, PropertyValueType.valueOf(valueTypes)));
            v.addAction(new ExportValue(e));
            return v;
        }
        PropertyValueType valueType = PropertyValueType.valueOf(valueTypes);
        String message = "";
        String typeMismatchMessage = "[VALUE] value spec types don't match";
        if (valueType == PropertyValueType.LiteralString) {
            if (vs instanceof LiteralString) {
                if (!((String)value.get(0)).equals(((LiteralString)vs).getValue())) {
                    message = "[VALUE] model: " + ((LiteralString)vs).getValue() + ", web: " + value.toString();
                }
            } else {
                message = typeMismatchMessage;
            }
        } else if (valueType == PropertyValueType.LiteralBoolean) {
            if (vs instanceof LiteralBoolean) {
                if ((Boolean)value.get(0) != ((LiteralBoolean)vs).isValue()) {
                    message = "[VALUE] model: " + ((LiteralBoolean)vs).isValue() + ", web: " + value.toString();
                }
            } else {
                message = typeMismatchMessage;
            }
        } else if (valueType == PropertyValueType.LiteralInteger) {
            if (vs instanceof LiteralInteger) {
                if (((LiteralInteger)vs).getValue() != (Long)value.get(0)) {
                    message = "[VALUE] model: " + ((LiteralInteger)vs).getValue() + ", web: " + value.toString();
                }
            } else if (vs instanceof LiteralUnlimitedNatural) {
                if (((LiteralUnlimitedNatural)vs).getValue() != (Long)value.get(0)) {
                    message = "[VALUE] model: " + ((LiteralUnlimitedNatural)vs).getValue() + ", web: " + value.toString();
                    valueType = PropertyValueType.LiteralUnlimitedNatural;
                }
            } else {
                message = typeMismatchMessage;
            }
        } else if (valueType == PropertyValueType.LiteralReal) {
            if (vs instanceof LiteralReal) {
                if (((LiteralReal)vs).getValue() != (Double)value.get(0)) {
                    message = "[VALUE] model: " + ((LiteralReal)vs).getValue() + ", web: " + value.toString();
                }
            } else {
                message = typeMismatchMessage;
            }
        } else if (valueType == PropertyValueType.ElementValue) {
            if (vs instanceof ElementValue) {
                if (((ElementValue)vs).getElement() == null || !((ElementValue)vs).getElement().getID().equals(value.get(0))) {
                    message = "[VALUE] model: " + ((ElementValue)vs).getElement() + ", web: " + value.toString();
                }
            } else {
                message = typeMismatchMessage;
            }
        } else if (vs instanceof Expression && valueType == PropertyValueType.Expression) {
            //???
            
        } else { //type of value in model and alfresco don't match or unknown type
            
        }   
        if (!message.equals("")) {
            ValidationRuleViolation v = new ValidationRuleViolation(e, message);
            v.addAction(new ImportValue(e, value, valueType));
            v.addAction(new ExportValue(e));
            return v;
        }
        return null;
    }
    
    private ValidationRuleViolation valueDiff(Slot e, JSONObject info) {
        Debug.outln( "valueDiff(Slot:" + Utils.slotValueToString( e )
                     + ", JSONObjec info=" + info );
        List<ValueSpecification> vs = e.getValue();
        String valueTypes = (String)info.get("valueType");
        JSONArray value = (JSONArray)info.get("value");
        Debug.outln("JSONArray value = " + value);
        
        if ((vs == null || vs.isEmpty()) && (valueTypes == null || value == null || value.size() == 0)) {
            Debug.outln("returning null: vs=" + vs + ", valueTypes=" + valueTypes + ", value=" + value);
            return null;
        }
        if (vs != null && vs.size() > 0 && (valueTypes == null || value == null || value.size() == 0)) {
            ValidationRuleViolation v = new ValidationRuleViolation(e, "[VALUE] model: not null, web: null");
            v.addAction(new ImportValue(e, null, null));
            v.addAction(new ExportValue(e));
            Debug.outln("1) returning ValidationRuleViolation: " + v );
            return v;
        }
        if ((vs == null || vs.isEmpty()) && value != null && value.size() > 0 && valueTypes != null) {
            ValidationRuleViolation v = new ValidationRuleViolation(e, "[VALUE] model: null, web: " + value.toString());
            v.addAction(new ImportValue(e, value, PropertyValueType.valueOf(valueTypes)));
            v.addAction(new ExportValue(e));
            Debug.outln("2) returning ValidationRuleViolation: " + v );
            return v;
        }
        if ((vs.size() != value.size())) {
            ValidationRuleViolation v = new ValidationRuleViolation(e, "[VALUE] model and web values don't match");
            v.addAction(new ImportValue(e, value, PropertyValueType.valueOf(valueTypes)));
            v.addAction(new ExportValue(e));
            Debug.outln("3) returning ValidationRuleViolation: " + v );
            return v;
        }

        PropertyValueType valueType = PropertyValueType.valueOf(valueTypes);
        String message = "";
        String typeMismatchMessage = "[VALUE] value spec types don't match";
        String badMessage = "[VALUE] model: " + RepresentationTextCreator.getRepresentedText(e) + ", web: " + value.toString();
        if (valueType == PropertyValueType.LiteralString) {
            if (vs.get(0) instanceof LiteralString) {
                for (int i = 0; i < vs.size(); i++) {
                    if (!((String)value.get(i)).equals(((LiteralString)vs.get(i)).getValue())) {
                        message = badMessage;
                        break;
                    }
                }
            } else {
                message = typeMismatchMessage;
            }
        } else if (valueType == PropertyValueType.LiteralBoolean) {
            if (vs.get(0) instanceof LiteralBoolean) {
                for (int i = 0; i < vs.size(); i++) {
                    if (((Boolean)value.get(i)) != (((LiteralBoolean)vs.get(i)).isValue())) {
                        message = badMessage;
                        break;
                    }
                }
            } else {
                message = typeMismatchMessage;
            }
        } else if (valueType == PropertyValueType.LiteralInteger) {
            if (vs.get(0) instanceof LiteralInteger) {
                for (int i = 0; i < vs.size(); i++) {
                    if (((LiteralInteger)vs.get(i)).getValue() != ((Long)value.get(i)).intValue()) {
                        message = badMessage;
                        break;
                    }
                }
            } else if (vs.get(0) instanceof LiteralUnlimitedNatural) {
                for (int i = 0; i < vs.size(); i++) {
                    if (((LiteralUnlimitedNatural)vs.get(i)).getValue() != ((Long)value.get(i)).intValue()) {
                        message = badMessage;
                        valueType = PropertyValueType.LiteralUnlimitedNatural;
                        break;
                    }
                }
            } else {
                message = "[VALUE] value spec types don't match";
            }
        } else if (valueType == PropertyValueType.LiteralReal) {
            if (vs.get(0) instanceof LiteralReal) {
                for (int i = 0; i < vs.size(); i++) {
                    if (((LiteralReal)vs.get(i)).getValue() != (Double)value.get(i)) {
                        message = badMessage;
                        break;
                    }
                }
            } else {
                message = typeMismatchMessage;
            }
        } else if (valueType == PropertyValueType.ElementValue) {
            if (vs.get(0) instanceof ElementValue) {
                for (int i = 0; i < vs.size(); i++) {
                    if (((ElementValue)vs.get(i)).getElement() == null || !((ElementValue)vs.get(i)).getElement().getID().equals(value.get(i))) {
                        message = badMessage;
                        break;
                    }
                }
            } else
                message = typeMismatchMessage;
        } else if (valueType == PropertyValueType.Expression) {
            //???
            
        } else { //unsupported type
            
        }   
        if (!message.equals("")) {
            ValidationRuleViolation v = new ValidationRuleViolation(e, message);
            v.addAction(new ImportValue(e, value, valueType));
            v.addAction(new ExportValue(e));
            Debug.outln("4) returning ValidationRuleViolation: " + v );
            return v;
        }
        Debug.outln("5) returning null" );
        return null;
    }
    
    public void showWindow() {
        List<ValidationSuite> vss = new ArrayList<ValidationSuite>();
        vss.add(suite);
        Utils.displayValidationWindow(vss, "Model Web Difference Validation");
    }
    
    public ValidationSuite getSuite() {
        return suite;
    }
}
