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
import gov.nasa.jpl.mbee.lib.Debug;
import gov.nasa.jpl.mbee.lib.Utils;
import gov.nasa.jpl.mbee.lib.Utils2;
import gov.nasa.jpl.mgss.mbee.docgen.validation.IRuleViolationAction;
import gov.nasa.jpl.mgss.mbee.docgen.validation.RuleViolationAction;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.nomagic.magicdraw.annotation.Annotation;
import com.nomagic.magicdraw.annotation.AnnotationAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.openapi.uml.SessionManager;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceSpecification;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.InstanceValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralBoolean;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralInteger;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralReal;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralString;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.LiteralUnlimitedNatural;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Property;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Slot;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.impl.ElementsFactory;

public class ImportValue extends RuleViolationAction implements AnnotationAction, IRuleViolationAction {

    private static final long serialVersionUID = 1L;
    private Element element;
    private JSONArray values;
    private PropertyValueType type;
    private ElementsFactory ef = Application.getInstance().getProject().getElementsFactory();
    private JSONObject result;
    public ImportValue(Element e, JSONArray values, PropertyValueType type, JSONObject result) {
        super("ImportValue", "Import value", null, null);
        this.element = e;
        this.values = values;
        this.type = type;
        this.result = result;
    }
    
    @Override
    public boolean canExecute(Collection<Annotation> arg0) {
        return true;
    }

