package gov.nasa.jpl.mbee.mdk.json;

import com.fasterxml.jackson.databind.ObjectMapper;

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
}
