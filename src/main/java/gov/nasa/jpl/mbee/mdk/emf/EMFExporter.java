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
package gov.nasa.jpl.mbee.mdk.emf;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Class;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectorEnd;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.metadata.UMLPackage;
import gov.nasa.jpl.mbee.mdk.api.function.TriFunction;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.lib.ClassUtils;
import gov.nasa.jpl.mbee.mdk.lib.Utils;
import org.eclipse.emf.ecore.EDataType;
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
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class EMFExporter implements BiFunction<Element, Project, JSONObject> {
    @Override
    public JSONObject apply(Element element, Project project) {
        return convert(element, project);
    }

    private static JSONObject convert(Element element, Project project) {
        return convert(element, project, false);
    }

    private static JSONObject convert(Element element, Project project, boolean nestedValueSpecification) {
        if (element == null) {
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        for (PreProcessor preProcessor : PreProcessor.values()) {
            if (nestedValueSpecification && preProcessor == PreProcessor.VALUE_SPECIFICATION) {
                continue;
            }
            jsonObject = preProcessor.getFunction().apply(element, project, jsonObject);
            if (jsonObject == null) {
                return null;
            }
        }
        for (EStructuralFeature eStructuralFeature : element.eClass().getEAllStructuralFeatures()) {
            ExportFunction function = Arrays.stream(EStructuralFeatureOverride.values())
                    .filter(override -> override.getPredicate().test(element, eStructuralFeature)).map(EStructuralFeatureOverride::getFunction)
                    .findAny().orElse(DEFAULT_E_STRUCTURAL_FEATURE_FUNCTION);
            jsonObject = function.apply(element, project, eStructuralFeature, jsonObject);
            if (jsonObject == null) {
                return null;
            }
        }
        return jsonObject;
    }

    public static String getEID(EObject eObject) {
        if (eObject == null) {
            return null;
        }
        if (eObject instanceof Slot) {
            Slot slot = (Slot) eObject;
            if (slot.getOwner() == null || ((Slot) eObject).getDefiningFeature() == null) {
                return null;
            }
            return getEID(slot.getOwner()) + "-slot-" + getEID(slot.getDefiningFeature());
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
            JSONArray metatypes = ((Stereotype) e).getSuperClass().stream().filter(c -> c instanceof Stereotype).map(EMFExporter::getEID).collect(Collectors.toCollection(JSONArray::new));
            metatypes.addAll(StereotypesHelper.getBaseClasses((Stereotype) e).stream().map(EMFExporter::getEID).collect(Collectors.toList()));
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
        JSONArray applied = stereotypes.stream().map(EMFExporter::getEID).collect(Collectors.toCollection(JSONArray::new));
        einfo.put("appliedStereotypeIds", applied);
        return einfo;
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

    private enum PreProcessor {
        TYPE(
                (element, project, jsonObject) -> {
                    jsonObject.put("type", element.eClass().getName());
                    return jsonObject;
                }
        ),
        METATYPE(
                (element, project, jsonObject) -> {
                    if (element instanceof Stereotype) {
                        jsonObject.put("isMetatype", true);
                        JSONArray metatypes = ((Stereotype) element).getSuperClass().stream().filter(c -> c instanceof Stereotype).map(EMFExporter::getEID).collect(Collectors.toCollection(JSONArray::new));
                        metatypes.addAll(StereotypesHelper.getBaseClasses((Stereotype) element).stream().map(EMFExporter::getEID).collect(Collectors.toList()));
                        jsonObject.put("metatypesId", metatypes);
                    }
                    if (element instanceof Class) {
                        try {
                            java.lang.Class c = StereotypesHelper.getClassOfMetaClass((Class) element);
                            if (c != null) {
                                jsonObject.put("isMetatype", true);
                                jsonObject.put("metatypes", new JSONArray());
                            }
                        } catch (Exception ex) {
                        }
                    }
                    List<Stereotype> stereotypes = StereotypesHelper.getStereotypes(element);
                    JSONArray applied = stereotypes.stream().map(EMFExporter::getEID).collect(Collectors.toCollection(JSONArray::new));
                    jsonObject.put("_appliedStereotypeIds", applied);
                    return jsonObject;
                }
        ),
        DOCUMENTATION(
                (element, project, jsonObject) -> {
                    jsonObject.put("documentation", Utils.stripHtmlWrapper(ModelHelper.getComment(element)));
                    return jsonObject;
                }
        ),
        SITE_CHARACTERIZATION(
                (element, project, jsonObject) -> {
                    if (element instanceof Package) {
                        jsonObject.put("_isSite", Utils.isSiteChar((Package) element));
                    }
                    return jsonObject;
                }
        ),
        VALUE_SPECIFICATION(
                (element, project, jsonObject) -> element instanceof ValueSpecification ? null : jsonObject
        ),
        CONNECTOR_END(
                (element, project, jsonObject) -> element instanceof ConnectorEnd ? null : jsonObject
        ),
        DIAGRAM(
                (element, project, jsonObject) -> element instanceof Diagram ? null : jsonObject
        ),
        SYNC(
                (element, project, jsonObject) -> element.getID().endsWith(MDKConstants.SYNC_SYSML_ID_SUFFIX) ||
                        element.getOwner() != null && element.getOwner().getID().endsWith(MDKConstants.SYNC_SYSML_ID_SUFFIX) ? null : jsonObject
        ),
        ATTACHED_PROJECT(
                (element, project, jsonObject) -> ProjectUtilities.isElementInAttachedProject(element) ? null : jsonObject
        );

        private TriFunction<Element, Project, JSONObject, JSONObject> function;

        PreProcessor(TriFunction<Element, Project, JSONObject, JSONObject> function) {
            this.function = function;
        }

        public TriFunction<Element, Project, JSONObject, JSONObject> getFunction() {
            return function;
        }
    }

    private static final SerializationFunction DEFAULT_SERIALIZATION_FUNCTION = (object, project, eStructuralFeature) -> {
        if (object == null) {
            return null;
        }
        else if (object instanceof Collection) {
            JSONArray jsonArray = new JSONArray();
            for (Object o : ((Collection<?>) object)) {
                Object serialized = EMFExporter.DEFAULT_SERIALIZATION_FUNCTION.apply(o, project, eStructuralFeature);
                if (serialized == null && o != null) {
                    // failed to serialize; taking the conservative approach and returning entire thing as null
                    return null;
                }
                jsonArray.add(serialized);
            }
            return jsonArray;
        }
        else if (object instanceof ValueSpecification) {
            return convert((ValueSpecification) object, project, true);
            //return fillValueSpecification((ValueSpecification) object);
        }
        else if (eStructuralFeature instanceof EReference && object instanceof EObject) {
            return EMFExporter.DEFAULT_SERIALIZATION_FUNCTION.apply(getEID(((EObject) object)), project, eStructuralFeature);
        }
        else if (eStructuralFeature.getEType() instanceof EDataType) {
            return EcoreUtil.convertToString((EDataType) eStructuralFeature.getEType(), object);
            //return ((Enumerator) object).getLiteral();
        }
        else if (object instanceof String || ClassUtils.isPrimitive(object)) {
            return object;
        }
        // if we get here we have no idea what to do with this object
        return null;
    };

    private static final ExportFunction DEFAULT_E_STRUCTURAL_FEATURE_FUNCTION = (element, project, eStructuralFeature, jsonObject) -> {
        if (!eStructuralFeature.isChangeable() || eStructuralFeature.isVolatile() || eStructuralFeature.isTransient() || eStructuralFeature.isUnsettable() || eStructuralFeature.isDerived() || eStructuralFeature.getName().startsWith("_")) {
            return EMFExporter.EMPTY_E_STRUCTURAL_FEATURE_FUNCTION.apply(element, project, eStructuralFeature, jsonObject);
        }
        return EMFExporter.UNCHECKED_E_STRUCTURAL_FEATURE_FUNCTION.apply(element, project, eStructuralFeature, jsonObject);
    };

    private static final ExportFunction UNCHECKED_E_STRUCTURAL_FEATURE_FUNCTION = (element, project, eStructuralFeature, jsonObject) -> {
        Object value = element.eGet(eStructuralFeature);
        Object serializedValue = DEFAULT_SERIALIZATION_FUNCTION.apply(value, project, eStructuralFeature);
        if (value != null && serializedValue == null) {
            System.err.println("[EMF] Failed to serialize " + eStructuralFeature + " for " + element + ": " + value + " - " + value.getClass());
            return jsonObject;
        }

        String key = eStructuralFeature.getName();
        if (eStructuralFeature instanceof EReference && EObject.class.isAssignableFrom(((EReference) eStructuralFeature).getEReferenceType().getInstanceClass())
                && !ValueSpecification.class.isAssignableFrom(((EReference) eStructuralFeature).getEReferenceType().getInstanceClass())) {
            key += "Id" + (eStructuralFeature.isMany() ? "s" : "");
        }
        jsonObject.put(key, serializedValue);
        return jsonObject;
    };

    private static final ExportFunction EMPTY_E_STRUCTURAL_FEATURE_FUNCTION = (element, project, eStructuralFeature, jsonObject) -> jsonObject;

    private enum EStructuralFeatureOverride {
        ID(
                (element, eStructuralFeature) -> eStructuralFeature == element.eClass().getEIDAttribute(),
                (element, project, eStructuralFeature, jsonObject) -> {
                    jsonObject.put("sysmlId", getEID(element));
                    return jsonObject;
                }
        ),
        OWNER(
                (element, eStructuralFeature) -> UMLPackage.Literals.ELEMENT__OWNER == eStructuralFeature,
                (element, project, eStructuralFeature, jsonObject) -> {
                    //UNCHECKED_E_STRUCTURAL_FEATURE_FUNCTION.apply(element, UMLPackage.Literals.ELEMENT__OWNER, jsonObject);
                    // safest way to prevent circular references, like with ValueSpecifications
                    jsonObject.put(UMLPackage.Literals.ELEMENT__OWNER.getName() + "Id", getEID(element.getOwner()));
                    return jsonObject;
                }
        ),
        OWNING(
                (element, eStructuralFeature) -> eStructuralFeature.getName().startsWith("owning"),
                EMPTY_E_STRUCTURAL_FEATURE_FUNCTION
        ),
        OWNED(
                (element, eStructuralFeature) -> eStructuralFeature.getName().startsWith("owned") && !eStructuralFeature.isOrdered(),
                EMPTY_E_STRUCTURAL_FEATURE_FUNCTION
        ),
        PACKAGED_ELEMENT(
                (element, eStructuralFeature) -> UMLPackage.Literals.PACKAGE__PACKAGED_ELEMENT == eStructuralFeature || UMLPackage.Literals.COMPONENT__PACKAGED_ELEMENT == eStructuralFeature,
                EMPTY_E_STRUCTURAL_FEATURE_FUNCTION
        ),
        NAMESPACE__OWNED_DIAGRAM(
                (element, eStructuralFeature) -> eStructuralFeature == UMLPackage.Literals.NAMESPACE__OWNED_DIAGRAM,
                EMPTY_E_STRUCTURAL_FEATURE_FUNCTION
        ),
        DIRECTED_RELATIONSHIP__SOURCE(
                (element, eStructuralFeature) -> UMLPackage.Literals.DIRECTED_RELATIONSHIP__SOURCE == eStructuralFeature,
                (element, project, eStructuralFeature, jsonObject) -> {
                    jsonObject.put("_" + eStructuralFeature.getName() + "Ids", DEFAULT_SERIALIZATION_FUNCTION.apply(element.eGet(eStructuralFeature), project, eStructuralFeature));
                    return jsonObject;
                }
        ),
        DIRECTED_RELATIONSHIP__TARGET(
                (element, eStructuralFeature) -> UMLPackage.Literals.DIRECTED_RELATIONSHIP__TARGET == eStructuralFeature,
                (element, project, eStructuralFeature, jsonObject) -> {
                    jsonObject.put("_" + eStructuralFeature.getName() + "Ids", DEFAULT_SERIALIZATION_FUNCTION.apply(element.eGet(eStructuralFeature), project, eStructuralFeature));
                    return jsonObject;
                }
        ),
        CONNECTOR__END(
                (element, eStructuralFeature) -> eStructuralFeature == UMLPackage.Literals.CONNECTOR__END,
                (element, project, eStructuralFeature, jsonObject) -> {
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
                    jsonObject.put("_propertyPathIds", DEFAULT_SERIALIZATION_FUNCTION.apply(propertyPaths, project, eStructuralFeature));

                    return DEFAULT_E_STRUCTURAL_FEATURE_FUNCTION.apply(element, project, eStructuralFeature, jsonObject);
                }
        ),
        VALUE_SPECIFICATION__EXPRESSION(
                (element, eStructuralFeature) -> eStructuralFeature == UMLPackage.Literals.VALUE_SPECIFICATION__EXPRESSION,
                (element, project, eStructuralFeature, jsonObject) -> {
                    Expression expression = null;
                    Object object = element.eGet(UMLPackage.Literals.VALUE_SPECIFICATION__EXPRESSION);
                    if (object instanceof Expression) {
                        expression = (Expression) object;
                    }
                    jsonObject.put(UMLPackage.Literals.VALUE_SPECIFICATION__EXPRESSION.getName() + "Id", expression != null ? expression.getID() : null);
                    return jsonObject;
                }
        );

        private BiPredicate<Element, EStructuralFeature> predicate;
        private ExportFunction function;

        EStructuralFeatureOverride(BiPredicate<Element, EStructuralFeature> predicate, ExportFunction function) {
            this.predicate = predicate;
            this.function = function;
        }

        public BiPredicate<Element, EStructuralFeature> getPredicate() {
            return predicate;
        }

        public ExportFunction getFunction() {
            return function;
        }
    }

    @FunctionalInterface
    interface SerializationFunction {
        Object apply(Object object, Project project, EStructuralFeature eStructuralFeature);
    }

    @FunctionalInterface
    interface ExportFunction {
        JSONObject apply(Element element, Project project, EStructuralFeature eStructuralFeature, JSONObject jsonObject);
    }
}
