package gov.nasa.jpl.mbee.mdk.ems;

import java.net.URI;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;

@NotThreadSafe
public class HttpDeleteWithBody extends HttpPost {
    public static final String METHOD_NAME = "DELETE";

    public HttpDeleteWithBody() {
        super();
    }

    public HttpDeleteWithBody(URI uri) {
        super(uri);
    }

    public HttpDeleteWithBody(String uri) {
        super(URI.create(uri));
    }

    public String getMethod() {
        return "DELETE";
    }
}