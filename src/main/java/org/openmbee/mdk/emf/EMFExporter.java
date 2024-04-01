package org.openmbee.mdk.emf;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.*;
import com.nomagic.ci.persistence.IAttachedProject;
import com.nomagic.ci.persistence.IProject;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.magicdraw.core.ProjectUtilitiesInternal;
import com.nomagic.magicdraw.esi.EsiUtils;
import com.nomagic.magicdraw.sysml.util.SysMLProfile;
import com.nomagic.uml2.ext.jmi.helpers.StereotypesHelper;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Package;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.*;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;
import com.nomagic.uml2.ext.magicdraw.mdprofiles.Stereotype;
import com.nomagic.uml2.ext.magicdraw.metadata.UMLPackage;
import org.openmbee.mdk.SysMLExtensions;
import org.openmbee.mdk.api.function.TriFunction;
import org.openmbee.mdk.api.incubating.MDKConstants;
import org.openmbee.mdk.api.incubating.convert.Converters;
import org.openmbee.mdk.api.stream.MDKCollectors;
import org.openmbee.mdk.json.JacksonUtils;
import org.openmbee.mdk.util.Utils;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;


public class EMFExporter implements BiFunction<Element, Project, ObjectNode> {
    @Override
    public ObjectNode apply(Element element, Project project) {
        return convert(element, project);
    }

    private static ObjectNode convert(Element element, Project project) {
        return convert(element, project, false);
    }

    private static ObjectNode convert(Element element, Project project, boolean nestedValueSpecification) {
        if (element == null) {
            return null;
        }
        ObjectNode objectNode = JacksonUtils.getObjectMapper().createObjectNode();
        for (Processor processor : Arrays.stream(Processor.values()).filter(processor -> processor.getType() == Processor.Type.PRE).collect(Collectors.toList())) {
            if (nestedValueSpecification && processor == Processor.VALUE_SPECIFICATION) {
                continue;
            }
            try {
                objectNode = processor.getFunction().apply(element, project, objectNode);
            } catch (RuntimeException e) {
                e.printStackTrace();
                System.out.println("EXCEPTION: " + element.getHumanName() + " | " + element.getLocalID() + " in " + project.getName());
            }
            if (objectNode == null) {
                return null;
            }
        }
        for (EStructuralFeature eStructuralFeature : element.eClass().getEAllStructuralFeatures()) {
            final ObjectNode finalObjectNode = objectNode;
            ExportFunction function = Arrays.stream(EStructuralFeatureOverride.values())
                    .filter(override -> override.getPredicate().test(element, project, eStructuralFeature, finalObjectNode)).map(EStructuralFeatureOverride::getFunction)
                    .findAny().orElse(DEFAULT_E_STRUCTURAL_FEATURE_FUNCTION);
            try {
                objectNode = function.apply(element, project, eStructuralFeature, objectNode);
            } catch (RuntimeException e) {
                e.printStackTrace();
                System.err.println(element);
            }
            if (objectNode == null) {
                return null;
            }
        }
        for (Processor processor : Arrays.stream(Processor.values()).filter(processor -> processor.getType() == Processor.Type.POST).collect(Collectors.toList())) {
            try {
                objectNode = processor.getFunction().apply(element, project, objectNode);
            } catch (RuntimeException e) {
                e.printStackTrace();
                System.out.println("EXCEPTION: " + element.getHumanName() + " | " + element.getLocalID() + " in " + project.getName());
            }
            if (objectNode == null) {
                return null;
            }
        }
        return objectNode;
    }

