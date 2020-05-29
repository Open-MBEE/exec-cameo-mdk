package gov.nasa.jpl.mbee.mdk.mms.endpoints;

import java.net.URISyntaxException;

public class MMSSearchEndpoint extends MMSRefEndpoint {
    public MMSSearchEndpoint(String baseUri) throws URISyntaxException {
        super(baseUri);
    }

    @Override
    public void prepareUriPath() {
        uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointType.SEARCH.getPath());
        uriBuilder.clearParameters();
    }
}
