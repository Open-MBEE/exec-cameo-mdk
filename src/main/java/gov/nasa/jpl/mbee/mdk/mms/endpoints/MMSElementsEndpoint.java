package gov.nasa.jpl.mbee.mdk.mms.endpoints;

import java.net.URISyntaxException;

public class MMSElementsEndpoint extends MMSRefEndpoint {
    public MMSElementsEndpoint(String baseUri) throws URISyntaxException {
        super(baseUri);
    }

    public static Builder builder() {
        return new ElementsBuilder();
    }

    public static class ElementsBuilder extends RefBuilder {
        @Override
        public void prepareUriPath() {
            super.prepareUriPath();
            uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointType.ELEMENTS.getPath());
        }
    }
}
