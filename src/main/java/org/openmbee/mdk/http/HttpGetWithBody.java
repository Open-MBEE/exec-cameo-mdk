package org.openmbee.mdk.http;

import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.methods.HttpPost;

import java.net.URI;

@NotThreadSafe
public class HttpGetWithBody extends HttpPost {
    public static final String METHOD_NAME = "GET";

    public HttpGetWithBody() {
        super();
    }

    public HttpGetWithBody(URI uri) {
        super(uri);
    }

    public HttpGetWithBody(String uri) {
        super(URI.create(uri));
    }

    @Override
    public String getMethod() {
        return "GET";
    }
}
