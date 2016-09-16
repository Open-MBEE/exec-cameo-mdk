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
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EMFExporter implements Function<Element, JSONObject> {
    @Override
    public JSONObject apply(Element element) {
        return createElement(element);
    }

    private static JSONObject createElement(Element element) {
        //debugUMLPackageLiterals();

        JSONObject jsonObject = new JSONObject();
        for (EStructuralFeature eStructuralFeature : element.eClass().getEAllStructuralFeatures()) {
            TriFunction<Element, EStructuralFeature, JSONObject, JSONObject> function = Arrays.stream(EStructuralFeatureOverride.values())
                    .filter(override -> override.getPredicate().test(element, eStructuralFeature)).map(EStructuralFeatureOverride::getFunction)
                    .findAny().orElse(DEFAULT_E_STRUCTURAL_FEATURE_FUNCTION);
            jsonObject = function.apply(element, eStructuralFeature, jsonObject);
            if (jsonObject == null) {
                return null;
            }
        }
        jsonObject.put("type", element.eClass().getName());
        jsonObject.put("documentation", Utils.stripHtmlWrapper(ModelHelper.getComment(element)));
        fillMetatype(element, jsonObject);
        return jsonObject;
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

    private static JSONObject fillMetatype(Element e, JSONObject einfo) {
        // info.put("isMetatype", false);
        if (e instanceof Stereotype) {
            einfo.put("isMetatype", true);
            JSONArray metatypes = new JSONArray();
            for (Class c : ((Stereotype) e).getSuperClass()) {
                if (c instanceof Stereotype) {
                    metatypes.add(c.getID());
                }
            }
            for (Class c : StereotypesHelper.getBaseClasses((Stereotype) e)) {
                metatypes.add(c.getID());
            }
            einfo.put("metatypesId", metatypes);
        }
        if (e instanceof Class) {
            try {
                java.lang.Class c = StereotypesHelper.getClassOfMetaClass((Class) e);
                if (c != null) {
                    einfo.put("isMetatype", true);
                    einfo.put("metatypes", new JSONArray());
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

        einfo.put("appliedStereotypeIds", applied);
        return einfo;
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
        else if (object instanceof ValueSpecification) {
            return fillValueSpecification((ValueSpecification) object);
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

    private static final TriFunction<Element, EStructuralFeature, JSONObject, JSONObject> DEFAULT_E_STRUCTURAL_FEATURE_FUNCTION = (element, eStructuralFeature, jsonObject) -> {
        if (element.eClass().getEIDAttribute() == null) {
            System.out.println("[EMF] EID Attribute missing: " + element);
            return null;
        }
        if (!eStructuralFeature.isChangeable() || eStructuralFeature.isVolatile() || eStructuralFeature.isTransient() || eStructuralFeature.isUnsettable() || eStructuralFeature.isDerived() || eStructuralFeature.getName().startsWith("_")) {
            return jsonObject;
        }
        return EMFExporter.UNCHECKED_E_STRUCTURAL_FEATURE_FUNCTION.apply(element, eStructuralFeature, jsonObject);
    };

    private static final TriFunction<Element, EStructuralFeature, JSONObject, JSONObject> UNCHECKED_E_STRUCTURAL_FEATURE_FUNCTION = (element, eStructuralFeature, jsonObject) -> {
        Object value = element.eGet(eStructuralFeature);
        Object serializedValue = DEFAULT_SERIALIZATION_FUNCTION.apply(value);
        if (value != null && serializedValue == null) {
            System.out.println("[EMF] Failed to serialize " + eStructuralFeature + " for " + element + ": " + value);
            return jsonObject;
        }

        String key = eStructuralFeature.getName();
        if (eStructuralFeature instanceof EReference && EObject.class.isAssignableFrom(((EReference) eStructuralFeature).getEReferenceType().getInstanceClass())) {
            key += "Id";
            if (eStructuralFeature.getUpperBound() < 0 || eStructuralFeature.getUpperBound() > 1) {
                key += "s";
            }
        }
        jsonObject.put(key, serializedValue);
        return jsonObject;
    };

    private static final TriFunction<Element, EStructuralFeature, JSONObject, JSONObject> EMPTY_E_STRUCTURAL_FEATURE_FUNCTION = (element, eStructuralFeature, jsonObject) -> jsonObject;

    private enum EStructuralFeatureOverride {
        ID(
                (element, eStructuralFeature) -> eStructuralFeature == element.eClass().getEIDAttribute(),
                (element, eStructuralFeature, jsonObject) -> {
                    jsonObject.put("sysmlId", getEID(element));
                    return jsonObject;
                }
        ),
        OWNER(
                (element, eStructuralFeature) -> UMLPackage.Literals.PACKAGEABLE_ELEMENT__OWNING_PACKAGE == eStructuralFeature,
                (element, eStructuralFeature, jsonObject) -> UNCHECKED_E_STRUCTURAL_FEATURE_FUNCTION.apply(element, UMLPackage.Literals.ELEMENT__OWNER, jsonObject)
        ),
        DIRECTED_RELATIONSHIP__SOURCE(
                (element, eStructuralFeature) -> UMLPackage.Literals.DIRECTED_RELATIONSHIP__SOURCE == eStructuralFeature,
                UNCHECKED_E_STRUCTURAL_FEATURE_FUNCTION
        ),
        DIRECTED_RELATIONSHIP__TARGET(
                (element, eStructuralFeature) -> UMLPackage.Literals.DIRECTED_RELATIONSHIP__TARGET == eStructuralFeature,
                UNCHECKED_E_STRUCTURAL_FEATURE_FUNCTION
        ),
        NAMESPACE__OWNED_DIAGRAM(
                (element, eStructuralFeature) -> eStructuralFeature == UMLPackage.Literals.NAMESPACE__OWNED_DIAGRAM,
                EMPTY_E_STRUCTURAL_FEATURE_FUNCTION
        ),
        CONNECTOR(
                (element, eStructuralFeature) -> eStructuralFeature == UMLPackage.Literals.CONNECTOR__END,
                (element, eStructuralFeature, jsonObject) -> {
                    Connector connector = (Connector) element;
                    // TODO Stop using Strings @donbot
                    List<List<Object>> propertyPaths = connector.getEnd().stream()
                            .map(connectorEnd -> StereotypesHelper.hasStereotype(connectorEnd, "NestedConnectorEnd") ? StereotypesHelper.getStereotypePropertyValue(connectorEnd, "NestedConnectorEnd", "propertyPath") : null)
                            .map(elements -> {
                                if (elements == null) {
                                    return new ArrayList<>(1);
                                }
                                List<Object> list = new ArrayList<>(elements.size() + 1);
                                for (Object o : elements) {
                                    list.add(o instanceof ElementValue ? ((ElementValue) o).getElement() : o);
                                }
                                return list;
                            }).collect(Collectors.toList());
                    for (int i = 0; i < propertyPaths.size(); i++) {
                        propertyPaths.get(i).add(connector.getEnd().get(i).getRole());
                    }
                    jsonObject.put("pathsOfPropertyIds", DEFAULT_SERIALIZATION_FUNCTION.apply(propertyPaths));

                    return DEFAULT_E_STRUCTURAL_FEATURE_FUNCTION.apply(element, eStructuralFeature, jsonObject);
                }
        )
        ;

        private BiPredicate<Element, EStructuralFeature> predicate;
        private TriFunction<Element, EStructuralFeature, JSONObject, JSONObject> function;

        EStructuralFeatureOverride(BiPredicate<Element, EStructuralFeature> predicate, TriFunction<Element, EStructuralFeature, JSONObject, JSONObject> function) {
            this.predicate = predicate;
            this.function = function;
        }

        public BiPredicate<Element, EStructuralFeature> getPredicate() {
            return predicate;
        }

        public TriFunction<Element, EStructuralFeature, JSONObject, JSONObject> getFunction() {
            return function;
        }
    }
}
