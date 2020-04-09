package gov.nasa.jpl.mbee.mdk.mms.endpoints;

import java.net.URISyntaxException;

public class MMSCommitEndpoint extends MMSProjectEndpoint {
    public MMSCommitEndpoint(String baseUri) {
        super(baseUri);
    }

    @Override
    public void prepareUriPath() {
        uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointConstants.COMMIT_ENDPOINT);
        uriBuilder.clearParameters();
    }

    public void setCommitId(String id) throws URISyntaxException {
        String path = uriBuilder.getPath();
        int i = path.indexOf(MMSEndpointConstants.COMMIT_ID_PLACEHOLDER);
        uriBuilder.setPath(path.substring(0, i) + id + path.substring(i + MMSEndpointConstants.COMMIT_ID_PLACEHOLDER.length()));
    }
}
