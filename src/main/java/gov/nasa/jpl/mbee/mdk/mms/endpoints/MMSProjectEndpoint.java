package gov.nasa.jpl.mbee.mdk.mms.endpoints;

public class MMSProjectEndpoint extends MMSProjectsEndpoint {
    public MMSProjectEndpoint(String baseUri) {
        super(baseUri);
    }

    @Override
    public void prepareUriPath() {
        uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointConstants.PROJECT_ENDPOINT);
        uriBuilder.clearParameters();
    }

    public void setProjectId(String id) {
        String path = uriBuilder.getPath();
        int i = path.indexOf(MMSEndpointConstants.PROJECT_ID_PLACEHOLDER);
        uriBuilder.setPath(path.substring(0, i) + id + path.substring(i + MMSEndpointConstants.PROJECT_ID_PLACEHOLDER.length()));
    }
}
