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
package gov.nasa.jpl.mbee.alfresco;

import gov.nasa.jpl.mbee.DocGen3Profile;
import gov.nasa.jpl.mbee.lib.Utils;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.uml.RepresentationTextCreator;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.classes.mddependencies.Dependency;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Comment;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.DirectedRelationship;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Expression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Generalization;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralBoolean;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralInteger;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralReal;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralUnlimitedNatural;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Type;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Extension;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;

public class ModelExporter {

    private JSONObject elementHierarchy = new JSONObject();
    private JSONObject elements = new JSONObject();
    private JSONObject relationshipElements = new JSONObject();
    private JSONObject propertyTypes = new JSONObject();
    //private JSONObject propertyValues = new JSONObject();
    private JSONObject elementValues = new JSONObject();
    private JSONArray roots = new JSONArray();
    
    private List<Element> starts;
    private int depth;
    private boolean packageOnly;
    
    private Stereotype view = Utils.getViewStereotype();
    private Stereotype viewpoint = Utils.getViewpointStereotype();
    
    public ModelExporter(Project prj, int depth, boolean pkgOnly) {
        this.depth = depth;
        starts = new ArrayList<Element>();
        for (Package pkg: prj.getModel().getNestedPackage()) {
            if (ProjectUtilities.isElementInAttachedProject(pkg))
                continue;//check for module??
            starts.add(pkg);
        }
        packageOnly = pkgOnly;
        
    }
    
    public ModelExporter(List<Element> roots, int depth, boolean pkgOnly) {
        this.depth = depth;
        this.starts = roots;
        packageOnly = pkgOnly;
    }
    
    public int getNumberOfElements() {
        return elements.size();
    }
    
