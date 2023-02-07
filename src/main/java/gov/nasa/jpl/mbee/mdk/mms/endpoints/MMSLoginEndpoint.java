package gov.nasa.jpl.mbee.mdk.mms.endpoints;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.http.Consts;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;

public class MMSLoginEndpoint extends MMSEndpoint {
    public MMSLoginEndpoint(String baseUri) throws URISyntaxException {
        super(baseUri);
    }

    public static Builder builder() {
        return new LoginBuilder();
    }

    public static class LoginBuilder extends Builder {
        @Override
        public void prepareUriPath() {
            String path = uriBuilder.getPath();
            uriBuilder.setPath((path == null ? "" : path) + MMSEndpointType.LOGIN.getPath());
            uriBuilder.clearParameters();
        }

        @Override
        public HttpRequestBase build() throws IOException, URISyntaxException {
            configureUri();

            if (uriBuilder != null) {
                prepareUriPath();
                // build request
                URI requestDest = uriBuilder.build();
                HttpRequestBase request = new HttpPost(requestDest);
                request.addHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
                request.addHeader("charset", (Consts.UTF_8).displayName());

                ObjectNode credentials = JacksonUtils.getObjectMapper().createObjectNode();
                credentials.put("username", getStringParam("username"));
                credentials.put("password", getStringParam("password"));
                StringEntity jsonData = new StringEntity(JacksonUtils.getObjectMapper().writeValueAsString(credentials),
                        ContentType.APPLICATION_JSON);
                ((HttpEntityEnclosingRequest) request).setEntity(jsonData);
                uriBuilder = null;
                return request;
            }
            return null;
        }
    }
}
