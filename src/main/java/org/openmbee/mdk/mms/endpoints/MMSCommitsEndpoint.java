package org.openmbee.mdk.mms.endpoints;

import java.net.URISyntaxException;

public class MMSCommitsEndpoint extends MMSRefEndpoint {
    public MMSCommitsEndpoint(String baseUri) throws URISyntaxException {
        super(baseUri);
    }

    public static Builder builder() {
        return new CommitsBuilder();
    }

    public static class CommitsBuilder extends RefBuilder {
        @Override
        public void prepareUriPath() {
            super.prepareUriPath();
            uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointType.COMMITS.getPath());
        }
    }
}
