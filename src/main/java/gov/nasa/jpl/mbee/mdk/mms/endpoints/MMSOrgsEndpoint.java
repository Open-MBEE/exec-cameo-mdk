package gov.nasa.jpl.mbee.mdk.mms.endpoints;

import org.apache.http.client.methods.HttpRequestBase;

import java.io.IOException;
import java.net.URISyntaxException;

public class MMSOrgsEndpoint extends MMSEndpoint {
    public MMSOrgsEndpoint(String baseUri) throws URISyntaxException {
        super(baseUri);
    }

    public static Builder builder() {
        return new OrgsBuilder();
    }

    public static class OrgsBuilder extends Builder {
        @Override
        public void prepareUriPath() {
            uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointType.ORGS.getPath());
            uriBuilder.clearParameters();
        }
    }
}
