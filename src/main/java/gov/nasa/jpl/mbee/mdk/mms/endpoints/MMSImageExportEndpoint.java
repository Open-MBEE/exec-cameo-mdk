package gov.nasa.jpl.mbee.mdk.mms.endpoints;

import java.net.URISyntaxException;

public class MMSImageExportEndpoint extends MMSElementsEndpoint {
    public MMSImageExportEndpoint(String baseUri) {
        super(baseUri);
    }

    @Override
    public void prepareUriPath() {
        uriBuilder.setPath(uriBuilder.getPath() + MMSEndpointConstants.IMAGE_EXPORT_ENDPOINT);
        uriBuilder.clearParameters();
    }

    public void setImageFile(String id) throws URISyntaxException {
        String path = uriBuilder.getPath();
        int i = path.indexOf(MMSEndpointConstants.IMAGE_FILE_PLACEHOLDER);
        String convertedId = id.replace(".", "%2E");
        uriBuilder.setPath(path.substring(0, i) + convertedId + path.substring(i + MMSEndpointConstants.IMAGE_FILE_PLACEHOLDER.length()));
    }
}
