package org.openmbee.mdk.mms.endpoints;

import java.net.URISyntaxException;

public class MMSProjectsEndpoint extends MMSEndpoint {
    public MMSProjectsEndpoint(String baseUri) throws URISyntaxException {
        super(baseUri);
    }

    public static Builder builder() {
        return new ProjectsBuilder();
    }

    public static class ProjectsBuilder extends Builder {
        @Override
        public void prepareUriPath() {
            String path = uriBuilder.getPath();
            uriBuilder.setPath((path == null ? "" : path) + MMSEndpointType.PROJECTS.getPath());
            uriBuilder.clearParameters();
        }
    }
}
