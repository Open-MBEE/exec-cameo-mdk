package gov.nasa.jpl.mbee.mdk.mms.endpoints;

import java.net.URISyntaxException;

public class MMSRefEndpoint extends MMSRefsEndpoint {
    public MMSRefEndpoint(String baseUri) throws URISyntaxException {
        super(baseUri);
    }

    public static Builder builder() {
        return new RefBuilder();
    }

    public static class RefBuilder extends RefsBuilder {
        @Override
        public void prepareUriPath() {
            String suffix = getStringParam(MMSEndpointBuilderConstants.URI_REF_SUFFIX);

            if(suffix.isEmpty()) {
                throw new NullPointerException();
            }

            super.prepareUriPath();
            uriBuilder.setPath(uriBuilder.getPath() + "/" + suffix);
        }
    }
}
