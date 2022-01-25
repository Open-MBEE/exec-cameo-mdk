package gov.nasa.jpl.mbee.mdk.json.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.JsonParserDelegate;
import gov.nasa.jpl.mbee.mdk.options.MDKOptionsGroup;

import java.io.File;
import java.io.IOException;

public class MMSJsonParser extends JsonParserDelegate {

    private File responseFile;

    public MMSJsonParser(JsonParser parser) {
        super(parser);
    }

    @Override
    public void close() throws IOException {
        if (!MDKOptionsGroup.getMDKOptions().isLogJson()) {
            if(responseFile != null) {
                if(!responseFile.delete()) { // if we cannot immediately delete we'll get it later
                    responseFile.deleteOnExit();
                }
            }
        }
        super.close();
    }

    public void configureClose(File f) {
        responseFile = f;
    }
}