    public static String getEID(EObject eObject) {
        if (eObject == null) {
            return null;
        }
        if (!(eObject instanceof Element)) {
            return EcoreUtil.getID(eObject);
        }
        Element element = (Element) eObject;
        Project project = Project.getProject(element);

        // custom handling of primary model id
        if (element instanceof Model && element == project.getPrimaryModel()) {
            return Converters.getIProjectToIdConverter().apply(project.getPrimaryProject()) + MDKConstants.PRIMARY_MODEL_ID_SUFFIX;
        }

        // local projects don't properly maintain the ids of some elements. this id spoofing mitigates that for us, but can mess up the MMS delta counts in some cases (annoying, but ultimately harmless)
        // NOTE - this spoofing is replicated in LocalSyncTransactionListener in order to properly add / remove elements in the unsynched queue. any updates here should be replicated there as well.
        // there's no more instance spec that's a result of stereotyping, so instance spec should just have their normal id
        /*if (eObject instanceof TimeExpression && ((TimeExpression) eObject).get_timeEventOfWhen() != null) {
            return getEID(((TimeExpression) eObject).get_timeEventOfWhen()) + MDKConstants.TIME_EXPRESSION_ID_SUFFIX;
        }*/
        if (element instanceof ValueSpecification && ((ValueSpecification) element).getOwningSlot() != null) {
            ValueSpecification slotValue = (ValueSpecification) element;
            return getEID(slotValue.getOwningSlot()) + MDKConstants.SLOT_VALUE_ID_SEPARATOR + slotValue.getOwningSlot().getValue().indexOf(slotValue) + "-" + slotValue.eClass().getName().toLowerCase();
        }
        if (element instanceof TaggedValue) {
            TaggedValue slot = (TaggedValue) element;
            if (slot.getTaggedValueOwner() != null && slot.getTagDefinition() != null) {
                // add _asi to owner in constructed id to maintain continuity with 19.x slots
                return getEID(slot.getOwner()) + MDKConstants.APPLIED_STEREOTYPE_INSTANCE_ID_SUFFIX + MDKConstants.SLOT_ID_SEPARATOR + getEID(slot.getTagDefinition());
            }
        }
        if (element instanceof Slot) {
            Slot slot = (Slot) element;
            if (slot.getOwningInstance() != null && ((Slot) element).getDefiningFeature() != null) {
                return getEID(slot.getOwningInstance()) + MDKConstants.SLOT_ID_SEPARATOR + getEID(slot.getDefiningFeature());
            }
        }
        return element.getLocalID();
    }

