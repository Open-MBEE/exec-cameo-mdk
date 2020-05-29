package gov.nasa.jpl.mbee.mdk.mms.endpoints;

import java.net.URISyntaxException;

public class MMSElementsEndpoint extends MMSRefEndpoint {
    public MMSElementsEndpoint(String baseUri) throws URISyntaxException {
        super(baseUri);
    }

    @Override
    public void prepareUriPath() {
        uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointType.ELEMENTS.getPath());
        uriBuilder.clearParameters();
    }
}
