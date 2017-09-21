package gov.nasa.jpl.mbee.mdk.mms.json;

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
public class JsonPatchFunction implements BiFunction<JsonNode, JsonNode, JsonNode> {
    private static JsonPatchFunction INSTANCE;

    @Override
    public JsonNode apply(JsonNode client, JsonNode server) {
        if (client == null && server == null) {
            return null;
        }
        client = client != null ? client.deepCopy() : JacksonUtils.getObjectMapper().createObjectNode();
        server = server != null ? server.deepCopy() : JacksonUtils.getObjectMapper().createObjectNode();
        preProcess(client, server);
        return JsonDiff.asJson(client, server);
    }

    public static void preProcess(JsonNode client, JsonNode server) {
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
        }
    }

    public static JsonPatchFunction getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JsonPatchFunction();
        }
        return INSTANCE;
    }
}
