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

import gov.nasa.jpl.mbee.ems.ExportUtility;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportComment;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportDoc;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportElement;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportName;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportOwner;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportRel;
import gov.nasa.jpl.mbee.ems.validation.actions.ExportValue;
import gov.nasa.jpl.mbee.ems.validation.actions.FixModelOwner;
import gov.nasa.jpl.mbee.ems.validation.actions.ImportComment;
import gov.nasa.jpl.mbee.ems.validation.actions.ImportDoc;
import gov.nasa.jpl.mbee.ems.validation.actions.ImportName;
import gov.nasa.jpl.mbee.ems.validation.actions.ImportRel;
import gov.nasa.jpl.mbee.ems.validation.actions.ImportValue;
import gov.nasa.jpl.mbee.ems.validation.actions.InitializeProjectModel;
import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRule;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mgss.mbee.docgen.validation.ViolationSeverity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.uml.RepresentationTextCreator;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Expression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceValue;
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
import com.nomagic.uml2.ext.magicdraw.mdprofiles.ProfileApplication;

public class ModelValidator {

    private ValidationSuite suite = new ValidationSuite("Model Sync");
    private ValidationRule nameDiff = new ValidationRule("Mismatched Name", "name is different", ViolationSeverity.ERROR);
    private ValidationRule docDiff = new ValidationRule("Mismatched Doc", "documentation is different", ViolationSeverity.ERROR);
    private ValidationRule valueDiff = new ValidationRule("Mismatched Value", "value is different", ViolationSeverity.ERROR);
    private ValidationRule ownership = new ValidationRule("Moved", "Wrong containment", ViolationSeverity.ERROR);
    private ValidationRule exist = new ValidationRule("Exist", "Doesn't Exist or Moved", ViolationSeverity.WARNING);
    private ValidationRule relDiff = new ValidationRule("Relationship", "Relationship source or target", ViolationSeverity.ERROR);
    private ValidationRule commentDiff = new ValidationRule("Comment", "Comment different", ViolationSeverity.ERROR);
    private ValidationRule projectExist = new ValidationRule("Project Exist", "Project doesn't exist", ViolationSeverity.ERROR);
    private Project prj;
    private Element start;
    private JSONObject result;
    private boolean checkExist;
    private Set<Element> elementSet;
    
    public ModelValidator(Element start, JSONObject result, boolean checkExist, Set<Element> elementSet) {
        this.start = start;
        suite.addValidationRule(nameDiff);
        suite.addValidationRule(docDiff);
        suite.addValidationRule(valueDiff);
        suite.addValidationRule(ownership);
        suite.addValidationRule(exist);
        suite.addValidationRule(relDiff);
        suite.addValidationRule(commentDiff);
        suite.addValidationRule(projectExist);
        this.checkExist = checkExist;
        this.result = result;
        prj = Application.getInstance().getProject();
        this.elementSet = elementSet;
    }
    
    public boolean checkProject() {
        String projectUrl = ExportUtility.getUrlWithSiteAndProject();
        if (projectUrl == null)
            return false;
        String response = ExportUtility.get(projectUrl);
        if (response == null) {
            ValidationRuleViolation v = new ValidationRuleViolation(Project.getProject(start).getModel(), "This project doesn't exist on the web yet, or the site has been moved");
            v.addAction(new InitializeProjectModel(false));
            projectExist.addViolation(v);
            return false;
        }
        if (ProjectUtilities.isElementInAttachedProject(start)){
            Utils.showPopupMessage("You should not validate or export elements not from this project! Open the right project and do it from there");
            return false;
        }
        String url = ExportUtility.getUrl();
        String id = start.getID();
        if (start == Application.getInstance().getProject().getModel())
            id = Application.getInstance().getProject().getPrimaryProject().getProjectID();
        url += "/javawebscripts/elements/" + id + "?recurse=true";
        response = ExportUtility.get(url);
        if (response == null) {
            response = "{\"elements\": []}";
        }
        result = (JSONObject)JSONValue.parse(response);
        ResultHolder.lastResults = result;
        return true;
    }
    
