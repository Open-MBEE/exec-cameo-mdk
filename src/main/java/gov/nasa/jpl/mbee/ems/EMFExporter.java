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
 *
 * @author Johannes Gross
 ******************************************************************************/
package gov.nasa.jpl.mbee.ems;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdtemplates.StringExpression;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.Duration;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.DurationInterval;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.TimeExpression;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdsimpletime.TimeInterval;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectorEnd;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.metadata.UMLPackage;
import gov.nasa.jpl.mbee.api.function.TriFunction;
import gov.nasa.jpl.mbee.lib.ClassUtils;
import gov.nasa.jpl.mbee.lib.Utils;
import java.util.function.BiPredicate;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class EMFExporter implements Function<Element, JSONObject> {
    @Override
    public JSONObject apply(Element element) {
        return createElement(element);
    }

    private static JSONObject createElement(Element element) {
        debugUMLPackageLiterals();
        // showElementMetaModel((NamedElement) element);

        JSONObject elementInfo = new JSONObject();
        /*if (element.eClass().getEIDAttribute() == null) {
            return null;
        }
        elementInfo.put("sysmlId", getEID(element));
        elementInfo.put("type", element.eClass().getName());
        if (UMLPackage.Literals.CONNECTOR.equals(element.eClass())) {
            fillConnectorSpecialization((Connector) element, elementInfo);
        }
        for (EAttribute sf : element.eClass().getEAllAttributes()) {
            if (sf.isDerived()) {
                continue;
            }
            if (element.eClass().getEIDAttribute().equals(sf)) {
                continue;
            }

            Object val = element.eGet(sf);
            if (val == null) {
                elementInfo.put(sf.getName(), null);
            }
            else if (val instanceof EList) {
                System.out.println("ELIST : " + val + " : " + val.getClass());
                elementInfo.put(sf.getName(), val);
            }
            else if (val instanceof EObject) {
                elementInfo.put(sf.getName(), val);
            }
            else if (val instanceof Boolean) {
                elementInfo.put(sf.getName(), val);
            }
            else {
                if (!val.toString().contains("html")) {
                    String escapedVal = StringEscapeUtils.escapeHtml(val.toString());
                    elementInfo.put(sf.getName(), escapedVal);
                }
            }
        }
        ArrayList<EReference> references = new ArrayList<EReference>();
        EList<EReference> refs = element.eClass().getEAllReferences();
        references.addAll(refs);
        EList<EReference> conts = element.eClass().getEAllContainments();
        references.removeAll(conts);

        for (EReference ref : references) {
            if (UMLPackage.Literals.ELEMENT__OWNER.equals(ref)) {
                elementInfo.put(UMLPackage.Literals.ELEMENT__OWNER.getName() + "Id", getEID(element.getOwner()));
                continue;
            }
            if (ref.isDerived() && !UMLPackage.Literals.DIRECTED_RELATIONSHIP__SOURCE.equals(ref) && !UMLPackage.Literals.DIRECTED_RELATIONSHIP__TARGET.equals(ref)) {
                continue;
            }

            Object val = element.eGet(ref);
            if (val != null) {
                if (val instanceof EList) {
                    System.out.println("ELIST : " + val + " : " + val.getClass());
                    JSONArray array = new JSONArray();
                    for (Object obs : ((EList) val)) {
                        if (obs instanceof EObject) {
                            if (!((EList<?>) val).isEmpty()) {
                                EObject eObject = (EObject) obs;
                                if (eObject.eClass().getEIDAttribute() != null && eObject instanceof Element) {
                                    array.add(getEID((Element) eObject));
                                }
                                else {
                                    System.out.println("Skipped: " + obs);
                                }
                            }
                        }
                    }
                    if (!array.isEmpty()) {
                        if (ref.getName().equals("source")) {
                            elementInfo.put("sourceId", array.get(0));
                        }
                        else if (ref.getName().equals("target")) {
                            elementInfo.put("targetId", array.get(0));
                        }
                        else {
                            elementInfo.put(ref.getName() + "Ids", array);
                        }
                    }
                }
                else if (val instanceof EObject) {
                    if (element.eClass().getEIDAttribute() != null) {
                        elementInfo.put(ref.getName() + "Id", getEID(element));
                    }
                    else {
                        System.out.println("Skipped: " + val);
                    }
                    // }
                }
            }

        }
        for (EReference ref : conts) {
            // if (!ref.getName().contains("_") & !"ownedDiagram".equals(ref.getName())) {
            if (!UMLPackage.Literals.NAMESPACE__OWNED_DIAGRAM.equals(ref)) {
                Object val = element.eGet(ref);
                if (val != null) {
                    if (val instanceof EList) {
                        System.out.println("ELIST : " + val + " : " + val.getClass());
                        if (!((EList<?>) val).isEmpty()) {
                            // if ("ownedAttribute".equals(ref.getName())) {
                            Iterator<?> it = ((EList<?>) val).iterator();
                            JSONArray childArray = new JSONArray();
                            JSONArray idArray = new JSONArray();

                            while (it.hasNext()) {
                                EObject eo = (EObject) it.next();
                                if (eo instanceof Element) {
                                    if (eo instanceof ValueSpecification) {
                                        childArray.add(fillValueSpecification((ValueSpecification) eo));
                                    }
                                    else {
                                        // fillElement((Element) eo);
                                        idArray.add(((Element) eo).getID());
                                    }
                                }
                            }
                            elementInfo.put(ref.getName() + "Ids", idArray);
                            if (!childArray.isEmpty()) {
                                elementInfo.put(ref.getName(), childArray);
                            }
                        }
                    }
                    else if (val instanceof EObject) {
                        if (val instanceof Element) {
                            if (val instanceof ValueSpecification) {
                                elementInfo.put(ref.getName(), fillValueSpecification((ValueSpecification) val));
                            }
                            else {
                                // fillElement((Element) val);
                                elementInfo.put(ref.getName() + "Id", getEID(((Element) val)));
                            }
                        }

                    }
                }
            }
        }*/

        for (EReference eReference : element.eClass().getEAllReferences()) {
            DEFAULT_EREFERENCE_FUNCTION.apply(element, eReference, elementInfo);
        }
        elementInfo.put("documentation", Utils.stripHtmlWrapper(ModelHelper.getComment(element)));
        fillMetatype(element, elementInfo);
        return elementInfo;
    }

    private static String getEID(EObject eObject) {
        if (eObject == null) {
            return null;
        }
        if (eObject instanceof Slot) {
            Slot slot = (Slot) eObject;
            if (slot.getOwner() == null || ((Slot) eObject).getDefiningFeature() == null) {
                return null;
            }
            return slot.getOwner().getID() + "-slot-" + slot.getDefiningFeature().getID();
        }
        if (eObject instanceof Model) {
            Model model = (Model) eObject;
            Project project = Project.getProject(model);
            if (eObject == project.getModel()) {
                return project.getPrimaryProject().getProjectID();
            }
        }
        return EcoreUtil.getID(eObject);
    }

    private static JSONObject fillConnectorSpecialization(Connector e, JSONObject elementInfo) {
        if (elementInfo == null) {
            elementInfo = new JSONObject();
        }
        elementInfo.put("type", "Connector");
        int i = 0;
        if (e.getEnd() == null) {
            return elementInfo;
        }
        for (ConnectorEnd end : e.getEnd()) {
            JSONArray propertyPath = new JSONArray();
            if (end.getRole() != null) {
                if (StereotypesHelper.hasStereotype(end, "NestedConnectorEnd")) {
                    List<Element> ps = StereotypesHelper.getStereotypePropertyValue(end, "NestedConnectorEnd", "propertyPath");
                    for (Element path : ps) {
                        if (path instanceof ElementValue) {
                            propertyPath.add(((ElementValue) path).getElement().getID());
                        }
                        else if (path instanceof Property) {
                            propertyPath.add(path.getID());
                        }
                    }
                }
                propertyPath.add(end.getRole().getID());
            }
            if (i == 0) {
                // specialization.put("sourceUpper", fillValueSpecification(end.getUpperValue(), null));
                // specialization.put("sourceLower", fillValueSpecification(end.getLowerValue(), null));
                elementInfo.put("endAPathIds", propertyPath);
            }
            else {
                // specialization.put("targetUpper", fillValueSpecification(end.getUpperValue(), null));
                // specialization.put("targetLower", fillValueSpecification(end.getLowerValue(), null));
                elementInfo.put("endBPathIds", propertyPath);
            }
            i++;
        }
        Association type = e.getType();
        elementInfo.put("typeId", (type == null) ? null : type.getID());
        elementInfo.put("kind", (e.getKind() == null) ? null : e.getKind().toString());
        return elementInfo;
    }

    private static JSONObject fillMetatype(Element e, JSONObject einfo) {
        JSONObject info = einfo;
        if (info == null) {
            info = new JSONObject();
            info.put("sysmlId", getEID(e));
        }
        // info.put("isMetatype", false);
        if (e instanceof Stereotype) {
            info.put("isMetatype", true);
            JSONArray metatypes = new JSONArray();
            for (Class c : ((Stereotype) e).getSuperClass()) {
                if (c instanceof Stereotype) {
                    metatypes.add(c.getID());
                }
            }
            for (Class c : StereotypesHelper.getBaseClasses((Stereotype) e)) {
                metatypes.add(c.getID());
            }
            info.put("metatypesId", metatypes);
        }
        if (e instanceof Class) {
            try {
                java.lang.Class c = StereotypesHelper.getClassOfMetaClass((Class) e);
                if (c != null) {
                    info.put("isMetatype", true);
                    info.put("metatypes", new JSONArray());
                }
            } catch (Exception ex) {
            }
        }
        List<Stereotype> stereotypes = StereotypesHelper.getStereotypes(e);
        JSONArray applied = new JSONArray();
        for (Stereotype s : stereotypes) {
            applied.add(s.getID());
        }
        // Class baseClass = StereotypesHelper.getBaseClass(e);
        // if (baseClass != null)
        // applied.add(baseClass.getID());

        info.put("appliedStereotypeIds", applied);
        return info;
    }

    private static JSONObject fillValueSpecification(ValueSpecification vs) {
        if (vs == null) {
            return null;
        }
        JSONObject elementInfo = new JSONObject();
        if (vs instanceof InstanceValue) {
            elementInfo.put("type", "InstanceValue");
            InstanceValue iv = (InstanceValue) vs;
            InstanceSpecification i = iv.getInstance();
            elementInfo.put("instance", ((i != null) ? i.getID() : null));
        }
        else if (vs instanceof LiteralSpecification) {
            fillLiteralSpecification(vs, elementInfo);
        }
        else if (vs instanceof ElementValue) {
            elementInfo.put("type", "ElementValue");
            Element elem = ((ElementValue) vs).getElement();
            elementInfo.put("elementId", ((elem != null) ? getEID(elem) : null));
        }
        else if (vs instanceof StringExpression) {
            createStringExpression(vs, elementInfo);
        }
        else if (vs instanceof Expression) {
            elementInfo.put("type", "Expression");
            expressionPutOperand(vs, elementInfo);
        }
        else if (vs instanceof OpaqueExpression) {
            elementInfo.put("type", "OpaqueExpression");
            List<String> body = ((OpaqueExpression) vs).getBody();
            elementInfo.put("body", ((body != null) ? makeJsonArray(body) : new JSONArray()));
            elementInfo.put("language", ((((OpaqueExpression) vs).getLanguage() != null) ? makeJsonArray(((OpaqueExpression) vs).getLanguage()) : new JSONArray()));
        }
        else if (vs instanceof TimeExpression) {
            elementInfo.put("type", "TimeExpression");
        }
        else if (vs instanceof Duration) {
            elementInfo.put("type", "Duration");
        }
        else if (vs instanceof DurationInterval) {
            elementInfo.put("type", "DurationInterval");
            elementInfo.put("min", null);
            elementInfo.put("max", null);
        }
        else if (vs instanceof TimeInterval) {
            elementInfo.put("type", "TimeInterval");
            elementInfo.put("min", null);
            elementInfo.put("max", null);
        }
        return elementInfo;
    }

    private static void createStringExpression(ValueSpecification vs, JSONObject elementInfo) {
        elementInfo.put("type", "StringExpression");
        expressionPutOperand(vs, elementInfo);
        for (StringExpression subexp : (((StringExpression) vs).getSubExpression())) {
            createStringExpression(vs, elementInfo);
        }
    }

    private static void expressionPutOperand(ValueSpecification vs, JSONObject elementInfo) {
        List<ValueSpecification> vsl = ((Expression) vs).getOperand();
        if (vsl != null && vsl.size() > 0) {
            JSONArray operand = new JSONArray();
            for (ValueSpecification vs2 : vsl) {
                operand.add(fillValueSpecification(vs2));
            }
            elementInfo.put("operand", operand);
        }
    }

    private static void fillLiteralSpecification(ValueSpecification vs, JSONObject elementInfo) {
        if (vs instanceof LiteralBoolean) {
            elementInfo.put("type", "LiteralBoolean");
            elementInfo.put("booleanValue", ((LiteralBoolean) vs).isValue());
        }
        else if (vs instanceof LiteralInteger) {
            elementInfo.put("type", "LiteralInteger");
            elementInfo.put("value", (long) ((LiteralInteger) vs).getValue());
        }
        else if (vs instanceof LiteralNull) {
            elementInfo.put("type", "LiteralNull");
        }
        else if (vs instanceof LiteralReal) {
            elementInfo.put("type", "LiteralReal");
            double real = ((LiteralReal) vs).getValue();
            elementInfo.put("value", real);
        }
        else if (vs instanceof LiteralString) {
            elementInfo.put("type", "LiteralString");
            elementInfo.put("value", Utils.stripHtmlWrapper(((LiteralString) vs).getValue()));
        }
        else if (vs instanceof LiteralUnlimitedNatural) {
            elementInfo.put("type", "LiteralUnlimitedNatural");
            elementInfo.put("value", (long) ((LiteralUnlimitedNatural) vs).getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> JSONArray makeJsonArray(Collection<T> collection) {
        JSONArray arr = new JSONArray();
        for (T t : collection) {
            if (t != null) {
                arr.add(t);
            }
        }
        return arr;
    }

    private static void debugUMLPackageLiterals() {
        for (Field field : UMLPackage.Literals.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                try {
                    field.setAccessible(true);
                    Object o = field.get(null);
                    System.out.println(field.getName() + ": " + o);
                    if (o instanceof EReference) {
                        EReference eReference = (EReference) o;
                        System.out.println(" --- " + eReference.getEReferenceType() + " : " + eReference.getEReferenceType().getInstanceClass());
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static final Function<Object, Object> DEFAULT_SERIALIZATION_FUNCTION = object -> {
        if (object == null) {
            return null;
        }
        else if (object instanceof Collection) {
            JSONArray jsonArray = new JSONArray();
            for (Object o : ((Collection) object)) {
                Object serializable = EMFExporter.DEFAULT_SERIALIZATION_FUNCTION.apply(o);
                if (serializable == null && o != null) {
                    // failed to serialize; taking the conservative approach and returning entire thing as null
                    return null;
                }
                jsonArray.add(serializable);
            }
            return jsonArray;
        }
        else if (object instanceof EObject) {
            return EMFExporter.DEFAULT_SERIALIZATION_FUNCTION.apply(getEID(((EObject) object)));
        }
        else if (object instanceof String || ClassUtils.isPrimitive(object)) {
            return object;
        }
        // if we get here we have no idea what to do with this object
        return null;
    };

    private static final TriFunction<Element, EReference, JSONObject, JSONObject> DEFAULT_EREFERENCE_FUNCTION = (element, eReference, jsonObject) -> {
        if (eReference.isDerived()) {
            return jsonObject;
        }

        Object value = element.eGet(eReference);
        Object serializedValue = DEFAULT_SERIALIZATION_FUNCTION.apply(value);
        if (value != null && serializedValue == null) {
            System.out.println("[EMF] Failed to serialize " + eReference + " for " + element + ".");
            return jsonObject;
        }

        String key = eReference.getName();
        if (EObject.class.isAssignableFrom(eReference.getEReferenceType().getInstanceClass())) {
            key += "Id";
            if (eReference.getUpperBound() < 0 || eReference.getUpperBound() > 1) {
                key += "s";
            }
        }
        jsonObject.put(key, serializedValue);
        return jsonObject;
    };

    public enum EReferenceOverride {

        ;

        private BiPredicate<Element, EReference> predicate;
        private BiFunction<Element, JSONObject, JSONObject> function;

        EReferenceOverride(BiPredicate<Element, EReference> predicate, BiFunction<Element, JSONObject, JSONObject> function) {
            this.predicate = predicate;
            this.function = function;
        }
    }
}
