package org.openmbee.mdk.fileexport;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.nomagic.ci.persistence.IAttachedProject;
import com.nomagic.ci.persistence.IProject;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.sysml.util.SysMLProfile;
import com.nomagic.uml2.ext.jmi.helpers.ModelHelper;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;
import com.nomagic.uml2.ext.magicdraw.metadata.UMLPackage;
import org.openmbee.mdk.api.incubating.MDKConstants;
import org.openmbee.mdk.api.stream.MDKCollectors;
import org.openmbee.mdk.emf.EMFExporter;
import org.openmbee.mdk.json.JacksonUtils;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Armin Zavada
 *
 * Based on code from EMFExporter by igomes
 *
 */
public class ElementContextProvider {

    private final Set<Element> exportedElements = new HashSet<>();
    private Set<Element> contextElements = null;
    private final ContextExportLevel contextExportLevel;

    public ElementContextProvider(ContextExportLevel contextExportLevel) {
        this.contextExportLevel = contextExportLevel;
    }

    public void registerExportedElement(Element element) {
        if (element == null || contextExportLevel == ContextExportLevel.None) {
            return;
        }

        exportedElements.add(element);
    }

    public Stream<ObjectNode> getAggregatedContextElements() {
        if (contextExportLevel == ContextExportLevel.None) {
            return Stream.empty();
        }

        if (contextElements == null) {
            contextElements = new HashSet<>();
            exportedElements.stream().flatMap(this::getContextElements).forEach(this::processContextElement);
        }

        return contextElements.stream().map(this::mapToJson);
    }

    private ObjectNode mapToJson(Element element) {
        ObjectNode objectNode = JacksonUtils.getObjectMapper().createObjectNode();

        objectNode.put(MDKConstants.ID_KEY, EMFExporter.getEID(element));
        objectNode.put(MDKConstants.TYPE_KEY, element.eClass().getName());
        objectNode.put(MDKConstants.DOCUMENTATION_KEY, ModelHelper.getComment(element));
        objectNode.put(MDKConstants.LABEL_KEY, element.getHumanName());

        if (element instanceof NamedElement) {
            objectNode.put(MDKConstants.NAME_KEY, ((NamedElement) element).getName());
            objectNode.put(MDKConstants.FQN_KEY, ((NamedElement) element).getQualifiedName());
        }

        if (contextExportLevel.isTransitiveIncluded()) {
            if (element instanceof TypedElement) {
                objectNode.put(MDKConstants.TYPE_ID_KEY, EMFExporter.getEID(((TypedElement) element).getType()));
            }

            ArrayNode applied = StereotypesHelper.getStereotypes(element).stream().map(stereotype ->
                    TextNode.valueOf(EMFExporter.getEID(stereotype))
            ).collect(MDKCollectors.toArrayNode());
            objectNode.set(MDKConstants.APPLIED_STEREOTYPE_IDS_KEY, applied);
        }

        IProject iProject = ProjectUtilities.getProjectFor(element);

        if (
                contextExportLevel.isContainmentIncluded()        // include containment information
                && element.getOwner() != null                     // we have an owner
                && !exportedElements.contains(element.getOwner()) // our owner is not in the primary model
        ) {
            // Simply add owner id
            objectNode.put(MDKConstants.OWNER_ID_KEY, EMFExporter.getEID(element.getOwner()));
        } else {
            // Either we omit hierarchy, this is a root object, or owner is inside the primary model
            // In either case, we consider this element a root object in context-space
            objectNode.put(MDKConstants.PROJECT_NAME, iProject.getName());
            objectNode.put(MDKConstants.PROJECT_ID, iProject.getProjectID());
        }

        return objectNode;
    }

    private Stream<Element> getContextElements(Element element) {
        if (!contextExportLevel.isDirectIncluded()) {
            return Stream.empty();
        }

        return Stream.concat(
                getElementContext(element),
                getFeatureContext(element)
        );
    }

    private Stream<Element> getElementContext(Element element) {
        return Arrays.stream(ElementContextAggregator.values())
                .flatMap(aggregator ->
                        aggregator.function.apply(element)
                );
    }

    private Stream<Element> getFeatureContext(Element element) {
        return element.eClass().getEAllStructuralFeatures().stream()
                .flatMap(feature ->
                        Arrays.stream(
                                FeatureContextAggregator.values()
                        ).filter(aggregator ->
                                aggregator.predicate.test(element, feature)
                        ).findFirst().map(aggregator ->
                                aggregator.function.apply(element, feature)
                        ).orElse(Stream.empty())
                );
    }

    private Stream<Element> getAdditionalContextElements(Element element) {
        Stream<Element> stream = Stream.empty();

        if (element instanceof TypedElement) {
            stream = Stream.concat(stream, Stream.of(((TypedElement) element).getType()));
        } else if (element instanceof Enumeration) {
            stream = Stream.concat(stream, ((Enumeration)element).getOwnedLiteral().stream());
        } else if (element instanceof Model) {
            stream = Stream.concat(stream, element.getOwnedElement().stream());
        }
        if (contextExportLevel.isContainmentIncluded() && element.getOwner() != null) {
            stream = Stream.concat(stream, Stream.of(element.getOwner()));
        }

        stream = Stream.concat(stream, StereotypesHelper.getStereotypes(element).stream());

        return stream;
    }

    private void processContextElement(Element element) {
        if (element == null || exportedElements.contains(element) || isSerializedElement(element)) {
            return;
        }

        if (contextElements.add(element) && contextExportLevel.isTransitiveIncluded()) {
            getAdditionalContextElements(element).forEach(this::processContextElement);
        }
    }

