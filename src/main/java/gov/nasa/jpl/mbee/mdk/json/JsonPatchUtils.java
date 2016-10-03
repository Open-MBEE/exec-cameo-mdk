package gov.nasa.jpl.mbee.mdk.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.diff.JsonDiff;
import com.github.fge.jsonpatch.operation.JsonPatchOperation;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by igomes on 9/26/16.
 */
public class JsonPatchUtils {
    public static boolean isEqual(String sourceJson, String targetJson) throws IOException {
        return isEqual(JacksonUtils.getObjectMapper().readTree(sourceJson), JacksonUtils.getObjectMapper().readTree(targetJson));
    }

    public static boolean isEqual(JsonNode source, JsonNode target) {
        if (source == null && target == null) {
            return true;
        }
        if (source == null || target == null) {
            return false;
        }
        JsonNode patch = JsonDiff.asJson(source, target);
        return isEqual(patch);
    }

    public static boolean isEqual(JsonNode patch) {
        return patch == null || patch.size() == 0;
    }

    public static JsonNode getDiffAsJson(JsonNode source, JsonNode target) {
        if (source == null && target == null) {
            return null;
        }
        if (source == null) {
            source = JacksonUtils.getObjectMapper().createObjectNode();
        }
        if (target == null) {
            target = JacksonUtils.getObjectMapper().createObjectNode();
        }
        return JsonDiff.asJson(source, target);
    }
}