    private static void dumpUMLPackageLiterals() {
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

    private enum Processor {
        /*APPLIED_STEREOTYPE(
                (element, project, objectNode) -> {
                    ArrayNode applied = StereotypesHelper.getStereotypes(element).stream().map(stereotype -> TextNode.valueOf(getEID(stereotype))).collect(MDKCollectors.toArrayNode());
                    objectNode.set(MDKConstants.APPLIED_STEREOTYPE_IDS_KEY, applied);
                    return objectNode;
                },
                Type.PRE
        ),*/
        ATTACHED_PROJECT(
                (element, project, objectNode) -> ProjectUtilities.isElementInAttachedProject(element) && ProjectUtilities.getAttachedProjects(project.getPrimaryProject()).stream().noneMatch(iAttachedProject -> ProjectUtilitiesInternal.distanceFromProject(iAttachedProject) == 1 && ProjectUtilities.isAttachedProjectRoot(element, iAttachedProject)) ? null : objectNode,
                Type.PRE
        ),
        COMMENT(
                (element, project, objectNode) -> {
                    if (!(element instanceof Comment)) {
                        return objectNode;
                    }
                    Comment comment = (Comment) element;
                    return comment.getAnnotatedElement().size() == 1 && comment.getAnnotatedElement().iterator().next() == comment.getOwner() ? null : objectNode;
                },
                Type.PRE
        ),
        DIAGRAM_TYPE(
                (element, project, objectNode) -> {
                    if (element instanceof Diagram) {
                        objectNode.put(MDKConstants.DIAGRAM_TYPE_KEY, ((Diagram) element).get_representation() != null ? ((Diagram) element).get_representation().getType() : null);
                    }
                    return objectNode;
                },
                Type.PRE
        ),
        DOCUMENTATION_PRE(
                (element, project, objectNode) -> {
                    objectNode.put(MDKConstants.DOCUMENTATION_KEY, (String) Utils.getElementAttribute(element, Utils.AvailableAttribute.Documentation));
                    return objectNode;
                },
                Type.PRE
        ),
        MOUNT_PRE(
                (element, project, objectNode) -> {
                    if (!ProjectUtilities.isAttachedProjectRoot(element)) {
                        return objectNode;
                    }
                    IProject iProject = ProjectUtilities.getProjectFor(element);
                    if (!(iProject instanceof IAttachedProject)) {
                        return objectNode;
                    }
                    IAttachedProject attachedProject = (IAttachedProject) iProject;
                    boolean isRemote = ProjectUtilities.isRemote(attachedProject) && !attachedProject.getLocationURI().isFile();

                    objectNode.put(MDKConstants.TYPE_KEY, "Mount");
                    objectNode.put(MDKConstants.MOUNTED_ELEMENT_ID_KEY, Converters.getIProjectToIdConverter().apply(attachedProject) + MDKConstants.PRIMARY_MODEL_ID_SUFFIX);
                    objectNode.put(MDKConstants.MOUNTED_ELEMENT_PROJECT_ID_KEY, Converters.getIProjectToIdConverter().apply(attachedProject));
                    EsiUtils.EsiBranchInfo branchInfo = EsiUtils.getCurrentBranch(attachedProject);
                    if (isRemote && branchInfo == null) {
                        return null;
                    }
                    String branchName;
                    if (!isRemote || (branchName = branchInfo.getName()) == null || "trunk".equals(branchName)) {
                        branchName = "master";
                    }
                    String branchId = "master".equals(branchName) ? "master" : branchInfo.getID().toString();
                    objectNode.put(MDKConstants.MOUNTED_REF_ID_KEY, branchId);
                    objectNode.put(MDKConstants.TWC_VERSION_KEY, isRemote ? ProjectUtilities.versionToInt(ProjectUtilities.getVersion(attachedProject).getName()) : -1);
                    return objectNode;
                },
                Type.PRE
        ),
        IS_GROUP(
                (element, project, objectNode) -> {
                    if (element instanceof Package) {
                        objectNode.put(MDKConstants.IS_GROUP_KEY, SysMLExtensions.getInstance(element).group().is(element));
                    }
                    return objectNode;
                },
                Type.PRE
        ),
        SYNC(
                (element, project, objectNode) -> element == null || Converters.getElementToIdConverter().apply(element).endsWith(MDKConstants.SYNC_SYSML_ID_SUFFIX) ||
                        element.getOwner() != null && Converters.getElementToIdConverter().apply(element.getOwner()).endsWith(MDKConstants.SYNC_SYSML_ID_SUFFIX) ? null : objectNode,
                Type.PRE
        ),
        /*
        TWC_ID is disabled indefinitely, due to our inability to update the ID and associated issues
        TWC_ID(
                (element, project, objectNode) -> {
                    if (project.isRemote()) {
                        objectNode.put(MDKConstants.TWC_ID_KEY, element.getID());
                    }
                    return objectNode;
                }
        ),
        */
        TYPE(
                (element, project, objectNode) -> {
                    if (!objectNode.has(MDKConstants.TYPE_KEY)) {
                        objectNode.put(MDKConstants.TYPE_KEY, element.eClass().getName());
                    }
                    return objectNode;
                },
                Type.PRE
        ),
        VALUE_SPECIFICATION(
                (element, project, objectNode) -> {
                    Element e = element;
                    do {
                        if (e instanceof ValueSpecification) {
                            return null;
                        }
                    } while ((e = e.getOwner()) != null);
                    return objectNode;
                },
                Type.PRE
        ),
        VIEW(
                (element, project, objectNode) -> {
                    Stereotype viewStereotype = SysMLProfile.getInstance(element).view().getStereotype();
                    if (viewStereotype == null || !StereotypesHelper.hasStereotypeOrDerived(element, viewStereotype)) {
                        return objectNode;
                    }
                    Constraint viewConstraint = Utils.getViewConstraint(element);
                    if (viewConstraint == null) {
                        return objectNode;
                    }
                    objectNode.set(MDKConstants.CONTENTS_KEY, DEFAULT_SERIALIZATION_FUNCTION.apply(viewConstraint.getSpecification(), project, null));
                    return objectNode;
                },
                Type.PRE
        ),
        VIEW_POST(
                (element, project, objectNode) -> {
                    Stereotype viewStereotype = SysMLProfile.getInstance(element).view().getStereotype();
                    JsonNode node = objectNode.get(MDKConstants.OWNED_RULE_IDS);
                    if (viewStereotype == null || !StereotypesHelper.hasStereotypeOrDerived(element, viewStereotype) ||
                            node == null || !node.isArray() || node.size() == 0) {
                        return objectNode;
                    }
                    ((ArrayNode)node).removeAll();
                    return objectNode;
                },
                Type.POST
        ),
        MOUNT_POST(
                (element, project, objectNode) -> {
                    if (!objectNode.get(MDKConstants.TYPE_KEY).asText().equals("Mount")) {
                        return objectNode;
                    }
                    Iterator<String> iterator = objectNode.fieldNames();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        if (key.equals(MDKConstants.MOUNTED_ELEMENT_ID_KEY)
                                || key.equals(MDKConstants.MOUNTED_ELEMENT_PROJECT_ID_KEY)
                                || key.equals(MDKConstants.MOUNTED_REF_ID_KEY)
                                || key.equals(MDKConstants.TWC_VERSION_KEY)
                                || key.equals(MDKConstants.TYPE_KEY)
                                || key.equals(MDKConstants.ID_KEY)
                                || key.equals(MDKConstants.OWNER_ID_KEY)) {
                            continue;
                        }
                        iterator.remove();
                    }
                    return objectNode;
                },
                Type.POST
        ),
        DOCUMENTATION_POST(
                (element, project, objectNode) -> {
                    if (element != null) {
                        Element docEl = Utils.getDocumentationElement(element);
                        if (docEl != null) {
                            ArrayNode ownedCommentIds = ((ArrayNode) objectNode.get(MDKConstants.OWNED_COMMENT_IDS_KEY));
                            for (int i = 0; i < ownedCommentIds.size(); i++) {
                                if (ownedCommentIds.get(i).asText().equals(getEID(docEl)))
                                    ownedCommentIds.remove(i);
                            }
                            if (ownedCommentIds.size() == 0) {
                                objectNode.remove(MDKConstants.OWNED_COMMENT_IDS_KEY);
                            } else {
                                objectNode.replace(MDKConstants.OWNED_COMMENT_IDS_KEY, ownedCommentIds);
                            }

                        }

                    }

                    return objectNode;
                },
                Type.POST
        ),
        TAGGEDVALUE_POST(
                (element, project, objectNode) -> {
                    if (!(element instanceof TaggedValue) || element instanceof ElementTaggedValue) {
                        return objectNode;
                    }
                    TaggedValue v = (TaggedValue)element;
                    ArrayNode l = (ArrayNode)objectNode.get("value");
                    if (l == null) {
                        l = JacksonUtils.getObjectMapper().createArrayNode();
                        objectNode.set("value", l);
                    }
                    l.removeAll();
                    // for continuity with 19.x mdk export, value is a list of objects with value key or elementId
                    for (Object o: v.getValue()) {
                        ObjectNode n = JacksonUtils.getObjectMapper().createObjectNode();
                        JsonNode node = null;
                        if (o instanceof String) {
                            node = TextNode.valueOf((String) o);
                        }
                        else if (o instanceof Boolean) {
                            node = BooleanNode.valueOf((boolean) o);
                        }
                        else if (o instanceof Integer) {
                            node = IntNode.valueOf((Integer) o);
                        }
                        else if (o instanceof Double) {
                            node = DoubleNode.valueOf((Double) o);
                        }
                        else if (o instanceof Long) {
                            node = LongNode.valueOf((Long) o);
                        }
                        else if (o instanceof Short) {
                            node = ShortNode.valueOf((Short) o);
                        }
                        else if (o instanceof Float) {
                            node = FloatNode.valueOf((Float) o);
                        }
                        else if (o instanceof BigInteger) {
                            node = BigIntegerNode.valueOf((BigInteger) o);
                        }
                        else if (o instanceof BigDecimal) {
                            node = DecimalNode.valueOf((BigDecimal) o);
                        }
                        n.set("value", node);
                        if (o instanceof Element) {
                            node = TextNode.valueOf(getEID((Element)o));
                            n.set("elementId", node);
                            n.remove("value");
                        }
                        l.add(n);
                    }
                    return objectNode;
                },
                Type.POST
        );

        private TriFunction<Element, Project, ObjectNode, ObjectNode> function;
        private Type type;

        Processor(TriFunction<Element, Project, ObjectNode, ObjectNode> function, Type type) {
            this.function = function;
            this.type = type;
        }

        public TriFunction<Element, Project, ObjectNode, ObjectNode> getFunction() {
            return function;
        }

        public Type getType() {
            return type;
        }

        public enum Type {
            PRE,
            POST
        }
    }

