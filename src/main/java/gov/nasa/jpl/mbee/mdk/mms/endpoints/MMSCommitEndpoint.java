package gov.nasa.jpl.mbee.mdk.mms.endpoints;

import java.net.URISyntaxException;

public class MMSCommitEndpoint extends MMSProjectEndpoint {
    public MMSCommitEndpoint(String baseUri) throws URISyntaxException {
        super(baseUri);
    }

    @Override
    public void prepareUriPath() {
        uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointType.COMMIT.getPath());
        uriBuilder.clearParameters();
    }

    public void setCommitId(String id) throws URISyntaxException {
        replaceUriPlaceholder(MMSEndpointType.COMMIT_ID_PLACEHOLDER, id);
    }
}
