package org.openmbee.mdk.mms.endpoints;

import java.net.URISyntaxException;

public class MMSValidateJwtToken extends MMSEndpoint {
    public MMSValidateJwtToken(String baseUri) throws URISyntaxException {
        super(baseUri);
    }

    public static Builder builder() {
        return new ValidateJwtTokenBuilder();
    }

    public static class ValidateJwtTokenBuilder extends Builder {
        @Override
        public void prepareUriPath() {
            String path = uriBuilder.getPath();
            uriBuilder.setPath((path == null ? "" : path) + MMSEndpointType.VALIDATETOKEN.getPath());
            uriBuilder.clearParameters();
        }
    }
}
