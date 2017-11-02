package gov.nasa.jpl.mbee.mdk.emf;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.openapi.uml.ModelElementsManager;
import com.nomagic.magicdraw.openapi.uml.ReadOnlyElementException;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.reflect.AbstractRepository;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdtemplates.ParameterableElement;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdtemplates.TemplateParameter;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.impl.UMLFactoryImpl;
import com.nomagic.uml2.ext.magicdraw.metadata.UMLFactory;
import com.nomagic.uml2.ext.magicdraw.metadata.UMLPackage;
import gov.nasa.jpl.mbee.mdk.api.function.TriFunction;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.JsonToElementFunction;
import gov.nasa.jpl.mbee.mdk.json.ImportException;
import gov.nasa.jpl.mbee.mdk.json.ReferenceException;
import gov.nasa.jpl.mbee.mdk.util.Changelog;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.UniqueEList;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.util.EcoreUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Created by igomes on 9/19/16.
 */
public class EMFImporter implements JsonToElementFunction {
    protected List<PreProcessor> preProcessors;
    protected List<EStructuralFeatureOverride> eStructuralFeatureOverrides;

    @Override
    public Changelog.Change<Element> apply(ObjectNode objectNode, Project project, Boolean strict) throws ImportException {
        return convert(objectNode, project, strict);
    }

    private synchronized Changelog.Change<Element> convert(ObjectNode objectNode, Project project, Boolean strict) throws ImportException {
        JsonNode jsonNode = objectNode.get(MDKConstants.ID_KEY);
        /*if (jsonNode == null || !jsonNode.isTextual()) {
            return null;
        }*/
        Element element = jsonNode != null && jsonNode.isTextual() ? getIdToElementConverter().apply(jsonNode.asText(), project) : null;
        Changelog.ChangeType changeType = element != null && !project.isDisposed(element) ? Changelog.ChangeType.UPDATED : Changelog.ChangeType.CREATED;

        try {
            for (PreProcessor preProcessor : getPreProcessors()) {
                element = preProcessor.getFunction().apply(objectNode, project, strict, element);
                if (element == null) {
                    return null;
                }
            }
            if (element.eClass() == null) {
                return null;
            }
            for (EStructuralFeature eStructuralFeature : element.eClass().getEAllStructuralFeatures()) {
                final Element finalElement = element;
                ImportFunction function = getEStructuralFeatureOverrides().stream().filter(override -> override.getPredicate()
                        .test(objectNode, eStructuralFeature, project, strict, finalElement)).map(EStructuralFeatureOverride::getFunction)
                        .findAny().orElse(DEFAULT_E_STRUCTURAL_FEATURE_FUNCTION);
                element = function.apply(objectNode, eStructuralFeature, project, strict, element);
                if (element == null) {
                    return null;
                }
            }
        } catch (RuntimeException e) {
            throw new ImportException(element, jsonNode, e.getMessage(), e);
        }
        return new Changelog.Change<>(element, changeType);
    }

    protected List<PreProcessor> getPreProcessors() {
        if (preProcessors == null) {
            preProcessors = Arrays.asList(PreProcessor.CREATE, PreProcessor.EDITABLE, PreProcessor.DOCUMENTATION, PreProcessor.SYSML_ID_VALIDATION);
        }
        return preProcessors;
    }

    public static class PreProcessor {
        public static final PreProcessor
                CREATE = getCreatePreProcessor(Converters.getIdToElementConverter()),
                EDITABLE = new PreProcessor(
                        (objectNode, project, strict, element) -> {
                            return element;
                        }
                ),
                DOCUMENTATION = new PreProcessor(
                        (objectNode, project, strict, element) -> {
                            JsonNode jsonNode = objectNode.get(MDKConstants.DOCUMENTATION_KEY);
                            if (jsonNode != null && jsonNode.isTextual()) {
                                ModelHelper.setComment(element, jsonNode.asText());
                            }
                            return element;
                        }
                ),
                SYSML_ID_VALIDATION = new PreProcessor(
                        (objectNode, project, strict, element) -> {
                            JsonNode jsonNode = objectNode.get(MDKConstants.ID_KEY);
                            if (jsonNode == null || !jsonNode.isTextual()) {
                                return element;
                            }
                            String id = jsonNode.asText();
                            if (id.startsWith(MDKConstants.HIDDEN_ID_PREFIX)) {
                                return null;
                            }
                            return element;
                        }
                );

