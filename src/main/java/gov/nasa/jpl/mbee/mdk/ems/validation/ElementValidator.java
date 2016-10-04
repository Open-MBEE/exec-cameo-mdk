package gov.nasa.jpl.mbee.mdk.ems.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.task.EmptyProgressStatus;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRule;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ViolationSeverity;
import gov.nasa.jpl.mbee.mdk.ems.actions.UpdateClientElementAction;
import gov.nasa.jpl.mbee.mdk.ems.json.JsonDiffFunction;
import gov.nasa.jpl.mbee.mdk.ems.json.JsonEquivalencePredicate;
import gov.nasa.jpl.mbee.mdk.ems.actions.CommitClientElementAction;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.json.JsonPatchUtils;
import gov.nasa.jpl.mbee.mdk.lib.Pair;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by igomes on 9/26/16.
 */
public class ElementValidator implements RunnableWithProgress {
    private Collection<Pair<Element, ObjectNode>> clientElements;
    private Collection<ObjectNode> serverElements;
    private final Project project;

    private ValidationSuite validationSuite = new ValidationSuite("Model Validation");
    private ValidationRule elementEquivalenceValidationRule = new ValidationRule("Element Equivalence", "Element shall be represented in MagicDraw and MMS equivalently.", ViolationSeverity.ERROR);
    private Map<String, Pair<Pair<Element, ObjectNode>, ObjectNode>> invalidElements = new LinkedHashMap<>();

    {
        validationSuite.addValidationRule(elementEquivalenceValidationRule);
    }

    public ElementValidator(Collection<Pair<Element, ObjectNode>> clientElements, Collection<ObjectNode> serverElements, Project project) {
        this.clientElements = clientElements;
        this.serverElements = serverElements;
        this.project = project;
    }

    public static Collection<Pair<Element, ObjectNode>> buildElementPairs(Collection<Element> elements, Project project) {
        List<Pair<Element, ObjectNode>> elementPairs = new ArrayList<>(elements.size());
        for (Element element : elements) {
            ObjectNode objectNode = Converters.getElementToJsonConverter().apply(element, project);
            if (objectNode == null) {
                continue;
            }
            elementPairs.add(new Pair<>(element, objectNode));
        }
        return elementPairs;
    }

    @Override
    public void run(ProgressStatus progressStatus) {
        if (progressStatus == null) {
            progressStatus = EmptyProgressStatus.getDefault();
        }
        progressStatus.setDescription("Mapping element(s)");
        progressStatus.setIndeterminate(true);

        if (clientElements == null) {
            clientElements = new ArrayList<>(0);
        }
        if (serverElements == null) {
            serverElements = new ArrayList<>(0);
        }
        Map<String, Pair<Element, ObjectNode>> clientElementMap = clientElements.stream().collect(Collectors.toMap(pair -> Converters.getElementToIdConverter().apply(pair.getFirst()), Function.identity()));
        Map<String, ObjectNode> serverElementMap = serverElements.stream().filter(json -> json.has(MDKConstants.SYSML_ID_KEY) && json.get(MDKConstants.SYSML_ID_KEY).isTextual()).collect(Collectors.toMap(json -> json.get(MDKConstants.SYSML_ID_KEY).asText(), Function.identity()));

        LinkedHashSet<String> elementKeySet = new LinkedHashSet<>();
        elementKeySet.addAll(clientElementMap.keySet());
        elementKeySet.addAll(serverElementMap.keySet());

        progressStatus.setDescription("Generating validation results for " + elementKeySet.size() + " element" + (elementKeySet.size() != 1 ? "s" : ""));
        progressStatus.setIndeterminate(false);
        progressStatus.setMax(elementKeySet.size());
        progressStatus.setCurrent(0);

        for (String id : elementKeySet) {
            Pair<Element, ObjectNode> clientElement = clientElementMap.get(id);
            Element clientElementElement = clientElement != null ? clientElement.getFirst() : null;
            ObjectNode clientElementJson = clientElement != null ? clientElement.getSecond() : null;
            ObjectNode serverElement = serverElementMap.get(id);
            try {
                ValidationRuleViolation validationRuleViolation = null;
                if (clientElementJson == null && serverElement == null) {
                    continue;
                }
                else if (clientElementJson == null) {
                    JsonNode nameJsonNode = serverElement.get(MDKConstants.NAME_KEY);
                    String name = nameJsonNode != null ? nameJsonNode.asText("<>") : "<>";
                    JsonNode typeJsonNode = serverElement.get(MDKConstants.TYPE_KEY);
                    String type = typeJsonNode != null ? typeJsonNode.asText("Element") : "Element";
                    JsonNode sysmlIdNode = serverElement.get(MDKConstants.SYSML_ID_KEY);
                    String sysmlId = sysmlIdNode != null ? sysmlIdNode.asText("<>") : "<>";

                    validationRuleViolation = new ValidationRuleViolation(project.getPrimaryModel(), "[MISSING IN CLIENT] " + type + " "
                            + name + " - " + sysmlId);
                }
                else if (serverElement == null) {
                    String name = "<>";
                    if (clientElementElement instanceof NamedElement && ((NamedElement) clientElementElement).getName() != null && !((NamedElement) clientElementElement).getName().isEmpty()) {
                        name = ((NamedElement) clientElementElement).getName();
                    }
                    validationRuleViolation = new ValidationRuleViolation(clientElementElement, "[MISSING ON MMS] " + clientElementElement.getHumanType() + " "
                            + name + " - " + Converters.getElementToIdConverter().apply(clientElementElement));
                }
                else {
                    JsonNode patch = JsonDiffFunction.getInstance().apply(clientElementJson, serverElement);
                    if (!JsonPatchUtils.isEqual(patch)) {
                        String name = "<>";
                        if (clientElementElement instanceof NamedElement && ((NamedElement) clientElementElement).getName() != null && !((NamedElement) clientElementElement).getName().isEmpty()) {
                            name = ((NamedElement) clientElementElement).getName();
                        }
                        validationRuleViolation = new ValidationRuleViolation(clientElementElement, "[NOT EQUIVALENT] " + clientElementElement.getHumanType() + " "
                                + name + " - " + Converters.getElementToIdConverter().apply(clientElementElement) + ": " + JacksonUtils.getObjectMapper().writeValueAsString(patch));
                    }
                }
                if (validationRuleViolation != null) {
                    validationRuleViolation.addAction(new CommitClientElementAction(id, clientElementElement, clientElementJson, project));
                    validationRuleViolation.addAction(new UpdateClientElementAction(id, clientElementElement, serverElement, project));
                    elementEquivalenceValidationRule.addViolation(validationRuleViolation);
                    invalidElements.put(id, new Pair<>(clientElement, serverElement));
                }
            } catch (IOException e) {
                Application.getInstance().getGUILog().log("[ERROR] Unexpected error occurred. Aborting model validation. Check logs for stack trace.");
                e.printStackTrace();
                break;
            }

            progressStatus.increase();
        }
    }

    public ValidationSuite getValidationSuite() {
        return validationSuite;
    }

    public Map<String, Pair<Pair<Element, ObjectNode>, ObjectNode>> getInvalidElements() {
        return invalidElements;
    }
}
