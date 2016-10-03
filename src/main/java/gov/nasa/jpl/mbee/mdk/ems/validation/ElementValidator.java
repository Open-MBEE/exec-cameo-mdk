package gov.nasa.jpl.mbee.mdk.ems.validation;

import com.fasterxml.jackson.databind.JsonNode;
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
    private Collection<Pair<Element, JSONObject>> clientElements;
    private Collection<JSONObject> serverElements;
    private final Project project;

    private ValidationSuite validationSuite = new ValidationSuite("Model Validation");
    private ValidationRule elementEquivalenceValidationRule = new ValidationRule("Element Equivalence", "Element shall be represented in MagicDraw and MMS equivalently.", ViolationSeverity.ERROR);
    private Map<String, Pair<Pair<Element, JSONObject>, JSONObject>> invalidElements = new LinkedHashMap<>();

    {
        validationSuite.addValidationRule(elementEquivalenceValidationRule);
    }

    public ElementValidator(Collection<Pair<Element, JSONObject>> clientElements, Collection<JSONObject> serverElements, Project project) {
        this.clientElements = clientElements;
        this.serverElements = serverElements;
        this.project = project;
    }

    public static Collection<Pair<Element, JSONObject>> buildElementPairs(Collection<Element> elements, Project project) {
        List<Pair<Element, JSONObject>> elementPairs = new ArrayList<>(elements.size());
        for (Element element : elements) {
            JSONObject jsonObject = Converters.getElementToJsonConverter().apply(element, project);
            if (jsonObject == null) {
                continue;
            }
            elementPairs.add(new Pair<>(element, jsonObject));
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
        Map<String, Pair<Element, JSONObject>> clientElementMap = clientElements.stream().collect(Collectors.toMap(pair -> Converters.getElementToIdConverter().apply(pair.getFirst()), Function.identity()));
        Map<String, JSONObject> serverElementMap = serverElements.stream().collect(Collectors.toMap(json -> (String) json.get(MDKConstants.SYSML_ID_KEY), Function.identity()));

        LinkedHashSet<String> elementKeySet = new LinkedHashSet<>();
        elementKeySet.addAll(clientElementMap.keySet());
        elementKeySet.addAll(serverElementMap.keySet());

        progressStatus.setDescription("Generating validation results for " + elementKeySet.size() + " element" + (elementKeySet.size() != 1 ? "s" : ""));
        progressStatus.setIndeterminate(false);
        progressStatus.setMax(elementKeySet.size());
        progressStatus.setCurrent(0);

        for (String id : elementKeySet) {
            Pair<Element, JSONObject> clientElement = clientElementMap.get(id);
            Element clientElementElement = clientElement != null ? clientElement.getFirst() : null;
            JSONObject clientElementJson = clientElement != null ? clientElement.getSecond() : null;
            JSONObject serverElement = serverElementMap.get(id);
            try {
                ValidationRuleViolation validationRuleViolation = null;
                if (clientElementJson == null && serverElement == null) {
                    continue;
                }
                else if (clientElementJson == null) {
                    String name = (String) serverElement.getOrDefault(MDKConstants.NAME_KEY, "<>");
                    if (name == null || name.isEmpty()) {
                        name = "<>";
                    }
                    validationRuleViolation = new ValidationRuleViolation(project.getPrimaryModel(), "[MISSING IN CLIENT] " + serverElement.getOrDefault(MDKConstants.TYPE_KEY, "Element") + " "
                            + name + " - " + serverElement.getOrDefault(MDKConstants.SYSML_ID_KEY, "<>"));
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
                    JsonNode source = JacksonUtils.getObjectMapper().readTree(clientElementJson.toJSONString());
                    JsonNode target = JacksonUtils.getObjectMapper().readTree(serverElement.toJSONString());
                    JsonNode patch = JsonDiffFunction.getInstance().apply(source, target);
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

    public Map<String, Pair<Pair<Element, JSONObject>, JSONObject>> getInvalidElements() {
        return invalidElements;
    }
}