    @SuppressWarnings("unchecked")
    public void validate() {
        JSONArray elements = (JSONArray)result.get("elements");
        if (elements == null)
            return;
        Map<String, JSONObject> elementsKeyed = new HashMap<String, JSONObject>();
        if (checkExist) {
            elementSet = new HashSet<Element>();
            getAllMissing(start, elementSet, elementsKeyed);
            validateModel(elementsKeyed, elementSet);
        } else {
            validateModel(elementsKeyed, elementSet);
        }
        result.put("elementsKeyed", elementsKeyed);
    }
    
    @SuppressWarnings("unchecked")
    private void validateModel(Map<String, JSONObject> elementsKeyed, Set<Element> all) {
        //Set<Element> all = new HashSet<Element>();
        Set<String> checked = new HashSet<String>();
        //getAllMissing(start, all, elementsKeyed);
        JSONArray elements = (JSONArray)result.get("elements");
        if (elements == null)
            return;
        for (JSONObject elementInfo: (List<JSONObject>)elements) {
            String elementId = (String)elementInfo.get("id");
            if (elementId.contains("-slot-")) {
                Element e = ExportUtility.getElementFromID(elementId);
                if (e != null)
                    elementId = e.getID();
                else
                    continue;
            }
            elementsKeyed.put(elementId, elementInfo);
        }
        for (Element e: all) {
            if (!elementsKeyed.containsKey(e.getID())) {
                if (checkExist) {
                    ValidationRuleViolation v = new ValidationRuleViolation(e, "[EXIST] This doesn't exist on alfresco or it may be moved");
                    v.addAction(new ExportElement(e));
                    exist.addViolation(v);
                }
                continue;
            }
            JSONObject elementInfo = (JSONObject)elementsKeyed.get(e.getID());
            checkElement(e, elementInfo);
            checked.add(e.getID());
        }
        Set<String> elementsKeyedIds = new HashSet<String>(elementsKeyed.keySet());
        elementsKeyedIds.removeAll(checked);
        for (String elementsKeyedId: elementsKeyedIds) {
            Element e = ExportUtility.getElementFromID(elementsKeyedId);
            if (e == null)
                continue;
            checkElement(e, elementsKeyed.get(elementsKeyedId));
        }
    }
    
    /*
    @SuppressWarnings("unchecked")
    private void validateViews(Map<String, JSONObject> elementsKeyed) {
        JSONArray elements = (JSONArray)result.get("elements");
        if (elements == null)
            return;
        for (JSONObject elementInfo: (List<JSONObject>)elements) {
            String elementId = (String)elementInfo.get("id");
            //Debug.outln("validating " + elementInfo + ", id = " + elementId);
            Element e = ExportUtility.getElementFromID(elementId);
            //Debug.outln("element = " + e);
            if (e == null) {
                continue;
            }
            if (elementsKeyed.containsKey(e.getID())) {
                //Debug.outln("elementKeyed (" + elementsKeyed + ") contains " + elementId);
                continue;
            }
            elementsKeyed.put(e.getID(), elementInfo);
            //Debug.outln( "element.getClass() = "
                         //+ e.getClass().getSimpleName() );
            checkElement(e, elementInfo);
            
        }
    }*/
    
