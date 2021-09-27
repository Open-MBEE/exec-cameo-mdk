package gov.nasa.jpl.mbee.mdk.mms;

import org.apache.http.entity.ContentType;

import java.io.InputStream;

public interface MMSArtifact {
    String getId();
    String getChecksum();
    InputStream getInputStream();
    ContentType getContentType();
    String getExtension();
}
