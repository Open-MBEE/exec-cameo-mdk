package gov.nasa.jpl.mbee.mdk.mms.endpoints;

public class MMSSearchEndpoint extends MMSRefEndpoint {
    public MMSSearchEndpoint(String baseUri) {
        super(baseUri);
    }

    @Override
    public void prepareUriPath() {
        uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointConstants.SEARCH_ENDPOINT);
        uriBuilder.clearParameters();
    }
}