    private void getAllMissing(Element current, Set<Element> missing, Map<String, JSONObject> elementsKeyed) {
        if (ProjectUtilities.isElementInAttachedProject(current))
            return;
        if (!ExportUtility.shouldAdd(current))
            return;
        if (!elementsKeyed.containsKey(current.getID()))
            if (!(current instanceof Model && ((Model)current).getName().equals("Data")))
                missing.add(current);
        for (Element e: current.getOwnedElement()) {
            getAllMissing(e, missing, elementsKeyed);            
        }
    }
    
    
    private void checkElement(Element e, JSONObject elementInfo) {
        String elementDoc = ModelHelper.getComment(e);
        String elementDocClean = Utils.stripHtmlWrapper(elementDoc).replace(" class=\"pwrapper\"", "");
        String elementName = null;
        String webDoc = (String)elementInfo.get("documentation");
        if (webDoc != null) {
            webDoc = webDoc.replace(" class=\"pwrapper\"", "");
            elementInfo.put("documentation", webDoc);
        }
        if (e instanceof NamedElement) {
            elementName = ((NamedElement)e).getName();
        }
        String webName = ExportUtility.unescapeHtml((String)elementInfo.get("name"));
        if (elementName != null && !elementName.equals(webName)) {
            ValidationRuleViolation v = new ValidationRuleViolation(e, "[NAME] model: " + elementName + ", web: " + webName);
            v.addAction(new ImportName((NamedElement)e, webName, result));
            v.addAction(new ExportName((NamedElement)e));
            nameDiff.addViolation(v);
        }
        if (elementDoc != null && !(webDoc == null && elementDoc.equals("")) && !elementDocClean.equals(webDoc)) {
            ValidationRuleViolation v = new ValidationRuleViolation(e, "[DOC] model: " + truncate(elementDocClean) + ", web: " + truncate((String)elementInfo.get("documentation")));
            v.addAction(new ImportDoc(e, webDoc, result));
            v.addAction(new ExportDoc(e));
            docDiff.addViolation(v);
        }
        if (e instanceof Property) {
            ValidationRuleViolation v = valueDiff((Property)e, elementInfo);
            if (v != null)
                valueDiff.addViolation(v);
        } else if (e instanceof Slot) {
            ValidationRuleViolation v = valueDiff((Slot)e, elementInfo);
            if (v != null)
                valueDiff.addViolation(v);
        } else if (e instanceof Comment) {
            ValidationRuleViolation v = commentDiff((Comment)e, elementInfo);
            if (v != null)
                valueDiff.addViolation(v);
        } else if (e instanceof DirectedRelationship) {
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
            if (websource != null && webtarget != null && localsource != null && localtarget != null && (websource != localsource || webtarget != localtarget)) {
                String msg = "[REL] ";
                if (websource != localsource)
                    msg += "model source: " + localsource.getHumanName() + ", web source: " + websource == null ? "null" : websource.getHumanName() + " ";
                if (webtarget != localtarget)
                    msg += "model target: " + localtarget.getHumanName() + ", web target: " + webtarget == null ? "null" : webtarget.getHumanName();
                ValidationRuleViolation v = new ValidationRuleViolation(e, msg);
                v.addAction(new ImportRel(e, result));
                v.addAction(new ExportRel(e));
                relDiff.addViolation(v);
            }
        }
        if ( e.getOwner() != null ) {
            String ownerID = e.getOwner().getID();
            String webOwnerID = (String)elementInfo.get("owner");
            if (webOwnerID == null || webOwnerID.startsWith("PROJECT"))
                webOwnerID = Application.getInstance().getProject().getModel().getID();
            if (!ownerID.equals(webOwnerID)) {
                Element owner = (Element)prj.getElementByID(webOwnerID);
                ValidationRuleViolation v = new ValidationRuleViolation(e, "[OWNER] model: " + e.getOwner().getHumanName() + ", web: " + (owner == null ? "null" : owner.getHumanName()));
                v.addAction(new FixModelOwner(e, owner, result));
                v.addAction(new ExportOwner(e));
                ownership.addViolation(v);
            }
        }
    }
    
    
    
