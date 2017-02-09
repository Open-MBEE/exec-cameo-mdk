package gov.nasa.jpl.mbee.mdk.ems.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nomagic.actions.ActionsCategory;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.task.EmptyProgressStatus;
import com.nomagic.task.ProgressStatus;
import com.nomagic.task.RunnableWithProgress;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import gov.nasa.jpl.mbee.mdk.actions.ClipboardAction;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRule;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationRuleViolation;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ValidationSuite;
import gov.nasa.jpl.mbee.mdk.docgen.validation.ViolationSeverity;
import gov.nasa.jpl.mbee.mdk.ems.actions.CommitClientElementAction;
import gov.nasa.jpl.mbee.mdk.ems.actions.UpdateClientElementAction;
import gov.nasa.jpl.mbee.mdk.ems.json.JsonDiffFunction;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.json.JsonPatchUtils;
import gov.nasa.jpl.mbee.mdk.lib.Pair;

import java.text.NumberFormat;
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


        int notEquivalentCount = 0;
        int missinginClientCount = 0;
        int missingOnMmsCount = 0;

        for (String id : elementKeySet) {
            Pair<Element, ObjectNode> clientElement = clientElementMap.get(id);
            Element clientElementElement = clientElement != null ? clientElement.getFirst() : null;
            ObjectNode clientElementObjectNode = clientElement != null ? clientElement.getSecond() : null;
            ObjectNode serverElement = serverElementMap.get(id);
            ValidationRuleViolation validationRuleViolation = null;
            JsonNode diff = null;
            if (clientElementObjectNode == null && serverElement == null) {
                continue;
            }
            else if (clientElementObjectNode == null) {
                JsonNode typeJsonNode = serverElement.get(MDKConstants.TYPE_KEY);
                String type = typeJsonNode != null ? typeJsonNode.asText("Element") : "Element";
                JsonNode nameJsonNode = serverElement.get(MDKConstants.NAME_KEY);
                String name = nameJsonNode != null ? nameJsonNode.asText("<>") : "<>";

                validationRuleViolation = new ValidationRuleViolation(project.getPrimaryModel(), "[MISSING IN CLIENT] " + type + " " + name);
                missinginClientCount++;
            }
            else if (serverElement == null) {
                String name = "<>";
                if (clientElementElement instanceof NamedElement && ((NamedElement) clientElementElement).getName() != null && !((NamedElement) clientElementElement).getName().isEmpty()) {
                    name = ((NamedElement) clientElementElement).getName();
                }
                validationRuleViolation = new ValidationRuleViolation(clientElementElement, "[MISSING ON MMS] " + clientElementElement.getHumanType() + " " + name);
                missingOnMmsCount++;
            }
            else {
                diff = JsonDiffFunction.getInstance().apply(clientElementObjectNode, serverElement);
                if (!JsonPatchUtils.isEqual(diff)) {
                    String name = "<>";
                    if (clientElementElement instanceof NamedElement && ((NamedElement) clientElementElement).getName() != null && !((NamedElement) clientElementElement).getName().isEmpty()) {
                        name = ((NamedElement) clientElementElement).getName();
                    }
                    validationRuleViolation = new ValidationRuleViolation(clientElementElement, "[NOT EQUIVALENT] " + clientElementElement.getHumanType() + " " + name);
                    notEquivalentCount++;
                }
            }
            if (validationRuleViolation != null) {
                validationRuleViolation.addAction(new CommitClientElementAction(id, clientElementElement, clientElementObjectNode, project));
                validationRuleViolation.addAction(new UpdateClientElementAction(id, clientElementElement, serverElement, project));

                ActionsCategory copyActionsCategory = new ActionsCategory("COPY", "Copy...");
                copyActionsCategory.setNested(true);
                validationRuleViolation.addAction(copyActionsCategory);
                copyActionsCategory.addAction(new ClipboardAction("ID", id));
                if (clientElementElement != null) {
                    copyActionsCategory.addAction(new ClipboardAction("Element Hyperlink", "mdel://" + Converters.getElementToIdConverter().apply(clientElementElement)));
                }
                if (clientElementObjectNode != null) {
                    try {
                        copyActionsCategory.addAction(new ClipboardAction("Local JSON", JacksonUtils.getObjectMapper().writeValueAsString(clientElementObjectNode)));
                    } catch (JsonProcessingException ignored) {
                    }
                }
                if (serverElement != null) {
                    try {
                        copyActionsCategory.addAction(new ClipboardAction("MMS JSON", JacksonUtils.getObjectMapper().writeValueAsString(serverElement)));
                    } catch (JsonProcessingException ignored) {
                    }
                }
                if (diff != null) {
                    try {
                        copyActionsCategory.addAction(new ClipboardAction("Diff", JacksonUtils.getObjectMapper().writeValueAsString(diff)));
                    } catch (JsonProcessingException ignored) {
                    }
                }

                elementEquivalenceValidationRule.addViolation(validationRuleViolation);
                invalidElements.put(id, new Pair<>(clientElement, serverElement));
            }
            progressStatus.increase();
        }
        Application.getInstance().getGUILog().log("[INFO] --- Start MDK Element Validation Summary ---");
        Application.getInstance().getGUILog().log("[INFO] " + NumberFormat.getInstance().format(missinginClientCount) + " element" + (missinginClientCount != 1 ? "s are" : " is") + " missing in client.");
        Application.getInstance().getGUILog().log("[INFO] " + NumberFormat.getInstance().format(missingOnMmsCount) + " element" + (missingOnMmsCount != 1 ? "s are" : "is") + " missing on MMS.");
        Application.getInstance().getGUILog().log("[INFO] " + NumberFormat.getInstance().format(notEquivalentCount) + " element" + (notEquivalentCount != 1 ? "s are" : " is") + " not equivalent between client and MMS.");
        Application.getInstance().getGUILog().log("[INFO] ---  End MDK Element Validation Summary  ---");
    }

    public ValidationSuite getValidationSuite() {
        return validationSuite;
    }

    public Map<String, Pair<Pair<Element, ObjectNode>, ObjectNode>> getInvalidElements() {
        return invalidElements;
    }
}
