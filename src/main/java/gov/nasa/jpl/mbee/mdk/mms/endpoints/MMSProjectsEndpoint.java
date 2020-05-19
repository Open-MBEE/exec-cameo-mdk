package gov.nasa.jpl.mbee.mdk.mms.endpoints;

public class MMSProjectsEndpoint extends MMSEndpoint {
    public MMSProjectsEndpoint(String baseUri) {
        super(baseUri);
    }

    @Override
    public void prepareUriPath() {
        uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointConstants.PROJECTS_ENDPOINT);
        uriBuilder.clearParameters();
    }
}
