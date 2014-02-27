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
import gov.nasa.jpl.mbee.ems.validation.PropertyValueType;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.uml.RepresentationTextCreator;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Expression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralBoolean;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralInteger;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralReal;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralUnlimitedNatural;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;

public class ExportValue extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    private static final long serialVersionUID = 1L;
    private Element element;
    
    public ExportValue(Element e) {
        super("ExportValue", "Export value", null, null);
        this.element = e;
    }
    
    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(Collection<Annotation> annos) {
        JSONObject send = new JSONObject();
        JSONArray infos = new JSONArray();
        Set<Element> set = new HashSet<Element>();
        for (Annotation anno: annos) {
            Element e = (Element)anno.getTarget();
            set.add(e);
            if (e instanceof Property || e instanceof Slot)
                infos.addAll(ExportUtility.getReferencedElements(e).values());
            JSONObject info = getInfo(e);
            infos.add(info);
        }
        if (!ExportUtility.okToExport(set))
            return;
        send.put("elements", infos);
        String url = ExportUtility.getPostElementsUrl();
        if (url == null) {
            return;
        }
        if (ExportUtility.send(url, send.toJSONString())) {
            this.removeViolationsAndUpdateWindow(annos);
            ExportUtility.sendProjectVersions();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!ExportUtility.okToExport(element))
            return;
        JSONObject info = getInfo(element);
        JSONArray elements = new JSONArray();
        JSONObject send = new JSONObject();
        if (element instanceof Property || element instanceof Slot)
            elements.addAll(ExportUtility.getReferencedElements(element).values());
        elements.add(info);
        send.put("elements", elements);
        String url = ExportUtility.getPostElementsUrl();
        if (url == null) {
            return;
        }
        if (ExportUtility.send(url, send.toJSONString())) {
            this.removeViolationAndUpdateWindow();
            ExportUtility.sendProjectVersion(element);
        }

    }

    @SuppressWarnings("unchecked")
    private JSONObject getInfo(Element e) {
        JSONObject elementInfo = new JSONObject();
        JSONArray value = new JSONArray();
        if (e instanceof Property) {
            ValueSpecification vs = ((Property)e).getDefaultValue();
            if (vs != null) {
                ExportUtility.addValues(e, value, elementInfo, vs);
            }
        } else if (e instanceof Slot) {
            List<ValueSpecification> vsl = ((Slot)e).getValue();
            if (vsl != null && vsl.size() > 0) {
                for (ValueSpecification vs: vsl) {
                    ExportUtility.addValues(e, value, elementInfo, vs);
                }
            }
        }
        elementInfo.put("id", ExportUtility.getElementID(e));
        return elementInfo;
    }
/*    
    @SuppressWarnings("unchecked")
    private void addValues(Element e, JSONArray value, JSONObject elementInfo, ValueSpecification vs) {
        if (vs instanceof LiteralBoolean) {
            elementInfo.put("valueType", PropertyValueType.LiteralBoolean.toString());
            value.add(((LiteralBoolean)vs).isValue());
        } else if (vs instanceof LiteralString) {
            elementInfo.put("valueType", PropertyValueType.LiteralString.toString());
            value.add(((LiteralString)vs).getValue());
        } else if (vs instanceof LiteralInteger || vs instanceof LiteralUnlimitedNatural) {
            elementInfo.put("valueType", PropertyValueType.LiteralInteger.toString());
            if (vs instanceof LiteralInteger) {
                value.add(((LiteralInteger)vs).getValue());
            } else 
                value.add(((LiteralUnlimitedNatural)vs).getValue());
        } else if (vs instanceof LiteralReal) {
            elementInfo.put("valueType", PropertyValueType.LiteralReal.toString());
            value.add(((LiteralReal)vs).getValue());
        } else if (vs instanceof Expression) {
            elementInfo.put("valueType", PropertyValueType.Expression.toString());
            value.add(RepresentationTextCreator.getRepresentedText(vs));
        } else if (vs instanceof ElementValue) {
            elementInfo.put("valueType", PropertyValueType.ElementValue.toString());
            Element ev = ((ElementValue)vs).getElement();
            if (ev != null) {
                value.add(ExportUtility.getElementID(ev));
            }
        } else if (vs instanceof InstanceValue) {
            elementInfo.put("valueType", PropertyValueType.ElementValue.toString());
            Element ev = ((InstanceValue)vs).getInstance();
            if (ev != null) {
                value.add(ExportUtility.getElementID(ev));
            }
        }
        elementInfo.put("value", value);
    }*/
}
