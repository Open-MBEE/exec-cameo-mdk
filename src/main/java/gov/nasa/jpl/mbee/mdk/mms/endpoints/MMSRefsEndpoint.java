package gov.nasa.jpl.mbee.mdk.mms.endpoints;

import java.net.URISyntaxException;

public class MMSRefsEndpoint extends MMSProjectEndpoint {
    public MMSRefsEndpoint(String baseUri) throws URISyntaxException {
        super(baseUri);
    }

    @Override
    public void prepareUriPath() {
        uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointType.REFS.getPath());
        uriBuilder.clearParameters();
    }
}