    private ValidationRuleViolation valueDiff(Property e, JSONObject info) {
        ValueSpecification vs = e.getDefaultValue();
        String valueTypes = (String)info.get("valueType");
        JSONArray value = (JSONArray)info.get("value");
        if ((vs == null || (vs instanceof ElementValue && ((ElementValue)vs).getElement() == null) || 
                (vs instanceof InstanceValue && ((InstanceValue)vs).getInstance() == null))
                && (valueTypes == null || value == null || value.isEmpty()))
            return null;
        if ((vs != null || (vs instanceof ElementValue && ((ElementValue)vs).getElement() != null) || 
                (vs instanceof InstanceValue && ((InstanceValue)vs).getInstance() != null))
                && (valueTypes == null || value == null || value.isEmpty())) {
            ValidationRuleViolation v = new ValidationRuleViolation(e, "[VALUE] model: not null, web: null");
            v.addAction(new ImportValue(e, null, null, result));
            v.addAction(new ExportValue(e));
            return v;
        }
        if ((vs == null || (vs instanceof ElementValue && ((ElementValue)vs).getElement() == null) || 
                (vs instanceof InstanceValue && ((InstanceValue)vs).getInstance() == null)) 
                && value != null && value.size() > 0 && valueTypes != null) {
            ValidationRuleViolation v = new ValidationRuleViolation(e, "[VALUE] model: null, web: " + truncate(value.toString()));
            v.addAction(new ImportValue(e, value, PropertyValueType.valueOf(valueTypes), result));
            v.addAction(new ExportValue(e));
            return v;
        }
        PropertyValueType valueType = PropertyValueType.valueOf(valueTypes);
        String message = "";
        String typeMismatchMessage = "[VALUE] value spec types don't match";
        if (valueType == PropertyValueType.LiteralString) {
            if (vs instanceof LiteralString) {
                String modelString = Utils.stripHtmlWrapper(((LiteralString)vs).getValue()).replace(" class=\"pwrapper\"", "");
                String webString = ((String)value.get(0)).replace(" class=\"pwrapper\"", "");
                value.set(0, webString);
                if (!modelString.equals(webString)) {
                    message = "[VALUE] model: " + truncate(modelString) + ", web: " + truncate(webString);
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
                Double webValue = null;
                if (value.get(0) instanceof Long)
                    webValue = Double.parseDouble(((Long)value.get(0)).toString());
                else
                    webValue = (Double)value.get(0);
                if (((LiteralReal)vs).getValue() != webValue) {
                    message = "[VALUE] model: " + ((LiteralReal)vs).getValue() + ", web: " + webValue.toString();
                }
            } else {
                message = typeMismatchMessage;
            }
        } else if (valueType == PropertyValueType.ElementValue) {
            if (vs instanceof ElementValue) {
                if (((ElementValue)vs).getElement() == null || !ExportUtility.getElementID(((ElementValue)vs).getElement()).equals(value.get(0))) {
                    message = "[VALUE] model: " + ((ElementValue)vs).getElement() + ", web: " + value.toString();
                }
            } else if (vs instanceof InstanceValue) {
                if (((InstanceValue)vs).getInstance() == null || !ExportUtility.getElementID(((InstanceValue)vs).getInstance()).equals(value.get(0))) {
                    message = "[VALUE] model: " + ((InstanceValue)vs).getInstance() + ", web: " + value.toString();
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
            v.addAction(new ImportValue(e, value, valueType, result));
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
        boolean nullElementValues = areNullElementValues(vs);
        if ((vs == null || vs.isEmpty() || nullElementValues) && (valueTypes == null || value == null || value.size() == 0)) {
            Debug.outln("returning null: vs=" + vs + ", valueTypes=" + valueTypes + ", value=" + value);
            return null;
        }
        if (vs != null && vs.size() > 0 && !nullElementValues && (valueTypes == null || value == null || value.size() == 0)) {
            ValidationRuleViolation v = new ValidationRuleViolation(e, "[VALUE] model: not null, web: null");
            v.addAction(new ImportValue(e, null, null, result));
            v.addAction(new ExportValue(e));
            Debug.outln("1) returning ValidationRuleViolation: " + v );
            return v;
        }
        if ((vs == null || vs.isEmpty() || nullElementValues) && value != null && value.size() > 0 && valueTypes != null) {
            ValidationRuleViolation v = new ValidationRuleViolation(e, "[VALUE] model: null, web: " + truncate(value.toString()));
            v.addAction(new ImportValue(e, value, PropertyValueType.valueOf(valueTypes), result));
            v.addAction(new ExportValue(e));
            Debug.outln("2) returning ValidationRuleViolation: " + v );
            return v;
        }
        if ((vs.size() != value.size())) {
            ValidationRuleViolation v = new ValidationRuleViolation(e, "[VALUE] model and web values don't match");
            v.addAction(new ImportValue(e, value, PropertyValueType.valueOf(valueTypes), result));
            v.addAction(new ExportValue(e));
            Debug.outln("3) returning ValidationRuleViolation: " + v );
            return v;
        }

        PropertyValueType valueType = PropertyValueType.valueOf(valueTypes);
        String message = "";
        String typeMismatchMessage = "[VALUE] value spec types don't match";
        String badMessage = "[VALUE] model: " + truncate(RepresentationTextCreator.getRepresentedText(e)) + ", web: " + truncate(value.toString());
        if (valueType == PropertyValueType.LiteralString) {
            if (vs.get(0) instanceof LiteralString) {
                for (int i = 0; i < vs.size(); i++) {
                    String modelString = Utils.stripHtmlWrapper(((LiteralString)vs.get(i)).getValue()).replace(" class=\"pwrapper\"", "");
                    String webString = ((String)value.get(i)).replace(" class=\"pwrapper\"", "");
                    value.set(i, webString);
                    if (!modelString.equals(webString)) {
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
                    Double webValue = null;
                    if (value.get(i) instanceof Long)
                        webValue = Double.parseDouble(((Long)value.get(i)).toString());
                    else
                        webValue = (Double)value.get(i);
                    if (((LiteralReal)vs.get(i)).getValue() != webValue) {
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
                    if (((ElementValue)vs.get(i)).getElement() == null || !ExportUtility.getElementID(((ElementValue)vs.get(i)).getElement()).equals(value.get(i))) {
                        message = badMessage;
                        break;
                    }
                }
            } else if (vs.get(0) instanceof InstanceValue) {
                for (int i = 0; i < vs.size(); i++) {
                    if (((InstanceValue)vs.get(i)).getInstance() == null || !ExportUtility.getElementID(((InstanceValue)vs.get(i)).getInstance()).equals(value.get(i))) {
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
            v.addAction(new ImportValue(e, value, valueType, result));
            v.addAction(new ExportValue(e));
            Debug.outln("4) returning ValidationRuleViolation: " + v );
            return v;
        }
        Debug.outln("5) returning null" );
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private ValidationRuleViolation commentDiff(Comment e, JSONObject elementInfo) {
        String modelBodyClean = Utils.stripHtmlWrapper(((Comment)e).getBody());
        String webBody = (String)elementInfo.get("body");
        if (webBody == null)
            webBody = "";
        ValidationRuleViolation v = null;
        if (!modelBodyClean.equals(webBody)) {
            v = new ValidationRuleViolation(e, "[Comment] model: " + truncate(modelBodyClean) + ", web: " + truncate(webBody));
            v.addAction(new ImportComment(e, webBody, result));
            v.addAction(new ExportComment(e));
        }
        Set<String> modelAnnotated = new HashSet<String>();
        for (Element el: e.getAnnotatedElement()) {
            modelAnnotated.add(el.getID());
        }
        JSONArray web = (JSONArray)elementInfo.get("annotatedElements");
        if (web != null) {
            Set<String> webs = new HashSet<String>(web);
            if (!webs.containsAll(modelAnnotated) || !modelAnnotated.containsAll(webs)) {
                if (v == null) {
                    v = new ValidationRuleViolation(e, "[Comment] The anchored elements are different");
                    v.addAction(new ImportComment(e, webBody, result));
                    v.addAction(new ExportComment(e));
                }
            }
        }
        return v;
    }
    public void showWindow() {
        List<ValidationSuite> vss = new ArrayList<ValidationSuite>();
        vss.add(suite);
        Utils.displayValidationWindow(vss, "Model Web Difference Validation");
    }
    
    public ValidationSuite getSuite() {
        return suite;
    }
    
    private boolean areNullElementValues(List<ValueSpecification> vs) {
        for (ValueSpecification v: vs) {
            if (!(v instanceof ElementValue || v instanceof InstanceValue) || 
                    (v instanceof ElementValue && ((ElementValue)v).getElement() != null) || 
                    (v instanceof InstanceValue && ((InstanceValue)v).getInstance() != null))
                return false;
        }
        return true;
    }
    
    private static String truncate(String s) {
        if (s == null)
            return null;
        if (s.length() > 50)
            return s.substring(0, 49) + "...";
        return s;
    }
}
