package org.openmbee.mdk.mms.endpoints;

import java.net.URISyntaxException;

public class MMSProjectEndpoint extends MMSProjectsEndpoint {
    public MMSProjectEndpoint(String baseUri) throws URISyntaxException {
        super(baseUri);
    }

    public static Builder builder() {
        return new ProjectBuilder();
    }

    public static class ProjectBuilder extends ProjectsBuilder {
        @Override
        public void prepareUriPath() throws NullPointerException {
            String suffix = getStringParam(MMSEndpointBuilderConstants.URI_PROJECT_SUFFIX);

            if(suffix.isEmpty()) {
                throw new NullPointerException();
            }

            super.prepareUriPath();
            uriBuilder.setPath(uriBuilder.getPath() + "/" + suffix);
        }
    }
}
