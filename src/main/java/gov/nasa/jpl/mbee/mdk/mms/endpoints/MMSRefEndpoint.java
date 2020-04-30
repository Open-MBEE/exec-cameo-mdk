package gov.nasa.jpl.mbee.mdk.mms.endpoints;

public class MMSRefEndpoint extends MMSRefsEndpoint {
    public MMSRefEndpoint(String baseUri) {
        super(baseUri);
    }

    @Override
    public void prepareUriPath() {
        uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointConstants.REF_ENDPOINT);
        uriBuilder.clearParameters();
    }

    public void setRefId(String id) {
        String path = uriBuilder.getPath();
        int i = path.indexOf(MMSEndpointConstants.REF_ID_PLACEHOLDER);
        uriBuilder.setPath(path.substring(0, i) + id + path.substring(i + MMSEndpointConstants.REF_ID_PLACEHOLDER.length()));
    }
}
