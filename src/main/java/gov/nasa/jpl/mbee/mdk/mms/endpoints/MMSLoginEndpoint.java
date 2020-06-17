package gov.nasa.jpl.mbee.mdk.mms.endpoints;

import static gov.nasa.jpl.mbee.mdk.mms.MMSUtils.HttpRequestType.GET;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.http.Consts;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;

import gov.nasa.jpl.mbee.mdk.json.JacksonUtils;
import gov.nasa.jpl.mbee.mdk.mms.MMSUtils;

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
            uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointType.LOGIN.getPath());
            uriBuilder.clearParameters();
        }

        @Override
        public HttpRequestBase build() throws IOException, URISyntaxException {
            if (uriBuilder == null) {
                String baseUri = getStringParam(MMSEndpointBuilderConstants.URI_BASE_PATH);
                if (!baseUri.isEmpty()) {
                    uriBuilder = new URIBuilder(baseUri);
                }
            }

            if (uriBuilder != null) {
                prepareUriPath();
                // build request
                URI requestDest = uriBuilder.build();
                HttpRequestBase request;
                MMSUtils.HttpRequestType type = getHttpTypeParam();
                // bulk GETs are not supported in MMS, but bulk PUTs are. checking and and
                // throwing error here in case
                File sendData = getFileParam();
                if (type == GET && sendData != null) {
                    throw new IOException("GETs with body are not supported");
                }
                switch (type) {
                    case POST:
                        request = new HttpPost(requestDest);
                        request.addHeader(HTTP.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
                        ObjectNode credentials = JacksonUtils.getObjectMapper().createObjectNode();
                        credentials.put("username", getStringParam("username"));
                        credentials.put("password", getStringParam("password"));
                        StringEntity jsonData = new StringEntity(
                                JacksonUtils.getObjectMapper().writeValueAsString(credentials),
                                ContentType.APPLICATION_JSON);
                        ((HttpEntityEnclosingRequest) request).setEntity(jsonData);
                        break;
                    case GET:
                    default:
                        request = new HttpGet(requestDest);
                        break;
                }
                request.addHeader("charset", (Consts.UTF_8).displayName());
                uriBuilder = null;
                return request;
            }
            return null;
        }
    }
}
