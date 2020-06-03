package gov.nasa.jpl.mbee.mdk.mms.endpoints;

import java.net.URISyntaxException;

public class MMSSearchEndpoint extends MMSRefEndpoint {
    public MMSSearchEndpoint(String baseUri) throws URISyntaxException {
        super(baseUri);
    }

    public static Builder builder() {
        return new SearchBuilder();
    }

    public static class SearchBuilder extends RefBuilder {
        @Override
        public void prepareUriPath() {
            super.prepareUriPath();
            uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointType.SEARCH.getPath());
        }
    }
}
