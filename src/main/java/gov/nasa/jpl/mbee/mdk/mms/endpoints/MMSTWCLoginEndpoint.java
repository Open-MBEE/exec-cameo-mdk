package gov.nasa.jpl.mbee.mdk.mms.endpoints;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.Consts;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;

public class MMSTWCLoginEndpoint extends MMSEndpoint {
    public MMSTWCLoginEndpoint(String baseUri) throws URISyntaxException {
        super(baseUri);
    }

    public static Builder builder() {
        return new LoginBuilder();
    }

    public static class LoginBuilder extends Builder {
        @Override
        public void prepareUriPath() {
            uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointType.LOGIN.getPath());
            uriBuilder.clearParameters();
        }

        @Override
        public HttpRequestBase build() throws IOException, URISyntaxException {
            configureUri();

            if (uriBuilder != null) {
                prepareUriPath();
                // build request
                URI requestDest = uriBuilder.build();
                HttpRequestBase request = new HttpGet(requestDest);
                request.addHeader("charset", (Consts.UTF_8).displayName());
                uriBuilder = null;
                return request;
            }
            return null;
        }
    }
}