    private enum ElementContextAggregator {
        APPLIED_STEREOTYPE(
                (element) -> {
                    return StereotypesHelper.getStereotypes(element).stream();
                }
        ),
        MOUNT(
                (element) -> {
                    if (!ProjectUtilities.isAttachedProjectRoot(element)) {
                        return Stream.empty();
                    }
                    IProject iProject = ProjectUtilities.getProjectFor(element);
                    if (!(iProject instanceof IAttachedProject)) {
                        return Stream.empty();
                    }
                    // TODO: get attached project primary model as element
                    return Stream.empty();
                }
        );

        private final ElementContextFunction function;

        private ElementContextAggregator(ElementContextFunction function) {
            this.function = function;
        }

        public ElementContextFunction getFunction() {
            return function;
        }

    }

    private enum FeatureContextAggregator {
        ID(
                (element, eStructuralFeature) -> eStructuralFeature == element.eClass().getEIDAttribute(),
                EMPTY_CONTEXT_FUNCTION
        ),
        OWNER(
                (element, eStructuralFeature) -> UMLPackage.Literals.ELEMENT__OWNER == eStructuralFeature,
                EMPTY_CONTEXT_FUNCTION
        ),
        OWNING(
                (element, eStructuralFeature) -> eStructuralFeature.getName().startsWith("owning"),
                EMPTY_CONTEXT_FUNCTION
        ),
        OWNED(
                (element, eStructuralFeature) -> eStructuralFeature.getName().startsWith("owned") && !eStructuralFeature.isOrdered(),
                EMPTY_CONTEXT_FUNCTION
        ),
        NESTED(
                (element, eStructuralFeature) -> eStructuralFeature.getName().startsWith("nested"),
                EMPTY_CONTEXT_FUNCTION
        ),
        PACKAGED_ELEMENT(
                (element, eStructuralFeature) -> UMLPackage.Literals.PACKAGE__PACKAGED_ELEMENT == eStructuralFeature || UMLPackage.Literals.COMPONENT__PACKAGED_ELEMENT == eStructuralFeature,
                EMPTY_CONTEXT_FUNCTION
        ),
        CONNECTOR__END(
                (element, eStructuralFeature) -> UMLPackage.Literals.CONNECTOR__END == eStructuralFeature,
                (element, eStructuralFeature) -> {
                    Connector connector = (Connector) element;
                    Stream<Element> nestedEnds = connector.getEnd().stream()
                            .filter(connectorEnd ->
                                    StereotypesHelper.hasStereotype(connectorEnd, SysMLProfile.NESTEDCONNECTOREND_STEREOTYPE)
                            ).flatMap(connectorEnd ->
                                    StereotypesHelper.getStereotypePropertyValue(connectorEnd, SysMLProfile.NESTEDCONNECTOREND_STEREOTYPE, SysMLProfile.ELEMENTPROPERTYPATH_PROPERTYPATH_PROPERTY).stream()
                            ).map(o ->
                                    o instanceof ElementValue ? ((ElementValue) o).getElement() : o
                            );
                    Stream<Element> ends = connector.getEnd().stream().map(end ->
                            end.getRole()
                    );

                    return Stream.concat(nestedEnds, ends);
                }
        ),
        VALUE_SPECIFICATION__EXPRESSION(
                (element, eStructuralFeature) -> eStructuralFeature == UMLPackage.Literals.VALUE_SPECIFICATION__EXPRESSION,
                EMPTY_CONTEXT_FUNCTION
        ),
        UML_CLASS(
                (element, eStructuralFeature) -> eStructuralFeature == UMLPackage.Literals.CLASSIFIER__UML_CLASS || eStructuralFeature == UMLPackage.Literals.PROPERTY__UML_CLASS || eStructuralFeature == UMLPackage.Literals.OPERATION__UML_CLASS,
                EMPTY_CONTEXT_FUNCTION
        ),
        GENERIC(
                (element, eStructuralFeature) -> true,
                (element, eStructuralFeature) -> {
                    if (!eStructuralFeature.isChangeable() || eStructuralFeature.isVolatile() || eStructuralFeature.isTransient() || eStructuralFeature.isUnsettable() || eStructuralFeature.isDerived() || eStructuralFeature.getName().startsWith("_")) {
                        return Stream.empty();
                    }
                    return getFlatStructuralFeatureStream(element, eStructuralFeature);
                }
        );

        private final FeatureContextFunction function;
        private final FeatureContextPredicate predicate;

        private FeatureContextAggregator(FeatureContextPredicate predicate, FeatureContextFunction function) {
            this.predicate = predicate;
            this.function = function;
        }

        public FeatureContextFunction getFunction() {
            return function;
        }

        public FeatureContextPredicate getPredicate() {
            return predicate;
        }

    }

    private static final FeatureContextFunction EMPTY_CONTEXT_FUNCTION = (element, eStructuralFeature) -> Stream.empty();

    private static boolean isSerializedElement(Element element) {
        return element instanceof ValueSpecification;
    }

    private static Stream<Element> getFlatStructuralFeatureStream(Element element, EStructuralFeature eStructuralFeature) {
        Object slot = element.eGet(eStructuralFeature);
        Stream<?> stream;

        if (slot == null) {
            stream = Stream.empty();
        } else if (slot instanceof Collection<?>) {
            stream = ((Collection<?>) slot).stream();
        } else {
            stream = Stream.of(slot);
        }

        return stream.filter(Element.class::isInstance).map(Element.class::cast);
    }

    @FunctionalInterface
    interface FeatureContextPredicate {
        boolean test(Element element, EStructuralFeature structuralFeature);
    }

    @FunctionalInterface
    interface FeatureContextFunction {
        Stream<? extends Element> apply(Element element, EStructuralFeature eStructuralFeature);
    }

    @FunctionalInterface
    interface ElementContextFunction {
        Stream<? extends Element> apply(Element element);
    }

}
