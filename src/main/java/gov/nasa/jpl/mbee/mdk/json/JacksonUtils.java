package gov.nasa.jpl.mbee.mdk.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gov.nasa.jpl.mbee.mdk.lib.MDUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.util.function.Function;

/**
 * Created by igomes on 9/26/16.
 */
public class JacksonUtils {
    private static ObjectMapper OBJECT_MAPPER_INSTANCE;

    public static ObjectMapper getObjectMapper() {
        if (OBJECT_MAPPER_INSTANCE == null) {
            OBJECT_MAPPER_INSTANCE = new ObjectMapper();
            if (MDUtils.isDeveloperMode()) {
                OBJECT_MAPPER_INSTANCE.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
                OBJECT_MAPPER_INSTANCE.enable(SerializationFeature.INDENT_OUTPUT);
            }
        }
        return OBJECT_MAPPER_INSTANCE;
    }

    public static JsonNode getAtPath(JsonNode json, String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        for (String key : path.split("/")) {
            key = key.replace("~0", "~").replace("~1", "/");
            if (json.isArray()) {
                if (!NumberUtils.isDigits(key)) {
                    return NullNode.getInstance();
                }
                int index = Integer.parseInt(key);
                if (index >= json.size()) {
                    return NullNode.getInstance();
                }
                json = json.get(index);
            }
            else {
                if (!json.has(key)) {
                    return NullNode.getInstance();
                }
                json = json.get(key);
            }
        }
        return json;
    }

    public static <R> R getAtPath(JsonNode json, String path, Function<JsonNode, R> function) {
        return function.apply(getAtPath(json, path));
    }

    @Deprecated
    public static void put(ObjectNode objectNode, String fieldName, Object object) {
        if (object == null) {
            objectNode.putNull(fieldName);
        }
        else if (object instanceof JsonNode) {
            objectNode.set(fieldName, (JsonNode) object);
        }
        else if (object instanceof String) {
            objectNode.put(fieldName, (String) object);
        }
        else if (object instanceof Boolean) {
            objectNode.put(fieldName, (Boolean) object);
        }
        else if (object instanceof Integer) {
            objectNode.put(fieldName, (Integer) object);
        }
        else if (object instanceof Double) {
            objectNode.put(fieldName, (Double) object);
        }
        else if (object instanceof Long) {
            objectNode.put(fieldName, (Long) object);
        }
        else if (object instanceof Short) {
            objectNode.put(fieldName, (Short) object);
        }
        else if (object instanceof Float) {
            objectNode.put(fieldName, (Float) object);
        }
        else if (object instanceof byte[]) {
            objectNode.put(fieldName, (byte[]) object);
        }
    }
}