    @SuppressWarnings("unchecked")
    public JSONObject getResult() {
        for (Element e: starts) {
            if (addToElements(e, 1))
                roots.add(e.getID());
        }
        JSONObject result = new JSONObject();
        result.put("roots", roots);
        result.put("elements", elements);
        result.put("elementHierarchy", elementHierarchy);
        JSONObject relationships = new JSONObject();
        relationships.put("relationshipElements", relationshipElements);
        relationships.put("propertyTypes", propertyTypes);
        //relationships.put("propertyValues", propertyValues);
        relationships.put("elementValues", elementValues);
        result.put("relationships", relationships);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private boolean addToElements(Element e, int curdepth) {
        if (elements.containsKey(e.getID()))
            return true;
        if (e instanceof Comment || e instanceof ValueSpecification || !(e instanceof Package) && packageOnly || e instanceof Extension)
            return false;
        JSONObject elementInfo = new JSONObject();
        if (e instanceof Package) {
            elementInfo.put("type", "Package");
        } else if (e instanceof Property) {
            elementInfo.put("type", "Property");
            elementInfo.put("isDerived", ((Property)e).isDerived());
            elementInfo.put("isSlot", false);
            ValueSpecification vs = ((Property)e).getDefaultValue();
            if (vs != null) {
                //addToElements(vs, 0);
                //propertyValues.put(e.getID(), vs.getID());
                JSONArray value = new JSONArray();
                addValues(e, value, elementInfo, vs);
            }
            Type type = ((Property)e).getType();
            if (type != null) {
                addToElements(type, 0);
                propertyTypes.put(e.getID(), type.getID());
            }
        } else if (e instanceof Slot) {
            elementInfo.put("type", "Property");
            elementInfo.put("isDerived", false);
            elementInfo.put("isSlot", true);
            List<ValueSpecification> vsl = ((Slot)e).getValue();
            if (vsl != null && vsl.size() > 0) {
                //addToElements(vs, 0);
                //propertyValues.put(e.getID(), vs.getID());
                JSONArray value = new JSONArray();
                for (ValueSpecification vs: vsl) {
                    addValues(e, value, elementInfo, vs);
                }
            }
            Element type = ((Slot)e).getDefiningFeature();
            if (type != null) {
                addToElements(type, 0);
                propertyTypes.put(e.getID(), type.getID());
            }
        } else if (e instanceof Dependency) {
            if (StereotypesHelper.hasStereotypeOrDerived(e, Utils.getConformsStereotype()))
                elementInfo.put("type", "Conform");
            else if (StereotypesHelper.hasStereotypeOrDerived(e, DocGen3Profile.queriesStereotype))
                elementInfo.put("type", "Expose");
            else
                elementInfo.put("type", "Dependency");
            addRelationship((Dependency)e);
        } else if (e instanceof Generalization) {
            elementInfo.put("type", "Generalization");
            addRelationship((Generalization)e);
   /*     } else if (e instanceof LiteralBoolean) {
            elementInfo.put("type", "LiteralBoolean");
            elementInfo.put("boolean", ((LiteralBoolean)e).isValue());
        } else if (e instanceof LiteralString) {
            elementInfo.put("type", "LiteralString");
            elementInfo.put("string", ((LiteralString)e).getValue());
        } else if (e instanceof LiteralInteger || e instanceof LiteralUnlimitedNatural) {
            elementInfo.put("type", "LiteralInteger");
            if (e instanceof LiteralInteger)
                elementInfo.put("integer", ((LiteralInteger)e).getValue());
            else
                elementInfo.put("integer", ((LiteralUnlimitedNatural)e).getValue());
        } else if (e instanceof LiteralReal) {
            elementInfo.put("type", "LiteralReal");
            elementInfo.put("double", ((LiteralReal)e).getValue());
        } else if (e instanceof Expression) {
            elementInfo.put("type", "Expression");
        } else if (e instanceof ElementValue) {
            elementInfo.put("type", "ElementValue");
            Element ev = ((ElementValue)e).getElement();
            if (ev != null) {
                addToElements(ev, 0);
                elementValues.put(e.getID(), ev.getID());
            } */
        } else {
            elementInfo.put("type", "Element");
        }
        if (StereotypesHelper.hasStereotypeOrDerived(e, view))
            elementInfo.put("isView", true);
        else
            elementInfo.put("isView", false);
        if (StereotypesHelper.hasStereotypeOrDerived(e, viewpoint))
            elementInfo.put("type", "Viewpoint");
        if (e instanceof NamedElement) {
            elementInfo.put("name", ((NamedElement)e).getName());
        } else
            elementInfo.put("name", "");
        elementInfo.put("documentation", ModelHelper.getComment(e));
        elementInfo.put("owner", e.getOwner().getID());
        elements.put(e.getID(), elementInfo);
        
        if ((depth != 0 && curdepth > depth) || curdepth == 0)
            return true;
        JSONArray children = new JSONArray();
        for (Element c: e.getOwnedElement()) {
            if (addToElements(c, curdepth+1))
                children.add(c.getID());
        }
        elementHierarchy.put(e.getID(), children);
        return true;
    }
    
    @SuppressWarnings("unchecked")
    private void addRelationship(DirectedRelationship dr) {
        JSONObject relInfo = new JSONObject();
        Element client = ModelHelper.getClientElement(dr);
        Element supplier = ModelHelper.getSupplierElement(dr);
        addToElements(client, 0);
        addToElements(supplier, 0);
        relInfo.put("source", client.getID());
        relInfo.put("target", supplier.getID());
        relationshipElements.put(dr.getID(), relInfo);
    }
    
    @SuppressWarnings("unchecked")
    private void addValues(Element e, JSONArray value, JSONObject elementInfo, ValueSpecification vs) {
        if (vs instanceof LiteralBoolean) {
            elementInfo.put("valueType", "LiteralBoolean");
            value.add(((LiteralBoolean)vs).isValue());
            elementInfo.put("boolean", value );
        } else if (vs instanceof LiteralString) {
            elementInfo.put("valueType", "LiteralString");
            value.add(((LiteralString)vs).getValue());
            elementInfo.put("string", value);
        } else if (vs instanceof LiteralInteger || vs instanceof LiteralUnlimitedNatural) {
            elementInfo.put("valueType", "LiteralInteger");
            if (vs instanceof LiteralInteger) {
                value.add(((LiteralInteger)vs).getValue());
            } else 
                value.add(((LiteralUnlimitedNatural)vs).getValue());
            elementInfo.put("integer", value);
        } else if (vs instanceof LiteralReal) {
            elementInfo.put("valueType", "LiteralReal");
            value.add(((LiteralReal)vs).getValue());
            elementInfo.put("double", value);
        } else if (vs instanceof Expression) {
            elementInfo.put("valueType", "Expression");
            value.add(RepresentationTextCreator.getRepresentedText(vs));
            elementInfo.put("expression", value);
        } else if (vs instanceof ElementValue) {
            elementInfo.put("valueType", "ElementValue");
            Element ev = ((ElementValue)vs).getElement();
            if (ev != null) {
                addToElements(ev, 0);
                value.add(ev.getID());
                elementValues.put(e.getID(), value);
            }
        }
    }
}