    private static final SerializationFunction DEFAULT_SERIALIZATION_FUNCTION = (object, project, eStructuralFeature) -> {
        if (object == null) {
            return NullNode.getInstance();
        }
        else if (object instanceof Collection) {
            ArrayNode arrayNode = JacksonUtils.getObjectMapper().createArrayNode();
            try {
                for (Object o : ((Collection<?>) object)) {
                    JsonNode serialized = EMFExporter.DEFAULT_SERIALIZATION_FUNCTION.apply(o, project, eStructuralFeature);
                    if (serialized == null && o != null) {
                        // failed to serialize; taking the conservative approach and returning entire thing as null
                        return NullNode.getInstance();
                    }
                    arrayNode.add(serialized);
                }
            } catch (UnsupportedOperationException e) {
                e.printStackTrace();
                System.err.println("Object: " + object.getClass());
            }
            return arrayNode;
        }
        else if (object instanceof ValueSpecification) {
            return convert((ValueSpecification) object, project, true);
            //return fillValueSpecification((ValueSpecification) object);
        }
        else if (eStructuralFeature instanceof EReference && object instanceof EObject) {
            return EMFExporter.DEFAULT_SERIALIZATION_FUNCTION.apply(getEID(((EObject) object)), project, eStructuralFeature);
        }
        else if (object instanceof String) {
            return TextNode.valueOf((String) object);
        }
        else if (object instanceof Boolean) {
            return BooleanNode.valueOf((boolean) object);
        }
        else if (object instanceof Integer) {
            return IntNode.valueOf((Integer) object);
        }
        else if (object instanceof Double) {
            return DoubleNode.valueOf((Double) object);
        }
        else if (object instanceof Long) {
            return LongNode.valueOf((Long) object);
        }
        else if (object instanceof Short) {
            return ShortNode.valueOf((Short) object);
        }
        else if (object instanceof Float) {
            return FloatNode.valueOf((Float) object);
        }
        else if (object instanceof BigInteger) {
            return BigIntegerNode.valueOf((BigInteger) object);
        }
        else if (object instanceof BigDecimal) {
            return DecimalNode.valueOf((BigDecimal) object);
        }
        else if (object instanceof byte[]) {
            return BinaryNode.valueOf((byte[]) object);
        }
        else if (eStructuralFeature.getEType() instanceof EDataType) {
            return TextNode.valueOf(EcoreUtil.convertToString((EDataType) eStructuralFeature.getEType(), object));
            //return ((Enumerator) object).getLiteral();
        }
        // if we get here we have no idea what to do with this object
        return NullNode.getInstance();
    };

