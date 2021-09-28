package gov.nasa.jpl.mbee.mdk.mms;

import org.apache.http.entity.ContentType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public interface MMSArtifact {
    String getId();
    String getChecksum();
    default InputStream getInputStream() {
        try {
            return new FileInputStream(getFile());
        } catch (FileNotFoundException e) {
            return null;
        }
    }
    File getFile() throws FileNotFoundException;
    ContentType getContentType();
    String getExtension();
}
