package gov.nasa.jpl.mbee.mdk.mms.json;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.function.BiPredicate;

/**
 * Created by igomes on 9/28/16.
 */
public class JsonEquivalencePredicate implements BiPredicate<JsonNode, JsonNode> {
    private static JsonEquivalencePredicate INSTANCE;

    @Override
    public boolean test(JsonNode source, JsonNode target) {
        if (source == null && target == null) {
            return true;
        }
        if (source == null || target == null) {
            return false;
        }
        JsonNode patch = JsonPatchFunction.getInstance().apply(source, target);
        return patch == null || patch.size() == 0;
    }

    public static JsonEquivalencePredicate getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JsonEquivalencePredicate();
        }
        return INSTANCE;
    }
}
