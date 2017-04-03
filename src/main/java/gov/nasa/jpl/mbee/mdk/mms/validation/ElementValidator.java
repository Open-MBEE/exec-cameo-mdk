package gov.nasa.jpl.mbee.mdk.mms.validation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
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
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.json.JsonPatchUtils;
import gov.nasa.jpl.mbee.mdk.mms.actions.CommitClientElementAction;
import gov.nasa.jpl.mbee.mdk.mms.actions.UpdateClientElementAction;
import gov.nasa.jpl.mbee.mdk.mms.json.JsonDiffFunction;
import gov.nasa.jpl.mbee.mdk.validation.ValidationRule;
import gov.nasa.jpl.mbee.mdk.validation.ValidationRuleViolation;
import gov.nasa.jpl.mbee.mdk.validation.ValidationSuite;
import gov.nasa.jpl.mbee.mdk.validation.ViolationSeverity;
import gov.nasa.jpl.mbee.mdk.util.Pair;

import java.io.IOException;
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
    private Collection<JsonParser> serverElementParsers;
    private final Project project;
    private int notEquivalentCount = 0;
    private int missingInClientCount = 0;
    private int missingOnMmsCount = 0;


    private ValidationSuite validationSuite = new ValidationSuite("Model Validation");
    private ValidationRule elementEquivalenceValidationRule = new ValidationRule("Element Equivalence", "Element shall be represented in MagicDraw and MMS equivalently.", ViolationSeverity.ERROR);
    private Map<String, Pair<Pair<Element, ObjectNode>, ObjectNode>> invalidElements = new LinkedHashMap<>();

    {
        validationSuite.addValidationRule(elementEquivalenceValidationRule);
    }

    public ElementValidator(Collection<Pair<Element, ObjectNode>> clientElements, Collection<ObjectNode> serverElements, Collection<JsonParser> serverElementParsers, Project project) {
        this.clientElements = clientElements;
        this.serverElements = serverElements;
        this.serverElementParsers = serverElementParsers;
        this.project = project;
    }

    public ElementValidator(Collection<Pair<Element, ObjectNode>> clientElements, Collection<ObjectNode> serverElements, Project project) {
        this.clientElements = clientElements;
        this.serverElements = serverElements;
        this.serverElementParsers = null;
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
            clientElements = new LinkedList<>();
        }
        Map<String, Pair<Element, ObjectNode>> clientElementMap = clientElements.stream().collect(Collectors.toMap(pair -> Converters.getElementToIdConverter().apply(pair.getKey()), Function.identity()));

        // process the parsers against the lists, adding processed keys to processed sets in case of multiple returns
        Set<String> processedElementIds = new HashSet<>();
        try {
            for (JsonParser jsonParser : serverElementParsers) {
                JsonToken current = (jsonParser.getCurrentToken() == null ? jsonParser.nextToken() : jsonParser.getCurrentToken());
                if (current != JsonToken.START_OBJECT) {
                    throw new IOException("Unable to build object from JSON parser.");
                }
                while (current != JsonToken.END_OBJECT) {
                    if (jsonParser.getCurrentName() == null) {
                        current = jsonParser.nextToken();
                        continue;
                    }
                    String keyName = jsonParser.getCurrentName();
                    if (keyName.equals("elements")) {
                        jsonParser.nextToken();
                        current = jsonParser.nextToken();
                        JsonNode value;
                        while (current != JsonToken.END_ARRAY) {
                            if (current == JsonToken.START_OBJECT) {
                                String id;
                                ObjectNode currentServerElement = JacksonUtils.parseJsonObject(jsonParser);
                                if ((value = currentServerElement.get(MDKConstants.ID_KEY)) != null && value.isTextual()
                                        && !processedElementIds.contains(id = value.asText())) {
                                    //remove element from client and server maps if present, add appropriate validations already
                                    processedElementIds.add(id);
                                    Pair<Element, ObjectNode> currentClientElement = clientElementMap.remove(id);
                                    if (currentClientElement == null) {
                                        addMissingInClientViolation(currentServerElement);
                                    }
                                    else {
                                        addElementEquivalenceViolation(currentClientElement, currentServerElement);
                                    }
                                }
                            }
                            else {
                                // ignore
                            }
                            current = jsonParser.nextToken();
                        }
                    }
                    current = jsonParser.nextToken();
                }
            }
        } catch (IOException e) {
            // stuff
        }


        if (serverElements == null) {
            serverElements = new LinkedList<>();
        }
        Map<String, ObjectNode> serverElementMap = serverElements.stream().filter(json -> json.has(MDKConstants.ID_KEY) && json.get(MDKConstants.ID_KEY).isTextual()).filter(json -> !processedElementIds.contains(json.get(MDKConstants.ID_KEY).asText()))
                .collect(Collectors.toMap(json -> json.get(MDKConstants.ID_KEY).asText(), Function.identity()));

        LinkedHashSet<String> elementKeySet = new LinkedHashSet<>();
        elementKeySet.addAll(clientElementMap.keySet());
        elementKeySet.addAll(serverElementMap.keySet());

        progressStatus.setDescription("Generating validation results for " + elementKeySet.size() + " element" + (elementKeySet.size() != 1 ? "s" : ""));
        progressStatus.setIndeterminate(false);
        progressStatus.setMax(elementKeySet.size());
        progressStatus.setCurrent(0);

        for (String id : elementKeySet) {
            Pair<Element, ObjectNode> clientElement = clientElementMap.get(id);
            Element clientElementElement = clientElement != null ? clientElement.getKey() : null;
            ObjectNode clientElementObjectNode = clientElement != null ? clientElement.getValue() : null;
            ObjectNode serverElement = serverElementMap.get(id);

            if (clientElement.getKey() == null && serverElement == null) {
                continue;
            }
            else if (clientElement == null) {
                addMissingInClientViolation(serverElement);
            }
            else if (serverElement == null) {
                addMissingOnMmsViolation(clientElement);
            }
            else {
                addElementEquivalenceViolation(clientElement, serverElement);
            }
            progressStatus.increase();
        }
        Application.getInstance().getGUILog().log("[INFO] --- Start MDK Element Validation Summary ---");
        Application.getInstance().getGUILog().log("[INFO] " + NumberFormat.getInstance().format(missingInClientCount) + " element" + (missingInClientCount != 1 ? "s are" : " is") + " missing in client.");
        Application.getInstance().getGUILog().log("[INFO] " + NumberFormat.getInstance().format(missingOnMmsCount) + " element" + (missingOnMmsCount != 1 ? "s are" : "is") + " missing on MMS.");
        Application.getInstance().getGUILog().log("[INFO] " + NumberFormat.getInstance().format(notEquivalentCount) + " element" + (notEquivalentCount != 1 ? "s are" : " is") + " not equivalent between client and MMS.");
        Application.getInstance().getGUILog().log("[INFO] ---  End MDK Element Validation Summary  ---");
    }

    private void addMissingInClientViolation(ObjectNode serverElement) {
        missingInClientCount++;
        JsonNode idJsonNode = serverElement.get(MDKConstants.ID_KEY);
        String id = idJsonNode != null ? idJsonNode.asText() : "";
        JsonNode typeJsonNode = serverElement.get(MDKConstants.TYPE_KEY);
        String type = typeJsonNode != null ? typeJsonNode.asText("Element") : "Element";
        JsonNode nameJsonNode = serverElement.get(MDKConstants.NAME_KEY);
        String name = nameJsonNode != null ? nameJsonNode.asText("<>") : "<>";
        finishViolation(new ValidationRuleViolation(project.getPrimaryModel(), "[MISSING IN CLIENT] " + type + " " + name), id, null, serverElement, null);
    }

    private void addMissingOnMmsViolation(Pair<Element, ObjectNode> clientElement) {
        missingOnMmsCount++;
        String name = "<>";
        if (clientElement.getKey() instanceof NamedElement && ((NamedElement) clientElement.getKey()).getName() != null && !((NamedElement) clientElement.getKey()).getName().isEmpty()) {
            name = ((NamedElement) clientElement.getKey()).getName();
        }
        finishViolation(new ValidationRuleViolation(clientElement.getKey(), "[MISSING ON MMS] " + clientElement.getKey().getHumanType() + " " + name), clientElement.getKey().getID(), clientElement, null, null);
    }

    public void addElementEquivalenceViolation(Pair<Element, ObjectNode> clientElement, ObjectNode serverElement) {
        JsonNode diff = JsonDiffFunction.getInstance().apply(clientElement.getValue(), serverElement);
        if (!JsonPatchUtils.isEqual(diff)) {
            notEquivalentCount++;
            String name = "<>";
            if (clientElement.getKey() instanceof NamedElement && ((NamedElement) clientElement.getKey()).getName() != null && !((NamedElement) clientElement.getKey()).getName().isEmpty()) {
                name = ((NamedElement) clientElement.getKey()).getName();
            }
            finishViolation(new ValidationRuleViolation(clientElement.getKey(), "[NOT EQUIVALENT] " + clientElement.getKey().getHumanType() + " " + name), clientElement.getKey().getID(), clientElement, serverElement, diff);
        }
    }

    public void finishViolation(ValidationRuleViolation validationRuleViolation, String id, Pair<Element, ObjectNode> clientElement, ObjectNode serverElement, JsonNode diff) {
        validationRuleViolation.addAction(new CommitClientElementAction(id, clientElement != null ? clientElement.getKey() : null, clientElement != null ? clientElement.getValue() : null, project));
        validationRuleViolation.addAction(new UpdateClientElementAction(id, clientElement != null ? clientElement.getKey() : null, serverElement, project));

        ActionsCategory copyActionsCategory = new ActionsCategory("COPY", "Copy...");
        copyActionsCategory.setNested(true);
        validationRuleViolation.addAction(copyActionsCategory);
        copyActionsCategory.addAction(new ClipboardAction("ID", id));
        if (clientElement != null) {
            copyActionsCategory.addAction(new ClipboardAction("Element Hyperlink", "mdel://" + clientElement.getKey().getID()));
            try {
                copyActionsCategory.addAction(new ClipboardAction("Local JSON", JacksonUtils.getObjectMapper().writeValueAsString(clientElement.getValue())));
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

    public ValidationSuite getValidationSuite() {
        return validationSuite;
    }

    public Map<String, Pair<Pair<Element, ObjectNode>, ObjectNode>> getInvalidElements() {
        return invalidElements;
    }
}
