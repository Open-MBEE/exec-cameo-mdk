package gov.nasa.jpl.mbee.mdk.mms.endpoints;

import java.net.URISyntaxException;

public class MMSSyncEnableEndpoint extends MMSEndpoint {
    public MMSSyncEnableEndpoint(String baseUri) throws URISyntaxException {
        super(baseUri);
    }

    public static Builder builder() {
        return new MMSSyncEnableEndpoint.SyncEnableBuilder();
    }

    public static class SyncEnableBuilder extends Builder {
        @Override
        public void prepareUriPath() {
            uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointType.SYNCENABLE.getPath());
            uriBuilder.clearParameters();
        }
    }
}