        static PreProcessor getCreatePreProcessor(BiFunction<String, Project, Element> idToElementConverter) {
            return new PreProcessor(
                    (objectNode, project, strict, element) -> {
                        if (element != null) {
                            return element;
                        }
                        JsonNode jsonNode = objectNode.get(MDKConstants.TYPE_KEY);
                        if (jsonNode == null || !jsonNode.isTextual()) {
                            return null;
                        }
                        String type = jsonNode.asText();
                        if (type.equals(UMLPackage.Literals.DIAGRAM.getName())) {
                            JsonNode diagramTypeJsonNode = objectNode.get(MDKConstants.DIAGRAM_TYPE_KEY);
                            if (diagramTypeJsonNode == null || !diagramTypeJsonNode.isTextual()) {
                                return null;
                            }
                            JsonNode ownerJsonNode = objectNode.get(MDKConstants.OWNER_ID_KEY);
                            if (ownerJsonNode == null || !ownerJsonNode.isTextual()) {
                                return null;
                            }
                            Element owner = idToElementConverter.apply(ownerJsonNode.asText(), project);
                            if (owner == null || !(owner instanceof Namespace)) {
                                return null;
                            }
                            try {
                                return ModelElementsManager.getInstance().createDiagram(diagramTypeJsonNode.asText(), (Namespace) owner);
                            } catch (ReadOnlyElementException e) {
                                throw new ImportException(element, objectNode, e.getMessage(), e);
                            }
                        }
                        EClassifier eClassifier = UMLPackage.eINSTANCE.getEClassifier(type);
                        if (!(eClassifier instanceof EClass)) {
                            return null;
                        }
                        AbstractRepository initialRepository = (UMLFactory.eINSTANCE instanceof UMLFactoryImpl) ? ((UMLFactoryImpl) UMLFactory.eINSTANCE).getRepository() : null;
                        EObject eObject;
                        try {
                            UMLFactory.eINSTANCE.setRepository(project.getRepository());
                            eObject = UMLFactory.eINSTANCE.create((EClass) eClassifier);
                        } finally {
                            UMLFactory.eINSTANCE.setRepository(initialRepository);
                        }
                        if (!(eObject instanceof Element)) {
                            return null;
                        }
                        return (Element) eObject;
                    }
            );
        }

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

