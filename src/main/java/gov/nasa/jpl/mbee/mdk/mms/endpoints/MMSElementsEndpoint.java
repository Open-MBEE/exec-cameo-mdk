package gov.nasa.jpl.mbee.mdk.mms.endpoints;

public class MMSElementsEndpoint extends MMSRefEndpoint {
    public MMSElementsEndpoint(String baseUri) {
        super(baseUri);
    }

    @Override
    public void prepareUriPath() {
        uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointConstants.ELEMENTS_ENDPOINT);
        uriBuilder.clearParameters();
    }
}
