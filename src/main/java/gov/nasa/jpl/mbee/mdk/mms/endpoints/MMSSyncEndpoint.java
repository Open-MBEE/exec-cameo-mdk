package gov.nasa.jpl.mbee.mdk.mms.endpoints;

import java.net.URISyntaxException;

public class MMSSyncEndpoint extends MMSEndpoint {
    public MMSSyncEndpoint(String baseUri) throws URISyntaxException {
        super(baseUri);
    }

    public static Builder builder() {
        return new MMSSyncEndpoint.SyncBuilder();
    }

    public static class SyncBuilder extends Builder {
        @Override
        public void prepareUriPath() {
            uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointType.BACKGROUNDSYNC.getPath());
            uriBuilder.clearParameters();
        }
    }
}
