package gov.nasa.jpl.mbee.mdk.mms.endpoints;

import java.net.URISyntaxException;

public class MMSProjectEndpoint extends MMSProjectsEndpoint {
    public MMSProjectEndpoint(String baseUri) throws URISyntaxException {
        super(baseUri);
    }

    @Override
    public void prepareUriPath() {
        uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointType.PROJECT.getPath());
        uriBuilder.clearParameters();
    }

    public void setProjectId(String id) throws URISyntaxException {
        replaceUriPlaceholder(MMSEndpointType.PROJECT_ID_PLACEHOLDER, id);
    }
}
