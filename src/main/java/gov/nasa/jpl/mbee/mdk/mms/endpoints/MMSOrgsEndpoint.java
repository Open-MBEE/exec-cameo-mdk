package gov.nasa.jpl.mbee.mdk.mms.endpoints;

public class MMSOrgsEndpoint extends MMSEndpoint {
    public MMSOrgsEndpoint(String baseUri) {
        super(baseUri);
    }

    @Override
    public void prepareUriPath() {
        uriBuilder.setPath(uriBuilder.getPath() + "/orgs");
    }
}