    @Override
    public void execute(Collection<Annotation> annos) {
        SessionManager.getInstance().createSession("Change values");
        Collection<Annotation> toremove = new HashSet<Annotation>();
        try {
            for (Annotation anno: annos) {
                Element e = (Element)anno.getTarget();
                if (!e.isEditable()) {
                    Application.getInstance().getGUILog().log("[ERROR] " + element.getHumanName() + " is not editable!");
                    continue;
                }
                PropertyValueType valueType = PropertyValueType.valueOf((String)((Map<String, JSONObject>)result.get("elementsKeyed")).get(e.getID()).get("valueType"));
                JSONArray vals = (JSONArray)((Map<String, JSONObject>)result.get("elementsKeyed")).get(e.getID()).get("value");
                if (e instanceof Property) {
                    if (vals == null || vals.isEmpty()) {
                        ((Property)e).setDefaultValue(null);
                    } else {
                        update((Property)e, valueType, vals.get(0));
                    }
                } else if (e instanceof Slot) {
                    if (values == null || values.isEmpty()) {
                        ((Slot)element).getValue().clear();
                    } else {
                        update((Slot)e, type, values);
                    }
                }
                //AnnotationManager.getInstance().remove(annotation);
                toremove.add(anno);
            }
            SessionManager.getInstance().closeSession();
            saySuccess();
            //AnnotationManager.getInstance().update();
            this.removeViolationsAndUpdateWindow(toremove);
        } catch (Exception ex) {
            SessionManager.getInstance().cancelSession();
            Utils.printException(ex);
        }
        
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!element.isEditable()) {
            Application.getInstance().getGUILog().log("[ERROR] " + element.getHumanName() + " is not editable!");
            return;
        }
        SessionManager.getInstance().createSession("Change value");
        try {
            if (element instanceof Property) {
                if (values == null || values.isEmpty()) {
                    ((Property)element).setDefaultValue(null);
                } else {
                    update((Property)element, type, values.get(0));
                }
            } else if (element instanceof Slot) {
                if (values == null || values.isEmpty()) {
                    ((Slot)element).getValue().clear();
                } else {
                    update((Slot)element, type, values);
                }
            }
            SessionManager.getInstance().closeSession();
            saySuccess();
            //AnnotationManager.getInstance().remove(annotation);
            //AnnotationManager.getInstance().update();
            this.removeViolationAndUpdateWindow();
        } catch (Exception ex) {
            SessionManager.getInstance().cancelSession();
            Utils.printException(ex);
        }
    }
    
    // TODO -- move to Utils and have setProperty() call this instead of always creating a new Property?
    private void update(Property e, PropertyValueType valueType, Object o) {
        //use nondestructive update if possible
        ValueSpecification newval = e.getDefaultValue();
        switch ( valueType ) {
        case LiteralString:
            if (newval instanceof LiteralString) {
                ((LiteralString)newval).setValue((String)o);
                return;
            } 
            newval = ef.createLiteralStringInstance();
            ((LiteralString)newval).setValue((String)o);
            break;
        case LiteralInteger:
            if (newval instanceof LiteralInteger) {
                ((LiteralInteger)newval).setValue(((Long)o).intValue());
                return;
            } else if (newval instanceof LiteralUnlimitedNatural) {
                ((LiteralUnlimitedNatural)newval).setValue(((Long)o).intValue());
                return;
            }
            newval = ef.createLiteralIntegerInstance();
            ((LiteralInteger)newval).setValue(((Long)o).intValue());
            break;
        case LiteralBoolean:
            if (newval instanceof LiteralBoolean) {
                ((LiteralBoolean)newval).setValue((Boolean)o);
                return;
            }
            newval = ef.createLiteralBooleanInstance();
            ((LiteralBoolean)newval).setValue((Boolean)o);
            break;
        case LiteralUnlimitedNatural:
            if (newval instanceof LiteralUnlimitedNatural) {
                ((LiteralUnlimitedNatural)newval).setValue(((Long)o).intValue());
                return;
            }
            newval = ef.createLiteralUnlimitedNaturalInstance();
            ((LiteralUnlimitedNatural)newval).setValue(((Long)o).intValue());
            break;
        case LiteralReal:
            Double value;
            if (o instanceof Long)
                value = Double.parseDouble(((Long)o).toString());
            else
                value = (Double)o;
            if (newval instanceof LiteralReal) {
                ((LiteralReal)newval).setValue((Double)o);
                return;
            }
            newval = ef.createLiteralRealInstance();
            ((LiteralReal)newval).setValue(value);
            break;
        case ElementValue:
            Element find = ExportUtility.getElementFromID((String)o);
            if (find == null) {
                Application.getInstance().getGUILog().log("Element with id " + o + " not found!");
                return;
            }
            if (newval instanceof ElementValue) {
                ((ElementValue)newval).setElement(find);
                return;
            } else if (newval instanceof InstanceValue) {
                if (!(find instanceof InstanceSpecification)) {
                    Application.getInstance().getGUILog().log("Element with id " + o + " is not an instance spec, cannot be put into an InstanceValue.");
                    return;
                }
                ((InstanceValue)newval).setInstance((InstanceSpecification)find);
                return;
            }
            newval = ef.createElementValueInstance();
            ((ElementValue)newval).setElement(find);
            break;
        default:
            Debug.error("Bad PropertyValueType: " + valueType);
        };
        e.setDefaultValue(newval);
        return;
    }
    
    // TODO -- move to Utils and have setSlot() call this instead of always creating a new Slot?
    private void update(Slot e, PropertyValueType valueType, ValueSpecification vs, Object o, int i) {
        ValueSpecification newval = vs; 
        /*if ( !Utils2.isNullOrEmpty( e.getValue() ) ) {
            for ( ValueSpecification v : e.getValue() ) {
                PropertyValueType modelType = PropertyValueType.toPropertyValueType( v );
                if ( valueType == modelType ||
                     valueType == PropertyValueType.ElementValue && v instanceof InstanceValue ||
                     valueType == PropertyValueType.LiteralInteger && v instanceof LiteralUnlimitedNatural) {
                    newval = v;
                    break;
                }
            }
        }
        if ( newval == null && !Utils2.isNullOrEmpty( e.getValue() ) ) {
            e.getValue().clear();
        }*/
        switch ( valueType ) {
        case LiteralString:
            if (newval instanceof LiteralString) {
                ((LiteralString)newval).setValue((String)o);
                return;
            } 
            newval = ef.createLiteralStringInstance();
            ((LiteralString)newval).setValue((String)o);
            break;
        case LiteralInteger:
            if (newval instanceof LiteralInteger) {
                ((LiteralInteger)newval).setValue(((Long)o).intValue());
                return;
            } else if (newval instanceof LiteralUnlimitedNatural) {
                ((LiteralUnlimitedNatural)newval).setValue(((Long)o).intValue());
                return;
            }
            newval = ef.createLiteralIntegerInstance();
            ((LiteralInteger)newval).setValue(((Long)o).intValue());
            break;
        case LiteralBoolean:
            if (newval instanceof LiteralBoolean) {
                ((LiteralBoolean)newval).setValue((Boolean)o);
                return;
            }
            newval = ef.createLiteralBooleanInstance();
            ((LiteralBoolean)newval).setValue((Boolean)o);
            break;
        case LiteralUnlimitedNatural:
            if (newval instanceof LiteralUnlimitedNatural) {
                ((LiteralUnlimitedNatural)newval).setValue(((Long)o).intValue());
                return;
            }
            newval = ef.createLiteralUnlimitedNaturalInstance();
            ((LiteralUnlimitedNatural)newval).setValue(((Long)o).intValue());
            break;
        case LiteralReal:
            Double value;
            if (o instanceof Long)
                value = Double.parseDouble(((Long)o).toString());
            else
                value = (Double)o;
            if (newval instanceof LiteralReal) {
                ((LiteralReal)newval).setValue(value);
                return;
            }
            newval = ef.createLiteralRealInstance();
            ((LiteralReal)newval).setValue(value);
            break;
        case ElementValue:
            Element find = ExportUtility.getElementFromID((String)o);
            if (find == null) {
                Application.getInstance().getGUILog().log("Element with id " + o + " not found!");
                return;
            }
            if (newval instanceof ElementValue) {
                ((ElementValue)newval).setElement(find);
                return;
            } else if (newval instanceof InstanceValue) {
                if (!(find instanceof InstanceSpecification)) {
                    Application.getInstance().getGUILog().log("Element with id " + o + " is not an instance spec, cannot be put into an InstanceValue.");
                    return;
                }
                ((InstanceValue)newval).setInstance((InstanceSpecification)find);
                return;
            }
            newval = ef.createElementValueInstance();
            ((ElementValue)newval).setElement(find);
            break;
        case InstanceValue:
            if (newval instanceof InstanceValue) {
                ((InstanceValue)newval).setInstance((InstanceSpecification)ExportUtility.getElementFromID((String)o));
                return;
            }
            newval = ef.createInstanceValueInstance();
            ((InstanceValue)newval).setInstance((InstanceSpecification)ExportUtility.getElementFromID((String)o));
            break;
        };
        //if ( e.getValue() != null && e.getValue().isEmpty() ) {
        if (e.getValue().size() > i)
            e.getValue().set(i, newval );
        else
            e.getValue().add(newval);
        //}
        return;
    }
    
    private void update(Slot e, PropertyValueType valueType, JSONArray values) {
        if ( e == null ) {
            Debug.error( "Trying to update a null slot!" );
            return;
        }
        for (int i = 0; i < values.size(); i++) {
            if (e.getValue().size() > i) {
                update(e, valueType, e.getValue().get(i), values.get(i), i);
            } else
                update(e, valueType, null, values.get(i), i);
        }
        /*if ( values.size() != 1 ) {
            Application.getInstance().getGUILog().log("[ERROR] " + e.getHumanName() + " must have exactly one value but is being updated with " + values.size() + "!");
            return;
        }
        if ( e.getValue() != null && e.getValue().size() > 1 ) {
            Application.getInstance().getGUILog().log("[ERROR] " + e.getHumanName() + " must have exactly one value to update, but there are " + e.getValue().size() + "!");
            return;
        }
        Object v = values.get( 0 );
        //Utils.setSlotValue((Slot)e, v);
        update( e, valueType, v );*/
    }
}
