package gov.nasa.jpl.mbee.mdk.mms.endpoints;

import java.net.URISyntaxException;

public class MMSRefEndpoint extends MMSRefsEndpoint {
    public MMSRefEndpoint(String baseUri) throws URISyntaxException {
        super(baseUri);
    }

    @Override
    public void prepareUriPath() {
        uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointType.REF.getPath());
        uriBuilder.clearParameters();
    }

    public void setRefId(String id) throws URISyntaxException {
        replaceUriPlaceholder(MMSEndpointType.REF_ID_PLACEHOLDER, id);
    }
}