    private static final ExportFunction DEFAULT_E_STRUCTURAL_FEATURE_FUNCTION = (element, project, eStructuralFeature, objectNode) -> {
        if (!eStructuralFeature.isChangeable() || eStructuralFeature.isVolatile() || eStructuralFeature.isTransient() || eStructuralFeature.isUnsettable() || eStructuralFeature.isDerived() || eStructuralFeature.getName().startsWith("_")) {
            return EMFExporter.EMPTY_E_STRUCTURAL_FEATURE_FUNCTION.apply(element, project, eStructuralFeature, objectNode);
        }
        return EMFExporter.UNCHECKED_E_STRUCTURAL_FEATURE_FUNCTION.apply(element, project, eStructuralFeature, objectNode);
    };

    private static final ExportFunction UNCHECKED_E_STRUCTURAL_FEATURE_FUNCTION = (element, project, eStructuralFeature, objectNode) -> {
        Object value = element.eGet(eStructuralFeature);
        JsonNode serializedValue = DEFAULT_SERIALIZATION_FUNCTION.apply(value, project, eStructuralFeature);
        if (value != null && serializedValue == null) {
            System.err.println("[EMF] Failed to serialize " + eStructuralFeature + " for " + element + ": " + value + " - " + value.getClass());
            return objectNode;
        }

        String key = eStructuralFeature.getName();
        if (eStructuralFeature instanceof EReference && EObject.class.isAssignableFrom(((EReference) eStructuralFeature).getEReferenceType().getInstanceClass())
                && !ValueSpecification.class.isAssignableFrom(((EReference) eStructuralFeature).getEReferenceType().getInstanceClass())) {
            key += "Id" + (eStructuralFeature.isMany() ? "s" : "");
        }
        objectNode.put(key, serializedValue);
        return objectNode;
    };

