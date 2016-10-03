package gov.nasa.jpl.mbee.mdk.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
                    return null;
                }
                int index = Integer.parseInt(key);
                if (index >= json.size()) {
                    return null;
                }
                json = json.get(index);
            }
            else {
                if (!json.has(key)) {
                    return null;
                }
                json = json.get(key);
            }
        }
        return json;
    }

    public static <R> R getAtPath(JsonNode json, String path, Function<JsonNode, R> function) {
        return function.apply(getAtPath(json, path));
    }
}
