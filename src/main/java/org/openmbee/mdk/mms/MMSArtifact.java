package org.openmbee.mdk.mms;

import org.apache.http.entity.ContentType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public interface MMSArtifact {
    String getId();
    String getChecksum();
    InputStream getInputStream();
    ContentType getContentType();
    String getFileExtension();
}