    private static final ExportFunction EMPTY_E_STRUCTURAL_FEATURE_FUNCTION = (element, project, eStructuralFeature, objectNode) -> objectNode;

    private enum EStructuralFeatureOverride {
        ID(
                (element, project, eStructuralFeature, objectNode) -> eStructuralFeature == element.eClass().getEIDAttribute(),
                (element, project, eStructuralFeature, objectNode) -> {
                    /*if (element instanceof ValueSpecification && !(element instanceof TimeExpression)) {
                        return objectNode;
                    }*/
                    objectNode.put(MDKConstants.ID_KEY, getEID(element));
                    return objectNode;
                }
        ),
        OWNER(
                (element, project, eStructuralFeature, objectNode) -> UMLPackage.Literals.ELEMENT__OWNER == eStructuralFeature,
                (element, project, eStructuralFeature, objectNode) -> {
                    Element owner = element.getOwner();
                    /*if (element instanceof ValueSpecification || owner instanceof ValueSpecification) {
                        return objectNode;
                    }*/
                    //UNCHECKED_E_STRUCTURAL_FEATURE_FUNCTION.apply(element, project, UMLPackage.Literals.ELEMENT__OWNER, objectNode);
                    // safest way to prevent circular references, like with ValueSpecifications
                    objectNode.put(MDKConstants.OWNER_ID_KEY, element instanceof Model && project.getModels().stream().anyMatch(model -> element == model) ? Converters.getIProjectToIdConverter().apply(project.getPrimaryProject()) : getEID(owner));
                    return objectNode;
                }
        ),
        DIRECTED_RELATIONSHIP__SOURCE(
                (element, project, eStructuralFeature, objectNode) -> UMLPackage.Literals.DIRECTED_RELATIONSHIP__SOURCE == eStructuralFeature,
                (element, project, eStructuralFeature, objectNode) -> {
                    objectNode.set(MDKConstants.DERIVED_KEY_PREFIX + eStructuralFeature.getName() + MDKConstants.IDS_KEY_SUFFIX, DEFAULT_SERIALIZATION_FUNCTION.apply(element.eGet(eStructuralFeature), project, eStructuralFeature));
                    return objectNode;
                }
        ),
        DIRECTED_RELATIONSHIP__TARGET(
                (element, project, eStructuralFeature, objectNode) -> UMLPackage.Literals.DIRECTED_RELATIONSHIP__TARGET == eStructuralFeature,
                (element, project, eStructuralFeature, objectNode) -> {
                    objectNode.set(MDKConstants.DERIVED_KEY_PREFIX + eStructuralFeature.getName() + MDKConstants.IDS_KEY_SUFFIX, DEFAULT_SERIALIZATION_FUNCTION.apply(element.eGet(eStructuralFeature), project, eStructuralFeature));
                    return objectNode;
                }
        ),
        CONNECTOR__END(
                (element, project, eStructuralFeature, objectNode) -> eStructuralFeature == UMLPackage.Literals.CONNECTOR__END,
                (element, project, eStructuralFeature, objectNode) -> {
                    Connector connector = (Connector) element;
                    List<List<Object>> propertyPaths = connector.getEnd().stream()
                            .map(connectorEnd -> SysMLProfile.NestedConnectorEndStereotype.isInstance(connectorEnd) ? SysMLProfile.getInstance(connector).nestedConnectorEnd().getPropertyPath(connector) : null)
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
                    objectNode.set(MDKConstants.PROPERTY_PATH_IDS_KEY, DEFAULT_SERIALIZATION_FUNCTION.apply(propertyPaths, project, eStructuralFeature));

                    return DEFAULT_E_STRUCTURAL_FEATURE_FUNCTION.apply(element, project, eStructuralFeature, objectNode);
                }
        ),
        VALUE_SPECIFICATION__EXPRESSION(
                (element, project, eStructuralFeature, objectNode) -> eStructuralFeature == UMLPackage.Literals.VALUE_SPECIFICATION__EXPRESSION,
                /*(element, project, eStructuralFeature, objectNode) -> {
                    Expression expression = null;
                    Object object = element.eGet(UMLPackage.Literals.VALUE_SPECIFICATION__EXPRESSION);
                    if (object instanceof Expression) {
                        expression = (Expression) object;
                    }
                    objectNode.put(UMLPackage.Literals.VALUE_SPECIFICATION__EXPRESSION.getName() + MDKConstants.ID_KEY_SUFFIX, expression != null ? expression.getID() : null);
                    return objectNode;
                }*/
                EMPTY_E_STRUCTURAL_FEATURE_FUNCTION
        ),
        UML_CLASS(
                (element, project, eStructuralFeature, objectNode) -> eStructuralFeature == UMLPackage.Literals.CLASSIFIER__UML_CLASS || eStructuralFeature == UMLPackage.Literals.PROPERTY__UML_CLASS || eStructuralFeature == UMLPackage.Literals.OPERATION__UML_CLASS,
                EMPTY_E_STRUCTURAL_FEATURE_FUNCTION
        );

        private ExportPredicate predicate;
        private ExportFunction function;

        EStructuralFeatureOverride(ExportPredicate predicate, ExportFunction function) {
            this.predicate = predicate;
            this.function = function;
        }

        public ExportPredicate getPredicate() {
            return predicate;
        }

        public ExportFunction getFunction() {
            return function;
        }
    }

    @FunctionalInterface
    interface SerializationFunction {
        JsonNode apply(Object object, Project project, EStructuralFeature eStructuralFeature);
    }

    @FunctionalInterface
    interface ExportPredicate {
        boolean test(Element element, Project project, EStructuralFeature structuralFeature, ObjectNode objectNode);
    }

    @FunctionalInterface
    interface ExportFunction {
        ObjectNode apply(Element element, Project project, EStructuralFeature eStructuralFeature, ObjectNode objectNode);
    }
}
