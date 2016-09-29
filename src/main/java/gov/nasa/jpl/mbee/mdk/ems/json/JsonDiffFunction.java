package gov.nasa.jpl.mbee.mdk.ems.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jsonpatch.diff.JsonDiff;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;

import java.util.Iterator;
import java.util.function.BiFunction;

/**
 * Created by igomes on 9/28/16.
 */
public class JsonDiffFunction implements BiFunction<JsonNode, JsonNode, JsonNode> {
    private static JsonDiffFunction INSTANCE;

    @Override
    public JsonNode apply(JsonNode source, JsonNode target) {
        if (source == null && target == null) {
            return null;
        }
        if (source == null) {
            source = JacksonUtils.getObjectMapper().createObjectNode();
        }
        if (target == null) {
            target = JacksonUtils.getObjectMapper().createObjectNode();
        }
        preProcessSourceAndTarget(source, target);
        return JsonDiff.asJson(source, target);
    }

    private static void preProcessSourceAndTarget(JsonNode source, JsonNode target) {
        if (!(source instanceof ObjectNode) || !(target instanceof ObjectNode)) {
            return;
        }
        ObjectNode sourceObjectNode = (ObjectNode) source;
        ObjectNode targetObjectNode = (ObjectNode) target;

        Iterator<String> targetKeyIterator = targetObjectNode.fieldNames();
        while (targetKeyIterator.hasNext()) {
            String targetKey = targetKeyIterator.next();
            if (targetKey.startsWith(MDKConstants.DERIVED_KEY_PREFIX) && !sourceObjectNode.has(targetKey)) {
                targetKeyIterator.remove();
            }
            // TODO Remove me once derived prefixed @donbot
            if (targetKey.equals("editable") || targetKey.equals("elasticid")) {
                targetKeyIterator.remove();
            }
        }
        if (targetObjectNode.has("type") && targetObjectNode.get("type").asText().equals("Model")) {
            targetObjectNode.remove(MDKConstants.OWNER_ID_KEY);
        }
    }

    public static JsonDiffFunction getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JsonDiffFunction();
        }
        return INSTANCE;
    }
}
