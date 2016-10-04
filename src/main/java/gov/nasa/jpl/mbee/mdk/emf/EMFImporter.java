package gov.nasa.jpl.mbee.mdk.emf;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.BaseElement;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ValueSpecification;
import com.nomagic.uml2.ext.magicdraw.metadata.UMLFactory;
import com.nomagic.uml2.ext.magicdraw.metadata.UMLPackage;
import gov.nasa.jpl.mbee.mdk.api.function.TriFunction;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.JsonToElementFunction;
import gov.nasa.jpl.mbee.mdk.ems.ImportException;
import gov.nasa.jpl.mbee.mdk.ems.ReferenceException;
import gov.nasa.jpl.mbee.mdk.lib.Changelog;
import gov.nasa.jpl.mbee.mdk.lib.ClassUtils;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.UniqueEList;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.util.EcoreUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by igomes on 9/19/16.
 */
public class EMFImporter implements JsonToElementFunction {
    @Override
    public Changelog.Change<Element> apply(ObjectNode objectNode, Project project, Boolean strict) throws ImportException {
        return convert(objectNode, project, strict);
    }

    private synchronized static Changelog.Change<Element> convert(ObjectNode objectNode, Project project, Boolean strict) throws ImportException {
        UMLFactory.eINSTANCE.setRepository(project.getRepository());
        project.getCounter().setCanResetIDForObject(true);

        JsonNode jsonNode = objectNode.get(MDKConstants.SYSML_ID_KEY);
        if (jsonNode.isNull() || !jsonNode.isTextual()) {
            return null;
        }
        Element element = ELEMENT_LOOKUP_FUNCTION.apply(jsonNode.asText(), project);
        Changelog.ChangeType changeType = element != null ? Changelog.ChangeType.UPDATED : Changelog.ChangeType.CREATED;

        for (PreProcessor preProcessor : PreProcessor.values()) {
            element = preProcessor.getFunction().apply(objectNode, project, strict, element);
            if (element == null) {
                return null;
            }
        }
        for (EStructuralFeature eStructuralFeature : element.eClass().getEAllStructuralFeatures()) {
            final Element finalElement = element;
            ImportFunction function = Arrays.stream(EStructuralFeatureOverride.values()).filter(override -> override.getPredicate()
                    .test(objectNode, eStructuralFeature, project, strict, finalElement)).map(EStructuralFeatureOverride::getFunction)
                    .findAny().orElse(DEFAULT_E_STRUCTURAL_FEATURE_FUNCTION);
            element = function.apply(objectNode, eStructuralFeature, project, strict, element);
            if (element == null) {
                return null;
            }
        }
        return new Changelog.Change<>(element, changeType);
    }

    public enum PreProcessor {
        CREATE(
                (objectNode, project, strict, element) -> {
                    if (element != null) {
                        return element;
                    }
                    JsonNode jsonNode = objectNode.get(MDKConstants.TYPE_KEY);
                    if (jsonNode.isNull() || !jsonNode.isTextual()) {
                        return null;
                    }
                    String type = jsonNode.asText();
                    if (type.equals("View") || type.equals("Document")) {
                        type = "Class";
                    }
                    EClassifier eClassifier = UMLPackage.eINSTANCE.getEClassifier(type);
                    if (!(eClassifier instanceof EClass)) {
                        return null;
                    }
                    EObject eObject = UMLFactory.eINSTANCE.create((EClass) eClassifier);
                    if (!(eObject instanceof Element)) {
                        return null;
                    }
                    return (Element) eObject;
                }
        ),
        DOCUMENTATION(
                (objectNode, project, strict, element) -> {
                    JsonNode jsonNode = objectNode.get("documentation");
                    if (!jsonNode.isNull() && jsonNode.isTextual()) {
                        ModelHelper.setComment(element, jsonNode.asText());
                    }
                    return element;
                }
        ),
        SYSML_ID_VALIDATION(
                (objectNode, project, strict, element) -> {
                    JsonNode jsonNode = objectNode.get(MDKConstants.SYSML_ID_KEY);
                    if (jsonNode.isNull() || !jsonNode.isTextual()) {
                        return null;
                    }
                    String id = jsonNode.asText();
                    if (id.startsWith(MDKConstants.HIDDEN_ID_PREFIX) || id.startsWith(project.getPrimaryProject().getProjectID())) {
                        return null;
                    }
                    return element;
                }
        );

