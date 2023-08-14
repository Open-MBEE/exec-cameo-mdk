package org.openmbee.mdk.mms.endpoints;

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
            String path = uriBuilder.getPath();
            uriBuilder.setPath((path == null ? "" : path) + MMSEndpointType.ORGS.getPath());
            uriBuilder.clearParameters();
        }
    }
}
