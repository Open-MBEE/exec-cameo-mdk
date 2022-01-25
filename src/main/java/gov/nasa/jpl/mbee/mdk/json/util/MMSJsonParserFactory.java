package gov.nasa.jpl.mbee.mdk.json.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.databind.MappingJsonFactory;

import java.io.*;

public class MMSJsonParserFactory extends MappingJsonFactory {

    @Override
    public JsonParser createParser(File f) throws IOException, JsonParseException {
        // true, since we create InputStream from File
        IOContext ctxt = _createContext(f, true);
        InputStream in = new FileInputStream(f);
        MMSJsonParser parser = (MMSJsonParser) _createParser(_decorate(in, ctxt), ctxt);
        parser.configureClose(f);
        return parser;
    }

    @Override
    protected JsonParser _createParser(InputStream in, IOContext ctxt) throws IOException {
        return new MMSJsonParser(super._createParser(in, ctxt));
    }

    @Override
    protected JsonParser _createParser(Reader r, IOContext ctxt) throws IOException {
        return new MMSJsonParser(super._createParser(r, ctxt));
    }

    @Override
    protected JsonParser _createParser(char[] data, int offset, int len, IOContext ctxt, boolean recyclable) throws IOException {
        return new MMSJsonParser(super._createParser(data, offset, len, ctxt, recyclable));
    }

    @Override
    protected JsonParser _createParser(byte[] data, int offset, int len, IOContext ctxt) throws IOException {
        return new MMSJsonParser(super._createParser(data, offset, len, ctxt));
    }

    @Override
    protected JsonParser _createParser(DataInput input, IOContext ctxt) throws IOException {
        return new MMSJsonParser(super._createParser(input, ctxt));
    }
}
