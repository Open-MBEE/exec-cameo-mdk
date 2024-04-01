package org.openmbee.mdk.mms.validation;

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
import org.openmbee.mdk.actions.ClipboardAction;
import org.openmbee.mdk.api.incubating.MDKConstants;
import org.openmbee.mdk.api.incubating.convert.Converters;
import org.openmbee.mdk.json.JacksonUtils;
import org.openmbee.mdk.mms.actions.CommitClientElementAction;
import org.openmbee.mdk.mms.actions.ElementDiffAction;
import org.openmbee.mdk.mms.actions.UpdateClientElementAction;
import org.openmbee.mdk.mms.json.JsonPatchFunction;
import org.openmbee.mdk.util.Pair;
import org.openmbee.mdk.validation.ValidationRule;
import org.openmbee.mdk.validation.ValidationRuleViolation;
import org.openmbee.mdk.validation.ValidationSuite;
import org.openmbee.mdk.validation.ViolationSeverity;

import java.io.File;
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
    private final Project project;

    private Collection<File> serverElementFiles;

    private int notEquivalentCount = 0;
    private int missingInClientCount = 0;
    private int missingOnMmsCount = 0;


    private final ValidationSuite validationSuite;
    private ValidationRule elementEquivalenceValidationRule = new ValidationRule("Element Equivalence", "Element shall be represented in MagicDraw and MMS equivalently.", ViolationSeverity.ERROR);
    private Map<String, Pair<Pair<Element, ObjectNode>, ObjectNode>> invalidElements = new LinkedHashMap<>();

    public ElementValidator(String name, Collection<Pair<Element, ObjectNode>> clientElements, Collection<ObjectNode> serverElements, Project project, Collection<File> serverElementFiles) {
        this.clientElements = clientElements;
        this.serverElements = serverElements;
        this.project = project;
        this.serverElementFiles = serverElementFiles;

        validationSuite = new ValidationSuite(name);
        validationSuite.addValidationRule(elementEquivalenceValidationRule);
    }

    public ElementValidator(String name, Collection<Pair<Element, ObjectNode>> clientElements, Collection<ObjectNode> serverElements, Project project) {
        this(name, clientElements, serverElements, project, Collections.emptyList());
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

        Map<String, Pair<Element, ObjectNode>> clientElementMap;
        if(clientElements == null) {
            clientElements = new LinkedList<>();
            clientElementMap = new HashMap<>();
        } else {
            clientElementMap = clientElements.stream().collect(Collectors.toMap(pair -> Converters.getElementToIdConverter().apply(pair.getKey()), Function.identity(), (s, a) -> a));
        }

        Map<String, ObjectNode> serverElementMap;
        if(serverElements == null) {
            serverElements = new LinkedList<>();
            serverElementMap = new HashMap<>();
        } else {
            serverElementMap = serverElements.stream().filter(json -> json.has(MDKConstants.ID_KEY) && json.get(MDKConstants.ID_KEY).isTextual() && !json.get(MDKConstants.ID_KEY).asText().startsWith(MDKConstants.HIDDEN_ID_PREFIX)).collect(Collectors.toMap(json -> json.get(MDKConstants.ID_KEY).asText(), Function.identity()));

        }

        try {
            processServerElements(serverElementMap);
        } catch(IOException e) {
            e.printStackTrace();
            Application.getInstance().getGUILog().log("[ERROR] An error occurred when attempting to process elements from the server.");
        }

        LinkedHashSet<String> elementKeySet = new LinkedHashSet<>();
        elementKeySet.addAll(clientElementMap.keySet());
        elementKeySet.addAll(serverElementMap.keySet());

        progressStatus.setDescription("Generating validation results for " + elementKeySet.size() + " element" + (elementKeySet.size() != 1 ? "s" : ""));
        progressStatus.setIndeterminate(false);
        progressStatus.setMax(elementKeySet.size());
        progressStatus.setCurrent(0);

        for (String id : elementKeySet) {
            Pair<Element, ObjectNode> clientElement = clientElementMap.get(id);
            ObjectNode serverElement = serverElementMap.get(id);

            if ((clientElement == null || clientElement.getKey() == null) && serverElement == null) {
                continue;
            } else if (clientElement == null) {
                addMissingInClientViolation(serverElement);
            } else if (serverElement == null) {
                addMissingOnMmsViolation(clientElement);
            } else {
                addElementEquivalenceViolation(clientElement, serverElement);
            }
            progressStatus.increase();
        }
        Application.getInstance().getGUILog().log("[INFO] --- Start " + validationSuite.getName() + " Summary ---");
        Application.getInstance().getGUILog().log("[INFO] " + NumberFormat.getInstance().format(missingInClientCount) + " element" + (missingInClientCount != 1 ? "s are" : " is") + " missing in client.");
        Application.getInstance().getGUILog().log("[INFO] " + NumberFormat.getInstance().format(missingOnMmsCount) + " element" + (missingOnMmsCount != 1 ? "s are" : "is") + " missing on MMS.");
        Application.getInstance().getGUILog().log("[INFO] " + NumberFormat.getInstance().format(notEquivalentCount) + " element" + (notEquivalentCount != 1 ? "s are" : " is") + " not equivalent between client and MMS.");
        Application.getInstance().getGUILog().log("[INFO] ---  End " + validationSuite.getName() + " Summary  ---");
    }

    private void processServerElements(Map<String, ObjectNode> serverElementMap) throws IOException {
        // process the parsers against the lists, adding processed keys to processed sets in case of multiple returns
        for (File responseFile : serverElementFiles) {
            Map<String, List<ObjectNode>> parsedResponseObjects = JacksonUtils.parseResponseIntoObjects(responseFile, MDKConstants.ELEMENTS_NODE);
            List<ObjectNode> elementObjectsList = parsedResponseObjects.get(MDKConstants.ELEMENTS_NODE);
            if(elementObjectsList != null && !elementObjectsList.isEmpty()) {
                Set<ObjectNode> elementObjects = new HashSet<>(elementObjectsList);
                if(serverObjectsOnlyHasBins(elementObjects)) {
                    // solves edge case where first model validation incorrectly removes bins from project
                    removeServerObjectNodeUsingIdPrefix(elementObjects, MDKConstants.HOLDING_BIN_ID_PREFIX);
                }
                //Remove View Instances Bin
                removeServerObjectNodeUsingIdPrefix(elementObjects, MDKConstants.VIEW_INSTANCES_BIN_PREFIX);

                for(ObjectNode jsonObject : elementObjects) {
                    JsonNode idValue = jsonObject.get(MDKConstants.ID_KEY);
                    if(idValue != null && idValue.isTextual() && !idValue.asText().startsWith(MDKConstants.HIDDEN_ID_PREFIX) && !serverElementMap.containsKey(idValue.asText())) {
                        String id = idValue.asText();
                        serverElementMap.put(id, jsonObject);
                    }
                }
            }
        }
    }

    private boolean serverObjectsOnlyHasBins(Set<ObjectNode> serverObjectNodes) {
        if(serverObjectNodes.size() != 2) {
            return false;
        }

        for(ObjectNode o : serverObjectNodes) {
            if(o.get(MDKConstants.ID_KEY).asText() != null) {
                String idValue = o.get(MDKConstants.ID_KEY).asText();
                if(!idValue.startsWith(MDKConstants.HOLDING_BIN_ID_PREFIX) &&
                        !idValue.startsWith(MDKConstants.VIEW_INSTANCES_BIN_PREFIX)) {
                    return false;
                }
            }
        }

        return true;
    }

    private void removeServerObjectNodeUsingIdPrefix(Set<ObjectNode> serverObjectNodes, String idPrefix) {
        for(ObjectNode o : serverObjectNodes) {
            if(o.get(MDKConstants.ID_KEY).asText() != null) {
                String idValue = o.get(MDKConstants.ID_KEY).asText();
                if(idValue.startsWith(idPrefix)) {
                    serverObjectNodes.remove(o);
                    break;
                }
            }
        }
    }

    private void addMissingInClientViolation(ObjectNode serverElement) {
        missingInClientCount++;
        JsonNode idJsonNode = serverElement.get(MDKConstants.ID_KEY);
        String id = idJsonNode != null ? idJsonNode.asText() : "";
        JsonNode typeJsonNode = serverElement.get(MDKConstants.TYPE_KEY);
        String type = typeJsonNode != null ? typeJsonNode.asText("Element") : "Element";
        JsonNode nameJsonNode = serverElement.get(MDKConstants.NAME_KEY);
        String name = nameJsonNode != null ? nameJsonNode.asText("<>") : "<>";
        if (name.equals("<>") || name.equals("")) {
            name = "<"  + id + ">";
        }
        finishViolation(new ValidationRuleViolation(project.getPrimaryModel(), "[MISSING IN CLIENT] " + type + " " + name), id, null, serverElement, null);
    }

    private void addMissingOnMmsViolation(Pair<Element, ObjectNode> clientElement) {
        missingOnMmsCount++;
        String name = "<>";
        if (clientElement.getKey() instanceof NamedElement && ((NamedElement) clientElement.getKey()).getName() != null && !((NamedElement) clientElement.getKey()).getName().isEmpty()) {
            name = ((NamedElement) clientElement.getKey()).getName();
        }
        finishViolation(new ValidationRuleViolation(clientElement.getKey(), "[MISSING ON MMS] " + clientElement.getKey().getHumanType() + " " + name), clientElement.getKey().getLocalID(), clientElement, null, null);
    }

    public void addElementEquivalenceViolation(Pair<Element, ObjectNode> clientElement, ObjectNode serverElement) {
        JsonNode diff = JsonPatchFunction.getInstance().apply(clientElement.getValue(), serverElement);
        if (diff != null && diff.size() != 0) {
            notEquivalentCount++;
            String name = "<>";
            if (clientElement.getKey() instanceof NamedElement && ((NamedElement) clientElement.getKey()).getName() != null && !((NamedElement) clientElement.getKey()).getName().isEmpty()) {
                name = ((NamedElement) clientElement.getKey()).getName();
            }
            finishViolation(new ValidationRuleViolation(clientElement.getKey(), "[NOT EQUIVALENT] " + clientElement.getKey().getHumanType() + " " + name), clientElement.getKey().getLocalID(), clientElement, serverElement, diff);
        }
    }

    public void finishViolation(ValidationRuleViolation validationRuleViolation, String id, Pair<Element, ObjectNode> clientElement, ObjectNode serverElement, JsonNode diff) {
        validationRuleViolation.addAction(new CommitClientElementAction(id, clientElement != null ? clientElement.getKey() : null, clientElement != null ? clientElement.getValue() : null, serverElement, project));
        validationRuleViolation.addAction(new UpdateClientElementAction(id, clientElement != null ? clientElement.getKey() : null, serverElement, project) {
            @Override
            protected ValidationRuleViolation getEditableValidationRuleViolation(Element element, ObjectNode objectNode, String sysmlId) {
                if (element != null && !element.isEditable()) {
                    if (objectNode == null) {
                        return new ValidationRuleViolation(element, "[DELETE FAILED] " + element.getHumanName() + " is not editable.");
                    }
                    return new ValidationRuleViolation(!project.isDisposed(element) ? element : project.getPrimaryModel(),
                            "[" + (!project.isDisposed(element) ? "UPDATE" : "CREATE") + " FAILED] " + (project.isDisposed(element) ? (sysmlId != null ? sysmlId : "<>") : element.getHumanName()) + " is not editable.");
                }
                return null;
            }
        });
        if (clientElement != null && clientElement.getValue() != null && serverElement != null && diff != null) {
            JsonNode client = clientElement.getValue().deepCopy();
            JsonNode server = serverElement.deepCopy();
            JsonPatchFunction.preProcess(client, server);
            validationRuleViolation.addAction(new ElementDiffAction(client, server, diff, project));
        }

        ActionsCategory copyActionsCategory = new ActionsCategory("COPY", "Copy...");
        copyActionsCategory.setNested(true);
        validationRuleViolation.addAction(copyActionsCategory);
        copyActionsCategory.addAction(new ClipboardAction("ID", id));
        if (clientElement != null) {
            copyActionsCategory.addAction(new ClipboardAction("Element Hyperlink", "mdel://" + clientElement.getKey().getLocalID()));
            try {
                copyActionsCategory.addAction(new ClipboardAction("Local JSON", JacksonUtils.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(clientElement.getValue())));
            } catch (JsonProcessingException ignored) {
            }
        }
        if (serverElement != null) {
            try {
                copyActionsCategory.addAction(new ClipboardAction("MMS JSON", JacksonUtils.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(serverElement)));
            } catch (JsonProcessingException ignored) {
            }
        }
        if (diff != null) {
            try {
                copyActionsCategory.addAction(new ClipboardAction("Diff", JacksonUtils.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(diff)));
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
