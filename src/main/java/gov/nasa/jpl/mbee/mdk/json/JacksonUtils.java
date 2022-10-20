package gov.nasa.jpl.mbee.mdk.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import gov.nasa.jpl.mbee.mdk.options.MDKEnvironmentOptionsGroup;
import gov.nasa.jpl.mbee.mdk.util.MDUtils;
import org.apache.commons.lang.math.NumberUtils;
import gov.nasa.jpl.mbee.mdk.api.incubating.MDKConstants;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

/**
 * Created by igomes on 9/26/16.
 */
public class JacksonUtils {
    private static ObjectMapper OBJECT_MAPPER_INSTANCE;
    private static JsonFactory JSON_FACTORY_INSTANCE;

    public static ObjectMapper getObjectMapper() {
        if (OBJECT_MAPPER_INSTANCE == null) {
            OBJECT_MAPPER_INSTANCE = new ObjectMapper();
            if (MDUtils.isDeveloperMode()) {
                OBJECT_MAPPER_INSTANCE.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
                if (MDKEnvironmentOptionsGroup.getInstance().isLogJson()) {
                    OBJECT_MAPPER_INSTANCE.enable(SerializationFeature.INDENT_OUTPUT);
                }
            }
        }
        return OBJECT_MAPPER_INSTANCE;
    }

    public static JsonFactory getJsonFactory() {
        if (JSON_FACTORY_INSTANCE == null) {
            JSON_FACTORY_INSTANCE = new TempFileJsonFactory(getObjectMapper());
        }
        return JSON_FACTORY_INSTANCE;
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

    public static ObjectNode parseJsonObject(JsonParser jsonParser) throws IOException {
        JsonToken current = (jsonParser.getCurrentToken() == null ? jsonParser.nextToken() : jsonParser.getCurrentToken());
        if (current != JsonToken.START_OBJECT) {
            throw new IOException("Unable to build object from JSON parser.");
        }
        return getObjectMapper().readTree(jsonParser);
    }

    public static ObjectNode parseJsonString(String string) throws IOException {
        JsonParser parser = JacksonUtils.getJsonFactory().createParser(string);
        return parseJsonObject(parser);
    }

    public static Map<String, Set<ObjectNode>> parseResponseIntoObjects(File responseFile, String expectedKey) throws IOException {
        JsonToken current;
        Map<String, Set<ObjectNode>> parsedResponseObjects = new HashMap<>();
        try(JsonParser jsonParser = JacksonUtils.getJsonFactory().createParser(responseFile)) {
            current = (jsonParser.getCurrentToken() == null ? jsonParser.nextToken() : jsonParser.getCurrentToken());
            if (current != JsonToken.START_OBJECT) {
                throw new IOException("Unable to build object from JSON parser.");
            }
            while (!jsonParser.isClosed() && current != JsonToken.END_OBJECT) {
                current = jsonParser.nextToken();
                String keyName;
                if(current != null) {
                    if(jsonParser.getCurrentName() != null) {
                        keyName = jsonParser.getCurrentName();
                        if(keyName.equals(expectedKey)) {
                            parsedResponseObjects.put(expectedKey, parseExpectedArray(jsonParser, current));
                        } else if(keyName.equals(MDKConstants.MESSAGES_NODE) ) {
                            parsedResponseObjects.put(MDKConstants.MESSAGES_NODE, parseExpectedArray(jsonParser, current));
                        } else if(keyName.equals(MDKConstants.REJECTED_NODE)) {
                            parsedResponseObjects.put(MDKConstants.REJECTED_NODE, parseExpectedArray(jsonParser, current));
                        } else if(keyName.equals("total") || keyName.equals("rejectedTotal")){
                            //TODO: fill in what to do with the totals
                        }
                        else {
                            throw new IOException("Unable to properly read this JSON format, check REST responses.");
                        }
                    }
                }
            }
        }
        return parsedResponseObjects;
    }

    private static Set<ObjectNode> parseExpectedArray(JsonParser jsonParser, JsonToken current) throws IOException {
        Set<ObjectNode> parsedObjects = new HashSet<>();
        if (current != null) { // assumes the calling method has begun initial parsing stages
            current = jsonParser.nextToken();
            if(current.equals(JsonToken.START_ARRAY)) {
                while (!jsonParser.isClosed() && current != JsonToken.END_ARRAY) {
                    if (current == JsonToken.START_OBJECT) {
                        ObjectNode currentJsonObject = JacksonUtils.parseJsonObject(jsonParser);
                        if (currentJsonObject != null) {
                            parsedObjects.add(currentJsonObject);
                        }
                    }
                    current = jsonParser.nextToken();
                }
            }
        }
        return parsedObjects;
    }
}
