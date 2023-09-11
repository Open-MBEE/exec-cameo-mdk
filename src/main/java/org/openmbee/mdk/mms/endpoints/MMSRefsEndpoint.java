package org.openmbee.mdk.mms.endpoints;

import java.net.URISyntaxException;

public class MMSRefsEndpoint extends MMSProjectEndpoint {
    public MMSRefsEndpoint(String baseUri) throws URISyntaxException {
        super(baseUri);
    }

    public static Builder builder() {
        return new RefsBuilder();
    }

    public static class RefsBuilder extends ProjectBuilder {
        @Override
        public void prepareUriPath() {
            super.prepareUriPath();
            uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointType.REFS.getPath());
        }
    }
}