        private PreProcessorFunction function;

        PreProcessor(PreProcessorFunction function) {
            this.function = function;
        }

        public PreProcessorFunction getFunction() {
            return function;
        }
    }

    private static final Function<EStructuralFeature, String> KEY_FUNCTION = eStructuralFeature -> {
        String key = eStructuralFeature.getName();
        if (eStructuralFeature instanceof EReference && EObject.class.isAssignableFrom(((EReference) eStructuralFeature).getEReferenceType().getInstanceClass())
                && !ValueSpecification.class.isAssignableFrom(((EReference) eStructuralFeature).getEReferenceType().getInstanceClass())) {
            key += "Id" + (eStructuralFeature.isMany() ? "s" : "");
        }
        return key;
    };

    public static final BiFunction<String, Project, Element> ELEMENT_LOOKUP_FUNCTION = (id, project) -> {
        if (id.equals(project.getPrimaryProject().getProjectID())) {
            return project.getModel();
        }
        BaseElement baseElement = project.getElementByID(id);
        return baseElement instanceof Element ? (Element) baseElement : null;
    };

    private static final DeserializationFunction DEFAULT_DESERIALIZATION_FUNCTION = (key, jsonNode, ignoreMultiplicity, objectNode, eStructuralFeature, project, strict, element) -> {
        if (jsonNode == null || jsonNode instanceof NullNode) {
            return null;
        }
        else if (!ignoreMultiplicity && eStructuralFeature.isMany()) {
            if (!(jsonNode instanceof ArrayNode)) {
                if (strict) {
                    throw new ImportException(element, objectNode, "Expected ArrayNode for key \"" + key + "\" in JSON, but instead got " + jsonNode.getClass().getSimpleName() + ".");
                }
                return null;
            }
            Collection<Object> collection = eStructuralFeature.isUnique() ? new UniqueEList<>() : new BasicEList<>();
            for (JsonNode nestedJsonNode : jsonNode) {
                Object deserialized = EMFImporter.DEFAULT_DESERIALIZATION_FUNCTION.apply(key, nestedJsonNode, true, objectNode, eStructuralFeature, project, strict, element);
                if (deserialized == null && nestedJsonNode != null) {
                    if (strict) {
                        throw new ImportException(element, objectNode, "Failed to deserialize " + eStructuralFeature + " for " + element + ": " + jsonNode + " - " + jsonNode.getClass());
                    }
                    continue;
                }
                if (deserialized != null && !eStructuralFeature.getEType().getInstanceClass().isAssignableFrom(deserialized.getClass())) {
                    if (strict) {
                        throw new ImportException(element, objectNode, "Expected a " + eStructuralFeature.getEType().getInstanceClass().getSimpleName() + " upon deserializing \"" + key + "\", but instead got a " + deserialized.getClass());
                    }
                    continue;
                }
                collection.add(deserialized);
            }
            return collection;
        }
        else if (eStructuralFeature instanceof EReference) {
            EReference eReference = (EReference) eStructuralFeature;
            if (ValueSpecification.class.isAssignableFrom(eReference.getEReferenceType().getInstanceClass()) && jsonNode instanceof ObjectNode) {
                Changelog.Change<Element> change = convert((ObjectNode) jsonNode, project, strict);
                return change != null ? change.getChanged() : null;
            }
            if (jsonNode.isNull() || !jsonNode.isTextual()) {
                if (strict) {
                    throw new ReferenceException(element, objectNode, "Expected a String for key \"" + key + "\" in JSON, but instead got a " + jsonNode.getClass().getSimpleName() + ".");
                }
                return null;
            }
            String id = jsonNode.asText();
            Element referencedElement = ELEMENT_LOOKUP_FUNCTION.apply(id, project);
            if (referencedElement == null) {
                if (strict) {
                    throw new ReferenceException(element, objectNode, "Could not find referenced element " + id + "in model for key \"" + key + "\" in JSON.");
                }
                return null;
            }
            if (!eReference.getEReferenceType().getInstanceClass().isAssignableFrom(referencedElement.getClass())) {
                if (strict) {
                    throw new ReferenceException(element, objectNode, "Expected a " + eReference.getEReferenceType().getInstanceClass().getSimpleName() + " for key \"" + key + "\" in JSON, but instead got a " + referencedElement.getClass().getSimpleName() + ".");
                }
                return null;
            }
            return referencedElement;
        }
        else if (eStructuralFeature.getEType() instanceof EDataType && jsonNode.isTextual()) {
            return EcoreUtil.createFromString((EDataType) eStructuralFeature.getEType(), jsonNode.asText());
        }
        else if (jsonNode.isTextual()) {
            return jsonNode.asText();
        }
        else if (jsonNode.isBoolean()) {
            return jsonNode.asBoolean();
        }
        else if (jsonNode.isInt()) {
            return jsonNode.asInt();
        }
        else if (jsonNode.isDouble()) {
            jsonNode.asDouble();
        }
        else if (jsonNode.isLong()) {
            return jsonNode.asLong();
        }
        else if (jsonNode.isShort()) {
            return jsonNode.shortValue();
        }
        else if (jsonNode.isFloat()) {
            return jsonNode.floatValue();
        }
        else if (jsonNode.isBigInteger()) {
            return jsonNode.bigIntegerValue();
        }
        else if (jsonNode.isBigDecimal()) {
            return jsonNode.decimalValue();
        }
        else if (jsonNode.isBinary()) {
            try {
                return jsonNode.binaryValue();
            } catch (IOException e) {
                throw new ImportException(element, objectNode, "Failed to deserialize binary value.", e);
            }
        }
        // if we get here we have no idea what to do with this object
        return null;
    };

