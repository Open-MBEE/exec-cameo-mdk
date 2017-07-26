package gov.nasa.jpl.mbee.mdk.model;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.expressions.ExpressionHelper;
import com.nomagic.magicdraw.expressions.ParameterizedExpression;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.CallBehaviorAction;
import com.nomagic.uml2.ext.magicdraw.actions.mdbasicactions.ValuePin;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ParameterDirectionKindEnum;
import com.nomagic.uml2.ext.magicdraw.commonbehaviors.mdbasicbehaviors.Behavior;
import gov.nasa.jpl.mbee.mdk.api.incubating.convert.Converters;
import gov.nasa.jpl.mbee.mdk.docgen.docbook.DocumentElement;
import org.apache.commons.lang3.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by igomes on 7/18/17.
 */
public class BehaviorQuery extends Query {
    private final CallBehaviorAction callBehaviorAction;

    public BehaviorQuery(CallBehaviorAction callBehaviorAction) {
        this.callBehaviorAction = callBehaviorAction;
    }

    @Override
    public List<DocumentElement> visit(boolean forViewEditor, String outputDir) {
        Behavior behavior = callBehaviorAction.getBehavior();
        ParameterizedExpression expression = ExpressionHelper.getBehaviorExpression(behavior);
        if (expression == null) {
            Application.getInstance().getGUILog().log("[WARNING] Could not build an expression from " + Converters.getElementToHumanNameConverter().apply(behavior) + ". Skipping query.");
            return Collections.emptyList();
        }

        Map<String, Object> inputs = behavior.getOwnedParameter().stream().filter(parameter -> ParameterDirectionKindEnum.IN.equals(parameter.getDirection()) || ParameterDirectionKindEnum.INOUT.equals(parameter.getDirection())).collect(Collectors.toMap(NamedElement::getName, parameter -> ObjectUtils.NULL, (v1, v2) -> v1, LinkedHashMap::new));
        // JDK-8148463
        inputs.replaceAll((k, v) -> !ObjectUtils.NULL.equals(v) ? v : null);
        Set<String> undefinedParameterNames = new LinkedHashSet<>(inputs.keySet());
        callBehaviorAction.getInput().stream().filter(input -> input instanceof ValuePin).map(input -> (ValuePin) input).forEach(valuePin -> {
            if (!inputs.containsKey(valuePin.getName())) {
                Application.getInstance().getGUILog().log("[WARNING] There is no behavior parameter of the same name as " + Converters.getElementToHumanNameConverter().apply(valuePin) + ". Ignoring its value for evaluation.");
                return;
            }
            inputs.put(valuePin.getName(), valuePin.getValue());
            undefinedParameterNames.remove(valuePin.getName());
        });

        Map<String, Object> defaultInputs = new LinkedHashMap<>();
        defaultInputs.put("context", this);
        defaultInputs.put("exposedElements", targets);
        defaultInputs.put("forViewEditor", forViewEditor);
        defaultInputs.put("outputDirectory", outputDir);
        for (Map.Entry<String, Object> input : defaultInputs.entrySet()) {
            if (!inputs.containsKey(input.getKey())) {
                continue;
            }
            inputs.put(input.getKey(), input.getValue());
            undefinedParameterNames.remove(input.getKey());
        }
        if (!undefinedParameterNames.isEmpty()) {
            Application.getInstance().getGUILog().log("[WARNING] The following parameters with direction \"" + ParameterDirectionKindEnum.IN.toString() + "\" of " + Converters.getElementToHumanNameConverter().apply(behavior) + " are undefined: " + Arrays.toString(undefinedParameterNames.toArray()) + ".");
        }

        Object result;
        try {
            result = ExpressionHelper.call(expression, inputs.values().toArray());
        } catch (Exception e) {
            Application.getInstance().getGUILog().log("[WARNING] An error occurred while executing a behavior expression. Skipping query. Reason: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
        if (result instanceof DocumentElement) {
            return Collections.singletonList((DocumentElement) result);
        }
        if (result instanceof Collection) {
            result = ((Collection<?>) result).toArray();
        }
        if (result instanceof Object[]) {
            Object[] objects = (Object[]) result;
            List<DocumentElement> documentElements = new ArrayList<>(objects.length);
            List<Object> skippedObjects = new ArrayList<>(objects.length);
            for (Object object : objects) {
                if (object instanceof DocumentElement) {
                    documentElements.add((DocumentElement) object);
                }
                else {
                    skippedObjects.add(object);
                }
            }
            if (!skippedObjects.isEmpty()) {
                Application.getInstance().getGUILog().log("[WARNING] Invalid object(s) returned from behavior expression. Expected a document element or a collection of document elements. Skipping the following objects: " + Arrays.toString(skippedObjects.toArray()));
            }
            return documentElements;
        }
        Application.getInstance().getGUILog().log("[WARNING] Invalid object returned from behavior expression: " + result + ". Expected a document element or a collection of document elements. Skipping query.");
        return Collections.emptyList();
    }
}