    private final DeserializationFunction DEFAULT_DESERIALIZATION_FUNCTION = (key, jsonNode, ignoreMultiplicity, objectNode, eStructuralFeature, project, strict, element) -> {
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
                Object deserialized = this.DEFAULT_DESERIALIZATION_FUNCTION.apply(key, nestedJsonNode, true, objectNode, eStructuralFeature, project, strict, element);
                if (deserialized == null && !nestedJsonNode.isNull()) {
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
                Changelog.Change<Element> change = this.convert((ObjectNode) jsonNode, project, strict);
                return change != null ? change.getChanged() : null;
            }
            if (!jsonNode.isTextual()) {
                if (strict) {
                    throw new ReferenceException(element, objectNode, "Expected a String for key \"" + key + "\" in JSON, but instead got a " + jsonNode.getClass().getSimpleName() + ".");
                }
                return null;
            }
            String id = jsonNode.asText();
            Element referencedElement = getIdToElementConverter().apply(id, project);
            System.out.println("[LOOKUP] " + id + " -> " + referencedElement);
            if (referencedElement == null) {
                if (strict) {
                    throw new ReferenceException(element, objectNode, "Could not find referenced element " + id + " in model for key \"" + key + "\" in JSON.");
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
            return jsonNode.asDouble();
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

    private final ImportFunction DEFAULT_E_STRUCTURAL_FEATURE_FUNCTION = (objectNode, eStructuralFeature, project, strict, element) -> {
        if (!eStructuralFeature.isChangeable() || eStructuralFeature.isVolatile() || eStructuralFeature.isTransient() || eStructuralFeature.isUnsettable() || eStructuralFeature.isDerived() || eStructuralFeature.getName().startsWith(MDKConstants.DERIVED_KEY_PREFIX)) {
            return EMFImporter.EMPTY_E_STRUCTURAL_FEATURE_FUNCTION.apply(objectNode, eStructuralFeature, project, strict, element);
        }
        return this.UNCHECKED_E_STRUCTURAL_FEATURE_FUNCTION.apply(objectNode, eStructuralFeature, project, strict, element);
    };

    private final ImportFunction UNCHECKED_E_STRUCTURAL_FEATURE_FUNCTION = (objectNode, eStructuralFeature, project, strict, element) -> {
        String key = KEY_FUNCTION.apply(eStructuralFeature);
        JsonNode jsonNode = objectNode.get(key);
        if (jsonNode == null) {
            /*if (strict) {
                throw new ImportException(element, objectNode, "Required key \"" + key + "\" missing from JSON.");
            }*/
            return element;
        }

        Object deserialized = DEFAULT_DESERIALIZATION_FUNCTION.apply(key, jsonNode, false, objectNode, eStructuralFeature, project, strict, element);

        if (deserialized == null && !jsonNode.isNull()) {
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
        if (object != null && currentValue != null && (object == currentValue || object.equals(currentValue))) {
            return element;
        }
        if (eStructuralFeature.isMany() && object instanceof Collection && currentValue instanceof Collection) {
            Collection<Object> currentCollection = (Collection<Object>) currentValue;
            Collection<Object> newCollection = (Collection<Object>) object;
            /*if (eStructuralFeature.isOrdered() && object instanceof List && currentValue instanceof List && currentValue.equals(object)) {
                return element;
            }
            if (!eStructuralFeature.isOrdered()) {
                CollectionUtils.isEqualCollection()
            }*/
            currentCollection.clear();
            currentCollection.addAll(newCollection);
            return element;
        }
        element.eSet(eStructuralFeature, object);
        return element;
    };

    private static final ImportFunction EMPTY_E_STRUCTURAL_FEATURE_FUNCTION = (objectNode, eStructuralFeature, project, strict, element) -> element;

    protected static class EStructuralFeatureOverride {
        public static final EStructuralFeatureOverride
                ID = new EStructuralFeatureOverride(
                (objectNode, eStructuralFeature, project, strict, element) -> eStructuralFeature == element.eClass().getEIDAttribute(),
                (objectNode, eStructuralFeature, project, strict, element) -> {
                    JsonNode jsonNode = objectNode.get(MDKConstants.ID_KEY);
                    if (jsonNode == null || !jsonNode.isTextual()) {
                            /*if (strict) {
                                throw new ImportException(element, objectNode, "Element JSON has missing/malformed ID.");
                            }
                            return null;*/
                        return element;
                    }
                    try {
                        boolean initialCanResetIDForObject = project.getCounter().canResetIDForObject();
                        project.getCounter().setCanResetIDForObject(true);
                        UNCHECKED_SET_E_STRUCTURAL_FEATURE_FUNCTION.apply(jsonNode.asText(), element.eClass().getEIDAttribute(), element);
                        project.getCounter().setCanResetIDForObject(initialCanResetIDForObject);
                    } catch (IllegalArgumentException e) {
                        throw new ImportException(element, objectNode, "Unexpected illegal argument exception. See logs for more information.", e);
                    }
                    return element;
                }
        ),
                OWNER = getOwnerEStructuralFeatureOverride(Converters.getIdToElementConverter());

        private ImportPredicate importPredicate;
        private ImportFunction importFunction;

        public EStructuralFeatureOverride(ImportPredicate importPredicate, ImportFunction importFunction) {
            this.importPredicate = importPredicate;
            this.importFunction = importFunction;
        }

        public ImportPredicate getPredicate() {
            return importPredicate;
        }

        public ImportFunction getFunction() {
            return importFunction;
        }

        protected static EStructuralFeatureOverride getOwnerEStructuralFeatureOverride(BiFunction<String, Project, Element> idToElementConverter) {
            return new EStructuralFeatureOverride(
                    (objectNode, eStructuralFeature, project, strict, element) -> UMLPackage.Literals.ELEMENT__OWNER == eStructuralFeature,
                    (objectNode, eStructuralFeature, project, strict, element) -> {
                        if (element instanceof Model || element instanceof ValueSpecification) {
                            return element;
                        }
                        JsonNode jsonNode = objectNode.get(MDKConstants.OWNER_ID_KEY);
                        if (jsonNode == null || !jsonNode.isTextual()) {
                            if (strict) {
                                throw new ImportException(element, objectNode, "Element JSON has missing/malformed owner.");
                            }
                            return null;
                        }
                        String owningElementId = jsonNode.asText();
                        Element owningElement = idToElementConverter.apply(owningElementId, project);
                        if (element instanceof Package
                                && (jsonNode = objectNode.get(MDKConstants.ID_KEY)) != null && jsonNode.isTextual() && jsonNode.asText().startsWith(MDKConstants.HOLDING_BIN_ID_PREFIX)
                                && owningElementId.equals(Converters.getIProjectToIdConverter().apply(project.getPrimaryProject()))) {
                            ((Package) element).setOwningPackage(project.getPrimaryModel());
                            return element;
                        }
                        if (owningElement == null) {
                            if (strict) {
                                JsonNode sysmlIdNode = objectNode.get(MDKConstants.ID_KEY);
                                throw new ImportException(element, objectNode, "Owner for element " + (sysmlIdNode != null && sysmlIdNode.isTextual() ? sysmlIdNode.asText("<>") : "<>") + " not found: " + jsonNode + ".");
                            }
                        }
                        try {
                            //element.setOwner(owningElement);
                            if (element instanceof PackageableElement && owningElement instanceof Package) {
                                ((PackageableElement) element).setOwningPackage((Package) owningElement);
                            }
                            else if (element instanceof ParameterableElement && owningElement instanceof TemplateParameter) {
                                ((ParameterableElement) element).setOwningTemplateParameter((TemplateParameter) owningElement);
                            }
                            else if (element instanceof Slot && owningElement instanceof InstanceSpecification) {
                                ((Slot) element).setOwningInstance((InstanceSpecification) owningElement);
                            }
                            else if (element instanceof InstanceSpecification
                                    && ((jsonNode = objectNode.get(KEY_FUNCTION.apply(UMLPackage.Literals.INSTANCE_SPECIFICATION__STEREOTYPED_ELEMENT))) != null && jsonNode.isTextual()
                                    || (jsonNode = objectNode.get(MDKConstants.ID_KEY)) != null && jsonNode.isTextual() && jsonNode.asText().endsWith(MDKConstants.APPLIED_STEREOTYPE_INSTANCE_ID_SUFFIX))) {
                                ((InstanceSpecification) element).setStereotypedElement(owningElement);
                            }
                            else {
                                element.setOwner(owningElement);
                            }
                        } catch (IllegalArgumentException e) {
                            System.out.println("ELEMENT: " + element + (element != null ? " " + Converters.getElementToIdConverter().apply(element) : ""));
                            System.out.println("OWNER: " + owningElement + (owningElement != null ? " " + Converters.getElementToIdConverter().apply(owningElement) : ""));
                            throw new ImportException(element, objectNode, "Unexpected illegal argument exception. See logs for more information.", e);
                        }
                        return element;
                    }
            );
        }
    }

    protected List<EStructuralFeatureOverride> getEStructuralFeatureOverrides() {
        if (eStructuralFeatureOverrides == null) {
            eStructuralFeatureOverrides = Arrays.asList(EStructuralFeatureOverride.ID, EStructuralFeatureOverride.OWNER);
        }
        return eStructuralFeatureOverrides;
    }

    protected BiFunction<String, Project, Element> getIdToElementConverter() {
        return Converters.getIdToElementConverter();
    }

    @FunctionalInterface
    public interface PreProcessorFunction {
        Element apply(ObjectNode objectNode, Project project, boolean strict, Element element) throws ImportException;
    }

    @FunctionalInterface
    interface DeserializationFunction {
        Object apply(String key, JsonNode jsonNode, boolean ignoreMultiplicity, ObjectNode objectNode, EStructuralFeature eStructuralFeature, Project project, boolean strict, Element element) throws ImportException;
    }

    @FunctionalInterface
    protected interface ImportFunction {
        Element apply(ObjectNode objectNode, EStructuralFeature eStructuralFeature, Project project, boolean strict, Element element) throws ImportException;
    }

    @FunctionalInterface
    interface ImportPredicate {
        boolean test(ObjectNode objectNode, EStructuralFeature eStructuralFeature, Project project, boolean strict, Element element);
    }
}
