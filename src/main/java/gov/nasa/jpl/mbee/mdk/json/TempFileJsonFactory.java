package gov.nasa.jpl.mbee.mdk.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.JsonParserDelegate;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.options.MDKOptionsGroup;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

class TempFileJsonFactory extends MappingJsonFactory {

    public TempFileJsonFactory() {
        super();
    }

    public TempFileJsonFactory(ObjectMapper mapper) {
        super(mapper);
    }

    public TempFileJsonFactory(JsonFactory src, ObjectMapper mapper) {
        super(src, mapper);
    }

    @Override
    public JsonParser createParser(File file) throws IOException {
        return new TempFileJsonParser(super.createParser(file), file);
    }

    private static class TempFileJsonParser extends JsonParserDelegate {

        private final File file;

        public TempFileJsonParser(JsonParser d, File file) {
            super(d);
            this.file = file;
        }

        @Override
        public void close() throws IOException {
            if (file != null && !MDKOptionsGroup.getMDKOptions().isLogJson()) {
                if (!file.delete()) {
                    file.deleteOnExit();
                }
            }
            super.close();
        }
    }

}
