package gov.nasa.jpl.mbee.mdk.mms.endpoints;

import java.net.URISyntaxException;

public class MMSCommitsEndpoint extends MMSRefEndpoint {
    public MMSCommitsEndpoint(String baseUri) throws URISyntaxException {
        super(baseUri);
    }

    @Override
    public void prepareUriPath() {
        uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointType.COMMITS.getPath());
        uriBuilder.clearParameters();
    }
}
