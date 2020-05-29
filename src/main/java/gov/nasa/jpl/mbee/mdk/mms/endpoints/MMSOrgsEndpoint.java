package gov.nasa.jpl.mbee.mdk.mms.endpoints;

import java.net.URISyntaxException;

public class MMSOrgsEndpoint extends MMSEndpoint {
    public MMSOrgsEndpoint(String baseUri) throws URISyntaxException {
        super(baseUri);
    }

    @Override
    public void prepareUriPath() {
        uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointType.ORGS.getPath());
        uriBuilder.clearParameters();
    }
}