    private static final ImportFunction DEFAULT_E_STRUCTURAL_FEATURE_FUNCTION = (objectNode, eStructuralFeature, project, strict, element) -> {
        if (!eStructuralFeature.isChangeable() || eStructuralFeature.isVolatile() || eStructuralFeature.isTransient() || eStructuralFeature.isUnsettable() || eStructuralFeature.isDerived() || eStructuralFeature.getName().startsWith("_")) {
            return EMFImporter.EMPTY_E_STRUCTURAL_FEATURE_FUNCTION.apply(objectNode, eStructuralFeature, project, strict, element);
        }
        return EMFImporter.UNCHECKED_E_STRUCTURAL_FEATURE_FUNCTION.apply(objectNode, eStructuralFeature, project, strict, element);
    };

    private static final ImportFunction UNCHECKED_E_STRUCTURAL_FEATURE_FUNCTION = (objectNode, eStructuralFeature, project, strict, element) -> {
        String key = KEY_FUNCTION.apply(eStructuralFeature);
        if (!objectNode.has(key)) {
            /*if (strict) {
                throw new ImportException(element, objectNode, "Required key \"" + key + "\" missing from JSON.");
            }*/
            return element;
        }

        JsonNode jsonNode = objectNode.get(key);
        Object deserialized = DEFAULT_DESERIALIZATION_FUNCTION.apply(key, jsonNode, false, objectNode, eStructuralFeature, project, strict, element);

        if (deserialized == null && jsonNode != null) {
            if (strict) {
                throw new ImportException(element, objectNode, "Failed to deserialize " + eStructuralFeature + " for " + element + ": " + jsonNode + " - " + jsonNode.getClass());
            }
            return element;
        }
        if (eStructuralFeature.isMany() && !(deserialized instanceof Collection)) {
            if (strict) {
                throw new ImportException(element, objectNode, "Expected a Collection for key \"" + key + "\" in JSON, but instead got a " + jsonNode.getClass().getSimpleName() + ".");
            }
            return element;
        }
        try {
            EMFImporter.UNCHECKED_SET_E_STRUCTURAL_FEATURE_FUNCTION.apply(deserialized, eStructuralFeature, element);
        } catch (ClassCastException | IllegalArgumentException e) {
            if (strict) {
                throw new ImportException(element, objectNode, "An unexpected exception occurred while setting " + eStructuralFeature + " of type " + eStructuralFeature.getEType() + " for " + element + " to " + jsonNode, e);
            }
            return element;
        }
        return element;
    };

