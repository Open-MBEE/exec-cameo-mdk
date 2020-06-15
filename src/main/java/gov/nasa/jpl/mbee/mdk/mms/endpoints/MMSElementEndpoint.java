package gov.nasa.jpl.mbee.mdk.mms.endpoints;

import java.net.URISyntaxException;

public class MMSElementEndpoint extends MMSElementsEndpoint {
    public MMSElementEndpoint(String baseUri) throws URISyntaxException {
        super(baseUri);
    }

    public static Builder builder() {
        return new MMSElementEndpoint.ElementBuilder();
    }

    public static class ElementBuilder extends RefBuilder {
        @Override
        public void prepareUriPath() {
            String suffix = getStringParam(MMSEndpointBuilderConstants.URI_ELEMENT_SUFFIX);

            if(suffix.isEmpty()) {
                throw new NullPointerException();
            }

            super.prepareUriPath();
            uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointType.ELEMENT.getPath() + "/" + suffix);
        }
    }
}
