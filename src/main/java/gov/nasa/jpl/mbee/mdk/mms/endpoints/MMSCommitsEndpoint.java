package gov.nasa.jpl.mbee.mdk.mms.endpoints;

public class MMSCommitsEndpoint extends MMSRefEndpoint {
    public MMSCommitsEndpoint(String baseUri) {
        super(baseUri);
    }

    @Override
    public void prepareUriPath() {
        uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointConstants.COMMITS_ENDPOINT);
        uriBuilder.clearParameters();
    }
}
