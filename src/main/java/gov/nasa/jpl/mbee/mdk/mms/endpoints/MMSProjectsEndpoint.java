package gov.nasa.jpl.mbee.mdk.mms.endpoints;

import java.net.URISyntaxException;

public class MMSProjectsEndpoint extends MMSEndpoint {
    public MMSProjectsEndpoint(String baseUri) throws URISyntaxException {
        super(baseUri);
    }

    @Override
    public void prepareUriPath() {
        uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointType.PROJECTS.getPath());
        uriBuilder.clearParameters();
    }
}
