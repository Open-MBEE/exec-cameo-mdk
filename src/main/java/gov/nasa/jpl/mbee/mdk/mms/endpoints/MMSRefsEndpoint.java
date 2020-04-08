package gov.nasa.jpl.mbee.mdk.mms.endpoints;

public class MMSRefsEndpoint extends MMSProjectEndpoint {
    public MMSRefsEndpoint(String baseUri) {
        super(baseUri);
    }

    @Override
    public void prepareUriPath() {
        uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointConstants.REFS_ENDPOINT);
        uriBuilder.clearParameters();
    }
}
