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
    public JsonNode apply(JsonNode client, JsonNode server) {
        if (client == null && server == null) {
            return null;
        }
        if (client == null) {
            client = JacksonUtils.getObjectMapper().createObjectNode();
        }
        if (server == null) {
            server = JacksonUtils.getObjectMapper().createObjectNode();
        }
        preProcess(client, server);
        return JsonDiff.asJson(client, server);
    }

    private static void preProcess(JsonNode client, JsonNode server) {
        if (!(client instanceof ObjectNode) || !(server instanceof ObjectNode)) {
            return;
        }
        ObjectNode sourceObjectNode = (ObjectNode) client;
        ObjectNode targetObjectNode = (ObjectNode) server;

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