    @SuppressWarnings("unchecked")
    private static final TriFunction<Object, EStructuralFeature, Element, Element> UNCHECKED_SET_E_STRUCTURAL_FEATURE_FUNCTION = (object, eStructuralFeature, element) -> {
        Object currentValue = element.eGet(eStructuralFeature);
        if (object == null && currentValue == null) {
            return element;
        }
        if (eStructuralFeature.isMany() && object != null && currentValue != null) {
            Collection<Object> currentCollection = (Collection<Object>) currentValue;
            Collection<Object> newCollection = (Collection<Object>) object;
            currentCollection.clear();
            currentCollection.addAll(newCollection);
            return element;
        }
        element.eSet(eStructuralFeature, object);
        return element;
    };

    private static final ImportFunction EMPTY_E_STRUCTURAL_FEATURE_FUNCTION = (objectNode, eStructuralFeature, project, strict, element) -> element;

    private enum EStructuralFeatureOverride {
        ID(
                (objectNode, eStructuralFeature, project, strict, element) -> eStructuralFeature == element.eClass().getEIDAttribute(),
                (objectNode, eStructuralFeature, project, strict, element) -> {
                    JsonNode jsonNode = objectNode.get(MDKConstants.SYSML_ID_KEY);
                    if (jsonNode.isNull() || !jsonNode.isTextual()) {
                        if (strict) {
                            throw new ImportException(element, objectNode, "Element JSON has missing/malformed ID.");
                        }
                        return null;
                    }
                    try {
                        UNCHECKED_SET_E_STRUCTURAL_FEATURE_FUNCTION.apply(jsonNode.asText(), element.eClass().getEIDAttribute(), element);
                    } catch (IllegalArgumentException e) {
                        throw new ImportException(element, objectNode, "Unexpected illegal argument exception. See logs for more information.", e);
                    }
                    return element;
                }
        ),
        OWNER(
                (objectNode, eStructuralFeature, project, strict, element) -> UMLPackage.Literals.ELEMENT__OWNER == eStructuralFeature,
                (objectNode, eStructuralFeature, project, strict, element) -> {
                    if (element instanceof Model) {
                        return element;
                    }
                    JsonNode jsonNode = objectNode.get(MDKConstants.OWNER_ID_KEY);
                    if (jsonNode.isNull() || !jsonNode.isTextual()) {
                        if (strict) {
                            throw new ImportException(element, objectNode, "Element JSON has missing/malformed ID.");
                        }
                        return null;
                    }
                    Element owningElement = ELEMENT_LOOKUP_FUNCTION.apply(jsonNode.asText(), project);
                    if (owningElement == null) {
                        if (strict) {
                            throw new ImportException(element, objectNode, "Owner for element " + objectNode.get(MDKConstants.SYSML_ID_KEY).asText("<>") + " not found: " + jsonNode + ".");
                        }
                        return null;
                    }
                    element.setOwner(owningElement);
                    return element;
                }
        );

        private ImportPredicate importPredicate;
        private ImportFunction importFunction;

        EStructuralFeatureOverride(ImportPredicate importPredicate, ImportFunction importFunction) {
            this.importPredicate = importPredicate;
            this.importFunction = importFunction;
        }

        public ImportPredicate getPredicate() {
            return importPredicate;
        }

        public ImportFunction getFunction() {
            return importFunction;
        }
    }

    @FunctionalInterface
    public interface PreProcessorFunction {
        Element apply(ObjectNode objectNode, Project project, boolean strict, Element element);
    }

    @FunctionalInterface
    interface DeserializationFunction {
        Object apply(String key, JsonNode jsonNode, boolean ignoreMultiplicity, ObjectNode objectNode, EStructuralFeature eStructuralFeature, Project project, boolean strict, Element element) throws ImportException;
    }

    @FunctionalInterface
    interface ImportFunction {
        Element apply(ObjectNode objectNode, EStructuralFeature eStructuralFeature, Project project, boolean strict, Element element) throws ImportException;
    }

    @FunctionalInterface
    interface ImportPredicate {
        boolean test(ObjectNode objectNode, EStructuralFeature eStructuralFeature, Project project, boolean strict, Element element);
    }
}
